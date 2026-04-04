package dev.pgm.community.alts.feature;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static tc.oc.pgm.util.player.PlayerComponent.player;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.pgm.community.CommunityPermissions;
import dev.pgm.community.alts.AltRiskConfig;
import dev.pgm.community.alts.AltRiskLevel;
import dev.pgm.community.alts.AltRiskSignal;
import dev.pgm.community.alts.AltRiskSignalType;
import dev.pgm.community.alts.AltRiskSummary;
import dev.pgm.community.alts.services.AltRiskService;
import dev.pgm.community.feature.FeatureBase;
import dev.pgm.community.moderation.feature.ModerationFeature;
import dev.pgm.community.sessions.feature.SessionFeature;
import dev.pgm.community.users.feature.UsersFeature;
import dev.pgm.community.utils.BroadcastUtils;
import dev.pgm.community.utils.Sounds;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.Nullable;
import tc.oc.pgm.util.named.NameStyle;

public class AltRiskFeature extends FeatureBase {

  private record CacheKey(UUID playerId, @Nullable String ip) {}

  private final Cache<CacheKey, AltRiskSummary> cache;
  private final AltRiskService service;
  private final UsersFeature users;

  public AltRiskFeature(
      Configuration config,
      Logger logger,
      UsersFeature users,
      SessionFeature sessions,
      ModerationFeature moderation) {
    super(new AltRiskConfig(config), logger, "Alt Risk");
    AltRiskConfig altRiskConfig = getAltRiskConfig();
    this.cache = CacheBuilder.newBuilder()
        .expireAfterWrite(altRiskConfig.getCacheDuration().toSeconds(), TimeUnit.SECONDS)
        .build();
    this.users = users;
    this.service = new AltRiskService(altRiskConfig, users, sessions, moderation);

    if (getConfig().isEnabled()) {
      enable();
    }
  }

  public AltRiskConfig getAltRiskConfig() {
    return (AltRiskConfig) getConfig();
  }

  @Override
  public void disable() {
    super.disable();
    cache.invalidateAll();
  }

  public @Nullable AltRiskSummary getCachedSummary(UUID playerId) {
    Player online = Bukkit.getPlayer(playerId);
    if (online != null) {
      String ip = online.getAddress().getAddress().getHostAddress();
      AltRiskSummary cached = cache.getIfPresent(new CacheKey(playerId, ip));
      if (cached != null) return cached;
    }
    return cache.getIfPresent(new CacheKey(playerId, null));
  }

  public CompletableFuture<AltRiskSummary> analyze(UUID playerId) {
    Player online = Bukkit.getPlayer(playerId);
    String ip = online != null ? online.getAddress().getAddress().getHostAddress() : null;
    return analyze(playerId, ip);
  }

  private CompletableFuture<AltRiskSummary> analyze(UUID playerId, @Nullable String currentIp) {
    CacheKey key = new CacheKey(playerId, currentIp);
    AltRiskSummary cached = cache.getIfPresent(key);
    if (cached != null) {
      return CompletableFuture.completedFuture(cached);
    }
    return service.analyze(playerId, currentIp).thenApplyAsync(summary -> {
      cache.put(key, summary);
      return summary;
    });
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    Player joiningPlayer = event.getPlayer();
    String address = joiningPlayer.getAddress().getAddress().getHostAddress();
    analyze(joiningPlayer.getUniqueId(), address).thenAcceptAsync(summary -> {
      if (!summary.requiresReview()) {
        return;
      }
      sendAlert(summary, joiningPlayer);
    });
  }

  public void sendAlert(AltRiskSummary summary, Player player) {
    Optional<AltRiskSignal> evasionSignal = summary.signals().stream()
        .filter(s -> s.type() == AltRiskSignalType.DIRECT_BAN_EVASION)
        .findFirst();

    if (evasionSignal.isEmpty()) {
      broadcastAlert(summary, player, null);
      return;
    }

    UUID bannedId = evasionSignal.get().linkedAccountId();
    users.getStoredProfile(bannedId).thenAcceptAsync(bannedProfile -> {
      String bannedName = bannedProfile != null ? bannedProfile.getUsername() : bannedId.toString();
      broadcastAlert(summary, player, bannedName);
    });
  }

  private void broadcastAlert(AltRiskSummary summary, Player player, String bannedName) {
    TextComponent.Builder message = text()
        .append(player(player, NameStyle.FANCY))
        .append(BroadcastUtils.BROADCAST_DIV)
        .append(text(summary.level().name(), summary.effectiveColor()))
        .append(text(" alt-risk ", NamedTextColor.GRAY))
        .append(text("(", NamedTextColor.GRAY))
        .append(text(summary.score(), summary.effectiveColor()))
        .append(text(")", NamedTextColor.GRAY));

    if (!summary.signals().isEmpty()) {
      AltRiskSignal topSignal = summary.signals().get(0);
      message
          .append(BroadcastUtils.BROADCAST_DIV)
          .append(text(topSignal.type().getLabel(), NamedTextColor.GRAY)
              .hoverEvent(
                  HoverEvent.showText(Component.text(topSignal.message(), NamedTextColor.GRAY))));
    }

    TextComponent.Builder alertBuilder = text()
        .append(message
            .hoverEvent(HoverEvent.showText(buildSignalHover(summary)))
            .clickEvent(ClickEvent.runCommand("/altscore " + player.getName()))
            .build());

    if (bannedName != null) {
      alertBuilder
          .append(text(" "))
          .append(text()
              .append(text("[", NamedTextColor.GRAY))
              .append(text("Ban", NamedTextColor.RED))
              .append(text("]", NamedTextColor.GRAY))
              .clickEvent(ClickEvent.runCommand(
                  "/ban " + player.getName() + " Ban Evasion - (" + bannedName + ")"))
              .hoverEvent(
                  HoverEvent.showText(text("Click to ban for ban evasion", NamedTextColor.RED))));
    }

    BroadcastUtils.sendAdminChatMessage(
        alertBuilder.build(),
        summary.level() == AltRiskLevel.HIGH ? Sounds.BAN_EVASION : null,
        CommunityPermissions.LOOKUP_OTHERS);
  }

  private Component buildSignalHover(AltRiskSummary summary) {
    TextComponent.Builder hover =
        text().append(text("Alt-Evasion Risk Signals", NamedTextColor.RED, TextDecoration.BOLD));
    for (AltRiskSignal signal : summary.signals()) {
      hover
          .append(newline())
          .append(text("  +" + signal.weight() + " ", NamedTextColor.GOLD))
          .append(Component.text(signal.message(), NamedTextColor.GRAY));
    }
    hover.append(newline()).append(text("Click to view full report", NamedTextColor.DARK_GRAY));
    return hover.build();
  }
}
