package me.yourname.eloplugin;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;

public class WebServer {

    private final Main plugin;
    private HttpServer server;
    private final int port = 8081;

    public WebServer(Main plugin) {
        this.plugin = plugin;
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new FileHandler());
            server.setExecutor(null); // Default executor
            server.start();
            plugin.getLogger().info("Leaderboard Web Server started at http://localhost:" + port + "/");

            // Check if the HTML file exists and warn if missing
            if (!new File(plugin.getDataFolder(), "index.html").exists()) {
                plugin.getLogger().warning("⚠ index.html is missing! Please upload it to: " + plugin.getDataFolder().getAbsolutePath());
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to start web server: " + e.getMessage());
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private class FileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();

            // Redirect root to index.html
            if (path.equals("/")) {
                path = "/index.html";
            }

            File file;
            // Serve the database from the specific path defined in DatabaseManager
            if (path.equals("/database.db")) {
                file = new File("/home/obroni/MCELO/plugins/EloPractice/database.db");
                // Fallback: If hardcoded path doesn't exist (e.g. local testing), check plugin folder
                if (!file.exists()) {
                    plugin.getLogger().warning("Database file missing at hardcoded path: " + file.getAbsolutePath());
                    plugin.getLogger().info("Attempting to serve database.db from local plugin folder instead.");
                    file = new File(plugin.getDataFolder(), "database.db");
                }
            } else {
                // Serve other files (html, css, js) from the plugin folder
                // Security: Basic check to prevent going up directories
                if (path.contains("..")) path = "/index.html";
                file = new File(plugin.getDataFolder(), path);
            }

            if (file.exists() && !file.isDirectory()) {
                byte[] bytes = Files.readAllBytes(file.toPath());
                
                // Set headers
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*"); // Allow external fetch
                exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate"); // Disable caching
                if (path.endsWith(".html")) exchange.getResponseHeaders().set("Content-Type", "text/html");
                else if (path.endsWith(".db")) exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");

                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        }
    }
}