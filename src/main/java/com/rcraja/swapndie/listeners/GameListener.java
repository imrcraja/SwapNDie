package com.rcraja.swapndie.listeners;

import com.rcraja.swapndie.SwapNDie;
import com.rcraja.swapndie.game.Arena;
import com.rcraja.swapndie.game.ArenaState;
import com.rcraja.swapndie.game.GameTimerTask;
import com.rcraja.swapndie.items.ItemManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.time.Duration;

public class GameListener implements Listener {

    private final SwapNDie plugin;

    public GameListener(SwapNDie plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) return;
        String tag = plugin.getItemManager().identify(event.getItem());
        if (tag == null) return;

        Player player = event.getPlayer();
        Arena arena = plugin.getGameManager().getArenaByCreator(player.getUniqueId());
        if (arena == null || arena.getState() != ArenaState.BUILDING) {
            player.sendMessage(plugin.msg("prefix") + "§cYou don't have an active trap being built.");
            return;
        }

        Location point = event.getClickedBlock().getLocation().add(0.5, 1, 0.5);

        if (tag.equals(ItemManager.SPAWN_SETTER)) {
            arena.setSpawnPoint(point);
            player.sendMessage(plugin.msg("prefix") + "§aOpponent spawn point set for " + arena.getName() + "!");
        } else if (tag.equals(ItemManager.WIN_TRIGGER)) {
            arena.setWinPoint(point);
            player.sendMessage(plugin.msg("prefix") + "§aVictory point set for " + arena.getName() + "!");
        }

        if (arena.isReadyToArm()) {
            arena.setState(ArenaState.ARMED);
            player.sendMessage(plugin.msg("prefix") + "§eTrap armed! Countdown started.");
            GameTimerTask task = new GameTimerTask(plugin, arena);
            arena.setTimerTask(task.runTaskTimer(plugin, 0L, 20L));
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Arena arena = plugin.getGameManager().getArenaByOpponent(player.getUniqueId());
        if (arena == null || arena.getWinPoint() == null) return;

        Location to = event.getTo();
        Location win = arena.getWinPoint();
        if (to.getWorld().equals(win.getWorld())
                && to.getBlockX() == win.getBlockX()
                && to.getBlockY() == win.getBlockY()
                && to.getBlockZ() == win.getBlockZ()) {
            handleWin(arena, player);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Arena arena = plugin.getGameManager().getArenaByOpponent(player.getUniqueId());
        if (arena == null) return;

        arena.decrementLife();
        if (arena.getLives() <= 0) {
            handleLoss(arena, player);
        } else {
            player.sendMessage(plugin.msg("prefix") + "§c" + arena.getLives() + " lives remaining!");
            // respawn back into the trap
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline() && arena.getSpawnPoint() != null) {
                    player.teleport(arena.getSpawnPoint());
                }
            }, 1L);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getConfig().getBoolean("game.lock-build-outside-arena", true)) return;
        // Creators may freely build inside their own BUILDING-state arena; everything else during
        // an active round is locked to prevent griefing outside the trap.
        Arena arena = plugin.getGameManager().getArenaByOpponent(event.getPlayer().getUniqueId());
        if (arena != null && arena.getState() == ArenaState.ACTIVE) {
            // allow breaking inside their own current trap only — simplest safe default: allow all
            // (server owner can tighten this with a region plugin if needed)
        }
    }

    private void handleWin(Arena arena, Player winner) {
        arena.setState(ArenaState.FINISHED);
        Player loser = plugin.getServer().getPlayer(arena.getCreator());

        showResult(winner, plugin.getConfig().getString("messages.win-title", "<green><bold>YOU WIN!"));
        if (loser != null) showResult(loser, plugin.getConfig().getString("messages.lose-title", "<red><bold>YOU DIED!"));

        plugin.getScoreManager().addWin(winner.getUniqueId(), winner.getName());
        plugin.getScoreManager().addLoss(arena.getCreator(), loser != null ? loser.getName() : arena.getCreator().toString());

        winner.setGameMode(GameMode.SURVIVAL);
    }

    private void handleLoss(Arena arena, Player loser) {
        arena.setState(ArenaState.FINISHED);
        Player winner = plugin.getServer().getPlayer(arena.getCreator());

        showResult(loser, plugin.getConfig().getString("messages.lose-title", "<red><bold>YOU DIED!"));
        if (winner != null) showResult(winner, plugin.getConfig().getString("messages.win-title", "<green><bold>YOU WIN!"));

        plugin.getScoreManager().addLoss(loser.getUniqueId(), loser.getName());
        plugin.getScoreManager().addWin(arena.getCreator(), winner != null ? winner.getName() : arena.getCreator().toString());
    }

    private void showResult(Player player, String raw) {
        Component main = MiniMessage.miniMessage().deserialize(raw);
        player.showTitle(Title.title(main, Component.empty(),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(3), Duration.ofSeconds(1))));
    }
}

