package me.yourname.eloplugin;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AdminCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public AdminCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("elopractice.admin")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sendHelp(sender, label);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        String playerName = args[1];

        @SuppressWarnings("deprecation")
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(playerName);

        if (!targetPlayer.hasPlayedBefore() && !targetPlayer.isOnline()) {
            sender.sendMessage("§cPlayer '" + playerName + "' not found.");
            return true;
        }

        PvPUser user = plugin.getUserManager().getUser(targetPlayer.getUniqueId());

        // If the player is offline, load their data first to prevent wiping stats/layouts with defaults
        if (!targetPlayer.isOnline()) {
            plugin.getDatabaseManager().loadUser(user);
        }

        switch (subCommand) {
            case "verify":
                user.setVerified(true);
                sender.sendMessage("§a" + targetPlayer.getName() + " has been given the 'Verified' rank.");
                break;
            case "unverify":
                user.setVerified(false);
                sender.sendMessage("§aThe 'Verified' rank has been removed from " + targetPlayer.getName() + ".");
                break;
            case "reset":
                if (targetPlayer.isOnline()) {
                    Player p = targetPlayer.getPlayer();
                    plugin.getUserManager().getUsers().remove(p.getUniqueId());
                    p.kickPlayer("§cYour data has been reset by an administrator.");
                } else {
                    plugin.getUserManager().getUsers().remove(targetPlayer.getUniqueId());
                }
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    plugin.getDatabaseManager().deleteUser(targetPlayer.getUniqueId());
                    sender.sendMessage("§aSuccessfully reset data for " + targetPlayer.getName() + ".");
                });
                return true;
            default:
                sendHelp(sender, label);
                break;
        }

        // Save changes to database asynchronously
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getDatabaseManager().saveUser(user);
        });

        return true;
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage("§6§lElo Admin Commands");
        sender.sendMessage("§e/" + label + " verify <player> §7- Give a player the verified rank.");
        sender.sendMessage("§e/" + label + " unverify <player> §7- Remove a player's verified rank.");
        sender.sendMessage("§e/" + label + " reset <player> §7- Wipe a player's data from the database.");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("verify", "unverify", "reset").stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("verify") || args[0].equalsIgnoreCase("unverify") || args[0].equalsIgnoreCase("reset"))) {
            return null; // Bukkit's default behavior will suggest online player names
        }
        return new ArrayList<>();
    }
}