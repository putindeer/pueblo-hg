package me.putindeer.puebloHG.game;

import me.putindeer.puebloHG.Main;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class VerticalBorder {
    private final Main plugin;
    private int minY, maxY;
    private double y1, y2;
    private double targetMinY, targetMaxY;
    private int transitionTime;
    private boolean shrinking = false;

    private BukkitRunnable borderShrinkTask, damageTask, borderDisplayTask, particleTask;

    public VerticalBorder(Main plugin) {
        this.plugin = plugin;
        loadConfigValues();
    }

    public void loadConfigValues() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("verticalborder");
        assert section != null;
        minY = section.getInt("minY", 30);
        maxY = section.getInt("maxY", 122);
        targetMinY = section.getDouble("targetMinY", 71);
        targetMaxY = section.getDouble("targetMaxY", 81);
    }

    public void start(int time) {
        transitionTime = time * 20;
        startBorderShrinkTask();
        startDamageTask();
        startBorderDisplayTask();
        startParticleTask();
    }

    public void stop() {
        if (borderShrinkTask != null) {
            borderShrinkTask.cancel();
            shrinking = false;
            borderShrinkTask = null;
        }
        if (damageTask != null) {
            damageTask.cancel();
            damageTask = null;
        }
        if (borderDisplayTask != null) {
            borderDisplayTask.cancel();
            borderDisplayTask = null;
        }
        if (particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }
    }

    private void startBorderShrinkTask() {
        borderShrinkTask = new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= transitionTime) {
                    this.cancel();
                    shrinking = false;
                    borderShrinkTask = null;
                    return;
                }

                double progress = (double) ticks / transitionTime;
                y1 = minY + (targetMinY - minY) * progress;
                y2 = maxY + (targetMaxY - maxY) * progress;

                ticks += 10;
            }
        };
        borderShrinkTask.runTaskTimer(plugin, 0, 10);
        shrinking = true;
    }

    private void startDamageTask() {
        damageTask = new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().stream()
                        .filter(p -> p.getGameMode().equals(GameMode.SURVIVAL))
                        .forEach(player -> {
                            double y = player.getLocation().getY();
                            if (y < y1 || y > y2) {
                                player.damage(4.0);
                            }
                        });
            }
        };
        damageTask.runTaskTimer(plugin, 0, 20);
    }

    private void startBorderDisplayTask() {
        borderDisplayTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    double y = player.getLocation().getY();
                    World world = player.getWorld();
                    WorldBorder realBorder = world.getWorldBorder();

                    if ((y - y1) <= 3 || (y2 - y) <= 3) {
                        WorldBorder fakeBorder = Bukkit.createWorldBorder();

                        fakeBorder.setCenter(realBorder.getCenter());
                        fakeBorder.setDamageAmount(realBorder.getDamageAmount());
                        fakeBorder.setDamageBuffer(realBorder.getDamageBuffer());
                        fakeBorder.setSize(realBorder.getSize());
                        if (shrinking) {
                            fakeBorder.setSize(realBorder.getSize() - 0.001, 1);
                        }
                        fakeBorder.setWarningTime(realBorder.getWarningTime());
                        fakeBorder.setWarningDistance(300000000);
                        player.setWorldBorder(fakeBorder);
                    }
                    else {
                        player.setWorldBorder(realBorder);
                    }
                }
            }
        };
        borderDisplayTask.runTaskTimer(plugin, 0, 1);
    }

    private void startParticleTask() {
        particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    if (world.getPlayers().isEmpty()) continue;
                    spawnBorderParticles(world, y1, false);
                    spawnBorderParticles(world, y2, true);
                }
            }
        };
        particleTask.runTaskTimer(plugin, 0, 20);
    }

    private void spawnBorderParticles(World world, double y, boolean isMaxY) {
        int borderSize = (int) world.getWorldBorder().getSize() / 2;
        Location center = world.getWorldBorder().getCenter();

        Particle.DustOptions options = new Particle.DustOptions(Color.RED, 3);
        int particleSpacing = 2;

        for (int x = -borderSize; x <= borderSize; x += particleSpacing) {
            for (int z = -borderSize; z <= borderSize; z += particleSpacing) {
                world.spawnParticle(Particle.DUST, new Location(world, center.getX() + x, isMaxY ? y + 2 : y, center.getZ() + z), 1, options);
            }
        }
    }
}
