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

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class LeaderboardManager implements HologramService {

    private static final String TAG = "lumarion_leaderboard";
    private static final String CONFIG_FILE = "Holograms.yml";
    private static final String DATA_FILE = "leaderboards-data.yml";
    private static final long TICK_PERIOD = 20L;

    private static final class Live {

        private UUID text;
        private UUID head;
        private UUID headOwner;
        private String lastText;
        private int lines;
    }

    private final HologramHost host;
    private final LeaderboardCommand command;
    private final JavaPlugin plugin;
    private final Map<String, Leaderboard> boards = new LinkedHashMap<>();
    private final Map<String, Live> live = new HashMap<>();
    private final Map<UUID, PlayerProfile> profiles = new HashMap<>();
    private LeaderboardStyle style;
    private YamlConfiguration rawConfig;
    private final Map<String, LeaderboardStyle> styles = new HashMap<>();
    private BukkitTask task;
    private int seconds;

    LeaderboardManager(HologramHost host) {
        this.host = host;
        this.plugin = (JavaPlugin) host.plugin();
        this.command = new LeaderboardCommand(this, host);
    }

    @Override
    public void start() {
        Msg.load(host);
        loadConfig();
        loadBoards();
        startTask();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        command.execute(sender, args);
    }

    @Override
    public List<String> complete(CommandSender sender, String[] args) {
        return command.complete(sender, args);
    }

    @Override
    public void reload() {
        Msg.load(host);
        loadConfig();
        removeAll();
        startTask();
    }

    @Override
    public void shutdown() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        removeAll();
    }

    private void loadConfig() {
        File file = new File(plugin.getDataFolder(), CONFIG_FILE);
        if (!file.exists()) {
            plugin.saveResource(CONFIG_FILE, false);
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        try (InputStream in = plugin.getResource(CONFIG_FILE)) {
            if (in != null) {
                yaml.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8)));
            }
        } catch (IOException exception) {
            plugin.getLogger().warning(CONFIG_FILE + " Defaults konnten nicht gelesen werden: " + exception.getMessage());
        }
        rawConfig = yaml;
        style = LeaderboardStyle.read(yaml);
        styles.clear();
    }

    private void loadBoards() {
        boards.clear();
        File file = new File(plugin.getDataFolder(), DATA_FILE);
        if (!file.exists()) {
            return;
        }
        ConfigurationSection root = YamlConfiguration.loadConfiguration(file).getConfigurationSection("leaderboards");
        if (root == null) {
            return;
        }
        for (String key : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(key);
            if (section == null || section.getString("world") == null) {
                continue;
            }
            Spot spot = new Spot();
            spot.world = section.getString("world");
            spot.x = section.getDouble("x");
            spot.y = section.getDouble("y");
            spot.z = section.getDouble("z");
            spot.yaw = (float) section.getDouble("yaw");
            spot.pitch = (float) section.getDouble("pitch");
            Leaderboard board = new Leaderboard(section.getString("name", key), spot, section.getStringList("stats"));
            board.cycleSeconds(section.getInt("cycle", 0));
            board.size(section.getInt("size", 0));
            board.title(section.getString("title"));
            board.head(section.contains("head") ? section.getBoolean("head") : null);
            board.scale(section.contains("scale") ? section.getDouble("scale") : null);
            board.billboard(section.getString("billboard"));
            board.background(section.getString("background"));
            board.alpha(section.contains("alpha") ? section.getInt("alpha") : null);
            board.layout(section.contains("layout") ? section.getStringList("layout") : null);
            board.entry(section.getString("entry"));
            ConfigurationSection overrides = section.getConfigurationSection("overrides");
            if (overrides != null) {
                for (Map.Entry<String, Object> override : overrides.getValues(true).entrySet()) {
                    if (!(override.getValue() instanceof ConfigurationSection)) {
                        board.overrides().put(override.getKey(), override.getValue());
                    }
                }
            }
            boards.put(Fmt.slug(board.name()), board);
        }
    }

    private void saveBoards() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<String, Leaderboard> entry : boards.entrySet()) {
            Leaderboard board = entry.getValue();
            String base = "leaderboards." + entry.getKey() + ".";
            yaml.set(base + "name", board.name());
            yaml.set(base + "world", board.location().world);
            yaml.set(base + "x", board.location().x);
            yaml.set(base + "y", board.location().y);
            yaml.set(base + "z", board.location().z);
            yaml.set(base + "yaw", board.location().yaw);
            yaml.set(base + "pitch", board.location().pitch);
            yaml.set(base + "stats", board.stats());
            if (board.cycleSeconds() > 0) yaml.set(base + "cycle", board.cycleSeconds());
            if (board.size() > 0) yaml.set(base + "size", board.size());
            if (board.title() != null) yaml.set(base + "title", board.title());
            if (board.head() != null) yaml.set(base + "head", board.head());
            if (board.scale() != null) yaml.set(base + "scale", board.scale());
            if (board.billboard() != null) yaml.set(base + "billboard", board.billboard());
            if (board.background() != null) yaml.set(base + "background", board.background());
            if (board.alpha() != null) yaml.set(base + "alpha", board.alpha());
            if (board.layout() != null) yaml.set(base + "layout", board.layout());
            if (board.entry() != null) yaml.set(base + "entry", board.entry());
            for (Map.Entry<String, Object> override : board.overrides().entrySet()) {
                yaml.set(base + "overrides." + override.getKey(), override.getValue());
            }
        }
        try {
            plugin.getDataFolder().mkdirs();
            yaml.save(new File(plugin.getDataFolder(), DATA_FILE));
        } catch (IOException exception) {
            plugin.getLogger().warning(DATA_FILE + " konnte nicht gespeichert werden: " + exception.getMessage());
        }
    }

    private void startTask() {
        if (task != null) {
            task.cancel();
        }
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 40L, TICK_PERIOD);
    }

    LeaderboardStyle style() {
        return style;
    }

    LeaderboardStyle styleFor(Leaderboard board) {
        if (board.overrides().isEmpty()) {
            return style;
        }
        return styles.computeIfAbsent(Fmt.slug(board.name()), key -> {
            YamlConfiguration copy = new YamlConfiguration();
            try {
                copy.loadFromString(rawConfig.saveToString());
            } catch (InvalidConfigurationException exception) {
                return style;
            }
            for (Map.Entry<String, Object> override : board.overrides().entrySet()) {
                copy.set("leaderboards." + override.getKey(), override.getValue());
            }
            return LeaderboardStyle.read(copy);
        });
    }

    List<String> stylePaths() {
        List<String> paths = new ArrayList<>();
        ConfigurationSection root = rawConfig.getConfigurationSection("leaderboards");
        if (root != null) {
            for (String key : root.getKeys(true)) {
                if (!root.isConfigurationSection(key) && !key.startsWith("titles.")
                        && !key.equals("enabled") && !key.equals("data-refresh-seconds")
                        && !key.startsWith("format.entry-ranks.")) {
                    paths.add(key);
                }
            }
        }
        for (int rank = 1; rank <= 3; rank++) {
            paths.add("format.entry-ranks." + rank);
        }
        return paths;
    }

    boolean validPath(String path) {
        if (path.startsWith("titles.") && path.length() > "titles.".length()) {
            return true;
        }
        if (path.matches("format\\.entry-ranks\\.[0-9]+")) {
            return true;
        }
        return stylePaths().contains(path);
    }

    Object coerce(String path, String input) {
        Object current = rawConfig.get("leaderboards." + path);
        try {
            if (current instanceof Boolean) {
                return Boolean.parseBoolean(input);
            }
            if (current instanceof Integer) {
                return Integer.parseInt(input.trim());
            }
            if (current instanceof Number) {
                return Double.parseDouble(input.trim());
            }
        } catch (NumberFormatException exception) {
            return null;
        }
        if (current instanceof List) {
            List<String> values = new ArrayList<>();
            for (String part : input.split(",")) {
                values.add(part.trim());
            }
            return values;
        }
        return input;
    }

    void setOverride(Leaderboard board, String path, Object value) {
        if (value == null) {
            board.overrides().remove(path);
        } else {
            board.overrides().put(path, value);
        }
        styles.remove(Fmt.slug(board.name()));
        save(board);
        refresh(board);
    }

    void clearOverrides(Leaderboard board) {
        board.overrides().clear();
        styles.remove(Fmt.slug(board.name()));
        save(board);
        refresh(board);
    }

    Leaderboard copy(Leaderboard source, String name, Location location) {
        Leaderboard board = new Leaderboard(name, Spot.of(location), source.stats());
        board.cycleSeconds(source.cycleSeconds());
        board.size(source.size());
        board.title(source.title());
        board.head(source.head());
        board.scale(source.scale());
        board.billboard(source.billboard());
        board.background(source.background());
        board.alpha(source.alpha());
        board.layout(source.layout());
        board.entry(source.entry());
        board.overrides().putAll(source.overrides());
        boards.put(Fmt.slug(name), board);
        save(board);
        refresh(board);
        return board;
    }

    Collection<Leaderboard> all() {
        return boards.values();
    }

    Leaderboard find(String name) {
        return boards.get(Fmt.slug(name));
    }

    Leaderboard create(String name, Location location, List<String> stats) {
        Leaderboard board = new Leaderboard(name, Spot.of(location), stats);
        boards.put(Fmt.slug(name), board);
        save(board);
        refresh(board);
        return board;
    }

    void delete(Leaderboard board) {
        boards.remove(Fmt.slug(board.name()));
        remove(Fmt.slug(board.name()));
        saveBoards();
    }

    void save(Leaderboard board) {
        saveBoards();
    }

    void refresh(Leaderboard board) {
        remove(Fmt.slug(board.name()));
        tick();
    }

    private void removeAll() {
        for (String key : new ArrayList<>(live.keySet())) {
            remove(key);
        }
        live.clear();
    }

    private void remove(String key) {
        Live state = live.remove(key);
        if (state == null) {
            return;
        }
        discard(state.text);
        discard(state.head);
    }

    private void discard(UUID id) {
        if (id == null) {
            return;
        }
        Entity entity = Bukkit.getEntity(id);
        if (entity != null) {
            entity.remove();
        }
    }

    private void tick() {
        if (!style.enabled()) {
            removeAll();
            return;
        }
        seconds++;
        for (Map.Entry<String, Leaderboard> entry : boards.entrySet()) {
            Leaderboard board = entry.getValue();
            Location location = board.location().toLocation();
            if (location == null || !chunkLoaded(location)) {
                continue;
            }
            Live state = live.computeIfAbsent(entry.getKey(), key -> new Live());
            boolean spawned = false;
            TextDisplay display = resolve(state.text, TextDisplay.class);
            if (display == null) {
                display = spawnText(board, location);
                state.text = display.getUniqueId();
                state.lastText = null;
                spawned = true;
            }
            if (!spawned && seconds % styleFor(board).renderSeconds() != 0 && state.lastText != null) {
                continue;
            }
            List<String> lines = render(board);
            String text = String.join("\n", lines);
            if (!text.equals(state.lastText)) {
                display.setText(text);
                state.lastText = text;
            }
            updateHead(board, state, location, lines.size(), spawned);
        }
    }

    private boolean chunkLoaded(Location location) {
        World world = location.getWorld();
        return world != null && world.isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    private <T extends Entity> T resolve(UUID id, Class<T> type) {
        if (id == null) {
            return null;
        }
        Entity entity = Bukkit.getEntity(id);
        return type.isInstance(entity) && entity.isValid() ? type.cast(entity) : null;
    }

    private double scaleOf(Leaderboard board) {
        return board.scale() != null ? Math.max(0.1, board.scale()) : styleFor(board).scale();
    }

    private TextDisplay spawnText(Leaderboard board, Location location) {
        LeaderboardStyle style = styleFor(board);
        Display.Billboard billboard = board.billboard() == null ? style.billboard()
                : orDefault(LeaderboardStyle.parse(Display.Billboard.class, board.billboard()), style.billboard());
        String hex = board.background() != null ? board.background() : style.background();
        int alpha = board.alpha() != null ? Math.max(0, Math.min(255, board.alpha())) : style.alpha();
        float scale = (float) scaleOf(board);
        return location.getWorld().spawn(location, TextDisplay.class, display -> {
            display.setPersistent(false);
            display.setInvulnerable(true);
            display.addScoreboardTag(TAG);
            display.setBillboard(billboard);
            display.setAlignment(style.alignment());
            display.setLineWidth(style.lineWidth());
            display.setShadowed(style.shadow());
            display.setSeeThrough(style.seeThrough());
            display.setTextOpacity((byte) style.textOpacity());
            display.setBackgroundColor(color(hex, alpha));
            display.setViewRange((float) (style.viewDistance() / 64.0));
            display.setTransformation(new Transformation(new Vector3f(), new AxisAngle4f(),
                    new Vector3f(scale, scale, scale), new AxisAngle4f()));
        });
    }

    private <E> E orDefault(E value, E fallback) {
        return value == null ? fallback : value;
    }

    private Color color(String hex, int alpha) {
        try {
            int rgb = Integer.parseInt(hex.replace("#", ""), 16);
            return Color.fromARGB(alpha, (rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
        } catch (NumberFormatException exception) {
            return Color.fromARGB(alpha, 11, 11, 20);
        }
    }

    private void updateHead(Leaderboard board, Live state, Location base, int lines, boolean forceMove) {
        LeaderboardStyle style = styleFor(board);
        boolean wanted = board.head() != null ? board.head() : style.head();
        List<String> stats = board.stats();
        if (stats.isEmpty()) {
            discard(state.head);
            state.head = null;
            state.headOwner = null;
            return;
        }
        String stat = stats.get(pageIndex(board, stats.size()));
        HologramTop top = host.top(stat, style.maxSize(), style.dataRefreshSeconds());
        if (!wanted || top.rows().isEmpty()) {
            discard(state.head);
            state.head = null;
            state.headOwner = null;
            return;
        }
        HologramRow leader = top.rows().get(0);
        double height = lines * style.lineHeight() * scaleOf(board) + style.headHeight();
        Location target = base.clone().add(0.0, height, 0.0);
        ItemDisplay display = resolve(state.head, ItemDisplay.class);
        if (display == null) {
            float scale = (float) style.headScale();
            display = base.getWorld().spawn(target, ItemDisplay.class, entity -> {
                entity.setPersistent(false);
                entity.setInvulnerable(true);
                entity.addScoreboardTag(TAG);
                entity.setBillboard(Display.Billboard.VERTICAL);
                entity.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.HEAD);
                entity.setViewRange((float) (style.viewDistance() / 64.0));
                entity.setTransformation(new Transformation(new Vector3f(),
                        new AxisAngle4f((float) Math.toRadians(style.headYaw()), 0.0f, 1.0f, 0.0f),
                        new Vector3f(scale, scale, scale), new AxisAngle4f()));
            });
            state.head = display.getUniqueId();
            state.headOwner = null;
        } else if (forceMove || state.lines != lines) {
            display.teleport(target);
        }
        state.lines = lines;
        if (leader.id() != null && !leader.id().equals(state.headOwner)) {
            state.headOwner = leader.id();
            display.setItemStack(skull(leader, display));
        }
    }

    private ItemStack skull(HologramRow leader, ItemDisplay display) {
        PlayerProfile profile = profiles.computeIfAbsent(leader.id(), id -> Bukkit.createProfile(id, leader.name()));
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setPlayerProfile((com.destroystokyo.paper.profile.PlayerProfile) profile);
        item.setItemMeta(meta);
        if (profile.getTextures().isEmpty()) {
            UUID displayId = display.getUniqueId();
            profile.update().thenAccept(updated -> Bukkit.getScheduler().runTask(plugin, () -> {
                profiles.put(leader.id(), updated);
                ItemDisplay current = resolve(displayId, ItemDisplay.class);
                if (current != null && leader.id().equals(ownerOf(displayId))) {
                    ItemStack refreshed = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta refreshedMeta = (SkullMeta) refreshed.getItemMeta();
                    refreshedMeta.setPlayerProfile((com.destroystokyo.paper.profile.PlayerProfile) updated);
                    refreshed.setItemMeta(refreshedMeta);
                    current.setItemStack(refreshed);
                }
            }));
        }
        return item;
    }

    private UUID ownerOf(UUID headId) {
        for (Live state : live.values()) {
            if (headId.equals(state.head)) {
                return state.headOwner;
            }
        }
        return null;
    }

    private int pageIndex(Leaderboard board, int pages) {
        int cycle = board.cycleSeconds() > 0 ? board.cycleSeconds() : styleFor(board).defaultCycle();
        if (pages <= 1 || cycle <= 0) {
            return 0;
        }
        return (int) ((System.currentTimeMillis() / 1000L / cycle) % pages);
    }

    private List<String> render(Leaderboard board) {
        LeaderboardStyle style = styleFor(board);
        List<String> stats = board.stats();
        List<String> lines = new ArrayList<>();
        if (stats.isEmpty()) {
            lines.add(host.render("<err>Keine Statistik gesetzt", Map.of(), Map.of()));
            return lines;
        }
        int index = pageIndex(board, stats.size());
        String stat = stats.get(index);
        int size = Math.max(1, Math.min(style.maxSize(), board.size() > 0 ? board.size() : style.defaultSize()));
        String title = board.title() != null && !board.title().isBlank() ? board.title() : style.titleOf(stat, host);
        HologramTop top = host.top(stat, style.maxSize(), style.dataRefreshSeconds());
        String dots = ".".repeat(1 + (int) (System.currentTimeMillis() / 1000L % 3L));
        StringBuilder pages = new StringBuilder();
        if (stats.size() > 1) {
            for (int page = 0; page < stats.size(); page++) {
                pages.append(page == index ? style.pageActive() : style.pageInactive());
                if (page < stats.size() - 1) {
                    pages.append(' ');
                }
            }
        }
        HologramRow leader = top.rows().isEmpty() ? null : top.rows().get(0);
        Map<String, String> text = new HashMap<>();
        text.put("stat", stat);
        text.put("size", String.valueOf(size));
        text.put("updated", Fmt.duration((int) Math.min(top.ageSeconds(), Integer.MAX_VALUE)));
        text.put("dots", dots);
        text.put("index", String.valueOf(index + 1));
        text.put("count", String.valueOf(stats.size()));
        text.put("leader", leader == null ? "-" : leader.name());
        text.put("leader_value", leader == null ? "0" : leader.value());
        Map<String, String> parsed = new HashMap<>();
        parsed.put("title", title);
        parsed.put("pages", pages.toString());

        List<String> layout = board.layout() != null && !board.layout().isEmpty() ? board.layout() : style.layout();
        for (String format : layout) {
            if (format == null) {
                continue;
            }
            if (format.trim().equalsIgnoreCase("<entries>")) {
                entries(lines, board, top, size, text, parsed);
                continue;
            }
            add(lines, format, text, parsed);
        }
        return lines;
    }

    private void entries(List<String> lines, Leaderboard board, HologramTop top, int size,
                         Map<String, String> text, Map<String, String> parsed) {
        LeaderboardStyle style = styleFor(board);
        if (!top.loaded()) {
            add(lines, style.waiting(), text, parsed);
            return;
        }
        for (int rank = 1; rank <= size; rank++) {
            Map<String, String> rankText = new HashMap<>(text);
            Map<String, String> rankParsed = new HashMap<>(parsed);
            rankText.put("rank", String.valueOf(rank));
            rankParsed.put("color", style.rankColor(rank));
            if (rank <= top.rows().size()) {
                HologramRow row = top.rows().get(rank - 1);
                String format = board.entry() != null && !board.entry().isBlank() ? board.entry() : style.entryFor(rank);
                rankText.put("name", row.name() == null ? "?" : row.name());
                rankText.put("value", row.value());
                add(lines, format, rankText, rankParsed);
            } else {
                add(lines, style.empty(), rankText, rankParsed);
            }
        }
    }

    private void add(List<String> lines, String format, Map<String, String> text, Map<String, String> parsed) {
        if (format == null || format.isEmpty()) {
            return;
        }
        String rendered = host.render(format, text, parsed);
        if (format.contains("<pages>") && Fmt.blank(rendered)) {
            return;
        }
        lines.add(rendered);
    }
}
