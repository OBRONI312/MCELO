package me.yourname.eloplugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.*;

public class QueueManager {

    private final Main plugin;
    // Maps Kit Name -> List of Player UUIDs waiting
    private final Map<String, List<UUID>> queues = new HashMap<>();

    // Custom Kit Matching
    private final Map<Integer, List<UUID>> customQueues = new HashMap<>();
    private final List<UUID> randomCustomQueue = new ArrayList<>();
    private final Map<UUID, DuelRequest> pendingDuels = new HashMap<>();

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
        if (queue.size() < 2)
            return;

        // Simple matchmaking: grab the first two players
        UUID p1 = queue.remove(0);
        UUID p2 = queue.remove(0);

        // Create and start the match
        plugin.getMatchManager().createMatch(plugin, p1, p2, kitName);
    }

    public void joinCustomQueue(UUID uuid, int slot) {
        if (plugin.getUserManager().getUser(uuid).getCustomKitLayout(slot) == null) {
            Bukkit.getPlayer(uuid).sendMessage("§cYou haven't created a kit in slot " + slot + " yet!");
            return;
        }

        leaveQueue(uuid);
        customQueues.putIfAbsent(slot, new ArrayList<>());
        List<UUID> q = customQueues.get(slot);

        if (!q.contains(uuid))
            q.add(uuid);
        Bukkit.getPlayer(uuid).sendMessage("§aQueued for Custom Kit #" + slot);

        if (q.size() >= 2) {
            UUID p1 = q.remove(0);
            UUID p2 = q.remove(0);
            plugin.getMatchManager().createMatch(plugin, p1, p2, "custom_" + slot);
        }
    }

    public void joinRandomCustomQueue(UUID uuid) {
        leaveQueue(uuid);
        randomCustomQueue.add(uuid);
        Bukkit.getPlayer(uuid).sendMessage("§aJoined the Random Custom Queue!");

        if (randomCustomQueue.size() >= 2) {
            UUID p1 = randomCustomQueue.remove(0);
            UUID p2 = randomCustomQueue.remove(0);
            // Pick slot 1 as default for random matches if they have it, or first available
            plugin.getMatchManager().createMatch(plugin, p1, p2, "custom_1");
        }
    }

    public void sendDuel(Player sender, Player target, int slot) {
        if (plugin.getUserManager().getUser(sender.getUniqueId()).getCustomKitLayout(slot) == null) {
            sender.sendMessage("§cYou cannot duel with an empty custom kit slot!");
            return;
        }

        pendingDuels.put(target.getUniqueId(), new DuelRequest(sender.getUniqueId(), slot));

        sender.sendMessage("§aSent a duel request to " + target.getName() + " with Custom Kit #" + slot);
        target.sendMessage("§e" + sender.getName() + " §7has challenged you to a §6Custom Kit #" + slot + " §7duel!");
        target.sendMessage("§7Type §e/custom duelaccept §7to play.");
    }

    public void acceptDuel(Player acceptor) {
        DuelRequest req = pendingDuels.remove(acceptor.getUniqueId());
        if (req == null) {
            acceptor.sendMessage("§cYou have no pending duel requests.");
            return;
        }

        Player sender = Bukkit.getPlayer(req.sender);
        if (sender == null || !sender.isOnline()) {
            acceptor.sendMessage("§cThe challenger is no longer online.");
            return;
        }

        plugin.getMatchManager().createMatch(plugin, req.sender, acceptor.getUniqueId(), "custom_" + req.slot);
    }

    public boolean leaveQueue(UUID uuid) {
        boolean wasInQueue = false;
        for (List<UUID> queue : queues.values()) {
            if (queue.remove(uuid)) {
                wasInQueue = true;
            }
        }
        for (List<UUID> q : customQueues.values()) {
            if (q.remove(uuid))
                wasInQueue = true;
        }
        if (randomCustomQueue.remove(uuid))
            wasInQueue = true;

        return wasInQueue;
    }

    private static class DuelRequest {
        UUID sender;
        int slot;

        DuelRequest(UUID sender, int slot) {
            this.sender = sender;
            this.slot = slot;
        }
    }
}