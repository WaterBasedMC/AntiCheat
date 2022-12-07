package waterbased.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import waterbased.anticheat.checks.movement.*;
import waterbased.anticheat.checks.player.CHECK_NoFall;
import waterbased.anticheat.checks.world.CHECK_BlockBreak;
import waterbased.anticheat.events.ServerTickEvent;
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
        Bukkit.getScheduler().getPendingTasks().forEach(BukkitTask::cancel);
    }

    private void enableChecks() {
        PluginManager pluginManager = Bukkit.getPluginManager();

        pluginManager.registerEvents(new Punishment(), this);

        //Player Movement
        pluginManager.registerEvents(new PlayerMovement(), this);

        //Movement
        pluginManager.registerEvents(new CHECK_Flight(), this);
        pluginManager.registerEvents(new CHECK_Elytra(), this);
        pluginManager.registerEvents(new CHECK_Speed(), this);
        pluginManager.registerEvents(new CHECK_FastLadder(), this);
        pluginManager.registerEvents(new CHECK_VehicleFlight(), this);

        //World
        pluginManager.registerEvents(new CHECK_BlockBreak(), this);

        //Other
        pluginManager.registerEvents(new CHECK_NoFall(), this);
    }

    private void startTicking() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            tick++;
            Bukkit.getPluginManager().callEvent(new ServerTickEvent(tick));
        }, 0, 1);
    }

}
