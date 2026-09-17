package com.rcraja.swapndie.game;

import org.bukkit.Location;

import java.util.UUID;

public class Arena {

    private final String name;
    private final UUID creator;
    private final Location trapOrigin;

    private Location spawnPoint;   // where the swapped-in player lands (set via Spawn Setter item)
    private Location winPoint;     // where the swapped-in player must reach to win (set via Victory Trigger item)

    private ArenaState state = ArenaState.BUILDING;

    private UUID opponent;         // player who will be swapped into this trap
    private int lives;
    private org.bukkit.scheduler.BukkitTask timerTask;

    public Arena(String name, UUID creator, Location trapOrigin) {
        this.name = name;
        this.creator = creator;
        this.trapOrigin = trapOrigin;
    }

    public String getName() { return name; }
    public UUID getCreator() { return creator; }
    public Location getTrapOrigin() { return trapOrigin; }

    public Location getSpawnPoint() { return spawnPoint; }
    public void setSpawnPoint(Location spawnPoint) { this.spawnPoint = spawnPoint; }

    public Location getWinPoint() { return winPoint; }
    public void setWinPoint(Location winPoint) { this.winPoint = winPoint; }

    public ArenaState getState() { return state; }
    public void setState(ArenaState state) { this.state = state; }

    public UUID getOpponent() { return opponent; }
    public void setOpponent(UUID opponent) { this.opponent = opponent; }

    public int getLives() { return lives; }
    public void setLives(int lives) { this.lives = lives; }
    public void decrementLife() { this.lives--; }

    public org.bukkit.scheduler.BukkitTask getTimerTask() { return timerTask; }
    public void setTimerTask(org.bukkit.scheduler.BukkitTask timerTask) { this.timerTask = timerTask; }

    public boolean isReadyToArm() {
        return state == ArenaState.BUILDING && spawnPoint != null && winPoint != null;
    }
}
