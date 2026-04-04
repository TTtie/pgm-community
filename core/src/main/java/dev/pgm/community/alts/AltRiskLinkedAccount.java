package dev.pgm.community.alts;

import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record AltRiskLinkedAccount(
    UUID accountId,
    int scoreContribution,
    int sharedIpsCount,
    boolean currentSharedIp,
    boolean joinedAfterPunishment,
    boolean activeBan,
    @Nullable Instant latestSharedIpTime,
    @Nullable Instant punishmentTime) {}
