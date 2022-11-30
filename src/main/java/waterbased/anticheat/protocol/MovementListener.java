package waterbased.anticheat.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import waterbased.anticheat.AntiCheat;
import waterbased.anticheat.events.PlayerOnGroundChangeEvent;
import waterbased.anticheat.events.PlayerPreciseMoveEvent;

import java.util.ArrayList;
import java.util.HashMap;

public class MovementListener {

    public static ArrayList<PacketListener> listeners = new ArrayList<PacketListener>();

    private static final HashMap<Player, Location> lastLocation = new HashMap<>();
    private static final HashMap<Player, Boolean> lastOnGroundState = new HashMap<>();

    public static void register() {

        ArrayList<PacketType> packetTypes = new ArrayList<PacketType>();
        packetTypes.add(PacketType.Play.Client.POSITION);
        packetTypes.add(PacketType.Play.Client.POSITION_LOOK);
        packetTypes.add(PacketType.Play.Client.LOOK);
        packetTypes.add(PacketType.Play.Client.GROUND);

        PacketListener listener1 = new PacketAdapter(AntiCheat.instance, packetTypes) {

            @Override
            public void onPacketReceiving(PacketEvent e) {
                PacketContainer packet = e.getPacket();

                Location loc = null;
                boolean onGround = false;

                if (packet.getType() == PacketType.Play.Client.POSITION) {
                    double x = packet.getDoubles().read(0);
                    double y = packet.getDoubles().read(1);
                    double z = packet.getDoubles().read(2);
                    onGround = packet.getBooleans().read(0);
                    loc = new Location(e.getPlayer().getWorld(), x, y, z);
                }

                if (packet.getType() == PacketType.Play.Client.POSITION_LOOK) {
                    double x = packet.getDoubles().read(0);
                    double y = packet.getDoubles().read(1);
                    double z = packet.getDoubles().read(2);
                    float yaw = packet.getFloat().read(0);
                    float pitch = packet.getFloat().read(1);
                    onGround = packet.getBooleans().read(0);
                    loc = new Location(e.getPlayer().getWorld(), x, y, z, yaw, pitch);
                }

                if (packet.getType() == PacketType.Play.Client.LOOK) {
                    float yaw = packet.getFloat().read(0);
                    float pitch = packet.getFloat().read(1);
                    onGround = packet.getBooleans().read(0);
                    loc = e.getPlayer().getLocation();
                    loc.setYaw(yaw);
                    loc.setPitch(pitch);
                }

                if(packet.getType() == PacketType.Play.Client.GROUND) {
                    onGround = packet.getBooleans().read(0);
                    loc = e.getPlayer().getLocation();
                }

                if (lastLocation.containsKey(e.getPlayer())) {
                    final Location finLoc = loc;
                    final boolean finOnGround = onGround;
                    Bukkit.getScheduler().runTask(AntiCheat.instance, () -> {
                        Bukkit.getPluginManager().callEvent(new PlayerPreciseMoveEvent(e.getPlayer(), lastLocation.get(e.getPlayer()), finLoc, finOnGround));
                        if(lastOnGroundState.getOrDefault(e.getPlayer(), false) != finOnGround) {
                            Bukkit.getPluginManager().callEvent(new PlayerOnGroundChangeEvent(e.getPlayer(), finOnGround));
                            lastOnGroundState.put(e.getPlayer(), finOnGround);
                        }
                    });
                }
                lastLocation.put(e.getPlayer(), loc);

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
