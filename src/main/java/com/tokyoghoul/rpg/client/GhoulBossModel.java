package com.tokyoghoul.rpg.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tokyoghoul.rpg.entity.GhoulBoss;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/** Large original boss model with procedural attack, breathing and recovery animations. */
public class GhoulBossModel extends EntityModel<GhoulBoss> {
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition r = mesh.getRoot();
        r.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0,32).addBox(-7,-10,-4,14,20,8).texOffs(0,60).addBox(-9,-4,-5,18,10,2), PartPose.offset(0,12,0));
        r.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(44,24).addBox(-8,-5,-5,16,10,3).texOffs(44,37).addBox(-10,-2,-6,20,5,2), PartPose.offset(0,5,0));
        r.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0,0).addBox(-6,-7,-6,12,14,12).texOffs(36,0).addBox(-3,-2,-8,6,5,3).texOffs(54,0).addBox(-4,-6,-7,8,2,2), PartPose.offset(0,-10,0));
        r.addOrReplaceChild("arm_l", CubeListBuilder.create().texOffs(64,0).addBox(-3,-2,-3,6,24,6).texOffs(88,0).addBox(-4,20,-4,8,8,8), PartPose.offset(-9,3,0));
        r.addOrReplaceChild("arm_r", CubeListBuilder.create().texOffs(64,0).mirror().addBox(-3,-2,-3,6,24,6).texOffs(88,0).mirror().addBox(-4,20,-4,8,8,8), PartPose.offset(9,3,0));
        r.addOrReplaceChild("leg_l", CubeListBuilder.create().texOffs(0,72).addBox(-4,0,-4,8,22,8), PartPose.offset(-4,32,0));
        r.addOrReplaceChild("leg_r", CubeListBuilder.create().texOffs(0,72).mirror().addBox(-4,0,-4,8,22,8), PartPose.offset(4,32,0));
        r.addOrReplaceChild("spines", CubeListBuilder.create().texOffs(32,72).addBox(-12,-1,2,24,15,3).texOffs(32,92).addBox(-9,-1,5,18,9,3), PartPose.offset(0,5,0));
        return LayerDefinition.create(mesh, 128, 128);
    }

    private final ModelPart body, chest, head, armL, armR, legL, legR, spines;
    public GhoulBossModel(ModelPart root) {
        body=root.getChild("body"); chest=root.getChild("chest"); head=root.getChild("head");
        armL=root.getChild("arm_l"); armR=root.getChild("arm_r"); legL=root.getChild("leg_l"); legR=root.getChild("leg_r"); spines=root.getChild("spines");
    }

    @Override public void setupAnim(GhoulBoss e, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch) {
        head.yRot = yaw * Mth.DEG_TO_RAD;
        head.xRot = pitch * Mth.DEG_TO_RAD;

        float walk = Mth.cos(limbSwing * 0.55F) * 0.62F * limbSwingAmount;
        float counter = Mth.cos(limbSwing * 0.55F + Mth.PI) * 0.62F * limbSwingAmount;
        legL.xRot = walk; legR.xRot = counter;
        armL.xRot = -walk * 0.52F; armR.xRot = -counter * 0.52F;
        armL.zRot = 0.05F; armR.zRot = -0.05F;

        float breathe = Mth.sin(age * 0.075F) * 0.055F;
        body.xRot = breathe * 0.45F;
        chest.xRot = breathe;
        chest.y = 5 + Mth.sin(age * 0.06F) * 0.12F;
        spines.zRot = Mth.sin(age * 0.09F) * 0.045F;

        int ticks = e.getAnimationTicks();
        if (e.getAnimationKind() == 1) {
            float progress = 1F - ticks / 50F;
            float crouch = progress < 0.48F ? progress / 0.48F : (1F - progress) / 0.52F;
            crouch = Mth.clamp(crouch, 0F, 1F);
            float ease = crouch * crouch * (3F - 2F * crouch);
            body.xRot = 0.70F * ease;
            head.xRot += 0.18F * ease;
            legL.xRot = 0.95F * ease; legR.xRot = 0.95F * ease;
            armL.xRot = -1.15F * ease; armR.xRot = -1.15F * ease;
            if (ticks < 25) {
                float impact = 1F - ticks / 25F;
                impact *= impact;
                armL.xRot = -1.15F - 0.55F * impact;
                armR.xRot = -1.15F - 0.55F * impact;
                spines.xRot = 0.35F * impact;
            }
        } else if (e.getAnimationKind() == 2) {
            float t = Mth.clamp(1F - ticks / 24F, 0F, 1F);
            float ease = t * t * (3F - 2F * t);
            armL.xRot = -1.55F * ease;
            armR.xRot = -1.55F * ease;
            body.xRot = -0.18F * ease;
            head.xRot += 0.25F * ease;
        } else if (e.getAnimationKind() == 3) {
            float shake = Mth.sin(age * 2.3F) * 0.10F;
            body.zRot = shake;
            head.zRot = -shake * 0.65F;
        }
    }

    @Override public void renderToBuffer(PoseStack p, VertexConsumer v, int light, int overlay, float r,float g,float b,float a) {
        body.render(p,v,light,overlay,r,g,b,a); chest.render(p,v,light,overlay,r,g,b,a); head.render(p,v,light,overlay,r,g,b,a);
        armL.render(p,v,light,overlay,r,g,b,a); armR.render(p,v,light,overlay,r,g,b,a); legL.render(p,v,light,overlay,r,g,b,a); legR.render(p,v,light,overlay,r,g,b,a); spines.render(p,v,light,overlay,r,g,b,a);
    }
}
