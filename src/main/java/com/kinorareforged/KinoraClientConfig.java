package com.kinorareforged;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class KinoraClientConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue SCREEN_SHAKE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        SCREEN_SHAKE = builder.comment("Kinora 技能镜头震动强度倍率，0 为关闭 / Kinora skill screen shake multiplier, 0 disables it").defineInRange("screenShake", 1.0D, 0.0D, 3.0D);
        SPEC = builder.build();
    }

    private KinoraClientConfig() {
    }
}
