package waterbased.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import waterbased.anticheat.checks.other.CHECK_NoFall;

public final class AntiCheat extends JavaPlugin {

    public static long tick = 0;

    public static final String PREFIX = "[WAC] ";
    public static AntiCheat instance;

    @Override
    public void onEnable() {
        instance = this;
        this.enableChecks();
        this.startTicking();
    }

    @Override
    public void onDisable() {

    }

    private void enableChecks() {
        Bukkit.getPluginManager().registerEvents(new CHECK_NoFall(), this);
    }

    private void startTicking() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            tick++;
        }, 0, 1);
    }

}
