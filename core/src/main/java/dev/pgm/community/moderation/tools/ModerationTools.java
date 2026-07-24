package dev.pgm.community.moderation.tools;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import dev.pgm.community.Community;
import dev.pgm.community.moderation.ModerationConfig;
import dev.pgm.community.moderation.tools.types.CustomTool;
import dev.pgm.community.moderation.tools.types.LookupSign;
import dev.pgm.community.moderation.tools.types.ModerationMenuTool;
import dev.pgm.community.moderation.tools.types.TeleportHook;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import tc.oc.pgm.api.player.event.ObserverInteractEvent;

public class ModerationTools {

  private static final int MAX_SLOT = 35; // Player inventory: hotbar + main

  private final ModerationMenuTool menu;
  private final TeleportHook tpHook;
  private final LookupSign sign;
  private final List<CustomTool> customTools;

  public ModerationTools(ModerationConfig config) {
    // TODO: allow reloads to enable/disable built-in tools
    this.menu = new ModerationMenuTool(config.getModMenuSlot(), config.isModMenuEnabled());
    this.tpHook = new TeleportHook(config.getPlayerHookSlot(), config.isPlayerHookEnabled());
    this.sign = new LookupSign(config.getLookupSignSlot(), config.isLookupSignEnabled());
    this.customTools = Lists.newArrayList();
    reload(config);
  }

  public void reload(ModerationConfig config) {
    Logger logger = Community.get().getLogger();
    customTools.clear();

    Set<Integer> usedSlots = Sets.newHashSet();
    if (config.isModMenuEnabled()) usedSlots.add(config.getModMenuSlot());
    if (config.isPlayerHookEnabled()) usedSlots.add(config.getPlayerHookSlot());
    if (config.isLookupSignEnabled()) usedSlots.add(config.getLookupSignSlot());

    for (CustomToolConfig toolConfig : config.getCustomTools()) {
      if (!toolConfig.isEnabled()) continue;

      if (toolConfig.getSlot() < 0 || toolConfig.getSlot() > MAX_SLOT) {
        logger.warning("Custom tool '"
            + toolConfig.getId()
            + "' has an invalid slot ("
            + toolConfig.getSlot()
            + "), skipping");
        continue;
      }

      if (usedSlots.contains(toolConfig.getSlot())) {
        logger.warning("Custom tool '"
            + toolConfig.getId()
            + "' uses slot "
            + toolConfig.getSlot()
            + " which is already taken by another tool, skipping");
        continue;
      }

      CustomTool tool = CustomTool.of(toolConfig, logger);
      if (tool != null) {
        usedSlots.add(toolConfig.getSlot());
        customTools.add(tool);
      }
    }
  }

  public ModerationMenuTool getMenu() {
    return menu;
  }

  public TeleportHook getTeleportHook() {
    return tpHook;
  }

  public LookupSign getLookupSign() {
    return sign;
  }

  public void onInteract(ObserverInteractEvent event) {
    menu.onInteract(event);
    tpHook.onInteract(event);
    sign.onInteract(event);
    for (CustomTool custom : customTools) {
      custom.onInteract(event);
    }
  }

  public void giveTools(Player player) {
    menu.give(player);
    tpHook.give(player);
    sign.give(player);
    for (CustomTool custom : customTools) {
      custom.give(player);
    }
  }
}
