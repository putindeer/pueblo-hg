package me.putindeer.puebloHG.game;

import lombok.Getter;
import me.putindeer.puebloHG.Main;
import me.putindeer.puebloHG.config.locations.LocationManager;
import me.putindeer.puebloHG.utils.Utils;
import me.putindeer.puebloHG.config.game.GameEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.sound.Sound;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.stream.Collectors;

public class Scatter implements Listener {
    private final Main plugin;
    public BukkitTask timer;
    public final List<Location> locations = new ArrayList<>();
    public final List<Player> scattering = new ArrayList<>();
    @Getter
    public boolean scatter = false;
    @Getter
    public int time;

    public Scatter(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.locationManager = new LocationManager(plugin);
    }

    public void scatter() {
        World world = Bukkit.getWorld("world");
        assert world != null;
        world.setPVP(false);
        scatter = true;
        plugin.utils.broadcast("&7La partida está por comenzar. Esperen un momento.");
        Iterator<Location> iterator = locations.iterator();
        List<Player> players = Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getGameMode() != GameMode.SPECTATOR)
                .collect(Collectors.toList());
        Collections.shuffle(players);
        Iterator<Player> playerIterator = players.iterator();
        final int[] scattered = {0};
        int maximumPlayers = Math.min(players.size(), 44);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!iterator.hasNext()) {
                    while (playerIterator.hasNext()) {
                        Player p = playerIterator.next();
                        p.setGameMode(GameMode.SPECTATOR);
                        p.teleport(new Location(Bukkit.getWorld("world"), -0.5, 77, 0.5));
                        plugin.utils.message(p, "&7Ahora estás en modo espectador ya que el límite de jugadores se alcanzó.");
                    }
                    start();
                    cancel();
                    return;
                }

                if (playerIterator.hasNext()) {
                    Location nextLocation = iterator.next();
                    Player p = playerIterator.next();
                    p.teleportAsync(nextLocation);
                    p.setGameMode(GameMode.SURVIVAL);
                    Utils.restorePlayer(p);
                    scattering.add(p);
                    plugin.alivePlayers.add(p.getUniqueId());
                    scattered[0]++;
                    plugin.utils.broadcast(false, "&8[&7" + scattered[0] + "&8/&7" + maximumPlayers + "&8] &3" + p.getName());
                } else {start(); cancel();}
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    private void start() {
        plugin.utils.broadcast("&7El TP ha terminado. La partida comenzará en 10 segundos.");
        String[] colors = {
                "&#00FF00", "&#1CE300", "&#39C600", "&#55AA00", "&#718E00",
                "&#8E7100", "&#AA5500", "&#C63900", "&#E31C00", "&#FF0000"
        };

        final int[] counter = {10};

        new BukkitRunnable() {
            @Override
            public void run() {
                if (counter[0] <= 0) {
                    plugin.totalPlayers = scattering.size();
                    scattering.clear();
                    Bukkit.getOnlinePlayers().forEach(p -> p.showTitle(Title.title(plugin.utils.chat("&b¡La partida ha empezado!"), plugin.utils.chat("&cBuena suerte"))));
                    Bukkit.getOnlinePlayers().forEach(player -> player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 600, 9)));

                    plugin.gameManager.started = true;
                    Objects.requireNonNull(Bukkit.getWorld("world")).getWorldBorder().setSize(800);

                    startTimers();
                    cancel();
                    return;
                }

                String color = colors[10 - counter[0]];
                int finalI = counter[0];

                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.showTitle(Title.title(plugin.utils.chat(color + finalI), Component.empty()));
                    player.playSound(Sound.sound(Key.key("block.note_block.harp"), net.kyori.adventure.sound.Sound.Source.MASTER, 1.0f, 1f));
                }

                counter[0]--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!e.hasExplicitlyChangedBlock()) return;
        Player p = e.getPlayer();
        if (!scattering.contains(p)) return;
        p.sendActionBar(plugin.utils.chat("&cNo tienes permitido moverte aún."));
        e.setCancelled(true);
    }

    private void startTimers() {
        time = 0;
        timer = new BukkitRunnable() {
            @Override
            public void run() {
                for (GameEvent event : plugin.events) {
                    if (time == event.triggerTime()) {
                        event.action().run();
                    }
                }

                int timeUntilNextEvent = getTimeUntilNextEvent(plugin.events);

                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendActionBar(plugin.utils.chat("&ePróximo evento en: &c" + plugin.utils.formatTime(timeUntilNextEvent)));
                }

                if (!plugin.gameManager.finalized) {
                    plugin.timer = plugin.utils.formatTime(time);
                }

                time++;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private int getTimeUntilNextEvent(List<GameEvent> events) {
        int timeUntilNextEvent = -1;

        for (GameEvent event : events) {
            int eventTime = event.triggerTime();
            if (eventTime < time) {
                int diff = time - eventTime;
                if (timeUntilNextEvent == -1 || diff < timeUntilNextEvent) {
                    timeUntilNextEvent = diff;
                }
            }
        }
        return timeUntilNextEvent;
    }
}
