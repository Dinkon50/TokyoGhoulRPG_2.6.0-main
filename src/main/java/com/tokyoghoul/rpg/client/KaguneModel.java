package com.tokyoghoul.rpg.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tokyoghoul.rpg.v2.KaguneType;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/**
 * Procedural organic kagune model. The four standard branches use many short
 * articulated segments so movement reads as a flexible biological appendage.
 * Shooting and Long deliberately collapse to one large projection.
 */
public class KaguneModel extends EntityModel<Player> {
    private static final int ROOTS = 4, SEGS = 18;
    private final ModelPart[][] seg = new ModelPart[ROOTS][SEGS];
    private final ModelPart[][] ridge = new ModelPart[ROOTS][SEGS];
    private final ModelPart[][] spine = new ModelPart[ROOTS][SEGS];
    private final ModelPart[] roots = new ModelPart[ROOTS];
    private final ModelPart[] accents = new ModelPart[ROOTS];

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        for (int r = 0; r < ROOTS; r++) {
            float side = r % 2 == 0 ? -1f : 1f;
            float row = r < 2 ? -1f : 1f;
            PartDefinition branch = root.addOrReplaceChild("r" + r,
                CubeListBuilder.create().texOffs(0, 0)
                    .addBox(-3.0f, -2.7f, 0, 6.0f, 5.4f, 6.0f),
                PartPose.offset(side * 3.6f, -0.55f + row * 0.4f, 1.9f + row * 0.65f));

            for (int s = 0; s < SEGS; s++) {
                float t = s / (float)(SEGS - 1);
                float w = 4.8f * (1f - 0.70f * t) + 0.70f;
                float h = 4.2f * (1f - 0.72f * t) + 0.60f;
                float len = 5.4f - s * 0.13f;
                branch = branch.addOrReplaceChild("s" + s,
                    CubeListBuilder.create()
                        .texOffs((s * 10) % 96, 16 + (s % 5) * 8)
                        .addBox(-w / 2f, -h / 2f, 0, w, h, len),
                    PartPose.offset(0, 0, s == 0 ? 5.55f : len - 0.28f));
                // Extra 3D organic ridges and small dorsal spines give every segment
                // a real volumetric silhouette instead of looking like flat cubes.
                branch.addOrReplaceChild("ridge",
                    CubeListBuilder.create().texOffs(116, 24)
                        .addBox(-w * 0.24f, -h * 0.62f, 0.45f, w * 0.48f, h * 0.20f, len * 0.82f),
                    PartPose.ZERO);
                branch.addOrReplaceChild("spine",
                    CubeListBuilder.create().texOffs(132, 24)
                        .addBox(-0.34f, -h * 0.95f, len * 0.24f, 0.68f, h * 0.55f, Math.max(0.9f, len * 0.18f)),
                    PartPose.ZERO);
            }

            branch.addOrReplaceChild("tip",
                CubeListBuilder.create().texOffs(92, 0)
                    .addBox(-0.48f, -0.48f, 0, 0.96f, 0.96f, 5.4f),
                PartPose.offset(0, 0, 4.35f));

            PartDefinition original = root.getChild("r" + r);
            original.addOrReplaceChild("accent",
                CubeListBuilder.create().texOffs(104, 0)
                    .addBox(-1.05f, -0.42f, 0.5f, 2.1f, 0.84f, 5.0f),
                PartPose.ZERO);
        }
        return LayerDefinition.create(mesh, 192, 192);
    }

    public KaguneModel(ModelPart root) {
        for (int r = 0; r < ROOTS; r++) {
            ModelPart p = root.getChild("r" + r);
            roots[r] = p;
            accents[r] = p.getChild("accent");
            for (int s = 0; s < SEGS; s++) {
                seg[r][s] = p.getChild("s" + s);
                ridge[r][s] = seg[r][s].getChild("ridge");
                spine[r][s] = seg[r][s].getChild("spine");
                p = seg[r][s];
            }
        }
    }

    public void animate(KaguneType type, float age, float move, float attack, float hurt,
                        boolean onGround, boolean crouching, float emergence, float grapple, float dash, float forward, float strafe, float vertical,
                        float attackPulse, float abilityPulse, float ragePulse) {
        float locomotion = Mth.clamp(move, 0f, 1.8f);
        float emergenceEase = emergence * emergence * (3f - 2f * emergence);
        float attackEase = attack * attack * (3f - 2f * attack);
        float grappleEase = grapple * grapple * (3f - 2f * grapple);
        float dashEase = dash * dash * (3f - 2f * dash);

        for (int r = 0; r < ROOTS; r++) {
            roots[r].xScale = roots[r].yScale = roots[r].zScale = 1f;
            accents[r].xScale = accents[r].yScale = accents[r].zScale = 1f;
            roots[r].visible = true;
            accents[r].visible = true;
            for (int s = 0; s < SEGS; s++) {
                seg[r][s].visible = true;
                ridge[r][s].visible = true;
                spine[r][s].visible = true;
                seg[r][s].xScale = seg[r][s].yScale = seg[r][s].zScale = 1f;
                ridge[r][s].xScale = ridge[r][s].yScale = ridge[r][s].zScale = 1f;
                spine[r][s].xScale = spine[r][s].yScale = spine[r][s].zScale = 1f;
                seg[r][s].xRot = seg[r][s].yRot = seg[r][s].zRot = 0f;
                ridge[r][s].xRot = ridge[r][s].yRot = ridge[r][s].zRot = 0f;
                spine[r][s].xRot = spine[r][s].yRot = spine[r][s].zRot = 0f;
            }
        }

        resetShape();
        switch (type) {
            case RINKAKU -> animateRinkaku(age, locomotion, attackEase, grappleEase, dashEase);
            case UKAKU -> animateUkaku(age, locomotion, attackEase, grappleEase, dashEase);
            case KOUKAKU -> animateKoukaku(age, locomotion, attackEase, grappleEase, dashEase);
            case BIKAKU -> animateBikaku(age, locomotion, attackEase, grappleEase, dashEase);
            case KAGERO -> animateKagero(age, locomotion, attackEase, grappleEase, dashEase);
            case SHOOTING -> animateShooting(age, attackEase, dashEase);
            case LONG -> animateLong(age, locomotion, attackEase, grappleEase, dashEase);
            default -> hideAll();
        }

        // High-quality universal locomotion layer.  This is deliberately applied AFTER
        // each kagune's own style animation, so every type gets the same physical
        // principles: inertia, delayed tips, counter-sway, air lift/drop and directional
        // banking.  Grapple/dash impulses are additional accents, not the base animation.
        float horizontalSpeed = (float)Math.sqrt(forward * forward + strafe * strafe);
        float totalSpeed = (float)Math.sqrt(forward * forward + strafe * strafe + vertical * vertical);
        float motion = Mth.clamp(totalSpeed * 1.65f, 0f, 1f);
        float horizontal = Mth.clamp(horizontalSpeed * 1.75f, 0f, 1f);
        float air = onGround ? 0f : 1f;
        float rise = Mth.clamp(vertical * 3.4f, -1f, 1f);
        float grappleImpulse = grappleEase * 0.9f + dashEase * 0.28f;

        // The root reacts first; the tip follows later, producing real-looking lag.
        float pitch = Mth.clamp(forward * 2.4f, -1f, 1f);
        float bank = Mth.clamp(strafe * 2.4f, -1f, 1f);
        float lift = Mth.clamp(rise, -1f, 1f);
        float idleBreath = Mth.sin(age * 0.105f) * 0.035f;
        float slowPulse = Mth.sin(age * 0.047f + 1.2f) * 0.022f;
        float directionWave = age * (0.13f + horizontal * 0.10f) + pitch * 1.4f - bank * 0.9f;

        for (int r = 0; r < ROOTS; r++) {
            float side = r % 2 == 0 ? -1f : 1f;
            float row = r < 2 ? -1f : 1f;
            float phase = directionWave + r * 0.82f;

            roots[r].xRot += -pitch * (0.16f + 0.20f * motion) * row;
            roots[r].xRot += lift * (0.12f + 0.16f * air) * row;
            roots[r].yRot += bank * (0.20f + 0.25f * motion);
            roots[r].zRot += bank * 0.10f * side;
            roots[r].zRot += idleBreath * side + slowPulse * row;

            for (int s = 0; s < SEGS; s++) {
                float t = (s + 1f) / SEGS;
                // Quadratic delay: base follows the body, tip follows last.
                float lag = t * t;
                float tipLag = t * t * t;
                float wave = Mth.sin(phase - s * 0.52f) * (0.018f + 0.070f * t);
                float secondary = Mth.cos(phase * 0.83f - s * 0.31f + r) * (0.012f + 0.038f * t);
                float breathing = Mth.sin(age * 0.095f + s * 0.30f + r) * 0.018f * (0.35f + t);

                seg[r][s].xRot += -pitch * (0.10f + 0.52f * lag) * row;
                seg[r][s].xRot += lift * (0.07f + 0.27f * tipLag) * row;
                seg[r][s].yRot += bank * (0.10f + 0.54f * lag);
                seg[r][s].zRot += bank * 0.07f * side * lag;
                seg[r][s].zRot += wave * side + secondary * side + breathing * side;

                // Volumetric secondary motion: ridges and spines lag behind the main segment.
                // This makes the appendage read as flexible tissue in 3D, not a chain of blocks.
                ridge[r][s].xRot = seg[r][s].xRot * 1.12f + wave * 0.45f;
                ridge[r][s].yRot = seg[r][s].yRot * 0.92f - secondary * 0.35f;
                ridge[r][s].zRot = seg[r][s].zRot * 1.20f;
                ridge[r][s].yScale = 0.82f + 0.22f * (float)Math.sin(Math.PI * t);
                spine[r][s].xRot = seg[r][s].xRot * 0.78f - wave * 0.28f;
                spine[r][s].yRot = seg[r][s].yRot * 0.72f;
                spine[r][s].zRot = seg[r][s].zRot * 1.55f + side * 0.025f;
                spine[r][s].zScale = 0.72f + 0.42f * (float)Math.sin(Math.PI * t);

                // Airborne movement makes the appendage float before falling back,
                // instead of snapping between ground and air poses.
                if (air > 0f) {
                    float airWave = Mth.sin(age * 0.16f - s * 0.40f + r) * 0.035f * (0.3f + t);
                    seg[r][s].xRot += airWave + lift * 0.08f * t * row;
                }

                // Grapple/dash creates a travelling recoil wave.  Only grapple-capable
                // kagune receive grappleEase; every kagune can still receive dashEase.
                if (grappleImpulse > 0.025f) {
                    float snap = Mth.sin(t * Mth.PI) * grappleImpulse;
                    float travel = Mth.sin((1f - t) * Mth.PI + age * 0.12f) * grappleImpulse;
                    seg[r][s].xRot += -snap * 0.11f * row;
                    seg[r][s].yRot += snap * 0.16f * side;
                    seg[r][s].zRot += travel * 0.08f * side;
                }
            }
        }

        // Advanced animation pass: cinematic attack, special-ability, Rage and hit reactions.
        // This is visual-only. It never changes movement, damage, cooldowns or targeting.
        float attackWave = Mth.sin((1f - attackPulse) * Mth.PI) * attackPulse;
        float abilityWave = Mth.sin((1f - abilityPulse) * Mth.PI) * abilityPulse;
        float rageWave = Mth.sin((1f - ragePulse) * Mth.PI) * ragePulse;
        float transitionWave = Mth.sin(emergenceEase * Mth.PI);
        float hitWave = hurt * hurt * (3f - 2f * hurt);

        for (int r = 0; r < ROOTS; r++) {
            float side = r % 2 == 0 ? -1f : 1f;
            float row = r < 2 ? -1f : 1f;
            float scale = 0.08f + 0.92f * emergenceEase;
            // Slight overshoot gives emergence a biological snap instead of a UI-style scale.
            float emergenceOvershoot = 1f + transitionWave * 0.12f;
            roots[r].xScale = roots[r].yScale = roots[r].zScale = scale * emergenceOvershoot;
            if (crouching) roots[r].xRot += 0.05f;
            roots[r].xRot += hurt * 0.08f * side;
            roots[r].xRot += attackWave * (0.22f + 0.08f * row);
            roots[r].yRot += abilityWave * 0.16f * side;
            roots[r].zRot += rageWave * 0.10f * side;
            roots[r].zRot += hitWave * 0.16f * side;

            for (int s = 0; s < SEGS; s++) {
                float t = (s + 1f) / SEGS;
                // The impulse travels down the appendage: root first, tip last.
                float travel = Mth.clamp((attackWave - t * 0.22f) / 0.78f, 0f, 1f);
                float specialTravel = Mth.clamp((abilityWave - t * 0.16f) / 0.84f, 0f, 1f);
                float rageTravel = Mth.clamp((rageWave - t * 0.12f) / 0.88f, 0f, 1f);
                float pulse = Mth.sin(t * Mth.PI);

                seg[r][s].xRot += -travel * (0.34f + 0.42f * t) * row;
                seg[r][s].yRot += travel * (0.24f + 0.34f * t) * side;
                seg[r][s].zRot += specialTravel * 0.20f * side;
                seg[r][s].xRot += specialTravel * 0.22f * pulse * row;
                seg[r][s].zRot += rageTravel * 0.13f * side;
                seg[r][s].yRot += hitWave * pulse * 0.11f * side;

                // Secondary geometry intentionally lags the main segment.
                ridge[r][s].xRot += travel * 0.16f * row;
                ridge[r][s].yRot += specialTravel * 0.12f * side;
                spine[r][s].xRot -= travel * 0.11f * row;
                spine[r][s].zRot += rageTravel * 0.08f * side;

                // Rage makes the silhouette breathe/pulse without recoloring the texture.
                float rageScale = 1f + rageTravel * (0.07f + 0.06f * pulse);
                ridge[r][s].xScale *= rageScale;
                ridge[r][s].yScale *= rageScale;
            }
        }

        // A visible but smooth emergence/retraction remains attached to the back.
        // The easing is intentionally slow enough to read as tissue unfolding.
    }

    private void resetShape() {
        for (int r = 0; r < ROOTS; r++) {
            roots[r].visible = true;
            roots[r].x = (r % 2 == 0 ? -3.6f : 3.6f);
            roots[r].y = -0.55f + (r < 2 ? -0.4f : 0.4f);
            roots[r].z = 1.9f + (r < 2 ? -0.65f : 0.65f);
            roots[r].xScale = roots[r].yScale = roots[r].zScale = 1f;
            accents[r].visible = true;
            accents[r].xScale = accents[r].yScale = accents[r].zScale = 1f;
            for (int s = 0; s < SEGS; s++) {
                seg[r][s].visible = true;
                ridge[r][s].visible = true;
                spine[r][s].visible = true;
                seg[r][s].xScale = seg[r][s].yScale = seg[r][s].zScale = 1f;
                ridge[r][s].xScale = ridge[r][s].yScale = ridge[r][s].zScale = 1f;
                spine[r][s].xScale = spine[r][s].yScale = spine[r][s].zScale = 1f;
            }
        }
    }

    private void hideAll() { for (ModelPart r : roots) r.visible = false; }

    private void animateRinkaku(float age, float move, float attack, float grapple, float dash) {
        // Four soft, independently articulated tentacles.
        float recoil = Mth.sin(dash * (float)Math.PI) * dash;
        float surge = Mth.sin((1f - dash) * (float)Math.PI) * dash; 
        for (int r = 0; r < ROOTS; r++) {
            float side = r % 2 == 0 ? -1f : 1f;
            float row = r < 2 ? -1f : 1f;
            float phase = r * 1.55f;
            roots[r].yRot = side * (0.16f + 0.05f * Mth.sin(age * 0.08f + phase));
            roots[r].xRot = row * 0.06f - attack * 0.20f * row - grapple * 0.70f * row + recoil * 0.42f * row;
            roots[r].yRot += surge * 0.16f * (r % 2 == 0 ? -1f : 1f);
            accents[r].xRot = -0.15f + attack * 0.35f;
            for (int s = 0; s < SEGS; s++) {
                float t = (s + 1f) / SEGS;
                float wave = Mth.sin(age * 0.12f + s * 0.58f + phase) * (0.045f + t * 0.075f);
                float wave2 = Mth.cos(age * 0.092f + s * 0.43f + phase) * (0.035f + t * 0.065f);
                seg[r][s].yRot = side * (0.08f + t * 0.14f) + wave + attack * side * t * 0.28f;
                seg[r][s].xRot = row * (0.025f + t * 0.035f) + wave2 - move * row * t * 0.08f - grapple * row * t * 0.45f - recoil * row * t * 0.75f;
                seg[r][s].yRot += surge * (0.22f + t * 0.42f) * (r % 2 == 0 ? -1f : 1f);
                seg[r][s].zRot = side * Mth.sin(age * 0.095f + s * 0.4f) * 0.035f;
            }
        }
    }

    private void animateUkaku(float age, float move, float attack, float grapple, float dash) {
        // Feather/wing silhouette: two broad upper projections.
        roots[2].visible = roots[3].visible = accents[2].visible = accents[3].visible = false;
        for (int r = 0; r < 2; r++) { roots[r].xScale = 1.65f; roots[r].yScale = 0.62f; roots[r].zScale = 1.15f; }
        float recoil = Mth.sin(dash * (float)Math.PI) * dash;
        float surge = Mth.sin((1f - dash) * (float)Math.PI) * dash; 
        for (int r = 0; r < ROOTS; r++) {
            float side = r % 2 == 0 ? -1f : 1f;
            float row = r < 2 ? -1f : 1f;
            roots[r].zRot = side * (0.32f + 0.05f * Mth.sin(age * 0.10f + r)) + side * surge * 0.32f;
            roots[r].xRot = -0.18f + row * 0.04f - attack * 0.28f;
            accents[r].zRot = side * (0.35f + attack * 0.5f);
            for (int s = 0; s < SEGS; s++) {
                float t = (s + 1f) / SEGS;
                seg[r][s].xRot = -0.10f - t * 0.17f - attack * t * 0.38f - surge * t * 0.55f;
                seg[r][s].zRot = side * (0.06f + t * 0.08f) + Mth.sin(age * 0.12f + s * 0.55f) * 0.035f;
                seg[r][s].yRot = side * (0.025f + t * 0.05f) + move * side * t * 0.05f;
            }
        }
    }

    private void animateKoukaku(float age, float move, float attack, float grapple, float dash) {
        // Heavy plated pair: fewer, thicker appendages.
        roots[2].visible = roots[3].visible = accents[2].visible = accents[3].visible = false;
        for (int r = 0; r < 2; r++) { roots[r].xScale = 1.35f; roots[r].yScale = 1.55f; roots[r].zScale = 1.18f; for (int s = 0; s < SEGS; s++) { seg[r][s].xScale = 1.25f; seg[r][s].yScale = 1.30f; } }
        float recoil = Mth.sin(dash * (float)Math.PI) * dash;
        float surge = Mth.sin((1f - dash) * (float)Math.PI) * dash; 
        for (int r = 0; r < ROOTS; r++) {
            float side = r % 2 == 0 ? -1f : 1f;
            float row = r < 2 ? -1f : 1f;
            roots[r].xRot = row * 0.12f - attack * 0.08f - recoil * 0.22f * row;
            roots[r].zRot = side * 0.12f;
            accents[r].yRot = side * (0.40f + attack * 0.45f);
            for (int s = 0; s < SEGS; s++) {
                float t=(s+1f)/SEGS;
                seg[r][s].yRot = side * (0.035f + t*0.055f);
                seg[r][s].xRot = row * (0.05f + t*0.08f) - attack * t * 0.20f - recoil * t * 0.42f * row;
                seg[r][s].zRot = side * Mth.sin(age*0.07f+s*0.3f)*0.02f;
            }
        }
    }

    private void animateBikaku(float age, float move, float attack, float grapple, float dash) {
        // Single balanced tail from the center of the lower back.
        roots[1].visible = roots[2].visible = roots[3].visible = false;
        accents[1].visible = accents[2].visible = accents[3].visible = false;
        roots[0].x = 0f; roots[0].y = 0.15f; roots[0].z = 2.45f; roots[0].xScale = 0.82f; roots[0].yScale = 0.88f; roots[0].zScale = 1.22f;
        float recoil = Mth.sin(dash * (float)Math.PI) * dash;
        float surge = Mth.sin((1f - dash) * (float)Math.PI) * dash; 
        for (int r = 0; r < ROOTS; r++) {
            float side = r % 2 == 0 ? -1f : 1f;
            float row = r < 2 ? -1f : 1f;
            roots[r].xRot = row * 0.18f - attack * row * 0.12f - recoil * row * 0.35f;
            roots[r].yRot = side * 0.08f;
            for (int s=0;s<SEGS;s++){
                float t=(s+1f)/SEGS;
                seg[r][s].xRot = row*(0.07f+t*0.12f)-attack*row*t*0.30f-recoil*row*t*0.60f;
                seg[r][s].yRot = side*(0.04f+t*0.06f)+Mth.sin(age*0.09f+s*0.45f)*0.025f;
            }
        }
    }

    private void animateKagero(float age, float move, float attack, float grapple, float dash) {
        // Hybrid three-branch silhouette with a dominant central tendril.
        roots[3].visible = accents[3].visible = false;
        roots[0].x = -2.8f; roots[1].x = 2.8f; roots[2].x = 0f; roots[2].y = 0.1f; roots[2].z = 2.25f;
        roots[2].xScale = 1.12f; roots[2].yScale = 1.12f;
        float recoil = Mth.sin(dash * (float)Math.PI) * dash;
        float surge = Mth.sin((1f - dash) * (float)Math.PI) * dash; 
        for (int r=0;r<ROOTS;r++){
            float side=r%2==0?-1f:1f; float row=r<2?-1f:1f;
            roots[r].zRot=side*(0.22f+0.05f*Mth.sin(age*0.13f+r));
            roots[r].xRot=row*0.10f-attack*0.35f*row-recoil*0.28f*row;
            accents[r].zRot=side*(0.45f+attack*0.7f);
            for(int s=0;s<SEGS;s++){
                float t=(s+1f)/SEGS;
                seg[r][s].yRot=side*(0.08f+0.10f*t)+Mth.sin(age*0.15f+s*0.7f+r)*0.05f+attack*side*t*0.32f;
                seg[r][s].zRot=side*(0.04f+0.06f*t);
                seg[r][s].xRot=row*(0.03f+0.06f*t)-move*row*t*0.06f-recoil*row*t*0.70f;
            }
        }
    }

    private void animateShooting(float age, float attack, float dash) {
        roots[0].x = 0f; roots[0].y = -0.15f; roots[0].z = 2.35f; roots[0].xScale = 0.72f; roots[0].yScale = 0.72f; roots[0].zScale = 1.25f;
        float recoil = Mth.sin(dash * (float)Math.PI) * dash;
        float surge = Mth.sin((1f - dash) * (float)Math.PI) * dash; 
        for (int r=0;r<ROOTS;r++) roots[r].visible=accents[r].visible=r==0;
        roots[0].xRot=-0.26f+0.06f*Mth.sin(age*0.12f)-attack*0.18f-recoil*0.55f;
        roots[0].yRot=0f; roots[0].zRot=0f;
        accents[0].xRot=-0.25f-attack*0.5f;
        for(int s=0;s<SEGS;s++){
            float t=(s+1f)/SEGS;
            seg[0][s].xRot=-0.03f-attack*0.55f*t-recoil*0.70f*t;
            seg[0][s].yRot=Mth.sin(age*0.10f+s*0.5f)*0.035f*t;
            seg[0][s].zRot=Mth.cos(age*0.09f+s*0.4f)*0.018f*t;
        }
    }

    private void animateLong(float age, float move, float attack, float grapple, float dash) {
        roots[0].x = 0f; roots[0].y = -0.25f; roots[0].z = 2.35f; roots[0].xScale = 0.82f; roots[0].yScale = 0.82f; roots[0].zScale = 1.35f;
        float recoil = Mth.sin(dash * (float)Math.PI) * dash;
        float surge = Mth.sin((1f - dash) * (float)Math.PI) * dash; 
        for (int r=0;r<ROOTS;r++) roots[r].visible=accents[r].visible=r==0;
        roots[0].xRot=-0.06f+0.04f*Mth.sin(age*0.08f)-attack*0.25f-grapple*0.95f-recoil*0.65f;
        roots[0].yRot=0.02f*Mth.sin(age*0.06f);
        accents[0].xRot=-0.1f-attack*0.6f-grapple*0.5f;
        for(int s=0;s<SEGS;s++){
            float t=(s+1f)/SEGS;
            seg[0][s].xRot=0.02f+attack*0.42f*t-grapple*0.80f*t-recoil*0.85f*t;
            seg[0][s].yRot=Mth.sin(age*0.075f+s*0.36f)*0.055f*t;
            seg[0][s].zRot=Mth.cos(age*0.06f+s*0.29f)*0.035f*t;
            seg[0][s].zScale=1.12f+0.35f*t;
        }
    }

    @Override public void setupAnim(Player e,float a,float b,float c,float d,float f) {}

    @Override public void renderToBuffer(PoseStack pose, VertexConsumer vc,int light,int overlay,float r,float g,float b,float a){
        for(ModelPart p:roots) if(p.visible) p.render(pose,vc,light,overlay,r,g,b,a);
    }
}
