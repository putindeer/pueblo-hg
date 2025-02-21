package me.putindeer.puebloHG.game;

import me.putindeer.puebloHG.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class PointsManager {
    private final Main plugin;
    private File file;
    private FileConfiguration config;

    public PointsManager(Main plugin) {
        this.plugin = plugin;
        setup();
    }

    public void setup() {
        file = new File(plugin.getDataFolder(), "points.yml");

        if (!file.exists()) {
            try {
                boolean created = file.createNewFile();
                if (!created) {
                    plugin.getLogger().warning("El archivo points.yml ya existe.");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("No se pudo crear points.yml debido a un error: " + e.getMessage());
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    public int getPoints(UUID uuid) {
        return config.getInt(uuid.toString(), 0);
    }

    public void addPoints(UUID uuid, int amount) {
        assert uuid != null;
        int newPoints = getPoints(uuid) + amount;
        config.set(uuid.toString(), newPoints);
        save();
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("No se pudo guardar puntos.yml");
        }
    }
}

