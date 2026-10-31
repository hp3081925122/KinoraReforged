package com.kinorareforged;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;

public final class MantydbosssitingEntity extends KinoraGeoPathfinder {
    private int talkAge;
    public MantydbosssitingEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.MAX_HEALTH, 400.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new LookAtPlayerGoal(this, ServerPlayer.class, 6.0F));
        goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 6.0F));
        goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    @Override
    protected String movementAnimation() {
        return "sit";
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider((id, inventory, ignored) -> new TalkMenu(id, inventory), Component.literal("Talk1")));
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel server)) return;
        if (!getPersistentData().getBoolean("KinoraTalkStarted")) getPersistentData().putBoolean("KinoraTalkStarted", true);
        talkAge++;
        if (talkAge == 40) say(server, "Why did you bring me back to life?");
        if (talkAge == 140) say(server, "don't worry, i won't harm you anymore");
        if (talkAge == 240) say(server, "is it because of the ancestral rune?");
        if (talkAge == 340) say(server, "...");
        if (talkAge == 440) say(server, "fine");
        if (talkAge == 500) {
            say(server, "I am not giving this to you out of kindness but out of loyalty to my daughter.");
            ItemEntity item = new ItemEntity(server, getX(), getY(), getZ(), new ItemStack(ModItems.PLANTATION_RUNE.get()));
            item.setPickUpDelay(2);
            server.addFreshEntity(item);
        }
        if (talkAge == 600) say(server, "now go");
        if (talkAge == 800) say(server, "More Content in v2.0");
    }

    private void say(ServerLevel server, String text) {
        server.getServer().getPlayerList().broadcastSystemMessage(Component.literal(text), false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("KinoraTalkAge", talkAge);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        talkAge = tag.getInt("KinoraTalkAge");
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(net.minecraft.world.entity.Entity entity) {
    }

    @Override
    protected void pushEntities() {
    }
}
