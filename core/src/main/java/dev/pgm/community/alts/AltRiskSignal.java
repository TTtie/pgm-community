package dev.pgm.community.alts;

import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record AltRiskSignal(
    AltRiskSignalType type,
    UUID linkedAccountId,
    int weight,
    String message,
    @Nullable Instant evidenceTime) {}
