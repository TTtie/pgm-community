package dev.pgm.community.alts;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.format.NamedTextColor;

public record AltRiskSummary(
    UUID targetId,
    int score,
    AltRiskLevel level,
    Instant generatedAt,
    List<AltRiskSignal> signals,
    List<AltRiskLinkedAccount> linkedAccounts) {

  public NamedTextColor effectiveColor() {
    return requiresReview() ? level.color() : NamedTextColor.GRAY;
  }

  public boolean requiresReview() {
    return signals.stream().anyMatch(signal -> switch (signal.type()) {
      case DIRECT_BAN_EVASION, JOINED_AFTER_BAN, LINKED_ACTIVE_BAN, LINKED_RECENT_BAN -> true;
      default -> false;
    });
  }
}
