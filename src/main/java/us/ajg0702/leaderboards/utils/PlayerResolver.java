/*
 * ajLeaderboards (Lumarion fork)
 * Original work Copyright (C) ajgeiss0702 - https://github.com/ajgeiss0702/ajLeaderboards
 * Modifications Copyright (C) 2026 Lumarion fork contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version. See the LICENSE file.
 */
package us.ajg0702.leaderboards.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.ajg0702.leaderboards.LeaderboardPlugin;

import java.util.Optional;
import java.util.UUID;

public final class PlayerResolver {

    private PlayerResolver() {}

    public static UUID parseUuid(String input) {
        if(input == null) return null;
        String s = input.trim();
        try {
            if(s.length() == 36) return UUID.fromString(s);
            if(s.length() == 32) {
                return UUID.fromString(s.replaceFirst(
                        "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{12})",
                        "$1-$2-$3-$4-$5"));
            }
        } catch (IllegalArgumentException ignored) {}
        return null;
    }

    public static UUID online(String name) {
        Player player = Bukkit.getPlayerExact(name);
        return player == null ? null : player.getUniqueId();
    }

    public static Optional<UUID> resolve(LeaderboardPlugin plugin, String input, UUID onlineHint) {
        UUID parsed = parseUuid(input);
        if(parsed != null) return Optional.of(parsed);
        if(onlineHint != null) return Optional.of(onlineHint);
        return plugin.getCache().findPlayerId(input);
    }
}
