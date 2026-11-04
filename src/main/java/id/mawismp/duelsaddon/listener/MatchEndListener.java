package id.mawismp.duelsaddon.listener;

import id.mawismp.duelsaddon.DuelsAddonExtension;
import me.realized.duels.api.arena.Arena;
import me.realized.duels.api.event.match.MatchEndEvent;
import me.realized.duels.api.match.Match;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;

public class MatchEndListener implements Listener {

    private final DuelsAddonExtension extension;

    public MatchEndListener(DuelsAddonExtension extension) {
        this.extension = extension;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onMatchEnd(MatchEndEvent event) {
        Match match = event.getMatch();
        Arena arena = match.getArena();

        Bukkit.getScheduler().runTaskLater(extension.getPlugin(), () -> {
            for (Player player : match.getPlayers()) {
                if (player != null && player.isOnline()) {
                    extension.getRankManager().checkAndAnnounceRankUp(player);
                }
            }
        }, 10L);

        if (extension.getConfig().getBoolean("arena-reset.enabled", true) && arena != null) {
            String arenaName = arena.getName();
            
            File schematicFile = new File(extension.getDataFolder(), "schematics/" + arenaName + ".schem");
            if (!schematicFile.exists()) {
                schematicFile = new File(extension.getDataFolder(), "schematics/" + arenaName + ".schematic");
            }

            Location pasteLocation = (arena.getPositions() != null && !arena.getPositions().isEmpty())
                    ? arena.getPositions().get(0)
                    : null;

            if (pasteLocation != null && schematicFile.exists()) {
                long delayTicks = extension.getConfig().getLong("arena-reset.reset-delay-ticks", 40L);
                File finalSchematic = schematicFile;

                Bukkit.getScheduler().runTaskLater(extension.getPlugin(), () -> {
                    extension.getArenaResetManager().resetArena(arenaName, pasteLocation, finalSchematic);
                }, delayTicks);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(extension.getPlugin(), () -> {
            extension.getRankManager().updateCache(event.getPlayer());
        }, 20L);
    }
}
