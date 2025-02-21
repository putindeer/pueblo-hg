package me.putindeer.puebloHG.game;

import fr.mrmicky.fastboard.adventure.FastBoard;
import me.putindeer.puebloHG.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Statistic;

import java.util.ArrayList;
import java.util.List;

public class Scoreboards {
    private static final Main plugin = Main.getPl();

    public static void updateBoard(FastBoard board) {
        Component headfooter = plugin.utils.chat("&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r&7&m &r");

        List<Component> lines = new ArrayList<>();
        lines.add(headfooter);

        if (plugin.totalPlayers != -1) {
            lines.add(plugin.utils.chat("Players: &3" + playingCount() + "&f/&3" + plugin.totalPlayers));
        } else {
            lines.add(plugin.utils.chat("Players: &3" + playingCount()));
        }

        boolean hasTimers = false;

        if (plugin.gameManager.started) {
            lines.add(plugin.utils.chat("Tiempo de partida: &3" + plugin.timer));
            lines.add(Component.empty());

            int pvpTime = Math.max(plugin.scatter.timeLeft - (29 * 60 + 30), 0);
            int borderTime = Math.max(plugin.scatter.timeLeft - (15 * 60), 0);
            int restockTime = Math.max(plugin.scatter.timeLeft - (10 * 60), 0);
            int verticalBorderTime = Math.max(plugin.scatter.timeLeft - (5 * 60), 0);
            int borderCloseTime = (plugin.scatter.timeLeft <= (15 * 60)) ? plugin.scatter.timeLeft : 0;

            String pvpFormatted = formatTime(pvpTime);
            String borderFormatted = formatTime(borderTime);
            String restockFormatted = formatTime(restockTime);
            String verticalBorderFormatted = formatTime(verticalBorderTime);
            String borderCloseFormatted = formatTime(borderCloseTime);

            if (pvpTime > 0) lines.add(plugin.utils.chat("&cPvP&f: " + pvpFormatted));
            if (borderTime > 0) lines.add(plugin.utils.chat("&eBorde&f: " + borderFormatted));
            if (restockTime > 0) lines.add(plugin.utils.chat("&6Restock&f: " + restockFormatted));
            if (verticalBorderTime > 0) lines.add(plugin.utils.chat("&dBorde Vertical&f: " + verticalBorderFormatted));
            if (borderCloseTime > 0) lines.add(plugin.utils.chat("&4Borde Final&f: " + borderCloseFormatted));
            if (pvpTime > 0 || borderTime > 0 || restockTime > 0 || verticalBorderTime > 0 || borderCloseTime > 0) hasTimers = true;
            if (hasTimers) lines.add(Component.empty());
            lines.add(plugin.utils.chat("Kills: &3" + board.getPlayer().getStatistic(Statistic.PLAYER_KILLS)));
            lines.add(Component.empty());
        } else {
            lines.add(Component.empty());
        }

        lines.add(plugin.utils.chat("Ping: &3" + board.getPlayer().getPing()));
        lines.add(headfooter);

        board.updateLines(lines.toArray(new Component[0]));
    }

    private static String formatTime(int seconds) {
        int minutes = seconds / 60;
        int sec = seconds % 60;
        return String.format("%02d:%02d", minutes, sec);
    }

    private static Integer playingCount() {
        return (int) Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getGameMode() != GameMode.SPECTATOR)
                .count();
    }
}
