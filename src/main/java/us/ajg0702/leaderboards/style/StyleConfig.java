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
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.YamlConfiguration;
import us.ajg0702.leaderboards.LeaderboardPlugin;
import us.ajg0702.leaderboards.boards.TimedType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public final class StyleConfig {

    public static final String FILE = "Leaderboards.yml";

    private final LeaderboardPlugin plugin;
    private volatile YamlConfiguration yaml = new YamlConfiguration();

    public StyleConfig(LeaderboardPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public synchronized void reload() {
        File file = new File(plugin.getDataFolder(), FILE);
        if (!file.exists()) {
            plugin.saveResource(FILE, false);
        }
        YamlConfiguration loaded = YamlConfiguration.loadConfiguration(file);
        try (InputStream in = plugin.getResource(FILE)) {
            if (in != null) {
                loaded.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8)));
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Could not read default " + FILE + ": " + e.getMessage());
        }
        yaml = loaded;
    }

    public int pageSize() {
        return Math.max(1, Math.min(50, yaml.getInt("top.page-size", 10)));
    }

    public String string(String path) {
        String value = yaml.getString(path);
        return value == null ? "" : value;
    }

    public List<String> lines(String path) {
        if (yaml.isList(path)) return yaml.getStringList(path);
        String single = yaml.getString(path);
        return single == null ? Collections.emptyList() : Collections.singletonList(single);
    }

    public TextColor rankColor(int rank) {
        String hex = yaml.getString("format.rank-colors.rank-" + rank, yaml.getString("format.rank-colors.default", "#9CA3AF"));
        TextColor color = TextColor.fromHexString(hex);
        return color == null ? Palette.MUTED : color;
    }

    public String boardName(String board) {
        String custom = yaml.getString("format.board-names." + board);
        return custom != null ? custom : Text.humanize(board.replaceFirst("^statistic_", ""));
    }

    public String boardTitle(String board) {
        String custom = yaml.getString("format.board-names." + board);
        String title = custom != null ? custom : Text.humanize(board.replaceFirst("^statistic_", ""));
        return yaml.getBoolean("format.small-caps-titles", true) ? Text.smallCaps(title) : title;
    }

    public String typeName(TimedType type) {
        return yaml.getString("format.types." + type.name(), type.name().toLowerCase(java.util.Locale.ROOT));
    }

    public TagResolver boardResolver(String board, TimedType type) {
        return TagResolver.resolver(
                Placeholder.unparsed("board", boardTitle(board)),
                Placeholder.unparsed("board_id", board),
                Placeholder.unparsed("type", typeName(type)),
                Placeholder.unparsed("type_id", type.name().toLowerCase(java.util.Locale.ROOT))
        );
    }

    public Component render(String template, TagResolver... resolvers) {
        return Text.mini(template, resolvers);
    }
}
