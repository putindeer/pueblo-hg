package me.putindeer.puebloHG.utils;

import me.putindeer.puebloHG.Main;
import me.putindeer.puebloHG.commands.*;
import me.putindeer.puebloHG.config.game.GameEventLoader;
import me.putindeer.puebloHG.config.locations.LocationManager;
import me.putindeer.puebloHG.game.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.scoreboard.*;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Objects;

public class StartThings {
    private final Main plugin;

    public StartThings(Main plugin) {
        this.plugin = plugin;
        enable();
    }

    public void enable() {
        createDataFolder();

        registerCommands();
        registerGameHandlers();
        registerTabAndScoreboard();
        registerHealthIndicators();
        registerRecipes();
    }

    public void createDataFolder() {
        if (!plugin.getDataFolder().exists()) {
            if (plugin.getDataFolder().mkdir()) {
                plugin.getLogger().info("Directorio del plugin creado con éxito.");
            } else {
                plugin.getLogger().warning("No se pudo crear el directorio del plugin.");
            }
        }
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
    }

    public void registerCommands() {
        new AddLocation(plugin);
        new ForceEnd(plugin);
        new Leaderboard(plugin);
        new RegisterChests(plugin);
        new Restock(plugin);
        new Start(plugin);
    }

    public void registerGameHandlers() {
        new GameEvents(plugin);
        plugin.events = new GameEventLoader(plugin).loadEventsFromConfig();
        plugin.gameManager = new GameManager(plugin);
        plugin.pointsManager = new PointsManager(plugin);
        plugin.scatter = new Scatter(plugin);
        plugin.locationManager = new LocationManager(plugin);
        plugin.verticalBorder = new VerticalBorder(plugin);
        new Placeholders().register();
    }

    public void registerTabAndScoreboard() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> plugin.boards.values().forEach(Scoreboards::updateBoard), 0, 20);
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> Bukkit.getOnlinePlayers().forEach(p -> p.sendPlayerListHeaderAndFooter(
                plugin.utils.chat("&3&lVenezuela Games"),
                plugin.utils.chat("&7Ping: &3" + p.getPing() + " &8| &7Tps: &3" + new DecimalFormat("##").format(plugin.getServer().getTPS()[0]))
        )),0, 100);
    }

    public void registerHealthIndicators() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        if (scoreboard.getObjective("PointsTabPL") == null) {
            scoreboard.registerNewObjective("PointsTabPL", Criteria.DUMMY, plugin.utils.chat("&e")).setDisplaySlot(DisplaySlot.PLAYER_LIST);
        }

        if (scoreboard.getObjective("HealthNamePL") == null) {
            scoreboard.registerNewObjective("HealthNamePL", Criteria.DUMMY, plugin.utils.chat("&c❤")).setDisplaySlot(DisplaySlot.BELOW_NAME);
        }

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                Objective objective = scoreboard.getObjective("PointsTabPL");
                Objective objective2 = scoreboard.getObjective("HealthNamePL");
                Score score1 = Objects.requireNonNull(objective).getScore(player.getName());
                Score score2 = Objects.requireNonNull(objective2).getScore(player.getName());
                double totalhealth = player.getHealth() + player.getAbsorptionAmount();
                score1.setScore(plugin.pointsManager.getPoints(player.getUniqueId()));
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
        ItemStack axe = new ItemStack(Material.DIAMOND_AXE);
        ShapedRecipe diamondAxe1 = new ShapedRecipe(NamespacedKey.minecraft("diamond_stick_axe_left"), axe);
        diamondAxe1.shape("DD ", "DS ", " S ");
        diamondAxe1.setIngredient('D', Material.DIAMOND);
        diamondAxe1.setIngredient('S', Material.WOODEN_SWORD);
        ShapedRecipe diamondAxe2 = new ShapedRecipe(NamespacedKey.minecraft("diamond_stick_axe_right"), axe);
        diamondAxe2.shape(" DD", " SD", " S ");
        diamondAxe2.setIngredient('D', Material.DIAMOND);
        diamondAxe2.setIngredient('S', Material.WOODEN_SWORD);
        Bukkit.addRecipe(diamondSword1);
        Bukkit.addRecipe(diamondSword2);
        Bukkit.addRecipe(diamondSword3);
        Bukkit.addRecipe(diamondAxe1);
        Bukkit.addRecipe(diamondAxe2);
    }
}
