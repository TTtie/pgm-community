package dev.pgm.community.audit;

import com.google.common.collect.Lists;
import dev.pgm.community.feature.config.FeatureConfigImpl;
import java.util.List;
import org.bukkit.configuration.Configuration;

public class CommandAuditConfig extends FeatureConfigImpl {

  private static final String KEY = "command-audit";

  private List<String> auditPermissions;
  private List<String> exemptPermissions;
  private List<String> includePrefixes;
  private List<String> includeCommands;
  private List<String> includePermissionContains;
  private List<String> excludeCommands;
  private boolean clickTeleport;

  public CommandAuditConfig(Configuration config) {
    super(KEY, config);
  }

  public List<String> getAuditPermissions() {
    return auditPermissions;
  }

  public List<String> getExemptPermissions() {
    return exemptPermissions;
  }

  public List<String> getIncludePrefixes() {
    return includePrefixes;
  }

  public List<String> getIncludeCommands() {
    return includeCommands;
  }

  public List<String> getIncludePermissionContains() {
    return includePermissionContains;
  }

  public List<String> getExcludeCommands() {
    return excludeCommands;
  }

  public boolean isClickTeleportEnabled() {
    return clickTeleport;
  }

  @Override
  public void reload(Configuration config) {
    super.reload(config);
    this.auditPermissions =
        normalizePermissionList(config.getStringList(KEY + ".audit-permissions"));
    this.exemptPermissions =
        normalizePermissionList(config.getStringList(KEY + ".exempt-permissions"));
    this.includePrefixes = normalizeCommandList(config.getStringList(KEY + ".include-prefixes"));
    this.includeCommands = normalizeCommandList(config.getStringList(KEY + ".include-commands"));
    this.includePermissionContains =
        normalizePermissionList(config.getStringList(KEY + ".include-permissions"));
    this.excludeCommands = normalizeCommandList(config.getStringList(KEY + ".exclude-commands"));
    this.clickTeleport = config.getBoolean(KEY + ".click-teleport");
  }

  private List<String> normalizeCommandList(List<String> list) {
    List<String> normalized = Lists.newArrayList();
    if (list == null) {
      return normalized;
    }
    for (String value : list) {
      if (value == null) continue;
      String trimmed = value.trim().toLowerCase();
      if (trimmed.isEmpty()) continue;
      if (!trimmed.startsWith("/")) {
        trimmed = "/" + trimmed;
      }
      normalized.add(trimmed);
    }
    return normalized;
  }

  private List<String> normalizePermissionList(List<String> list) {
    List<String> normalized = Lists.newArrayList();
    if (list == null) {
      return normalized;
    }
    for (String value : list) {
      if (value == null) continue;
      String trimmed = value.trim().toLowerCase();
      if (trimmed.isEmpty()) continue;
      normalized.add(trimmed);
    }
    return normalized;
  }
}
