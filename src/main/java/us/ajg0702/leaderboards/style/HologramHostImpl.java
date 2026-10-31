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
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import us.ajg0702.leaderboards.LeaderboardPlugin;
import us.ajg0702.leaderboards.boards.StatEntry;
import us.ajg0702.leaderboards.boards.TimedType;
import us.ajg0702.leaderboards.holograms.HologramHost;
import us.ajg0702.leaderboards.holograms.HologramRow;
import us.ajg0702.leaderboards.holograms.HologramTop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class HologramHostImpl implements HologramHost {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character('§')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private static final class Snapshot {
        private volatile List<HologramRow> rows = Collections.emptyList();
        private volatile long at;
        private volatile boolean loaded;
        private volatile int limit;
        private final AtomicBoolean loading = new AtomicBoolean();
    }

    private final LeaderboardPlugin plugin;
    private final Map<String, Snapshot> cache = new ConcurrentHashMap<>();

    public HologramHostImpl(LeaderboardPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Plugin plugin() {
        return plugin;
    }

    @Override
    public List<String> statKeys() {
        return new ArrayList<>(plugin.getTopManager().getBoards());
    }

    @Override
    public String normalizeStat(String input) {
        if (input == null || input.isBlank()) return null;
        String[] parts = input.trim().split(":", 2);
        String board = null;
        for (String candidate : plugin.getTopManager().getBoards()) {
            if (candidate.equalsIgnoreCase(parts[0])) board = candidate;
        }
        if (board == null) return null;
        TimedType type = TimedType.ALLTIME;
        if (parts.length == 2) {
            try {
                type = TimedType.valueOf(parts[1].toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return type == TimedType.ALLTIME ? board : board + ":" + type.lowerName();
    }

    @Override
    public String statTitle(String stat) {
        String[] parts = stat.split(":", 2);
        String title = plugin.getStyle().boardName(parts[0]);
        if (parts.length == 2) {
            try {
                title += " · " + plugin.getStyle().typeName(TimedType.valueOf(parts[1].toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return title;
    }

    @Override
    public HologramTop top(String stat, int limit, int maxAgeSeconds) {
        Snapshot snapshot = cache.computeIfAbsent(stat, key -> new Snapshot());
        long now = System.currentTimeMillis();
        boolean stale = !snapshot.loaded || limit > snapshot.limit || now - snapshot.at >= maxAgeSeconds * 1000L;
        if (stale && snapshot.loading.compareAndSet(false, true)) {
            plugin.getScheduler().runTaskAsynchronously(() -> {
                try {
                    load(stat, limit, snapshot);
                } catch (Exception e) {
                    plugin.getLogger().warning("Could not load leaderboard '" + stat + "': " + e.getMessage());
                } finally {
                    snapshot.loading.set(false);
                }
            });
        }
        long age = snapshot.loaded ? Math.max(0L, (now - snapshot.at) / 1000L) : 0L;
        return new HologramTop(snapshot.loaded, snapshot.rows, age);
    }

    private void load(String stat, int limit, Snapshot snapshot) {
        String[] parts = stat.split(":", 2);
        String board = parts[0];
        TimedType type = TimedType.ALLTIME;
        if (parts.length == 2) {
            try {
                type = TimedType.valueOf(parts[1].toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
            }
        }
        List<HologramRow> rows = new ArrayList<>();
        if (plugin.getTopManager().getBoards().contains(board)) {
            for (int position = 1; position <= limit; position++) {
                StatEntry entry = plugin.getCache().getStat(position, board, type);
                if (entry == null || !entry.hasPlayer()) break;
                rows.add(new HologramRow(entry.getPlayerID(), entry.getPlayerName(), entry.getScoreFormatted()));
            }
        }
        snapshot.rows = rows;
        snapshot.limit = limit;
        snapshot.at = System.currentTimeMillis();
        snapshot.loaded = true;
    }

    @Override
    public String render(String template, Map<String, String> text, Map<String, String> parsed) {
        List<TagResolver> resolvers = new ArrayList<>();
        text.forEach((key, value) -> resolvers.add(Placeholder.unparsed(key, value == null ? "" : value)));
        parsed.forEach((key, value) -> resolvers.add(Placeholder.parsed(key, Text.legacyToMini(value == null ? "" : value))));
        Component component = Text.mini(Text.legacyToMini(template), resolvers.toArray(new TagResolver[0]));
        return LEGACY.serialize(component);
    }

    @Override
    public void send(CommandSender receiver, String template, Map<String, String> text) {
        List<TagResolver> resolvers = new ArrayList<>();
        text.forEach((key, value) -> resolvers.add(Placeholder.unparsed(key, value == null ? "" : value)));
        Component component = Text.mini(template, resolvers.toArray(new TagResolver[0]));
        plugin.getAdventure().sender(receiver).sendMessage(component);
    }
}
