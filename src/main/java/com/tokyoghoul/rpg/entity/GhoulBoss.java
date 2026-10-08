package com.tokyoghoul.rpg.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;

public class GhoulBoss extends PathfinderMob {
    private int animationTicks = 0;
    private int animationKind = 0;
    private boolean waveUsed = false;
    private int noDamageTicks = 0;
    private boolean healing = false;

    public GhoulBoss(EntityType<? extends GhoulBoss> type, Level level) {
        super(type, level);
        xpReward = 80;
        setCustomName(Component.literal("§4§lАрамэ — Пожиратель"));
        setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 400.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.30D)
            .add(Attributes.ATTACK_DAMAGE, 20.0D)
            .add(Attributes.ATTACK_KNOCKBACK, 1.1D)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.95D)
            .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15D, true));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 18.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<Player>(this, Player.class, 32, true, false, entity -> entity.isAlive()));
    }

    @Override public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (animationTicks > 0) animationTicks--;
            return;
        }

        if (animationTicks > 0) animationTicks--;
        noDamageTicks++;
        LivingTargetTracker();
        if (tickCount % 20 == 0 && getHealth() < getMaxHealth() && (noDamageTicks >= 600 || distanceToNearestPlayer() > 40.0D)) {
            healing = true;
            heal((float)(getMaxHealth() / 60.0D));
            if (level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 2.2D, getZ(), 12, 1.0, 1.2, 1.0, 0.02);
            }
        }
        if (distanceToNearestPlayer() <= 40.0D && noDamageTicks < 600) healing = false;

        if (!waveUsed && getHealth() <= getMaxHealth() * 0.20F && animationTicks == 0) {
            animationKind = 1;
            animationTicks = 50;
            waveUsed = true;
        }

        if (animationKind == 1 && animationTicks == 25) {
            if (level() instanceof ServerLevel sl) {
                sl.playSound(null, blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, getSoundSource(), 1.4F, 0.55F);
                sl.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 1.0D, getZ(), 8, 2.0, 0.2, 2.0, 0.0);
                for (Player p : sl.getEntitiesOfClass(Player.class, getBoundingBox().inflate(10.0D))) {
                    if (p.isAlive()) {
                        p.hurt(sl.damageSources().mobAttack(this), 22.0F);
                        double dx = p.getX() - getX();
                        double dz = p.getZ() - getZ();
                        double len = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
                        p.push(dx / len * 1.15D, 0.45D, dz / len * 1.15D);
                    }
                }
            }
            animationKind = 0;
        }

        // A restrained aura hugs the actual hitbox while the boss is below 20% HP.
        if (getHealth() <= getMaxHealth() * 0.20F && tickCount % 3 == 0 && level() instanceof ServerLevel sl) {
            double half = getBbWidth() * 0.55D;
            double minX = getX() - half, maxX = getX() + half;
            double minZ = getZ() - half, maxZ = getZ() + half;
            double y = getY() + 0.12D + level().random.nextDouble() * Math.max(0.25D, getBbHeight() - 0.2D);
            int side = level().random.nextInt(4);
            double px = side == 0 ? minX : side == 1 ? maxX : minX + level().random.nextDouble() * (maxX - minX);
            double pz = side == 2 ? minZ : side == 3 ? maxZ : minZ + level().random.nextDouble() * (maxZ - minZ);
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, px, y, pz, 2, 0.04D, 0.08D, 0.04D, 0.002D);
        }

        if (tickCount % 45 == 0) {
            Player target = nearestPlayer(14.0D);
            if (target != null && target.isAlive()) {
                animationKind = 2;
                animationTicks = 24;
                if (level() instanceof ServerLevel sl) sl.playSound(null, blockPosition(), SoundEvents.WARDEN_ATTACK_IMPACT, getSoundSource(), 1.1F, 0.65F);
                target.hurt(level().damageSources().mobAttack(this), 16.0F);
            }
        }
    }

    private void LivingTargetTracker() {
        Player p = nearestPlayer(18.0D);
        if (p != null) getLookControl().setLookAt(p, 30.0F, 30.0F);
        if (p != null && p.distanceTo(this) <= 18.0F) noDamageTicks = Math.max(0, noDamageTicks);
    }

    private Player nearestPlayer(double range) {
        if (!(level() instanceof ServerLevel sl)) return null;
        return sl.getNearestPlayer(this, range);
    }

    private double distanceToNearestPlayer() {
        Player p = nearestPlayer(128.0D);
        return p == null ? Double.MAX_VALUE : distanceTo(p);
    }

    public int getAnimationTicks() { return animationTicks; }
    public int getAnimationKind() { return animationKind; }
    public boolean isHealing() { return healing; }

    @Override public boolean hurt(DamageSource source, float amount) {
        boolean hit = super.hurt(source, amount);
        if (hit) {
            noDamageTicks = 0;
            healing = false;
            if (animationTicks == 0) {
                animationKind = 3;
                animationTicks = 10;
            }
        }
        return hit;
    }

    @Override protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source) {
        if (level() instanceof ServerLevel sl) sl.playSound(null, blockPosition(), SoundEvents.WARDEN_HURT, getSoundSource(), 1.25F, 0.72F);
        return SoundEvents.WARDEN_HURT;
    }
    @Override protected net.minecraft.sounds.SoundEvent getDeathSound() { return SoundEvents.WARDEN_DEATH; }

    @Override protected net.minecraft.sounds.SoundEvent getAmbientSound() { return SoundEvents.WARDEN_AMBIENT; }
    @Override protected float getSoundVolume() { return 1.2F; }
    @Override public float getVoicePitch() { return 0.55F; }
    @Override public boolean isPersistenceRequired() { return true; }
    @Override public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }
}
