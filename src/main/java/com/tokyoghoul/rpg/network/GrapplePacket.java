package com.tokyoghoul.rpg.network;

import com.tokyoghoul.rpg.capability.GhoulData;
import com.tokyoghoul.rpg.v2.KaguneType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Long-range kagune pull. Only Rinkaku and Long have enough reach/structure to perform it. */
public record GrapplePacket(){
    public static void encode(GrapplePacket p,FriendlyByteBuf b){}
    public static GrapplePacket decode(FriendlyByteBuf b){return new GrapplePacket();}
    public static void handle(GrapplePacket p,Supplier<NetworkEvent.Context> c){
        c.get().enqueueWork(() -> {
            Player s=c.get().getSender(); if(s==null) return;
            GhoulData.get(s).ifPresent(d -> {
                if((d.getRace()!=GhoulData.Race.GHOUL && d.getRace()!=GhoulData.Race.HALF_GHOUL) || !d.isKaguneActive()) return;
                KaguneType type=d.getKaguneType();
                if(type!=KaguneType.RINKAKU && type!=KaguneType.LONG) return;
                long now=s.level().getGameTime();
                long cd=s.getPersistentData().getLong("TG_KaguneGrappleCooldown");
                if(now<cd) return;
                HitResult hit=s.pick(36.0D,0.0F,false);
                if(hit.getType()!=HitResult.Type.BLOCK) return;
                Vec3 target=((BlockHitResult)hit).getLocation().add(0,0.45,0);
                Vec3 delta=target.subtract(s.position());
                double len=delta.length();
                if(len<3.0 || len>36.0) return;
                Vec3 velocity=delta.normalize().scale(Math.min(3.2,0.75+len*0.095));
                s.setDeltaMovement(velocity);
                s.hurtMarked=true;
                s.getPersistentData().putLong("TG_KaguneGrappleCooldown",now+100L);
                d.sync(s);
            });
        });
        c.get().setPacketHandled(true);
    }
}
