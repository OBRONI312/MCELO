package me.yourname.eloplugin;

import org.bukkit.scheduler.BukkitRunnable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class LeaderboardExporter extends BukkitRunnable {

    private final Main plugin;

    public LeaderboardExporter(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        new Thread(this::export).start();
    }

    private void export() {
        DatabaseManager db = plugin.getDatabaseManager();
        if (db == null)
            return;

        Map<UUID, DatabaseManager.UserRecord> cache = db.getCache();
        if (cache.isEmpty()) {
            // If the cache is empty, we don't want to overwrite a potentially good
            // leaderboard.json with []
            // This usually happens if DatabaseManager failed to load or on a very first
            // run.
            return;
        }

        List<Map<String, Object>> leaderboard = new ArrayList<>();

        for (Map.Entry<UUID, DatabaseManager.UserRecord> entry : cache.entrySet()) {
            DatabaseManager.UserRecord record = entry.getValue();

            // Filter kits by match count (minimum 3) for both individual display and
            // overall calculation
            Map<String, Integer> filteredKits = new HashMap<>();
            int totalElo = 0;
            int qualifiedKits = 0;

            if (record.elos != null && record.kitMatches != null) {
                for (Map.Entry<String, Integer> eloEntry : record.elos.entrySet()) {
                    String kitName = eloEntry.getKey();
                    int matchesInKit = record.kitMatches.getOrDefault(kitName, 0);

                    if (matchesInKit >= 3) {
                        int eloValue = eloEntry.getValue();
                        filteredKits.put(kitName, eloValue);
                        totalElo += eloValue;
                        qualifiedKits++;
                    }
                }
            }

            // Only show the player if they have at least one kit with 3+ matches
            if (qualifiedKits == 0)
                continue;

            Map<String, Object> playerMap = new HashMap<>();
            playerMap.put("uuid", entry.getKey().toString());
            playerMap.put("username", record.username);
            playerMap.put("verified", record.verified);
            playerMap.put("wins", record.wins);
            playerMap.put("losses", record.losses);
            playerMap.put("kits", filteredKits);
            playerMap.put("overall", totalElo / qualifiedKits);

            leaderboard.add(playerMap);
        }

        // Sort by overall Elo descending for the JSON file (optional but helpful)
        leaderboard.sort((a, b) -> (int) b.get("overall") - (int) a.get("overall"));

        // Build JSON manually to avoid adding another dependency or complex Gson setup
        // here
        // Actually we have Gson in DatabaseManager, but we can just use it here too.
        com.google.gson.Gson gson = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(leaderboard);

        File file = new File(plugin.getDataFolder(), "leaderboard.json");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(jsonString);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not export leaderboard.json: " + e.getMessage());
        }
    }
}