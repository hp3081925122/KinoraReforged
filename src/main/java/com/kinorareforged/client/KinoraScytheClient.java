package com.kinorareforged.client;

import com.kinorareforged.KinoraNetwork;
import com.kinorareforged.ModItems;
import com.kinorareforged.ScytheWaveEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Quaternionf;

public final class KinoraScytheClient {
    public static void onAttackKey(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!event.isAttack() || event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND || minecraft.player == null || minecraft.gameMode == null) {
            return;
        }
        if (!minecraft.player.getMainHandItem().is(ModItems.MANTYD_SCYTHE.get()) || minecraft.player.isSpectator()) {
            return;
        }
        HitResult hit = minecraft.hitResult;
        if (hit == null || hit.getType() != HitResult.Type.MISS || minecraft.player.getAttackStrengthScale(0.5F) < 0.9F) {
            return;
        }
        PacketDistributor.sendToServer(KinoraNetwork.WavePayload.INSTANCE);
    }

    public static final class WaveRenderer extends EntityRenderer<ScytheWaveEntity> {
        private static final ResourceLocation MAIN = ResourceLocation.fromNamespaceAndPath("kinora_reforged", "textures/particle/slash_2.png");
        private static final ResourceLocation TRAIL = ResourceLocation.fromNamespaceAndPath("kinora_reforged", "textures/particle/slash_3.png");

        public WaveRenderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public void render(ScytheWaveEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
            float fade = entity.fade(partialTick);
            float grow = Mth.clamp((entity.tickCount + partialTick) / 3.0F, 0.4F, 1.0F);
            float yaw = entity.getYRot() * Mth.DEG_TO_RAD;
            poseStack.pushPose();
            poseStack.mulPose(new Quaternionf().rotateY(Mth.PI - yaw).rotateX(-Mth.HALF_PI));
            quad(poseStack, buffers.getBuffer(RenderType.entityTranslucentEmissive(TRAIL)), 0.0F, -0.9F, 1.5F * grow, 0.35F * fade);
            quad(poseStack, buffers.getBuffer(RenderType.entityTranslucentEmissive(TRAIL)), 0.0F, -0.45F, 1.6F * grow, 0.6F * fade);
            quad(poseStack, buffers.getBuffer(RenderType.entityTranslucentEmissive(MAIN)), 0.0F, 0.0F, 1.7F * grow, fade);
            poseStack.popPose();
            super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
        }

        private static void quad(PoseStack poseStack, VertexConsumer buffer, float ox, float oy, float half, float alpha) {
            PoseStack.Pose pose = poseStack.last();
            float[][] corners = {{-1.0F, -1.0F, 0.0F, 1.0F}, {1.0F, -1.0F, 1.0F, 1.0F}, {1.0F, 1.0F, 1.0F, 0.0F}, {-1.0F, 1.0F, 0.0F, 0.0F}};
            for (int i = 0; i < 4; i++) {
                vertex(buffer, pose, corners[i], ox, oy, half, alpha, 1.0F);
            }
            for (int i = 3; i >= 0; i--) {
                vertex(buffer, pose, corners[i], ox, oy, half, alpha, -1.0F);
            }
        }

        private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float[] c, float ox, float oy, float half, float alpha, float normal) {
            buffer.addVertex(pose, ox + c[0] * half, oy + c[1] * half, 0.0F)
                    .setColor(1.0F, 1.0F, 1.0F, Mth.clamp(alpha, 0.0F, 1.0F))
                    .setUv(c[2], c[3])
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(0xF000F0)
                    .setNormal(pose, 0.0F, 0.0F, normal);
        }

        @Override
        public ResourceLocation getTextureLocation(ScytheWaveEntity entity) {
            return MAIN;
        }
    }

    private KinoraScytheClient() {
    }
}
