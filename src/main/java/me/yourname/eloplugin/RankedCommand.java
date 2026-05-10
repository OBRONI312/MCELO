package me.yourname.eloplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RankedCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;
    private final List<String> validKits;

    public RankedCommand(Main plugin) {
        this.plugin = plugin;
        this.validKits = plugin.getKitEditorManager().getValidKits();
    }

    // FIXED: Added the correct 4 parameters (Sender, Command, Label, Args)
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "join":
                handleJoin(player, args);
                break;
            case "editkit":
                handleEdit(player, args);
                break;
            case "stats":
                handleStats(player);
                break;
            case "savekit":
                handleSaveKit(player);
                break;
            case "palette":
                handlePalette(player);
                break;
            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void handleJoin(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /ranked join <kit>");
            return;
        }

        String kit = args[1].toLowerCase();
        if (!validKits.contains(kit)) {
            player.sendMessage("§cInvalid kit! Available: " + String.join(", ", validKits));
            return;
        }

        plugin.getQueueManager().joinQueue(player.getUniqueId(), kit);
    }

    private void handleEdit(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /ranked editkit <kit>");
            return;
        }

        String kit = args[1].toLowerCase();
        if (!validKits.contains(kit)) {
            player.sendMessage("§cInvalid kit!");
            return;
        }

        plugin.getKitEditorManager().openEditor(player, kit, false);
    }

    private void handleSaveKit(Player player) {
        if (plugin.getKitEditorManager().getEditingKit(player.getUniqueId()) == null) {
            player.sendMessage("§cYou are not editing a kit!");
            return;
        }
        plugin.getKitEditorManager().saveKit(player);
    }

    private void handlePalette(Player player) {
        UUID playerUUID = player.getUniqueId();
        String editing = plugin.getKitEditorManager().getEditingKit(playerUUID);
        if (editing == null || !editing.equals("vanilla")) {
            player.sendMessage("§cThe palette is only available while editing the vanilla kit.");
            return;
        }
        plugin.getKitEditorManager().openVanillaPalette(player, 1);
    }

    private void handleStats(Player player) {
        PvPUser user = plugin.getUserManager().getUser(player.getUniqueId());
        player.sendMessage("§6§lYOUR STATS");
        player.sendMessage("§eWins: §f" + user.getWins());
        player.sendMessage("§eLosses: §f" + user.getLosses());
        player.sendMessage("§6§lKit Ratings:");
        for (String kit : validKits) {
            int matches = user.getKitMatches(kit);
            int elo = user.getElo(kit);
            String status = matches >= 3 ? "§f" + elo : "§7Unranked (" + matches + "/3)";
            player.sendMessage(" §7- §e" + kit + ": " + status);
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6§lRanked Commands");
        player.sendMessage("§e/ranked join <kit> §7- Queue for a match");
        player.sendMessage("§e/ranked editkit <kit> §7- Customize your layout");
        player.sendMessage("§e/ranked savekit §7- Save your current layout");
        player.sendMessage("§e/ranked palette §7- Item Selection (Vanilla only)");
        player.sendMessage("§e/ranked stats §7- View your rank");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias,
            @NotNull String[] args) {
        if (args.length == 1) {
            List<String> cmds = new ArrayList<>(Arrays.asList("join", "editkit", "stats", "savekit", "palette"));
            return cmds.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("editkit"))) {
            return validKits.stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}