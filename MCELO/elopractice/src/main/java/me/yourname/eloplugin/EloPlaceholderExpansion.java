package me.yourname.eloplugin;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class EloPlaceholderExpansion extends PlaceholderExpansion {

    private final Main plugin;

    public EloPlaceholderExpansion(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getAuthor() {
        return "YourName"; 
    }

    @Override
    public @NotNull String getIdentifier() {
        return "elopractice"; // This defines the %elopractice_...% part
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true; // Keeps the placeholder registered during reloads
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        // This handles %elopractice_elo_<kitname>%
        if (params.startsWith("elo_")) {
            String kitName = params.substring(4);
            PvPUser user = plugin.getUserManager().getUser(player.getUniqueId());
            return String.valueOf(user.getElo(kitName));
        }

        return null; // Placeholder not found
    }
}