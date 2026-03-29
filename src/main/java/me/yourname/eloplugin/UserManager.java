package me.yourname.eloplugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserManager implements Listener {

    private final Main plugin;
    private final Map<UUID, PvPUser> users = new HashMap<>();

    public UserManager(Main plugin) {
        this.plugin = plugin;
        // Register this class as a listener so Join/Quit events work
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Gets the PvPUser object for a player. 
     * If they aren't loaded, it creates a new profile for them.
     */
    public PvPUser getUser(UUID uuid) {
        return users.computeIfAbsent(uuid, PvPUser::new);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Ensure user is loaded into memory when they join
        PvPUser user = getUser(player.getUniqueId());

        // Load data from SQL async
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getDatabaseManager().loadUser(user);
        });

        // Restore inventory if they quit during a match and are rejoining
        if (user.getSavedInventory() != null) {
            player.getInventory().setContents(user.getSavedInventory());
            user.setSavedInventory(null);
            // Let them know their items are back
            player.sendMessage("§aYour inventory from before your last match has been restored.");
        } else {
            giveLobbyItems(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        PvPUser user = users.get(uuid);
        if (user != null) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                plugin.getDatabaseManager().saveUser(user);
            });
        }

        // If the player count drops to 1 or 0 (remaining), reset the arena distance counter
        if (Bukkit.getOnlinePlayers().size() <= 2) {
            plugin.getArenaManager().resetArenaCounter();
        }
    }

    public void giveLobbyItems(Player player) {
        player.getInventory().clear();

        ItemStack queueItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta qMeta = queueItem.getItemMeta();
        if (qMeta != null) {
            qMeta.setDisplayName("§bRanked Queue §7(Right Click)");
            queueItem.setItemMeta(qMeta);
        }

        ItemStack editorItem = new ItemStack(Material.BOOK);
        ItemMeta eMeta = editorItem.getItemMeta();
        if (eMeta != null) {
            eMeta.setDisplayName("§eKit Editor §7(Right Click)");
            editorItem.setItemMeta(eMeta);
        }

        player.getInventory().setItem(0, queueItem);
        player.getInventory().setItem(4, editorItem);
    }

    /**
     * Returns the full map of online users.
     */
    public Map<UUID, PvPUser> getUsers() {
        return users;
    }
}