package com.rcraja.swapndie.items;

import com.rcraja.swapndie.SwapNDie;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/**
 * Every custom item SwapNDie hands out — all native, no third-party plugin needed.
 * Each item is tagged via PersistentDataContainer so listeners/GameListener and
 * items/CatalogGUI can recognize it regardless of display name/lore.
 */
public class ItemManager {

    public static final String SPAWN_SETTER = "spawn_setter";
    public static final String WIN_TRIGGER = "win_trigger";
    public static final String GUN = "gun";
    public static final String CAR_SPAWNER = "car_spawner";
    public static final String BOSS_SPAWNER = "boss_spawner";
    public static final String GHOST_BLOCK = "ghost_block";

    private final SwapNDie plugin;
    private final NamespacedKey key;

    public ItemManager(SwapNDie plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "swapndie_item");
    }

    public ItemStack createSpawnSetter() {
        return build(Material.RED_BED,
                plugin.getConfig().getString("items.spawn-setter-name", "<red>Trap Spawn Setter"),
                List.of("<gray>Right-click a block to set", "<gray>where the opponent lands."),
                SPAWN_SETTER);
    }

    public ItemStack createWinTrigger() {
        return build(Material.EMERALD_BLOCK,
                plugin.getConfig().getString("items.win-trigger-name", "<green>Victory Trigger"),
                List.of("<gray>Right-click a block to set", "<gray>the win point of the trap."),
                WIN_TRIGGER);
    }

    public ItemStack createGun() {
        return build(Material.CROSSBOW,
                "<dark_red><bold>SwapNDie Blaster",
                List.of("<gray>Right-click to fire.", "<gray>Damages whoever you're aiming at.", "<yellow>Ammo: 32 shots"),
                GUN);
    }

    public ItemStack createCarSpawner() {
        return build(Material.SADDLE,
                "<gold><bold>SwapNDie Car",
                List.of("<gray>Right-click the ground to", "<gray>spawn a rideable car."),
                CAR_SPAWNER);
    }

    public ItemStack createBossSpawner() {
        return build(Material.SPAWNER,
                "<dark_purple><bold>Trap Guardian Spawner",
                List.of("<gray>Right-click the ground to", "<gray>summon a boosted guard mob."),
                BOSS_SPAWNER);
    }

    public ItemStack createGhostBlock() {
        return build(Material.GLASS,
                "<aqua><bold>Ghost Block Placer",
                List.of("<gray>Right-click a block to make it", "<gray>LOOK solid to the next victim —", "<gray>the real block stays walkable."),
                GHOST_BLOCK);
    }

    private ItemStack build(Material material, String miniMessageName, List<String> loreLines, String tag) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MiniMessage.miniMessage().deserialize(miniMessageName));
        meta.lore(loreLines.stream().map(MiniMessage.miniMessage()::deserialize).toList());
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, tag);
        item.setItemMeta(meta);
        return item;
    }

    /** Returns one of the tag constants above, or null if the item isn't a SwapNDie custom item. */
    public String identify(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }

    public NamespacedKey getKey() {
        return key;
    }
}

