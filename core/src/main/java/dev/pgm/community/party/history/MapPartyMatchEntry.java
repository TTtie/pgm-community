package dev.pgm.community.party.history;

import static net.kyori.adventure.text.Component.text;
import static tc.oc.pgm.util.text.TemporalComponent.duration;
import static tc.oc.pgm.util.text.TemporalComponent.relativePastApproximate;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import tc.oc.pgm.api.match.Match;

public class MapPartyMatchEntry {

  private static final DateTimeFormatter END_TIME_FORMAT =
      DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a z", Locale.ENGLISH);

  private final String matchId;
  private final String mapName;
  private final Duration matchDuration;
  private final Instant endTime;

  public MapPartyMatchEntry(Match match) {
    this.matchId = match.getId();
    this.mapName = match.getMap().getName();
    this.matchDuration = match.getDuration();
    this.endTime = Instant.now();
  }

  public Instant getEndTime() {
    return endTime;
  }

  public String getMapName() {
    return mapName;
  }

  public Duration getMatchDuration() {
    return matchDuration;
  }

  public Component formatLine(int index) {
    Component matchLength = duration(matchDuration, NamedTextColor.DARK_AQUA);

    Component timeSince = text()
        .append(text("(", NamedTextColor.GRAY))
        .append(relativePastApproximate(endTime).color(NamedTextColor.DARK_GREEN))
        .append(text(")", NamedTextColor.GRAY))
        .hoverEvent(HoverEvent.showText(text()
            .append(text("This match ended at ", NamedTextColor.GRAY))
            .append(text(
                END_TIME_FORMAT.format(endTime.atZone(ZoneId.systemDefault())),
                NamedTextColor.DARK_GREEN))))
        .build();

    return text()
        .append(text(index + ". ", NamedTextColor.GRAY))
        .append(text(mapName, NamedTextColor.GOLD))
        .appendSpace()
        .append(text("(", NamedTextColor.GRAY))
        .append(text("#" + matchId, NamedTextColor.YELLOW))
        .append(text(")", NamedTextColor.GRAY))
        .appendSpace()
        .append(text("(", NamedTextColor.GRAY))
        .append(matchLength)
        .append(text(")", NamedTextColor.GRAY))
        .appendSpace()
        .append(timeSince)
        .build();
  }
}
