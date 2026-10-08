package com.tokyoghoul.rpg.event;

import com.tokyoghoul.rpg.capability.GhoulData;
import com.tokyoghoul.rpg.network.NetworkHandler;
import com.tokyoghoul.rpg.network.OpenOriginPacket;
import com.tokyoghoul.rpg.entity.GhoulBoss;
import com.tokyoghoul.rpg.init.ModItems;
import com.tokyoghoul.rpg.world.GhoulBossArena;
import com.tokyoghoul.rpg.world.RareLocations;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

@Mod.EventBusSubscriber
public class ModEvents {

    @SubscribeEvent
    public static void biteLock(LivingEvent.LivingTickEvent e){
        LivingEntity entity = e.getEntity();
        long until = entity.getPersistentData().getLong("TG_BiteLock");
        if(until > entity.level().getGameTime()){
            entity.setDeltaMovement(0, 0, 0);
            entity.setJumping(false);
            if(entity instanceof net.minecraft.world.entity.Mob mob) mob.getNavigation().stop();
            float lockY = entity.getPersistentData().getFloat("TG_BiteLockYRot");
            float lockX = entity.getPersistentData().getFloat("TG_BiteLockXRot");
            entity.setYRot(lockY);
            entity.setXRot(lockX);
            entity.setYHeadRot(lockY);
            entity.setYBodyRot(lockY);
        }
    }


    @SubscribeEvent
    public static void biteLockInteract(PlayerInteractEvent e){
        if(e.getEntity().getPersistentData().getLong("TG_BiteLock") > e.getEntity().level().getGameTime()) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void biteLockAttack(AttackEntityEvent e){
        if(e.getEntity().getPersistentData().getLong("TG_BiteLock") > e.getEntity().level().getGameTime()) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void biteLockBreak(BlockEvent.BreakEvent e){
        if(e.getPlayer().getPersistentData().getLong("TG_BiteLock") > e.getPlayer().level().getGameTime()) e.setCanceled(true);
    }

    @SubscribeEvent
    public static void serverTick(ServerTickEvent e){
        if(e.phase != TickEvent.Phase.END) return;
        ServerLevel overworld = e.getServer().overworld();
        GhoulBossArena.tick(overworld);
        RareLocations.tick(overworld);
    }
    @SubscribeEvent
    public static void join(PlayerEvent.PlayerLoggedInEvent e){
        if(!(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp)) return;
        GhoulData.get(sp).ifPresent(d -> {
            if(!d.isOriginChosen())
                NetworkHandler.CHANNEL.send(
                    net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> sp),
                    new OpenOriginPacket()
                );
            d.sync(sp);
        });
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent e){
        if(e.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp)
            GhoulData.get(sp).ifPresent(d -> d.sync(sp));
    }

    @SubscribeEvent
    public static void clone(PlayerEvent.Clone e){
        e.getOriginal().getPersistentData().getAllKeys().forEach(k ->
            e.getEntity().getPersistentData().put(k,e.getOriginal().getPersistentData().get(k))
        );
    }

    @SubscribeEvent
    public static void death(LivingDeathEvent e){
        if(e.getEntity() instanceof GhoulBoss boss && e.getEntity().level() instanceof ServerLevel sl){
            if(e.getSource().getEntity() instanceof Player p){
                p.getInventory().placeItemBackInInventory(new ItemStack(ModItems.BOSS_QUINQUE.get()));
                p.displayClientMessage(Component.literal("§4§lУникальная Квинке получена: §cПожиратель"), false);
            } else {
                boss.spawnAtLocation(new ItemStack(ModItems.BOSS_QUINQUE.get()));
            }
            GhoulBossArena.onBossDeath(sl);
            return;
        }
        if(!(e.getSource().getEntity() instanceof Player p) || e.getEntity()==p) return;

        GhoulData.get(p).ifPresent(d -> {
            d.addPoints(1);
            d.addRC(1);

            if(d.getRace()==GhoulData.Race.GHOUL || d.getRace()==GhoulData.Race.HALF_GHOUL)
                d.addRage(3);
            else if(d.getRace()==GhoulData.Race.CCG)
                d.addRage(3);

            int q=p.getPersistentData().getInt("GhoulQuestKills")+1;
            p.getPersistentData().putInt("GhoulQuestKills",q);
            if(q>=10){
                d.addPoints(5);
                d.addRC(5);
                if(d.getRace()!=GhoulData.Race.CCG) d.addRage(3);
                else d.addRage(3);
                p.getPersistentData().putInt("GhoulQuestKills",0);
                p.displayClientMessage(Component.literal(
                    "§6Квест выполнен: Охота ×10 — §e+5 очков, +5 RC"
                ),false);
            }

            d.sync(p);
            p.displayClientMessage(Component.literal(
                d.getRace()==GhoulData.Race.CCG ? "§b+3 боевого духа" : "§c+5 ярости"
            ),true);
        });
    }

    @SubscribeEvent
    public static void eatMeat(LivingEntityUseItemEvent.Finish e){
        if(!(e.getEntity() instanceof Player p)) return;
        ItemStack stack=e.getItem();
        if(stack.isEmpty()) return;

        GhoulData.get(p).ifPresent(d -> {
            if(d.getRace()!=GhoulData.Race.GHOUL && d.getRace()!=GhoulData.Race.HALF_GHOUL) return;
            boolean flesh=stack.is(Items.ROTTEN_FLESH);
            var food=stack.getFoodProperties(p);
            boolean meat=food!=null && food.isMeat();
            if(flesh || meat){
                int gain=flesh?6:3;
                d.addRage(gain);
                d.addRC(2);
                d.setHunger(Math.min(100, d.getHunger() + (flesh ? 35 : 20)));
                d.sync(p);
                p.displayClientMessage(Component.literal("§4Плоть поглощена: +"+gain+" ярости"),true);
            }
        });
    }

    @SubscribeEvent
    public static void hurt(LivingHurtEvent e){
        if(!(e.getSource().getEntity() instanceof Player p)) return;

        GhoulData.get(p).ifPresent(d -> {
            boolean quinque = p.getMainHandItem().getItem() instanceof com.tokyoghoul.rpg.item.QuinqueItem || p.getMainHandItem().is(ModItems.QUINQUE_BLADE.get()) || p.getMainHandItem().is(ModItems.CCG_MASTER_QUINQUE.get()) || p.getMainHandItem().is(ModItems.BOSS_QUINQUE.get());
            if(d.getRace() == GhoulData.Race.CCG && quinque) {
                var stack = p.getMainHandItem();
                if(stack.getOrCreateTag().getBoolean("TG_StrikeReady")) {
                    stack.getOrCreateTag().putBoolean("TG_StrikeReady", false);
                    p.getPersistentData().putLong("TG_QuinqueStrikeCooldown", p.level().getGameTime() + 20L * 30L);
                    e.setAmount(e.getAmount() * 3.0F);
                    d.addRage(5);
                    d.sync(p);
                    p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1, false, false, false));
                    if(p.level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.CRIT, p.getX(), p.getY()+1.0, p.getZ(), 18, .5,.6,.5,.08);
                    p.displayClientMessage(Component.literal("§b§lКВИНКЕ: СИЛЬНЫЙ УДАР"), true);
                }
            }
            if(!d.isRageActive()) return;

            if(d.getRace()==GhoulData.Race.CCG)
                e.setAmount(e.getAmount()*(1.12f + d.getTactics()*0.015f));
            else
                e.setAmount(e.getAmount()*(1.5f + d.getStrength()*0.03f));
        });
    }

    @SubscribeEvent
    public static void quinqueBleed(LivingEvent.LivingTickEvent e){
        LivingEntity entity=e.getEntity();
        int ticks=entity.getPersistentData().getInt("TG_QuinqueBleed");
        if(ticks<=0)return;
        ticks--; entity.getPersistentData().putInt("TG_QuinqueBleed",ticks);
        if(ticks%20==0) entity.hurt(entity.damageSources().generic(),1.0f);
        if(entity.level() instanceof ServerLevel sl && ticks%6==0) sl.sendParticles(ParticleTypes.DAMAGE_INDICATOR,entity.getX(),entity.getY()+entity.getBbHeight()*.6,entity.getZ(),2,.12,.15,.12,.02);
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent e){
        if(e.phase!=TickEvent.Phase.END || e.player.level().isClientSide) return;

        GhoulData.get(e.player).ifPresent(d -> {
            d.tick();

            if(d.isRageActive()){
                if(d.getRace()==GhoulData.Race.CCG){
                    e.player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,25,0,false,false,false));
                    e.player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,25,0,false,false,false));
                } else {
                    e.player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,25,1,false,false,false));
                    e.player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,25,1,false,false,false));
                    e.player.addEffect(new MobEffectInstance(
                        MobEffects.REGENERATION,25,Math.min(2,d.getRegen()/5),false,false,false
                    ));
                }

                if(e.player.level() instanceof ServerLevel sl && e.player.tickCount%2==0){
                    double x=e.player.getX(), y=e.player.getY()+1.0, z=e.player.getZ();
                    if(d.getRace()==GhoulData.Race.CCG){
                        sl.sendParticles(new DustParticleOptions(new Vector3f(0.05f,0.45f,0.9f),1.0f),
                            x,y,z,7,0.65,0.9,0.65,0.01);
                        sl.sendParticles(ParticleTypes.END_ROD,x,y,z,2,0.3,0.5,0.3,0.01);
                    } else {
                        sl.sendParticles(new DustParticleOptions(new Vector3f(0.55f,0.01f,0.03f),1.3f),
                            x,y,z,10,0.75,1.0,0.75,0.02);
                        sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,x,y,z,3,0.45,0.7,0.45,0.01);
                    }
                }
            }

            if(d.getRace()==GhoulData.Race.GHOUL || d.getRace()==GhoulData.Race.HALF_GHOUL){
                if(e.player.tickCount%100==0) d.setHunger(d.getHunger()-1);
                if(d.isKaguneActive() && e.player.tickCount%10==0)
                    e.player.heal(0.15f+d.getRegen()*0.03f);
                boolean critical=e.player.getHealth()<=e.player.getMaxHealth()*0.20f && d.getHunger()<=20;
                if(critical && d.getBleedingTicks()<=0) d.setBleedingTicks(20*30);
            } else if(d.getRace()==GhoulData.Race.CCG){
                if(e.player.tickCount%300==0) d.setHunger(d.getHunger()-1);
                if(e.player.getHealth()<=e.player.getMaxHealth()*0.15f && d.getBleedingTicks()<=0)
                    d.setBleedingTicks(20*30);
            }

            if(d.getBleedingTicks()>0){
                if(e.player.tickCount%20==0){
                    e.player.hurt(e.player.damageSources().generic(), 0.35f);
                }
                if(d.getBleedingTicks()==1){
                    e.player.hurt(e.player.damageSources().fellOutOfWorld(), 1000f);
                }
            }

            if(e.player.tickCount%10==0)
                d.sync(e.player);
        });
    }
}
