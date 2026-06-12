package com.victorfaurschou.snapleashedentities;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SnapLeashedEntities implements ModInitializer {

    @Override
    public void onInitialize() {
        ServerTickEvents.START_LEVEL_TICK.register(this::onWorldTick);
    }

    private void onWorldTick(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            for (Leashable leashable : Leashable.leashableLeashedTo(player)) {
                double snapThreshold = leashable.leashSnapDistance() - 2.0;
                if (leashable.leashDistanceTo(player) > snapThreshold) {
                    Entity entity = (Entity) leashable;
                    Vec3 dest = findSafePos(level, entity, player.position());
                    entity.teleportTo(dest.x, dest.y, dest.z);
                }
            }
        }
    }

    private static Vec3 findSafePos(ServerLevel level, Entity entity, Vec3 center) {
        float w = entity.getBbWidth();
        float h = entity.getBbHeight();
        for (int r = 0; r <= 3; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (r > 0 && Math.abs(dx) < r && Math.abs(dz) < r) continue;
                    for (int dy = 2; dy >= -3; dy--) {
                        double cx = center.x + dx;
                        double cy = center.y + dy;
                        double cz = center.z + dz;
                        AABB aabb = new AABB(cx - w / 2.0, cy, cz - w / 2.0, cx + w / 2.0, cy + h, cz + w / 2.0);
                        BlockPos below = BlockPos.containing(cx, cy - 0.1, cz);
                        if (level.noCollision(entity, aabb) && level.getBlockState(below).isSolid()) {
                            return new Vec3(cx, cy, cz);
                        }
                    }
                }
            }
        }
        return center;
    }
}
