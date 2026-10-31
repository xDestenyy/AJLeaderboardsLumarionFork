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

import java.util.List;

public interface HologramService {

    void start();

    void reload();

    void shutdown();

    void execute(CommandSender sender, String[] args);

    List<String> complete(CommandSender sender, String[] args);
}
