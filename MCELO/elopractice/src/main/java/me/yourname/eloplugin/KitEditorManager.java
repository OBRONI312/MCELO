package me.yourname.eloplugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class KitEditorManager {

    private final Main plugin;
    // Tracks which kit a player is currently editing
    private final Map<UUID, String> editingKit = new HashMap<>();
    private final Map<UUID, Boolean> editMode = new HashMap<>(); // true = server, false = personal

    private final List<String> validKits = Arrays.asList(
        "vanilla", "uhc", "pot", "nethop", "mace", "smp", "sword", "axe", "lifesteal", "spear-mace", "cartpvp"
    );

    public KitEditorManager(Main plugin) {
        this.plugin = plugin;
    }

    /**
     * Opens the editing GUI for a specific kit.
     */
    public void openEditor(Player player, String kitName, boolean isServerEdit) {
        // Leave any queue the player is in.
        if (plugin.getQueueManager().leaveQueue(player.getUniqueId())) {
            player.sendMessage("§cYou have been removed from the queue because you started editing a kit.");
        }

        ItemStack[] layout;

        if (isServerEdit) {
            // Load server default (or hardcoded if not set)
            layout = getServerDefault(kitName);
        } else {
            // Load player layout -> Server Default -> Hardcoded Default
            PvPUser user = plugin.getUserManager().getUser(player.getUniqueId());
            layout = user.getKitLayout(kitName);
            
            if (layout == null) {
                layout = getServerDefault(kitName);
            }
        }
        
        // Fallback handled in getServerDefault or above logic ensures layout isn't null usually, 
        // but getServerDefault calls DefaultKitProvider which returns items or empty array.

        // Clear player inventory and load the kit
        player.getInventory().clear();

        // Map the kit layout to player inventory slots (0-40)
        // 0-35: Storage, 36-39: Armor, 40: Offhand
        for (int i = 0; i < layout.length && i <= 40; i++) {
            if (layout[i] != null) {
                player.getInventory().setItem(i, layout[i]);
            }
        }

        editingKit.put(player.getUniqueId(), kitName.toLowerCase());
        editMode.put(player.getUniqueId(), isServerEdit);
        
        player.sendMessage("§aEditing kit §e" + kitName + "§a.");
        player.sendMessage("§7Organize your inventory as you wish.");
        player.sendMessage("§7When finished, type §e/ranked savekit §7to save.");
    }

    public void saveKit(Player player) {
        String kitName = editingKit.get(player.getUniqueId());
        if (kitName == null) return;
        
        boolean isServer = isEditingServer(player.getUniqueId());
        ItemStack[] layout = extractLayout(player.getInventory());
        
        if (isServer) {
            player.sendMessage("§cServer kit editing is disabled.");
        } else {
            plugin.getUserManager().getUser(player.getUniqueId()).saveKitLayout(kitName, layout);
            player.sendMessage("§a§l✔ §7Your §e" + kitName + " §7layout has been saved!");
        }
        
        stopEditing(player.getUniqueId());
        plugin.getUserManager().giveLobbyItems(player);
    }

    private ItemStack[] extractLayout(Inventory inv) {
        // Capture slots 0-40 (Main Inv, Armor, Offhand)
        // Using getContents() ensures we get the proper array from PlayerInventory
        ItemStack[] contents = inv.getContents();
        ItemStack[] layout = new ItemStack[41];
        
        // Copy available contents up to 41 slots (safe for PlayerInventory)
        for (int i = 0; i < layout.length && i < contents.length; i++) {
            layout[i] = contents[i];
        }
        return layout;
    }

    public String getEditingKit(UUID uuid) {
        return editingKit.get(uuid);
    }

    public boolean isEditingServer(UUID uuid) {
        return editMode.getOrDefault(uuid, false);
    }

    public ItemStack[] getServerDefault(String kitName) {
        return DefaultKitProvider.getDefaultKit(kitName);
    }

    public List<String> getValidKits() {
        return validKits;
    }

    public void stopEditing(UUID uuid) {
        editingKit.remove(uuid);
        editMode.remove(uuid);
    }
}