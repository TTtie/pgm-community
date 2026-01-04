package dev.pgm.community.platform.sportpaper.features.serverlinks;

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.packet.State;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.gson.JsonParser;
import com.viaversion.viaversion.libs.mcstructs.text.utils.JsonNbtConverter;
import dev.pgm.community.serverlinks.types.ServerLink;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.entity.Player;

public class ViaServerLinks {
  private static final Protocol<?, ?, ?, ?> serverLinkProtocol = findServerLinkProtocol();

  public static void sendToPlayer(Player player, List<ServerLink> serverLinks) {
    if (!Via.getAPI().isInjected(player.getUniqueId())) return;
    UserConnection userConnection = Via.getAPI().getConnection(player.getUniqueId());
    if (userConnection != null
        && userConnection
            .getProtocolInfo()
            .protocolVersion()
            .newerThanOrEqualTo(ProtocolVersion.v1_21)) {
      PacketWrapper serverLinksPacket = createPacket(userConnection, serverLinks);
      serverLinksPacket.scheduleSend(serverLinkProtocol.getClass());
    }
  }

  private static PacketWrapper createPacket(UserConnection conn, List<ServerLink> links) {
    var packetTypes = serverLinkProtocol.getPacketTypesProvider().mappedClientboundPacketTypes();
    var packetType = packetTypes.get(State.PLAY).typeByName("SERVER_LINKS");
    PacketWrapper packet = PacketWrapper.create(packetType, conn);
    packet.write(Types.VAR_INT, links.size());
    // TODO: is there a better way to do this?
    for (ServerLink link : links) {
      packet.write(Types.BOOLEAN, link.builtinType() != null);
      if (link.builtinType() != null) {
        packet.write(Types.VAR_INT, link.builtinType().ordinal());
      } else {
        packet.write(Types.TAG, toViaTag(link.customText()));
      }
      packet.write(Types.STRING, link.uri().toString());
    }

    return packet;
  }

  private static Tag toViaTag(Component component) {
    return JsonNbtConverter.toNbt(
        JsonParser.parseString(GsonComponentSerializer.gson().serialize(component)));
  }

  private static Protocol<?, ?, ?, ?> findServerLinkProtocol() {
    return Via.getManager()
        .getProtocolManager()
        .getProtocol(/* to */ ProtocolVersion.v1_21, /* from */ ProtocolVersion.v1_20_5);
  }
}
