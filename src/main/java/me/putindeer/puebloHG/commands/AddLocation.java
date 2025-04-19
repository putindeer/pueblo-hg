package me.putindeer.puebloHG.commands;

import me.putindeer.puebloHG.Main;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AddLocation implements CommandExecutor {
    private final Main plugin;

    public AddLocation(Main plugin) {
        this.plugin = plugin;
        Objects.requireNonNull(plugin.getCommand("addlocation")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            plugin.utils.message(sender, "<red>Este comando solo puede ser ejecutado por un jugador.");
            return true;
        }

        Location loc = player.getLocation();
        plugin.locationManager.addNewLocation(loc);

        plugin.utils.message(player, "<green>Ubicación añadida correctamente en tu posición actual.");
        plugin.utils.message(player, "<gray>Coordenadas: X: " + loc.getBlockX() +
                ", Y: " + loc.getBlockY() +
                ", Z: " + loc.getBlockZ());

        return true;
    }
}
