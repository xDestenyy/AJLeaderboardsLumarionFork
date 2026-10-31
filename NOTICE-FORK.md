# AJLeaderboardsLumarionFork (fork of ajLeaderboards)

This is a **modified version** of [ajLeaderboards](https://github.com/ajgeiss0702/ajLeaderboards)
(version 2.11.0, commit `70e0419`) by **ajgeiss0702**.

* Original work: Copyright (C) ajgeiss0702
* Modifications: Copyright (C) 2026 Lumarion fork contributors
* License: **GNU General Public License v3.0** (see `LICENSE`, unchanged from upstream).
  This fork and every distributed build of it must stay under GPL-3.0-or-later and the
  corresponding source code must be made available to everyone who receives a jar.
* This fork is not affiliated with or endorsed by the original author. Please do not ask
  upstream for support with this fork.

## Changes compared to upstream 2.11.0 (GPLv3 section 5a)

All changes are on the `lumarion-fork` git branch; `git diff 70e0419..lumarion-fork` shows every one of them.

### UUID-based statistics
Upstream already uses the UUID as primary key (`id`) of every board table. Remaining name-based paths were removed:
* `Cache#removePlayer` deletes by UUID (was: by cached name). `/ajlb removeplayer` resolves name -> UUID first, also accepts a raw UUID.
* `/ajlb update` no longer uses `Bukkit.getOfflinePlayer(String)`; it resolves to a UUID (online player, UUID input, or name cache).
* Tie-breaking in rank queries uses the UUID instead of the cached name, so ranks of players with equal scores cannot change after a rename.
* New `Cache#syncPlayerName`: on join, the cached name of that UUID is refreshed on all boards, so a renamed player shows up with the new name immediately - the stats stay attached to the UUID.
* New `PlayerResolver` / `Cache#findPlayerId` for name -> UUID lookups (convenience for commands only).

### Duels style + customization
* New package `us.ajg0702.leaderboards.style` (`Palette`, `Text`, `StyleConfig`) with the Duels colour theme and MiniMessage tags (`<ok> <err> <warn> <info> <acc> <cyan> <hi> <mute> <dim>`).
* New config `Leaderboards.yml` (same idea as Duels' `Scoreboard.yml`): header/entry/own-position/footer templates, rank colours, board names, type names, all messages.
* New commands `/bestenliste <board> [type] [page]` (aliases `/lbtop`, `/topliste`) and `/lbstats [player|uuid] [type]`, permission `ajleaderboards.top` (default: true). Entries are clickable and open the stats by UUID.
* `/ajlb reload` also reloads `Leaderboards.yml`.
* Default sign layout changed to match the palette.

### Leaderboard holograms (port of the Duels system)
* New Gradle module `holograms` (Java 21, compiled against Paper 1.21.11): `TextDisplay` leaderboards with the head of the first place (`ItemDisplay`), automatic page cycling between several boards, live refresh and per-leaderboard overrides.
* New sub command `/ajlb hologram` (aliases `holo`, `hg`; permission `ajleaderboards.leaderboard`) with the same sub commands as the Duels `/leaderboard` command (`create, delete, move, tp, list, info, set, line, reload, help`) plus `clone`, `line insert` and additional named `set` options (entry1-3, empty, waiting, colors, color, alignment, linewidth, shadow, seethrough, opacity, viewdistance, interval, headscale, headheight, lineheight, all) so every look setting can be changed per hologram.
* New configs `Holograms.yml` (same `leaderboards:` keys as the Duels config), `leaderboard-messages.yml` (prefix and all messages) and the data file `leaderboards-data.yml`.
* The project now targets Java 21 (was Java 8) because the module needs the Paper 1.21 API.

### Build
* Version `2.11.0-lumarion.1`, jar name `AJLeaderboardsLumarionFork`, UTF-8 compile/resource encoding.
