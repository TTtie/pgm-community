package dev.pgm.community.alts;

public enum AltRiskSignalType {
  DIRECT_BAN_EVASION("Direct ban evasion"),
  SHARED_CURRENT_IP("Shared current IP"),
  SHARED_KNOWN_IP("Shared known IP"),
  MULTIPLE_SHARED_IPS("Multiple shared IPs"),
  JOINED_AFTER_BAN("Joined post-ban"),
  RELAYED_SESSION("Session relay"),
  LINKED_ACTIVE_BAN("Linked account banned"),
  LINKED_RECENT_BAN("Linked recently banned"),
  FRESH_ACCOUNT_LINKED("Fresh account");

  private final String label;

  AltRiskSignalType(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
