package id.mawismp.duelsaddon;

import id.mawismp.duelsaddon.arena.ArenaResetManager;
import id.mawismp.duelsaddon.listener.MatchEndListener;
import id.mawismp.duelsaddon.placeholder.DuelsExpansion;
import id.mawismp.duelsaddon.rank.RankManager;
import me.realized.duels.api.extension.Extension;
import org.bukkit.Bukkit;

import java.io.File;

public class DuelsAddonExtension extends Extension {

    private RankManager rankManager;
    private ArenaResetManager arenaResetManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        createSchematicsFolder();

        this.rankManager = new RankManager(this);
        this.rankManager.loadRanks();

        this.arenaResetManager = new ArenaResetManager(this);

        registerListener(new MatchEndListener(this));

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new DuelsExpansion(this).register();
            getPlugin().getLogger().info("[DuelsAddon] PlaceholderAPI expansion registered (%duelsaddon_*)");
        }

        getPlugin().getLogger().info("[DuelsAddon] Native Duels Extension v1.1.0 Loaded!");
    }

    @Override
    public void onDisable() {
        getPlugin().getLogger().info("[DuelsAddon] Native Duels Extension Disabled!");
    }

    private void createSchematicsFolder() {
        File folder = new File(getDataFolder(), "schematics");
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public RankManager getRankManager() {
        return rankManager;
    }

    public ArenaResetManager getArenaResetManager() {
        return arenaResetManager;
    }
}
