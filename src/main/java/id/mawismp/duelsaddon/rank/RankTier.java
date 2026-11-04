package id.mawismp.duelsaddon.rank;

public class RankTier {
    private final String id;
    private final String displayName;
    private final int minElo;
    private final int maxElo;

    public RankTier(String id, String displayName, int minElo, int maxElo) {
        this.id = id;
        this.displayName = displayName;
        this.minElo = minElo;
        this.maxElo = maxElo;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMinElo() {
        return minElo;
    }

    public int getMaxElo() {
        return maxElo;
    }

    public boolean isInRange(int elo) {
        return elo >= minElo && (maxElo == -1 || elo <= maxElo);
    }
}
