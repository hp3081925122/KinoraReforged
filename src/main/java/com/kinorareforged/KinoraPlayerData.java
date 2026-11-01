package com.kinorareforged;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class KinoraPlayerData implements net.neoforged.neoforge.common.util.ValueIOSerializable {
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<KinoraPlayerData>> TYPE = KinoraReforged.ATTACHMENTS.register("player_data", () -> AttachmentType.serializable(KinoraPlayerData::new).build());
    private int fluteTicks;
    private String fluteDimension = "";
    private int comboAge;
    private boolean comboActive;
    private long waveTick;

    public static DeferredHolder<AttachmentType<?>, AttachmentType<KinoraPlayerData>> init() {
        return TYPE;
    }

    public static KinoraPlayerData get(Player player) { return player.getData(TYPE); }
    public static void set(Player player, String key, int value) { KinoraPlayerData d = get(player); if (key.equals("KinoraFluteTicks")) d.fluteTicks = value; else if (key.equals("KinoraScytheCombo")) d.comboAge = value; }
    public static void set(Player player, String key, long value) { if (key.equals("KinoraScytheWaveTick")) get(player).waveTick = value; }
    public static void set(Player player, String key, String value) { if (key.equals("KinoraFluteDimension")) get(player).fluteDimension = value; }
    public int fluteTicks() { return fluteTicks; }
    public void fluteTicks(int value) { fluteTicks = value; }
    public String fluteDimension() { return fluteDimension; }
    public int comboAge() { return comboAge; }
    public void comboAge(int value) { comboAge = value; }
    public boolean comboActive() { return comboActive; }
    public void comboActive(boolean value) { comboActive = value; }
    public long waveTick() { return waveTick; }
    public void waveTick(long value) { waveTick = value; }
    @Override public void serialize(ValueOutput output) { output.putInt("KinoraFluteTicks", fluteTicks); output.putString("KinoraFluteDimension", fluteDimension); output.putInt("KinoraScytheCombo", comboAge); output.putBoolean("KinoraScytheComboActive", comboActive); output.putLong("KinoraScytheWaveTick", waveTick); }
    @Override public void deserialize(ValueInput input) { fluteTicks = input.getIntOr("KinoraFluteTicks", 0); fluteDimension = input.getStringOr("KinoraFluteDimension", ""); comboAge = input.getIntOr("KinoraScytheCombo", 0); comboActive = input.getBooleanOr("KinoraScytheComboActive", false); waveTick = input.getLongOr("KinoraScytheWaveTick", 0L); }
}
