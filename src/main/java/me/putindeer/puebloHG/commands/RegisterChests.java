package me.putindeer.puebloHG.commands;

import me.putindeer.puebloHG.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static me.putindeer.puebloHG.commands.Restock.locations;

public class RegisterChests implements CommandExecutor {
    private final Main plugin;

    public RegisterChests(Main plugin) {
        this.plugin = plugin;
        Objects.requireNonNull(plugin.getCommand("registerchests")).setExecutor(this);
        setup();
        loadChestLocations();
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        saveChestLocations();
        plugin.utils.message(sender, "&7Se han guardado los cofres en el archivo exitosamente.");
        return true;
    }

    public void setup() {
        File file = new File(plugin.getDataFolder(), "chests.yml");

        if (!file.exists()) {
            try {
                boolean created = file.createNewFile();
                if (!created) {
                    plugin.getLogger().warning("El archivo chests.yml ya existe.");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("No se pudo crear chests.yml debido a un error: " + e.getMessage());
            }
        }
    }

    public void saveChestLocations() {
        File file = new File(plugin.getDataFolder(), "chests.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        World world = Bukkit.getWorld("world");
        int minY = 30;
        int maxY = 110;

        for (int x = -400; x <= 400; x++) {
            for (int z = -400; z <= 400; z++) {
                for (int y = minY; y <= maxY; y++) {
                    Location loc = new Location(world, x, y, z);
                    Block block = loc.getBlock();
                    if (block.getType() == Material.CHEST) {
                        locations.add(loc);
                        config.set("chests." + loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ(), true);
                    }
                }
            }
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Ocurrió un error al guardar los cofres: " + e);
        }
    }

    public void loadChestLocations() {
        File file = new File(plugin.getDataFolder(), "chests.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        if (config.contains("chests")) {
            for (String key : Objects.requireNonNull(config.getConfigurationSection("chests")).getKeys(false)) {
                String[] parts = key.split(",");
                if (parts.length == 4) {
                    String worldName = parts[0];
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    int z = Integer.parseInt(parts[3]);

                    World world = Bukkit.getWorld(worldName);
                    if (world != null) {
                        Location location = new Location(world, x, y, z);
                        locations.add(location);
                    }
                }
            }
        }
    }
}
