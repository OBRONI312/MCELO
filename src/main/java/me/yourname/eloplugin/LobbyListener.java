package me.yourname.eloplugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class LobbyListener implements Listener {

    private final Main plugin;

    public LobbyListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

        // Prevent usage if in match or editing
        if (plugin.getUserManager().getUser(player.getUniqueId()).isInMatch()) return;
        if (plugin.getKitEditorManager().getEditingKit(player.getUniqueId()) != null) return;

        String name = item.getItemMeta().getDisplayName();

        if (item.getType() == Material.DIAMOND_SWORD && name.contains("Ranked Queue")) {
            openKitGui(player, "§8Ranked Queue");
            event.setCancelled(true);
        } else if (item.getType() == Material.BOOK && name.contains("Kit Editor")) {
            openKitGui(player, "§8Kit Editor");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();

        if (title.equals("§8Ranked Queue") || title.equals("§8Kit Editor")) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;

            Player player = (Player) event.getWhoClicked();
            ItemStack clicked = event.getCurrentItem();
            List<String> lore = clicked.getItemMeta().getLore();

            if (lore != null) {
                for (String line : lore) {
                    if (line.startsWith("§0kit:")) {
                        String kitName = line.substring(6);
                        player.closeInventory();

                        if (title.equals("§8Ranked Queue")) {
                            player.performCommand("ranked join " + kitName);
                        } else {
                            player.performCommand("ranked editkit " + kitName);
                        }
                        break;
                    }
                }
            }
        }
    }

    private void openKitGui(Player player, String title) {
        Inventory inv = Bukkit.createInventory(null, 27, title);

        // Filler
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
        }
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, filler);
        }

        // Kit Items
        addKitItem(inv, 4, Material.END_CRYSTAL, "§aVanilla", "vanilla");
        addKitItem(inv, 10, Material.DIAMOND_SWORD, "§aSword", "sword");
        addKitItem(inv, 11, Material.DIAMOND_AXE, "§aAxe", "axe");
        addKitItem(inv, 12, Material.POTION, "§aPot", "pot");
        addKitItem(inv, 13, Material.NETHERITE_HELMET, "§aNethOP", "nethop");
        addKitItem(inv, 14, Material.MACE, "§aMace", "mace");
        addKitItem(inv, 15, Material.LAVA_BUCKET, "§aUHC", "uhc");
        addKitItem(inv, 16, Material.NETHERITE_SWORD, "§aSMP", "smp");
        addKitItem(inv, 21, Material.TNT_MINECART, "§aCart", "cartpvp");
        addKitItem(inv, 22, Material.COBWEB, "§aLifesteal", "lifesteal");
        addKitItem(inv, 23, Material.TRIDENT, "§aSpear-mace", "spear-mace");
        
        player.openInventory(inv);
    }

    private void addKitItem(Inventory inv, int slot, Material mat, String displayName, String kitId) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            meta.setLore(Arrays.asList("§7Click to select", "§0kit:" + kitId));
            item.setItemMeta(meta);
        }
        inv.setItem(slot, item);
    }
}