package me.putindeer.puebloHG;

import com.destroystokyo.paper.event.block.AnvilDamagedEvent;
import jdk.jfr.Event;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Events implements Listener {
    private final Main plugin;

    public Events(Main plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onAnvil(AnvilDamagedEvent e) {
        e.setCancelled(true);
    }
}
