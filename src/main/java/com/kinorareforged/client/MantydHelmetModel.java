package com.kinorareforged.client;

import com.kinorareforged.KinoraReforged;
import com.kinorareforged.ModItems;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

public final class MantydHelmetModel {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(KinoraReforged.id("modelmantydhelmet"), "main");

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.8F)), PartPose.offset(0.0F, 24.0F, 0.0F));
        head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(6, 16).addBox(-2.0F, -9.0F, -1.0F, 1.0F, 8.0F, 2.0F, new CubeDeformation(0.9F)), PartPose.offsetAndRotation(-5.0F, -8.0F, -4.0F, -0.7854F, 0.0F, -0.7854F));
        head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 16).addBox(1.0F, -9.0F, -1.0F, 1.0F, 8.0F, 2.0F, new CubeDeformation(0.9F)), PartPose.offsetAndRotation(5.0F, -8.0F, -4.0F, -0.7854F, 0.0F, 0.7854F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(LAYER_LOCATION, MantydHelmetModel::createBodyLayer);
    }

    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                ModelPart head = Minecraft.getInstance().getEntityModels().bakeLayer(LAYER_LOCATION).getChild("head");
                HumanoidModel<LivingEntity> model = new HumanoidModel<>(new ModelPart(List.of(), Map.of(
                        "head", head,
                        "hat", empty(),
                        "body", empty(),
                        "right_arm", empty(),
                        "left_arm", empty(),
                        "right_leg", empty(),
                        "left_leg", empty())));
                model.crouching = living.isShiftKeyDown();
                model.attackTime = original.attackTime;
                model.young = living.isBaby();
                return model;
            }
        }, ModItems.MANTYD_HELMET.get());
    }

    private static ModelPart empty() {
        return new ModelPart(List.of(), Map.of());
    }

    private MantydHelmetModel() {
    }
}
