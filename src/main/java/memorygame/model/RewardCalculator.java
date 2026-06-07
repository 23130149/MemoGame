package memorygame.model;

public final class RewardCalculator {

    private static final int POINTS_PER_GOLD = 10;

    private RewardCalculator() {
    }

    public static long calculateGoldFromScore(int score) {
        if (score <= 0) {
            return 0;
        }

        return score / (long) POINTS_PER_GOLD;
    }
}