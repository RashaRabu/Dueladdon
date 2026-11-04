package id.mawismp.duelsaddon.rank;

import id.mawismp.duelsaddon.DuelsAddonExtension;
import me.realized.duels.api.user.User;
import me.realized.duels.api.user.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class RankManager {

    private final DuelsAddonExtension extension;
    private final List<RankTier> rankTiers = new ArrayList<>();
    private final Map<UUID, RankTier> cachedPlayerRanks = new HashMap<>();

    public RankManager(DuelsAddonExtension extension) {
        this.extension = extension;
    }

    public void loadRanks() {
        rankTiers.clear();
        ConfigurationSection section = extension.getConfig().getConfigurationSection("ranks");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            String displayName = section.getString(key + ".display-name", key);
            int minElo = section.getInt(key + ".min-elo", 0);
            int maxElo = section.getInt(key + ".max-elo", -1);
            rankTiers.add(new RankTier(key, displayName, minElo, maxElo));
        }

        rankTiers.sort(Comparator.comparingInt(RankTier::getMinElo));
    }

    public RankTier getRankForElo(int elo) {
        RankTier current = rankTiers.isEmpty() ? null : rankTiers.get(0);
        for (RankTier tier : rankTiers) {
            if (tier.isInRange(elo)) {
                return tier;
            }
            if (elo >= tier.getMinElo()) {
                current = tier;
            }
        }
        return current;
    }

    public RankTier getNextRank(RankTier current) {
        if (current == null || rankTiers.isEmpty()) return null;
        int index = rankTiers.indexOf(current);
        if (index >= 0 && index < rankTiers.size() - 1) {
            return rankTiers.get(index + 1);
        }
        return null;
    }

    public int getPlayerElo(Player player) {
        if (extension.getPlugin() != null) {
            UserManager userManager = extension.getPlugin().getUserManager();
            if (userManager != null) {
                User user = userManager.get(player);
                if (user != null) {
                    return user.getElo();
                }
            }
        }
        return 1000;
    }

    public void checkAndAnnounceRankUp(Player player) {
        int elo = getPlayerElo(player);
        RankTier newRank = getRankForElo(elo);
        RankTier oldRank = cachedPlayerRanks.get(player.getUniqueId());

        if (oldRank != null && newRank != null && !oldRank.getId().equals(newRank.getId())) {
            int oldIndex = rankTiers.indexOf(oldRank);
            int newIndex = rankTiers.indexOf(newRank);

            if (newIndex > oldIndex) {
                triggerRankUpAnnouncement(player, oldRank, newRank);
            }
        }
        cachedPlayerRanks.put(player.getUniqueId(), newRank);
    }

    public void updateCache(Player player) {
        int elo = getPlayerElo(player);
        cachedPlayerRanks.put(player.getUniqueId(), getRankForElo(elo));
    }

    private void triggerRankUpAnnouncement(Player player, RankTier oldRank, RankTier newRank) {
        List<String> rawMessages = new ArrayList<>();
        if (extension.getConfig().isList("announcement.broadcast-messages")) {
            rawMessages = extension.getConfig().getStringList("announcement.broadcast-messages");
        } else if (extension.getConfig().isString("announcement.broadcast-messages")) {
            rawMessages.add(extension.getConfig().getString("announcement.broadcast-messages"));
        }

        for (String msg : rawMessages) {
            String formatted = msg.replace("%player%", player.getName())
                    .replace("%old_rank%", ChatColor.translateAlternateColorCodes('&', oldRank.getDisplayName()))
                    .replace("%new_rank%", ChatColor.translateAlternateColorCodes('&', newRank.getDisplayName()));
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', formatted));
        }

        if (extension.getConfig().getBoolean("announcement.title.enabled", true)) {
            String title = extension.getConfig().getString("announcement.title.header", "")
                    .replace("%new_rank%", newRank.getDisplayName());
            String subtitle = extension.getConfig().getString("announcement.title.footer", "")
                    .replace("%player%", player.getName());

            player.sendTitle(
                    ChatColor.translateAlternateColorCodes('&', title),
                    ChatColor.translateAlternateColorCodes('&', subtitle),
                    10, 70, 20
            );
        }

        if (extension.getConfig().getBoolean("announcement.sound.enabled", true)) {
            String soundName = extension.getConfig().getString("announcement.sound.type", "UI_TOAST_CHALLENGE_COMPLETE");
            try {
                Sound sound = Sound.valueOf(soundName);
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException ignored) {}
        }
    }
}
