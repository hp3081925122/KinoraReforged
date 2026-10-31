package com.kinorareforged;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class KinoraConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue BOSS_MAX_HEALTH;
    public static final ModConfigSpec.DoubleValue BOSS_ATTACK_DAMAGE;
    public static final ModConfigSpec.DoubleValue BOSS_KNOCKBACK_RESISTANCE;
    public static final ModConfigSpec.DoubleValue BOSS_MOVEMENT_SPEED;
    public static final ModConfigSpec.DoubleValue SCYTHE_WAVE_DAMAGE_RATIO;
    public static final ModConfigSpec.DoubleValue SCYTHE_COMBO_BASE_DAMAGE;
    public static final ModConfigSpec.DoubleValue SCYTHE_COMBO_ATTACK_RATIO;
    public static final ModConfigSpec.IntValue SCYTHE_COMBO_COOLDOWN;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        BOSS_MAX_HEALTH = builder.comment("Kinora 最大生命值 / Kinora maximum health").defineInRange("bossMaxHealth", 400.0D, 1.0D, 1000000.0D);
        BOSS_ATTACK_DAMAGE = builder.comment("Kinora 技能伤害基数，原版技能伤害按 40 缩放 / Kinora skill damage base, original skill damage scales from 40").defineInRange("bossAttackDamage", 40.0D, 0.0D, 1000000.0D);
        BOSS_KNOCKBACK_RESISTANCE = builder.comment("Kinora 击退抗性 / Kinora knockback resistance").defineInRange("bossKnockbackResistance", 1.0D, 0.0D, 1.0D);
        BOSS_MOVEMENT_SPEED = builder.comment("Kinora 移动速度 / Kinora movement speed").defineInRange("bossMovementSpeed", 0.42D, 0.0D, 2.0D);
        SCYTHE_WAVE_DAMAGE_RATIO = builder.comment("棠郎古镰满蓄力剑气伤害 = 玩家攻击力 × 此倍率 / Ancestral Scythe full-charge wave damage = player attack damage x this ratio").defineInRange("scytheWaveDamageRatio", 0.6D, 0.0D, 100.0D);
        SCYTHE_COMBO_BASE_DAMAGE = builder.comment("棠郎古镰右键连斩每段基础伤害 / Ancestral Scythe right-click combo base damage per hit").defineInRange("scytheComboBaseDamage", 3.0D, 0.0D, 10000.0D);
        SCYTHE_COMBO_ATTACK_RATIO = builder.comment("棠郎古镰右键连斩每段追加的玩家攻击力比例 / Ancestral Scythe combo extra damage per hit as a ratio of player attack damage").defineInRange("scytheComboAttackRatio", 0.3D, 0.0D, 100.0D);
        SCYTHE_COMBO_COOLDOWN = builder.comment("棠郎古镰右键连斩冷却（tick） / Ancestral Scythe combo cooldown in ticks").defineInRange("scytheComboCooldown", 120, 0, 72000);
        SPEC = builder.build();
    }

    private KinoraConfig() {
    }
}
