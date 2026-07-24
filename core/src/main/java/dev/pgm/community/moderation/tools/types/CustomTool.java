package dev.pgm.community.moderation.tools.types;

import static dev.pgm.community.util.PlayerUtils.PLAYER_UTILS;
import static net.kyori.adventure.text.Component.text;
import static tc.oc.pgm.util.text.TemporalComponent.duration;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.pgm.community.moderation.tools.CustomToolConfig;
import dev.pgm.community.moderation.tools.ToolBase;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;
import tc.oc.pgm.api.player.MatchPlayer;
import tc.oc.pgm.api.player.event.ObserverInteractEvent;
import tc.oc.pgm.util.Audience;

public class CustomTool extends ToolBase {

  private static final String SKULL_PREFIX = "skull:";
  private static final String PLAYER_VAR = "%player%";
  private static final String SENDER_VAR = "%sender%";

  private final CustomToolConfig config;
  private final Material material;
  private final @Nullable ItemStack skullItem;
  private final @Nullable Cache<UUID, Instant> cooldowns;

  private CustomTool(CustomToolConfig config, Material material, @Nullable ItemStack skullItem) {
    super(config.getSlot(), true);
    this.config = config;
    this.material = material;
    this.skullItem = skullItem;
    this.cooldowns = config.getCooldown() == null
        ? null
        : CacheBuilder.newBuilder()
            .expireAfterWrite(config.getCooldown().toMillis(), TimeUnit.MILLISECONDS)
            .build();
  }

  public static @Nullable CustomTool of(CustomToolConfig config, Logger logger) {
    if (!config.hasActions()) {
      logger.warning("Custom tool '" + config.getId() + "' has no actions defined, skipping");
      return null;
    }

    String materialValue = config.getMaterial();
    if (materialValue.toLowerCase().startsWith(SKULL_PREFIX)) {
      String url = resolveSkullUrl(materialValue.substring(SKULL_PREFIX.length()));
      if (url == null) {
        logger.warning(
            "Custom tool '" + config.getId() + "' has an invalid skull texture, skipping");
        return null;
      }
      ItemStack skull =
          PLAYER_UTILS.customSkull(url, config.getName(), config.getLore().toArray(new String[0]));
      return new CustomTool(config, skull.getType(), skull);
    }

    Material material = Material.matchMaterial(materialValue);
    if (material == null) {
      logger.warning("Custom tool '"
          + config.getId()
          + "' has an unknown material '"
          + materialValue
          + "', skipping");
      return null;
    }
    return new CustomTool(config, material, null);
  }

  private static @Nullable String resolveSkullUrl(String value) {
    if (value.isEmpty()) return null;
    if (value.startsWith("http://") || value.startsWith("https://")) {
      return value;
    }
    try {
      String decoded = new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
      JsonObject json = new Gson().fromJson(decoded, JsonObject.class);
      return json.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
    } catch (Exception e) {
      return null;
    }
  }

  public String getId() {
    return config.getId();
  }

  @Override
  public String getName() {
    return config.getName();
  }

  @Override
  public List<String> getLore() {
    return config.getLore();
  }

  @Override
  public Material getMaterial() {
    return material;
  }

  @Override
  public String getPermission() {
    return config.getPermission();
  }

  @Override
  public ItemStack getItem() {
    return skullItem != null ? skullItem.clone() : super.getItem();
  }

  @Override
  public void onLeftClick(ObserverInteractEvent event) {
    run(event, config.getLeftClick(), event.getClickedPlayer());
  }

  @Override
  public void onRightClick(ObserverInteractEvent event) {
    MatchPlayer clicked = event.getClickedPlayer();
    if (clicked != null && !config.getRightClickPlayer().isEmpty()) {
      run(event, config.getRightClickPlayer(), clicked);
      return;
    }
    run(event, config.getRightClick(), clicked);
  }

  private void run(
      ObserverInteractEvent event, List<String> commands, @Nullable MatchPlayer target) {
    if (commands.isEmpty()) return;

    Player sender = event.getPlayer().getBukkit();
    Audience viewer = Audience.get(sender);

    if (isOnCooldown(sender, viewer)) return;

    boolean executed = false;
    for (String command : commands) {
      if (command.contains(PLAYER_VAR) && target == null) {
        viewer.sendWarning(text("Click a player to use this tool"));
        continue;
      }

      String parsed = command;
      if (target != null) {
        parsed = parsed.replace(PLAYER_VAR, target.getBukkit().getName());
      }
      parsed = parsed.replace(SENDER_VAR, sender.getName());
      if (parsed.startsWith("/")) {
        parsed = parsed.substring(1);
      }

      Bukkit.dispatchCommand(config.isConsole() ? Bukkit.getConsoleSender() : sender, parsed);
      executed = true;
    }

    if (executed && cooldowns != null) {
      cooldowns.put(sender.getUniqueId(), Instant.now());
    }
  }

  private boolean isOnCooldown(Player sender, Audience viewer) {
    if (cooldowns == null || config.getCooldown() == null) return false;

    Instant lastUse = cooldowns.getIfPresent(sender.getUniqueId());
    if (lastUse == null) return false;

    Duration remaining = config.getCooldown().minus(Duration.between(lastUse, Instant.now()));
    if (remaining.isNegative() || remaining.isZero()) return false;

    viewer.sendWarning(text()
        .append(text("You must wait "))
        .append(duration(remaining, NamedTextColor.YELLOW))
        .append(text(" before using this again"))
        .build());
    return true;
  }
}
