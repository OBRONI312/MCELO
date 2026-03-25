package me.yourname.eloplugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MatchManager {

    private final List<Match> activeMatches = new ArrayList<>();

    public void createMatch(Main plugin, UUID p1, UUID p2, String kit) {
        Match match = new Match(plugin, p1, p2, kit);
        activeMatches.add(match);
        
        if (!match.start()) {
            activeMatches.remove(match);
        }
    }

    public Match getMatch(UUID uuid) {
        for (Match match : activeMatches) {
            if (match.containsPlayer(uuid)) {
                return match;
            }
        }
        return null;
    }

    public void removeMatch(Match match) {
        activeMatches.remove(match);
    }
}