package me.yourname.eloplugin;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ArenaManager {

    private final Main plugin;
    private int matchCounter = 0;

    public ArenaManager(Main plugin) {
        this.plugin = plugin;
        // Automatically create the arenas folder if it doesn't exist
        File arenaFolder = new File(plugin.getDataFolder(), "arenas");
        if (!arenaFolder.exists()) {
            arenaFolder.mkdirs();
        }

        // Automatically create subfolders for each valid kit so you know where to put files
        if (plugin.getKitEditorManager() != null) {
            for (String kit : plugin.getKitEditorManager().getValidKits()) {
                File kitDir = new File(arenaFolder, kit);
                if (!kitDir.exists()) {
                    kitDir.mkdirs();
                }
            }
        }
    }

    public ArenaInstance pasteArena(String kitName) {
        matchCounter++;
        int xOffset = matchCounter * 500;
        
        World world = Bukkit.getWorlds().get(0);
        Location center = new Location(world, xOffset, 100, 0);

        // 1. Try to find a schematic file
        File kitFolder = new File(new File(plugin.getDataFolder(), "arenas"), kitName);
        File schemFile = null;

        if (kitFolder.exists() && kitFolder.isDirectory()) {
            File[] files = kitFolder.listFiles((dir, name) -> name.endsWith(".schem") || name.endsWith(".schematic"));
            if (files != null && files.length > 0) {
                schemFile = files[ThreadLocalRandom.current().nextInt(files.length)];
            } else {
                plugin.getLogger().warning("No .schem files found in " + kitFolder.getPath() + " for kit '" + kitName + "'.");
            }
        } else {
            plugin.getLogger().warning("Arena folder not found: " + kitFolder.getPath());
        }

        // 2. Try to load the schematic into a clipboard
        Clipboard clipboard = null;
        if (schemFile != null) {
            ClipboardFormat format = ClipboardFormats.findByFile(schemFile);
            if (format != null) {
                try (ClipboardReader reader = format.getReader(new FileInputStream(schemFile))) {
                    clipboard = reader.read();
                } catch (IOException e) {
                    plugin.getLogger().severe("Could not load schematic file: " + schemFile.getName());
                    e.printStackTrace();
                }
            } else {
                plugin.getLogger().warning("Unsupported schematic format for: " + schemFile.getName());
            }
        }

        // 3. If clipboard is loaded, paste it. Otherwise, use fallback.
        if (clipboard != null) {
            try {
                // --- Create Undo Clipboard ---
                BlockVector3 pasteCenter = BlockVector3.at(center.getX(), center.getY(), center.getZ());
                Region pasteRegion = clipboard.getRegion().clone();
                pasteRegion.shift(pasteCenter.subtract(clipboard.getOrigin()));

                BlockArrayClipboard undoClipboard = new BlockArrayClipboard(pasteRegion);

                try (EditSession copySession = WorldEdit.getInstance().newEditSessionBuilder().world(BukkitAdapter.adapt(world)).build()) {
                    ForwardExtentCopy copyOperation = new ForwardExtentCopy(
                        copySession, pasteRegion, undoClipboard, pasteRegion.getMinimumPoint()
                    );
                    Operations.complete(copyOperation);
                }

                // --- Paste Arena Schematic ---
                try (EditSession pasteSession = WorldEdit.getInstance().newEditSessionBuilder().world(BukkitAdapter.adapt(world)).build()) {
                    Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(pasteSession)
                        .to(pasteCenter)
                        .ignoreAirBlocks(true)
                        .build();
                    Operations.complete(operation);
                }
                
                // Successfully pasted, return instance with both clipboards
                return new ArenaInstance(center, clipboard, undoClipboard);

            } catch (Exception e) {
                plugin.getLogger().severe("An error occurred during arena creation/pasting!");
                e.printStackTrace();
            }
        }
        
        // Fallback: This code is reached if schemFile is null, clipboard failed to load, or pasting threw an exception.
        plugin.getLogger().info("Generating fallback Bedrock platform for kit: " + kitName);
        createPlatform(center);
        return new ArenaInstance(center, null, null);
    }

    private void createPlatform(Location center) {
        World w = center.getWorld();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        // Create a 20x20 bedrock platform
        for (int x = -10; x <= 10; x++) {
            for (int z = -10; z <= 10; z++) {
                w.getBlockAt(cx + x, cy - 1, cz + z).setType(Material.BEDROCK);
            }
        }
    }

    public void resetArenaCounter() {
        this.matchCounter = 0;
    }

    public class ArenaInstance {
        private final Location center;
        private final List<Location> spawnPoints = new ArrayList<>();
        private final Clipboard schematicClipboard;
        private final Clipboard undoClipboard;

        public ArenaInstance(Location center, Clipboard schematicClipboard, Clipboard undoClipboard) {
            this.center = center;
            this.schematicClipboard = schematicClipboard;
            this.undoClipboard = undoClipboard;
            
            if (this.schematicClipboard != null) {
                calculateSchematicSpawns();
            } else {
                // Manual spawn points for the fallback platform (Red/Blue sides)
                spawnPoints.add(center.clone().add(-8, 1, 0)); // P1
                spawnPoints.add(center.clone().add(8, 1, 0));  // P2
            }
        }

        private void calculateSchematicSpawns() {
            if (schematicClipboard.getRegion() == null) return;
            
            Location redLoc = null;
            Location blueLoc = null;

            for (BlockVector3 vec : schematicClipboard.getRegion()) {
                String type = schematicClipboard.getBlock(vec).getBlockType().toString();
                
                if (type.contains("red_concrete")) {
                    redLoc = getPasteLocation(vec);
                } else if (type.contains("blue_concrete")) {
                    blueLoc = getPasteLocation(vec);
                }
            }

            // Order matters: P1 (Red), P2 (Blue)
            if (redLoc != null) {
                spawnPoints.add(redLoc);
            } else {
                // If red concrete is missing, use the center of the schematic
                spawnPoints.add(center.clone().add(0.5, 1, 0.5));
            }

            if (blueLoc != null) {
                spawnPoints.add(blueLoc);
            } else {
                // If blue concrete is missing, use the center of the schematic
                spawnPoints.add(center.clone().add(0.5, 1, 0.5));
            }
        }

        private Location getPasteLocation(BlockVector3 vec) {
            BlockVector3 offset = vec.subtract(schematicClipboard.getOrigin());
            // +1 Y so they stand on top of the block, +0.5 to center
            return center.clone().add(offset.x(), offset.y() + 1, offset.z()).add(0.5, 0, 0.5);
        }

        public List<Location> getSpawnPoints() {
            return spawnPoints;
        }

        public void remove() {
            if (undoClipboard != null) {
                try (EditSession undoSession = WorldEdit.getInstance().newEditSessionBuilder().world(BukkitAdapter.adapt(center.getWorld())).build()) {
                    Operation operation = new ClipboardHolder(undoClipboard)
                            .createPaste(undoSession)
                            .to(undoClipboard.getRegion().getMinimumPoint())
                            .ignoreAirBlocks(false) // Paste air to remove new blocks
                            .build();
                    Operations.complete(operation);
                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to roll back arena schematic!");
                    e.printStackTrace();
                }
            } else {
                // Remove fallback platform
                World w = center.getWorld();
                int cx = center.getBlockX();
                int cy = center.getBlockY();
                int cz = center.getBlockZ();
                for (int x = -10; x <= 10; x++) {
                    for (int z = -10; z <= 10; z++) {
                        w.getBlockAt(cx + x, cy - 1, cz + z).setType(Material.AIR);
                    }
                }
            }
        }
    }
}