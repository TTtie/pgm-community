package dev.pgm.community.audit;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static tc.oc.pgm.util.player.PlayerComponent.player;

import dev.pgm.community.feature.FeatureBase;
import dev.pgm.community.utils.BroadcastUtils;
import java.util.List;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import tc.oc.pgm.util.named.NameStyle;

public class CommandAuditFeature extends FeatureBase {

  public CommandAuditFeature(Configuration config, Logger logger) {
    super(new CommandAuditConfig(config), logger, "Staff Command Audit");

    if (getConfig().isEnabled()) {
      enable();
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
    Player player = event.getPlayer();
    if (!shouldAudit(player)) return;

    String command = event.getMessage();
    if (!shouldLog(command)) return;

    Component alert = buildAlert(player, command);
    BroadcastUtils.sendAdminChatMessage(alert, null, null, null);
  }

  private boolean shouldAudit(Player player) {
    CommandAuditConfig config = getCommandAuditConfig();
    if (hasAnyPermission(player, config.getExemptPermissions())) {
      return false;
    }
    List<String> auditPermissions = config.getAuditPermissions();
    return auditPermissions.isEmpty() || hasAnyPermission(player, auditPermissions);
  }

  private boolean shouldLog(String command) {
    if (command == null) return false;
    String normalized = command.trim().toLowerCase();
    if (normalized.isEmpty()) return false;

    CommandAuditConfig config = getCommandAuditConfig();
    if (matchesAny(normalized, config.getExcludeCommands())) return false;
    if (matchesAny(normalized, config.getIncludePrefixes())) return true;
    if (matchesAny(normalized, config.getIncludeCommands())) return true;
    return matchesCommandPermission(normalized, config.getIncludePermissionContains());
  }

  private boolean matchesCommandPermission(String command, List<String> permissionContains) {
    if (permissionContains.isEmpty()) return false;
    String label = extractCommandLabel(command);
    if (label.isEmpty()) return false;
    Command cmd = getCommand(label);
    if (cmd == null || cmd.getPermission() == null) return false;

    String permission = cmd.getPermission().toLowerCase();
    for (String matcher : permissionContains) {
      if (permission.contains(matcher)) {
        return true;
      }
    }
    return false;
  }

  private Command getCommand(String label) {
    PluginCommand pluginCommand = Bukkit.getPluginCommand(label);
    if (pluginCommand != null) {
      return pluginCommand;
    }

    try {
      CommandMap commandMap = Bukkit.getServer().getCommandMap();
      if (commandMap != null) {
        return commandMap.getCommand(label);
      }
    } catch (NoSuchMethodError ignored) {

    }
    return null;
  }

  private String extractCommandLabel(String command) {
    String[] parts = command.split(" ");
    if (parts.length == 0) return "";
    String label = parts[0];
    if (label.startsWith("/")) {
      label = label.substring(1);
    }
    int namespaceIndex = label.indexOf(':');
    if (namespaceIndex >= 0 && namespaceIndex + 1 < label.length()) {
      label = label.substring(namespaceIndex + 1);
    }
    return label;
  }

  private boolean matchesAny(String command, List<String> values) {
    for (String value : values) {
      if (command.startsWith(value)) return true;
    }
    return false;
  }

  private boolean hasAnyPermission(Player player, List<String> permissions) {
    for (String permission : permissions) {
      if (player.hasPermission(permission)) {
        return true;
      }
    }
    return false;
  }

  private Component buildAlert(Player player, String command) {
    TextComponent.Builder builder = text()
        .append(player(player, NameStyle.FANCY))
        .append(text(" executed ", NamedTextColor.GRAY))
        .append(text(command, NamedTextColor.LIGHT_PURPLE));

    if (getCommandAuditConfig().isClickTeleportEnabled()) {
      builder.hoverEvent(HoverEvent.showText(DEFAULT_HOVER));
      builder.append(space()).append(buildViewComponent(player));
    } else {
      builder.hoverEvent(HoverEvent.showText(DEFAULT_HOVER));
    }

    return builder.build();
  }

  private static final Component DEFAULT_HOVER = text(
          "You've been alerted to this action", NamedTextColor.GRAY)
      .append(newline())
      .append(text("in order to promote transparency.", NamedTextColor.GRAY));

  private Component buildViewComponent(Player player) {
    return text()
        .append(text("[", NamedTextColor.GRAY))
        .append(text("View", NamedTextColor.AQUA))
        .append(text("]", NamedTextColor.GRAY))
        .clickEvent(ClickEvent.runCommand(getTeleportCommand(player)))
        .hoverEvent(HoverEvent.showText(text("Click to teleport", NamedTextColor.GRAY)))
        .build();
  }

  private String getTeleportCommand(Player player) {
    return String.format(
        "/tploc %d %d %d",
        player.getLocation().getBlockX(),
        player.getLocation().getBlockY(),
        player.getLocation().getBlockZ());
  }

  public CommandAuditConfig getCommandAuditConfig() {
    return (CommandAuditConfig) getConfig();
  }
}
