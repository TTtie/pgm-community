package dev.pgm.community.moderation.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import dev.pgm.community.CommunityCommand;
import dev.pgm.community.moderation.feature.ModerationFeature;
import dev.pgm.community.moderation.punishments.Punishment;
import dev.pgm.community.moderation.punishments.PunishmentType;
import dev.pgm.community.moderation.punishments.types.ExpirablePunishment;
import dev.pgm.community.usernames.UsernameService;
import dev.pgm.community.utils.BroadcastUtils;
import dev.pgm.community.utils.CommandAudience;
import dev.pgm.community.utils.MessageUtils;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import net.kyori.text.TranslatableComponent;
import net.kyori.text.event.HoverEvent;
import net.kyori.text.event.HoverEvent.Action;
import net.kyori.text.format.TextColor;
import org.bukkit.Bukkit;
import tc.oc.pgm.util.chat.Sound;
import tc.oc.pgm.util.named.NameStyle;
import tc.oc.pgm.util.text.PeriodFormats;
import tc.oc.pgm.util.text.TextFormatter;
import tc.oc.pgm.util.text.formatting.PaginatedComponentResults;
import tc.oc.pgm.util.text.types.PlayerComponent;

public class PunishmentCommand extends CommunityCommand {

  public static final Duration DEFAULT_TEMPBAN_LENGTH = Duration.ofDays(7); // TODO: Maybe config?

  public static final Sound PARDON_ALERT = new Sound("note.harp", 1f, 1.5f);

  @Dependency private ModerationFeature moderation;
  @Dependency private UsernameService usernames;

  @CommandAlias("punish|p")
  @Description("Issue the most appropriate punishment for a player")
  @Syntax("[player] [reason]")
  @CommandCompletion("@players")
  @CommandPermission("TODO.punishment")
  public void punish(CommandAudience audience, OnlinePlayer target, String reason) {
    moderation
        .getNextPunishment(target.getPlayer().getUniqueId())
        .thenAcceptAsync(
            type -> {
              Duration length = type == PunishmentType.TEMP_BAN ? DEFAULT_TEMPBAN_LENGTH : null;
              moderation.punish(
                  type,
                  target.getPlayer().getUniqueId(),
                  audience,
                  reason,
                  length,
                  true,
                  isVanished(audience));
            });
  }

  @CommandAlias("punishmenthistory|ph")
  @Description("View a list of recent punishments")
  @Syntax("[page]")
  @CommandPermission("TODO.punishment")
  public void viewRecentPunishments(CommandAudience audience, @Default("1") int page) {
    sendPunishmentHistory(audience, null, moderation.getRecentPunishments(), page);
    // FIXME: Bug, currently we cache punishments to allow for this command to work. However
    // punishments are not updated if /pardon is done on an entry
    // Maybe see if we should fetch ALL punishments everytime sorted by time, or simply see if we
    // can modify the cached entries to match updates performed
  }

  @CommandAlias("repeatpunishment|rp")
  @Description("Repeat the last punishment you performed for another player")
  @Syntax("[player]")
  @CommandCompletion("@players")
  @CommandPermission("TODO.punishment")
  public void repeatPunishment(
      CommandAudience audience, @co.aikar.commands.annotation.Optional OnlinePlayer target) {
    audience
        .getId()
        .ifPresent(
            id -> {
              Optional<Punishment> last = moderation.getLastPunishment(id);
              if (last.isPresent()) {
                Punishment lastPunishment = last.get();
                PunishmentType type = lastPunishment.getType();
                String reason = lastPunishment.getReason();
                Duration length = ExpirablePunishment.getDuration(lastPunishment);

                if (target != null) {
                  moderation.punish(
                      type,
                      target.getPlayer().getUniqueId(),
                      audience,
                      reason,
                      length,
                      true,
                      isVanished(audience));
                } else {
                  // No target supplied, show last punishment
                  Component lastPunishMsg =
                      TextComponent.builder()
                          .append("Last punishment: ", TextColor.GRAY)
                          .append(lastPunishment.formatBroadcast(usernames))
                          .build();

                  audience.sendMessage(lastPunishMsg);
                }
              } else {
                audience.sendMessage(
                    TextComponent.of(
                        "You have not issued any recent punishments",
                        TextColor.RED)); // TODO: Translate
              }
            });
  }

  @CommandAlias("unban|pardon|forgive")
  @Description("Pardon all active punishments for a player")
  @Syntax("[player]")
  @CommandCompletion("*")
  @CommandPermission("TODO.punishment")
  public void unbanPlayer(CommandAudience audience, String target) {
    moderation
        .isBanned(target)
        .thenAcceptAsync(
            isBanned -> {
              if (isBanned) {
                moderation
                    .pardon(target, audience.getId())
                    .thenAcceptAsync(
                        pardon -> {
                          if (!pardon) {
                            audience.sendWarning(
                                TextComponent.builder()
                                    .append(target, TextColor.DARK_AQUA)
                                    .append(" could not be ", TextColor.GRAY)
                                    .append("unbanned")
                                    .color(TextColor.RED)
                                    .build());
                          } else {
                            BroadcastUtils.sendAdminChat(
                                TextComponent.builder()
                                    .append(target, TextColor.DARK_AQUA)
                                    .append(" was unbanned by ", TextColor.GRAY)
                                    .append(audience.getStyledName())
                                    .build(),
                                PARDON_ALERT);
                          }
                          // TODO: translate
                        });
              } else {
                audience.sendWarning(
                    TextComponent.builder()
                        .append(target, TextColor.AQUA)
                        .append(" has no active bans", TextColor.GRAY)
                        .build());
              }
            });
  }

  @CommandAlias("lookup|l")
  @Description("View infraction history of a player")
  @Syntax("[player] [page]")
  @CommandCompletion("@players")
  @CommandPermission("TODO.punishment") // TODO: Permission
  public void viewPunishmentHistory(
      CommandAudience audience, String target, @Default("1") int page) {
    moderation
        .query(target)
        .thenAcceptAsync(punishments -> sendPunishmentHistory(audience, target, punishments, page));
  }

  public void sendPunishmentHistory(
      CommandAudience audience, String target, Collection<Punishment> punishmentData, int page) {
    Component headerResultCount =
        TextComponent.of(Long.toString(punishmentData.size()), TextColor.RED);

    int perPage = 7;
    int pages = (punishmentData.size() + perPage - 1) / perPage;
    page = Math.max(1, Math.min(page, pages));

    Component pageNum =
        TranslatableComponent.of(
            "command.simplePageHeader",
            TextColor.GRAY,
            TextComponent.of(Integer.toString(page), TextColor.RED),
            TextComponent.of(Integer.toString(pages), TextColor.RED));

    Component targetName =
        target != null
            ? PlayerComponent.of(Bukkit.getPlayer(target), target, NameStyle.FANCY)
            : TextComponent.empty();

    Component header =
        TextComponent.builder()
            .append(TranslatableComponent.of("moderation.records.history", TextColor.GRAY))
            .append(
                target != null
                    ? TextComponent.builder()
                        .append(" - ", TextColor.DARK_GRAY)
                        .append(targetName)
                        .build()
                    : TextComponent.empty())
            .append(
                TextComponent.of(" (")
                    .append(headerResultCount)
                    .append(TextComponent.of(") » "))
                    .append(pageNum))
            .color(TextColor.GRAY)
            .build();

    Component formattedHeader =
        TextFormatter.horizontalLineHeading(audience.getSender(), header, TextColor.DARK_GRAY);
    new PaginatedComponentResults<Punishment>(formattedHeader, perPage) {
      @Override
      public Component format(Punishment data, int index) {
        TextComponent.Builder builder = TextComponent.builder();

        // Punishments that can be removed (bans / mutes), show a small status indicator
        if (data.getType().canRescind()) {
          Component status = data.isActive() ? MessageUtils.ACCEPT : MessageUtils.DENY;
          builder
              .append(
                  status.hoverEvent(
                      HoverEvent.showText(
                          TextComponent.of(
                              data.isActive()
                                  ? "Punishment is active"
                                  : "Punishment is no longer active",
                              TextColor.GRAY))))
              .append(" ");
        } else {
          builder.append(MessageUtils.WARNING).append(" ");
        }

        builder.append(data.formatBroadcast(usernames));
        TextComponent.Builder hover = TextComponent.builder();
        hover
            .append("Issued ", TextColor.GRAY)
            .append(
                PeriodFormats.relativePastApproximate(data.getTimeIssued())
                    .color(TextColor.YELLOW));

        Duration length = ExpirablePunishment.getDuration(data);
        // When a punishments can expire, show expire time on hover
        if (length != null) {
          Instant endDate = data.getTimeIssued().plus(length);
          if (data.isActive()) {
            hover
                .append("\n")
                .append("Expires in ", TextColor.GRAY)
                .append(
                    PeriodFormats.briefNaturalApproximate(data.getTimeIssued(), endDate, 1)
                        .color(TextColor.YELLOW));
          } else if (!data.wasUpdated()) {
            hover
                .append("\n")
                .append("Expired ", TextColor.GRAY)
                .append(PeriodFormats.relativePastApproximate(endDate).color(TextColor.YELLOW));
          }
        }

        if (data.wasUpdated()) {
          hover
              .append("\n")
              .append("Infraction lifted by ", TextColor.GRAY) // TODO: translate
              .append(usernames.renderUsername(data.getLastUpdatedBy()))
              .append(" ")
              .append(
                  PeriodFormats.relativePastApproximate(data.getLastUpdated())
                      .color(TextColor.YELLOW));
        }

        return builder.hoverEvent(HoverEvent.of(Action.SHOW_TEXT, hover.build())).build();
      }

      @Override
      public Component formatEmpty() {
        return target != null
            ? TranslatableComponent.of(
                "moderation.records.lookupNone",
                TextColor.RED,
                TextComponent.of(target, TextColor.AQUA))
            : TextComponent.of("There have been no recent punishments", TextColor.RED);
      }
    }.display(
        audience.getAudience(),
        punishmentData.stream().sorted().collect(Collectors.toList()),
        page);
  }
}
