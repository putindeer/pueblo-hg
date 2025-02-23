package me.putindeer.puebloHG.commands;

import me.putindeer.puebloHG.Main;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Leaderboard implements CommandExecutor {
    private final Main plugin;

    public Leaderboard(Main plugin) {
        this.plugin = plugin;
        Objects.requireNonNull(plugin.getCommand("leaderboard")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("fullleaderboard")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String @NotNull [] args) {
        switch (cmd.getName().toLowerCase()) {
            case "leaderboard", "lb", "toppoints" -> sendTopPlayers(sender, 5);
            case "fullleaderboard", "fulllb", "fulltoppoints" -> sendTopPlayers(sender, Integer.MAX_VALUE);
        }
        return true;
    }

    private void sendTopPlayers(CommandSender sender, int maxEntries) {
        List<Map.Entry<UUID, Integer>> topPlayers = plugin.pointsManager.getTopPlayers();
        int size = Math.min(topPlayers.size(), maxEntries);

        if (topPlayers.isEmpty()) {
            plugin.utils.message(sender, "&cNo hay jugadores con puntos aún.");
            return;
        }

        plugin.utils.message(sender, "&bTablero de Puntos del Evento");

        for (int i = 0; i < size; i++) {
            Map.Entry<UUID, Integer> entry = topPlayers.get(i);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(entry.getKey());
            String name = offlinePlayer.getName() != null ? offlinePlayer.getName() : "Desconocido";
            plugin.utils.message(sender, "&7" + (i + 1) + ". &b" + name + " &7- &c" + entry.getValue() + " puntos");
        }
    }
}
