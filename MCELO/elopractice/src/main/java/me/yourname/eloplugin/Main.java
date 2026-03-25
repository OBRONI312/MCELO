package me.yourname.eloplugin;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private UserManager userManager;
    private MatchManager matchManager;
    private QueueManager queueManager;
    private KitEditorManager kitEditorManager;
    private ArenaManager arenaManager;
    private DatabaseManager databaseManager;
    private WebServer webServer;

    @Override
    public void onEnable() {
        // Create config.yml and plugin folder if they don't exist
        loadConfig();

        // Initialize everything
        this.databaseManager = new DatabaseManager(this);
        this.userManager = new UserManager(this);
        this.matchManager = new MatchManager();
        this.queueManager = new QueueManager(this);
        this.kitEditorManager = new KitEditorManager(this);
        this.arenaManager = new ArenaManager(this);

        // Start Web Server for Leaderboard
        this.webServer = new WebServer(this);
        this.webServer.start();

        // Register Command
        RankedCommand rankedCmd = new RankedCommand(this);
        if (getCommand("ranked") != null) {
            getCommand("ranked").setExecutor(rankedCmd);
            getCommand("ranked").setTabCompleter(rankedCmd);
        }

        // Register Admin Command
        AdminCommand adminCmd = new AdminCommand(this);
        if (getCommand("elo-admin") != null) {
            getCommand("elo-admin").setExecutor(adminCmd);
            getCommand("elo-admin").setTabCompleter(adminCmd);
        }
        
        // Register Listener
        getServer().getPluginManager().registerEvents(new MatchListener(this), this);
        getServer().getPluginManager().registerEvents(new KitListener(this), this);
        getServer().getPluginManager().registerEvents(new LobbyListener(this), this);

        // Register PAPI Expansion
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new EloPlaceholderExpansion(this).register();
        }

        // Run Leaderboard Export every 5 minutes (6000 ticks)
        new LeaderboardExporter(this).runTaskTimer(this, 200L, 6000L);
    }

    @Override
    public void onDisable() {
        if (webServer != null) {
            webServer.stop();
        }
    }

    private void loadConfig() {
        // Sets default values and saves config.yml if missing
        getConfig().addDefault("prefix", "&6[EloPractice] ");
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    // These "Getters" fix your undefined method errors!
    public UserManager getUserManager() { return userManager; }
    public MatchManager getMatchManager() { return matchManager; }
    public QueueManager getQueueManager() { return queueManager; }
    public KitEditorManager getKitEditorManager() { return kitEditorManager; }
    public ArenaManager getArenaManager() { return arenaManager; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
}