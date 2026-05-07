package me.yourname.eloplugin;

import org.bukkit.Bukkit;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;

public class DatabaseManager {

    private final Main plugin;
    private Connection connection;

    public DatabaseManager(Main plugin) {
        this.plugin = plugin;
        connect();
    }

    private void connect() {
        try {
            // Use plugin folder for database storage
            File pluginFolder = plugin.getDataFolder();
            if (!pluginFolder.exists()) {
                pluginFolder.mkdirs();
            }

            File dbFile = new File(pluginFolder, "database.db");
            String dbPath = dbFile.getAbsolutePath();

            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            createTable();
            updateSchema(); // Ensure new columns exist
            plugin.getLogger().info("Connected to database at " + dbPath);
        } catch (SQLException e) {
            plugin.getLogger().severe("Could not connect to database! Path: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    private void createTable() {
        try (Statement statement = connection.createStatement()) {
            // Create table if it doesn't exist
            statement.execute("CREATE TABLE IF NOT EXISTS player_stats (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "username TEXT, " +
                    "wins INTEGER DEFAULT 0, " +
                    "losses INTEGER DEFAULT 0, " +
                    "elo_data TEXT, " +
                    "verified BOOLEAN DEFAULT 0," +
                    "kit_layouts TEXT," +
                    "custom_maps TEXT)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateSchema() {
        addColumnIfNotExists("kit_layouts", "TEXT");
        addColumnIfNotExists("custom_maps", "TEXT");
    }

    private void addColumnIfNotExists(String columnName, String type) {
        try (ResultSet rs = connection.getMetaData().getColumns(null, null, "player_stats", columnName)) {
            if (!rs.next()) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("ALTER TABLE player_stats ADD COLUMN " + columnName + " " + type);
                    plugin.getLogger().info("Added missing column '" + columnName + "' to player_stats table.");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to check or add column '" + columnName + "': " + e.getMessage());
        }
    }

    public void saveUser(PvPUser user) {
        // Serialize the Map<String, Integer> of elo
        StringBuilder eloBuilder = new StringBuilder();
        for (Map.Entry<String, Integer> entry : user.getElosMap().entrySet()) {
            eloBuilder.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
        }

        // Serialize Kit Layouts
        StringBuilder layoutBuilder = new StringBuilder();
        for (Map.Entry<String, ItemStack[]> entry : user.getKitLayoutsMap().entrySet()) {
            String base64 = ItemStackSerializer.toBase64(entry.getValue());
            layoutBuilder.append(entry.getKey()).append("::").append(base64).append("||");
        }

        // Serialize Custom Kit Layouts (1-9)
        for (int i = 1; i <= 9; i++) {
            ItemStack[] layout = user.getCustomKitLayout(i);
            if (layout != null) {
                String base64 = ItemStackSerializer.toBase64(layout);
                layoutBuilder.append("custom_").append(i).append("::").append(base64).append("||");
            }
        }

        // Serialize Custom Map Selections
        StringBuilder mapBuilder = new StringBuilder();
        for (int i = 1; i <= 9; i++) {
            mapBuilder.append(i).append(":").append(user.getCustomKitMap(i)).append(",");
        }

        String sql = "INSERT OR REPLACE INTO player_stats (uuid, username, wins, losses, elo_data, verified, kit_layouts, custom_maps) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getUuid().toString());
            ps.setString(2, Bukkit.getOfflinePlayer(user.getUuid()).getName());
            ps.setInt(3, user.getWins());
            ps.setInt(4, user.getLosses());
            ps.setString(5, eloBuilder.toString());
            ps.setBoolean(6, user.isVerified());
            ps.setString(7, layoutBuilder.toString());
            ps.setString(8, mapBuilder.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadUser(PvPUser user) {
        String sql = "SELECT * FROM player_stats WHERE uuid=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getUuid().toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                user.setWins(rs.getInt("wins"));
                user.setLosses(rs.getInt("losses"));
                user.setVerified(rs.getBoolean("verified"));

                String eloData = rs.getString("elo_data");
                if (eloData != null && !eloData.isEmpty()) {
                    for (String part : eloData.split(",")) {
                        String[] pair = part.split(":");
                        if (pair.length == 2) {
                            try {
                                user.setElo(pair[0], Integer.parseInt(pair[1]));
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                }

                String layoutData = rs.getString("kit_layouts");
                if (layoutData != null && !layoutData.isEmpty()) {
                    for (String part : layoutData.split("\\|\\|")) {
                        String[] pair = part.split("::");
                        if (pair.length == 2) {
                            if (pair[0].startsWith("custom_")) {
                                try {
                                    int slot = Integer.parseInt(pair[0].split("_")[1]);
                                    user.saveCustomKitLayout(slot, ItemStackSerializer.fromBase64(pair[1]));
                                } catch (Exception ignored) {
                                }
                            } else {
                                user.saveKitLayout(pair[0], ItemStackSerializer.fromBase64(pair[1]));
                            }
                        }
                    }
                }

                String mapData = rs.getString("custom_maps");
                if (mapData != null && !mapData.isEmpty()) {
                    for (String part : mapData.split(",")) {
                        String[] pair = part.split(":");
                        if (pair.length == 2) {
                            try {
                                user.setCustomKitMap(Integer.parseInt(pair[0]), pair[1]);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteUser(UUID uuid) {
        String sql = "DELETE FROM player_stats WHERE uuid=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}