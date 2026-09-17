package com.rcraja.swapndie.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class Msg {

    public static Component mm(String raw) {
        return MiniMessage.miniMessage().deserialize(raw == null ? "" : raw);
    }

    public static String legacy(String raw) {
        return LegacyComponentSerializer.legacySection().serialize(mm(raw));
    }
}
