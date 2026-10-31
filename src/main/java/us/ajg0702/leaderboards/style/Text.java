/*
 * ajLeaderboards (Lumarion fork)
 * Original work Copyright (C) ajgeiss0702 - https://github.com/ajgeiss0702/ajLeaderboards
 * Modifications Copyright (C) 2026 Lumarion fork contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version. See the LICENSE file.
 */
package us.ajg0702.leaderboards.style;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class Text {

    private static final TagResolver THEME = TagResolver.builder()
            .tag("ok", Tag.styling(Palette.SUCCESS))
            .tag("err", Tag.styling(Palette.ERROR))
            .tag("warn", Tag.styling(Palette.WARN))
            .tag("info", Tag.styling(Palette.INFO))
            .tag("acc", Tag.styling(Palette.PRIMARY))
            .tag("cyan", Tag.styling(Palette.SECONDARY))
            .tag("hi", Tag.styling(Palette.TEXT))
            .tag("mute", Tag.styling(Palette.MUTED))
            .tag("dim", Tag.styling(Palette.DIM))
            .build();

    private static final MiniMessage MINI = MiniMessage.builder()
            .editTags(tags -> tags.resolver(THEME))
            .build();

    private static final String SMALL_CAPS = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀꜱᴛᴜᴠᴡxʏᴢ";

    private static final Pattern LEGACY = Pattern.compile("&(#[0-9a-fA-F]{6}|[0-9a-fk-orA-FK-OR])");
    private static final Map<Character, String> LEGACY_TAGS = Map.ofEntries(
            Map.entry('0', "black"), Map.entry('1', "dark_blue"), Map.entry('2', "dark_green"),
            Map.entry('3', "dark_aqua"), Map.entry('4', "dark_red"), Map.entry('5', "dark_purple"),
            Map.entry('6', "gold"), Map.entry('7', "gray"), Map.entry('8', "dark_gray"),
            Map.entry('9', "blue"), Map.entry('a', "green"), Map.entry('b', "aqua"),
            Map.entry('c', "red"), Map.entry('d', "light_purple"), Map.entry('e', "yellow"),
            Map.entry('f', "white"), Map.entry('k', "obfuscated"), Map.entry('l', "bold"),
            Map.entry('m', "strikethrough"), Map.entry('n', "underlined"), Map.entry('o', "italic"),
            Map.entry('r', "reset"));

    private Text() {
    }

    public static Component mini(String input, TagResolver... resolvers) {
        return MINI.deserialize(input, resolvers);
    }

    public static String legacyToMini(String input) {
        String normalized = input.replace('\u00a7', '&');
        return LEGACY.matcher(normalized).replaceAll(match -> {
            String code = match.group(1);
            return "<" + (code.startsWith("#") ? code : LEGACY_TAGS.get(Character.toLowerCase(code.charAt(0)))) + ">";
        });
    }

    public static String smallCaps(String input) {
        StringBuilder builder = new StringBuilder(input.length());
        for (char character : input.toCharArray()) {
            char lower = Character.toLowerCase(character);
            builder.append(lower >= 'a' && lower <= 'z' ? SMALL_CAPS.charAt(lower - 'a') : character);
        }
        return builder.toString();
    }

    public static String humanize(String id) {
        StringBuilder out = new StringBuilder();
        for (String part : id.split("[_\\-\\s]+")) {
            if (part.isEmpty()) continue;
            if (out.length() > 0) out.append(' ');
            out.append(part.substring(0, 1).toUpperCase(Locale.ROOT)).append(part.substring(1));
        }
        return out.length() == 0 ? id : out.toString();
    }
}
