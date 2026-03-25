# MCELO

A high-performance, feature-rich Minecraft Practice plugin designed for 1.21.1 competitive play. This plugin provides a complete ranked environment with Elo ratings, personalized kit layouts, and automated arena management.

## 🚀 Features

*   **Ranked Matchmaking:** Queue for various kits with a standard Elo rating system (K-Factor: 32).
*   **Personalized Kit Editor:** Players can customize their inventory layouts for every kit, which are saved to a persistent database.
*   **Dynamic Arenas:** Uses WorldEdit/FAWE to paste schematics for matches and automatically rolls back changes (blocks placed/broken) once the match ends.
*   **Comprehensive Kit Roster:** Supports Vanilla, UHC, Pot, NethOP, Mace, SMP, Sword, Axe, Lifesteal, Spear-mace, and CartPvP.
*   **Built-in Web Server:** Automatically exports a `leaderboard.json` and hosts a mini web server (Port 8081) to display rankings on a website.
*   **SQLite Persistence:** All player stats (wins, losses, elo) and custom kit layouts are saved via SQLite.
*   **PlaceholderAPI Support:** Use `%elopractice_elo_<kit>%` to display ratings in tab, scoreboards, or chat.

## 🛠 Commands

### Player Commands (`/ranked`)
| Command | Description |
| :--- | :--- |
| `/ranked join <kit>` | Join the queue for a specific kit. |
| `/ranked editkit <kit>` | Enter edit mode to customize a kit layout. |
| `/ranked savekit` | Save your current inventory as your personal layout. |
| `/ranked stats` | View your wins, losses, and kit ratings. |

### Admin Commands (`/elo-admin`)
*Permission: `elopractice.admin`*
| Command | Description |
| :--- | :--- |
| `/elo-admin verify <player>` | Give a player the verified status (visible on web leaderboard). |
| `/elo-admin unverify <player>` | Remove verified status. |
| `/elo-admin reset <player>` | Completely wipe a player's data from the database. |

## 📂 Installation

1.  Ensure you have **WorldEdit** (or FastAsyncWorldEdit) and **PlaceholderAPI** installed.
2.  Place `EloPractice.jar` in your `plugins` folder.
3.  Create an `arenas` folder inside `plugins/EloPractice/`.
4.  Create subfolders for each kit (e.g., `plugins/EloPractice/arenas/vanilla/`) and place `.schem` or `.schematic` files inside them.
    *   *Note: Use Red Concrete for Player 1 spawn and Blue Concrete for Player 2 spawn inside your schematics.*
5.  Restart the server.

## 🌐 Web Leaderboard

The plugin starts a web server on port `8081` by default. 
*   Access the raw data at: `http://your-ip:8081/leaderboard.json`
*   Place an `index.html` in the `plugins/EloPractice/` folder to serve a custom frontend.

## 💻 Developer Setup

### Dependencies
*   Spigot/Paper API 1.21.1
*   WorldEdit/FastAsyncWorldEdit API
*   PlaceholderAPI
*   SQLite JDBC

### Building
This project is designed to be built with Maven or Gradle. Ensure you include the WorldEdit and PlaceholderAPI repositories in your build configuration.

## 📝 Configuration

The plugin currently uses a hardcoded database path at `/home/obroni/MCELO/plugins/EloPractice/database.db` with an automatic fallback to the local plugin folder if that path is inaccessible.

```yaml
# config.yml
prefix: "&6[EloPractice] "
```

---
*Developed for competitive Minecraft communities.*
