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
            // Set the absolute path for the database as requested
            String dbPath = "/home/obroni/MCELO/plugins/EloPractice/database.db";
            File dbFile = new File(dbPath);
            
            // Fallback: If we cannot write to the hardcoded path (e.g. on Windows), use default folder
            if (!dbFile.getParentFile().exists() && !dbFile.getParentFile().mkdirs()) {
                plugin.getLogger().warning("Could not create directory for hardcoded path: " + dbPath);
                plugin.getLogger().info("Falling back to default plugin folder storage.");
                dbFile = new File(plugin.getDataFolder(), "database.db");
                dbPath = dbFile.getAbsolutePath();
            }
            
            // Ensure the directory structure exists
            if (dbFile.getParentFile() != null && !dbFile.getParentFile().exists()) {
                dbFile.getParentFile().mkdirs();
            }

            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            createTable();
            updateSchema(); // Ensure new columns exist
            plugin.getLogger().info("Connected to database at " + dbPath);
        } catch (SQLException | ClassNotFoundException e) {
            plugin.getLogger().severe("Could not connect to database! Path: /home/obroni/MCELO/plugins/EloPractice/database.db");
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
                    "kit_layouts TEXT)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateSchema() {
        // Attempt to add kit_layouts column if it doesn't exist (for existing databases)
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE player_stats ADD COLUMN kit_layouts TEXT");
        } catch (SQLException e) {
            // Column likely already exists, ignore
        }
    }

    public void saveUser(PvPUser user) {
        // Serialize the Map<String, Integer> of elo
        StringBuilder eloBuilder = new StringBuilder();
        for (String kit : plugin.getKitEditorManager().getValidKits()) {
            eloBuilder.append(kit).append(":").append(user.getElo(kit)).append(",");
        }

        // Serialize Kit Layouts
        StringBuilder layoutBuilder = new StringBuilder();
        for (Map.Entry<String, ItemStack[]> entry : user.getKitLayoutsMap().entrySet()) {
            String base64 = ItemStackSerializer.toBase64(entry.getValue());
            layoutBuilder.append(entry.getKey()).append("::").append(base64).append("||");
        }

        String sql = "INSERT OR REPLACE INTO player_stats (uuid, username, wins, losses, elo_data, verified, kit_layouts) VALUES(?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getUuid().toString());
            ps.setString(2, Bukkit.getOfflinePlayer(user.getUuid()).getName());
            ps.setInt(3, user.getWins());
            ps.setInt(4, user.getLosses());
            ps.setString(5, eloBuilder.toString());
            ps.setBoolean(6, user.isVerified());
            ps.setString(7, layoutBuilder.toString());
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
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }

                String layoutData = rs.getString("kit_layouts");
                if (layoutData != null && !layoutData.isEmpty()) {
                    for (String part : layoutData.split("\\|\\|")) {
                        String[] pair = part.split("::");
                        if (pair.length == 2) {
                            user.saveKitLayout(pair[0], ItemStackSerializer.fromBase64(pair[1]));
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