package me.putindeer.puebloHG.game;

import me.putindeer.puebloHG.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
        winPoints = plugin.getConfig().getInt("win-points");
        secondPlacePoints = plugin.getConfig().getInt("second-place");
        thirdPlacePoints = plugin.getConfig().getInt("third-place");
        killPoints = plugin.getConfig().getInt("kill-points");
        survivePoints = plugin.getConfig().getInt("survive-points");
    }

    private void checkForWinner() {
        if (plugin.alivePlayers.size() <= 1) {
            endGame();
        }
    }

    public void endGame() {
        finalized = true;

        if (plugin.scatter.gameTimer != -1) {
            Bukkit.getScheduler().cancelTask(plugin.scatter.gameTimer);
            plugin.scatter.gameTimer = -1;
        }

        if (plugin.scatter.eventTimer != null) {
            plugin.scatter.eventTimer.cancel();
            plugin.scatter.eventTimer = null;
        }

        Player winner = Bukkit.getPlayer(plugin.alivePlayers.iterator().next());

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
            int countdown = 30;

            @Override
            public void run() {
                if (countdown <= 0) {
                    resetGame();
                    cancel();
                    return;
                }

                if (countdown == 30) {
                    plugin.utils.broadcast("&eLa partida se reiniciará en &630 segundos.");
                } else if (countdown <= 5) {
                    plugin.utils.broadcast("&cReiniciando en &4" + countdown + "...");
                }

                countdown--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void handleWinner(Player winner) {
        if (winner != null) {
            plugin.pointsManager.addPoints(winner.getUniqueId(), winPoints);
            plugin.utils.message(winner, "&a+" + winPoints + " puntos por ganar.");
            plugin.utils.broadcast("&e¡" + winner.getName() + " ha ganado la partida!");
            spawnFireworks(winner.getLocation());
            winner.showTitle(Title.title(
                    plugin.utils.chat("&a¡Has ganado la partida!"),
                    plugin.utils.chat("&eFelicidades."),
                    Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(3), Duration.ofSeconds(1))
            ));
        }
    }

    public void handleSecondPlace() {
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

    public void handleThirdPlace() {
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
        }

        plugin.alivePlayers.clear();
        plugin.scatter.scatter = false;
        started = false;
        finalized = false;
        plugin.scatter.timeLeft = 30 * 60;
    }

    private void deleteItems(World world) {
        world.getEntities().stream()
                .filter(entity -> entity instanceof Item || entity instanceof Arrow || entity instanceof SpectralArrow)
                .forEach(Entity::remove);
    }

    public void resetDoors() {
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

            plugin.utils.message(survivor, "&a+" + survivePoints + " punto por sobrevivir.");
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
        if (!plugin.gameManager.started) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        if (block.getBlockData() instanceof Openable) {
            doors.add(block.getLocation());
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!event.hasExplicitlyChangedBlock()) return;
        Player player = event.getPlayer();
        Location loc = player.getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        int radius = 12;
        int centerX = 25;
        int centerZ = 25;
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
}

