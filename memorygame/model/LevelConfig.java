package memorygame.model;

public enum LevelConfig {

    DE("Dễ", 4, 4, 120, 1.0),
    TRUNG_BINH("Trung bình", 6, 6, 90, 1.5),
    KHO("Khó", 8, 8, 60, 2.0);

    private final String displayName;
    private final int rows;
    private final int cols;
    private final int timeSeconds;
    private final double scoreMultiplier;

    LevelConfig(String displayName, int rows, int cols, int timeSeconds, double scoreMultiplier) {
        this.displayName = displayName;
        this.rows = rows;
        this.cols = cols;
        this.timeSeconds = timeSeconds;
        this.scoreMultiplier = scoreMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getTimeSeconds() {
        return timeSeconds;
    }

    public double getScoreMultiplier() {
        return scoreMultiplier;
    }

    public int getTotalCards() {
        return rows * cols;
    }

    public int getTotalPairs() {
        return getTotalCards() / 2;
    }
}