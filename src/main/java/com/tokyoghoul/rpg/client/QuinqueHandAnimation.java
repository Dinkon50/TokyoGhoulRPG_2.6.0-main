package com.tokyoghoul.rpg.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tokyoghoul.rpg.item.QuinqueItem;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * High-detail first-person Quinque animation controller.
 * Each weapon has a distinct wind-up, impact and recovery profile.
 * The model remains the normal item model; this controller animates the whole weapon
 * in the hand without changing server-authoritative damage logic.
 */
public final class QuinqueHandAnimation {
    private record Profile(float preX,float preY,float preZ,float hitX,float hitY,float hitZ,
                           float tx,float ty,float tz,float weight,float recoil) {}

    @SubscribeEvent
    public static void renderHand(RenderHandEvent e) {
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null) return;
        ItemStack stack=mc.player.getItemInHand(e.getHand());
        if(!(stack.getItem() instanceof QuinqueItem)) return;
        String id=BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
        PoseStack p=e.getPoseStack();
        float swing=Mth.clamp(e.getSwingProgress(),0f,1f);
        float equip=e.getEquipProgress();
        float age=mc.player.tickCount+e.getPartialTick();
        float idle=Mth.sin(age*0.085f)*0.010f;
        boolean charged=stack.getOrCreateTag().getBoolean("TG_StrikeReady");
        Profile q=profile(id,charged);

        // Natural breathing and hand stabilization.
        p.translate(0.0D, -0.025D*equip+idle, 0.015D*Mth.cos(age*0.07f));
        p.mulPose(Axis.ZP.rotationDegrees(idle*30f));
        p.mulPose(Axis.XP.rotationDegrees(Mth.sin(age*0.06f)*0.55f));

        if(swing>0.001f) animateStrike(p,swing,q);
        else {
            // Tiny weapon-specific idle offset so every Quinque has a distinct stance.
            p.mulPose(Axis.XP.rotationDegrees(q.preX*0.06f));
            p.mulPose(Axis.YP.rotationDegrees(q.preY*0.045f));
            p.mulPose(Axis.ZP.rotationDegrees(q.preZ*0.045f));
        }
    }

    private static void animateStrike(PoseStack p,float s,Profile q){
        float pre=Mth.clamp(s/0.28f,0f,1f);
        float hit=Mth.clamp((s-0.18f)/0.44f,0f,1f);
        float rec=Mth.clamp((s-0.55f)/0.45f,0f,1f);
        pre=1f-(float)Math.pow(1f-pre,2.2f);
        hit=(float)(1.0-Math.pow(1.0-hit,3.0));
        rec=rec*rec*(3f-2f*rec);

        // Pull back before impact.
        p.mulPose(Axis.XP.rotationDegrees(q.preX*pre + q.hitX*hit*(1f-rec) + q.hitX*0.22f*rec));
        p.mulPose(Axis.YP.rotationDegrees(q.preY*pre + q.hitY*hit*(1f-rec) + q.hitY*0.12f*rec));
        p.mulPose(Axis.ZP.rotationDegrees(q.preZ*pre + q.hitZ*hit*(1f-rec) + q.hitZ*0.16f*rec));
        p.translate(q.tx*pre + q.tz*0.18f*hit,
                    q.ty*pre - q.ty*0.35f*hit,
                    q.tz*pre + q.tz*0.42f*hit);
        float squash=1f+q.weight*0.035f*hit;
        p.scale(squash,1f+q.weight*0.012f*hit,1f+q.weight*0.025f*hit);

        // Impact recoil: a very short reverse snap followed by recovery.
        float impact=(float)Math.sin(hit*Math.PI);
        p.mulPose(Axis.XP.rotationDegrees(-q.recoil*impact));
        p.translate(0.0D,0.0D,-0.025D*q.recoil*impact);
    }

    private static Profile profile(String id,boolean charged){
        return switch(id){
            case "quinque_blade" -> new Profile(-18,-18,-12,58,36,72,0.03f,-0.05f,0.08f,1.05f,3f);
            case "quinque_blind" -> new Profile(25,-12,-48,-70,30,92,-0.04f,0.02f,0.04f,1.00f,4f);
            case "quinque_cannon" -> new Profile(-12,-4,-8,20,8,24,0,-0.04f,0.12f,1.25f,7f);
            case "quinque_chain" -> new Profile(12,-35,-20,32,65,115,0.02f,0.03f,0.18f,0.92f,5f);
            case "quinque_claw" -> new Profile(-8,30,-55,-32,-54,105,-0.05f,-0.02f,0.04f,0.95f,3f);
            case "quinque_crimson" -> new Profile(20,-32,40,-42,104,-120,-0.06f,0.01f,0.06f,1.10f,4f);
            case "quinque_edge" -> new Profile(-10,38,-22,-38,-88,76,0.02f,-0.03f,0.09f,1.02f,3f);
            case "quinque_frost" -> new Profile(-4,-20,10,-62,34,-35,0.01f,0.03f,0.06f,0.96f,3f);
            case "quinque_glaive" -> new Profile(24,-26,55,-48,82,-128,-0.05f,-0.02f,0.05f,1.12f,5f);
            case "quinque_guard" -> new Profile(8,-18,-8,-18,38,22,0.0f,-0.05f,0.03f,1.18f,2f);
            case "quinque_hammer" -> new Profile(-72,12,-12,125,-18,38,0,-0.14f,0.12f,1.28f,8f);
            case "quinque_harpoon" -> new Profile(-20,8,-8,-36,28,-28,0.0f,0.02f,0.25f,0.98f,4f);
            case "quinque_inferno" -> new Profile(-42,10,18,82,-20,58,0,-0.07f,0.08f,1.06f,5f);
            case "quinque_lance" -> new Profile(-14,4,-4,-10,8,-12,0,0.01f,0.38f,0.98f,2f);
            case "quinque_mantis" -> new Profile(-12,-42,28,-30,86,-68,0.04f,-0.01f,0.08f,0.97f,4f);
            case "quinque_rapier" -> new Profile(-8,-10,-10,6,18,-18,0,0.01f,0.42f,0.88f,1.5f);
            case "quinque_scythe" -> new Profile(22,-38,30,-46,94,-132,-0.06f,0.0f,0.05f,1.08f,5f);
            case "quinque_titan" -> new Profile(-66,-14,-22,116,30,50,0,-0.14f,0.12f,1.34f,9f);
            case "quinque_twin" -> new Profile(-14,-34,-42,-28,66,80,0.0f,-0.02f,0.08f,0.94f,4f);
            case "quinque_venom" -> new Profile(-18,20,-12,44,-72,36,0.02f,-0.03f,0.08f,0.93f,3f);
            case "quinque_void" -> new Profile(-34,28,-40,-54,-82,96,0,-0.08f,0.12f,1.12f,6f);
            case "ccg_master_quinque" -> new Profile(-28,-20,-24,78,62,82,0,-0.10f,0.15f,1.24f,7f);
            case "boss_quinque" -> new Profile(-36,28,-46,96,-78,110,0,-0.12f,0.18f,1.30f,9f);
            default -> new Profile(-18,-18,-12,58,36,72,0,-0.05f,0.08f,1f,3f);
        };
    }
    private QuinqueHandAnimation(){}
}
