package com.kinorareforged;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public final class PetalProjectileEntity extends AbstractArrow implements ItemSupplier {
    private double impactKnockback;

    public PetalProjectileEntity(EntityType<? extends PetalProjectileEntity> type, Level level) {
        super(type, level);
        setBaseDamage(5.0D);
        impactKnockback = 1.0D;
    }

    public PetalProjectileEntity(EntityType<? extends PetalProjectileEntity> type, LivingEntity owner, Level level) {
        super(type, owner, level, ItemStack.EMPTY, null);
        setBaseDamage(5.0D);
        impactKnockback = 1.0D;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.AIR);
    }

    @Override
    protected void doKnockback(LivingEntity target, DamageSource source) {
        double resistance = Math.max(0.0D, 1.0D - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        var impulse = getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize().scale(impactKnockback * resistance);
        if (impulse.lengthSqr() > 0.0D) {
            target.push(impulse.x, 0.1D * resistance, impulse.z);
            target.hurtMarked = true;
        }
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return ItemStack.EMPTY;
    }
}
