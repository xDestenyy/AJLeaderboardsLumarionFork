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

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public enum Msg {
    ERROR_PLAYERS_ONLY("<err>Das können nur Spieler."),
    LB_CREATED("<ok>Leaderboard <hi><name></hi> <ok>wurde an deiner Position erstellt."),
    LB_DELETED("<mute>Leaderboard <hi><name></hi> <mute>wurde gelöscht."),
    LB_EMPTY("<mute>Es gibt noch keine Leaderboards. Erstelle eines mit <hi>/ajlb hologram create</hi><mute>."),
    LB_EXISTS("<err>Ein Leaderboard mit dem Namen <hi><name></hi> <err>existiert bereits."),
    LB_CLONED("<ok>Leaderboard <hi><name></hi> <ok>wurde nach <hi><target></hi> <ok>kopiert."),
    LB_INFO("<mute>Leaderboard <hi><name></hi> <dim>│</dim> Stats <hi><stats></hi> <dim>│</dim> Wechsel <hi><cycle>s</hi> <dim>│</dim> Einträge <hi><size></hi> <dim>│</dim> Kopf <hi><head></hi> <dim>│</dim> Skalierung <hi><scale></hi> <dim>│</dim> Billboard <hi><billboard></hi>"),
    LB_INVALID_STAT("<err>Unbekannte Statistik <hi><stat></hi><err>. Nutze eine Bestenliste aus <hi>/ajlb list</hi><err>, optional mit Zeitraum (zum Beispiel <hi>statistic_player_kills:weekly</hi><err>), oder all."),
    LB_INVALID_VALUE("<err>Ungültiger Wert für <hi><option></hi><err>."),
    LB_INVALID_LINE("<err>Es gibt keine Zeile <hi><index></hi><err>."),
    LB_LINES_HEADER("<mute>Zeilen von <hi><name></hi><mute>:"),
    LB_LIST_ENTRY("<dim>▸</dim> <hi><name></hi> <dim>│</dim> <mute><stats>"),
    LB_LIST_HEADER("<gradient:#8B5CF6:#22D3EE><bold>Leaderboards</bold></gradient>"),
    LB_MOVED("<ok>Leaderboard <hi><name></hi> <ok>wurde an deine Position verschoben."),
    LB_NOT_FOUND("<err>Leaderboard <hi><name></hi> <err>wurde nicht gefunden."),
    LB_RELOADED("<ok>Leaderboards wurden neu geladen."),
    LB_STYLE_HEADER("<mute>Eigene Einstellungen von <hi><name></hi><mute>:"),
    LB_STYLE_ENTRY("<dim>▸</dim> <hi><path></hi> <dim>»</dim> <mute><value>"),
    LB_STYLE_NONE("<mute><hi><name></hi> <mute>nutzt überall die Standard-Einstellungen aus der Holograms.yml."),
    LB_USAGE("<mute>Benutzung <dim>»</dim> <hi>/<usage></hi>"),
    LB_UPDATED("<ok><hi><option></hi> <ok>von <hi><name></hi> <ok>wurde aktualisiert."),
    LB_WORLD_MISSING("<err>Die Welt dieses Leaderboards ist nicht geladen.");

    private static final int VERSION = 3;
    private static final String VERSION_KEY = "messages-version";
    private static final String PREFIX_KEY = "prefix";
    private static final String DEFAULT_PREFIX = "<bold><gradient:#8B5CF6:#22D3EE>Duels</gradient></bold> <dark_gray>»</dark_gray> ";
    private static final Map<Msg, String> TEXTS = new EnumMap<>(Msg.class);
    private static String prefix = DEFAULT_PREFIX;
    private static HologramHost host;

    private final String fallback;

    Msg(String fallback) {
        this.fallback = fallback;
    }

    static void load(HologramHost hologramHost) {
        host = hologramHost;
        Plugin plugin = hologramHost.plugin();
        File file = new File(plugin.getDataFolder(), "leaderboard-messages.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        boolean outdated = yaml.getInt(VERSION_KEY, 0) != VERSION;
        boolean changed = outdated;
        if (outdated) {
            yaml.set(VERSION_KEY, VERSION);
            yaml.set(PREFIX_KEY, DEFAULT_PREFIX);
        }
        prefix = yaml.getString(PREFIX_KEY, DEFAULT_PREFIX);
        for (Msg message : values()) {
            String path = message.path();
            if (outdated || !yaml.contains(path)) {
                yaml.set(path, message.fallback);
                changed = true;
            }
            TEXTS.put(message, yaml.getString(path, message.fallback));
        }
        if (changed) {
            try {
                plugin.getDataFolder().mkdirs();
                yaml.save(file);
            } catch (IOException exception) {
                plugin.getLogger().warning("leaderboard-messages.yml konnte nicht gespeichert werden: " + exception.getMessage());
            }
        }
    }

    public void send(CommandSender receiver, Object... pairs) {
        Map<String, String> values = new LinkedHashMap<>();
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            values.put(String.valueOf(pairs[i]), String.valueOf(pairs[i + 1]));
        }
        host.send(receiver, prefix + TEXTS.getOrDefault(this, fallback), values);
    }

    private String path() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
