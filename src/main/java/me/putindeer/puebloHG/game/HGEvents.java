package me.putindeer.puebloHG.game;

import com.destroystokyo.paper.event.block.AnvilDamagedEvent;
import fr.mrmicky.fastboard.adventure.FastBoard;
import me.putindeer.puebloHG.Main;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;

public class HGEvents implements Listener {
    private final Main plugin;

    public HGEvents(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        event.joinMessage(plugin.utils.chat("&8(&a+&8) " + p.getName()));

        FastBoard board = new FastBoard(p);
        board.updateTitle(plugin.utils.chat("&3&lVenezuela Games"));
        plugin.boards.put(p.getUniqueId(), board);
        p.sendPlayerListHeaderAndFooter(
                plugin.utils.chat("&3&lVenezuela Games"),
                plugin.utils.chat("&7Ping: &3" + p.getPing() + " &8| &7Tps: &3" + new DecimalFormat("##").format(plugin.getServer().getTPS()[0])));

        if (p.getGameMode() == GameMode.SURVIVAL) {
            Location loc = new Location(Bukkit.getWorld("world"), -999.5, 100, 1000.5);
            p.teleport(loc);
            restorePlayer(p);
        }
    }

    @EventHandler
    public void onLeft(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        e.quitMessage(plugin.utils.chat("&8(&c-&8) " + p.getName()));
        FastBoard board = plugin.boards.remove(p.getUniqueId());
        if (board != null) {
            board.delete();
        }
    }

    private void restorePlayer(Player p) {
        p.getInventory().clear();
        plugin.utils.setMaxHealth(p);
        p.setFoodLevel(20);
        p.setSaturation(5.0f);
        p.getActivePotionEffects().forEach(potionEffect -> p.removePotionEffect(potionEffect.getType()));
        p.setLevel(0);
        p.setExp(0.0f);
        p.setFireTicks(0);
        p.setItemOnCursor(new ItemStack(Material.AIR));
        p.setInvulnerable(false);
    }

    private void deleteItems(World world) {
        world.getEntities().stream()
                .filter(entity -> entity instanceof Item || entity instanceof Arrow || entity instanceof SpectralArrow)
                .forEach(Entity::remove);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
        switch (event.getInventory().getType()) {
            case BARREL, FURNACE, SMOKER, BLAST_FURNACE, HOPPER -> event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
        if (event.getClickedBlock() == null) return;
        Material type = event.getClickedBlock().getType();
        if (type.name().startsWith("POTTED_") || type.name().endsWith("_TRAPDOOR") || type.name().endsWith("_CANDLE")) {
            event.setCancelled(true);
        }
        switch (type) {
            case CHISELED_BOOKSHELF, FLOWER_POT, SWEET_BERRY_BUSH, CANDLE, BREWING_STAND, CAULDRON,
                 WATER_CAULDRON, LAVA_CAULDRON, POWDER_SNOW_CAULDRON -> event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemFrameInteract(PlayerInteractEntityEvent event) {
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;
        if (event.getRightClicked().getType() == EntityType.ITEM_FRAME) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        Player p = event.getPlayer();
        if (p.getGameMode().equals(GameMode.CREATIVE)) return;
        event.setCancelled(true);
        event.setUseBed(Event.Result.DENY);
        p.setRespawnLocation(null);
    }

    @EventHandler
    public void onAnvil(AnvilDamagedEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onHangingPlace(HangingPlaceEvent event) {
        if (event.getPlayer() != null && event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Player p && p.getGameMode() == GameMode.CREATIVE) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void cancelGrief(EntityExplodeEvent event) {
        if (event.getEntity() instanceof TNTPrimed || event.getEntity() instanceof Creeper) {
            event.blockList().clear();
        }
    }

    @EventHandler
    public void onArmorStandExplode(EntityDamageEvent event) {
        if (event.getEntity().getType() != EntityType.ARMOR_STAND) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION ||
                event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onArmorStandDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity().getType() != EntityType.ARMOR_STAND) return;
        if (!(event.getDamager() instanceof Player p)) return;
        if (p.getGameMode() != GameMode.SURVIVAL) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onSnowballHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Snowball snowball) {
            if (event.getHitEntity() instanceof Player player) {
                player.damage(0.0000001);
                player.getWorld().playSound(player.getLocation(), "minecraft:entity.player.hurt", 1.0f, 1.0f);
                Vector knockback = snowball.getVelocity().normalize().multiply(0.8);
                knockback.setY(0.4);
                player.setVelocity(knockback);
            }
        }
    }

    @EventHandler
    public void onDecoratedPotHit(ProjectileHitEvent event) {
        if (event.getHitBlock() != null && event.getHitBlock().getType() == Material.DECORATED_POT) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.getBlock().getType() == Material.DECORATED_POT) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.setShouldDropExperience(false);
        if (plugin.started) {
            Player p = event.getPlayer();
            p.teleport(new Location(p.getWorld(), 0, 94, 11));
            p.setGameMode(GameMode.SPECTATOR);
        }
    }
}
