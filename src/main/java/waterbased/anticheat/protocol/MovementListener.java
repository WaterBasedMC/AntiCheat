package waterbased.anticheat.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import waterbased.anticheat.AntiCheat;
import waterbased.anticheat.checks.movement.PlayerMovement;

import java.util.ArrayList;
import java.util.HashMap;

public class MovementListener {

    private static final HashMap<Player, Location> lastLocation = new HashMap<>();
    public static ArrayList<PacketListener> listeners = new ArrayList<PacketListener>();

    public static void register() {

        ArrayList<PacketType> packetTypes1 = new ArrayList<PacketType>();
        packetTypes1.add(PacketType.Play.Client.POSITION);
        packetTypes1.add(PacketType.Play.Client.POSITION_LOOK);
        packetTypes1.add(PacketType.Play.Client.LOOK);
        packetTypes1.add(PacketType.Play.Client.GROUND);

        PacketListener listener1 = new PacketAdapter(AntiCheat.instance, packetTypes1) {

            @Override
            public void onPacketReceiving(PacketEvent e) {

                if (!AntiCheat.instance.isEnabled()) return;

                PacketContainer packet = e.getPacket();

                if (packet.getType() == PacketType.Play.Client.POSITION) {
                    double x = packet.getDoubles().read(0);
                    double y = packet.getDoubles().read(1);
                    double z = packet.getDoubles().read(2);
                    boolean onGround = packet.getBooleans().read(0);
                    if (!PlayerMovement.movePacket(e.getPlayer(), x, y, z, onGround)) {
                        e.setCancelled(true);
                    }
                }

                if (packet.getType() == PacketType.Play.Client.POSITION_LOOK) {
                    double x = packet.getDoubles().read(0);
                    double y = packet.getDoubles().read(1);
                    double z = packet.getDoubles().read(2);
                    float yaw = packet.getFloat().read(0);
                    float pitch = packet.getFloat().read(1);
                    boolean onGround = packet.getBooleans().read(0);
                    if (!PlayerMovement.movePacket(e.getPlayer(), x, y, z, yaw, pitch, onGround)) {
                        e.setCancelled(true);
                    }
                }

                if (packet.getType() == PacketType.Play.Client.LOOK) {
                    float yaw = packet.getFloat().read(0);
                    float pitch = packet.getFloat().read(1);
                    boolean onGround = packet.getBooleans().read(0);
                    if (!PlayerMovement.lookPacket(e.getPlayer(), yaw, pitch, onGround)) {
                        e.setCancelled(true);
                    }
                }

                if (packet.getType() == PacketType.Play.Client.GROUND) {
                    boolean onGround = packet.getBooleans().read(0);
                    if (!PlayerMovement.groundPacket(e.getPlayer(), onGround)) {
                        e.setCancelled(true);
                    }
                }

            }

        };
        listeners.add(listener1);
        ProtocolLibrary.getProtocolManager().addPacketListener(listener1);

    }

    public static void unregister() {
        for (PacketListener listener : listeners) {
            ProtocolLibrary.getProtocolManager().removePacketListener(listener);
        }
    }

}
