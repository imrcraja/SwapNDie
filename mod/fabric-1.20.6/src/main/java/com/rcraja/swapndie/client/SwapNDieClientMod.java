package com.rcraja.swapndie.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * Client-side only. Minecraft's Creative-mode tabs are hardcoded and can only be
 * extended by an actual mod — this registers a real new tab: "SwapNDie".
 *
 * Every stack here carries the exact NBT ("PublicBukkitValues" under the
 * minecraft:custom_data component) that Paper's PersistentDataContainer writes,
 * under the SAME NamespacedKey the SwapNDie-1.20.6.jar plugin uses
 * (NamespacedKey(plugin, "swapndie_item") -> "swapndie-1.20.6:swapndie_item").
 * So when a player in Creative clicks one of these, vanilla's own protocol
 * sends the full item — tag included — to the server, and the plugin's
 * ItemManager#identify() recognizes it exactly like an item it handed out
 * itself. No server-side mod, no extra plugin.
 *
 * NOTE: this NBT layout ("PublicBukkitValues") is an internal Paper/Spigot
 * implementation detail, not official public API — if a future Paper version
 * changes how it stores PersistentDataContainer values, this file is the one
 * place to update the tag-building code below.
 */
public class SwapNDieClientMod implements ClientModInitializer {

    // Must match this version's paired plugin jar's Bukkit plugin `name:` (lowercased) in plugin.yml
    private static final String PLUGIN_NAMESPACE = "swapndie-1.20.6";
    private static final String NBT_KEY = "swapndie_item";

    public static final ItemGroup SWAPNDIE_GROUP = Registry.register(
            Registries.ITEM_GROUP,
            Identifier.of("swapndie", "catalog"),
            FabricItemGroup.builder()
                    .icon(() -> tagged(Items.CROSSBOW, "<Blaster>", "gun", List.of()))
                    .displayName(Text.literal("SwapNDie"))
                    .entries((displayContext, entries) -> {
                        entries.add(tagged(Items.RED_BED, "Trap Spawn Setter", "spawn_setter",
                                List.of("Right-click a block to set", "where the opponent lands.")));
                        entries.add(tagged(Items.EMERALD_BLOCK, "Victory Trigger", "win_trigger",
                                List.of("Right-click a block to set", "the win point of the trap.")));
                        entries.add(tagged(Items.CROSSBOW, "SwapNDie Blaster", "gun",
                                List.of("Right-click to fire.", "Damages whoever you're aiming at.")));
                        entries.add(tagged(Items.SADDLE, "SwapNDie Car", "car_spawner",
                                List.of("Right-click the ground to", "spawn a rideable car.")));
                        entries.add(tagged(Items.SPAWNER, "Trap Guardian Spawner", "boss_spawner",
                                List.of("Right-click the ground to", "summon a boosted guard mob.")));
                        entries.add(tagged(Items.GLASS, "Ghost Block Placer", "ghost_block",
                                List.of("Right-click a block to make it", "LOOK solid to the next victim.")));
                    })
                    .build());

    @Override
    public void onInitializeClient() {
        // Registration above runs on class load; nothing else needed client-side.
    }

    private static ItemStack tagged(Item base, String name, String tagValue, List<String> lore) {
        ItemStack stack = new ItemStack(base);
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name));
        if (!lore.isEmpty()) {
            stack.set(DataComponentTypes.LORE, new LoreComponent(lore.stream().map(Text::literal).toList()));
        }

        NbtCompound publicBukkitValues = new NbtCompound();
        publicBukkitValues.putString(PLUGIN_NAMESPACE + ":" + NBT_KEY, tagValue);
        NbtCompound customData = new NbtCompound();
        customData.put("PublicBukkitValues", publicBukkitValues);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(customData));

        return stack;
    }
}

