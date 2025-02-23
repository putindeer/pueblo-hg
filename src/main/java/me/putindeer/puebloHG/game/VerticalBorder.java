package me.putindeer.puebloHG.game;

import me.putindeer.puebloHG.Main;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class VerticalBorder {
    private final Main plugin;
    private double minY1 = 30, maxY1 = 122;
    private final double targetMinY = 71, targetMaxY = 81;
    private final int transitionTime = 5 * 60 * 20;
    private boolean shrinking = false;

    private BukkitRunnable borderShrinkTask, damageTask, borderDisplayTask, particleTask;

    public VerticalBorder(Main plugin) {
        this.plugin = plugin;
    }

    public void start() {
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
                minY1 = 30 + (targetMinY - 30) * progress;
                maxY1 = 122 + (targetMaxY - 122) * progress;

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
                for (Player player : Bukkit.getOnlinePlayers()) {
                    double y = player.getLocation().getY();
                    World world = player.getWorld();
                    WorldBorder realBorder = world.getWorldBorder();

                    if (y < minY1 || y > maxY1) {
                        player.damage(4.0);
                    } else if ((y - minY1) <= 3 || (maxY1 - y) <= 3) {
                        WorldBorder fakeBorder = Bukkit.createWorldBorder();
                        fakeBorder.setCenter(realBorder.getCenter());
                        fakeBorder.setDamageAmount(realBorder.getDamageAmount());
                        fakeBorder.setDamageBuffer(realBorder.getDamageBuffer());
                        fakeBorder.setSize(realBorder.getSize());
                        if (shrinking) {
                            fakeBorder.setSize(realBorder.getSize() - 0.001, 1);
                        }
                        fakeBorder.setWarningDistance(300000000);
                        fakeBorder.setWarningTime(realBorder.getWarningTime());

                        player.setWorldBorder(fakeBorder);
                    } else {
                        player.setWorldBorder(realBorder);
                    }
                }
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

                    if ((y - minY1) <= 3 || (maxY1 - y) <= 3) {
                        WorldBorder fakeBorder = Bukkit.createWorldBorder();

                        fakeBorder.setCenter(realBorder.getCenter());
                        fakeBorder.setDamageAmount(realBorder.getDamageAmount());
                        fakeBorder.setDamageBuffer(realBorder.getDamageBuffer());
                        fakeBorder.setSize(realBorder.getSize());
                        fakeBorder.setSize(realBorder.getSize() - 0.001, 1);
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
                    spawnBorderParticles(world, minY1, false);
                    spawnBorderParticles(world, maxY1, true);
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
