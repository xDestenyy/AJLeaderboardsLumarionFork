/*
 * ajLeaderboards (Lumarion fork)
 * Original work Copyright (C) ajgeiss0702 - https://github.com/ajgeiss0702/ajLeaderboards
 * Modifications Copyright (C) 2026 Lumarion fork contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version. See the LICENSE file.
 */
package us.ajg0702.leaderboards.commands.main.subcommands;

import org.bukkit.Bukkit;
import us.ajg0702.commands.CommandSender;
import us.ajg0702.commands.SubCommand;
import us.ajg0702.leaderboards.LeaderboardPlugin;

import java.util.Arrays;
import java.util.List;

public class Hologram extends SubCommand {

    private final LeaderboardPlugin plugin;

    public Hologram(LeaderboardPlugin plugin) {
        super("hologram", Arrays.asList("holo", "hg", "hologramm"), "ajleaderboards.leaderboard", "Verwaltet Leaderboard-Hologramme");
        this.plugin = plugin;
    }

    @Override
    public List<String> autoComplete(CommandSender sender, String[] args) {
        List<String> options = plugin.getHolograms().complete(bukkit(sender), args);
        return filterCompletion(options, args.length == 0 ? "" : args[args.length - 1]);
    }

    @Override
    public void execute(CommandSender sender, String[] args, String label) {
        plugin.getHolograms().execute(bukkit(sender), args);
    }

    private org.bukkit.command.CommandSender bukkit(CommandSender sender) {
        Object handle = sender.getHandle();
        if (handle instanceof org.bukkit.command.CommandSender bukkitSender) {
            return bukkitSender;
        }
        return Bukkit.getConsoleSender();
    }
}
