package me.yourname.eloplugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import java.util.UUID;

public class MatchListener implements Listener {

    private final Main plugin;

    public MatchListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // Check if they were actually in a match
        PvPUser user = plugin.getUserManager().getUser(victim.getUniqueId());
        if (!user.isInMatch()) return;

        // Find the match the player is in
        Match match = plugin.getMatchManager().getMatch(victim.getUniqueId());
        if (match != null) {
            // If there's a killer in the match, they win.
            // Otherwise (e.g. fall damage, void), the opponent wins.
            UUID winnerId = (killer != null && match.containsPlayer(killer.getUniqueId()))
                    ? killer.getUniqueId()
                    : match.getOpponent(victim.getUniqueId());

            match.end(winnerId);
            event.getDrops().clear(); // Don't drop items on the arena floor
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        
        // FIXED: Using leaveQueue instead of removeFromQueue
        plugin.getQueueManager().leaveQueue(uuid);

        // If they leave during a match, the other person should win
        Match match = plugin.getMatchManager().getMatch(uuid);
        if (match != null) {
            match.end(match.getOpponent(uuid));
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PvPUser user = plugin.getUserManager().getUser(player.getUniqueId());
        if (user.getSavedInventory() != null) {
            plugin.getUserManager().giveLobbyItems(player);
            user.setSavedInventory(null);
        }
    }
}