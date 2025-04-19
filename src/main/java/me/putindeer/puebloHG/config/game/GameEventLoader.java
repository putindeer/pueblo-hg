package me.putindeer.puebloHG.config.game;

import me.putindeer.puebloHG.Main;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static me.putindeer.puebloHG.commands.Restock.restock;

public class GameEventLoader {

    private final Main plugin;
    private final Map<GameEventType, Integer> eventTimes = new HashMap<>();

    public GameEventLoader(Main plugin) {
        this.plugin = plugin;
    }

    public List<GameEvent> loadEventsFromConfig() {
        List<GameEvent> events = new ArrayList<>();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("game-events");

        if (section == null) return events;

        for (String key : section.getKeys(false)) {
            String typeStr = section.getString(key + ".type");
            int time = section.getInt(key + ".time");

            if (typeStr == null) continue;

            try {
                GameEventType type = GameEventType.valueOf(typeStr.toUpperCase());
                eventTimes.put(type, time);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Evento no reconocido: " + typeStr);
            }
        }

        for (String key : section.getKeys(false)) {
            String typeStr = section.getString(key + ".type");
            int time = section.getInt(key + ".time");

            if (typeStr == null) continue;

            try {
                GameEventType type = GameEventType.valueOf(typeStr.toUpperCase());
                Runnable action = getActionForType(type);
                events.add(new GameEvent(type, time, action));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Evento no reconocido, otra vez: " + typeStr);
            }
        }

        return events;
    }

    private Runnable getActionForType(GameEventType type) {
        return switch (type) {
            case ENABLE_PVP -> () -> {
                World world = Bukkit.getWorld("world");
                assert world != null;
                world.setPVP(true);
                plugin.utils.broadcast(Sound.sound(Key.key("entity.wither.spawn"), Sound.Source.MASTER, 1.0f, 1f),
                        "&cEl PvP ha sido activado. Buena suerte.");
            };
            case SHRINK_BORDER -> () -> {
                int shrinkDuration = calculateDuration(GameEventType.SHRINK_BORDER, GameEventType.END_BORDER);

                plugin.utils.broadcast(Sound.sound(Key.key("entity.blaze.death"), Sound.Source.MASTER, 1.0f, 1f),
                        "&eEl borde del mundo se empezó a reducir. Se reducirá completamente en " + (shrinkDuration / 60) + " minutos.");

                Objects.requireNonNull(Bukkit.getWorld("world")).getWorldBorder().setSize(55, shrinkDuration);
            };
            case RESTOCK_CHESTS -> () -> {
                plugin.utils.broadcast(Sound.sound(Key.key("entity.villager.work_fletcher"), Sound.Source.MASTER, 1.0f, 1f),
                        "&6¡Los cofres han sido reabastecidos!");
                restock();
            };
            case VERTICAL_BORDER -> () -> {
                int verticalDuration = calculateDuration(GameEventType.VERTICAL_BORDER, GameEventType.END_VERTICAL_BORDER);

                plugin.utils.broadcast(Sound.sound(Key.key("entity.blaze.death"), Sound.Source.MASTER, 1.0f, 1f),
                        "&dEl borde ahora se cerrará verticalmente en " + (verticalDuration / 60) + " minutos.");

                plugin.verticalBorder.start(verticalDuration);
            };
            case END_VERTICAL_BORDER -> () -> plugin.utils.broadcast(Sound.sound(Key.key("entity.allay.death"), Sound.Source.MASTER, 1.0f, 0.1f),
                            "&4¡El borde vertical se ha cerrado completamente!");
            case END_BORDER -> () -> plugin.utils.broadcast(Sound.sound(Key.key("entity.allay.death"), Sound.Source.MASTER, 1.0f, 0.1f),
                            "&4¡El borde se ha cerrado completamente!");
            case GLOWING -> () -> {
                plugin.utils.broadcast(Sound.sound(Key.key("entity.allay.death"), Sound.Source.MASTER, 1.0f, 0.1f),
                        "&e¡Se ha dado Glowing a todos los jugadores!");

                Bukkit.getOnlinePlayers().stream()
                        .filter(player -> player.getGameMode() == GameMode.SURVIVAL)
                        .filter(player -> plugin.alivePlayers.contains(player.getUniqueId()))
                        .forEach(player -> player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0)));
            };
        };
    }

    /**
     * Calcula la duración entre dos eventos en segundos
     * @param startEvent El evento de inicio
     * @param endEvent El evento de fin
     * @return La duración en segundos, o un valor predeterminado si no se puede calcular
     */
    private int calculateDuration(GameEventType startEvent, GameEventType endEvent) {
        if (eventTimes.containsKey(startEvent) && eventTimes.containsKey(endEvent)) {
            int startTime = eventTimes.get(startEvent);
            int endTime = eventTimes.get(endEvent);

            if (endTime > startTime) {
                return endTime - startTime;
            }
        }

        if (startEvent == GameEventType.SHRINK_BORDER) {
            plugin.getLogger().warning("No se pudo calcular la duración del cierre del borde horizontal. Usando valor predeterminado (600 segundos).");
            return 600;
        } else if (startEvent == GameEventType.VERTICAL_BORDER) {
            plugin.getLogger().warning("No se pudo calcular la duración del cierre del borde vertical. Usando valor predeterminado (300 segundos).");
            return 300;
        }

        return 600;
    }
}