package me.putindeer.puebloHG.game;

import me.putindeer.puebloHG.Main;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.sound.Sound;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Openable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static me.putindeer.puebloHG.commands.Restock.restock;

public class GameManager implements Listener {
    private final Main plugin;
    public boolean started = false;
    public boolean finalized = false;
    int winPoints;
    int secondPlacePoints;
    int thirdPlacePoints;
    int killPoints;
    int survivePoints;
    private final Set<Location> doors = new HashSet<>();

    public GameManager(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        assignPointsValues();
    }

    private void assignPointsValues() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("points");
        assert section != null;
        winPoints = section.getInt("win-points");
        secondPlacePoints = section.getInt("second-place");
        thirdPlacePoints = section.getInt("third-place");
        killPoints = section.getInt("kill-points");
        survivePoints = section.getInt("survive-points");
    }

    private void checkForWinner() {
        if (plugin.alivePlayers.size() <= 1) {
            endGame();
        }
    }

    public void endGame() {
        finalized = true;

        plugin.scatter.stop();
        plugin.verticalBorder.stop();

        Player w = null;
        if (!plugin.alivePlayers.isEmpty()){
            w = Bukkit.getPlayer(plugin.alivePlayers.iterator().next());
        }
        final Player winner = w;

        handleWinner(winner);
        handleSecondPlace();
        handleThirdPlace();

        Bukkit.getOnlinePlayers().stream().filter(p -> !p.equals(winner))
                .forEach(p -> p.showTitle(Title.title(
                        plugin.utils.chat("&6La partida ha finalizado"),
                        plugin.utils.chat(winner != null ? "&aGanador&f: " + winner.getName() : ""),
                        Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(3), Duration.ofSeconds(1))
                )));

        new BukkitRunnable() {
            int countdown = 20;

            @Override
            public void run() {
                if (countdown <= 0) {
                    resetGame();
                    cancel();
                    return;
                }

                if (countdown == 20) {
                    plugin.utils.broadcast("&eLa partida se reiniciará en &620 segundos&e.");
                } else if (countdown <= 5) {
                    plugin.utils.broadcast("&cReiniciando en &4" + countdown + "...");
                }

                countdown--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void handleWinner(Player winner) {
        if (winner != null) {
            plugin.pointsManager.addPoints(winner.getUniqueId(), winPoints);
            plugin.utils.message(winner, "&a+" + winPoints + " puntos por ganar.");
            plugin.utils.broadcast(Sound.sound(Key.key("entity.wither.death"), Sound.Source.MASTER, 10f, 1f), "&e¡" + winner.getName() + " ha ganado la partida!");
            spawnFireworks(winner.getLocation());
            winner.showTitle(Title.title(
                    plugin.utils.chat("&a¡Has ganado la partida!"),
                    plugin.utils.chat("&eFelicidades."),
                    Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(3), Duration.ofSeconds(1))
            ));
        }
    }

    private void handleSecondPlace() {
        UUID secondPlace = getSecondPlace();
        if (secondPlace != null) {
            plugin.pointsManager.addPoints(secondPlace, secondPlacePoints);
            Player secondPlayer = Bukkit.getPlayer(secondPlace);
            if (secondPlayer != null) {
                plugin.utils.message(secondPlayer, "&a+" + secondPlacePoints + " puntos por quedar en segundo lugar.");
                plugin.utils.broadcast("&#C0C0C0¡" + secondPlayer.getName() + " ha quedado en segundo lugar!");
            } else {
                plugin.utils.broadcast("&7¡" + Bukkit.getOfflinePlayer(secondPlace).getName() + " ha quedado en segundo lugar!");
            }
        }
    }

    private UUID getSecondPlace() {
        if (!plugin.deadPlayers.isEmpty()) {
            return plugin.deadPlayers.getLast();
        }
        return null;
    }

    private void handleThirdPlace() {
        UUID thirdPlace = getThirdPlace();
        if (thirdPlace != null) {
            plugin.pointsManager.addPoints(thirdPlace, thirdPlacePoints);
            Player thirdPlayer = Bukkit.getPlayer(thirdPlace);
            if (thirdPlayer != null) {
                plugin.utils.message(thirdPlayer, "&a+" + thirdPlacePoints + " puntos por quedar en tercer lugar.");
                plugin.utils.broadcast("&#CD7F32¡" + thirdPlayer.getName() + " ha quedado en tercer lugar!");
            } else {
                plugin.utils.broadcast("&#CD7F32¡" + Bukkit.getOfflinePlayer(thirdPlace).getName() + " ha quedado en tercer lugar!");
            }
        }
    }

    private UUID getThirdPlace() {
        if (plugin.deadPlayers.size() >= 2) {
            return plugin.deadPlayers.get(plugin.deadPlayers.size() - 2);
        }
        return null;
    }

    private void resetGame() {
        World world = Bukkit.getWorld("world");
        if (world == null) return;

        world.getWorldBorder().setSize(6000);
        deleteItems(world);
        resetDoors();

        for (Player p : Bukkit.getOnlinePlayers()) {
            Location loc = new Location(Bukkit.getWorld("world"), -999.5, 100, 1000.5);
            p.teleport(loc);
            plugin.utils.restorePlayer(p);
            p.getInventory().clear();
            p.setStatistic(Statistic.PLAYER_KILLS, 0);
            p.setWorldBorder(p.getWorld().getWorldBorder());;
            p.setGameMode(GameMode.SURVIVAL);
        }

        plugin.alivePlayers.clear();
        plugin.deadPlayers.clear();
        plugin.scatter.scatter = false;
        plugin.totalPlayers = -1;
        started = false;
        finalized = false;
        plugin.scatter.time = 0;
        restock();
    }

    private void deleteItems(World world) {
        world.getEntities().stream()
                .filter(entity -> entity instanceof Item || entity instanceof Arrow || entity instanceof SpectralArrow)
                .forEach(Entity::remove);
    }

    private void resetDoors() {
        for (Location loc : doors) {
            Block block = loc.getBlock();
            if (block.getBlockData() instanceof Openable door) {
                door.setOpen(false);
                block.setBlockData(door);
            }
        }
        doors.clear();
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.setShouldDropExperience(false);

        if (started) {
            Player p = event.getPlayer();
            if (!plugin.alivePlayers.contains(p.getUniqueId())) return;
            Player killer = p.getKiller();

            Component deathMessage = handleDeathMessage(event.deathMessage(), p, killer);
            event.deathMessage(deathMessage);

            resetPlayerAfterDeath(p);

            handlePoints(p, killer);

            checkForWinner();
        }
    }

    private void resetPlayerAfterDeath(Player p) {
        plugin.alivePlayers.remove(p.getUniqueId());
        plugin.deadPlayers.add(p.getUniqueId());
        if (!p.isOnline()) return;

        Location loc = p.getLocation();
        p.setGameMode(GameMode.SPECTATOR);
        p.teleport(loc);
    }

    private Component handleDeathMessage(Component deathMessage, Player p, Player killer) {
        Component playerText = plugin.utils.chat("<white>" + p.getName() + " <gray>[<red>" + p.getStatistic(Statistic.PLAYER_KILLS) + "<gray>]</white>");
        TextReplacementConfig playerReplacement = TextReplacementConfig.builder()
                .matchLiteral(p.getName())
                .replacement(playerText)
                .build();
        deathMessage = deathMessage.replaceText(playerReplacement);

        if (killer != null) {
            Component killerText = plugin.utils.chat("<white>" + killer.getName() + " <gray>[<red>" + (killer.getStatistic(Statistic.PLAYER_KILLS) + 1) + "<gray>]</white>");
            TextReplacementConfig killerReplacement = TextReplacementConfig.builder()
                    .matchLiteral(killer.getName())
                    .replacement(killerText)
                    .build();
            deathMessage = deathMessage.replaceText(killerReplacement);
        }

        return plugin.utils.prefix.append(deathMessage.color(NamedTextColor.WHITE));
    }

    private void handlePoints(Player p, Player killer) {
        if (killer != null) {
            plugin.pointsManager.addPoints(killer.getUniqueId(), killPoints);
            plugin.utils.message(killer, "&6+" + killPoints + " puntos por matar a " + p.getName());
        }

        plugin.alivePlayers.forEach(uuid -> {
            plugin.pointsManager.addPoints(uuid, survivePoints);
            Player survivor = plugin.getServer().getPlayer(uuid);
            if (survivor == null) return;

            plugin.utils.message(survivor, Sound.sound(Key.key("entity.lightning_bolt.thunder"), Sound.Source.MASTER, 1.0f, 1f), "&a+" + survivePoints + " punto por sobrevivir.");
        });
    }

    private void spawnFireworks(Location location) {
        Firework fw = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK_ROCKET);
        FireworkMeta fwm = fw.getFireworkMeta();

        fwm.setPower(0);
        fwm.addEffect(FireworkEffect.builder().withColor(Color.FUCHSIA).flicker(true).build());

        fw.setFireworkMeta(fwm);
        fw.detonate();

        for (int i = 0; i < 3; i++) {
            plugin.utils.delay(i * 10, () -> {
                Firework fw2 = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK_ROCKET);
                fw2.setFireworkMeta(fwm);
            });
        }
    }

    @EventHandler
    public void onDoorInteract(PlayerInteractEvent event) {
        if (!started) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        if (block.getBlockData() instanceof Openable) {
            doors.add(block.getLocation());
        }
    }

    @EventHandler
    public void onWindChargeExplode(EntityExplodeEvent event) {
        if (event.getEntity() instanceof WindCharge) {
            for (Block block : event.blockList()) {
                if (block.getBlockData() instanceof Openable) {
                    doors.add(block.getLocation());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!started) return;
        if (!event.hasExplicitlyChangedBlock()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) return;
        Location loc = player.getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        int radius = 25;
        int centerX = -1;
        int centerZ = -1;
        boolean isInSafeZone = Math.abs(loc.getBlockX() - centerX) <= radius &&
                Math.abs(loc.getBlockZ() - centerZ) <= radius;

        int minY = 63;
        if (loc.getBlockY() < minY && !isInSafeZone) {
            if (!player.hasPotionEffect(PotionEffectType.DARKNESS)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, Integer.MAX_VALUE, 0, false, false));
            }
        } else {
            if (player.hasPotionEffect(PotionEffectType.DARKNESS)) {
                player.removePotionEffect(PotionEffectType.DARKNESS);
            }
        }
    }

    private final HashMap<UUID, Integer> remainingTime = new HashMap<>();

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!plugin.alivePlayers.contains(player.getUniqueId())) return;
        UUID uuid = player.getUniqueId();

        remainingTime.putIfAbsent(uuid, 60);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!remainingTime.containsKey(uuid)) {
                    cancel();
                    return;
                }

                int timeLeft = remainingTime.get(uuid) - 1;
                remainingTime.put(uuid, timeLeft);

                if (timeLeft <= 0) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (!Bukkit.getOfflinePlayer(uuid).isOnline()) {
                            plugin.utils.broadcast(handleDeathMessage(Component.text(player.getName() + " murió por desconectarse."), player, null));
                            resetPlayerAfterDeath(player);
                            handlePoints(player, null);
                            checkForWinner();
                        }
                        remainingTime.remove(uuid);
                    });
                    cancel();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 20L, 20L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        remainingTime.remove(event.getPlayer().getUniqueId());
    }
}