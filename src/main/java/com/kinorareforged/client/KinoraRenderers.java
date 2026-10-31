package com.kinorareforged.client;

import com.kinorareforged.BossMantydEntity;
import com.kinorareforged.KinoraGeoEntity;
import com.kinorareforged.KinoraGeoMonster;
import com.kinorareforged.KinoraGeoPathfinder;
import com.kinorareforged.MantydBloomEntity;
import com.kinorareforged.MantydBossSpawnEntity;
import com.kinorareforged.MantydbossEchoEntity;
import com.kinorareforged.MantydbossdeadEntity;
import com.kinorareforged.MantydbosssitingEntity;
import com.kinorareforged.MantydbosssitingtwoEntity;
import com.kinorareforged.ModEntities;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public final class KinoraRenderers {
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BOSS_MANTYD.get(), context -> new Renderer<>(context, new Model<>("mantydboss", true), 1.2F, null, false));
        event.registerEntityRenderer(ModEntities.PETAL_PROJECTILE.get(), PetalArrowRenderer::new);
        event.registerEntityRenderer(ModEntities.MANTYD_BLOOM.get(), context -> new Renderer<>(context, new Model<>("flowerentity", false), 1.2F, id("textures/entities/flowerentitytextureglow.png"), true));
        event.registerEntityRenderer(ModEntities.MANTYD_BOSS_SPAWN.get(), context -> new Renderer<>(context, new Model<>("mantydboss", true), 1.2F, null, true));
        event.registerEntityRenderer(ModEntities.MANTYDBOSSDEAD.get(), context -> new Renderer<>(context, new Model<>("mantydboss", false), 0.0F, null, false));
        event.registerEntityRenderer(ModEntities.MANTYDBOSSSITING.get(), context -> new Renderer<>(context, new Model<>("mantydboss", true), 0.0F, null, true));
        event.registerEntityRenderer(ModEntities.MANTYD_BOSS_ECHO.get(), context -> new Renderer<>(context, new Model<>("mantydboss", true), 1.2F, id("textures/entities/mantydbosstextureecho.png"), true));
        event.registerEntityRenderer(ModEntities.MANTYDBOSSSITINGTWO.get(), context -> new Renderer<>(context, new Model<>("mantydboss", true), 0.0F, null, true));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("kinora_reforged", path);
    }

    private static final class Model<T extends net.minecraft.world.entity.Mob & KinoraGeoEntity> extends GeoModel<T> {
        private final String model;
        private final boolean headTracking;

        private Model(String model, boolean headTracking) {
            this.model = model;
            this.headTracking = headTracking;
        }

        @Override
        public ResourceLocation getModelResource(T entity) {
            return id("geo/" + model + ".geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(T entity) {
            return id("textures/entities/" + entity.getTexture() + ".png");
        }

        @Override
        public ResourceLocation getAnimationResource(T entity) {
            return id("animations/" + (model.equals("flowerentity") ? "flowerentity" : "mantydboss") + ".animation.json");
        }

        @Override
        public void setCustomAnimations(T entity, long instanceId, AnimationState<T> state) {
            super.setCustomAnimations(entity, instanceId, state);
            if (!headTracking) {
                return;
            }
            var head = getAnimationProcessor().getBone("head");
            var data = state.getData(software.bernie.geckolib.constant.DataTickets.ENTITY_MODEL_DATA);
            if (head != null && data != null) {
                head.setRotX(data.headPitch() * net.minecraft.util.Mth.DEG_TO_RAD);
                head.setRotY(data.netHeadYaw() * net.minecraft.util.Mth.DEG_TO_RAD);
            }
        }
    }

    private static final class Renderer<T extends net.minecraft.world.entity.Mob & KinoraGeoEntity> extends GeoEntityRenderer<T> {
        private final boolean deathRotation;

        private Renderer(EntityRendererProvider.Context context, GeoModel<T> model, float shadow, ResourceLocation glow, boolean deathRotation) {
            super(context, model);
            shadowRadius = shadow;
            this.deathRotation = deathRotation;
            if (glow != null) addRenderLayer(new GlowLayer<>(this, glow));
        }

        @Override
        public RenderType getRenderType(T entity, ResourceLocation texture, MultiBufferSource buffers, float partialTick) {
            return RenderType.entityTranslucent(getTextureLocation(entity));
        }

        @Override
        protected float getDeathMaxRotation(T entity) {
            return deathRotation ? super.getDeathMaxRotation(entity) : 0.0F;
        }
    }

    private static final class GlowLayer<T extends net.minecraft.world.entity.Mob & KinoraGeoEntity> extends GeoRenderLayer<T> {
        private final ResourceLocation texture;

        private GlowLayer(GeoRenderer<T> renderer, ResourceLocation texture) {
            super(renderer);
            this.texture = texture;
        }

        @Override
        public void render(PoseStack pose, T entity, BakedGeoModel model, RenderType type, MultiBufferSource buffers, VertexConsumer vertices, float partial, int light, int overlay) {
            RenderType glow = RenderType.eyes(texture);
            getRenderer().reRender(model, pose, buffers, entity, glow, buffers.getBuffer(glow), partial, light, OverlayTexture.NO_OVERLAY, -1);
        }
    }

    private static final class PetalArrowRenderer extends ArrowRenderer<com.kinorareforged.PetalProjectileEntity> {
        private PetalArrowRenderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public ResourceLocation getTextureLocation(com.kinorareforged.PetalProjectileEntity entity) {
            return ResourceLocation.withDefaultNamespace("textures/entity/projectiles/arrow.png");
        }
    }

    private KinoraRenderers() {
    }
}
