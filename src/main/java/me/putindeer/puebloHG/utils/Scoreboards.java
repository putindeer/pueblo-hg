package me.putindeer.puebloHG.utils;

import fr.mrmicky.fastboard.adventure.FastBoard;
import me.putindeer.puebloHG.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;

public class Scoreboards {
    private static final Main plugin = Main.getPl();
    public static void updateBoard(FastBoard board) {
        Component headfooter = plugin.utils.chat("&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r");

        board.updateLines(
                headfooter,
                plugin.utils.chat("&oPlayers &r&8» &3" + playingCount()),
                Component.empty(),
                plugin.utils.chat("&oPing &r&8» &3" + board.getPlayer().getPing()),
                headfooter
        );
    }

    public static Integer playingCount() {
        return (int) Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getGameMode() == GameMode.SURVIVAL)
                .count();
    }
}
