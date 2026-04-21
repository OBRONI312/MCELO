package me.yourname.eloplugin;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CustomCommand implements CommandExecutor, TabCompleter {

  private final Main plugin;

  public CustomCommand(Main plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
      @NotNull String[] args) {
    if (!(sender instanceof Player))
      return true;
    Player player = (Player) sender;

    if (args.length == 0) {
      sendHelp(player);
      return true;
    }

    switch (args[0].toLowerCase()) {
      case "edit":
        plugin.getKitEditorManager().openCustomKitSelector(player);
        break;
      case "duel":
        handleDuel(player, args);
        break;
      case "duelaccept":
        plugin.getQueueManager().acceptDuel(player);
        break;
      case "fight":
        handleFight(player, args);
        break;
      case "randomque":
        plugin.getQueueManager().joinRandomCustomQueue(player.getUniqueId());
        break;
      default:
        sendHelp(player);
        break;
    }
    return true;
  }

  private void handleDuel(Player player, String[] args) {
    if (args.length < 3) {
      player.sendMessage("§cUsage: /custom duel <kit#> <player>");
      return;
    }
    try {
      int slot = Integer.parseInt(args[1]);
      if (slot < 1 || slot > 9)
        throw new NumberFormatException();
      Player target = Bukkit.getPlayer(args[2]);
      if (target == null) {
        player.sendMessage("§cPlayer not found.");
        return;
      }
      plugin.getQueueManager().sendDuel(player, target, slot);
    } catch (NumberFormatException e) {
      player.sendMessage("§cKit number must be 1-9.");
    }
  }

  private void handleFight(Player player, String[] args) {
    if (args.length < 2) {
      player.sendMessage("§cUsage: /custom fight <kit#>");
      return;
    }
    try {
      int slot = Integer.parseInt(args[1]);
      if (slot < 1 || slot > 9)
        throw new NumberFormatException();
      plugin.getQueueManager().joinCustomQueue(player.getUniqueId(), slot);
    } catch (NumberFormatException e) {
      player.sendMessage("§cKit number must be 1-9.");
    }
  }

  private void sendHelp(Player player) {
    player.sendMessage("§6§lCustom Kit Commands");
    player.sendMessage("§e/custom edit §7- Open slot selector");
    player.sendMessage("§e/custom duel <1-9> <player> §7- Challenge someone");
    player.sendMessage("§e/custom duelaccept §7- Accept challenge");
    player.sendMessage("§e/custom fight <1-9> §7- Queue for a specific custom kit");
    player.sendMessage("§e/custom randomque §7- Match against any custom kit");
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
      @NotNull String alias, @NotNull String[] args) {
    if (args.length == 1) {
      return Arrays.asList("edit", "duel", "duelaccept", "fight", "randomque").stream()
          .filter(s -> s.startsWith(args[0].toLowerCase()))
          .collect(Collectors.toList());
    }
    if (args.length == 2) {
      if (args[0].equalsIgnoreCase("duel") || args[0].equalsIgnoreCase("fight")) {
        return Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9").stream()
            .filter(s -> s.startsWith(args[1]))
            .collect(Collectors.toList());
      }
    }
    if (args.length == 3 && args[0].equalsIgnoreCase("duel")) {
      return null; // Default behavior: suggest online players
    }
    return new ArrayList<>();
  }
}