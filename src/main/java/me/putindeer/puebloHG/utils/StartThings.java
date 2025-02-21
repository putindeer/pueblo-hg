package me.putindeer.puebloHG.utils;

import me.putindeer.puebloHG.Main;
import me.putindeer.puebloHG.commands.Restock;
import me.putindeer.puebloHG.commands.Start;
import me.putindeer.puebloHG.game.HGEvents;
import me.putindeer.puebloHG.game.Scoreboards;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.scoreboard.*;

import java.text.DecimalFormat;
import java.util.Objects;

public class StartThings {
    private final Main plugin;

    public StartThings(Main plugin) {
        this.plugin = plugin;
        enable();
    }

    public void enable() {
        registerCommands();
        registerListeners();
        if (!plugin.getDataFolder().exists()) {
            if (plugin.getDataFolder().mkdir()) {
                plugin.getLogger().info("Directorio del plugin creado con éxito.");
            } else {
                plugin.getLogger().warning("No se pudo crear el directorio del plugin.");
            }
        }

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> plugin.boards.values().forEach(Scoreboards::updateBoard), 0, 20);
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> Bukkit.getOnlinePlayers().forEach(p -> p.sendPlayerListHeaderAndFooter(
                plugin.utils.chat("&3&lVenezuela Games"),
                plugin.utils.chat("&7Ping: &3" + p.getPing() + " &8| &7Tps: &3" + new DecimalFormat("##").format(plugin.getServer().getTPS()[0]))
        )),0, 100);

        registerScoreboard();
        registerRecipes();
    }

    public void registerCommands() {
        new Restock(plugin);
        new Start(plugin);
    }

    public void registerListeners() {
        new HGEvents(plugin);
    }

    public void registerScoreboard() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        if (scoreboard.getObjective("HealthTabPL") == null) {
            scoreboard.registerNewObjective("HealthTabPL", Criteria.DUMMY, plugin.utils.chat("&e")).setDisplaySlot(DisplaySlot.PLAYER_LIST);
        }

        if (scoreboard.getObjective("HealthNamePL") == null) {
            scoreboard.registerNewObjective("HealthNamePL", Criteria.DUMMY, plugin.utils.chat("&c❤")).setDisplaySlot(DisplaySlot.BELOW_NAME);
        }

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                Objective objective = scoreboard.getObjective("HealthTabPL");
                Objective objective2 = scoreboard.getObjective("HealthNamePL");
                Score score1 = Objects.requireNonNull(objective).getScore(player.getName());
                Score score2 = Objects.requireNonNull(objective2).getScore(player.getName());
                double totalhealth = player.getHealth() + player.getAbsorptionAmount();
                score1.setScore((int) Math.floor((totalhealth / 20) * 100));
                score2.setScore((int) Math.floor((totalhealth / 20) * 100));
            }
        },0,5);
    }

    public void registerRecipes() {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        ShapedRecipe diamondSword1 = new ShapedRecipe(NamespacedKey.minecraft("diamond_stick_left"), sword);
        diamondSword1.shape("D  ", "D  ", "S  ");
        diamondSword1.setIngredient('D', Material.DIAMOND);
        diamondSword1.setIngredient('S', Material.WOODEN_SWORD);
        ShapedRecipe diamondSword2 = new ShapedRecipe(NamespacedKey.minecraft("diamond_stick_center"), sword);
        diamondSword2.shape(" D ", " D ", " S ");
        diamondSword2.setIngredient('D', Material.DIAMOND);
        diamondSword2.setIngredient('S', Material.WOODEN_SWORD);
        ShapedRecipe diamondSword3 = new ShapedRecipe(NamespacedKey.minecraft("diamond_stick_right"), sword);
        diamondSword3.shape("  D", "  D", "  S");
        diamondSword3.setIngredient('D', Material.DIAMOND);
        diamondSword3.setIngredient('S', Material.WOODEN_SWORD);
        Bukkit.addRecipe(diamondSword1);
        Bukkit.addRecipe(diamondSword2);
        Bukkit.addRecipe(diamondSword3);
    }
}
