package me.yourname.eloplugin;

import org.bukkit.scheduler.BukkitRunnable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        Connection conn = plugin.getDatabaseManager().getConnection();
        if (conn == null)
            return;

        Map<String, PlayerData> dataMap = new HashMap<>();

        try {
            // 1. Fetch Users
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM player_stats")) {
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String uuid = rs.getString("uuid");
                    PlayerData pd = new PlayerData();
                    pd.uuid = uuid;
                    pd.username = rs.getString("username");
                    pd.verified = rs.getBoolean("verified");
                    pd.wins = rs.getInt("wins");
                    pd.losses = rs.getInt("losses");

                    if (pd.wins + pd.losses == 0)
                        continue;

                    // Parse Elo Data
                    String eloData = rs.getString("elo_data");
                    int totalElo = 0;
                    int kitCount = 0;

                    if (eloData != null && !eloData.isEmpty()) {
                        for (String part : eloData.split(",")) {
                            String[] pair = part.split(":");
                            if (pair.length == 2) {
                                try {
                                    int elo = Integer.parseInt(pair[1]);
                                    pd.kits.put(pair[0], elo);
                                    totalElo += elo;
                                    kitCount++;
                                } catch (NumberFormatException ignored) {
                                }
                            }
                        }
                    }

                    pd.overall = (kitCount > 0) ? (totalElo / kitCount) : 1000;
                    dataMap.put(uuid, pd);
                }
                rs.close();
            }

            // Step 2 removed because we parsed elo directly from the single table above

            // 3. Build JSON
            StringBuilder json = new StringBuilder();
            json.append("[");
            int i = 0;
            for (PlayerData pd : dataMap.values()) {
                if (i > 0)
                    json.append(",");
                json.append("{");
                json.append("\"uuid\":\"").append(pd.uuid).append("\",");
                json.append("\"username\":\"").append(pd.username).append("\",");
                json.append("\"verified\":").append(pd.verified).append(",");
                json.append("\"wins\":").append(pd.wins).append(",");
                json.append("\"losses\":").append(pd.losses).append(",");
                json.append("\"overall\":").append(pd.overall).append(",");

                json.append("\"kits\":{");
                int k = 0;
                for (Map.Entry<String, Integer> kit : pd.kits.entrySet()) {
                    if (k > 0)
                        json.append(",");
                    json.append("\"").append(kit.getKey()).append("\":").append(kit.getValue());
                    k++;
                }
                json.append("}");

                json.append("}");
                i++;
            }
            json.append("]");

            File file = new File(plugin.getDataFolder(), "leaderboard.json");
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(json.toString());
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private static class PlayerData {
        String uuid, username;
        boolean verified;
        int wins, losses, overall;
        Map<String, Integer> kits = new HashMap<>();
    }
}