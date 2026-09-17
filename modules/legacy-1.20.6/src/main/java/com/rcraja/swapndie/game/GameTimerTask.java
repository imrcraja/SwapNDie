package com.rcraja.swapndie.game;

import com.rcraja.swapndie.SwapNDie;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;

public class GameTimerTask extends BukkitRunnable {

    private final SwapNDie plugin;
    private final Arena arena;
    private int secondsLeft;

    public GameTimerTask(SwapNDie plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
        this.secondsLeft = plugin.getConfig().getInt("game.build-timer-seconds", 300);
    }

    @Override
    public void run() {
        if (secondsLeft <= 10 && secondsLeft > 0) {
            String raw = plugin.getConfig().getString("messages.countdown-title", "<red><bold>%time%")
                    .replace("%time%", String.valueOf(secondsLeft));
            var title = Title.title(
                    MiniMessage.miniMessage().deserialize(raw),
                    net.kyori.adventure.text.Component.empty(),
                    Title.Times.times(Duration.ZERO, Duration.ofMillis(900), Duration.ZERO));

            Player creator = plugin.getServer().getPlayer(arena.getCreator());
            Player opponent = plugin.getServer().getPlayer(arena.getOpponent());
            if (creator != null) creator.showTitle(title);
            if (opponent != null) opponent.showTitle(title);
        }

        if (secondsLeft <= 0) {
            swapIn();
            this.cancel();
            return;
        }

        secondsLeft--;
    }

    private void swapIn() {
        Player opponent = plugin.getServer().getPlayer(arena.getOpponent());
        if (opponent == null || arena.getSpawnPoint() == null) {
            // opponent offline or trap incomplete — abort this round quietly
            arena.setState(ArenaState.FINISHED);
            return;
        }

        opponent.teleport(arena.getSpawnPoint());
        opponent.setGameMode(GameMode.SURVIVAL);
        arena.setLives(plugin.getConfig().getInt("game.lives", 3));
        arena.setState(ArenaState.ACTIVE);

        Player creator = plugin.getServer().getPlayer(arena.getCreator());
        if (creator != null) {
            creator.sendMessage(plugin.msg("prefix") +
                    "§eYour opponent has been swapped into your trap! Good luck watching them suffer.");
        }
        opponent.sendMessage(plugin.msg("prefix") + "§cYou've been swapped into a trap! Find the Victory Trigger to escape.");
    }
}

