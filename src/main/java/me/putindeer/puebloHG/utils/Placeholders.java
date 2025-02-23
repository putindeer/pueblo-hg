package me.putindeer.puebloHG.utils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.putindeer.puebloHG.Main;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Placeholders extends PlaceholderExpansion {
    private final Main plugin = Main.getPl();

    @Override
    public @NotNull String getIdentifier() {
        return "hg";
    }

    @Override
    public @NotNull String getAuthor() {
        return "putindeer";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String identifier) {
        if (identifier.equalsIgnoreCase("points") && player != null) {
            return String.valueOf(plugin.pointsManager.getPoints(player.getUniqueId()));
        }

        if (identifier.startsWith("toppoints-")) {
            try {
                int position = Integer.parseInt(identifier.replace("toppoints-", "")) - 1;
                List<Map.Entry<UUID, Integer>> topPlayers = plugin.pointsManager.getTopPlayers();

                if (position < 0 || position >= topPlayers.size()) {
                    return "N/A";
                }

                UUID uuid = topPlayers.get(position).getKey();
                int points = topPlayers.get(position).getValue();
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                String playerName = offlinePlayer.getName() != null ? offlinePlayer.getName() : "Desconocido";

                return playerName + " - " + points + " puntos";
            } catch (NumberFormatException e) {
                return "Número incorrecto";
            }
        }

        return null;
    }
}
