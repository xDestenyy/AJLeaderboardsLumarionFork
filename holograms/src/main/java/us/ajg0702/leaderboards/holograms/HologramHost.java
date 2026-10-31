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
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;

public interface HologramHost {

    Plugin plugin();

    List<String> statKeys();

    String normalizeStat(String input);

    String statTitle(String stat);

    HologramTop top(String stat, int limit, int maxAgeSeconds);

    String render(String template, Map<String, String> text, Map<String, String> parsed);

    void send(CommandSender receiver, String template, Map<String, String> text);
}
