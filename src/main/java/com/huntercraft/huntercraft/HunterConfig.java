package com.huntercraft.huntercraft;

import com.huntercraft.huntercraft.data.HunterPlayerData;
import net.minecraftforge.common.ForgeConfigSpec;

public final class HunterConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ForgeConfigSpec.BooleanValue RANDOM_HATSU_TYPE;
    public static final ForgeConfigSpec.ConfigValue<String> BLACKLIST_URL;
    public static final ForgeConfigSpec.IntValue ABILITY_BARS_ON_SCREEN;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("nen");
        RANDOM_HATSU_TYPE = builder
                .comment("When true, choosing a Hatsu path assigns a random non-Specialist Nen type instead of the clicked type.")
                .define("randomHatsuType", false);
        builder.pop();
        builder.push("blacklist");
        BLACKLIST_URL = builder
                .comment("Raw JSON URL for the remote blacklist. Format: { \"blacklist\": [{ \"uuid\": \"...\", \"reason\": \"...\" }] }")
                .define("url", "https://raw.githubusercontent.com/YOUR_GITHUB_USERNAME/huntercraft-blacklist/main/blacklist.json");
        builder.pop();
        COMMON_SPEC = builder.build();

        ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();
        clientBuilder.push("ui");
        ABILITY_BARS_ON_SCREEN = clientBuilder
                .comment("How many combat bars are shown on the HUD at once.")
                .defineInRange("abilityBarsOnScreen", 1, 1, HunterPlayerData.COMBAT_BAR_COUNT);
        clientBuilder.pop();
        CLIENT_SPEC = clientBuilder.build();
    }

    private HunterConfig() {
    }
}
