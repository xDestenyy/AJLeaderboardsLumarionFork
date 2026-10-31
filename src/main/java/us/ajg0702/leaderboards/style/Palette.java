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

import net.kyori.adventure.text.format.TextColor;

public final class Palette {

    public static final String PRIMARY_HEX = "#8B5CF6";
    public static final String SECONDARY_HEX = "#22D3EE";

    public static final TextColor PRIMARY = TextColor.fromHexString(PRIMARY_HEX);
    public static final TextColor SECONDARY = TextColor.fromHexString(SECONDARY_HEX);
    public static final TextColor TEXT = TextColor.fromHexString("#F3F4F6");
    public static final TextColor MUTED = TextColor.fromHexString("#9CA3AF");
    public static final TextColor DIM = TextColor.fromHexString("#4B5563");
    public static final TextColor SUCCESS = TextColor.fromHexString("#4ADE80");
    public static final TextColor ERROR = TextColor.fromHexString("#F87171");
    public static final TextColor WARN = TextColor.fromHexString("#FBBF24");
    public static final TextColor INFO = TextColor.fromHexString("#38BDF8");

    private Palette() {
    }
}
