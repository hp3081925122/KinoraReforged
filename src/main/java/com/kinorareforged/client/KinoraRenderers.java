package com.kinorareforged.client;

import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.kinorareforged.BossMantydEntity;
import com.kinorareforged.KinoraGeoEntity;
import com.kinorareforged.MantydBloomEntity;
import com.kinorareforged.MantydBossSpawnEntity;
import com.kinorareforged.MantydbossEchoEntity;
import com.kinorareforged.MantydbossdeadEntity;
import com.kinorareforged.MantydbosssitingEntity;
import com.kinorareforged.MantydbosssitingtwoEntity;
import com.kinorareforged.ModEntities;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class KinoraRenderers {
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BOSS_MANTYD.get(), context -> new Renderer<>(context, new Model<>("mantydboss", true), 1.2F, null, false));
        event.registerEntityRenderer(ModEntities.PETAL_PROJECTILE.get(), PetalArrowRenderer::new);
        event.registerEntityRenderer(ModEntities.SCYTHE_WAVE.get(), KinoraScytheClient.WaveRenderer::new);
        event.registerEntityRenderer(ModEntities.MANTYD_BLOOM.get(), context -> new Renderer<>(context, new Model<>("flowerentity", false), 1.2F, id("textures/entities/flowerentitytextureglow.png"), true));
        event.registerEntityRenderer(ModEntities.MANTYD_BOSS_SPAWN.get(), context -> new Renderer<>(context, new Model<>("mantydboss", true), 1.2F, null, true));
        event.registerEntityRenderer(ModEntities.MANTYDBOSSDEAD.get(), context -> new Renderer<>(context, new Model<>("mantydboss", false), 0.0F, null, false));
        event.registerEntityRenderer(ModEntities.MANTYDBOSSSITING.get(), context -> new Renderer<>(context, new Model<>("mantydboss", true), 0.0F, null, true));
        event.registerEntityRenderer(ModEntities.MANTYD_BOSS_ECHO.get(), context -> new Renderer<>(context, new Model<>("mantydboss", true), 1.2F, id("textures/entities/mantydbosstextureecho.png"), true));
        event.registerEntityRenderer(ModEntities.MANTYDBOSSSITINGTWO.get(), context -> new Renderer<>(context, new Model<>("mantydboss", true), 0.0F, null, true));
    }

    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath("kinora_reforged", path); }

    private static final class Model<T extends net.minecraft.world.entity.Mob & KinoraGeoEntity> extends GeoModel<T> {
        private final String model;
        private final boolean headTracking;
        private Model(String model, boolean headTracking) { this.model = model; this.headTracking = headTracking; }
        @Override public Identifier getModelResource(GeoRenderState state) { return id(model); }
        @Override public Identifier getTextureResource(GeoRenderState state) { return id("textures/entities/" + ((KinoraRenderState) state).getDataMap().getOrDefault(KinoraData.TEXTURE, "mantydbosstexture") + ".png"); }
        @Override public Identifier getAnimationResource(T entity) { return id(model.equals("flowerentity") ? "flowerentity" : "mantydboss"); }
    }

    private static final class Renderer<T extends net.minecraft.world.entity.Mob & KinoraGeoEntity> extends GeoEntityRenderer<T, KinoraRenderState> {
        private final boolean headTracking;
        private final boolean deathRotation;
        private Renderer(EntityRendererProvider.Context context, Model<T> model, float shadow, Identifier glow, boolean deathRotation) {
            super(context, model);
            this.shadowRadius = shadow;
            this.headTracking = model.headTracking;
            this.deathRotation = deathRotation;
            if (glow != null) withRenderLayer(new GlowLayer<>(this, glow));
        }
        @Override public KinoraRenderState createRenderState(T entity, Void context) { return new KinoraRenderState(); }
        @Override public void extractRenderState(T entity, KinoraRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            state.addGeckolibData(KinoraData.TEXTURE, entity.getTexture());
            state.addGeckolibData(com.kinorareforged.KinoraAnimationData.WALK_SPEED, state.walkAnimationSpeed);
        }
        @Override public void preRenderPass(RenderPassInfo<KinoraRenderState> pass, SubmitNodeCollector collector) {
            super.preRenderPass(pass, collector);
            if (headTracking) {
                pass.addBoneUpdater((info, snapshots) -> snapshots.ifPresent("head", head -> {
                    head.setRotX(pass.renderState().xRot * Mth.DEG_TO_RAD);
                    head.setRotY(pass.renderState().yRot * Mth.DEG_TO_RAD);
                }));
            }
        }
        @Override public RenderType getRenderType(KinoraRenderState state, Identifier texture) { return RenderTypes.entityTranslucent(texture); }
        @Override protected float getDeathMaxRotation(GeoRenderState state) { return deathRotation ? super.getDeathMaxRotation(state) : 0.0F; }
    }

    private static final class GlowLayer<T extends net.minecraft.world.entity.Mob & KinoraGeoEntity> extends GeoRenderLayer<T, Void, KinoraRenderState> {
        private final Identifier texture;
        private GlowLayer(GeoRenderer<T, Void, KinoraRenderState> renderer, Identifier texture) { super(renderer); this.texture = texture; }
        @Override public void submitRenderTask(RenderPassInfo<KinoraRenderState> pass, SubmitNodeCollector collector) {
            getRenderer().submitRenderTasks(pass, collector.order(1), RenderTypes.eyes(texture));
        }
    }

    private static final class PetalArrowRenderer extends ArrowRenderer<com.kinorareforged.PetalProjectileEntity, ArrowRenderState> {
        private PetalArrowRenderer(EntityRendererProvider.Context context) { super(context); }
        @Override public ArrowRenderState createRenderState() { return new ArrowRenderState(); }
        @Override protected Identifier getTextureLocation(ArrowRenderState state) { return Identifier.withDefaultNamespace("textures/entity/projectiles/arrow.png"); }
    }

    private static final class KinoraData {
        private static final DataTicket<String> TEXTURE = DataTicket.create("kinora_texture", String.class);
    }

    private KinoraRenderers() {}
}
