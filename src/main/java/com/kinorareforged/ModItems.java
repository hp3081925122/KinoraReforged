package com.kinorareforged;

import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(KinoraReforged.MOD_ID);
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, KinoraReforged.MOD_ID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> MANTYD_HELMET_MATERIAL = ARMOR_MATERIALS.register("mantydhelmet", () -> new ArmorMaterial(
            Map.of(ArmorItem.Type.HELMET, 4),
            15,
            SoundEvents.ARMOR_EQUIP_TURTLE,
            () -> Ingredient.EMPTY,
            List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(KinoraReforged.MOD_ID, "mantydhelmet"))),
            2.0F,
            0.1F));

    public static final DeferredHolder<Item, AncestralBagItem> ANCESTRAL_BAG = ITEMS.register("ancestral_bag", () -> new AncestralBagItem(epicStackProperties()));
    public static final DeferredHolder<Item, MantydFluteItem> MANTYD_FLUTE = ITEMS.register("mantyd_flute", () -> new MantydFluteItem(epicStackProperties()));
    public static final DeferredHolder<Item, MantydMedicineItem> MANTYD_MEDICINE = ITEMS.register("mantyd_medicine", () -> new MantydMedicineItem(epicStackProperties()));
    public static final DeferredHolder<Item, PlantationRuneItem> PLANTATION_RUNE = ITEMS.register("plantation_rune", () -> new PlantationRuneItem(epicStackProperties()));
    public static final DeferredHolder<Item, MantydScytheItem> MANTYD_SCYTHE = ITEMS.register("mantyd_scythe", () -> new MantydScytheItem());
    public static final DeferredHolder<Item, MantydHelmetItem> MANTYD_HELMET = ITEMS.register("mantydhelmet_helmet", () -> new MantydHelmetItem(MANTYD_HELMET_MATERIAL));
    public static final DeferredHolder<Item, Item> BOSS_MANTYD_SPAWN_EGG = ITEMS.register("boss_mantyd_spawn_egg", () -> new DeferredSpawnEggItem(ModEntities.BOSS_MANTYD, -3980848, -12469716, new Item.Properties()));
    public static final DeferredHolder<Item, Item> MANTYD_BLOOM_SPAWN_EGG = ITEMS.register("mantyd_bloom_spawn_egg", () -> new DeferredSpawnEggItem(ModEntities.MANTYD_BLOOM, -26374, -9728, new Item.Properties()));
    public static final DeferredHolder<Item, Item> MANTYD_BOSS_SPAWN_SPAWN_EGG = ITEMS.register("mantyd_boss_spawn_spawn_egg", () -> new DeferredSpawnEggItem(ModEntities.MANTYD_BOSS_SPAWN, -2265900, -13921492, new Item.Properties()));
    public static final DeferredHolder<Item, Item> MANTYDBOSSDEAD_SPAWN_EGG = ITEMS.register("mantydbossdead_spawn_egg", () -> new DeferredSpawnEggItem(ModEntities.MANTYDBOSSDEAD, -2265900, -13921492, new Item.Properties()));
    public static final DeferredHolder<Item, Item> MANTYDBOSSSITING_SPAWN_EGG = ITEMS.register("mantydbosssiting_spawn_egg", () -> new DeferredSpawnEggItem(ModEntities.MANTYDBOSSSITING, -2265900, -13921492, new Item.Properties()));
    public static final DeferredHolder<Item, Item> MANTYD_BOSS_ECHO_SPAWN_EGG = ITEMS.register("mantydboss_echo_spawn_egg", () -> new DeferredSpawnEggItem(ModEntities.MANTYD_BOSS_ECHO, -13882172, -12613889, new Item.Properties()));
    public static final DeferredHolder<Item, Item> MANTYDBOSSSITINGTWO_SPAWN_EGG = ITEMS.register("mantydbosssitingtwo_spawn_egg", () -> new DeferredSpawnEggItem(ModEntities.MANTYDBOSSSITINGTWO, -2265900, -13921492, new Item.Properties()));

    private static Item.Properties epicStackProperties() {
        return new Item.Properties().stacksTo(64).rarity(Rarity.EPIC);
    }

    private static class DescribedItem extends Item {
        private final String[] descriptionKeys;

        private DescribedItem(Properties properties, String... descriptionKeys) {
            super(properties);
            this.descriptionKeys = descriptionKeys;
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
            super.appendHoverText(stack, context, lines, flag);
            for (String key : descriptionKeys) {
                lines.add(Component.translatable(key));
            }
        }
    }

    public static final class AncestralBagItem extends DescribedItem {
        private AncestralBagItem(Properties properties) {
            super(properties, "item.kinora_reforged.ancestral_bag.description");
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (!level.isClientSide && stack.is(this)) {
                stack.shrink(1);
                ServerLevel server = (ServerLevel) level;
                drop(server, player, new ItemStack(MANTYD_SCYTHE.get()));
                drop(server, player, new ItemStack(MANTYD_HELMET.get()));
                if (level.random.nextDouble() <= 0.5D) {
                    drop(server, player, new ItemStack(Items.NETHER_STAR));
                    if (level.random.nextDouble() <= 0.5D) {
                        drop(server, player, new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE));
                    }
                }
                if (level.random.nextDouble() <= 0.9D) {
                    for (int i = 0; i < 4; i++) {
                        drop(server, player, new ItemStack(Items.DIAMOND));
                    }
                    if (level.random.nextDouble() <= 0.4D) {
                        drop(server, player, new ItemStack(Items.DIAMOND));
                        drop(server, player, new ItemStack(Items.DIAMOND));
                        if (level.random.nextDouble() <= 0.4D) {
                            drop(server, player, new ItemStack(Items.DIAMOND));
                        }
                    }
                }
                if (level.random.nextDouble() <= 0.5D) {
                    drop(server, player, new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE));
                }
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        private static void drop(ServerLevel level, Player player, ItemStack stack) {
            ItemEntity entity = new ItemEntity(level, player.getX(), player.getY() + 0.5D, player.getZ(), stack);
            entity.setPickUpDelay(0);
            entity.setUnlimitedLifetime();
            level.addFreshEntity(entity);
        }
    }

    public static final class MantydFluteItem extends DescribedItem {
        private MantydFluteItem(Properties properties) {
            super(properties, "item.kinora_reforged.mantyd_flute.description");
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (player.getCooldowns().isOnCooldown(this)) {
                return InteractionResultHolder.fail(stack);
            }
            player.getCooldowns().addCooldown(this, 2000);
            if (!level.isClientSide) {
                level.playSound(null, player.blockPosition(), ModSounds.MANTYD_FLUTE_SOUND.get(), net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 80, 2, false, false));
                player.getPersistentData().putInt("KinoraFluteTicks", 80);
                player.getPersistentData().putString("KinoraFluteDimension", player.level().dimension().location().toString());
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
    }

    public static final class MantydMedicineItem extends DescribedItem {
        private MantydMedicineItem(Properties properties) {
            super(properties, "item.kinora_reforged.mantyd_medicine.description_0", "item.kinora_reforged.mantyd_medicine.description_1");
        }
    }

    public static final class PlantationRuneItem extends DescribedItem {
        private PlantationRuneItem(Properties properties) {
            super(properties, "item.kinora_reforged.plantation_rune.description");
        }
    }

    public static final class MantydScytheItem extends SwordItem {
        private MantydScytheItem() {
            super(new MantydScytheTier(), new Item.Properties().stacksTo(1).fireResistant().attributes(SwordItem.createAttributes(new MantydScytheTier(), 3, -2.6F)));
        }
    }

    public static final class MantydHelmetItem extends ArmorItem {
        private MantydHelmetItem(DeferredHolder<ArmorMaterial, ArmorMaterial> material) {
            super(material, Type.HELMET, new Item.Properties().stacksTo(1).fireResistant());
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
            super.appendHoverText(stack, context, lines, flag);
            lines.add(Component.translatable("item.kinora_reforged.mantydhelmet_helmet.description"));
        }

        @Override
        public ResourceLocation getArmorTexture(ItemStack stack, net.minecraft.world.entity.Entity entity, net.minecraft.world.entity.EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
            return ResourceLocation.fromNamespaceAndPath(KinoraReforged.MOD_ID, "textures/entities/mantydhelmettexture.png");
        }
    }

    private static final class MantydScytheTier implements Tier {
        @Override
        public int getUses() {
            return 0;
        }

        @Override
        public float getSpeed() {
            return 4.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 7.0F;
        }

        @Override
        public TagKey<Block> getIncorrectBlocksForDrops() {
            return BlockTags.INCORRECT_FOR_STONE_TOOL;
        }

        @Override
        public int getEnchantmentValue() {
            return 15;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY;
        }
    }

    private ModItems() {
    }
}
