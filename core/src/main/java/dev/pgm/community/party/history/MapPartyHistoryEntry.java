package dev.pgm.community.party.history;

import static net.kyori.adventure.text.Component.text;
import static tc.oc.pgm.util.text.TemporalComponent.duration;
import static tc.oc.pgm.util.text.TemporalComponent.relativePastApproximate;

import dev.pgm.community.party.MapParty;
import dev.pgm.community.party.MapPartyType;
import dev.pgm.community.party.hosts.MapPartyHosts;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public class MapPartyHistoryEntry {

  private static final DateTimeFormatter END_TIME_FORMAT =
      DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a z", Locale.ENGLISH);

  private final int sequence;
  private String name;
  private String description;
  private Component styledName;
  private MapPartyType type;
  private String mainHostName;
  private List<String> hostNames;
  private final Instant startTime;
  private Instant endTime;
  private Duration duration;
  private final List<MapPartyMatchEntry> matches;

  public MapPartyHistoryEntry(int sequence, MapParty party, Instant startTime) {
    this.sequence = sequence;
    updateFromParty(party);
    this.startTime = startTime;
    this.matches = new ArrayList<>();
  }

  private String resolveMainHostName(MapPartyHosts hosts) {
    UUID mainHostId = hosts.getMainHostId();
    String cached = hosts.getCachedName(mainHostId);
    return cached != null ? cached : mainHostId.toString();
  }

  public void addMatch(MapPartyMatchEntry entry) {
    matches.add(entry);
  }

  public void updateFromParty(MapParty party) {
    this.name = party.getName();
    String partyDescription = party.getDescription();
    this.description = partyDescription != null ? partyDescription : "No description";
    this.styledName = party.getStyledName();
    this.type = party.getEventType();
    this.mainHostName = resolveMainHostName(party.getHosts());
    this.hostNames = new ArrayList<>(party.getHosts().getHostNames());
  }

  public void end(Instant endTime) {
    this.endTime = endTime;
    if (startTime != null) {
      this.duration = Duration.between(startTime, endTime);
    }
  }

  public Instant getEndTime() {
    return endTime;
  }

  public int getSequence() {
    return sequence;
  }

  public String getName() {
    return name;
  }

  public Component getStyledName() {
    return styledName;
  }

  public int getMatchCount() {
    return matches.size();
  }

  public Component format(boolean verbose) {
    Component durationComponent =
        duration != null ? duration(duration, NamedTextColor.DARK_AQUA) : text("Unknown");
    Component durationValue = duration != null
        ? duration(duration, NamedTextColor.AQUA)
        : text("Unknown", NamedTextColor.GRAY);

    Component nameComponent = styledName;
    if (!verbose) {
      Component nameHover = text()
          .append(styledName)
          .appendNewline()
          .append(text(description, NamedTextColor.BLUE))
          .appendNewline()
          .append(text("(" + type.name() + ")", NamedTextColor.GRAY))
          .appendNewline()
          .append(text("Click for more info", NamedTextColor.GRAY))
          .build();
      nameComponent = styledName.hoverEvent(HoverEvent.showText(nameHover));
    }

    Component durationHover = text()
        .append(text("Duration", NamedTextColor.DARK_AQUA))
        .append(text(": ", NamedTextColor.GRAY))
        .append(durationValue)
        .appendNewline()
        .append(text("Ended", NamedTextColor.YELLOW))
        .append(text(": ", NamedTextColor.GRAY))
        .append(relativePastApproximate(endTime).color(NamedTextColor.GREEN))
        .appendNewline()
        .append(text("Ended at", NamedTextColor.YELLOW))
        .append(text(": ", NamedTextColor.GRAY))
        .append(text(
            END_TIME_FORMAT.format(endTime.atZone(ZoneId.systemDefault())), NamedTextColor.GREEN))
        .build();

    Component mapCount = text()
        .append(text("(", NamedTextColor.GRAY))
        .append(text(matches.size(), NamedTextColor.AQUA))
        .append(text(" map" + (matches.size() == 1 ? "" : "s"), NamedTextColor.GRAY))
        .append(text(")", NamedTextColor.GRAY))
        .build();
    if (!verbose) {
      mapCount = mapCount.hoverEvent(HoverEvent.showText(buildMapHover()));
    }

    TextComponent.Builder hostHover = text();
    if (hostNames.isEmpty()) {
      hostHover.append(text("Unknown", NamedTextColor.DARK_AQUA));
    } else {
      for (int i = 0; i < hostNames.size(); i++) {
        if (i > 0) {
          hostHover.appendNewline();
        }
        hostHover.append(text(hostNames.get(i), NamedTextColor.AQUA));
      }
    }

    Component hostCount = text()
        .append(text("(", NamedTextColor.GRAY))
        .append(text(hostNames.size(), NamedTextColor.AQUA))
        .append(text(" host" + (hostNames.size() == 1 ? "" : "s"), NamedTextColor.GRAY))
        .append(text(")", NamedTextColor.GRAY))
        .build();
    if (!verbose) {
      hostCount = hostCount.hoverEvent(HoverEvent.showText(hostHover));
    }

    Component inlineDuration = durationComponent;
    if (!verbose) {
      inlineDuration = durationComponent.hoverEvent(HoverEvent.showText(durationHover));
    }

    Component top = text()
        .append(text("#" + sequence, NamedTextColor.YELLOW))
        .appendSpace()
        .append(nameComponent)
        .appendSpace()
        .append(text("(", NamedTextColor.GRAY))
        .append(inlineDuration)
        .append(text(")", NamedTextColor.GRAY))
        .appendSpace()
        .append(mapCount)
        .appendSpace()
        .append(hostCount)
        .build();
    if (!verbose) {
      top = top.clickEvent(ClickEvent.runCommand("/event history view " + sequence));
    }

    Component hostsComponent = text()
        .append(text("Host: ", NamedTextColor.GRAY))
        .append(text(mainHostName, NamedTextColor.GOLD))
        .append(text(" (", NamedTextColor.GRAY))
        .append(text(hostNames.size(), NamedTextColor.AQUA))
        .append(text(" total)", NamedTextColor.GRAY))
        .build();

    String hostList = hostNames.isEmpty() ? "Unknown" : String.join(", ", hostNames);
    Component hostListComponent = text()
        .append(text("Hosts: ", NamedTextColor.GRAY))
        .append(text(hostList, NamedTextColor.GOLD))
        .build();

    Component descriptionComponent = text()
        .append(text("Desc: ", NamedTextColor.GRAY))
        .append(text(description, NamedTextColor.BLUE))
        .build();

    Component typeComponent = text()
        .append(text("Type: ", NamedTextColor.GRAY))
        .append(text(type.name(), NamedTextColor.AQUA))
        .build();

    Component endedComponent = text()
        .append(text("Ended: ", NamedTextColor.GRAY))
        .append(relativePastApproximate(endTime).color(NamedTextColor.DARK_GREEN))
        .build();

    Component endedAtComponent = text()
        .append(text("Ended at: ", NamedTextColor.GRAY))
        .append(text(
            END_TIME_FORMAT.format(endTime.atZone(ZoneId.systemDefault())), NamedTextColor.GREEN))
        .build();

    if (!verbose) {
      return top;
    }

    Component matchHeader = text()
        .append(text("Matches: ", NamedTextColor.GRAY))
        .append(text(matches.size(), NamedTextColor.AQUA))
        .build();

    Component verboseBlock = text()
        .append(top)
        .appendNewline()
        .append(text("  "))
        .append(hostsComponent)
        .appendNewline()
        .append(text("  "))
        .append(hostListComponent)
        .appendNewline()
        .append(text("  "))
        .append(descriptionComponent)
        .appendNewline()
        .append(text("  "))
        .append(typeComponent)
        .appendNewline()
        .append(text("  "))
        .append(endedComponent)
        .appendNewline()
        .append(text("  "))
        .append(endedAtComponent)
        .appendNewline()
        .append(text("  "))
        .append(matchHeader)
        .build();

    Component combined = verboseBlock;
    for (int i = 0; i < matches.size(); i++) {
      Component line = matches.get(i).formatLine(i + 1);
      combined = combined.appendNewline().append(text("  ")).append(line);
    }

    return combined;
  }

  private Component buildMapHover() {
    TextComponent.Builder hover = text();
    if (matches.isEmpty()) {
      return hover.append(text("No maps played", NamedTextColor.GRAY)).build();
    }

    int limit = Math.min(matches.size(), 5);
    for (int i = 0; i < limit; i++) {
      if (i > 0) {
        hover.appendNewline();
      }
      MapPartyMatchEntry match = matches.get(i);
      hover
          .append(text((i + 1) + ". ", NamedTextColor.GRAY))
          .append(text(match.getMapName(), NamedTextColor.GOLD))
          .appendSpace()
          .append(text("(", NamedTextColor.GRAY))
          .append(duration(match.getMatchDuration(), NamedTextColor.AQUA))
          .append(text(")", NamedTextColor.GRAY));
    }

    if (matches.size() > limit) {
      hover.appendNewline().append(text("Click to view all maps", NamedTextColor.YELLOW));
    }

    return hover.build();
  }
}
