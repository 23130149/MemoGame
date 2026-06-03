package memorygame.model;
public class DifficultyLevel {

    public enum Level {
        EASY("Dễ", 4, 4, 8, 120, 1.0f, 2,
                "Lưới 4×4 – 8 cặp thẻ. Phù hợp cho người mới bắt đầu."),
        MEDIUM("Trung bình", 6, 6, 18, 90, 1.5f, 3,
                "Lưới 6×6 – 18 cặp thẻ. Thử thách vừa phải."),
        HARD("Khó", 8, 8, 32, 60, 2.0f, 5,
                "Lưới 8×8 – 32 cặp thẻ. Dành cho người chơi kinh nghiệm.");

        private final String displayName;
        private final int gridRows;
        private final int gridCols;
        private final int totalPairs;
        private final int timeLimitSec;
        private final float scoreMultiplier;
        private final int hintCount;
        private final String description;

        Level(String displayName, int gridRows, int gridCols, int totalPairs,
              int timeLimitSec, float scoreMultiplier, int hintCount, String description) {
            this.displayName = displayName;
            this.gridRows = gridRows;
            this.gridCols = gridCols;
            this.totalPairs = totalPairs;
            this.timeLimitSec = timeLimitSec;
            this.scoreMultiplier = scoreMultiplier;
            this.hintCount = hintCount;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public int getGridRows() { return gridRows; }
        public int getGridCols() { return gridCols; }
        public int getTotalPairs() { return totalPairs; }
        public int getTimeLimitSec() { return timeLimitSec; }
        public float getScoreMultiplier() { return scoreMultiplier; }
        public int getHintCount() { return hintCount; }
        public String getDescription() { return description; }
    }

    private final int levelId;
    private final String levelName;
    private final int gridRows;
    private final int gridCols;
    private final int totalPairs;
    private final int timeLimitSec;
    private final float scoreMultiplier;
    private final int hintCount;
    private final String description;
    private final boolean isActive;

    public DifficultyLevel(Level level) {
        this.levelId = level.ordinal() + 1;
        this.levelName = level.getDisplayName();
        this.gridRows = level.getGridRows();
        this.gridCols = level.getGridCols();
        this.totalPairs = level.getTotalPairs();
        this.timeLimitSec = level.getTimeLimitSec();
        this.scoreMultiplier = level.getScoreMultiplier();
        this.hintCount = level.getHintCount();
        this.description = level.getDescription();
        this.isActive = true;
    }

    public int getLevelId() { return levelId; }
    public String getLevelName() { return levelName; }
    public int getGridRows() { return gridRows; }
    public int getGridCols() { return gridCols; }
    public int getTotalPairs() { return totalPairs; }
    public int getTimeLimitSec() { return timeLimitSec; }
    public float getScoreMultiplier() { return scoreMultiplier; }
    public int getHintCount() { return hintCount; }
    public String getDescription() { return description; }
    public boolean isActive() { return isActive; }

    public String getLevelInfo() {
        return String.format(
                "%s | %d×%d | %d cặp | %ds | x%.1f điểm | %d gợi ý",
                levelName, gridRows, gridCols, totalPairs, timeLimitSec, scoreMultiplier, hintCount
        );
    }

    public boolean validate() {
        return levelName != null && !levelName.isEmpty()
                && gridRows > 0 && gridCols > 0
                && totalPairs > 0
                && timeLimitSec > 0
                && scoreMultiplier > 0
                && hintCount > 0
                && isActive;
    }

    @Override
    public String toString() { return levelName; }
}