package com.kinorareforged;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.TooltipFlag;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(KinoraReforged.MOD_ID);

    private static final TagKey<Item> NEVER_REPAIR = TagKey.create(Registries.ITEM, KinoraReforged.id("never_repair"));
    public static final ResourceKey<EquipmentAsset> MANTYD_HELMET_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, KinoraReforged.id("mantydhelmet"));
    public static final ArmorMaterial MANTYD_HELMET_MATERIAL = new ArmorMaterial(
            11, Map.of(ArmorType.HELMET, 4), 15, SoundEvents.ARMOR_EQUIP_TURTLE,
            2.0F, 0.1F, NEVER_REPAIR, MANTYD_HELMET_ASSET);
    private static final ToolMaterial MANTYD_SCYTHE_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_STONE_TOOL, 1, 4.0F, 7.0F, 15, NEVER_REPAIR);

    public static final DeferredItem<AncestralBagItem> ANCESTRAL_BAG = ITEMS.registerItem("ancestral_bag", p -> new AncestralBagItem(p.stacksTo(64).rarity(Rarity.EPIC)));
    public static final DeferredItem<MantydFluteItem> MANTYD_FLUTE = ITEMS.registerItem("mantyd_flute", p -> new MantydFluteItem(p.stacksTo(64).rarity(Rarity.EPIC)));
    public static final DeferredItem<MantydMedicineItem> MANTYD_MEDICINE = ITEMS.registerItem("mantyd_medicine", p -> new MantydMedicineItem(p.stacksTo(64).rarity(Rarity.EPIC)));
    public static final DeferredItem<PlantationRuneItem> PLANTATION_RUNE = ITEMS.registerItem("plantation_rune", p -> new PlantationRuneItem(p.stacksTo(64).rarity(Rarity.EPIC)));
    public static final DeferredItem<MantydScytheItem> MANTYD_SCYTHE = ITEMS.registerItem("mantyd_scythe", MantydScytheItem::new);
    public static final DeferredItem<MantydHelmetItem> MANTYD_HELMET = ITEMS.registerItem("mantydhelmet_helmet", p -> new MantydHelmetItem(p));
    public static final DeferredItem<SpawnEggItem> BOSS_MANTYD_SPAWN_EGG = ITEMS.registerItem("boss_mantyd_spawn_egg", p -> new SpawnEggItem(p.spawnEgg(ModEntities.BOSS_MANTYD.get())));
    public static final DeferredItem<SpawnEggItem> MANTYD_BLOOM_SPAWN_EGG = ITEMS.registerItem("mantyd_bloom_spawn_egg", p -> new SpawnEggItem(p.spawnEgg(ModEntities.MANTYD_BLOOM.get())));
    public static final DeferredItem<SpawnEggItem> MANTYD_BOSS_SPAWN_SPAWN_EGG = ITEMS.registerItem("mantyd_boss_spawn_spawn_egg", p -> new SpawnEggItem(p.spawnEgg(ModEntities.MANTYD_BOSS_SPAWN.get())));
    public static final DeferredItem<SpawnEggItem> MANTYDBOSSDEAD_SPAWN_EGG = ITEMS.registerItem("mantydbossdead_spawn_egg", p -> new SpawnEggItem(p.spawnEgg(ModEntities.MANTYDBOSSDEAD.get())));
    public static final DeferredItem<SpawnEggItem> MANTYDBOSSSITING_SPAWN_EGG = ITEMS.registerItem("mantydbosssiting_spawn_egg", p -> new SpawnEggItem(p.spawnEgg(ModEntities.MANTYDBOSSSITING.get())));
    public static final DeferredItem<SpawnEggItem> MANTYD_BOSS_ECHO_SPAWN_EGG = ITEMS.registerItem("mantydboss_echo_spawn_egg", p -> new SpawnEggItem(p.spawnEgg(ModEntities.MANTYD_BOSS_ECHO.get())));
    public static final DeferredItem<SpawnEggItem> MANTYDBOSSSITINGTWO_SPAWN_EGG = ITEMS.registerItem("mantydbosssitingtwo_spawn_egg", p -> new SpawnEggItem(p.spawnEgg(ModEntities.MANTYDBOSSSITINGTWO.get())));

    private static Item.Properties epicStackProperties() { return new Item.Properties().stacksTo(64).rarity(Rarity.EPIC); }

    private static class DescribedItem extends Item {
        private final String[] descriptionKeys;
        private DescribedItem(Properties properties, String... descriptionKeys) { super(properties); this.descriptionKeys = descriptionKeys; }
        @Override public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
            super.appendHoverText(stack, context, display, lines, flag);
            for (String key : descriptionKeys) lines.accept(Component.translatable(key));
        }
    }

    public static final class AncestralBagItem extends DescribedItem {
        private AncestralBagItem(Properties properties) { super(properties, "item.kinora_reforged.ancestral_bag.description"); }
        @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (level.isClientSide()) com.kinorareforged.client.KinoraClientEffects.displayItemActivation(new ItemStack(this));
            if (!level.isClientSide() && level instanceof ServerLevel server) {
                stack.shrink(1);
                drop(server, player, new ItemStack(MANTYD_SCYTHE.get()));
                drop(server, player, new ItemStack(MANTYD_HELMET.get()));
                if (level.getRandom().nextDouble() <= 0.5D) {
                    drop(server, player, new ItemStack(Items.NETHER_STAR));
                    if (level.getRandom().nextDouble() <= 0.5D) drop(server, player, new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE));
                }
                if (level.getRandom().nextDouble() <= 0.9D) {
                    for (int i = 0; i < 4; i++) drop(server, player, new ItemStack(Items.DIAMOND));
                    if (level.getRandom().nextDouble() <= 0.4D) {
                        drop(server, player, new ItemStack(Items.DIAMOND)); drop(server, player, new ItemStack(Items.DIAMOND));
                        if (level.getRandom().nextDouble() <= 0.4D) drop(server, player, new ItemStack(Items.DIAMOND));
                    }
                }
                if (level.getRandom().nextDouble() <= 0.5D) drop(server, player, new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE));
            }
            return InteractionResult.SUCCESS;
        }
        private static void drop(ServerLevel level, Player player, ItemStack stack) {
            ItemEntity entity = new ItemEntity(level, player.getX(), player.getY() + 0.5D, player.getZ(), stack);
            entity.setPickUpDelay(0); entity.setUnlimitedLifetime(); level.addFreshEntity(entity);
        }
    }

    public static final class MantydFluteItem extends DescribedItem {
        private MantydFluteItem(Properties properties) { super(properties, "item.kinora_reforged.mantyd_flute.description"); }
        @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (player.getCooldowns().isOnCooldown(stack)) return InteractionResult.FAIL;
            player.getCooldowns().addCooldown(stack, 2000);
            if (!level.isClientSide()) {
                level.playSound(null, player.blockPosition(), ModSounds.MANTYD_FLUTE_SOUND.get(), net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F, 1.0F);
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOWNESS, 80, 2, false, false));
                KinoraPlayerData.set(player, "KinoraFluteTicks", 80);
                KinoraPlayerData.set(player, "KinoraFluteDimension", player.level().dimension().identifier().toString());
            }
            return InteractionResult.SUCCESS;
        }
    }

    public static final class MantydMedicineItem extends DescribedItem {
        private MantydMedicineItem(Properties properties) { super(properties, "item.kinora_reforged.mantyd_medicine.description_0", "item.kinora_reforged.mantyd_medicine.description_1"); }
    }
    public static final class PlantationRuneItem extends DescribedItem {
        private PlantationRuneItem(Properties properties) { super(properties, "item.kinora_reforged.plantation_rune.description"); }
    }

    public static final class MantydScytheItem extends Item {
        private MantydScytheItem(Item.Properties properties) { super(properties.sword(MANTYD_SCYTHE_MATERIAL, 3.0F, -2.6F).component(DataComponents.MAX_DAMAGE, null).component(DataComponents.DAMAGE, null).stacksTo(1).fireResistant().component(DataComponents.UNBREAKABLE, Unit.INSTANCE)); }
        @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (hand != InteractionHand.MAIN_HAND || player.getCooldowns().isOnCooldown(stack)) return InteractionResult.PASS;
            if (!level.isClientSide()) KinoraScythe.startCombo(player);
            return InteractionResult.SUCCESS;
        }
        @Override public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
            super.appendHoverText(stack, context, display, lines, flag);
            lines.accept(Component.translatable("item.kinora_reforged.mantyd_scythe.wave").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
            lines.accept(Component.translatable("item.kinora_reforged.mantyd_scythe.combo", KinoraConfig.SCYTHE_COMBO_COOLDOWN.get() / 20.0D).withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
        }
    }

    public static final class MantydHelmetItem extends Item {
        private MantydHelmetItem(Item.Properties properties) { super(properties.humanoidArmor(MANTYD_HELMET_MATERIAL, ArmorType.HELMET).component(DataComponents.MAX_DAMAGE, null).component(DataComponents.DAMAGE, null).stacksTo(1).fireResistant()); }
        @Override public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
            super.appendHoverText(stack, context, display, lines, flag);
            lines.accept(Component.translatable("item.kinora_reforged.mantydhelmet_helmet.description"));
        }
    }

    private ModItems() {}
}
