package com.huntercraft.huntercraft.abilities.bungeegum;

import com.huntercraft.huntercraft.effect.HunterMobEffects;
import com.huntercraft.huntercraft.entity.ElasticAuraConstructEntity;
import com.huntercraft.huntercraft.entity.HunterEntityTypes;
import com.huntercraft.huntercraft.particle.HunterParticles;
import com.huntercraft.huntercraft.util.HunterDataUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class ElasticAuraManager {
    public static final String TECHNIQUE_ID = "elastic_aura";
    public static final int TAG_DURATION_TICKS = 200;
    public static final int TRAP_DURATION_TICKS = 20 * 18;
    public static final int TRAP_STUN_TICKS = 80;
    public static final int MAX_TAGS = 2;
    public static final int MAX_TRAPS = 3;
    public static final int TEXTURE_SURPRISE_TICKS = 7 * 20;
    public static final int REFLECT_TICKS = 50;
    private static final String ELASTIC_AURA_STATE = "HuntercraftElasticAura";
    private static final String TAGS = "Tags";
    private static final String TEXTURE_SURPRISE_UNTIL = "TextureSurpriseUntil";
    private static final String REFLECT_HIDDEN = "ReflectHidden";
    private static final String BIND_UUID_A = "BindUuidA";
    private static final String BIND_UUID_B = "BindUuidB";
    private static final String BIND_TICKS = "BindTicks";
    private static final String BIND_CONSTRUCT_UUID = "BindConstructUuid";
    private static final String PULL_CONSTRUCT_UUID = "PullConstructUuid";
    public static final int BIND_DURATION_TICKS = 60;

    private ElasticAuraManager() {
    }

    public static boolean hasElasticAura(ServerPlayer player) {
        return HunterDataUtil.getOptional(player)
                .map(data -> TECHNIQUE_ID.equals(data.getNenTechniqueId()))
                .orElse(false);
    }

    public static void tick(ServerPlayer player) {
        CompoundTag state = getState(player);
        ListTag tagList = state.getList(TAGS, Tag.TAG_COMPOUND);
        long gameTime = player.level().getGameTime();
        for (int i = tagList.size() - 1; i >= 0; i--) {
            CompoundTag entry = tagList.getCompound(i);
            if (entry.getLong("ExpireAt") < gameTime) {
                discardConstruct(player.serverLevel(), entry);
                tagList.remove(i);
            }
        }
        state.put(TAGS, tagList);

        // Tick active entity bind (2 entities pulled together)
        tickBind(player, state);

        TagEntry anchor = getTagEntries(player).stream().filter(TagEntry::isAnchor).findFirst().orElse(null);
        if (anchor == null) {
            return;
        }
        // Only pull the first entity tag toward the anchor — one at a time
        List<TagEntry> entityEntries = getTagEntries(player).stream().filter(TagEntry::isEntity).toList();
        if (entityEntries.isEmpty()) {
            return;
        }
        TagEntry entry = entityEntries.get(0);
        LivingEntity target = resolveLiving(player.serverLevel(), entry.entityUuid);
        if (target == null || !target.isAlive()) {
            clearTag(player, entry);
            return;
        }
        applyElasticPull(target, anchor.position, 1.35D, 0.24D);
        if (!entry.hidden && player.tickCount % 2 == 0) {
            spawnElasticTrail(player.serverLevel(), anchor.position.add(0.0D, 0.15D, 0.0D), target.getEyePosition(), getNenColor(player));
        }
        if (target.position().distanceTo(anchor.position) <= 1.2D) {
            HunterDataUtil.applyStun(target, player, 30);
            // Clear anchor and all entity tags once the pull completes
            clearTag(player, entry);
            clearTag(player, anchor);
        }
    }

    private static void tickBind(ServerPlayer player, CompoundTag state) {
        int bindTicks = state.getInt(BIND_TICKS);
        if (bindTicks <= 0) {
            discardUuid(player.serverLevel(), state.getString(BIND_CONSTRUCT_UUID));
            state.remove(BIND_CONSTRUCT_UUID);
            return;
        }
        state.putInt(BIND_TICKS, bindTicks - 1);
        LivingEntity entityA = resolveLiving(player.serverLevel(), state.getString(BIND_UUID_A));
        LivingEntity entityB = resolveLiving(player.serverLevel(), state.getString(BIND_UUID_B));
        if (entityA == null || !entityA.isAlive() || entityB == null || !entityB.isAlive()) {
            state.putInt(BIND_TICKS, 0);
            discardUuid(player.serverLevel(), state.getString(BIND_CONSTRUCT_UUID));
            state.remove(BIND_CONSTRUCT_UUID);
            return;
        }
        Vec3 midpoint = entityA.position().add(0.0D, entityA.getBbHeight() * 0.5D, 0.0D)
                .lerp(entityB.position().add(0.0D, entityB.getBbHeight() * 0.5D, 0.0D), 0.5D);
        Vec3 flatMidpointA = new Vec3(midpoint.x, entityA.getY(), midpoint.z);
        Vec3 flatMidpointB = new Vec3(midpoint.x, entityB.getY(), midpoint.z);
        applyElasticPull(entityA, flatMidpointA, 1.2D, 0.0D);
        applyElasticPull(entityB, flatMidpointB, 1.2D, 0.0D);
        entityA.setDeltaMovement(entityA.getDeltaMovement().x, Math.min(0.0D, entityA.getDeltaMovement().y), entityA.getDeltaMovement().z);
        entityB.setDeltaMovement(entityB.getDeltaMovement().x, Math.min(0.0D, entityB.getDeltaMovement().y), entityB.getDeltaMovement().z);
    }

    public static void startBind(ServerPlayer player, LivingEntity entityA, LivingEntity entityB) {
        CompoundTag state = getState(player);
        discardUuid(player.serverLevel(), state.getString(BIND_CONSTRUCT_UUID));
        state.putString(BIND_UUID_A, entityA.getStringUUID());
        state.putString(BIND_UUID_B, entityB.getStringUUID());
        state.putInt(BIND_TICKS, BIND_DURATION_TICKS);
        ElasticAuraConstructEntity construct = spawnStringConstruct(player.serverLevel(), player, entityA.getEyePosition().lerp(entityB.getEyePosition(), 0.5D),
                ElasticAuraConstructEntity.TYPE_BIND_STRING, entityA.getStringUUID() + ";" + entityB.getStringUUID(), false, BIND_DURATION_TICKS + 4);
        if (construct != null) {
            state.putString(BIND_CONSTRUCT_UUID, construct.getStringUUID());
        }
    }

    public static void armTextureSurprise(ServerPlayer player) {
        getState(player).putLong(TEXTURE_SURPRISE_UNTIL, player.level().getGameTime() + TEXTURE_SURPRISE_TICKS);
    }

    public static boolean consumeTextureSurprise(ServerPlayer player) {
        CompoundTag state = getState(player);
        long until = state.getLong(TEXTURE_SURPRISE_UNTIL);
        if (until < player.level().getGameTime()) {
            state.remove(TEXTURE_SURPRISE_UNTIL);
            return false;
        }
        state.remove(TEXTURE_SURPRISE_UNTIL);
        return true;
    }

    public static void setReflectHidden(ServerPlayer player, boolean hidden) {
        CompoundTag state = getState(player);
        if (hidden) {
            state.putBoolean(REFLECT_HIDDEN, true);
        } else {
            state.remove(REFLECT_HIDDEN);
        }
    }

    public static boolean isReflectHidden(ServerPlayer player) {
        return getState(player).getBoolean(REFLECT_HIDDEN);
    }

    public static void addAnchor(ServerPlayer player, Vec3 position, UUID constructUuid, boolean hidden) {
        addTagEntry(player, new TagEntry("anchor", "", position, constructUuid, hidden, player.level().getGameTime()));
    }

    public static void addEntityTag(ServerPlayer player, LivingEntity target, boolean hidden) {
        ElasticAuraConstructEntity construct = spawnStringConstruct(player.serverLevel(), player, target.getEyePosition(),
                ElasticAuraConstructEntity.TYPE_ENTITY_TAG, target.getStringUUID(), hidden, TAG_DURATION_TICKS);
        addTagEntry(player, new TagEntry("entity", target.getStringUUID(), target.position(), construct == null ? null : construct.getUUID(), hidden, player.level().getGameTime()));
    }

    public static boolean handleTaggedMelee(ServerPlayer player, LivingEntity attacked) {
        List<TagEntry> entries = getTagEntries(player);
        TagEntry attackedEntry = entries.stream()
                .filter(entry -> entry.isEntity() && attacked.getStringUUID().equals(entry.entityUuid))
                .findFirst()
                .orElse(null);

        TagEntry anchor = entries.stream().filter(TagEntry::isAnchor).findFirst().orElse(null);
        List<TagEntry> entityEntries = entries.stream().filter(TagEntry::isEntity).toList();
        ServerLevel level = player.serverLevel();
        if (attackedEntry == null && anchor != null) {
            addEntityTag(player, attacked, anchor.hidden);
            return true;
        }
        // No anchor but already have 1 entity tag — tag this second entity to set up the bind
        if (attackedEntry == null && anchor == null && entityEntries.size() == 1) {
            boolean hidden = entityEntries.get(0).hidden;
            addEntityTag(player, attacked, hidden);
            return true;
        }
        if (attackedEntry == null) {
            return false;
        }

        if (anchor != null) {
            applyElasticPull(attacked, anchor.position, 1.45D, 0.28D);
            attacked.addEffect(new MobEffectInstance(HunterMobEffects.STUNNED.get(), 18, 0, false, false, true));
            clearTag(player, attackedEntry);
            return true;
        }

        if (entityEntries.size() >= 2) {
            LivingEntity other = resolveLiving(level, entityEntries.stream()
                    .filter(entry -> !attacked.getStringUUID().equals(entry.entityUuid))
                    .map(entry -> entry.entityUuid)
                    .findFirst()
                    .orElse(""));
            if (other != null) {
                // Start sustained bind — tick() will pull them together over BIND_DURATION_TICKS
                startBind(player, attacked, other);
                HunterDataUtil.applyStun(attacked, player, 40);
                HunterDataUtil.applyStun(other, player, 40);
                for (TagEntry entry : entityEntries) {
                    clearTag(player, entry);
                }
                return true;
            }
        }
        return false;
    }

    public static void spawnAnchorConstruct(ServerLevel level, ServerPlayer owner, Vec3 position, boolean hidden) {
        ElasticAuraConstructEntity construct = HunterEntityTypes.ELASTIC_AURA_CONSTRUCT.get().create(level);
        if (construct == null) {
            return;
        }
        Vec3 grounded = new Vec3(position.x, Math.floor(position.y) - 0.02D, position.z);
        construct.setOwner(owner);
        construct.setConstructType(ElasticAuraConstructEntity.TYPE_ANCHOR);
        construct.setColor(getNenColor(owner));
        construct.setHidden(hidden);
        construct.setDurationTicks(TAG_DURATION_TICKS);
        construct.moveTo(grounded.x, grounded.y, grounded.z, owner.getYRot(), 0.0F);
        level.addFreshEntity(construct);
        addAnchor(owner, grounded, construct.getUUID(), hidden);
    }

    public static ElasticAuraConstructEntity placeTrap(ServerLevel level, ServerPlayer owner, Vec3 position, boolean hidden) {
        List<ElasticAuraConstructEntity> traps = level.getEntitiesOfClass(ElasticAuraConstructEntity.class,
                owner.getBoundingBox().inflate(64.0D),
                entity -> entity.isOwnedBy(owner) && entity.isTrap());
        if (traps.size() >= MAX_TRAPS) {
            traps.stream().min(Comparator.comparingInt(entity -> entity.tickCount)).ifPresent(Entity::discard);
        }

        ElasticAuraConstructEntity construct = HunterEntityTypes.ELASTIC_AURA_CONSTRUCT.get().create(level);
        if (construct == null) {
            return null;
        }
        construct.setOwner(owner);
        construct.setConstructType(ElasticAuraConstructEntity.TYPE_TRAP);
        construct.setColor(getNenColor(owner));
        construct.setHidden(hidden);
        construct.setDurationTicks(TRAP_DURATION_TICKS);
        construct.moveTo(position.x, Math.floor(position.y) + 0.05D, position.z, owner.getYRot(), 0.0F);
        level.addFreshEntity(construct);
        return construct;
    }

    public static void spawnPullString(ServerPlayer player, LivingEntity target, boolean hidden, int durationTicks) {
        CompoundTag state = getState(player);
        discardUuid(player.serverLevel(), state.getString(PULL_CONSTRUCT_UUID));
        ElasticAuraConstructEntity construct = spawnStringConstruct(player.serverLevel(), player, target.getEyePosition(),
                ElasticAuraConstructEntity.TYPE_PULL_STRING, target.getStringUUID(), hidden, durationTicks);
        if (construct != null) {
            state.putString(PULL_CONSTRUCT_UUID, construct.getStringUUID());
        }
    }

    public static void clearPullString(ServerPlayer player) {
        CompoundTag state = getState(player);
        discardUuid(player.serverLevel(), state.getString(PULL_CONSTRUCT_UUID));
        state.remove(PULL_CONSTRUCT_UUID);
    }

    public static void triggerTrap(ServerPlayer owner, LivingEntity target, ElasticAuraConstructEntity trap) {
        HunterDataUtil.applyStun(target, owner, TRAP_STUN_TICKS);
        target.setDeltaMovement(target.getDeltaMovement().scale(0.1D));
        target.hurtMarked = true;
        spawnElasticBurst(owner.serverLevel(), target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D), trap.getColor());
        trap.setHeldTargetUuid(target.getStringUUID());
        trap.onTriggered(TRAP_STUN_TICKS);
        // Clear the anchor tag so no additional entities get pulled into this trap
        TagEntry anchor = getTagEntries(owner).stream().filter(TagEntry::isAnchor).findFirst().orElse(null);
        if (anchor != null) {
            clearTag(owner, anchor);
        }
    }

    public static boolean reflectProjectile(ServerPlayer player, Projectile projectile, Entity attacker) {
        Vec3 direction = attacker != null
                ? attacker.getEyePosition().subtract(player.getEyePosition())
                : projectile.getDeltaMovement().scale(-1.0D);
        if (direction.lengthSqr() < 1.0E-4D) {
            direction = player.getLookAngle();
        }
        direction = direction.normalize().scale(Math.max(1.2D, projectile.getDeltaMovement().length() + 0.3D));
        projectile.setOwner(player);
        projectile.setPos(player.getX(), player.getEyeY() - 0.1D, player.getZ());
        projectile.setDeltaMovement(direction);
        projectile.hasImpulse = true;
        spawnElasticBurst(player.serverLevel(), player.getEyePosition(), getNenColor(player));
        return true;
    }

    public static void spawnElasticTrail(ServerLevel level, Vec3 start, Vec3 end, int color) {
        Vec3 delta = end.subtract(start);
        int steps = Math.max(4, (int) Math.ceil(delta.length() * 2.2D));
        Vector3f colorVec = colorVector(color);
        for (int i = 0; i <= steps; i++) {
            Vec3 point = start.lerp(end, i / (double) steps);
            level.sendParticles(new DustParticleOptions(colorVec, 1.05F), point.x, point.y, point.z, 1, 0.01D, 0.01D, 0.01D, 0.0D);
        }
    }

    public static void spawnElasticBurst(ServerLevel level, Vec3 position, int color) {
        Vector3f colorVec = colorVector(color);
        level.sendParticles(new DustParticleOptions(colorVec, 1.25F), position.x, position.y, position.z, 8, 0.12D, 0.12D, 0.12D, 0.0D);
    }

    public static void spawnReflectParticle(ServerLevel level, Vec3 position, int color, double scale) {
        Vector3f colorVec = colorVector(color);
        level.sendParticles(HunterParticles.ELASTIC_REFLECT.get(), position.x, position.y, position.z, 0, colorVec.x(), colorVec.y(), colorVec.z(), scale);
    }

    public static int getNenColor(ServerPlayer player) {
        return HunterDataUtil.get(player).getNenAuraColor();
    }

    public static Vec3 getHandPosition(Player player) {
        Vec3 look = player.getLookAngle().normalize();
        Vec3 side = new Vec3(-look.z, 0.0D, look.x).normalize().scale(0.34D);
        return player.getEyePosition().add(look.scale(0.55D)).add(side).add(0.0D, -0.18D, 0.0D);
    }

    private static void addTagEntry(ServerPlayer player, TagEntry entry) {
        CompoundTag state = getState(player);
        List<TagEntry> entries = getTagEntries(player);
        entries.add(entry);
        entries.sort(Comparator.comparingLong(left -> left.createdAt));
        while (entries.size() > MAX_TAGS) {
            TagEntry removed = entries.remove(0);
            discardConstruct(player.serverLevel(), removed);
        }
        ListTag list = new ListTag();
        for (TagEntry value : entries) {
            list.add(value.save(player.level().getGameTime() + TAG_DURATION_TICKS));
        }
        state.put(TAGS, list);
    }

    private static void clearTag(ServerPlayer player, TagEntry toRemove) {
        List<TagEntry> entries = getTagEntries(player);
        entries.removeIf(entry -> entry.matches(toRemove));
        ListTag list = new ListTag();
        for (TagEntry entry : entries) {
            list.add(entry.save(player.level().getGameTime() + TAG_DURATION_TICKS));
        }
        getState(player).put(TAGS, list);
        discardConstruct(player.serverLevel(), toRemove);
    }

    private static ElasticAuraConstructEntity spawnStringConstruct(ServerLevel level, ServerPlayer owner, Vec3 position, String type, String heldTargetUuid, boolean hidden, int durationTicks) {
        ElasticAuraConstructEntity construct = HunterEntityTypes.ELASTIC_AURA_CONSTRUCT.get().create(level);
        if (construct == null) {
            return null;
        }
        construct.setOwner(owner);
        construct.setConstructType(type);
        construct.setColor(getNenColor(owner));
        construct.setHidden(hidden);
        construct.setHeldTargetUuid(heldTargetUuid);
        construct.setDurationTicks(durationTicks);
        construct.moveTo(position.x, position.y, position.z, owner.getYRot(), 0.0F);
        level.addFreshEntity(construct);
        return construct;
    }

    private static void discardConstruct(ServerLevel level, CompoundTag entry) {
        if (entry.hasUUID("ConstructUuid")) {
            Entity entity = level.getEntity(entry.getUUID("ConstructUuid"));
            if (entity != null) {
                entity.discard();
            }
        }
    }

    private static void discardConstruct(ServerLevel level, TagEntry entry) {
        if (entry.constructUuid != null) {
            Entity entity = level.getEntity(entry.constructUuid);
            if (entity != null) {
                entity.discard();
            }
        }
    }

    private static void discardUuid(ServerLevel level, String uuidString) {
        if (uuidString == null || uuidString.isBlank()) {
            return;
        }
        try {
            Entity entity = level.getEntity(UUID.fromString(uuidString));
            if (entity != null) {
                entity.discard();
            }
        } catch (IllegalArgumentException ignored) {
        }
    }

    private static List<TagEntry> getTagEntries(ServerPlayer player) {
        List<TagEntry> entries = new ArrayList<>();
        ListTag list = getState(player).getList(TAGS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            entries.add(TagEntry.load(list.getCompound(i)));
        }
        return entries;
    }

    private static CompoundTag getState(ServerPlayer player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ELASTIC_AURA_STATE, Tag.TAG_COMPOUND)) {
            persistent.put(ELASTIC_AURA_STATE, new CompoundTag());
        }
        return persistent.getCompound(ELASTIC_AURA_STATE);
    }

    public static LivingEntity resolveLiving(ServerLevel level, String uuidString) {
        if (uuidString == null || uuidString.isBlank()) {
            return null;
        }
        try {
            return level.getEntity(UUID.fromString(uuidString)) instanceof LivingEntity living ? living : null;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static void applyElasticPull(LivingEntity target, Vec3 destination, double horizontalScale, double verticalScale) {
        Vec3 toDestination = destination.subtract(target.position());
        if (toDestination.lengthSqr() < 0.001D) {
            target.setDeltaMovement(target.getDeltaMovement().scale(0.2D));
        } else {
            Vec3 pull = toDestination.normalize().scale(Math.min(1.35D, 0.35D + (toDestination.length() * 0.12D)));
            target.setDeltaMovement(
                    Mth.clamp(pull.x * horizontalScale, -1.5D, 1.5D),
                    Mth.clamp((pull.y * verticalScale) + 0.08D, -0.45D, 0.65D),
                    Mth.clamp(pull.z * horizontalScale, -1.5D, 1.5D)
            );
        }
        target.hasImpulse = true;
        target.hurtMarked = true;
    }

    private static Vector3f colorVector(int color) {
        return new Vector3f(
                ((color >> 16) & 0xFF) / 255.0F,
                ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F
        );
    }

    private record TagEntry(String type, String entityUuid, Vec3 position, UUID constructUuid, boolean hidden, long createdAt) {
        private boolean isAnchor() {
            return "anchor".equals(this.type);
        }

        private boolean isEntity() {
            return "entity".equals(this.type);
        }

        private boolean matches(TagEntry other) {
            return this.type.equals(other.type)
                    && this.entityUuid.equals(other.entityUuid)
                    && this.hidden == other.hidden
                    && ((this.constructUuid == null && other.constructUuid == null)
                    || (this.constructUuid != null && this.constructUuid.equals(other.constructUuid)));
        }

        private CompoundTag save(long expireAt) {
            CompoundTag tag = new CompoundTag();
            tag.putString("Type", this.type);
            tag.putString("EntityUuid", this.entityUuid);
            tag.putDouble("X", this.position.x);
            tag.putDouble("Y", this.position.y);
            tag.putDouble("Z", this.position.z);
            if (this.constructUuid != null) {
                tag.putUUID("ConstructUuid", this.constructUuid);
            }
            tag.putBoolean("Hidden", this.hidden);
            tag.putLong("CreatedAt", this.createdAt);
            tag.putLong("ExpireAt", expireAt);
            return tag;
        }

        private static TagEntry load(CompoundTag tag) {
            return new TagEntry(
                    tag.getString("Type"),
                    tag.getString("EntityUuid"),
                    new Vec3(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z")),
                    tag.hasUUID("ConstructUuid") ? tag.getUUID("ConstructUuid") : null,
                    tag.getBoolean("Hidden"),
                    tag.getLong("CreatedAt")
            );
        }
    }
}
