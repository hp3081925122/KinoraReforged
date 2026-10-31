package com.kinorareforged;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, KinoraReforged.MOD_ID);
    public static final DeferredHolder<SoundEvent, SoundEvent> KINORA_BOSS_THEME = register("kinorabosstheme");
    public static final DeferredHolder<SoundEvent, SoundEvent> MANTYD_INTRO_FIGHT = register("mantydintrofight");
    public static final DeferredHolder<SoundEvent, SoundEvent> KINORA_INTRO_BETTER = register("kinoraintrobetter");
    public static final DeferredHolder<SoundEvent, SoundEvent> MANTYD_FLUTE_SOUND = register("mantydflutesound");
    public static final DeferredHolder<SoundEvent, SoundEvent> MANTYD_TALK_1 = register("mantydtalk1");
    public static final DeferredHolder<SoundEvent, SoundEvent> MANTYD_TALK_2 = register("mantydtalk2");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String path) {
        return SOUNDS.register(path, () -> SoundEvent.createVariableRangeEvent(KinoraReforged.id(path)));
    }

    private ModSounds() {
    }
}
