package me.yourname.eloplugin;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
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
    // Tracks the custom slot index being configured
    private final Map<UUID, Integer> editingCustomSlot = new HashMap<>();

    // Tracks which inventory slot a player is currently enchanting
    private final Map<UUID, Integer> enchantingSlot = new HashMap<>();

    private final List<String> validKits = Arrays.asList(
            "vanilla", "uhc", "pot", "nethop", "mace", "smp", "sword", "axe", "lifesteal", "spear-mace", "cartpvp");

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

        // Fallback handled in getServerDefault or above logic ensures layout isn't null
        // usually,
        // but getServerDefault calls DefaultKitProvider which returns items or empty
        // array.

        // Clear player inventory and load the kit
        player.getInventory().clear();

        if (kitName.startsWith("custom_")) {
            player.sendMessage("§aEditing Custom Kit Slot §e" + editingCustomSlot.get(player.getUniqueId()) + "§a.");
        } else {
            player.sendMessage("§aEditing kit §e" + kitName + "§a.");
        }
        // 0-35: Storage, 36-39: Armor, 40: Offhand
        for (int i = 0; i < layout.length && i <= 40; i++) {
            if (layout[i] != null) {
                player.getInventory().setItem(i, layout[i]);
            }
        }

        editingKit.put(player.getUniqueId(), kitName.toLowerCase());
        editMode.put(player.getUniqueId(), isServerEdit);

        player.sendMessage("§7Organize your inventory as you wish.");
        if (kitName.startsWith("custom_") || kitName.equals("vanilla")) {
            player.sendMessage("§7Type §e/ranked palette §7to get items.");
        }
        player.sendMessage("§7When finished, type §e/ranked savekit §7to save.");
    }

    public void openCustomKitSelector(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "§8Select Custom Kit Slot");
        PvPUser user = plugin.getUserManager().getUser(player.getUniqueId());

        for (int i = 1; i <= 9; i++) {
            boolean exists = user.getCustomKitLayout(i) != null;
            ItemStack book = new ItemStack(Material.BOOK);
            ItemMeta meta = book.getItemMeta();
            meta.setDisplayName("§eCustom Kit #" + i);
            if (exists) {
                meta.setLore(
                        Arrays.asList("§aKit Defined", "§7Arena: §f" + user.getCustomKitMap(i), "§eClick to Edit"));
                book.addUnsafeEnchantment(Enchantment.LUCK_OF_THE_SEA, 1);
            } else {
                meta.setLore(Arrays.asList("§cEmpty Slot", "§eClick to Create"));
            }
            book.setItemMeta(meta);
            inv.setItem(i - 1, book);
        }
        player.openInventory(inv);
    }

    public void openMapSelector(Player player, int slot) {
        Inventory inv = Bukkit.createInventory(null, 18, "§8Select Arena Type for Slot #" + slot);
        editingCustomSlot.put(player.getUniqueId(), slot);

        int i = 0;
        for (String kit : validKits) {
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§b" + kit.toUpperCase() + " Arena");
            item.setItemMeta(meta);
            inv.setItem(i++, item);
        }
        player.openInventory(inv);
    }

    public void saveKit(Player player) {
        String kitName = editingKit.get(player.getUniqueId());
        if (kitName == null)
            return;

        boolean isServer = isEditingServer(player.getUniqueId());
        ItemStack[] layout = extractLayout(player.getInventory());

        if (isServer) {
            player.sendMessage("§cServer kit editing is disabled.");
        } else if (kitName.startsWith("custom_")) {
            int slot = editingCustomSlot.getOrDefault(player.getUniqueId(), 1);
            plugin.getUserManager().getUser(player.getUniqueId()).saveCustomKitLayout(slot, layout);
            player.sendMessage("§a§l✔ §7Custom Kit §e#" + slot + " §7has been saved!");
        } else {
            plugin.getUserManager().getUser(player.getUniqueId()).saveKitLayout(kitName, layout);
            player.sendMessage("§a§l✔ §7Your §e" + kitName + " §7layout has been saved!");
        }

        stopEditing(player.getUniqueId());
        editingCustomSlot.remove(player.getUniqueId());
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
                    Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS,
                    Material.NETHERITE_BOOTS,
                    Material.END_CRYSTAL, Material.OBSIDIAN, Material.RESPAWN_ANCHOR, Material.GLOWSTONE,
                    Material.TOTEM_OF_UNDYING,
                    Material.GOLDEN_APPLE, Material.ENCHANTED_GOLDEN_APPLE, Material.ENDER_PEARL,
                    Material.EXPERIENCE_BOTTLE,
                    Material.FIREWORK_ROCKET, Material.POWERED_RAIL, Material.RAIL, Material.TNT_MINECART);

            int slot = 0;
            for (Material mat : gear) {
                inv.setItem(slot++, new ItemStack(mat));
            }

            ItemStack spear = new ItemStack(Material.TRIDENT); // This is just a placeholder, actual spear is in DefaultKitProvider
            ItemMeta sMeta = spear.getItemMeta();
            sMeta.setDisplayName("§3Netherite Spear");
            spear.setItemMeta(sMeta);
            inv.setItem(26, spear);

            ItemStack enchantGuide = new ItemStack(Material.ENCHANTED_BOOK);
            ItemMeta guideMeta = enchantGuide.getItemMeta();
            guideMeta.setDisplayName("§d§lHow to Enchant");
            guideMeta.setLore(Arrays.asList("§7To enchant an item, simply", "§eShift + Right-Click §7it", "§7in your inventory below!"));
            enchantGuide.setItemMeta(guideMeta);
            inv.setItem(49, enchantGuide);

            inv.setItem(53, DefaultKitProvider.createNamedItem(Material.ARROW, "§aNext Page ->"));
        } else {
            // Potions Page
            PotionType[] types = { PotionType.STRONG_STRENGTH, PotionType.LONG_FIRE_RESISTANCE,
                    PotionType.LONG_SWIFTNESS, PotionType.STRONG_SWIFTNESS, PotionType.STRONG_TURTLE_MASTER };
            int slot = 0;
            for (PotionType type : types) { // Call from DefaultKitProvider
                ItemStack pot = DefaultKitProvider.createPotion(type, true);
                inv.setItem(slot, pot);
                inv.setItem(slot + 9, DefaultKitProvider.createShulker(Material.PURPLE_SHULKER_BOX, pot));
                slot++;
            }

            ItemStack enchantGuide = new ItemStack(Material.ENCHANTED_BOOK);
            ItemMeta guideMeta = enchantGuide.getItemMeta();
            guideMeta.setDisplayName("§d§lHow to Enchant");
            guideMeta.setLore(Arrays.asList("§7To enchant an item, simply", "§eShift + Right-Click §7it", "§7in your inventory below!"));
            enchantGuide.setItemMeta(guideMeta);
            inv.setItem(49, enchantGuide);

            inv.setItem(45, DefaultKitProvider.createNamedItem(Material.ARROW, "§c<- Previous Page"));
        }
        player.openInventory(inv);
    }

    public void openEnchantEditor(Player player, ItemStack item, int page) {
        if (item == null || item.getType() == Material.AIR)
            return;

        List<Enchantment> possible = new ArrayList<>();
        try {
            // Modern 1.21 Registry iteration
            for (Enchantment ench : org.bukkit.Registry.ENCHANTMENT) {
                if (ench.canEnchantItem(item))
                    possible.add(ench);
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
            inv.setItem(53, DefaultKitProvider.createNamedItem(Material.LIME_STAINED_GLASS_PANE, "§aNext Page"));
        }
        if (page > 1) {
            inv.setItem(45, DefaultKitProvider.createNamedItem(Material.LIME_STAINED_GLASS_PANE, "§cPrevious Page"));
        }

        player.openInventory(inv);
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