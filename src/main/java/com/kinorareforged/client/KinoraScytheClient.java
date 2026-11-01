package com.kinorareforged.client;

import com.kinorareforged.KinoraNetwork;
import com.kinorareforged.ModItems;
import com.kinorareforged.ScytheWaveEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.joml.Quaternionf;

public final class KinoraScytheClient {
    public static void onAttackKey(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!event.isAttack() || event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND || minecraft.player == null || minecraft.gameMode == null) return;
        if (!minecraft.player.getMainHandItem().is(ModItems.MANTYD_SCYTHE.get()) || minecraft.player.isSpectator()) return;
        HitResult hit = minecraft.hitResult;
        if (hit == null || hit.getType() != HitResult.Type.MISS || minecraft.player.getAttackStrengthScale(0.5F) < 0.9F) return;
        ClientPacketDistributor.sendToServer(KinoraNetwork.WavePayload.INSTANCE);
    }

    public static final class WaveState extends EntityRenderState {
        float fade, grow, yaw, pitch;
    }

    public static final class WaveRenderer extends EntityRenderer<ScytheWaveEntity, WaveState> {
        private static final Identifier MAIN = Identifier.fromNamespaceAndPath("kinora_reforged", "textures/particle/slash_2.png");
        private static final Identifier TRAIL = Identifier.fromNamespaceAndPath("kinora_reforged", "textures/particle/slash_3.png");
        public WaveRenderer(EntityRendererProvider.Context context) { super(context); }
        @Override public WaveState createRenderState() { return new WaveState(); }
        @Override public void extractRenderState(ScytheWaveEntity entity, WaveState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.fade = entity.fade(partialTick);
            state.grow = Mth.clamp((entity.tickCount + partialTick) / 3.0F, 0.4F, 1.0F);
            var motion = entity.getDeltaMovement();
            double horizontal = motion.horizontalDistance();
            state.yaw = horizontal > 1.0E-4D ? (float) Mth.atan2(-motion.x, motion.z) : entity.getYRot() * Mth.DEG_TO_RAD;
            state.pitch = (float) Mth.atan2(-motion.y, Math.max(horizontal, 1.0E-4D));
        }
        @Override public void submit(WaveState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
            pose.pushPose();
            pose.mulPose(new Quaternionf().rotateY(Mth.PI - state.yaw).rotateX(-state.pitch).rotateX(-Mth.HALF_PI));
            collector.submitCustomGeometry(pose, RenderTypes.entityTranslucentEmissive(TRAIL), (matrix, buffer) -> {
                quad(matrix, buffer, 0.0F, -0.9F, 1.5F * state.grow, 0.35F * state.fade);
                quad(matrix, buffer, 0.0F, -0.45F, 1.6F * state.grow, 0.6F * state.fade);
            });
            collector.submitCustomGeometry(pose, RenderTypes.entityTranslucentEmissive(MAIN), (matrix, buffer) -> quad(matrix, buffer, 0.0F, 0.0F, 1.7F * state.grow, state.fade));
            pose.popPose();
            super.submit(state, pose, collector, camera);
        }
        private static void quad(PoseStack.Pose pose, VertexConsumer buffer, float ox, float oy, float half, float alpha) {
            float[][] c = {{-1,-1,0,1},{1,-1,1,1},{1,1,1,0},{-1,1,0,0}};
            for (float[] v : c) vertex(buffer, pose, v, ox, oy, half, alpha, 1.0F);
            for (int i = 3; i >= 0; i--) vertex(buffer, pose, c[i], ox, oy, half, alpha, -1.0F);
        }
        private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float[] c, float ox, float oy, float half, float alpha, float normal) {
            buffer.addVertex(pose, ox + c[0] * half, oy + c[1] * half, 0.0F).setColor(1.0F, 1.0F, 1.0F, Mth.clamp(alpha, 0.0F, 1.0F)).setUv(c[2], c[3]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0F, 0.0F, normal);
        }
    }
    private KinoraScytheClient() {}
}
