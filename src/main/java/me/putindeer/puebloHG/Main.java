package me.putindeer.puebloHG;

import com.google.common.util.concurrent.Service;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    public static Main pl;

    @Override
    public void onEnable() {
        pl = this;
        new Events(pl);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
