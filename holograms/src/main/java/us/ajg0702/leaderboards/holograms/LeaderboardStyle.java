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

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class LeaderboardStyle {

    private final boolean enabled;
    private final int renderSeconds;
    private final int dataRefreshSeconds;
    private final double viewDistance;
    private final int defaultSize;
    private final int maxSize;
    private final int defaultCycle;
    private final double scale;
    private final Display.Billboard billboard;
    private final TextDisplay.TextAlignment alignment;
    private final int lineWidth;
    private final String background;
    private final int alpha;
    private final boolean shadow;
    private final boolean seeThrough;
    private final int textOpacity;
    private final boolean head;
    private final double headScale;
    private final double headHeight;
    private final double lineHeight;
    private final double headYaw;
    private final List<String> rankColors;
    private final String defaultColor;
    private final String title;
    private final String subtitle;
    private final String divider;
    private final String entry;
    private final String empty;
    private final String footer;
    private final String waiting;
    private final List<String> layout;
    private final Map<Integer, String> entryRanks = new HashMap<>();
    private final String pageActive;
    private final String pageInactive;
    private final Map<String, String> titles = new HashMap<>();

    private LeaderboardStyle(FileConfiguration config) {
        ConfigurationSection root = config.getConfigurationSection("leaderboards");
        if (root == null) {
            root = config.createSection("leaderboards");
        }
        enabled = root.getBoolean("enabled", true);
        renderSeconds = Math.max(1, root.getInt("render-interval-seconds", 5));
        dataRefreshSeconds = Math.max(1, root.getInt("data-refresh-seconds", 30));
        viewDistance = Math.max(8.0, root.getDouble("view-distance", 48.0));
        maxSize = Math.max(1, Math.min(100, root.getInt("max-size", 15)));
        defaultSize = Math.max(1, Math.min(maxSize, root.getInt("default-size", 10)));
        defaultCycle = Math.max(0, root.getInt("default-cycle-seconds", 10));
        scale = Math.max(0.1, root.getDouble("display.scale", 1.0));
        billboard = parseEnum(Display.Billboard.class, root.getString("display.billboard", "CENTER"), Display.Billboard.CENTER);
        alignment = parseEnum(TextDisplay.TextAlignment.class, root.getString("display.alignment", "CENTER"), TextDisplay.TextAlignment.CENTER);
        lineWidth = Math.max(50, root.getInt("display.line-width", 240));
        background = root.getString("display.background", "#0B0B14");
        alpha = Math.max(0, Math.min(255, root.getInt("display.background-alpha", 150)));
        shadow = root.getBoolean("display.shadow", true);
        seeThrough = root.getBoolean("display.see-through", false);
        textOpacity = Math.max(26, Math.min(255, root.getInt("display.text-opacity", 255)));
        head = root.getBoolean("head.enabled", true);
        headScale = Math.max(0.1, root.getDouble("head.scale", 0.8));
        headHeight = root.getDouble("head.height", 0.55);
        lineHeight = Math.max(0.05, root.getDouble("head.line-height", 0.25));
        headYaw = root.getDouble("head.yaw-offset", 180.0);
        List<String> colors = root.getStringList("colors.ranks");
        rankColors = colors.isEmpty() ? List.of("#FACC15", "#E5E7EB", "#FB923C") : new ArrayList<>(colors);
        defaultColor = root.getString("colors.default", "#9CA3AF");
        title = root.getString("format.title", "<gradient:#8B5CF6:#22D3EE><bold><title></bold></gradient>");
        subtitle = root.getString("format.subtitle", "<dim>Top <size> <dark_gray>·</dark_gray> aktualisiert vor <updated>");
        divider = root.getString("format.divider", "<dark_gray><strikethrough>                                  </strikethrough>");
        entry = root.getString("format.entry", "<color:<color>><bold><rank>.</bold> <white><name> <dark_gray>»</dark_gray> <color:<color>><value>");
        empty = root.getString("format.empty", "<dark_gray><rank>. <dim>— frei —");
        footer = root.getString("format.footer", "<pages>");
        waiting = root.getString("format.waiting", "<mute>Bestenliste wird geladen<dim><dots>");
        List<String> configured = root.getStringList("format.layout");
        if (configured.isEmpty()) {
            configured = new ArrayList<>();
            configured.add(title);
            configured.add(subtitle);
            configured.add(divider);
            configured.add("<entries>");
            configured.add(divider);
            configured.add(footer);
        }
        layout = List.copyOf(configured);
        ConfigurationSection ranks = root.getConfigurationSection("format.entry-ranks");
        if (ranks != null) {
            for (String key : ranks.getKeys(false)) {
                try {
                    entryRanks.put(Integer.parseInt(key), ranks.getString(key, entry));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        pageActive = root.getString("format.page-active", "<hi>●</hi>");
        pageInactive = root.getString("format.page-inactive", "<dark_gray>○</dark_gray>");
        ConfigurationSection customTitles = root.getConfigurationSection("titles");
        if (customTitles != null) {
            for (String key : customTitles.getKeys(false)) {
                titles.put(key.toLowerCase(Locale.ROOT), customTitles.getString(key, key));
            }
        }
    }

    static LeaderboardStyle read(FileConfiguration config) {
        return new LeaderboardStyle(config);
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> type, String value, E fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return fallback;
        }
    }

    static <E extends Enum<E>> E parse(Class<E> type, String value) {
        return parseEnum(type, value, null);
    }

    boolean enabled() {
        return enabled;
    }

    int renderSeconds() {
        return renderSeconds;
    }

    int dataRefreshSeconds() {
        return dataRefreshSeconds;
    }

    double viewDistance() {
        return viewDistance;
    }

    int defaultSize() {
        return defaultSize;
    }

    int maxSize() {
        return maxSize;
    }

    int defaultCycle() {
        return defaultCycle;
    }

    double scale() {
        return scale;
    }

    Display.Billboard billboard() {
        return billboard;
    }

    TextDisplay.TextAlignment alignment() {
        return alignment;
    }

    int lineWidth() {
        return lineWidth;
    }

    String background() {
        return background;
    }

    int alpha() {
        return alpha;
    }

    boolean shadow() {
        return shadow;
    }

    boolean seeThrough() {
        return seeThrough;
    }

    int textOpacity() {
        return textOpacity;
    }

    boolean head() {
        return head;
    }

    double headScale() {
        return headScale;
    }

    double headHeight() {
        return headHeight;
    }

    double lineHeight() {
        return lineHeight;
    }

    double headYaw() {
        return headYaw;
    }

    String rankColor(int rank) {
        return rank >= 1 && rank <= rankColors.size() ? rankColors.get(rank - 1) : defaultColor;
    }

    String empty() {
        return empty;
    }

    String waiting() {
        return waiting;
    }

    List<String> layout() {
        return layout;
    }

    String entryFor(int rank) {
        return entryRanks.getOrDefault(rank, entry);
    }

    String pageActive() {
        return pageActive;
    }

    String pageInactive() {
        return pageInactive;
    }

    String titleOf(String stat, HologramHost host) {
        String custom = titles.get(stat.toLowerCase(Locale.ROOT));
        return custom != null ? custom : host.statTitle(stat);
    }
}
