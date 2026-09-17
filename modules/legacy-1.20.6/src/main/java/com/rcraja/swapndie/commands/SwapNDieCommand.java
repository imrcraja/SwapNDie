package com.rcraja.swapndie.commands;

import com.rcraja.swapndie.SwapNDie;
import com.rcraja.swapndie.game.Arena;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class SwapNDieCommand implements CommandExecutor, TabCompleter {

    private final SwapNDie plugin;

    private static final List<String> SUBCOMMANDS = List.of(
            "start", "tp", "additem", "catalog", "score", "top", "config", "reload", "give", "help");

    public SwapNDieCommand(SwapNDie plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start" -> cmdStart(sender);
            case "tp" -> cmdTp(sender, args);
            case "additem" -> cmdAddItem(sender, args);
            case "catalog" -> cmdCatalog(sender);
            case "score" -> cmdScore(sender, args);
            case "top" -> cmdTop(sender);
            case "config" -> cmdConfig(sender, args);
            case "reload" -> cmdReload(sender);
            case "give" -> cmdGive(sender, args);
            case "help" -> sendHelp(sender);
            default -> sender.sendMessage(plugin.msg("prefix") + "§cUnknown subcommand. Try /swapndie help");
        }
        return true;
    }

    // ---- start ----
    private void cmdStart(CommandSender sender) {
        if (!(sender instanceof Player player)) { sender.sendMessage("§cPlayers only."); return; }
        if (!player.hasPermission("swapndie.play")) { sender.sendMessage("§cNo permission."); return; }

        Arena arena = plugin.getGameManager().createGame(player);
        player.sendMessage(plugin.msg("prefix") + "§aNew trap started: §e" + arena.getName() +
                "§a! You're in Creative — build your trap, then place the two custom items " +
                "(§c/swapndie additem spawn§a and §c/swapndie additem win§a).");
    }

    // ---- tp ----
    private void cmdTp(CommandSender sender, String[] args) {
        if (!sender.hasPermission("swapndie.admin")) { sender.sendMessage("§cNo permission."); return; }
        if (args.length < 3) {
            sender.sendMessage(plugin.msg("prefix") + "§cUsage: /swapndie tp <gameName> <player|all>");
            return;
        }
        Arena arena = plugin.getGameManager().getArena(args[1]);
        if (arena == null) { sender.sendMessage(plugin.msg("prefix") + "§cNo such game: " + args[1]); return; }
        if (arena.getSpawnPoint() == null) {
            sender.sendMessage(plugin.msg("prefix") + "§cThat trap has no spawn point set yet.");
            return;
        }

        List<Player> targets = new ArrayList<>();
        if (args[2].equalsIgnoreCase("all")) {
            targets.addAll(Bukkit.getOnlinePlayers());
        } else {
            for (int i = 2; i < args.length; i++) {
                Player t = Bukkit.getPlayerExact(args[i]);
                if (t != null) targets.add(t);
            }
        }

        for (Player t : targets) {
            t.teleport(arena.getSpawnPoint());
            if (arena.getOpponent() == null || arena.getOpponent().equals(t.getUniqueId())) {
                arena.setOpponent(t.getUniqueId());
            }
        }
        sender.sendMessage(plugin.msg("prefix") + "§aTeleported " + targets.size() + " player(s) to " + arena.getName());
    }

    // ---- additem ----
    private void cmdAddItem(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("§cPlayers only."); return; }
        if (args.length < 2) {
            sender.sendMessage(plugin.msg("prefix") + "§cUsage: /swapndie additem <spawn|win>");
            return;
        }
        switch (args[1].toLowerCase()) {
            case "spawn" -> player.getInventory().addItem(plugin.getItemManager().createSpawnSetter());
            case "win" -> player.getInventory().addItem(plugin.getItemManager().createWinTrigger());
            default -> sender.sendMessage(plugin.msg("prefix") + "§cUnknown item type. Use spawn or win.");
        }
    }

    // ---- catalog (creative-tab substitute — no other plugin needed) ----
    private void cmdCatalog(CommandSender sender) {
        if (!(sender instanceof Player player)) { sender.sendMessage("§cPlayers only."); return; }
        plugin.getCatalogGUI().open(player);
    }

    // ---- score ----
    private void cmdScore(CommandSender sender, String[] args) {
        Player target = args.length >= 2 ? Bukkit.getPlayerExact(args[1])
                : (sender instanceof Player p ? p : null);
        if (target == null) { sender.sendMessage(plugin.msg("prefix") + "§cSpecify a player name."); return; }

        var sm = plugin.getScoreManager();
        sender.sendMessage(plugin.msg("prefix") + "§e" + target.getName() +
                " §7— §aWins: " + sm.getWins(target.getUniqueId()) +
                " §c Losses: " + sm.getLosses(target.getUniqueId()) +
                " §bPoints: " + sm.getPoints(target.getUniqueId()));
    }

    // ---- top ----
    private void cmdTop(CommandSender sender) {
        sender.sendMessage(plugin.msg("prefix") + "§6§lTop Players");
        int i = 1;
        for (var entry : plugin.getScoreManager().getTop(10).entrySet()) {
            sender.sendMessage("§e#" + i + " §f" + entry.getKey() + " §7- §b" + entry.getValue() + " pts");
            i++;
        }
    }

    // ---- config ----
    private void cmdConfig(CommandSender sender, String[] args) {
        if (!sender.hasPermission("swapndie.admin")) { sender.sendMessage("§cNo permission."); return; }
        if (args.length < 3) {
            sender.sendMessage(plugin.msg("prefix") + "§cUsage: /swapndie config <key> <value>");
            sender.sendMessage("§7Keys: arena.flatten-radius, arena.arena-spacing, game.build-timer-seconds, game.lives");
            return;
        }
        String key = args[1];
        String value = args[2];
        Object parsed = value.matches("-?\\d+") ? Integer.parseInt(value) : value;
        plugin.getConfig().set(key, parsed);
        plugin.saveConfig();
        sender.sendMessage(plugin.msg("prefix") + "§aSet §e" + key + " §ato §e" + value);
    }

    private void cmdReload(CommandSender sender) {
        if (!sender.hasPermission("swapndie.admin")) { sender.sendMessage("§cNo permission."); return; }
        plugin.reloadConfig();
        sender.sendMessage(plugin.msg("prefix") + "§aConfig reloaded.");
    }

    // ---- give (integration bridge) ----
    private void cmdGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("swapndie.admin")) { sender.sendMessage("§cNo permission."); return; }
        if (args.length < 3) {
            sender.sendMessage(plugin.msg("prefix") + "§cUsage: /swapndie give <gun|vehicle|boss|item|ghostblock> <player> [id]");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[2]);
        if (target == null) { sender.sendMessage(plugin.msg("prefix") + "§cPlayer not found."); return; }
        String id = args.length >= 4 ? args[3] : "";

        boolean ok = switch (args[1].toLowerCase()) {
            case "gun" -> plugin.getIntegrationManager().giveGun(target, id);
            case "vehicle" -> plugin.getIntegrationManager().giveVehicle(target, id);
            case "boss" -> plugin.getIntegrationManager().spawnBoss(target, id);
            case "item" -> plugin.getIntegrationManager().giveCustomItem(target, id);
            case "ghostblock" -> plugin.getIntegrationManager().giveGhostBlock(target);
            default -> false;
        };

        sender.sendMessage(plugin.msg("prefix") + (ok
                ? "§aDelivered."
                : "§cThat integration isn't enabled/installed (check config.yml -> integrations)."));
    }

    // ---- help ----
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§c§l== SwapNDie Commands ==");
        sender.sendMessage("§e/swapndie start §7- start a new trap (teleports you far away, gives Creative)");
        sender.sendMessage("§e/swapndie additem <spawn|win> §7- get the two trap-setting items");
        sender.sendMessage("§e/swapndie catalog §7- open the full item catalog (guns, car, boss, ghost block, etc.)");
        sender.sendMessage("§e/swapndie tp <game> <player|all> §7- teleport player(s) into a trap");
        sender.sendMessage("§e/swapndie score [player] §7- view win/loss/points");
        sender.sendMessage("§e/swapndie top §7- leaderboard");
        sender.sendMessage("§e/swapndie config <key> <value> §7- customize plugin settings (admin)");
        sender.sendMessage("§e/swapndie give <gun|vehicle|boss|item|ghostblock> <player> [id] §7- hand out integrated content (admin)");
        sender.sendMessage("§e/swapndie reload §7- reload config.yml (admin)");
        sender.sendMessage("§7Plugin by §cRC RAJA GAMER 2.0");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return SUBCOMMANDS.stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("additem")) {
            return List.of("spawn", "win").stream().filter(s -> s.startsWith(args[1].toLowerCase())).toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return List.of("gun", "vehicle", "boss", "item", "ghostblock").stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase())).toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("tp")) {
            return new ArrayList<>(plugin.getGameManager().getArenas().keySet());
        }
        return Collections.emptyList();
    }
    }

