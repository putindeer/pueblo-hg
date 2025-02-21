package me.putindeer.puebloHG.commands;

import me.putindeer.puebloHG.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ForceEnd implements CommandExecutor {
    private final Main plugin;

    public ForceEnd(Main plugin) {
        this.plugin = plugin;
        Objects.requireNonNull(plugin.getCommand("forceend")).setExecutor(this);
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        plugin.gameManager.endGame();
        plugin.utils.message(sender, "&7La partida se ha finalizado de manera forzada.");
        return true;
    }
}
