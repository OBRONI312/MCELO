package me.yourname.eloplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.UUID;

public class Match {

    private final Main plugin;
    private final UUID player1;
    private final UUID player2;
    private final String kitName;
    private ArenaManager.ArenaInstance arenaInstance;
    private boolean ending = false;

    public Match(Main plugin, UUID player1, UUID player2, String kitName) {
        this.plugin = plugin;
        this.player1 = player1;
        this.player2 = player2;
        this.kitName = kitName;
    }

    public boolean start() {
        Player p1 = Bukkit.getPlayer(player1);
        Player p2 = Bukkit.getPlayer(player2);

        if (p1 == null || p2 == null)
            return false;

        // FIXED: Using getUserManager() instead of undefined getUser()
        PvPUser u1 = plugin.getUserManager().getUser(player1);
        PvPUser u2 = plugin.getUserManager().getUser(player2);

        u1.setInMatch(true);
        u2.setInMatch(true);

        u1.setSavedInventory(p1.getInventory().getContents());
        u2.setSavedInventory(p2.getInventory().getContents());

        preparePlayer(p1, u1);
        preparePlayer(p2, u2);

        // --- Arena Generation & Teleportation ---
        String arenaStyle = kitName;
        if (kitName.startsWith("custom_")) {
            try {
                int slot = Integer.parseInt(kitName.split("_")[1]);
                arenaStyle = u1.getCustomKitMap(slot);
            } catch (Exception ignored) {
            }
        }
        this.arenaInstance = plugin.getArenaManager().pasteArena(arenaStyle);

        if (this.arenaInstance != null && this.arenaInstance.getSpawnPoints().size() >= 2) {
            // Index 0: Red Concrete (P1), Index 1: Blue Concrete (P2)
            Location loc1 = this.arenaInstance.getSpawnPoints().get(0);
            Location loc2 = this.arenaInstance.getSpawnPoints().get(1);

            // Make them face each other
            if (loc1.distanceSquared(loc2) > 0.5) {
                loc1.setDirection(loc2.toVector().subtract(loc1.toVector()));
                loc2.setDirection(loc1.toVector().subtract(loc2.toVector()));
            }

            p1.teleport(loc1);
            p2.teleport(loc2);
        } else if (this.arenaInstance != null) {
            // Fallback if no Netherite blocks found: center offset
            // We just use the first spawn point or world coords if empty, calculated
            // manually here as fallback
            p1.sendMessage("§cWarning: Spawn points not configured in schematic (use Netherite Blocks).");
            // (Simple fallback omitted to keep code clean, usually indicates schematic
            // error)
            u1.setInMatch(false);
            u2.setInMatch(false);
            return false;
        } else {
            p1.sendMessage("§cError: Arena schematic not found. Match cancelled.");
            p2.sendMessage("§cError: Arena schematic not found. Match cancelled.");
            u1.setInMatch(false);
            u2.setInMatch(false);
            return false;
        }

        p1.sendMessage("§aMatch started against " + p2.getName() + " §7(Kit: " + kitName + ")");
        p2.sendMessage("§aMatch started against " + p1.getName() + " §7(Kit: " + kitName + ")");

        return true;
    }

    private void preparePlayer(Player player, PvPUser user) {
        player.getInventory().clear();
        // Clear active effects and fire to ensure a fair start
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.setFireTicks(0);
        player.setSaturation(20.0f);

        ItemStack[] finalLayout;
        ItemStack[] defaultLayout;
        ItemStack[] sourceLayout;

        if (kitName.startsWith("custom_")) {
            int slot = Integer.parseInt(kitName.split("_")[1]);
            sourceLayout = user.getCustomKitLayout(slot);
            String mapType = user.getCustomKitMap(slot);
            defaultLayout = plugin.getKitEditorManager().getServerDefault(mapType);
        } else {
            sourceLayout = user.getKitLayout(kitName);
            defaultLayout = plugin.getKitEditorManager().getServerDefault(kitName);
        }
        if (sourceLayout == null) {
            finalLayout = defaultLayout;
        } else {
            // Copy the player's layout into a standard 41-slot array (36 main + 4 armor + 1
            // offhand)
            finalLayout = new ItemStack[41];
            System.arraycopy(sourceLayout, 0, finalLayout, 0, Math.min(sourceLayout.length, 41));

            // PATCH: If player's saved kit is missing armor (null), use default armor
            for (int i = 36; i <= 40; i++) {
                if (i < defaultLayout.length && finalLayout[i] == null && defaultLayout[i] != null) {
                    finalLayout[i] = defaultLayout[i];
                }
            }
        }

        // The layout array maps to inventory slots 0-40
        // 0-35: Main inventory
        // 36-39: Armor (boots, leggings, chestplate, helmet)
        // 40: Off-hand
        for (int i = 0; i < finalLayout.length && i <= 40; i++) {
            if (finalLayout[i] != null) {
                player.getInventory().setItem(i, finalLayout[i]);
            }
        }

        AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            player.setHealth(maxHealth.getValue());
        }
        player.setFoodLevel(20);
    }

    public void end(UUID winnerUuid) {
        if (ending)
            return;
        ending = true;

        PvPUser winner = plugin.getUserManager().getUser(winnerUuid);
        UUID loserUuid = (winnerUuid.equals(player1)) ? player2 : player1;
        PvPUser loser = plugin.getUserManager().getUser(loserUuid);

        winner.setInMatch(false);
        loser.setInMatch(false);

        winner.addWin();
        loser.addLoss();
        
        winner.incrementKitMatches(kitName);
        loser.incrementKitMatches(kitName);

        // Calculate Elo change using the EloCalculator
        int oldWinnerElo = winner.getElo(kitName);
        int oldLoserElo = loser.getElo(kitName);
        int newWinnerElo = EloCalculator.calculateNewElo(oldWinnerElo, oldLoserElo, true);
        int newLoserElo = EloCalculator.calculateNewElo(oldLoserElo, oldWinnerElo, false);
        winner.setElo(kitName, newWinnerElo);
        loser.setElo(kitName, newLoserElo);

        // Save stats to Database immediately so website updates
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getDatabaseManager().saveUser(winner);
            plugin.getDatabaseManager().saveUser(loser);
        });

        Player winnerPlayer = Bukkit.getPlayer(winnerUuid);
        if (winnerPlayer != null && winnerPlayer.isOnline()) {
            winnerPlayer.sendMessage("§6§lVICTORY! §e" + newWinnerElo + " §a(+" + (newWinnerElo - oldWinnerElo) + ")");
        }

        Player loserPlayer = Bukkit.getPlayer(loserUuid);
        if (loserPlayer != null && loserPlayer.isOnline()) {
            loserPlayer.sendMessage("§c§lDEFEAT! §e" + newLoserElo + " §c(" + (newLoserElo - oldLoserElo) + ")");
        }

        // Wait 3 seconds before teleporting and cleaning up
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Location spawn = Bukkit.getWorlds().get(0).getSpawnLocation();

            Player wp = Bukkit.getPlayer(winnerUuid);
            if (wp != null && !wp.isDead()) {
                restoreInventory(wp, winner);
                wp.teleport(spawn);
            }

            Player lp = Bukkit.getPlayer(loserUuid);
            if (lp != null && !lp.isDead()) {
                restoreInventory(lp, loser);
                lp.teleport(spawn);
            }

            if (this.arenaInstance != null) {
                this.arenaInstance.remove();
            }
            plugin.getMatchManager().removeMatch(this);
        }, 60L); // 60 ticks = 3 seconds
    }

    private void restoreInventory(Player player, PvPUser user) {
        plugin.getUserManager().giveLobbyItems(player);
        user.setSavedInventory(null);
    }

    public UUID getOpponent(UUID uuid) {
        if (uuid.equals(player1)) {
            return player2;
        } else {
            return player1;
        }
    }

    public boolean containsPlayer(UUID uuid) {
        return player1.equals(uuid) || player2.equals(uuid);
    }
}