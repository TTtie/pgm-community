package dev.pgm.community.moderation.tools;

import static tc.oc.pgm.util.text.TextParser.parseDuration;

import dev.pgm.community.CommunityPermissions;
import java.time.Duration;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.jspecify.annotations.Nullable;

public class CustomToolConfig {

  private final String id;
  private final boolean enabled;
  private final int slot;
  private final String material;
  private final String name;
  private final List<String> lore;
  private final String permission;
  private final @Nullable Duration cooldown;
  private final boolean console;
  private final List<String> leftClick;
  private final List<String> rightClick;
  private final List<String> rightClickPlayer;

  private CustomToolConfig(String id, ConfigurationSection section) {
    this.id = id;
    this.enabled = section.getBoolean("enabled", true);
    this.slot = section.getInt("slot", -1);
    this.material = section.getString("material", "");
    this.name = section.getString("name", "&b" + id);
    this.lore = section.getStringList("lore");
    this.permission = section.getString("permission", CommunityPermissions.STAFF);
    this.cooldown = parseCooldown(section.getString("cooldown"));
    this.console = "console".equalsIgnoreCase(section.getString("run-as", "player"));

    ConfigurationSection actions = section.getConfigurationSection("actions");
    this.leftClick = getCommandList(actions, "left-click");
    this.rightClick = getCommandList(actions, "right-click");
    this.rightClickPlayer = getCommandList(actions, "right-click-player");
  }

  public static CustomToolConfig parse(String id, ConfigurationSection section) {
    return new CustomToolConfig(id, section);
  }

  private static @Nullable Duration parseCooldown(@Nullable String value) {
    if (value == null || value.isEmpty()) return null;
    try {
      Duration cooldown = parseDuration(value);
      return cooldown == null || cooldown.isNegative() || cooldown.isZero() ? null : cooldown;
    } catch (Exception e) {
      return null;
    }
  }

  private static List<String> getCommandList(@Nullable ConfigurationSection actions, String key) {
    if (actions == null) return List.of();
    List<String> commands = actions.getStringList(key);
    if (!commands.isEmpty()) return commands;
    String single = actions.getString(key);
    return single == null || single.isEmpty() ? List.of() : List.of(single);
  }

  public String getId() {
    return id;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public int getSlot() {
    return slot;
  }

  public String getMaterial() {
    return material;
  }

  public String getName() {
    return name;
  }

  public List<String> getLore() {
    return lore;
  }

  public String getPermission() {
    return permission;
  }

  public @Nullable Duration getCooldown() {
    return cooldown;
  }

  public boolean isConsole() {
    return console;
  }

  public List<String> getLeftClick() {
    return leftClick;
  }

  public List<String> getRightClick() {
    return rightClick;
  }

  public List<String> getRightClickPlayer() {
    return rightClickPlayer;
  }

  public boolean hasActions() {
    return !leftClick.isEmpty() || !rightClick.isEmpty() || !rightClickPlayer.isEmpty();
  }
}
