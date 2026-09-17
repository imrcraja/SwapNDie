package com.rcraja.swapndie.items;

import com.rcraja.swapndie.SwapNDie;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ItemManager {

    public static final String SPAWN_SETTER = "spawn_setter";
    public static final String WIN_TRIGGER = "win_trigger";

    private final SwapNDie plugin;
    private final NamespacedKey key;

    public ItemManager(SwapNDie plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "swapndie_item");
    }

    public ItemStack createSpawnSetter() {
        return build(Material.RED_BED,
                plugin.getConfig().getString("items.spawn-setter-name", "<red>Trap Spawn Setter"),
                SPAWN_SETTER);
    }

    public ItemStack createWinTrigger() {
        return build(Material.EMERALD_BLOCK,
                plugin.getConfig().getString("items.win-trigger-name", "<green>Victory Trigger"),
                WIN_TRIGGER);
    }

    private ItemStack build(Material material, String miniMessageName, String tag) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(MiniMessage.miniMessage().deserialize(miniMessageName));
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, tag);
        item.setItemMeta(meta);
        return item;
    }

    /** Returns SPAWN_SETTER, WIN_TRIGGER, or null if the item isn't a SwapNDie custom item. */
    public String identify(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }
}

