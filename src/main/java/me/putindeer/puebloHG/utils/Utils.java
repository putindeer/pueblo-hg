package me.putindeer.puebloHG.utils;

import lombok.NoArgsConstructor;
import me.putindeer.puebloHG.Main;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


@NoArgsConstructor
@SuppressWarnings("unused")
public class Utils {
    /**
     * Restaura completamente a un jugador
     * @param p El jugador a restaurar
     */
    public static void restorePlayer(Player p) {
        p.getInventory().clear();
        Main.pl.utils.setMaxHealth(p);
        p.setFoodLevel(20);
        p.setSaturation(5.0f);
        p.getActivePotionEffects().forEach(potionEffect -> p.removePotionEffect(potionEffect.getType()));
        p.setLevel(0);
        p.setExp(0.0f);
        p.setFireTicks(0);
        p.setItemOnCursor(new ItemStack(Material.AIR));
        p.setInvulnerable(false);
        p.setStatistic(Statistic.PLAYER_KILLS, 0);
        p.setWorldBorder(p.getWorld().getWorldBorder());
    }
}
