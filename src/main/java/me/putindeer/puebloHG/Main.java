package me.putindeer.puebloHG;

import fr.mrmicky.fastboard.adventure.FastBoard;
import lombok.Getter;
import me.putindeer.puebloHG.game.GameManager;
import me.putindeer.puebloHG.game.PointsManager;
import me.putindeer.puebloHG.game.Scatter;
import me.putindeer.puebloHG.game.VerticalBorder;
import me.putindeer.puebloHG.utils.StartThings;
import org.bukkit.plugin.java.JavaPlugin;
import us.polarismc.api.util.PluginUtils;

import java.util.*;

public final class Main extends JavaPlugin {
    @Getter
    public static Main pl;
    public int totalPlayers = -1;
    public PluginUtils utils;
    public GameManager gameManager;
    public PointsManager pointsManager;
    public Scatter scatter;
    public VerticalBorder verticalBorder;
    public final Set<UUID> alivePlayers = new HashSet<>();
    public final List<UUID> deadPlayers = new ArrayList<>();
    public final Map<UUID, FastBoard> boards = new HashMap<>();
    public String timer = "";

    @Override
    public void onEnable() {
        pl = this;
        utils = new PluginUtils(this, "&8[&3HG&8] &f");
        new StartThings(this);
    }

    @Override
    public void onDisable() {
        pointsManager.save();
    }
}
