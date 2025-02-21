package me.putindeer.puebloHG;

import fr.mrmicky.fastboard.adventure.FastBoard;
import lombok.Getter;
import me.putindeer.puebloHG.game.GameManager;
import me.putindeer.puebloHG.game.PointsManager;
import me.putindeer.puebloHG.game.Scatter;
import me.putindeer.puebloHG.utils.StartThings;
import me.putindeer.puebloHG.utils.Utils;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class Main extends JavaPlugin {
    @Getter
    public static Main pl;
    public int totalPlayers = -1;
    public Utils utils;
    public GameManager gameManager;
    public PointsManager pointsManager;
    public Scatter scatter;
    public Set<UUID> alivePlayers = new HashSet<>();
    public List<UUID> deadPlayers = new ArrayList<>();
    public Map<UUID, FastBoard> boards = new HashMap<>();
    public String timer = "";

    @Override
    public void onEnable() {
        pl = this;
        utils = new Utils();
        new StartThings(this);
    }

    @Override
    public void onDisable() {
        pointsManager.save();
    }
}
