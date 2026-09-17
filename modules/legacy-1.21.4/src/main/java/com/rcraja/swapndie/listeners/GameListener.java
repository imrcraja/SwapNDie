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
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.util.RayTraceResult;

import java.time.Duration;
import java.util.List;

public class GameListener implements Listener {

    private final SwapNDie plugin;

    public GameListener(SwapNDie plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        String tag = plugin.getItemManager().identify(event.getItem());
        if (tag == null) return;

        Player player = event.getPlayer();

        // Trap-building tools: need a right-clicked block + an owned arena being built
        if (tag.equals(ItemManager.SPAWN_SETTER) || tag.equals(ItemManager.WIN_TRIGGER)) {
            if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) return;
            handleTrapPointSetter(player, tag, event.getClickedBlock().getLocation());
            event.setCancelled(true);
            return;
        }

        // Everything below fires on right-click, block or air
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;

        switch (tag) {
            case ItemManager.GUN -> handleGun(player, event.getItem());
            case ItemManager.CAR_SPAWNER -> handleCarSpawner(player);
            case ItemManager.BOSS_SPAWNER -> handleBossSpawner(player);
            case ItemManager.GHOST_BLOCK -> handleGhostBlock(player, event);
            default -> { return; }
        }
        event.setCancelled(true);
    }

    private void handleTrapPointSetter(Player player, String tag, Location clickedBlockLoc) {
        Arena arena = plugin.getGameManager().getArenaByCreator(player.getUniqueId());
        if (arena == null || arena.getState() != ArenaState.BUILDING) {
            player.sendMessage(plugin.msg("prefix") + "§cYou don't have an active trap being built.");
            return;
        }

        Location point = clickedBlockLoc.add(0.5, 1, 0.5);

        if (tag.equals(ItemManager.SPAWN_SETTER)) {
            arena.setSpawnPoint(point);
            player.sendMessage(plugin.msg("prefix") + "§aOpponent spawn point set for " + arena.getName() + "!");
        } else {
            arena.setWinPoint(point);
            player.sendMessage(plugin.msg("prefix") + "§aVictory point set for " + arena.getName() + "!");
        }

        if (arena.isReadyToArm()) {
            arena.setState(ArenaState.ARMED);
            player.sendMessage(plugin.msg("prefix") + "§eTrap armed! Countdown started.");
            GameTimerTask task = new GameTimerTask(plugin, arena);
            arena.setTimerTask(task.runTaskTimer(plugin, 0L, 20L));
        }
    }

    /** Native gun: ray-traces from the player's eyes, damages whatever living entity it hits, consumes 1 durability point as ammo. */
    private void handleGun(Player player, ItemStack gunItem) {
        double range = plugin.getConfig().getDouble("weapons.gun-range", 40.0);
        double damage = plugin.getConfig().getDouble("weapons.gun-damage", 6.0);

        RayTraceResult result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(), player.getEyeLocation().getDirection(), range,
                entity -> entity instanceof LivingEntity && !entity.equals(player));

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT_MULTI, 1f, 1.4f);
        player.getWorld().spawnParticle(Particle.CRIT, player.getEyeLocation(), 6, 0.1, 0.1, 0.1, 0.2);

        if (result != null && result.getHitEntity() instanceof LivingEntity target) {
            target.damage(damage, player);
        }

        // ammo tracking via item durability so it works without any extra plugin
        if (gunItem.getItemMeta() instanceof Damageable dmg) {
            int newDamage = dmg.getDamage() + 1;
            int maxAmmo = plugin.getConfig().getInt("weapons.gun-max-ammo", 32);
            if (newDamage >= maxAmmo) {
                gunItem.setAmount(0); // out of ammo — item disappears, catalog has more
                player.sendMessage(plugin.msg("prefix") + "§cOut of ammo!");
            } else {
                dmg.setDamage(newDamage);
                gunItem.setItemMeta((org.bukkit.inventory.meta.ItemMeta) dmg);
            }
        }
    }

    /** Native "car": a tamed, saddled Horse — rideable anywhere, no rails/water needed, no vehicle plugin required. */
    private void handleCarSpawner(Player player) {
        Horse horse = (Horse) player.getWorld().spawnEntity(player.getLocation(), EntityType.HORSE);
        horse.customName(MiniMessage.miniMessage().deserialize("<gold><bold>SwapNDie Car"));
        horse.setCustomNameVisible(true);
        horse.setTamed(true);
        horse.setOwner(player);
        horse.setAdult();
        horse.getInventory().setSaddle(new ItemStack(org.bukkit.Material.SADDLE));
        horse.setJumpStrength(1.0);
        player.sendMessage(plugin.msg("prefix") + "§aCar spawned — right-click it to drive.");
    }

    /** Native "boss": a boosted, equipped vanilla mob — no MythicMobs required. */
    private void handleBossSpawner(Player player) {
        LivingEntity boss = (LivingEntity) player.getWorld().spawnEntity(
                player.getLocation().add(player.getLocation().getDirection().multiply(2)), EntityType.VINDICATOR);
        double hp = plugin.getConfig().getDouble("boss.health", 60.0);
        double dmg = plugin.getConfig().getDouble("boss.attack-damage", 10.0);
        boss.customName(MiniMessage.miniMessage().deserialize("<dark_purple><bold>Trap Guardian"));
        boss.setCustomNameVisible(true);
        var maxHealthAttr = boss.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
        if (maxHealthAttr != null) maxHealthAttr.setBaseValue(hp);
        boss.setHealth(hp);
        var atkAttr = boss.getAttribute(org.bukkit.attribute.Attribute.ATTACK_DAMAGE);
        if (atkAttr != null) atkAttr.setBaseValue(dmg);
        player.sendMessage(plugin.msg("prefix") + "§5Trap Guardian summoned!");
    }

    /** Native ghost block: fakes a solid-looking block for ONE player only via a packet-level client update — the real block never changes, so it stays walkable for everyone else. No ProtocolLib needed, Paper exposes this directly. */
    private void handleGhostBlock(Player placer, PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        Location loc = event.getClickedBlock().getLocation();
        Arena arena = plugin.getGameManager().getArenaByCreator(placer.getUniqueId());
        if (arena == null) {
            placer.sendMessage(plugin.msg("prefix") + "§cStart a trap first with /swapndie start.");
            return;
        }
        BlockData illusionData = org.bukkit.Bukkit.createBlockData(org.bukkit.Material.STONE);
        Player victim = plugin.getServer().getPlayer(arena.getOpponent());
        List<Player> targets = victim != null ? List.of(victim) : plugin.getServer().getOnlinePlayers().stream()
                .filter(p -> !p.equals(placer)).toList();
        for (Player p : targets) {
            p.sendBlockChange(loc, illusionData);
        }
        placer.sendMessage(plugin.msg("prefix") + "§bGhost block placed — looks solid to your opponent only.");
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
        Arena arena = plugin.getGameManager().getArenaByOpponent(event.getPlayer().getUniqueId());
        if (arena != null && arena.getState() == ArenaState.ACTIVE) {
            // server owner can tighten this further with a region plugin if needed
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
