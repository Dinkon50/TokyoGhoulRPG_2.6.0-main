package com.tokyoghoul.rpg.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tokyoghoul.rpg.capability.GhoulData;
import com.tokyoghoul.rpg.v2.KaguneType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class PlayerKaguneRenderer {
    private static KaguneModel MODEL;
    private static boolean previousActive;
    private static float emergence = 0f;
    private static int grappleTicks = 0;
    private static int dashTicks = 0;
    private static final Map<UUID, float[]> SMOOTH_VELOCITY = new HashMap<>();
    private static final Map<UUID, float[]> ANIMATION_STATE = new HashMap<>();

    // Animation-only impulses. They never affect damage, cooldowns or movement.
    // [0] attack, [1] ability, [2] rage, [3] hit reaction, [4] transition, [5] previous attack, [6] last decay tick
    private static float[] animationState(UUID id) {
        return ANIMATION_STATE.computeIfAbsent(id, k -> new float[7]);
    }
    public static void triggerAttack() {
        if (Minecraft.getInstance().player == null) return;
        animationState(Minecraft.getInstance().player.getUUID())[0] = 1f;
        spawnAttackBurst(Minecraft.getInstance().player, currentKaguneType(), false);
    }
    public static void triggerAbility() {
        if (Minecraft.getInstance().player == null) return;
        animationState(Minecraft.getInstance().player.getUUID())[1] = 1f;
        spawnAttackBurst(Minecraft.getInstance().player, currentKaguneType(), true);
    }
    public static void triggerRage() {
        if (Minecraft.getInstance().player == null) return;
        animationState(Minecraft.getInstance().player.getUUID())[2] = 1f;
        spawnRageBurst(Minecraft.getInstance().player);
    }
    public static void triggerHit() { if (Minecraft.getInstance().player != null) animationState(Minecraft.getInstance().player.getUUID())[3] = 1f; }
    public static void triggerTransition() { if (Minecraft.getInstance().player != null) animationState(Minecraft.getInstance().player.getUUID())[4] = 1f; }

    public static void triggerGrapple(){ grappleTicks = 10; }
    public static void triggerDash(){ dashTicks = 12; }
    public static boolean isDashActive(){ return dashTicks > 0; }

    private static float decay(float value, float amount) {
        return value <= 0.001f ? 0f : Math.max(0f, value - amount);
    }

    private static void spawnAttackBurst(net.minecraft.client.player.LocalPlayer player, KaguneType type, boolean special) {
        int count = special ? 12 : 8;
        double reach = type == KaguneType.SHOOTING ? 1.6 : type == KaguneType.LONG ? 1.9 : 1.25;
        Vec3 look = player.getLookAngle();
        for (int i = 0; i < count; i++) {
            double spread = (player.getRandom().nextDouble() - 0.5) * 0.65;
            double px = player.getX() + look.x * reach + spread;
            double py = player.getY() + 1.0 + (player.getRandom().nextDouble() - 0.5) * 0.8;
            double pz = player.getZ() + look.z * reach + spread;
            player.level().addParticle(special ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.CRIT,
                px, py, pz, look.x * 0.08, 0.02, look.z * 0.08);
        }
    }

    private static void spawnRageBurst(net.minecraft.client.player.LocalPlayer player) {
        for (int i = 0; i < 20; i++) {
            double a = player.getRandom().nextDouble() * Math.PI * 2.0;
            double radius = 0.35 + player.getRandom().nextDouble() * 0.75;
            player.level().addParticle(ParticleTypes.SOUL,
                player.getX() + Math.cos(a) * radius,
                player.getY() + 0.5 + player.getRandom().nextDouble() * 1.4,
                player.getZ() + Math.sin(a) * radius,
                Math.cos(a) * 0.025, 0.035, Math.sin(a) * 0.025);
        }
    }

    @SubscribeEvent
    public static void render(RenderPlayerEvent.Post e) {
        if (ClientGhoulData.race() != GhoulData.Race.GHOUL && ClientGhoulData.race() != GhoulData.Race.HALF_GHOUL) return;

        boolean active = ClientGhoulData.kaguneActive();
        if (active) emergence = Mth.clamp(emergence + 0.085f, 0f, 1f);
        else emergence = Mth.clamp(emergence - 0.115f, 0f, 1f);
        if (!active && emergence <= 0.01f) { previousActive = false; return; }

        if (MODEL == null) MODEL = new KaguneModel(Minecraft.getInstance().getEntityModels().bakeLayer(ClientSetup.KAGUNE_LAYER));
        var player = e.getEntity();
        PoseStack pose = e.getPoseStack();
        pose.pushPose();
        pose.translate(0, 1.34, 0.26);

        float t = player.tickCount + e.getPartialTick();
        Vec3 velocity = player.getDeltaMovement();
        float yaw = player.getYRot() * Mth.DEG_TO_RAD;
        float rawForward = (float)(velocity.x * -Mth.sin(yaw) + velocity.z * Mth.cos(yaw));
        float rawStrafe = (float)(velocity.x * Mth.cos(yaw) + velocity.z * Mth.sin(yaw));
        float rawVertical = (float)velocity.y;
        float[] sv = SMOOTH_VELOCITY.computeIfAbsent(player.getUUID(), k -> new float[3]);
        float smoothing = player.isFallFlying() || player.getAbilities().flying ? 0.18f : 0.24f;
        sv[0] = Mth.lerp(smoothing, sv[0], rawForward);
        sv[1] = Mth.lerp(smoothing, sv[1], rawStrafe);
        sv[2] = Mth.lerp(smoothing, sv[2], rawVertical);
        float forward = sv[0];
        float strafe = sv[1];
        float vertical = sv[2];
        float move = Mth.clamp((float)Math.sqrt(forward * forward + strafe * strafe) * 6.0f, 0f, 1.8f);
        if (player.isFallFlying() || player.getAbilities().flying) move = Math.max(move, 0.18f);
        float attack = player.getAttackAnim(e.getPartialTick());
        float hurt = player.hurtTime > 0 ? player.hurtTime / 10f : 0f;
        float[] state = animationState(player.getUUID());
        if (attack > 0.10f && state[5] < 0.10f) state[0] = 1f;
        state[5] = attack;
        // Decay impulses once per game tick rather than once per render frame.
        // This keeps the animation duration stable at 30/60/120/144 FPS.
        if (state[6] != player.tickCount) {
            state[0] = decay(state[0], 0.16f);
            state[1] = decay(state[1], 0.13f);
            state[2] = decay(state[2], 0.08f);
            state[3] = decay(state[3], 0.18f);
            state[6] = player.tickCount;
        }
        state[4] = Mth.clamp(emergence, 0f, 1f);
        if (hurt > 0.05f) state[3] = Math.max(state[3], hurt);
        if (grappleTicks > 0) grappleTicks--;
        if (dashTicks > 0) dashTicks--;
        // Grapple animation is intentionally restricted to grapple-capable kagune.
        // All kagune still receive the universal movement layer below.
        KaguneType currentType = currentKaguneType();
        boolean grappleCapable = currentType == KaguneType.RINKAKU || currentType == KaguneType.LONG;
        float grapple = grappleCapable && grappleTicks > 0 ? 1f - (grappleTicks / 10f) : 0f;
        float dash = dashTicks > 0 ? 1f - (dashTicks / 12f) : 0f;
        float attackPulse = state[0];
        float abilityPulse = state[1];
        float ragePulse = state[2];
        float hitPulse = Math.max(state[3], hurt);
        MODEL.animate(currentType, t, move, attack, hitPulse, player.onGround(), player.isCrouching(), emergence, grapple, dash, forward, strafe, vertical, attackPulse, abilityPulse, ragePulse);


        MultiBufferSource buf = e.getMultiBufferSource();
        VertexConsumer vc = buf.getBuffer(RenderType.entityCutoutNoCull(texture()));
        MODEL.renderToBuffer(pose, vc, e.getPackedLight(), OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 0.98f);
        pose.popPose();

        // A restrained burst of particles makes release feel physical rather than like an item swap.
        if (player == Minecraft.getInstance().player && active && !previousActive && player.level().isClientSide) {
            for (int i = 0; i < 18; i++) {
                double ox = (player.getRandom().nextDouble() - 0.5) * 0.9;
                double oy = 0.7 + player.getRandom().nextDouble() * 1.1;
                double oz = (player.getRandom().nextDouble() - 0.5) * 0.6;
                player.level().addParticle(ParticleTypes.DAMAGE_INDICATOR,
                    player.getX() + ox, player.getY() + oy, player.getZ() + oz,
                    (player.getRandom().nextDouble() - 0.5) * 0.04, 0.02, (player.getRandom().nextDouble() - 0.5) * 0.04);
            }
        }
        if (player == Minecraft.getInstance().player && active && dashTicks > 0 && player.level().isClientSide && player.tickCount % 1 == 0) {
            Vec3 look = player.getLookAngle();
            for (int i = 0; i < 2; i++) {
                double side = (player.getRandom().nextDouble() - 0.5) * 0.55;
                player.level().addParticle(ParticleTypes.CLOUD,
                    player.getX() - look.x * 0.35 + side, player.getY() + 0.15 + player.getRandom().nextDouble() * 0.8,
                    player.getZ() - look.z * 0.35 + side, -look.x * 0.03, 0.005, -look.z * 0.03);
            }
        }
        if (player == Minecraft.getInstance().player && active && player.tickCount % 6 == 0 && player.level().isClientSide) {
            player.level().addParticle(ParticleTypes.SMOKE, player.getX(), player.getY() + 1.15, player.getZ(), 0, 0.01, 0);
        }
        if (player == Minecraft.getInstance().player) previousActive = active;
    }

    private static KaguneType currentKaguneType() {
        int i = ClientGhoulData.kaguneType();
        KaguneType[] v = KaguneType.values();
        return i >= 0 && i < v.length ? v[i] : KaguneType.RINKAKU;
    }

    private static ResourceLocation texture() {
        return switch (currentKaguneType()) {
            case UKAKU -> new ResourceLocation("tokyoghoulrpg", "textures/entity/kagune_ukaku.png");
            case KOUKAKU -> new ResourceLocation("tokyoghoulrpg", "textures/entity/kagune_koukaku.png");
            case BIKAKU -> new ResourceLocation("tokyoghoulrpg", "textures/entity/kagune_bikaku.png");
            case KAGERO -> new ResourceLocation("tokyoghoulrpg", "textures/entity/kagune_kagero.png");
            case SHOOTING -> new ResourceLocation("tokyoghoulrpg", "textures/entity/kagune_shooting.png");
            case LONG -> new ResourceLocation("tokyoghoulrpg", "textures/entity/kagune_long.png");
            default -> new ResourceLocation("tokyoghoulrpg", "textures/entity/kagune_rinkaku.png");
        };
    }

    private PlayerKaguneRenderer() {}
}
