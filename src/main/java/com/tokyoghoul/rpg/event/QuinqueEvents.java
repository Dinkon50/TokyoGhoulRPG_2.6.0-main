package com.tokyoghoul.rpg.event;
import com.tokyoghoul.rpg.capability.GhoulData;
import com.tokyoghoul.rpg.item.QuinqueItem;
import com.tokyoghoul.rpg.init.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber public final class QuinqueEvents {
 private static boolean q(ItemStack s){return s.getItem() instanceof QuinqueItem||s.is(ModItems.QUINQUE_BLADE.get())||s.is(ModItems.CCG_MASTER_QUINQUE.get())||s.is(ModItems.BOSS_QUINQUE.get());}
 @SubscribeEvent public static void attack(AttackEntityEvent e){if(q(e.getEntity().getMainHandItem())&&GhoulData.get(e.getEntity()).map(d->d.getRace()==GhoulData.Race.GHOUL||d.getRace()==GhoulData.Race.HALF_GHOUL).orElse(false))e.setCanceled(true);}
 @SubscribeEvent public static void block(PlayerInteractEvent.LeftClickBlock e){if(q(e.getEntity().getMainHandItem())&&GhoulData.get(e.getEntity()).map(d->d.getRace()==GhoulData.Race.GHOUL||d.getRace()==GhoulData.Race.HALF_GHOUL).orElse(false))e.setCanceled(true);}
 @SubscribeEvent public static void hurt(LivingHurtEvent e){if(!(e.getSource().getEntity() instanceof Player p)||!q(p.getMainHandItem()))return;GhoulData.get(p).ifPresent(d->{if(d.getRace()==GhoulData.Race.GHOUL||d.getRace()==GhoulData.Race.HALF_GHOUL)e.setCanceled(true);else if(d.getRace()==GhoulData.Race.HUMAN)e.setAmount(e.getAmount()*.45f);else if(d.getRace()==GhoulData.Race.CCG){float m=d.getQuinqueMastery();e.setAmount(e.getAmount()*(1f+m*.035f));if(m>=2&&d.quinqueBleed()&&p.getRandom().nextFloat()<.30f)e.getEntity().getPersistentData().putInt("TG_QuinqueBleed",60+(int)m*10);if(m>=3&&d.quinquePoison()&&p.getRandom().nextFloat()<.25f)e.getEntity().addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON,45,0));if(m>=3&&d.quinqueBlind()&&p.getRandom().nextFloat()<.22f)e.getEntity().addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.BLINDNESS,30,0));if(m>=3&&d.quinqueSlow()&&p.getRandom().nextFloat()<.28f)e.getEntity().addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,50,1));}});}
}
