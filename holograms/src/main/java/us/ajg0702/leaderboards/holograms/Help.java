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

import java.util.Map;

final class Help {

    record Entry(String usage, String description) {
    }

    private Help() {
    }

    static Entry entry(String usage, String description) {
        return new Entry(usage, description);
    }

    static void send(HologramHost host, CommandSender sender, String title, Entry... entries) {
        String divider = "<dim>" + "━".repeat(28) + "</dim>";
        host.send(sender, "", Map.of());
        host.send(sender, "<bold><gradient:#8B5CF6:#22D3EE><t></gradient></bold>", Map.of("t", Fmt.smallCaps(title)));
        host.send(sender, divider, Map.of());
        for (Entry entry : entries) {
            host.send(sender, "<acc>▸ </acc><click:suggest_command:'/" + base(entry.usage())
                            + "'><hover:show_text:'<mute>Klicken zum Einfügen'><cyan>/<usage></cyan></hover></click>"
                            + "<dim> · </dim><mute><description></mute>",
                    Map.of("usage", entry.usage(), "description", entry.description()));
        }
        host.send(sender, divider, Map.of());
    }

    private static String base(String usage) {
        int cut = usage.length();
        for (char marker : new char[]{'<', '['}) {
            int index = usage.indexOf(marker);
            if (index >= 0) {
                cut = Math.min(cut, index);
            }
        }
        return usage.substring(0, cut).replace("'", "");
    }
}
