package me.yourname.eloplugin;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Files;

public class WebServer {

    private final Main plugin;
    private Undertow server;
    private final int port;

    public WebServer(Main plugin, int port) {
        this.plugin = plugin;
        this.port = port;
    }

    public void start() {
        try {
            server = Undertow.builder()
                    .addHttpListener(port, "0.0.0.0")
                    .setHandler(new FileHandler())
                    .build();
            server.start();
            plugin.getLogger().info("Leaderboard Web Server started at http://localhost:" + port + "/");

            // Check if the HTML file exists and warn if missing
            if (!new File(plugin.getDataFolder(), "index.html").exists()) {
                plugin.getLogger().warning(
                        "⚠ index.html is missing! Please upload it to: " + plugin.getDataFolder().getAbsolutePath());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to start web server: " + e.getMessage());
        }
    }

    public void stop() {
        if (server != null) {
            server.stop();
        }
    }

    private class FileHandler implements HttpHandler {
        @Override
        public void handleRequest(HttpServerExchange exchange) throws Exception {
            String path = exchange.getRequestPath();

            // Redirect root to index.html
            if (path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            File file;
            // Security: Prevent access to sensitive files like config.yml
            if (path.endsWith(".yml") || path.contains("..")) {
                exchange.setStatusCode(403);
                exchange.getResponseSender().send("403 Forbidden");
                return;
            }

            file = new File(plugin.getDataFolder(), path);

            if (file.exists() && !file.isDirectory()) {
                byte[] bytes = Files.readAllBytes(file.toPath());

                // Set CORS headers
                exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");
                exchange.getResponseHeaders().put(Headers.CACHE_CONTROL, "no-cache, no-store, must-revalidate");

                // Set content type
                if (path.endsWith(".html")) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html; charset=utf-8");
                } else if (path.endsWith(".json")) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                } else if (path.endsWith(".css")) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/css");
                } else if (path.endsWith(".js")) {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/javascript");
                }

                exchange.setStatusCode(200);
                exchange.getResponseSender().send(ByteBuffer.wrap(bytes));
            } else {
                exchange.setStatusCode(404);
                exchange.getResponseSender().send("404 Not Found");
            }
        }
    }
}