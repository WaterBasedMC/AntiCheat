package waterbased.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import waterbased.anticheat.checks.movement.CHECK_Elytra;
import waterbased.anticheat.checks.movement.CHECK_FastLadder;
import waterbased.anticheat.checks.movement.CHECK_Flight;
import waterbased.anticheat.checks.movement.CHECK_Speed;
import waterbased.anticheat.checks.other.CHECK_NoFall;
import waterbased.anticheat.checks.world.CHECK_BlockBreak;
import waterbased.anticheat.protocol.MovementListener;
import waterbased.anticheat.utils.Punishment;

public final class AntiCheat extends JavaPlugin {

    public static long tick = 0;

    public static final String PREFIX = "[WAC] ";
    public static AntiCheat instance;

    @Override
    public void onEnable() {
        instance = this;
        this.enableChecks();
        this.startTicking();
        MovementListener.register();
    }

    @Override
    public void onDisable() {
        MovementListener.unregister();
    }

    private void enableChecks() {
        Bukkit.getPluginManager().registerEvents(new Punishment(), this);

        //Movement
        Bukkit.getPluginManager().registerEvents(new CHECK_Flight(), this);
        Bukkit.getPluginManager().registerEvents(new CHECK_Elytra(), this);
        Bukkit.getPluginManager().registerEvents(new CHECK_Speed(), this);
        Bukkit.getPluginManager().registerEvents(new CHECK_FastLadder(), this);

        //World
        Bukkit.getPluginManager().registerEvents(new CHECK_BlockBreak(), this);

        //Other
        Bukkit.getPluginManager().registerEvents(new CHECK_NoFall(), this);
    }

    private void startTicking() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            tick++;
        }, 0, 1);
    }

}
