package me.yourname.eloplugin;

public class EloCalculator {

    private static final int K_FACTOR = 32; // How fast ELO changes

    public static int calculateNewElo(int currentElo, int opponentElo, boolean won) {
        // This is the standard Elo formula used in Chess/League of Legends
        double expectedScore = 1.0 / (1.0 + Math.pow(10.0, (opponentElo - currentElo) / 400.0));
        int actualScore = won ? 1 : 0;
        
        int newElo = (int) (currentElo + K_FACTOR * (actualScore - expectedScore));
        return Math.max(newElo, 100);
    }
}
