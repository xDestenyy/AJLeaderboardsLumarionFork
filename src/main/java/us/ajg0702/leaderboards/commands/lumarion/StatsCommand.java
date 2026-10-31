/*
 * ajLeaderboards (Lumarion fork)
 * Original work Copyright (C) ajgeiss0702 - https://github.com/ajgeiss0702/ajLeaderboards
 * Modifications Copyright (C) 2026 Lumarion fork contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version. See the LICENSE file.
 */
package us.ajg0702.leaderboards.commands.lumarion;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import us.ajg0702.commands.BaseCommand;
import us.ajg0702.commands.CommandSender;
import us.ajg0702.leaderboards.LeaderboardPlugin;
import us.ajg0702.leaderboards.boards.StatEntry;
import us.ajg0702.leaderboards.boards.TimedType;
import us.ajg0702.leaderboards.style.StyleConfig;
import us.ajg0702.leaderboards.utils.PlayerResolver;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

public class StatsCommand extends BaseCommand {

    private final LeaderboardPlugin plugin;

    public StatsCommand(LeaderboardPlugin plugin) {
        super("lbstats", Arrays.asList("lbstat", "bestenstats"), "ajleaderboards.top", "Zeigt die Platzierungen eines Spielers");
        this.plugin = plugin;
    }

    @Override
    public List<String> autoComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filterCompletion(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()), args[0]);
        }
        if (args.length == 2) return filterCompletion(TimedType.lowerNames(), args[1]);
        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args, String label) {
        StyleConfig style = plugin.getStyle();
        if (!checkPermission(sender)) {
            sender.sendMessage(style.render(style.string("messages.no-permission")));
            return;
        }

        String input;
        if (args.length >= 1) {
            input = args[0];
        } else if (sender.isPlayer()) {
            input = ((Player) sender.getHandle()).getUniqueId().toString();
        } else {
            sender.sendMessage(style.render(style.string("messages.stats-usage"), Placeholder.unparsed("label", label)));
            return;
        }

        TimedType type = TimedType.ALLTIME;
        if (args.length >= 2) {
            try {
                type = TimedType.valueOf(args[1].toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                sender.sendMessage(style.render(style.string("messages.stats-usage"), Placeholder.unparsed("label", label)));
                return;
            }
        }

        final TimedType finalType = type;
        final UUID onlineHint = PlayerResolver.online(input);
        final String finalInput = input;
        plugin.getScheduler().runTaskAsynchronously(() -> {
            UUID id = PlayerResolver.resolve(plugin, finalInput, onlineHint).orElse(null);
            if (id == null) {
                sender.sendMessage(style.render(style.string("messages.player-unknown"), Placeholder.unparsed("player", finalInput)));
                return;
            }
            show(sender, id, finalType);
        });
    }

    private void show(CommandSender sender, UUID id, TimedType type) {
        StyleConfig style = plugin.getStyle();
        OfflinePlayer target = Bukkit.getOfflinePlayer(id);

        String name = null;
        int shown = 0;
        java.util.List<net.kyori.adventure.text.Component> lines = new java.util.ArrayList<>();
        for (String board : plugin.getTopManager().getBoards()) {
            StatEntry entry = plugin.getCache().getStatEntry(target, board, type);
            if (entry == null || !entry.hasPlayer() || entry.getPosition() <= 0) continue;
            if (name == null) name = entry.getPlayerName();            shown++;
            lines.add(style.render(style.string("stats.entry"), style.boardResolver(board, type),
                    TopCommand.entryResolver(style, entry, entry.getPosition())));
        }
        if (name == null) name = target.getName() != null ? target.getName() : id.toString();

        TagResolver player = Placeholder.unparsed("player", name);
        if (shown == 0) {
            sender.sendMessage(style.render(style.string("messages.stats-none"), player));
            return;
        }
        for (String line : style.lines("stats.header")) {
            sender.sendMessage(style.render(line, player, Placeholder.unparsed("type", style.typeName(type))));
        }
        lines.forEach(sender::sendMessage);
        for (String line : style.lines("stats.footer")) {
            sender.sendMessage(style.render(line, player));
        }
    }
}
