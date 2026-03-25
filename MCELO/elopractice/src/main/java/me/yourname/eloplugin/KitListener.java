package me.yourname.eloplugin;

import org.bukkit.Location;
import org.bukkit.Material;
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

        // Prevent dropping via inventory UI
        if (event.getAction() == InventoryAction.DROP_ALL_CURSOR || 
            event.getAction() == InventoryAction.DROP_ALL_SLOT || 
            event.getAction() == InventoryAction.DROP_ONE_CURSOR || 
            event.getAction() == InventoryAction.DROP_ONE_SLOT) {
            event.setCancelled(true);
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