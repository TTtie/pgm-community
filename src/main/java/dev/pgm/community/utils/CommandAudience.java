package dev.pgm.community.utils;

import java.util.Optional;
import java.util.UUID;
import net.kyori.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tc.oc.pgm.util.chat.Audience;
import tc.oc.pgm.util.named.NameStyle;
import tc.oc.pgm.util.text.types.PlayerComponent;

public class CommandAudience {

  private Audience audience;
  private CommandSender sender;

  public CommandAudience(CommandSender sender) {
    this.sender = sender;
    this.audience = Audience.get(sender);
  }

  public Optional<UUID> getId() {
    return Optional.ofNullable(sender instanceof Player ? ((Player) sender).getUniqueId() : null);
  }

  public CommandSender getSender() {
    return sender;
  }

  public Audience getAudience() {
    return audience;
  }

  public Component getStyledName() {
    return PlayerComponent.of(sender, NameStyle.FANCY);
  }

  public void sendMessage(Component message) {
    audience.sendMessage(message);
  }

  public void sendWarning(Component message) {
    audience.sendWarning(message);
  }
}
