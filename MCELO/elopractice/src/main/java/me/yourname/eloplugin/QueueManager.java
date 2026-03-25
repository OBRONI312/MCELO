package me.yourname.eloplugin;

import org.bukkit.Bukkit;
import java.util.*;

public class QueueManager {

    private final Main plugin;
    // Maps Kit Name -> List of Player UUIDs waiting
    private final Map<String, List<UUID>> queues = new HashMap<>();

    public QueueManager(Main plugin) {
        this.plugin = plugin;
    }

    public void joinQueue(UUID uuid, String kit) {
        String kitName = kit.toLowerCase();
        
        // Check if player is already in a match
        PvPUser user = plugin.getUserManager().getUser(uuid); // FIXED
        if (user.isInMatch()) {
            Bukkit.getPlayer(uuid).sendMessage("§cYou are already in a match!");
            return;
        }

        queues.putIfAbsent(kitName, new ArrayList<>());
        List<UUID> queue = queues.get(kitName);

        if (queue.contains(uuid)) {
            Bukkit.getPlayer(uuid).sendMessage("§cYou are already in the " + kitName + " queue.");
            return;
        }

        // If we are here, they are not in the target queue.
        // Remove them from any other queue they might be in.
        leaveQueue(uuid);

        queue.add(uuid);
        Bukkit.getPlayer(uuid).sendMessage("§aJoined the " + kitName + " queue!");

        tryMatch(kitName);
    }

    private void tryMatch(String kitName) {
        List<UUID> queue = queues.get(kitName);
        if (queue.size() < 2) return;

        // Simple matchmaking: grab the first two players
        UUID p1 = queue.remove(0);
        UUID p2 = queue.remove(0);

        // Create and start the match
        plugin.getMatchManager().createMatch(plugin, p1, p2, kitName);
    }

    public boolean leaveQueue(UUID uuid) {
        boolean wasInQueue = false;
        for (List<UUID> queue : queues.values()) {
            if (queue.remove(uuid)) {
                wasInQueue = true;
            }
        }
        return wasInQueue;
    }
}