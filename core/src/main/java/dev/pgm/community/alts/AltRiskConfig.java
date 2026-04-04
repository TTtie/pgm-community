package dev.pgm.community.alts;

import static tc.oc.pgm.util.text.TextParser.parseDuration;

import dev.pgm.community.feature.config.FeatureConfigImpl;
import java.time.Duration;
import org.bukkit.configuration.Configuration;

public class AltRiskConfig extends FeatureConfigImpl {

  private static final String KEY = "alt-risk";

  private int mediumThreshold;
  private int highThreshold;
  private Duration cacheDuration;
  private Duration recentBanWindow;
  private Duration joinAfterBanWindow;
  private Duration relayWindow;
  private int freshAccountJoinCount;

  private int directBanEvasionWeight;
  private int sharedCurrentIpWeight;
  private int sharedKnownIpWeight;
  private int multipleSharedIpsWeight;
  private int joinedAfterBanWeight;
  private int relayedSessionWeight;
  private int linkedActiveBanWeight;
  private int linkedRecentBanWeight;
  private int freshAccountLinkedWeight;

  public AltRiskConfig(Configuration config) {
    super(KEY, config);
  }

  public int getMediumThreshold() {
    return mediumThreshold;
  }

  public int getHighThreshold() {
    return highThreshold;
  }

  public Duration getCacheDuration() {
    return cacheDuration;
  }

  public Duration getRecentBanWindow() {
    return recentBanWindow;
  }

  public Duration getJoinAfterBanWindow() {
    return joinAfterBanWindow;
  }

  public Duration getRelayWindow() {
    return relayWindow;
  }

  public int getFreshAccountJoinCount() {
    return freshAccountJoinCount;
  }

  public int getDirectBanEvasionWeight() {
    return directBanEvasionWeight;
  }

  public int getSharedCurrentIpWeight() {
    return sharedCurrentIpWeight;
  }

  public int getSharedKnownIpWeight() {
    return sharedKnownIpWeight;
  }

  public int getMultipleSharedIpsWeight() {
    return multipleSharedIpsWeight;
  }

  public int getJoinedAfterBanWeight() {
    return joinedAfterBanWeight;
  }

  public int getRelayedSessionWeight() {
    return relayedSessionWeight;
  }

  public int getLinkedActiveBanWeight() {
    return linkedActiveBanWeight;
  }

  public int getLinkedRecentBanWeight() {
    return linkedRecentBanWeight;
  }

  public int getFreshAccountLinkedWeight() {
    return freshAccountLinkedWeight;
  }

  @Override
  public void reload(Configuration config) {
    super.reload(config);
    this.mediumThreshold = config.getInt(KEY + ".medium-threshold", 30);
    this.highThreshold = config.getInt(KEY + ".high-threshold", 60);
    this.cacheDuration = parseDuration(config.getString(KEY + ".cache-duration", "3m"));
    this.recentBanWindow = parseDuration(config.getString(KEY + ".recent-ban-window", "7d"));
    this.joinAfterBanWindow =
        parseDuration(config.getString(KEY + ".join-after-ban-window", "30m"));
    this.relayWindow = parseDuration(config.getString(KEY + ".relay-window", "20m"));
    this.freshAccountJoinCount = config.getInt(KEY + ".fresh-account-join-count", 5);

    this.directBanEvasionWeight = config.getInt(KEY + ".weights.direct-ban-evasion", 60);
    this.sharedCurrentIpWeight = config.getInt(KEY + ".weights.shared-current-ip", 40);
    this.sharedKnownIpWeight = config.getInt(KEY + ".weights.shared-known-ip", 25);
    this.multipleSharedIpsWeight = config.getInt(KEY + ".weights.multiple-shared-ips", 10);
    this.joinedAfterBanWeight = config.getInt(KEY + ".weights.joined-after-ban", 30);
    this.relayedSessionWeight = config.getInt(KEY + ".weights.relayed-session", 15);
    this.linkedActiveBanWeight = config.getInt(KEY + ".weights.linked-active-ban", 20);
    this.linkedRecentBanWeight = config.getInt(KEY + ".weights.linked-recent-ban", 10);
    this.freshAccountLinkedWeight = config.getInt(KEY + ".weights.fresh-account-linked", 10);
  }
}
