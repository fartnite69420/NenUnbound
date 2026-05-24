package com.huntercraft.huntercraft.api.blacklist;

public class HunterBlacklistEntry {
    private final String uuid;
    private final String reason;

    public HunterBlacklistEntry(String uuid, String reason) {
        this.uuid = uuid;
        this.reason = reason;
    }

    public String getUuid() {
        return uuid;
    }

    public String getReason() {
        return reason;
    }
}
