package dev.pgm.community.alts;

import net.kyori.adventure.text.format.NamedTextColor;

public enum AltRiskLevel {
  LOW,
  MEDIUM,
  HIGH;

  public static AltRiskLevel fromScore(int score, int mediumThreshold, int highThreshold) {
    if (score >= highThreshold) {
      return HIGH;
    }
    if (score >= mediumThreshold) {
      return MEDIUM;
    }
    return LOW;
  }

  public NamedTextColor color() {
    return switch (this) {
      case HIGH -> NamedTextColor.RED;
      case MEDIUM -> NamedTextColor.YELLOW;
      case LOW -> NamedTextColor.GREEN;
    };
  }
}
