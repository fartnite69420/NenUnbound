package com.huntercraft.huntercraft.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class AfterImageStore {
    public static final AfterImageStore INSTANCE = new AfterImageStore();

    public static class Snapshot {
        public final UUID playerUuid;
        public final double x;
        public final double y;
        public final double z;
        public final float yaw;
        public final float pitch;
        public int ticksRemaining;
        public final int maxTicks;

        public Snapshot(UUID playerUuid, double x, double y, double z, float yaw, float pitch, int ticks) {
            this.playerUuid = playerUuid;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
            this.ticksRemaining = ticks;
            this.maxTicks = ticks;
        }

        public float getAlpha() {
            return this.maxTicks > 0 ? (float) this.ticksRemaining / (float) this.maxTicks : 0.0F;
        }
    }

    private final List<Snapshot> snapshots = new ArrayList<>();

    private AfterImageStore() {
    }

    public void addSnapshot(Snapshot snapshot) {
        this.snapshots.add(snapshot);
    }

    public void tick() {
        Iterator<Snapshot> iterator = this.snapshots.iterator();
        while (iterator.hasNext()) {
            Snapshot snapshot = iterator.next();
            snapshot.ticksRemaining--;
            if (snapshot.ticksRemaining <= 0) {
                iterator.remove();
            }
        }
    }

    public List<Snapshot> getSnapshots() {
        return this.snapshots;
    }

    public void clear() {
        this.snapshots.clear();
    }
}
