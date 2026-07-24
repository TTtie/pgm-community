package dev.pgm.community.events;

import dev.pgm.community.moderation.punishments.PunishmentType;
import dev.pgm.community.utils.CommandAudience;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** PlayerPardonEvent - Called when a punishment is pardoned */
public class PlayerPardonEvent extends CommunityEvent {

  private final CommandAudience sender;
  private final UUID targetId;
  private final PunishmentType type;

  public PlayerPardonEvent(@Nullable CommandAudience sender, UUID targetId, PunishmentType type) {
    this.sender = sender;
    this.targetId = targetId;
    this.type = type;
  }

  @Nullable
  public CommandAudience getSender() {
    return sender;
  }

  public UUID getTargetId() {
    return targetId;
  }

  public PunishmentType getType() {
    return type;
  }
}
