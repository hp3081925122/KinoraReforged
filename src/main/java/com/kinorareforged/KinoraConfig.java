package com.kinorareforged;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class KinoraConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue BOSS_MAX_HEALTH;
    public static final ModConfigSpec.DoubleValue BOSS_ATTACK_DAMAGE;
    public static final ModConfigSpec.DoubleValue BOSS_KNOCKBACK_RESISTANCE;
    public static final ModConfigSpec.DoubleValue BOSS_MOVEMENT_SPEED;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        BOSS_MAX_HEALTH = builder.comment("Kinora 最大生命值 / Kinora maximum health").defineInRange("bossMaxHealth", 400.0D, 1.0D, 1000000.0D);
        BOSS_ATTACK_DAMAGE = builder.comment("Kinora 技能伤害基数，原版技能伤害按 40 缩放 / Kinora skill damage base, original skill damage scales from 40").defineInRange("bossAttackDamage", 40.0D, 0.0D, 1000000.0D);
        BOSS_KNOCKBACK_RESISTANCE = builder.comment("Kinora 击退抗性 / Kinora knockback resistance").defineInRange("bossKnockbackResistance", 1.0D, 0.0D, 1.0D);
        BOSS_MOVEMENT_SPEED = builder.comment("Kinora 移动速度 / Kinora movement speed").defineInRange("bossMovementSpeed", 0.42D, 0.0D, 2.0D);
        SPEC = builder.build();
    }

    private KinoraConfig() {
    }
}
