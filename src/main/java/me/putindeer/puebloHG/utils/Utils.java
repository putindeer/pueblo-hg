package me.putindeer.puebloHG.utils;

import lombok.NoArgsConstructor;
import me.putindeer.puebloHG.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Objects;

@NoArgsConstructor
@SuppressWarnings("unused")
public class Utils {
    /**
     * Prefix del plugin
     */
    public final Component prefix = chat("&8[&3HG&8] &f");

    /**
     * Convierte un texto con códigos HEX a un 'Component'
     * @param s La 'String' que recibe
     * @return El texto convertido
     */
    public Component chat(String s){
        return MiniMessage.miniMessage().deserialize(convert(s));
    }
    public Component chat(Component s){
        return MiniMessage.miniMessage().deserialize(convert(PlainTextComponentSerializer.plainText().serialize(s)));
    }

    /**
     * Convierte códigos HEX a tags de MiniMessage
     * @param s La 'String' con códigos HEX
     * @return La 'String' con tags de MiniMessage
     */
    public String convert(String s) {
        s = s.replaceAll("&#([A-Fa-f0-9]{6})", "<#$1>");
        return s.replace("&0", "<black>").replace("&1", "<dark_blue>").replace("&2", "<dark_green>").replace("&3", "<dark_aqua>").replace("&4", "<dark_red>").replace("&5", "<dark_purple>").replace("&6", "<gold>").replace("&7", "<gray>").replace("&8", "<dark_gray>").replace("&9", "<blue>").replace("&a", "<green>").replace("&b", "<aqua>").replace("&c", "<red>").replace("&d", "<light_purple>").replace("&e", "<yellow>").replace("&f", "<white>").replace("&n", "<underlined>").replace("&m", "<strikethrough>").replace("&k", "<obfuscated>").replace("&o", "<italic>").replace("&l", "<bold>").replace("&r", "<reset>");
    }

    /**
     * Envia un mensaje a todos los jugadores del servidor
     * @param c El texto, como 'String' o 'Component'
     */
    public void broadcast(String c) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(prefix.append(chat(c)));
        }
        Bukkit.getConsoleSender().sendMessage(prefix.append(chat(c)));
    }
    public void broadcast(Component c) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(prefix.append(chat(c)));
        }
        Bukkit.getConsoleSender().sendMessage(prefix.append(chat(c)));
    }

    /**
     * Envia un mensaje y un sonido a todos los jugadores del servidor
     * @param c El texto que quieres mostrar, como 'String' o 'Component'
     * @param s El sonido
     */
    public void broadcast(String c, Sound s) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(prefix.append(chat(c)));
            p.playSound(p.getLocation(), s, 10, 1);
        }
        Bukkit.getConsoleSender().sendMessage(prefix.append(chat(c)));
    }
    public void broadcast(Component c, Sound s) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(prefix.append(chat(c)));
            p.playSound(p.getLocation(), s, 10, 1);
        }
        Bukkit.getConsoleSender().sendMessage(prefix.append(chat(c)));
    }

    /**
     * Envia un mensaje a todos los jugadores del servidor sin prefix
     * @param c El texto, como 'String'
     */
    public void broadcastNoPrefix(String c) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(chat(c));
        }
        Bukkit.getConsoleSender().sendMessage(chat(c));
    }

    /**
     * Envia un mensaje a uno o más jugadores
     * @param ps Los jugadores que recibirán los textos
     * @param c Los textos, como 'String' o 'Component'
     */
    public void message(Collection<Player> ps, String... c) {
        for (Player p : ps) {
            for (String part : c) {
                p.sendMessage(prefix.append(chat(part)));
            }
        }
    }

    public void message(Collection<Player> ps, Component... c) {
        for (Player p : ps) {
            for (Component part : c) {
                p.sendMessage(prefix.append(chat(part)));
            }
        }
    }

    public void message(CommandSender p, String... c) {
        for (String part : c) {
            p.sendMessage(prefix.append(chat(part)));
        }
    }

    public void message(CommandSender p, Component... c) {
        for (Component part : c) {
            p.sendMessage(prefix.append(chat(part)));
        }
    }

    /**
     * Envia un mensaje y un sonido a uno o más jugadores
     * @param ps Los jugadores que recibirán los textos
     * @param s El sonido
     * @param c Los textos, como 'String' o 'Component'
     */
    public void message(Collection<Player> ps, Sound s, String... c) {
        for (Player p : ps) {
            for (String part : c) {
                p.sendMessage(prefix.append(chat(part)));
            }
            p.playSound(p.getLocation(), s, 10, 1);
        }
    }

    public void message(Collection<Player> ps, Sound s, Component... c) {
        for (Player p : ps) {
            for (Component part : c) {
                p.sendMessage(prefix.append(chat(part)));
            }
            p.playSound(p.getLocation(), s, 10, 1);
        }
    }

    public void message(CommandSender cs, Sound s, String... c) {
        Player p = (Player) cs;
        for (String part : c) {
            p.sendMessage(prefix.append(chat(part)));
        }
        p.playSound(p.getLocation(), s, 10, 1);
    }

    public void message(CommandSender cs, Sound s, Component... c) {
        Player p = (Player) cs;
        for (Component part : c) {
            p.sendMessage(prefix.append(chat(part)));
        }
        p.playSound(p.getLocation(), s, 10, 1);
    }

    /**
     * Restablece la vida del jugador al máximo
     * @param p El jugador
     */
    public void setMaxHealth(Player p) {
        p.setHealth(Objects.requireNonNull(p.getAttribute(Attribute.MAX_HEALTH)).getDefaultValue());
    }

    /**
     * Restaura completamente a un jugador
     * @param p El jugador a restaurar
     */
    public void restorePlayer(Player p) {
        p.getInventory().clear();
        setMaxHealth(p);
        p.setFoodLevel(20);
        p.setSaturation(5.0f);
        p.getActivePotionEffects().forEach(potionEffect -> p.removePotionEffect(potionEffect.getType()));
        p.setLevel(0);
        p.setExp(0.0f);
        p.setFireTicks(0);
        p.setItemOnCursor(new ItemStack(Material.AIR));
        p.setInvulnerable(false);
        p.setGameMode(GameMode.SURVIVAL);
        p.setStatistic(Statistic.PLAYER_KILLS, 0);
    }

    /**
     * Ejecuta una tarea ({@link Runnable}) después de un tiempo especificado.
     * @param delay El tiempo de espera en ticks antes de ejecutar.
     * @param run La tarea a ejecutar, implementada como un {@code Runnable}.
     */
    public void delay(int delay, Runnable run) {
        Bukkit.getScheduler().runTaskLater(Main.getPl(), run,delay);
    }

    public void delay(Runnable run) {
        delay(1, run);
    }
}
