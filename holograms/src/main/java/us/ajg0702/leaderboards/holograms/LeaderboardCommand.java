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

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class LeaderboardCommand {

    private static final String ROOT = "ajlb hologram";

    private static final List<String> SUBS = List.of("help", "create", "delete", "move", "tp", "clone", "list", "info",
            "set", "line", "reload");

    private static final List<String> LINE_SUBS = List.of("show", "add", "insert", "set", "remove", "reset");

    private static final List<String> OPTIONS = List.of("stats", "cycle", "size", "title", "head", "scale",
            "billboard", "background", "alpha", "entry", "entry1", "entry2", "entry3", "empty", "waiting",
            "colors", "color", "alignment", "linewidth", "shadow", "seethrough", "opacity", "viewdistance",
            "interval", "headscale", "headheight", "lineheight", "all");

    private static final Map<String, String> EXTRA = Map.ofEntries(
            Map.entry("entry1", "format.entry-ranks.1"),
            Map.entry("entry2", "format.entry-ranks.2"),
            Map.entry("entry3", "format.entry-ranks.3"),
            Map.entry("empty", "format.empty"),
            Map.entry("waiting", "format.waiting"),
            Map.entry("colors", "colors.ranks"),
            Map.entry("color", "colors.default"),
            Map.entry("alignment", "display.alignment"),
            Map.entry("linewidth", "display.line-width"),
            Map.entry("shadow", "display.shadow"),
            Map.entry("seethrough", "display.see-through"),
            Map.entry("opacity", "display.text-opacity"),
            Map.entry("viewdistance", "view-distance"),
            Map.entry("interval", "render-interval-seconds"),
            Map.entry("headscale", "head.scale"),
            Map.entry("headheight", "head.height"),
            Map.entry("lineheight", "head.line-height"));

    private final LeaderboardManager manager;
    private final HologramHost host;

    LeaderboardCommand(LeaderboardManager manager, HologramHost host) {
        this.manager = manager;
        this.host = host;
    }

    void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            help(sender);
            return;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "help" -> help(sender);
            case "create" -> {
                if (args.length < 3) {
                    usage(sender, "create <name> <stat|all>");
                } else if (player(sender) != null) {
                    create(player(sender), args[1], args[2]);
                }
            }
            case "delete" -> withBoard(sender, args, "delete <name>", (s, board) -> {
                manager.delete(board);
                Msg.LB_DELETED.send(s, "name", board.name());
            });
            case "move" -> withBoard(sender, args, "move <name>", (s, board) -> {
                Player player = player(s);
                if (player == null) {
                    return;
                }
                board.location(Spot.of(player.getLocation()));
                manager.save(board);
                manager.refresh(board);
                Msg.LB_MOVED.send(s, "name", board.name());
            });
            case "tp" -> withBoard(sender, args, "tp <name>", (s, board) -> {
                Player player = player(s);
                if (player == null) {
                    return;
                }
                Location location = board.location().toLocation();
                if (location == null) {
                    Msg.LB_WORLD_MISSING.send(s);
                    return;
                }
                player.teleportAsync(location);
            });
            case "clone" -> {
                if (args.length < 3) {
                    usage(sender, "clone <name> <neu>");
                } else if (player(sender) != null) {
                    clone(player(sender), args[1], args[2]);
                }
            }
            case "list" -> list(sender);
            case "info" -> withBoard(sender, args, "info <name>", this::info);
            case "reload" -> {
                manager.reload();
                Msg.LB_RELOADED.send(sender);
            }
            case "set" -> {
                if (args.length < 4) {
                    usage(sender, "set <name> <option> <wert|reset>");
                } else {
                    set(sender, args[1], args[2], String.join(" ", Arrays.copyOfRange(args, 3, args.length)));
                }
            }
            case "line" -> lineCommand(sender, args);
            default -> help(sender);
        }
    }

    List<String> complete(CommandSender sender, String[] args) {
        if (args.length <= 1) {
            return SUBS;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "delete", "move", "tp", "info", "clone" -> {
                return args.length == 2 ? names() : List.of();
            }
            case "create" -> {
                return args.length == 3 ? statNames() : List.of();
            }
            case "set" -> {
                if (args.length == 2) return names();
                if (args.length == 3) return OPTIONS;
                if (args.length == 4) return valueHints(args[2]);
                return List.of();
            }
            case "line" -> {
                if (args.length == 2) return LINE_SUBS;
                if (args.length == 3) return names();
                return List.of();
            }
            default -> {
                return List.of();
            }
        }
    }

    private interface BoardAction {
        void run(CommandSender sender, Leaderboard board);
    }

    private void withBoard(CommandSender sender, String[] args, String usage, BoardAction action) {
        if (args.length < 2) {
            usage(sender, usage);
            return;
        }
        Leaderboard board = manager.find(args[1]);
        if (board == null) {
            Msg.LB_NOT_FOUND.send(sender, "name", args[1]);
            return;
        }
        action.run(sender, board);
    }

    private Player player(CommandSender sender) {
        if (sender instanceof Player player) {
            return player;
        }
        Msg.ERROR_PLAYERS_ONLY.send(sender);
        return null;
    }

    private void usage(CommandSender sender, String usage) {
        Msg.LB_USAGE.send(sender, "usage", ROOT + " " + usage);
    }

    private List<String> names() {
        return manager.all().stream().map(Leaderboard::name).toList();
    }

    private List<String> statNames() {
        List<String> names = new ArrayList<>(host.statKeys());
        names.add("all");
        return names;
    }

    private List<String> valueHints(String option) {
        return switch (option.toLowerCase(Locale.ROOT)) {
            case "stats" -> statNames();
            case "head" -> List.of("true", "false", "reset");
            case "billboard" -> List.of("CENTER", "FIXED", "VERTICAL", "HORIZONTAL", "reset");
            case "background" -> List.of("#0B0B14", "reset");
            case "alignment" -> List.of("CENTER", "LEFT", "RIGHT", "reset");
            case "shadow", "seethrough" -> List.of("true", "false", "reset");
            case "colors" -> List.of("#FACC15,#E5E7EB,#FB923C", "reset");
            case "color" -> List.of("#9CA3AF", "reset");
            default -> List.of("reset");
        };
    }

    private void help(CommandSender sender) {
        Help.send(host, sender, "Leaderboards",
                Help.entry(ROOT + " create <name> <stat|all>", "Erstellt ein Leaderboard an deiner Position"),
                Help.entry(ROOT + " delete <name>", "Löscht ein Leaderboard"),
                Help.entry(ROOT + " move <name>", "Verschiebt es an deine Position"),
                Help.entry(ROOT + " tp <name>", "Teleportiert dich zum Leaderboard"),
                Help.entry(ROOT + " clone <name> <neu>", "Kopiert es samt Einstellungen an deine Position"),
                Help.entry(ROOT + " list | info <name>", "Übersicht und Details"),
                Help.entry(ROOT + " set <name> stats <stat,stat|all>", "Statistiken (mehrere wechseln automatisch)"),
                Help.entry(ROOT + " set <name> cycle <sekunden>", "Wechselintervall bei mehreren Statistiken"),
                Help.entry(ROOT + " set <name> size <anzahl>", "Anzahl der Einträge"),
                Help.entry(ROOT + " set <name> title <text|reset>", "Eigener Titel (MiniMessage)"),
                Help.entry(ROOT + " set <name> head <true|false|reset>", "Kopf des Erstplatzierten"),
                Help.entry(ROOT + " set <name> scale <wert|reset>", "Größe"),
                Help.entry(ROOT + " set <name> billboard <modus|reset>", "CENTER, FIXED, VERTICAL, HORIZONTAL"),
                Help.entry(ROOT + " set <name> background <#hex|reset>", "Hintergrundfarbe"),
                Help.entry(ROOT + " set <name> alpha <0-255|reset>", "Hintergrund-Transparenz"),
                Help.entry(ROOT + " set <name> entry <text|reset>", "Format der Ranglisten-Einträge"),
                Help.entry(ROOT + " set <name> entry1|entry2|entry3 <text|reset>", "Format für Platz 1, 2 oder 3"),
                Help.entry(ROOT + " set <name> empty|waiting <text|reset>", "Text für freie Plätze und Ladeanzeige"),
                Help.entry(ROOT + " set <name> colors <#hex,#hex,#hex|reset>", "Farben von Platz 1, 2 und 3"),
                Help.entry(ROOT + " set <name> color <#hex|reset>", "Farbe aller anderen Plätze"),
                Help.entry(ROOT + " set <name> alignment <CENTER|LEFT|RIGHT|reset>", "Textausrichtung"),
                Help.entry(ROOT + " set <name> linewidth|opacity|interval|viewdistance <zahl|reset>", "Zeilenbreite, Deckkraft, Takt, Sichtweite"),
                Help.entry(ROOT + " set <name> shadow|seethrough <true|false|reset>", "Schatten und Durchsicht"),
                Help.entry(ROOT + " set <name> headscale|headheight|lineheight <zahl|reset>", "Größe und Position des Kopfes"),
                Help.entry(ROOT + " set <name> all reset", "Setzt alle eigenen Einstellungen zurück"),
                Help.entry(ROOT + " line show <name>", "Zeigt alle Zeilen mit Nummern"),
                Help.entry(ROOT + " line add <name> <text>", "Fügt eine Zeile hinzu (MiniMessage, Farben, Platzhalter)"),
                Help.entry(ROOT + " line insert <name> <nr> <text>", "Fügt eine Zeile vor Nummer <nr> ein"),
                Help.entry(ROOT + " line set <name> <nr> <text>", "Ersetzt eine Zeile"),
                Help.entry(ROOT + " line remove <name> <nr>", "Entfernt eine Zeile"),
                Help.entry(ROOT + " line reset <name>", "Setzt auf das Layout der Config zurück"),
                Help.entry(ROOT + " reload", "Lädt Konfiguration und Hologramme neu"));
    }

    private void create(Player player, String name, String stat) {
        if (manager.find(name) != null) {
            Msg.LB_EXISTS.send(player, "name", name);
            return;
        }
        List<String> stats = parseStats(stat);
        if (stats == null) {
            Msg.LB_INVALID_STAT.send(player, "stat", stat);
            return;
        }
        Location location = player.getLocation();
        location.setYaw(location.getYaw() + 180.0f);
        location.setPitch(0.0f);
        manager.create(name, location, stats);
        Msg.LB_CREATED.send(player, "name", name);
    }

    private void clone(Player player, String name, String target) {
        Leaderboard source = manager.find(name);
        if (source == null) {
            Msg.LB_NOT_FOUND.send(player, "name", name);
            return;
        }
        if (manager.find(target) != null) {
            Msg.LB_EXISTS.send(player, "name", target);
            return;
        }
        Location location = player.getLocation();
        location.setYaw(location.getYaw() + 180.0f);
        location.setPitch(0.0f);
        manager.copy(source, target, location);
        Msg.LB_CLONED.send(player, "name", source.name(), "target", target);
    }

    private List<String> parseStats(String input) {
        if (input.equalsIgnoreCase("all")) {
            List<String> all = host.statKeys();
            return all.isEmpty() ? null : new ArrayList<>(all);
        }
        List<String> stats = new ArrayList<>();
        for (String part : input.split(",")) {
            String normalized = host.normalizeStat(part.trim());
            if (normalized == null) {
                return null;
            }
            if (!stats.contains(normalized)) {
                stats.add(normalized);
            }
        }
        return stats.isEmpty() ? null : stats;
    }

    private void list(CommandSender sender) {
        if (manager.all().isEmpty()) {
            Msg.LB_EMPTY.send(sender);
            return;
        }
        Msg.LB_LIST_HEADER.send(sender);
        for (Leaderboard board : manager.all()) {
            Msg.LB_LIST_ENTRY.send(sender, "name", board.name(), "stats", String.join(", ", board.stats()));
        }
    }

    private void info(CommandSender sender, Leaderboard board) {
        LeaderboardStyle style = manager.styleFor(board);
        Msg.LB_INFO.send(sender,
                "name", board.name(),
                "stats", String.join(", ", board.stats()),
                "cycle", board.cycleSeconds() > 0 ? board.cycleSeconds() : style.defaultCycle(),
                "size", board.size() > 0 ? board.size() : style.defaultSize(),
                "head", board.head() != null ? board.head() : style.head(),
                "scale", board.scale() != null ? board.scale() : style.scale(),
                "billboard", board.billboard() != null ? board.billboard() : style.billboard().name());
        if (!board.overrides().isEmpty()) {
            Msg.LB_STYLE_HEADER.send(sender, "name", board.name());
            for (Map.Entry<String, String> option : EXTRA.entrySet()) {
                Object value = board.overrides().get(option.getValue());
                if (value != null) {
                    Msg.LB_STYLE_ENTRY.send(sender, "path", option.getKey(), "value", String.valueOf(value));
                }
            }
        }
    }

    private void lineCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            usage(sender, "line <show|add|insert|set|remove|reset> <name> ...");
            return;
        }
        String action = args[1].toLowerCase(Locale.ROOT);
        String name = args[2];
        switch (action) {
            case "show", "reset" -> line(sender, name, action, 0, null);
            case "add" -> {
                if (args.length < 4) {
                    usage(sender, "line add <name> <text>");
                    return;
                }
                line(sender, name, "add", 0, join(args, 3));
            }
            case "remove" -> {
                Integer index = args.length >= 4 ? number(args[3]) : null;
                if (index == null) {
                    usage(sender, "line remove <name> <nr>");
                    return;
                }
                line(sender, name, "remove", index, null);
            }
            case "set", "insert" -> {
                Integer index = args.length >= 5 ? number(args[3]) : null;
                if (index == null) {
                    usage(sender, "line " + action + " <name> <nr> <text>");
                    return;
                }
                line(sender, name, action, index, join(args, 4));
            }
            default -> usage(sender, "line <show|add|insert|set|remove|reset> <name> ...");
        }
    }

    private Integer number(String input) {
        try {
            int value = Integer.parseInt(input);
            return value >= 1 ? value : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String join(String[] args, int from) {
        return String.join(" ", Arrays.copyOfRange(args, from, args.length));
    }

    private void line(CommandSender sender, String name, String action, int index, String text) {
        Leaderboard board = manager.find(name);
        if (board == null) {
            Msg.LB_NOT_FOUND.send(sender, "name", name);
            return;
        }
        List<String> layout = new ArrayList<>(board.layout() != null && !board.layout().isEmpty()
                ? board.layout() : manager.styleFor(board).layout());
        switch (action) {
            case "show" -> {
                Msg.LB_LINES_HEADER.send(sender, "name", board.name());
                for (int number = 0; number < layout.size(); number++) {
                    host.send(sender, "<dark_gray><number>. </dark_gray><gray><line></gray>",
                            Map.of("number", String.valueOf(number + 1), "line", layout.get(number)));
                }
                return;
            }
            case "reset" -> board.layout(null);
            case "add" -> {
                layout.add(text);
                board.layout(layout);
            }
            case "insert" -> {
                if (index > layout.size() + 1) {
                    Msg.LB_INVALID_LINE.send(sender, "index", index);
                    return;
                }
                layout.add(index - 1, text);
                board.layout(layout);
            }
            case "set" -> {
                if (index > layout.size()) {
                    Msg.LB_INVALID_LINE.send(sender, "index", index);
                    return;
                }
                layout.set(index - 1, text);
                board.layout(layout);
            }
            case "remove" -> {
                if (index > layout.size()) {
                    Msg.LB_INVALID_LINE.send(sender, "index", index);
                    return;
                }
                layout.remove(index - 1);
                board.layout(layout);
            }
            default -> {
                return;
            }
        }
        manager.save(board);
        manager.refresh(board);
        Msg.LB_UPDATED.send(sender, "name", board.name(), "option", "line");
    }

    private boolean validExtra(String option, String value) {
        return switch (option) {
            case "alignment" -> LeaderboardStyle.parse(TextDisplay.TextAlignment.class, value) != null;
            case "color" -> value.matches("#[0-9a-fA-F]{6}");
            case "colors" -> {
                for (String part : value.split(",")) {
                    if (!part.trim().matches("#[0-9a-fA-F]{6}")) {
                        yield false;
                    }
                }
                yield true;
            }
            default -> true;
        };
    }

    private void set(CommandSender sender, String name, String option, String value) {
        Leaderboard board = manager.find(name);
        if (board == null) {
            Msg.LB_NOT_FOUND.send(sender, "name", name);
            return;
        }
        boolean reset = value.equalsIgnoreCase("reset");
        boolean ok = true;
        try {
            switch (option.toLowerCase(Locale.ROOT)) {
                case "stats" -> {
                    List<String> stats = parseStats(value);
                    if (stats == null) {
                        Msg.LB_INVALID_STAT.send(sender, "stat", value);
                        return;
                    }
                    board.stats(stats);
                }
                case "cycle" -> board.cycleSeconds(reset ? 0 : Math.max(0, Integer.parseInt(value)));
                case "size" -> board.size(reset ? 0 : Math.max(1, Integer.parseInt(value)));
                case "title" -> board.title(reset ? null : value);
                case "entry" -> board.entry(reset ? null : value);
                case "head" -> board.head(reset ? null : Boolean.parseBoolean(value));
                case "scale" -> board.scale(reset ? null : Double.parseDouble(value));
                case "billboard" -> {
                    if (reset) {
                        board.billboard(null);
                    } else if (LeaderboardStyle.parse(Display.Billboard.class, value) != null) {
                        board.billboard(value.toUpperCase(Locale.ROOT));
                    } else {
                        ok = false;
                    }
                }
                case "background" -> {
                    if (reset) {
                        board.background(null);
                    } else if (value.matches("#?[0-9a-fA-F]{6}")) {
                        board.background(value.startsWith("#") ? value : "#" + value);
                    } else {
                        ok = false;
                    }
                }
                case "alpha" -> board.alpha(reset ? null : Math.max(0, Math.min(255, Integer.parseInt(value))));
                case "all" -> {
                    if (reset) {
                        board.cycleSeconds(0);
                        board.size(0);
                        board.title(null);
                        board.entry(null);
                        board.head(null);
                        board.scale(null);
                        board.billboard(null);
                        board.background(null);
                        board.alpha(null);
                        board.layout(null);
                        manager.clearOverrides(board);
                    } else {
                        ok = false;
                    }
                }
                default -> {
                    String path = EXTRA.get(option.toLowerCase(Locale.ROOT));
                    if (path == null) {
                        ok = false;
                    } else if (reset) {
                        manager.setOverride(board, path, null);
                    } else {
                        ok = validExtra(option.toLowerCase(Locale.ROOT), value);
                        Object typed = ok ? manager.coerce(path, value) : null;
                        if (typed == null) {
                            ok = false;
                        } else {
                            manager.setOverride(board, path, typed);
                        }
                    }
                }
            }
        } catch (NumberFormatException exception) {
            ok = false;
        }
        if (!ok) {
            Msg.LB_INVALID_VALUE.send(sender, "option", option);
            return;
        }
        manager.save(board);
        manager.refresh(board);
        Msg.LB_UPDATED.send(sender, "name", board.name(), "option", option);
    }
}
