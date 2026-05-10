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
        if (db == null) return;

        Map<UUID, DatabaseManager.UserRecord> cache = db.getCache();
        List<Map<String, Object>> leaderboard = new ArrayList<>();

        for (Map.Entry<UUID, DatabaseManager.UserRecord> entry : cache.entrySet()) {
            DatabaseManager.UserRecord record = entry.getValue();
            
            // Only include players who have actually played (wins + losses > 0)
            if (record.wins + record.losses == 0) continue;

            Map<String, Object> playerMap = new HashMap<>();
            playerMap.put("uuid", entry.getKey().toString());
            playerMap.put("username", record.username);
            playerMap.put("verified", record.verified);
            playerMap.put("wins", record.wins);
            playerMap.put("losses", record.losses);
            playerMap.put("kits", record.elos);

            // Calculate overall average Elo
            int totalElo = 0;
            int count = 0;
            if (record.elos != null) {
                for (int elo : record.elos.values()) {
                    totalElo += elo;
                    count++;
                }
            }
            playerMap.put("overall", count > 0 ? (totalElo / count) : 1000);

            leaderboard.add(playerMap);
        }

        // Sort by overall Elo descending for the JSON file (optional but helpful)
        leaderboard.sort((a, b) -> (int)b.get("overall") - (int)a.get("overall"));

        // Build JSON manually to avoid adding another dependency or complex Gson setup here
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