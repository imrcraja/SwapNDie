package com.rcraja.swapndie.game;

import com.rcraja.swapndie.SwapNDie;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class GameManager {

    private final SwapNDie plugin;
    private final Map<String, Arena> arenas = new LinkedHashMap<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    public GameManager(SwapNDie plugin) {
        this.plugin = plugin;
    }

    /** Creates a new game slot (game1, game2, ...), teleports the creator far away, flattens the platform, gives creative. */
    public Arena createGame(Player creator) {
        int id = counter.incrementAndGet();
        String name = "game" + id;

        World world = creator.getWorld();
        int spacing = plugin.getConfig().getInt("arena.arena-spacing", 500);
        // spread arenas out along the X axis so they never overlap
        double x = id * spacing;
        double z = id * spacing;

        double baseY = plugin.getConfig().getInt("arena.platform-y", -1);
        if (baseY < 0) {
            baseY = creator.getLocation().getY();
        }

        Location trapOrigin = new Location(world, x + 0.5, baseY, z + 0.5);

        Arena arena = new Arena(name, creator.getUniqueId(), trapOrigin);
        arenas.put(name, arena);

        flattenArena(trapOrigin);

        creator.teleport(trapOrigin.clone().add(0, 1, 0));
        creator.setGameMode(org.bukkit.GameMode.CREATIVE);

        return arena;
    }

    /** Flattens a radius around the origin into a flat build platform. */
    public void flattenArena(Location origin) {
        int radius = plugin.getConfig().getInt("arena.flatten-radius", 20);
        int clearHeight = plugin.getConfig().getInt("arena.clear-height", 40);
        Material floorMat = Material.matchMaterial(
                plugin.getConfig().getString("arena.platform-material", "SMOOTH_STONE"));
        if (floorMat == null) floorMat = Material.SMOOTH_STONE;

        World world = origin.getWorld();
        int baseX = origin.getBlockX();
        int baseY = origin.getBlockY();
        int baseZ = origin.getBlockZ();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                world.getBlockAt(baseX + x, baseY - 1, baseZ + z).setType(floorMat);
                for (int y = 0; y < clearHeight; y++) {
                    world.getBlockAt(baseX + x, baseY + y, baseZ + z).setType(Material.AIR);
                }
            }
        }
    }

    public Arena getArena(String name) {
        return arenas.get(name);
    }

    public Map<String, Arena> getArenas() {
        return arenas;
    }

    /** Finds the arena a player currently owns as creator (their most recent build). */
    public Arena getArenaByCreator(UUID creatorId) {
        Arena latest = null;
        for (Arena a : arenas.values()) {
            if (a.getCreator().equals(creatorId)) {
                latest = a;
            }
        }
        return latest;
    }

    /** Finds the arena a player is currently the swapped-in victim of (ACTIVE state). */
    public Arena getArenaByOpponent(UUID playerId) {
        for (Arena a : arenas.values()) {
            if (a.getState() == ArenaState.ACTIVE && playerId.equals(a.getOpponent())) {
                return a;
            }
        }
        return null;
    }

    public void removeArena(String name) {
        Arena a = arenas.remove(name);
        if (a != null && a.getTimerTask() != null) {
            a.getTimerTask().cancel();
        }
    }
}
