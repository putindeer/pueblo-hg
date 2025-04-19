package me.putindeer.puebloHG.config.locations;

import me.putindeer.puebloHG.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class LocationManager {
    private final Main plugin;
    private final File file;
    private FileConfiguration config;

    public LocationManager(Main plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "locations.yml");
        setup();
    }

    private void setup() {
        if (!file.exists()) {
            try {
                boolean created = file.createNewFile();
                if (!created) {
                    plugin.getLogger().warning("El archivo locations.yml ya existe.");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("No se pudo crear locations.yml: " + e.getMessage());
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        loadLocationsFromConfig();
    }

    private void loadLocationsFromConfig() {
        ConfigurationSection section = config.getConfigurationSection("locations");

        if (section == null) {
            plugin.getLogger().severe("No se encontró la sección 'locations' en el config.yml. No habrán localizaciones.");
            return;
        }

        World world = Bukkit.getWorld("world");
        if (world == null) {
            plugin.getLogger().severe("No se encontró el mundo 'world'. No habrán localizaciones.");
            return;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection locationSection = section.getConfigurationSection(key);
            if (locationSection == null) continue;

            double x = locationSection.getDouble("x");
            double y = locationSection.getDouble("y");
            double z = locationSection.getDouble("z");

            Location location = new Location(world, x, y, z);
            plugin.scatter.locations.add(location);
        }

        if (plugin.scatter.locations.isEmpty()) {
            plugin.getLogger().warning("No se cargaron ubicaciones válidas desde config.yml. No habrán localizaciones.");
        } else {
            plugin.getLogger().info("Se cargaron " + plugin.scatter.locations.size() + " ubicaciones desde config.yml.");
        }
    }

    public void addNewLocation(Location location) {
        int nextLocationNumber = getNextLocationNumber();
        String locationName = "location-" + nextLocationNumber;

        if (config.getConfigurationSection("locations") == null) {
            config.createSection("locations");
        }

        ConfigurationSection locationSection = config.createSection("locations." + locationName);

        locationSection.set("x", location.getX());
        locationSection.set("y", location.getY());
        locationSection.set("z", location.getZ());

        try {
            config.save(file);
            plugin.scatter.locations.add(location);
            plugin.getLogger().info("Ubicación '" + locationName + "' guardada correctamente.");
        } catch (IOException e) {
            plugin.getLogger().severe("No se pudo guardar la ubicación '" + locationName + "': " + e.getMessage());
        }
    }

    private int getNextLocationNumber() {
        ConfigurationSection locationsSection = config.getConfigurationSection("locations");
        if (locationsSection == null) return 1;

        Set<String> keys = locationsSection.getKeys(false);
        if (keys.isEmpty()) return 1;

        int highestNumber = 0;
        for (String key : keys) {
            if (key.startsWith("location-")) {
                try {
                    int num = Integer.parseInt(key.substring(9));
                    if (num > highestNumber) {
                        highestNumber = num;
                    }
                } catch (NumberFormatException ignored) {
                    // Se ignoran las claves con formato incorrecto
                }
            }
        }

        return highestNumber + 1;
    }
}
