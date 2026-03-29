package me.yourname.eloplugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
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

    // Tracks which inventory slot a player is currently enchanting
    private final Map<UUID, Integer> enchantingSlot = new HashMap<>();

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

    public void openVanillaPalette(Player player, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8Vanilla Palette - Page " + page);
        
        if (page == 1) {
            List<Material> gear = Arrays.asList(
                    Material.NETHERITE_SWORD, Material.NETHERITE_AXE, Material.MACE, Material.TRIDENT,
                    Material.BOW, Material.CROSSBOW, Material.NETHERITE_PICKAXE, Material.SHIELD, Material.ELYTRA,
                    Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS,
                    Material.END_CRYSTAL, Material.OBSIDIAN, Material.RESPAWN_ANCHOR, Material.GLOWSTONE, Material.TOTEM_OF_UNDYING,
                    Material.GOLDEN_APPLE, Material.ENCHANTED_GOLDEN_APPLE, Material.ENDER_PEARL, Material.EXPERIENCE_BOTTLE,
                    Material.FIREWORK_ROCKET, Material.POWERED_RAIL, Material.RAIL, Material.TNT_MINECART
            );
            
            int slot = 0;
            for (Material mat : gear) {
                inv.setItem(slot++, new ItemStack(mat));
            }
            
            ItemStack spear = new ItemStack(Material.TRIDENT);
            ItemMeta sMeta = spear.getItemMeta();
            sMeta.setDisplayName("§3Netherite Spear");
            spear.setItemMeta(sMeta);
            inv.setItem(26, spear);

            inv.setItem(53, createNamedItem(Material.ARROW, "§aNext Page ->"));
        } else {
            // Potions Page
            PotionType[] types = { PotionType.STRONG_STRENGTH, PotionType.LONG_FIRE_RESISTANCE, PotionType.LONG_SWIFTNESS, PotionType.STRONG_SWIFTNESS, PotionType.STRONG_TURTLE_MASTER };
            int slot = 0;
            for (PotionType type : types) {
                ItemStack pot = createPotion(type, true);
                inv.setItem(slot, pot);
                inv.setItem(slot + 9, createShulker(Material.PURPLE_SHULKER_BOX, pot));
                slot++;
            }
            inv.setItem(45, createNamedItem(Material.ARROW, "§c<- Previous Page"));
        }
        player.openInventory(inv);
    }

    public void openEnchantEditor(Player player, ItemStack item, int page) {
        if (item == null || item.getType() == Material.AIR) return;
        
        List<Enchantment> possible = new ArrayList<>();
        try {
            // Modern 1.21 Registry iteration
            for (Enchantment ench : org.bukkit.Registry.ENCHANTMENT) {
                if (ench.canEnchantItem(item)) possible.add(ench);
            }
        } catch (Exception e) {
            player.sendMessage("§cCould not load enchantments: " + e.getMessage());
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, "§8Enchant: " + item.getType().name() + " - Page " + page);
        int start = (page - 1) * 45;
        
        for (int i = 0; i < 45 && (start + i) < possible.size(); i++) {
            Enchantment ench = possible.get(start + i);
            ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
            meta.addStoredEnchant(ench, ench.getMaxLevel(), true);
            
            List<String> lore = new ArrayList<>();
            lore.add(item.containsEnchantment(ench) ? "§a§lACTIVE" : "§7Inactive");
            lore.add("§eLeft-Click to Add");
            lore.add("§cRight-Click to Remove");
            meta.setLore(lore);
            book.setItemMeta(meta);
            inv.setItem(i, book);
        }

        if (possible.size() > start + 45) {
            inv.setItem(53, createNamedItem(Material.LIME_STAINED_GLASS_PANE, "§aNext Page"));
        }
        if (page > 1) {
            inv.setItem(45, createNamedItem(Material.LIME_STAINED_GLASS_PANE, "§cPrevious Page"));
        }

        player.openInventory(inv);
    }

    private ItemStack createNamedItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPotion(PotionType type, boolean splash) {
        ItemStack pot = new ItemStack(splash ? Material.SPLASH_POTION : Material.POTION);
        PotionMeta meta = (PotionMeta) pot.getItemMeta();
        meta.setBasePotionType(type);
        pot.setItemMeta(meta);
        return pot;
    }

    private ItemStack createShulker(Material color, ItemStack content) {
        ItemStack shulker = new ItemStack(color);
        BlockStateMeta bsm = (BlockStateMeta) shulker.getItemMeta();
        ShulkerBox box = (ShulkerBox) bsm.getBlockState();
        for (int i = 0; i < 27; i++) box.getInventory().setItem(i, content);
        bsm.setBlockState(box);
        bsm.setDisplayName("§fShulker of " + content.getType().name());
        shulker.setItemMeta(bsm);
        return shulker;
    }

    public Map<UUID, Integer> getEnchantingSlot() {
        return enchantingSlot;
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