package id.mawismp.duelsaddon.arena;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import id.mawismp.duelsaddon.DuelsAddonExtension;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrowableProjectile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;

public class ArenaResetManager {

    private final DuelsAddonExtension extension;

    public ArenaResetManager(DuelsAddonExtension extension) {
        this.extension = extension;
    }

    public void resetArena(String arenaName, Location pasteLocation, File schematicFile) {
        if (!schematicFile.exists()) {
            extension.getPlugin().getLogger().warning("[DuelsAddon] Schematic file not found: " + schematicFile.getPath());
            return;
        }

        Runnable pasteTask = () -> {
            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            if (format == null) {
                extension.getPlugin().getLogger().warning("[DuelsAddon] Invalid schematic format for: " + schematicFile.getName());
                return;
            }

            try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
                Clipboard clipboard = reader.read();
                World world = BukkitAdapter.adapt(pasteLocation.getWorld());

                try (EditSession editSession = WorldEdit.getInstance().newEditSessionFactory().getEditSession(world, -1)) {
                    Operation operation = new ClipboardHolder(clipboard)
                            .createPaste(editSession)
                            .to(BlockVector3.at(pasteLocation.getX(), pasteLocation.getY(), pasteLocation.getZ()))
                            .ignoreAirBlocks(false)
                            .build();
                    Operations.complete(operation);
                    editSession.flushSession();
                }

                if (extension.getConfig().getBoolean("arena-reset.clear-dropped-items", true)) {
                    int radius = extension.getConfig().getInt("arena-reset.clear-radius", 50);
                    Bukkit.getScheduler().runTask(extension.getPlugin(), () -> clearEntitiesInArea(pasteLocation, radius));
                }

            } catch (IOException e) {
                extension.getPlugin().getLogger().log(Level.SEVERE, "[DuelsAddon] Failed to reset arena: " + arenaName, e);
            }
        };

        boolean isFawePresent = Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit");
        if (isFawePresent) {
            Bukkit.getScheduler().runTaskAsynchronously(extension.getPlugin(), pasteTask);
        } else {
            Bukkit.getScheduler().runTask(extension.getPlugin(), pasteTask);
        }
    }

    private void clearEntitiesInArea(Location loc, int radius) {
        if (loc.getWorld() == null) return;
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (!(entity instanceof Player) && (entity instanceof Item || entity instanceof Arrow || entity instanceof ThrowableProjectile)) {
                entity.remove();
            }
        }
    }
}
