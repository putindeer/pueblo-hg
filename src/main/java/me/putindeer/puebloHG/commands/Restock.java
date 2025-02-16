package me.putindeer.puebloHG.commands;

import me.putindeer.puebloHG.Main;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Restock implements CommandExecutor {
    private final Main plugin;

    public Restock(Main plugin) {
        this.plugin = plugin;
        Objects.requireNonNull(plugin.getCommand("restock")).setExecutor(this);
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        int centerX = 0;
        int centerZ = 0;
        World world = Bukkit.getWorld("world");
        if (world == null) {
            plugin.utils.message(sender, "&8[&3HG&8] &7El mundo no se ha encontrado.");
            return false;
        }
        Random random = new Random();

        int minY = 55;
        int maxY = 85;

        for (int x = -400; x <= 400; x++) {
            for (int z = -400; z <= 400; z++) {
                for (int y = minY; y <= maxY; y++) {
                    Location loc = new Location(world, centerX + x, y, centerZ + z);
                    Block block = loc.getBlock();

                    if (block.getType() == Material.CHEST) {
                        Chest chest = (Chest) block.getState();

                        LootTable lootTable = getRandomLootTable(random);
                        LootContext context = new LootContext.Builder(loc).lootedEntity((Player) sender).build();

                        Inventory chestInventory = chest.getInventory();
                        chestInventory.clear();

                        lootTable.fillInventory(chestInventory, random, context);
                    }
                }
            }
        }

        plugin.utils.message(sender, "&8[&3HG&8] &7Los cofres han sido reemplazados con la LootTable personalizada.");
        return true;
    }

    private LootTable getRandomLootTable(Random random) {
        int chance = random.nextInt(100);

        if (chance < 45) {
            return Bukkit.getLootTable(new NamespacedKey("hungergames", "chests/low_tier_chest"));
        } else if (chance < 80) {
            return Bukkit.getLootTable(new NamespacedKey("hungergames", "chests/mid_tier_chest"));
        } else {
            return Bukkit.getLootTable(new NamespacedKey("hungergames", "chests/high_tier_chest"));
        }
    }
}
