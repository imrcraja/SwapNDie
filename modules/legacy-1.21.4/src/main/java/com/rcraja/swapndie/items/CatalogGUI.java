package com.rcraja.swapndie.items;

import com.rcraja.swapndie.SwapNDie;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Minecraft's Creative-mode tabs are hardcoded client-side — a plugin or resource
 * pack can't inject a real new tab there without a Forge/Fabric mod. This GUI is
 * the practical, fully server-side substitute: every SwapNDie item lives here,
 * one click away, no other plugin required.
 */
public class CatalogGUI implements Listener {

    private static final String TITLE = "SwapNDie Catalog";

    private final SwapNDie plugin;

    public CatalogGUI(SwapNDie plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, MiniMessage.miniMessage().deserialize("<red><bold>" + TITLE));

        inv.setItem(10, plugin.getItemManager().createSpawnSetter());
        inv.setItem(11, plugin.getItemManager().createWinTrigger());
        inv.setItem(12, plugin.getItemManager().createGun());
        inv.setItem(13, plugin.getItemManager().createCarSpawner());
        inv.setItem(14, plugin.getItemManager().createBossSpawner());
        inv.setItem(15, plugin.getItemManager().createGhostBlock());

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getView().title() == null) return;
        String plainTitle = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(event.getView().title());
        if (!plainTitle.equals(TITLE)) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || plugin.getItemManager().identify(clicked) == null) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        player.getInventory().addItem(clicked.clone());
        player.sendMessage(plugin.msg("prefix") + "§aAdded to your inventory.");
    }
}

