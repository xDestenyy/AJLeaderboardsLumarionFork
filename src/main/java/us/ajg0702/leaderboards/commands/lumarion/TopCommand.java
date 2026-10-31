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
import org.bukkit.entity.Player;
import us.ajg0702.commands.BaseCommand;
import us.ajg0702.commands.CommandSender;
import us.ajg0702.leaderboards.LeaderboardPlugin;
import us.ajg0702.leaderboards.boards.StatEntry;
import us.ajg0702.leaderboards.boards.TimedType;
import us.ajg0702.leaderboards.style.StyleConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class TopCommand extends BaseCommand {

    private final LeaderboardPlugin plugin;

    public TopCommand(LeaderboardPlugin plugin) {
        super("bestenliste", Arrays.asList("lbtop", "topliste"), "ajleaderboards.top", "Zeigt eine Bestenliste");
        this.plugin = plugin;
    }

    @Override
    public List<String> autoComplete(CommandSender sender, String[] args) {
        if (args.length == 1) return filterCompletion(plugin.getTopManager().getBoards(), args[0]);
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
        if (args.length < 1) {
            sender.sendMessage(style.render(style.string("messages.top-usage"), Placeholder.unparsed("label", label)));
            sender.sendMessage(style.render(style.string("messages.boards"),
                    Placeholder.unparsed("boards", String.join(", ", plugin.getTopManager().getBoards()))));
            return;
        }

        String board = null;
        for (String candidate : plugin.getTopManager().getBoards()) {
            if (candidate.equalsIgnoreCase(args[0])) board = candidate;
        }
        if (board == null) {
            sender.sendMessage(style.render(style.string("messages.board-unknown"), Placeholder.unparsed("board_id", args[0])));
            return;
        }

        TimedType type = TimedType.ALLTIME;
        int page = 1;
        for (int i = 1; i < args.length; i++) {
            try {
                type = TimedType.valueOf(args[i].toUpperCase(Locale.ROOT));
                continue;
            } catch (IllegalArgumentException ignored) {
            }
            try {
                page = Integer.parseInt(args[i]);
            } catch (NumberFormatException e) {
                sender.sendMessage(style.render(style.string("messages.top-usage"), Placeholder.unparsed("label", label)));
                return;
            }
        }

        final String finalBoard = board;
        final TimedType finalType = type;
        final int requestedPage = page;
        final Player viewer = sender.isPlayer() ? (Player) sender.getHandle() : null;
        final UUID viewerId = viewer == null ? null : viewer.getUniqueId();

        plugin.getScheduler().runTaskAsynchronously(() -> show(sender, viewerId, finalBoard, finalType, requestedPage));
    }

    private void show(CommandSender sender, UUID viewerId, String board, TimedType type, int requestedPage) {
        StyleConfig style = plugin.getStyle();
        int size = plugin.getCache().getBoardSize(board);
        int pageSize = style.pageSize();
        int pages = Math.max(1, (size + pageSize - 1) / pageSize);
        if (requestedPage < 1 || requestedPage > pages) {
            sender.sendMessage(style.render(style.string("messages.page-invalid"),
                    Placeholder.unparsed("pages", String.valueOf(pages))));
            return;
        }

        TagResolver base = TagResolver.resolver(
                style.boardResolver(board, type),
                Placeholder.unparsed("page", String.valueOf(requestedPage)),
                Placeholder.unparsed("pages", String.valueOf(pages)),
                Placeholder.unparsed("size", String.valueOf(size))
        );

        for (String line : style.lines("top.header")) {
            sender.sendMessage(style.render(line, base));
        }

        int from = (requestedPage - 1) * pageSize + 1;
        int to = Math.min(Math.max(size, from), from + pageSize - 1);
        if (size == 0) {
            for (String line : style.lines("top.empty")) sender.sendMessage(style.render(line, base));
        }
        for (int position = from; size > 0 && position <= to; position++) {
            StatEntry entry = plugin.getCache().getStat(position, board, type);
            sender.sendMessage(renderEntry(style, base, entry, position, viewerId));
        }

        if (viewerId != null) {
            StatEntry own = plugin.getCache().getStatEntry(org.bukkit.Bukkit.getOfflinePlayer(viewerId), board, type);
            if (own != null && own.hasPlayer() && own.getPosition() > 0) {
                for (String line : style.lines("top.own")) {
                    sender.sendMessage(style.render(line, base, entryResolver(style, own, own.getPosition())));
                }
            } else {
                for (String line : style.lines("top.own-none")) sender.sendMessage(style.render(line, base));
            }
        }

        for (String line : style.lines("top.footer")) {
            sender.sendMessage(style.render(line, base));
        }
    }

    private net.kyori.adventure.text.Component renderEntry(StyleConfig style, TagResolver base, StatEntry entry, int position, UUID viewerId) {
        if (entry == null || !entry.hasPlayer()) {
            return style.render(style.string("top.entry.empty"), base, Placeholder.unparsed("rank", String.valueOf(position)),
                    Placeholder.styling("rank_color", style.rankColor(position)));
        }
        String template;
        if (viewerId != null && viewerId.equals(entry.getPlayerID())) {
            template = style.string("top.entry.self");
        } else if (position <= 3) {
            template = style.string("top.entry.rank-" + position);
        } else {
            template = style.string("top.entry.default");
        }
        if (template.isEmpty()) template = style.string("top.entry.default");
        return style.render(template, base, entryResolver(style, entry, position));
    }

    static TagResolver entryResolver(StyleConfig style, StatEntry entry, int position) {
        String name = entry.getPlayerName() == null ? "?" : entry.getPlayerName();
        List<TagResolver> resolvers = new ArrayList<>();
        resolvers.add(Placeholder.unparsed("rank", String.valueOf(position)));
        resolvers.add(Placeholder.styling("rank_color", style.rankColor(position)));
        resolvers.add(Placeholder.unparsed("player", name));
        resolvers.add(Placeholder.unparsed("value", entry.getScoreFormatted()));
        resolvers.add(Placeholder.unparsed("value_pretty", entry.getScorePretty()));
        resolvers.add(Placeholder.unparsed("time", entry.getTime()));
        if (entry.getPlayerID() != null) {
            resolvers.add(Placeholder.parsed("uuid", entry.getPlayerID().toString()));
        }
        return TagResolver.resolver(resolvers);
    }
}
