package dev.pgm.community.party.events;

import dev.pgm.community.party.MapParty;
import org.bukkit.command.CommandSender;

public class MapPartyStartEvent extends MapPartyEvent {

  public MapPartyStartEvent(MapParty party, CommandSender sender) {
    super(party, sender);
  }
}
