package com.huntercraft.huntercraft.faction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FactionSavedData extends SavedData {
    private static final String DATA_NAME = "huntercraft_factions";

    private final Map<String, FactionRecord> factions = new HashMap<>();
    private final Map<UUID, String> playerFactions = new HashMap<>();
    private final Map<UUID, InviteRecord> pendingInvites = new HashMap<>();

    public static FactionSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FactionSavedData::load, FactionSavedData::new, DATA_NAME);
    }

    public static FactionSavedData load(CompoundTag tag) {
        FactionSavedData data = new FactionSavedData();

        ListTag factionsTag = tag.getList("Factions", Tag.TAG_COMPOUND);
        for (int i = 0; i < factionsTag.size(); i++) {
            CompoundTag factionTag = factionsTag.getCompound(i);
            FactionRecord record = FactionRecord.fromTag(factionTag);
            data.factions.put(normalize(record.name), record);
            for (UUID member : record.members) {
                data.playerFactions.put(member, record.name);
            }
        }

        ListTag invitesTag = tag.getList("PendingInvites", Tag.TAG_COMPOUND);
        for (int i = 0; i < invitesTag.size(); i++) {
            CompoundTag inviteTag = invitesTag.getCompound(i);
            data.pendingInvites.put(inviteTag.getUUID("Target"), InviteRecord.fromTag(inviteTag));
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag factionsTag = new ListTag();
        this.factions.values().stream()
                .sorted(Comparator.comparing(record -> record.name.toLowerCase(Locale.ROOT)))
                .forEach(record -> factionsTag.add(record.toTag()));
        tag.put("Factions", factionsTag);

        ListTag invitesTag = new ListTag();
        this.pendingInvites.forEach((target, invite) -> invitesTag.add(invite.toTag(target)));
        tag.put("PendingInvites", invitesTag);
        return tag;
    }

    public boolean createFaction(ServerPlayer owner, String requestedName) {
        String name = sanitizeName(requestedName);
        if (name.isBlank() || this.playerFactions.containsKey(owner.getUUID()) || hasFactionNamed(name)) {
            return false;
        }

        FactionRecord record = new FactionRecord(name, owner.getUUID(), new HashSet<>(Set.of(owner.getUUID())));
        this.factions.put(normalize(name), record);
        this.playerFactions.put(owner.getUUID(), name);
        this.pendingInvites.remove(owner.getUUID());
        setDirty();
        return true;
    }

    public boolean invitePlayer(ServerPlayer inviter, ServerPlayer target) {
        String factionName = this.playerFactions.get(inviter.getUUID());
        if (factionName == null || this.playerFactions.containsKey(target.getUUID())) {
            return false;
        }
        FactionRecord record = this.factions.get(normalize(factionName));
        if (record == null || !record.owner.equals(inviter.getUUID())) {
            return false;
        }
        this.pendingInvites.put(target.getUUID(), new InviteRecord(record.name, inviter.getScoreboardName()));
        setDirty();
        return true;
    }

    public boolean acceptInvite(ServerPlayer target) {
        InviteRecord invite = this.pendingInvites.remove(target.getUUID());
        if (invite == null || this.playerFactions.containsKey(target.getUUID())) {
            return false;
        }
        FactionRecord record = this.factions.get(normalize(invite.factionName));
        if (record == null) {
            return false;
        }
        record.members.add(target.getUUID());
        this.playerFactions.put(target.getUUID(), record.name);
        setDirty();
        return true;
    }

    public boolean declineInvite(ServerPlayer target) {
        if (this.pendingInvites.remove(target.getUUID()) != null) {
            setDirty();
            return true;
        }
        return false;
    }

    public boolean leaveFaction(ServerPlayer player) {
        FactionRecord record = getFactionForPlayer(player.getUUID());
        if (record == null || record.owner.equals(player.getUUID())) {
            return false;
        }
        if (!record.members.remove(player.getUUID())) {
            return false;
        }
        this.playerFactions.remove(player.getUUID());
        this.pendingInvites.remove(player.getUUID());
        setDirty();
        return true;
    }

    public boolean disbandFaction(ServerPlayer player) {
        FactionRecord record = getFactionForPlayer(player.getUUID());
        if (record == null || !record.owner.equals(player.getUUID())) {
            return false;
        }

        this.factions.remove(normalize(record.name));
        for (UUID member : record.members) {
            this.playerFactions.remove(member);
        }
        this.pendingInvites.entrySet().removeIf(entry -> record.name.equalsIgnoreCase(entry.getValue().factionName));
        setDirty();
        return true;
    }

    public boolean areFactionMates(Player first, Player second) {
        String firstFaction = this.playerFactions.get(first.getUUID());
        String secondFaction = this.playerFactions.get(second.getUUID());
        return firstFaction != null && firstFaction.equals(secondFaction);
    }

    public String getFactionName(UUID playerId) {
        return this.playerFactions.getOrDefault(playerId, "");
    }

    public String getFactionOwnerName(MinecraftServer server, UUID playerId) {
        FactionRecord record = getFactionForPlayer(playerId);
        return record == null ? "" : resolvePlayerName(server, record.owner);
    }

    public String getPendingInviteFaction(UUID playerId) {
        InviteRecord invite = this.pendingInvites.get(playerId);
        return invite == null ? "" : invite.factionName;
    }

    public String getPendingInviterName(UUID playerId) {
        InviteRecord invite = this.pendingInvites.get(playerId);
        return invite == null ? "" : invite.inviterName;
    }

    public Set<String> getFactionMemberNames(MinecraftServer server, UUID playerId) {
        FactionRecord record = getFactionForPlayer(playerId);
        if (record == null) {
            return Set.of();
        }
        List<String> names = new ArrayList<>();
        for (UUID member : record.members) {
            names.add(resolvePlayerName(server, member));
        }
        names.sort(String::compareToIgnoreCase);
        return new HashSet<>(names);
    }

    public Set<UUID> getFactionMemberIds(UUID playerId) {
        FactionRecord record = getFactionForPlayer(playerId);
        return record == null ? Set.of() : Set.copyOf(record.members);
    }

    public Set<String> getInvitablePlayerNames(MinecraftServer server, UUID playerId) {
        FactionRecord record = getFactionForPlayer(playerId);
        if (record == null || !record.owner.equals(playerId)) {
            return Set.of();
        }
        List<String> names = new ArrayList<>();
        for (ServerPlayer onlinePlayer : server.getPlayerList().getPlayers()) {
            if (onlinePlayer.getUUID().equals(playerId) || this.playerFactions.containsKey(onlinePlayer.getUUID())) {
                continue;
            }
            names.add(onlinePlayer.getScoreboardName());
        }
        names.sort(String::compareToIgnoreCase);
        return new HashSet<>(names);
    }

    private FactionRecord getFactionForPlayer(UUID playerId) {
        String factionName = this.playerFactions.get(playerId);
        return factionName == null ? null : this.factions.get(normalize(factionName));
    }

    private boolean hasFactionNamed(String name) {
        return this.factions.containsKey(normalize(name));
    }

    private static String sanitizeName(String name) {
        if (name == null) {
            return "";
        }
        String cleaned = name.trim().replaceAll("\\s+", " ");
        if (cleaned.length() < 3 || cleaned.length() > 20) {
            return "";
        }
        return cleaned;
    }

    private static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    private static String resolvePlayerName(MinecraftServer server, UUID playerId) {
        ServerPlayer onlinePlayer = server.getPlayerList().getPlayer(playerId);
        return onlinePlayer != null ? onlinePlayer.getScoreboardName() : playerId.toString();
    }

    private record InviteRecord(String factionName, String inviterName) {
        private static InviteRecord fromTag(CompoundTag tag) {
            return new InviteRecord(tag.getString("FactionName"), tag.getString("InviterName"));
        }

        private CompoundTag toTag(UUID target) {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("Target", target);
            tag.putString("FactionName", this.factionName);
            tag.putString("InviterName", this.inviterName);
            return tag;
        }
    }

    private record FactionRecord(String name, UUID owner, Set<UUID> members) {
        private static FactionRecord fromTag(CompoundTag tag) {
            Set<UUID> members = new HashSet<>();
            ListTag membersTag = tag.getList("Members", Tag.TAG_STRING);
            for (int i = 0; i < membersTag.size(); i++) {
                members.add(UUID.fromString(membersTag.getString(i)));
            }
            return new FactionRecord(tag.getString("Name"), tag.getUUID("Owner"), members);
        }

        private CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putString("Name", this.name);
            tag.putUUID("Owner", this.owner);
            ListTag membersTag = new ListTag();
            this.members.stream()
                    .map(UUID::toString)
                    .sorted(String::compareToIgnoreCase)
                    .forEach(uuid -> membersTag.add(StringTag.valueOf(uuid)));
            tag.put("Members", membersTag);
            return tag;
        }
    }
}
