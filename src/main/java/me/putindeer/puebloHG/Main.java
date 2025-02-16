package me.putindeer.puebloHG;

import fr.mrmicky.fastboard.adventure.FastBoard;
import lombok.Getter;
import me.putindeer.puebloHG.managers.Scatter;
import me.putindeer.puebloHG.utils.StartThings;
import me.putindeer.puebloHG.utils.Utils;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class Main extends JavaPlugin {
    @Getter
    public static Main pl;
    public Utils utils;
    public Scatter scatter;
    public static Set<String> alivePlayers = new HashSet<>();
    public Map<UUID, FastBoard> boards = new HashMap<>();

    @Override
    public void onEnable() {
        pl = this;
        utils = new Utils();
        scatter = new Scatter(this);
        new StartThings(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
