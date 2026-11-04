package id.mawismp.duelsaddon.placeholder;

import id.mawismp.duelsaddon.DuelsAddonExtension;
import id.mawismp.duelsaddon.rank.RankTier;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DuelsExpansion extends PlaceholderExpansion {

    private final DuelsAddonExtension extension;

    public DuelsExpansion(DuelsAddonExtension extension) {
        this.extension = extension;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "duelsaddon";
    }

    @Override
    public @NotNull String getAuthor() {
        return "MawiSMP";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        int elo = extension.getRankManager().getPlayerElo(player);
        RankTier currentRank = extension.getRankManager().getRankForElo(elo);

        switch (params.toLowerCase()) {
            case "rank":
                return currentRank != null ? currentRank.getId() : "Unranked";

            case "rank_formatted":
            case "rank_display":
                return currentRank != null ? ChatColor.translateAlternateColorCodes('&', currentRank.getDisplayName()) : "&7Unranked";

            case "elo":
                return String.valueOf(elo);

            case "next_rank":
                RankTier nextRank = extension.getRankManager().getNextRank(currentRank);
                return nextRank != null ? ChatColor.translateAlternateColorCodes('&', nextRank.getDisplayName()) : "&aMAX TIER";

            case "elo_needed":
                RankTier next = extension.getRankManager().getNextRank(currentRank);
                if (next == null) return "0";
                int needed = next.getMinElo() - elo;
                return String.valueOf(Math.max(0, needed));

            default:
                return null;
        }
    }
}
