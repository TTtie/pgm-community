package dev.pgm.community.party.history;

import dev.pgm.community.party.MapParty;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import tc.oc.pgm.api.match.Match;

public class MapPartyHistoryStore {

  private final List<MapPartyHistoryEntry> entries;
  private MapPartyHistoryEntry current;
  private int sequence;

  public MapPartyHistoryStore() {
    this.entries = new ArrayList<>();
    this.sequence = 0;
  }

  public void start(MapParty party) {
    Instant startTime = party.getStartTime() != null ? party.getStartTime() : Instant.now();
    current = new MapPartyHistoryEntry(++sequence, party, startTime);
  }

  public void addMatch(Match match) {
    if (current == null) return;
    current.addMatch(new MapPartyMatchEntry(match));
  }

  public void end(MapParty party, Instant endTime) {
    if (current == null) return;
    if (party != null) {
      current.updateFromParty(party);
    }
    current.end(endTime);
    entries.add(current);
    current = null;
  }

  public void restart(MapParty party, Instant endTime) {
    end(party, endTime);
    start(party);
  }

  public boolean hasAnyEndedEvents() {
    return !entries.isEmpty();
  }

  public Optional<MapPartyHistoryEntry> mostRecent() {
    return entries.stream().max(Comparator.comparing(MapPartyHistoryEntry::getEndTime));
  }

  public Optional<MapPartyHistoryEntry> getBySequence(int sequence) {
    return entries.stream().filter(entry -> entry.getSequence() == sequence).findFirst();
  }

  public List<MapPartyHistoryEntry> getEntriesDescending() {
    List<MapPartyHistoryEntry> sorted = new ArrayList<>(entries);
    sorted.sort(Comparator.comparing(MapPartyHistoryEntry::getEndTime).reversed());
    return sorted;
  }
}
