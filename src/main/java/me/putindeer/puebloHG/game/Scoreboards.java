package me.putindeer.puebloHG.game;

import fr.mrmicky.fastboard.adventure.FastBoard;
import me.putindeer.puebloHG.Main;
import me.putindeer.puebloHG.config.game.GameEvent;
import me.putindeer.puebloHG.config.game.GameEventType;
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
            lines.add(plugin.utils.chat("Jugadores: &3" + playingCount() + "&f/&3" + plugin.totalPlayers));
        } else {
            lines.add(plugin.utils.chat("Jugadores: &3" + playingCount()));
        }

        if (plugin.gameManager.started) {
            lines.add(plugin.utils.chat("Tiempo de partida: &3" + plugin.timer));
            lines.add(Component.empty());

            List<Component> shownEvents = new ArrayList<>();

            for (GameEvent event : plugin.events) {
                int seconds = Math.max(plugin.scatter.time - event.triggerTime(), 0);
                if (seconds == 0) continue;

                switch (event.type()) {
                    case END_BORDER -> {
                        GameEvent border = plugin.events.stream()
                                .filter(e -> e.type() == GameEventType.SHRINK_BORDER)
                                .findFirst()
                                .orElse(null);
                        if (border != null && plugin.scatter.time > border.triggerTime()) {
                            continue;
                        }
                    }
                    case END_VERTICAL_BORDER -> {
                        GameEvent border = plugin.events.stream()
                                .filter(e -> e.type() == GameEventType.VERTICAL_BORDER)
                                .findFirst().orElse(null);
                        if (border != null && plugin.scatter.time > border.triggerTime()) {
                            continue;
                        }

                        GameEvent endBorder = plugin.events.stream()
                                .filter(e -> e.type() == GameEventType.END_BORDER)
                                .findFirst().orElse(null);
                        if (endBorder != null) {
                            int borderSeconds = Math.max(plugin.scatter.time - endBorder.triggerTime(), 0);
                            if (borderSeconds == seconds) {
                                continue;
                            }
                        }
                    }
                    default -> {}
                }

                if (event.type() == GameEventType.END_VERTICAL_BORDER) {
                    GameEvent border = plugin.events.stream()
                            .filter(e -> e.type() == GameEventType.VERTICAL_BORDER)
                            .findFirst()
                            .orElse(null);
                    if (border != null && plugin.scatter.time > border.triggerTime()) {
                        continue;
                    }
                }

                if (event.type() == GameEventType.END_VERTICAL_BORDER) {
                    GameEvent endBorder = plugin.events.stream()
                            .filter(e -> e.type() == GameEventType.END_BORDER)
                            .findFirst().orElse(null);
                    if (endBorder != null) {
                        int borderSeconds = Math.max(plugin.scatter.time - endBorder.triggerTime(), 0);
                        if (borderSeconds == seconds) {
                            continue;
                        }
                    }
                }

                String timeStr = plugin.utils.formatTime(seconds);
                String label = switch (event.type()) {
                    case ENABLE_PVP          -> "&cPvP&f: ";
                    case SHRINK_BORDER       -> "&eBorde&f: ";
                    case RESTOCK_CHESTS      -> "&6Restock&f: ";
                    case VERTICAL_BORDER     -> "&dBorde Vertical&f: ";
                    case END_BORDER          -> "&4Borde Final&f: ";
                    case END_VERTICAL_BORDER -> "&4Borde Vertical Final&f: ";
                    case GLOWING             -> "&eGlowing: ";
                };

                shownEvents.add(plugin.utils.chat(label + timeStr));
            }


            lines.addAll(shownEvents);

            if (!shownEvents.isEmpty()) {
                lines.add(Component.empty());
            }

            lines.add(plugin.utils.chat("Kills: &3" + board.getPlayer().getStatistic(Statistic.PLAYER_KILLS)));
            lines.add(Component.empty());
        } else {
            lines.add(Component.empty());
            lines.add(plugin.utils.chat("Puntos: &3" + plugin.pointsManager.getPoints(board.getPlayer().getUniqueId())));
        }

        lines.add(plugin.utils.chat("Ping: &3" + board.getPlayer().getPing()));
        lines.add(headfooter);

        board.updateLines(lines.toArray(new Component[0]));
    }

    private static Integer playingCount() {
        return (int) Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getGameMode() != GameMode.SPECTATOR)
                .filter(p -> !plugin.gameManager.started || plugin.alivePlayers.contains(p.getUniqueId()))
                .count();
    }
}
