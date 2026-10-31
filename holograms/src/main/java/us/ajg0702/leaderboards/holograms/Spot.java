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
import org.bukkit.Location;
import org.bukkit.World;

final class Spot {

    String world;
    double x;
    double y;
    double z;
    float yaw;
    float pitch;

    static Spot of(Location location) {
        Spot spot = new Spot();
        spot.world = location.getWorld() == null ? null : location.getWorld().getName();
        spot.x = location.getX();
        spot.y = location.getY();
        spot.z = location.getZ();
        spot.yaw = location.getYaw();
        spot.pitch = location.getPitch();
        return spot;
    }

    Location toLocation() {
        if (world == null) {
            return null;
        }
        World bukkitWorld = Bukkit.getWorld(world);
        return bukkitWorld == null ? null : new Location(bukkitWorld, x, y, z, yaw, pitch);
    }
}
