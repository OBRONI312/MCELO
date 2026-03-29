package me.yourname.eloplugin;

import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PvPUser {

    private final UUID uuid;
    private final Map<String, Integer> elos = new HashMap<>();
    private int wins;
    private int losses;
    private boolean inMatch;
    
    // Matchmaking Settings
    private boolean strictMatching;
    private int eloLimit;
    private ItemStack[] savedInventory;
    private boolean verified;

    /**
     * Supports the full 1.21.11 Kit Roster:
     * vanilla, uhc, pot, nethop, mace, smp, sword, axe, lifesteal, spear-mace
     */
    private final Map<String, ItemStack[]> kitLayouts = new HashMap<>();

    public PvPUser(UUID uuid) {
        this.uuid = uuid;
        this.wins = 0;
        this.losses = 0;
        this.inMatch = false;
        
        // Default competitive matchmaking constraints
        this.strictMatching = false;
        this.eloLimit = 200; 
        this.verified = false;
    }

    // --- Stats Getters & Setters ---

    public UUID getUuid() { return uuid; }

    public int getElo(String kitName) {
        return elos.getOrDefault(kitName.toLowerCase(), 1000);
    }

    public void setElo(String kitName, int elo) {
        elos.put(kitName.toLowerCase(), elo);
    }

    public Map<String, Integer> getElosMap() { return elos; }

    public int getWins() { return wins; }

    public void setWins(int wins) { this.wins = wins; }

    public void addWin() { this.wins++; }

    public int getLosses() { return losses; }

    public void setLosses(int losses) { this.losses = losses; }

    public void addLoss() { this.losses++; }

    public boolean isInMatch() { return inMatch; }

    public void setInMatch(boolean inMatch) { this.inMatch = inMatch; }

    public boolean isVerified() { return verified; }

    public void setVerified(boolean verified) { this.verified = verified; }

    // --- Matchmaking Settings ---

    public boolean isStrictMatching() { return strictMatching; }

    public void setStrictMatching(boolean strictMatching) { 
        this.strictMatching = strictMatching; 
    }

    public int getEloLimit() { return eloLimit; }

    public void setEloLimit(int eloLimit) { 
        this.eloLimit = eloLimit; 
    }

    // --- Inventory Management ---
    public void setSavedInventory(ItemStack[] items) {
        this.savedInventory = items;
    }

    public ItemStack[] getSavedInventory() {
        return savedInventory;
    }

    // --- Kit Layout Logic ---

    /**
     * Retrieves a player's custom layout for a specific kit.
     * Returns null if they haven't saved a custom one yet.
     */
    public ItemStack[] getKitLayout(String kitName) {
        return kitLayouts.get(kitName.toLowerCase());
    }

    public Map<String, ItemStack[]> getKitLayoutsMap() { return kitLayouts; }

    public void removeKitLayout(String kitName) {
        kitLayouts.remove(kitName.toLowerCase());
    }

    /**
     * Saves the current inventory state as a custom layout for a kit.
     * Called when the player closes the KitEditor or finishes a match.
     */
    public void saveKitLayout(String kitName, ItemStack[] items) {
        // We clone the array to ensure the data is "snapshotted" 
        // and not modified by external inventory changes.
        kitLayouts.put(kitName.toLowerCase(), items.clone());
    }
}