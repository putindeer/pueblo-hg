package me.putindeer.puebloHG.game;

import lombok.Getter;
import me.putindeer.puebloHG.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

import static me.putindeer.puebloHG.commands.Restock.restock;

public class Scatter implements Listener {
    private final Main plugin;
    List<Player> scattering = new ArrayList<>();
    @Getter
    public boolean scatter = false;
    @Getter
    public int timeLeft;

    public Scatter(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void scatter() {
        World world = Bukkit.getWorld("world");
        assert world != null;
        world.setPVP(false);
        scatter = true;
        plugin.utils.broadcast("&8[&3HG&8] &7La partida está por comenzar. Esperen un momento.");
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
                        plugin.utils.message(p, "&8[&3HG&8] &7Ahora estás en modo espectador ya que el límite de jugadores se alcanzó.");
                    }
                    start();
                    cancel();
                    return;
                }

                if (playerIterator.hasNext()) {
                    Location nextLocation = iterator.next();
                    Player p = playerIterator.next();
                    p.teleportAsync(nextLocation);
                    scattering.add(p);
                    scattered[0]++;
                    plugin.utils.broadcast("&8[&7" + scattered[0] + "&8/&7" + maximumPlayers + "&8] &3" + p.getName());
                } else {start(); cancel();}
            }
        }.runTaskTimer(plugin, 0L, 5L);

    }

    public void start() {
        plugin.utils.broadcast("&8[&3HG&8] &7El TP ha terminado. La partida comenzará en 10 segundos.");
        String[] colors = {
                "&#00FF00", "&#1CE300", "&#39C600", "&#55AA00", "&#718E00",
                "&#8E7100", "&#AA5500", "&#C63900", "&#E31C00", "&#FF0000"
        };

        final int[] counter = {10};

        new BukkitRunnable() {
            @Override
            public void run() {
                if (counter[0] <= 0) {
                    Main.alivePlayers = scattering.stream().map(Player::getName).collect(Collectors.toSet());
                    scattering.clear();
                    Bukkit.getOnlinePlayers().forEach(p -> p.showTitle(Title.title(plugin.utils.chat("&b¡La partida ha empezado!"), plugin.utils.chat("&cBuena suerte"))));
                    plugin.getServer().getOnlinePlayers().forEach(player -> player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 600, 9)));

                    plugin.started = true;
                    Objects.requireNonNull(Bukkit.getWorld("world")).getWorldBorder().setSize(800);
                    time();

                    startTimers();
                    cancel();
                    return;
                }

                String color = colors[10 - counter[0]];
                int finalI = counter[0];

                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.showTitle(Title.title(plugin.utils.chat(color + finalI), Component.empty()));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1, 1);
                }

                counter[0]--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!e.hasExplicitlyChangedBlock()) return;
        Player p = e.getPlayer();
        if (!scattering.contains(p)) return;
        p.sendActionBar(plugin.utils.chat("&cNo tienes permitido moverte aún."));
        e.setCancelled(true);
    }

    List<Location> locations = Arrays.asList(
            new Location(Bukkit.getWorld("world"), 12.5, 73, 13.5),
            new Location(Bukkit.getWorld("world"), 8.5, 73, 15.5),
            new Location(Bukkit.getWorld("world"), 3.5, 73, 17.5),
            new Location(Bukkit.getWorld("world"), -0.5, 73, 18.5),
            new Location(Bukkit.getWorld("world"), -4.5, 73, 17.5),
            new Location(Bukkit.getWorld("world"), -9.5, 73, 15.5),
            new Location(Bukkit.getWorld("world"), -13.5, 73, 13.5),
            new Location(Bukkit.getWorld("world"), -15.5, 73, 9.5),
            new Location(Bukkit.getWorld("world"), -17.5, 73, 4.5),
            new Location(Bukkit.getWorld("world"), -18.5, 73, 0.5),
            new Location(Bukkit.getWorld("world"), -17.5, 73, -3.5),
            new Location(Bukkit.getWorld("world"), -15.5, 73, -8.5),
            new Location(Bukkit.getWorld("world"), -13.5, 73, -12.5),
            new Location(Bukkit.getWorld("world"), -9.5, 73, -14.5),
            new Location(Bukkit.getWorld("world"), -4.5, 73, -16.5),
            new Location(Bukkit.getWorld("world"), -0.5, 73, -17.5),
            new Location(Bukkit.getWorld("world"), 3.5, 73, -16.5),
            new Location(Bukkit.getWorld("world"), 8.5, 73, -14.5),
            new Location(Bukkit.getWorld("world"), 12.5, 73, -12.5),
            new Location(Bukkit.getWorld("world"), 14.5, 73, -8.5),
            new Location(Bukkit.getWorld("world"), 16.5, 73, -3.5),
            new Location(Bukkit.getWorld("world"), 17.5, 73, 0.5),
            new Location(Bukkit.getWorld("world"), 16.5, 73, 4.5),
            new Location(Bukkit.getWorld("world"), 14.5, 73, 9.5),
            new Location(Bukkit.getWorld("world"), 11.5, 73, -3.5),
            new Location(Bukkit.getWorld("world"), 12.5, 73, 0.5),
            new Location(Bukkit.getWorld("world"), 11.5, 73, 4.5),
            new Location(Bukkit.getWorld("world"), 10.5, 73, 8.5),
            new Location(Bukkit.getWorld("world"), 7.5, 73, 11.5),
            new Location(Bukkit.getWorld("world"), 3.5, 73, 12.5),
            new Location(Bukkit.getWorld("world"), -0.5, 73, 13.5),
            new Location(Bukkit.getWorld("world"), -4.5, 73, 12.5),
            new Location(Bukkit.getWorld("world"), -8.5, 73, 11.5),
            new Location(Bukkit.getWorld("world"), -11.5, 73, 8.5),
            new Location(Bukkit.getWorld("world"), -12.5, 73, 4.5),
            new Location(Bukkit.getWorld("world"), -13.5, 73, 0.5),
            new Location(Bukkit.getWorld("world"), -12.5, 73, -3.5),
            new Location(Bukkit.getWorld("world"), -11.5, 73, -7.5),
            new Location(Bukkit.getWorld("world"), -8.5, 73, -10.5),
            new Location(Bukkit.getWorld("world"), -4.5, 73, -11.5),
            new Location(Bukkit.getWorld("world"), -0.5, 73, -12.5),
            new Location(Bukkit.getWorld("world"), 3.5, 73, -11.5),
            new Location(Bukkit.getWorld("world"), 7.5, 73, -10.5),
            new Location(Bukkit.getWorld("world"), 10.5, 73, -7.5)
    );

    public void startTimers() {
        timeLeft = 30 * 60;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (timeLeft <= 0) {
                    plugin.utils.broadcast("&8[&3HG&8] &4¡El mapa se ha cerrado completamente!");
                    cancel();
                    return;
                }

                int nextEventTime = 0;
                if (timeLeft > 29 * 60 + 30) nextEventTime = 29 * 60 + 30;
                else if (timeLeft > 15 * 60) nextEventTime = 15 * 60;
                else if (timeLeft > 10 * 60) nextEventTime = 10 * 60;
                else if (timeLeft > 5 * 60) nextEventTime = 5 * 60;

                int timeUntilNextEvent = timeLeft - nextEventTime;

                switch (timeLeft) {
                    case 29 * 60 + 30 -> {
                        World world = Bukkit.getWorld("world");
                        assert world != null;
                        world.setPVP(true);
                        plugin.utils.broadcast("&8[&3HG&8] &cEl PvP ha sido activado. Buena suerte.", Sound.ENTITY_WITHER_SPAWN);
                    }
                    case 15 * 60 -> {
                        plugin.utils.broadcast("&8[&3HG&8] &eEl borde del mundo se empezó a reducir. Se reducirá completamente en 15 minutos.");
                        Objects.requireNonNull(Bukkit.getWorld("world")).getWorldBorder().setSize(74, 900);
                    }
                    case 10 * 60 -> {
                        plugin.utils.broadcast("&8[&3HG&8] &6¡Los cofres han sido reabastecidos!");
                        restock();
                    }
                    case 5 * 60 -> {
                        plugin.utils.broadcast("&8[&3HG&8] &dEl borde ahora se cerrará verticalmente.");
                    }
                }

                int minutes = timeUntilNextEvent / 60;
                int seconds = timeUntilNextEvent % 60;
                String formattedTime = String.format("%02d:%02d", minutes, seconds);

                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendActionBar(plugin.utils.chat("&ePróximo evento en: &c" + formattedTime));
                }

                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void time() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            int min = 0;
            int sec = 0;

            @Override
            public void run() {
                if (!plugin.finalized) {
                    sec++;
                    if (sec == 60) {
                        sec = 0;
                        min++;
                    }

                    plugin.timer = String.format("%02d:%02d", min, sec);
                }
            }
        }, 0L, 20L);
    }
}
