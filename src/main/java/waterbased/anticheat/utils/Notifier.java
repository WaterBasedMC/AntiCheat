package waterbased.anticheat.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import waterbased.anticheat.AntiCheat;

public class Notifier {

    public enum Check {

        OTHER_NoFall("NoFall", "Taking no fall damage");

        private String name;
        private String description;

        Check(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

    }

    public static void notify(Check check, Player player, String message) {

        TextComponent msg = Component.empty()
                .append(Component
                        .text()
                        .color(NamedTextColor.GOLD)
                        .hoverEvent(Component
                                .text("WaterBased AntiCheat")
                                .color(NamedTextColor.GRAY))
                        .content(AntiCheat.PREFIX))
                .append(Component
                        .text()
                        .decoration(TextDecoration.BOLD, false)
                        .decoration(TextDecoration.ITALIC, true)
                        .color(NamedTextColor.RED)
                        .content(player.getName()))
                .append(Component
                        .text(" failed ")
                        .decoration(TextDecoration.BOLD, false)
                        .decoration(TextDecoration.ITALIC, false)
                        .color(NamedTextColor.WHITE))
                .append(Component
                        .text(check.getName())
                        .decoration(TextDecoration.BOLD, false)
                        .decoration(TextDecoration.ITALIC, true)
                        .color(NamedTextColor.RED)
                        .hoverEvent(Component
                                .text()
                                .decoration(TextDecoration.BOLD, false)
                                .decoration(TextDecoration.ITALIC, true)
                                .color(NamedTextColor.GRAY)
                                .content(check.getDescription()).asComponent()
                        ))
                .append(Component
                        .text(" (" + message + ")")
                        .decoration(TextDecoration.BOLD, false)
                        .decoration(TextDecoration.ITALIC, false)
                        .color(NamedTextColor.WHITE));


        final TextComponent finalMsg = msg;

        Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission("wac.notify")).forEach(p -> p.sendMessage(finalMsg));
        AntiCheat.instance.getLogger().warning(player.getName() + " failed " + check.getName() + ": (" + message + ")");

    }


}
