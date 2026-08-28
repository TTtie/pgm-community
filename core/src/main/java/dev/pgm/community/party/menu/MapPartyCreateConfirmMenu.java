package dev.pgm.community.party.menu;

import static tc.oc.pgm.util.bukkit.BukkitUtils.colorize;

import dev.pgm.community.party.feature.MapPartyFeature;
import dev.pgm.community.party.history.MapPartyHistoryEntry;
import dev.pgm.community.utils.compatibility.Materials;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.content.InventoryContents;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import tc.oc.pgm.util.inventory.ItemBuilder;
import tc.oc.pgm.util.text.TemporalComponent;
import tc.oc.pgm.util.text.TextTranslations;

public class MapPartyCreateConfirmMenu extends MapPartyMenu {

  private static final String TITLE = "&c&lConfirm Event Creation";
  private static final int ROWS = 6;
  private static final boolean HOST_ONLY = false;

  private final String forceCommand;
  private final String actionLabel;

  public MapPartyCreateConfirmMenu(
      MapPartyFeature feature, Player viewer, String forceCommand, String actionLabel) {
    super(feature, TITLE, ROWS, HOST_ONLY, viewer);
    this.forceCommand = forceCommand;
    this.actionLabel = actionLabel;
  }

  @Override
  public void init(Player player, InventoryContents contents) {
    contents.fillBorders(getBorderItem());

    ClickableItem noItem = ClickableItem.of(
        new ItemBuilder()
            .material(Materials.WOOL)
            .color(DyeColor.RED)
            .name(colorize("&c&lNo"))
            .lore(colorize("&7Cancel and return"))
            .build(),
        c -> {
          if (getParent() != null) {
            getParent().open();
          } else {
            close();
          }
        });

    ClickableItem yesItem = ClickableItem.of(
        new ItemBuilder()
            .material(Materials.WOOL)
            .color(DyeColor.LIME)
            .name(colorize("&a&lYes"))
            .lore(colorize("&7Force create this event"))
            .build(),
        c -> {
          Bukkit.dispatchCommand(getViewer(), forceCommand);
          if (getParent() != null) {
            getParent().open();
          } else {
            close();
          }
        });

    for (int row = 2; row <= 4; row++) {
      for (int col = 1; col <= 3; col++) {
        contents.set(row, col, noItem);
      }
      for (int col = 5; col <= 7; col++) {
        contents.set(row, col, yesItem);
      }
    }

    contents.set(
        1,
        4,
        ClickableItem.empty(new ItemBuilder()
            .material(Material.PAPER)
            .name(colorize(actionLabel))
            .lore(getDetailsLore())
            .build()));

    addBackButton(contents);
  }

  @Override
  public void update(Player player, InventoryContents contents) {}

  private String[] getDetailsLore() {
    List<String> lore = new ArrayList<>();
    lore.add(colorize("&7There was already an event hosted today!"));
    lore.add("");

    MapPartyHistoryEntry last = getFeature().getMostRecentHistory().orElse(null);
    if (last != null) {
      String styledName =
          LegacyComponentSerializer.legacyAmpersand().serialize(last.getStyledName());
      lore.add(colorize(styledName));
      lore.add(colorize(" &f- &7Ended &a"
          + TextTranslations.translateLegacy(
              TemporalComponent.relativePastApproximate(last.getEndTime()))));
      lore.add(colorize(" &f- &7Matches: &b" + last.getMatchCount()));
    } else {
      lore.add(colorize(" &f- &cNo prior events found"));
    }

    lore.add("");
    lore.add(colorize("&cAre you sure you want to host another?"));

    return lore.toArray(new String[0]);
  }
}
