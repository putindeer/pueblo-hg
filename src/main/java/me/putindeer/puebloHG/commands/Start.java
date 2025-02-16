package me.putindeer.puebloHG.commands;

import me.putindeer.puebloHG.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Start implements CommandExecutor {
    private final Main plugin;

    public Start(Main plugin) {
        this.plugin = plugin;
        Objects.requireNonNull(plugin.getCommand("start")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String @NotNull [] strings) {
        if (plugin.scatter.isScatter()) {
            plugin.utils.message(sender, "&8[&3HG&8] &7La partida ya ha iniciado.");
            return false;
        }
        plugin.scatter.scatter();
        return false;
    }
}
