package me.putindeer.puebloHG.game;

import me.putindeer.puebloHG.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PointsManager {
    private final Main plugin;
    private final File file;
    private FileConfiguration config;
    private final Map<UUID, Integer> pointsCache = new HashMap<>();

    public PointsManager(Main plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "points.yml");
        setup();
    }

    private void setup() {
        if (!file.exists()) {
            try {
                boolean created = file.createNewFile();
                if (!created) {
                    plugin.getLogger().warning("El archivo points.yml ya existe.");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("No se pudo crear points.yml: " + e.getMessage());
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        loadPoints();
    }

    private void loadPoints() {
        for (String key : config.getKeys(false)) {
            pointsCache.put(UUID.fromString(key), config.getInt(key, 0));
        }
    }

    public int getPoints(UUID uuid) {
        return pointsCache.getOrDefault(uuid, 0);
    }

    public void addPoints(UUID uuid, int amount) {
        pointsCache.put(uuid, getPoints(uuid) + amount);
    }

    public void save() {
        for (Map.Entry<UUID, Integer> entry : pointsCache.entrySet()) {
            config.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("No se pudo guardar points.yml");
        }
    }

    public List<Map.Entry<UUID, Integer>> getTopPlayers() {
        List<Map.Entry<UUID, Integer>> sortedList = new ArrayList<>(pointsCache.entrySet());
        sortedList.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        return sortedList;
    }
}