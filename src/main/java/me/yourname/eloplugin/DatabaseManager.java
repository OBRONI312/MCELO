package me.yourname.eloplugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseManager {

    private final Main plugin;
    private final File dataFile;
    private final Map<UUID, UserRecord> cache = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public DatabaseManager(Main plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "players.json");
        
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        loadAll();
    }

    private void loadAll() {
        if (!dataFile.exists()) return;
        try (Reader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Map<UUID, UserRecord>>() {}.getType();
            Map<UUID, UserRecord> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                cache.putAll(loaded);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Could not load players.json: " + e.getMessage());
        }
    }

    public synchronized void saveAll() {
        try (Writer writer = new FileWriter(dataFile)) {
            gson.toJson(cache, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save players.json: " + e.getMessage());
        }
    }

    public void close() {
        saveAll();
    }

    public void saveUser(PvPUser user) {
        UserRecord record = new UserRecord();
        record.username = Bukkit.getOfflinePlayer(user.getUuid()).getName();
        record.wins = user.getWins();
        record.losses = user.getLosses();
        record.elos = new HashMap<>(user.getElosMap());
        record.verified = user.isVerified();

        // Serialize Kit Layouts
        StringBuilder layoutBuilder = new StringBuilder();
        for (Map.Entry<String, ItemStack[]> entry : user.getKitLayoutsMap().entrySet()) {
            String base64 = ItemStackSerializer.toBase64(entry.getValue());
            layoutBuilder.append(entry.getKey()).append("::").append(base64).append("||");
        }
        for (int i = 1; i <= 9; i++) {
            ItemStack[] layout = user.getCustomKitLayout(i);
            if (layout != null) {
                String base64 = ItemStackSerializer.toBase64(layout);
                layoutBuilder.append("custom_").append(i).append("::").append(base64).append("||");
            }
        }
        record.kitLayouts = layoutBuilder.toString();

        // Serialize Maps
        StringBuilder mapBuilder = new StringBuilder();
        for (int i = 1; i <= 9; i++) {
            mapBuilder.append(i).append(":").append(user.getCustomKitMap(i)).append(",");
        }
        record.customMaps = mapBuilder.toString();

        cache.put(user.getUuid(), record);
        saveAll();
    }

    public void loadUser(PvPUser user) {
        UserRecord record = cache.get(user.getUuid());
        if (record != null) {
            user.setWins(record.wins);
            user.setLosses(record.losses);
            user.setVerified(record.verified);
            if (record.elos != null) {
                for (Map.Entry<String, Integer> entry : record.elos.entrySet()) {
                    user.setElo(entry.getKey(), entry.getValue());
                }
            }

            if (record.kitLayouts != null && !record.kitLayouts.isEmpty()) {
                for (String part : record.kitLayouts.split("\\|\\|")) {
                    String[] pair = part.split("::");
                    if (pair.length == 2) {
                        if (pair[0].startsWith("custom_")) {
                            try {
                                int slot = Integer.parseInt(pair[0].split("_")[1]);
                                user.saveCustomKitLayout(slot, ItemStackSerializer.fromBase64(pair[1]));
                            } catch (Exception ignored) {}
                        } else {
                            user.saveKitLayout(pair[0], ItemStackSerializer.fromBase64(pair[1]));
                        }
                    }
                }
            }

            if (record.customMaps != null && !record.customMaps.isEmpty()) {
                for (String part : record.customMaps.split(",")) {
                    String[] pair = part.split(":");
                    if (pair.length == 2) {
                        try {
                            user.setCustomKitMap(Integer.parseInt(pair[0]), pair[1]);
                        } catch (Exception ignored) {}
                    }
                }
            }
        }
    }

    public void deleteUser(UUID uuid) {
        cache.remove(uuid);
        saveAll();
    }

    // This method is kept for compatibility with LeaderboardExporter but doesn't return a Connection anymore
    // We will update LeaderboardExporter to not use it.
    @Deprecated
    public Object getConnection() {
        return null;
    }

    public Map<UUID, UserRecord> getCache() {
        return cache;
    }

    public static class UserRecord {
        public String username;
        public int wins;
        public int losses;
        public Map<String, Integer> elos;
        public boolean verified;
        public String kitLayouts;
        public String customMaps;
    }
}