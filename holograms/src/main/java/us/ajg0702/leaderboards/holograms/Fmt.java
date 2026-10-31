/*
 * ajLeaderboards (Lumarion fork)
 * Original work Copyright (C) ajgeiss0702 - https://github.com/ajgeiss0702/ajLeaderboards
 * Modifications Copyright (C) 2026 Lumarion fork contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version. See the LICENSE file.
 */
package us.ajg0702.leaderboards.holograms;

import java.util.Locale;

final class Fmt {

    private static final String SMALL_CAPS = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀꜱᴛᴜᴠᴡxʏᴢ";

    private Fmt() {
    }

    static String slug(String input) {
        String slug = input.toLowerCase(Locale.ROOT).trim().replaceAll("[^a-z0-9_-]+", "-").replaceAll("^-+|-+$", "");
        return slug.isEmpty() ? "unnamed" : slug;
    }

    static String duration(int totalSeconds) {
        return "%d:%02d".formatted(totalSeconds / 60, totalSeconds % 60);
    }

    static String smallCaps(String input) {
        StringBuilder builder = new StringBuilder(input.length());
        for (char character : input.toCharArray()) {
            char lower = Character.toLowerCase(character);
            builder.append(lower >= 'a' && lower <= 'z' ? SMALL_CAPS.charAt(lower - 'a') : character);
        }
        return builder.toString();
    }

    static boolean blank(String rendered) {
        return rendered.replaceAll("§.", "").isBlank();
    }
}
