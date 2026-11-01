package com.kinorareforged;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(KinoraReforged.MOD_ID);
    public static final DeferredHolder<EntityType<?>, EntityType<BossMantydEntity>> BOSS_MANTYD = ENTITIES.registerEntityType("boss_mantyd", BossMantydEntity::new, MobCategory.MONSTER, b -> b.sized(1.0F, 2.5F).clientTrackingRange(64).updateInterval(3));
    public static final DeferredHolder<EntityType<?>, EntityType<PetalProjectileEntity>> PETAL_PROJECTILE = ENTITIES.registerEntityType("petal_projectile", PetalProjectileEntity::new, MobCategory.MISC, b -> b.sized(0.5F, 0.5F).clientTrackingRange(64).updateInterval(1));
    public static final DeferredHolder<EntityType<?>, EntityType<ScytheWaveEntity>> SCYTHE_WAVE = ENTITIES.registerEntityType("scythe_wave", ScytheWaveEntity::new, MobCategory.MISC, b -> b.sized(0.5F, 0.5F).noSave().clientTrackingRange(64).updateInterval(1));
    public static final DeferredHolder<EntityType<?>, EntityType<MantydBloomEntity>> MANTYD_BLOOM = ENTITIES.registerEntityType("mantyd_bloom", MantydBloomEntity::new, MobCategory.CREATURE, b -> b.sized(1.0F, 0.8F).clientTrackingRange(64).updateInterval(3));
    public static final DeferredHolder<EntityType<?>, EntityType<MantydBossSpawnEntity>> MANTYD_BOSS_SPAWN = ENTITIES.registerEntityType("mantyd_boss_spawn", MantydBossSpawnEntity::new, MobCategory.AMBIENT, b -> b.fireImmune().sized(1.0F, 2.5F).clientTrackingRange(32).updateInterval(3));
    public static final DeferredHolder<EntityType<?>, EntityType<MantydbossdeadEntity>> MANTYDBOSSDEAD = ENTITIES.registerEntityType("mantydbossdead", MantydbossdeadEntity::new, MobCategory.AMBIENT, b -> b.fireImmune().sized(1.8F, 0.5F).clientTrackingRange(32).updateInterval(3));
    public static final DeferredHolder<EntityType<?>, EntityType<MantydbosssitingEntity>> MANTYDBOSSSITING = ENTITIES.registerEntityType("mantydbosssiting", MantydbosssitingEntity::new, MobCategory.AMBIENT, b -> b.fireImmune().sized(2.5F, 0.5F).clientTrackingRange(64).updateInterval(3));
    public static final DeferredHolder<EntityType<?>, EntityType<MantydbossEchoEntity>> MANTYD_BOSS_ECHO = ENTITIES.registerEntityType("mantydboss_echo", MantydbossEchoEntity::new, MobCategory.AMBIENT, b -> b.fireImmune().sized(1.0F, 2.5F).clientTrackingRange(64).updateInterval(3));
    public static final DeferredHolder<EntityType<?>, EntityType<MantydbosssitingtwoEntity>> MANTYDBOSSSITINGTWO = ENTITIES.registerEntityType("mantydbosssitingtwo", MantydbosssitingtwoEntity::new, MobCategory.AMBIENT, b -> b.fireImmune().sized(2.5F, 0.5F).clientTrackingRange(64).updateInterval(3));
    public static void register(IEventBus bus) { ENTITIES.register(bus); bus.addListener(ModEntities::registerAttributes); }
    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(BOSS_MANTYD.get(), BossMantydEntity.createAttributes().build());
        event.put(MANTYD_BLOOM.get(), MantydBloomEntity.createAttributes().build());
        event.put(MANTYD_BOSS_SPAWN.get(), MantydBossSpawnEntity.createAttributes().build());
        event.put(MANTYDBOSSDEAD.get(), MantydbossdeadEntity.createAttributes().build());
        event.put(MANTYDBOSSSITING.get(), MantydbosssitingEntity.createAttributes().build());
        event.put(MANTYD_BOSS_ECHO.get(), MantydbossEchoEntity.createAttributes().build());
        event.put(MANTYDBOSSSITINGTWO.get(), MantydbosssitingtwoEntity.createAttributes().build());
    }
    private ModEntities() {}
}
