package com.huntercraft.huntercraft.api.blacklist;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.huntercraft.huntercraft.HunterConfig;
import com.huntercraft.huntercraft.HunterCraftMod;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class HunterBlacklistManager {
    private static final Map<UUID, String> BLACKLIST = new HashMap<>();
    private static final Gson GSON = new Gson();
    private static String lastLoadStatus = "Blacklist has not been loaded yet.";

    private HunterBlacklistManager() {
    }

    public static synchronized boolean loadBlacklist() {
        String urlValue = HunterConfig.BLACKLIST_URL.get().trim();
        if (urlValue.isEmpty()) {
            BLACKLIST.clear();
            lastLoadStatus = "HunterCraft blacklist URL is empty.";
            HunterCraftMod.LOGGER.warn(lastLoadStatus);
            return false;
        }

        try {
            URLConnection connection = new URL(urlValue).openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", HunterCraftMod.MODID + "-blacklist");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                JsonObject root = GSON.fromJson(reader, JsonObject.class);
                JsonArray entries = root != null && root.has("blacklist") && root.get("blacklist").isJsonArray()
                        ? root.getAsJsonArray("blacklist")
                        : new JsonArray();

                Map<UUID, String> loaded = new HashMap<>();
                for (JsonElement element : entries) {
                    if (!element.isJsonObject()) {
                        continue;
                    }
                    JsonObject entry = element.getAsJsonObject();
                    if (!entry.has("uuid") || !entry.has("reason")) {
                        continue;
                    }
                    try {
                        UUID uuid = UUID.fromString(entry.get("uuid").getAsString());
                        String reason = entry.get("reason").getAsString();
                        loaded.put(uuid, reason == null || reason.isBlank() ? "Blacklisted." : reason);
                    } catch (IllegalArgumentException ignored) {
                    }
                }

                BLACKLIST.clear();
                BLACKLIST.putAll(loaded);
                lastLoadStatus = "Loaded " + BLACKLIST.size() + " HunterCraft blacklist entr" + (BLACKLIST.size() == 1 ? "y." : "ies.");
                HunterCraftMod.LOGGER.info(lastLoadStatus);
                return true;
            }
        } catch (Exception exception) {
            lastLoadStatus = "Failed to load HunterCraft blacklist: " + exception.getMessage();
            HunterCraftMod.LOGGER.warn(lastLoadStatus, exception);
            return false;
        }
    }

    public static synchronized boolean isBlacklisted(UUID uuid) {
        return BLACKLIST.containsKey(uuid);
    }

    public static synchronized String getBlacklistReason(UUID uuid) {
        return BLACKLIST.get(uuid);
    }

    public static synchronized Map<UUID, String> getBlacklist() {
        return Collections.unmodifiableMap(new HashMap<>(BLACKLIST));
    }

    public static synchronized String getLastLoadStatus() {
        return lastLoadStatus;
    }
}
