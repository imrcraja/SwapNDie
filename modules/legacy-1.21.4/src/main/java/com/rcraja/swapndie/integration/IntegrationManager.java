package com.rcraja.swapndie.integration;

import com.rcraja.swapndie.SwapNDie;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * SwapNDie does not bundle guns/vehicles/bosses itself — it does not need to.
 * If the server owner installs a dedicated plugin for that feature
 * (WeaponMechanics for guns, TurboVehicles/InfiniteVehicles for cars,
 * MythicMobs for bosses, ItemsAdder/Oraxen for custom textured items,
 * Ghostblocks for ghost blocks), SwapNDie hooks into them by dispatching
 * their own commands as configured in config.yml, so trap-builders can
 * hand out that content as part of their trap without SwapNDie having to
 * reimplement it (and without touching anyone else's copyrighted code/assets).
 */
public class IntegrationManager {

    private final SwapNDie plugin;

    public IntegrationManager(SwapNDie plugin) {
        this.plugin = plugin;
    }

    public boolean isInstalled(String pluginName) {
        return plugin.getServer().getPluginManager().getPlugin(pluginName) != null;
    }

    private boolean enabled(String path) {
        return plugin.getConfig().getBoolean("integrations." + path + ".enabled", false);
    }

    private void dispatch(String template, Player player, String arg, Location loc) {
        String cmd = template
                .replace("%player%", player.getName())
                .replace("%arg%", arg == null ? "" : arg)
                .replace("%world%", loc.getWorld().getName())
                .replace("%x%", String.valueOf(loc.getBlockX()))
                .replace("%y%", String.valueOf(loc.getBlockY()))
                .replace("%z%", String.valueOf(loc.getBlockZ()));
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd);
    }

    public boolean giveGun(Player player, String gunId) {
        if (!enabled("weaponmechanics") || !isInstalled("WeaponMechanics")) return false;
        dispatch(plugin.getConfig().getString("integrations.weaponmechanics.give-command"), player, gunId, player.getLocation());
        return true;
    }

    public boolean giveVehicle(Player player, String vehicleId) {
        boolean turbo = isInstalled("TurboVehicles");
        boolean infinite = isInstalled("InfiniteVehicles");
        if (!enabled("vehicles") || !(turbo || infinite)) return false;
        dispatch(plugin.getConfig().getString("integrations.vehicles.give-command"), player, vehicleId, player.getLocation());
        return true;
    }

    public boolean spawnBoss(Player nearPlayer, String bossId) {
        if (!enabled("mythicmobs") || !isInstalled("MythicMobs")) return false;
        dispatch(plugin.getConfig().getString("integrations.mythicmobs.spawn-command"), nearPlayer, bossId, nearPlayer.getLocation());
        return true;
    }

    public boolean giveCustomItem(Player player, String itemId) {
        boolean ia = isInstalled("ItemsAdder");
        boolean ox = isInstalled("Oraxen");
        if (!enabled("itemsadder") || !(ia || ox)) return false;
        dispatch(plugin.getConfig().getString("integrations.itemsadder.give-command"), player, itemId, player.getLocation());
        return true;
    }

    public boolean giveGhostBlock(Player player) {
        if (!enabled("ghostblocks") || !isInstalled("Ghostblocks")) return false;
        dispatch(plugin.getConfig().getString("integrations.ghostblocks.give-command"), player, null, player.getLocation());
        return true;
    }
}

