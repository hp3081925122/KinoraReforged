package com.kinorareforged;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, KinoraReforged.MOD_ID);
    public static final DeferredHolder<EntityType<?>, EntityType<BossMantydEntity>> BOSS_MANTYD = ENTITIES.register("boss_mantyd", () -> EntityType.Builder.of(BossMantydEntity::new, MobCategory.MONSTER).sized(1.0F, 2.5F).clientTrackingRange(64).updateInterval(3).build("kinora_reforged:boss_mantyd"));
    public static final DeferredHolder<EntityType<?>, EntityType<PetalProjectileEntity>> PETAL_PROJECTILE = ENTITIES.register("petal_projectile", () -> EntityType.Builder.<PetalProjectileEntity>of(PetalProjectileEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(64).updateInterval(1).build("kinora_reforged:petal_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<ScytheWaveEntity>> SCYTHE_WAVE = ENTITIES.register("scythe_wave", () -> EntityType.Builder.<ScytheWaveEntity>of(ScytheWaveEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).noSave().clientTrackingRange(64).updateInterval(1).build("kinora_reforged:scythe_wave"));
    public static final DeferredHolder<EntityType<?>, EntityType<MantydBloomEntity>> MANTYD_BLOOM = ENTITIES.register("mantyd_bloom", () -> EntityType.Builder.of(MantydBloomEntity::new, MobCategory.CREATURE).sized(1.0F, 0.8F).clientTrackingRange(64).updateInterval(3).build("kinora_reforged:mantyd_bloom"));
    public static final DeferredHolder<EntityType<?>, EntityType<MantydBossSpawnEntity>> MANTYD_BOSS_SPAWN = ENTITIES.register("mantyd_boss_spawn", () -> EntityType.Builder.of(MantydBossSpawnEntity::new, MobCategory.AMBIENT).fireImmune().sized(1.0F, 2.5F).clientTrackingRange(32).updateInterval(3).build("kinora_reforged:mantyd_boss_spawn"));
    public static final DeferredHolder<EntityType<?>, EntityType<MantydbossdeadEntity>> MANTYDBOSSDEAD = ENTITIES.register("mantydbossdead", () -> EntityType.Builder.of(MantydbossdeadEntity::new, MobCategory.AMBIENT).fireImmune().sized(1.8F, 0.5F).clientTrackingRange(32).updateInterval(3).build("kinora_reforged:mantydbossdead"));
    public static final DeferredHolder<EntityType<?>, EntityType<MantydbosssitingEntity>> MANTYDBOSSSITING = ENTITIES.register("mantydbosssiting", () -> EntityType.Builder.of(MantydbosssitingEntity::new, MobCategory.AMBIENT).fireImmune().sized(2.5F, 0.5F).clientTrackingRange(64).updateInterval(3).build("kinora_reforged:mantydbosssiting"));
    public static final DeferredHolder<EntityType<?>, EntityType<MantydbossEchoEntity>> MANTYD_BOSS_ECHO = ENTITIES.register("mantydboss_echo", () -> EntityType.Builder.of(MantydbossEchoEntity::new, MobCategory.AMBIENT).fireImmune().sized(1.0F, 2.5F).clientTrackingRange(64).updateInterval(3).build("kinora_reforged:mantydboss_echo"));
    public static final DeferredHolder<EntityType<?>, EntityType<MantydbosssitingtwoEntity>> MANTYDBOSSSITINGTWO = ENTITIES.register("mantydbosssitingtwo", () -> EntityType.Builder.of(MantydbosssitingtwoEntity::new, MobCategory.AMBIENT).fireImmune().sized(2.5F, 0.5F).clientTrackingRange(64).updateInterval(3).build("kinora_reforged:mantydbosssitingtwo"));

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
        bus.addListener(ModEntities::registerAttributes);
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(BOSS_MANTYD.get(), BossMantydEntity.createAttributes().build());
        event.put(MANTYD_BLOOM.get(), MantydBloomEntity.createAttributes().build());
        event.put(MANTYD_BOSS_SPAWN.get(), MantydBossSpawnEntity.createAttributes().build());
        event.put(MANTYDBOSSDEAD.get(), MantydbossdeadEntity.createAttributes().build());
        event.put(MANTYDBOSSSITING.get(), MantydbosssitingEntity.createAttributes().build());
        event.put(MANTYD_BOSS_ECHO.get(), MantydbossEchoEntity.createAttributes().build());
        event.put(MANTYDBOSSSITINGTWO.get(), MantydbosssitingtwoEntity.createAttributes().build());
    }

    private ModEntities() {
    }
}
