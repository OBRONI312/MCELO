package me.yourname.eloplugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class KitListener implements Listener {

    private final Main plugin;

    public KitListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (plugin.getKitEditorManager().getEditingKit(player.getUniqueId()) == null) return;

        // Handle GUI interactions
        String title = event.getView().getTitle();
        if (title.startsWith("§8Vanilla Palette")) {
            event.setCancelled(true);
            handlePaletteClick(player, event.getCurrentItem(), title);
            return;
        }
        if (title.startsWith("§8Enchant:")) {
            event.setCancelled(true);
            handleEnchantClick(player, event.getCurrentItem(), event.isRightClick(), title);
            return;
        }

        // Prevent dropping via inventory UI
        if (event.getAction() == InventoryAction.DROP_ALL_CURSOR || 
            event.getAction() == InventoryAction.DROP_ALL_SLOT || 
            event.getAction() == InventoryAction.DROP_ONE_CURSOR || 
            event.getAction() == InventoryAction.DROP_ONE_SLOT) {
            event.setCancelled(true);
        }

        // Shift + Right Click to Enchant
        if (event.isShiftClick() && event.isRightClick() && event.getClickedInventory() == player.getInventory()) {
            if ("vanilla".equals(plugin.getKitEditorManager().getEditingKit(player.getUniqueId()))) {
                event.setCancelled(true);
                ItemStack item = event.getCurrentItem();
                if (item != null && item.getType() != Material.AIR) {
                    plugin.getKitEditorManager().getEnchantingSlot().put(player.getUniqueId(), event.getSlot());
                    plugin.getKitEditorManager().openEnchantEditor(player, item, 1);
                }
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (plugin.getKitEditorManager().getEditingKit(event.getPlayer().getUniqueId()) != null) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cYou cannot drop items while editing. Organize only.");
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (plugin.getKitEditorManager().getEditingKit(player.getUniqueId()) != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (plugin.getKitEditorManager().getEditingKit(event.getEntity().getUniqueId()) != null) {
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (plugin.getKitEditorManager().getEditingKit(event.getPlayer().getUniqueId()) != null) {
            Location from = event.getFrom();
            Location to = event.getTo();
            // Allow looking around (pitch/yaw) but prevent X, Y, Z movement
            if (to != null && (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ())) {
                Location newLoc = from.clone();
                newLoc.setPitch(to.getPitch());
                newLoc.setYaw(to.getYaw());
                event.setTo(newLoc);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (plugin.getKitEditorManager().getEditingKit(player.getUniqueId()) != null) {
            event.setCancelled(true);
            return;
        }

        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.PLAYER_HEAD && item.hasItemMeta()) {
            if ("§6Golden Head".equals(item.getItemMeta().getDisplayName())) {
                if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    event.setCancelled(true);
                    if (!player.hasCooldown(Material.PLAYER_HEAD)) {
                        item.setAmount(item.getAmount() - 1);
                        player.removePotionEffect(PotionEffectType.REGENERATION);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1)); // Regen 2 for 10s
                        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 0)); // Abs 1 for 2m
                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1f, 1f);
                        player.setCooldown(Material.PLAYER_HEAD, 20);
                    }
                }
            }
        }
    }

    private void handlePaletteClick(Player player, ItemStack clicked, String title) {
        if (clicked == null) return;
        if (clicked.getType() == Material.ARROW) {
            int page = title.contains("Page 1") ? 2 : 1;
            plugin.getKitEditorManager().openVanillaPalette(player, page);
            return;
        }
        player.getInventory().addItem(clicked.clone());
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
    }

    private void handleEnchantClick(Player player, ItemStack clicked, boolean isRightClick, String title) {
        if (clicked == null) return;

        // Handle Pagination for Enchantments
        if (clicked.getType() == Material.LIME_STAINED_GLASS_PANE) {
            int page = 1;
            try {
                String[] parts = title.split(" - Page ");
                if (parts.length > 1) page = Integer.parseInt(parts[1]);
            } catch (Exception ignored) {}

            if (clicked.getItemMeta().getDisplayName().contains("Next")) page++;
            else if (page > 1) page--;

            int slot = plugin.getKitEditorManager().getEnchantingSlot().getOrDefault(player.getUniqueId(), -1);
            ItemStack item = player.getInventory().getItem(slot);
            plugin.getKitEditorManager().openEnchantEditor(player, item, page);
            return;
        }

        if (clicked.getType() != Material.ENCHANTED_BOOK) return;
        
        int slot = plugin.getKitEditorManager().getEnchantingSlot().getOrDefault(player.getUniqueId(), -1);
        if (slot == -1) return;

        ItemStack item = player.getInventory().getItem(slot);
        if (item == null) return;

        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) clicked.getItemMeta();
        if (meta == null || meta.getStoredEnchants().isEmpty()) return;

        Enchantment ench = meta.getStoredEnchants().keySet().iterator().next();
        
        int page = 1; 
        try { 
            String[] parts = title.split(" - Page ");
            if (parts.length > 1) page = Integer.parseInt(parts[1].trim());
        } catch (Exception ignored) {}

        if (isRightClick) {
            item.removeEnchantment(ench);
            player.sendMessage("§cRemoved " + ench.getKey().getKey());
        } else {
            // Check conflicts
            for (Enchantment existing : item.getEnchantments().keySet()) {
                if (ench.conflictsWith(existing) && !ench.equals(existing)) {
                    player.sendMessage("§cConflict: " + ench.getKey().getKey() + " conflicts with " + existing.getKey().getKey());
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    return;
                }
            }
            
            // Handle Survival Logic: Multishot vs Piercing, Infinity vs Mending
            if (isVanillaConflict(item.getType(), ench, item)) {
                player.sendMessage("§cThese enchantments are incompatible in Vanilla Minecraft.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }

            item.addUnsafeEnchantment(ench, ench.getMaxLevel());
            player.sendMessage("§aApplied " + ench.getKey().getKey());
        }
        
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f);
        plugin.getKitEditorManager().openEnchantEditor(player, item, page);
    }

    private boolean isVanillaConflict(Material mat, Enchantment ench, ItemStack item) {
        if (mat == Material.CROSSBOW) {
            if (ench.equals(Enchantment.MULTISHOT) && item.containsEnchantment(Enchantment.PIERCING)) return true;
            if (ench.equals(Enchantment.PIERCING) && item.containsEnchantment(Enchantment.MULTISHOT)) return true;
        }
        if (mat == Material.BOW) {
            if (ench.equals(Enchantment.ARROW_INFINITE) && item.containsEnchantment(Enchantment.MENDING)) return true;
            if (ench.equals(Enchantment.MENDING) && item.containsEnchantment(Enchantment.ARROW_INFINITE)) return true;
        }
        if (mat == Material.TRIDENT) {
            if (ench.equals(Enchantment.RIPTIDE) && (item.containsEnchantment(Enchantment.LOYALTY) || item.containsEnchantment(Enchantment.CHANNELING))) return true;
            if ((ench.equals(Enchantment.LOYALTY) || ench.equals(Enchantment.CHANNELING)) && item.containsEnchantment(Enchantment.RIPTIDE)) return true;
        }
        // Bukkit's conflictsWith covers Protection types, Sharpness types, etc.
        return false;
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        if (plugin.getKitEditorManager().getEditingKit(event.getPlayer().getUniqueId()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (plugin.getKitEditorManager().getEditingKit(event.getPlayer().getUniqueId()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (plugin.getKitEditorManager().getEditingKit(event.getPlayer().getUniqueId()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (plugin.getKitEditorManager().getEditingKit(player.getUniqueId()) != null) {
                event.setCancelled(true);
            }
        }
    }
}