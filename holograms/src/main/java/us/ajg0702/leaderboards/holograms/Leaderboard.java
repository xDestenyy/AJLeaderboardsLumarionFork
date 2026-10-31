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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class Leaderboard {

    private String name;
    private Spot location;
    private List<String> stats = new ArrayList<>();
    private int cycleSeconds;
    private int size;
    private String title;
    private Boolean head;
    private Double scale;
    private String billboard;
    private String background;
    private Integer alpha;
    private List<String> layout;
    private String entry;
    private final Map<String, Object> overrides = new LinkedHashMap<>();

    Leaderboard(String name, Spot location, List<String> stats) {
        this.name = name;
        this.location = location;
        this.stats = new ArrayList<>(stats);
    }

    String name() {
        return name;
    }

    Spot location() {
        return location;
    }

    void location(Spot location) {
        this.location = location;
    }

    List<String> stats() {
        return stats;
    }

    void stats(List<String> stats) {
        this.stats = new ArrayList<>(stats);
    }

    int cycleSeconds() {
        return cycleSeconds;
    }

    void cycleSeconds(int cycleSeconds) {
        this.cycleSeconds = cycleSeconds;
    }

    int size() {
        return size;
    }

    void size(int size) {
        this.size = size;
    }

    String title() {
        return title;
    }

    void title(String title) {
        this.title = title;
    }

    Boolean head() {
        return head;
    }

    void head(Boolean head) {
        this.head = head;
    }

    Double scale() {
        return scale;
    }

    void scale(Double scale) {
        this.scale = scale;
    }

    String billboard() {
        return billboard;
    }

    void billboard(String billboard) {
        this.billboard = billboard;
    }

    String background() {
        return background;
    }

    void background(String background) {
        this.background = background;
    }

    Integer alpha() {
        return alpha;
    }

    void alpha(Integer alpha) {
        this.alpha = alpha;
    }

    List<String> layout() {
        return layout;
    }

    void layout(List<String> layout) {
        this.layout = layout == null ? null : new ArrayList<>(layout);
    }

    Map<String, Object> overrides() {
        return overrides;
    }

    String entry() {
        return entry;
    }

    void entry(String entry) {
        this.entry = entry;
    }
}
