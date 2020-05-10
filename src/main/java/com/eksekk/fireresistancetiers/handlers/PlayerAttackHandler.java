package com.eksekk.fireresistancetiers.handlers;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

public class PlayerAttackHandler
{
	public static final Class PLAYER_CLASS = EntityPlayer.class;
	public static Class ENTITY_CLASS = EntityLivingBase.class;
	
	public static final Method canBlockDamageSource = ObfuscationReflectionHelper.findMethod(ENTITY_CLASS, "func_184583_d", boolean.class, DamageSource.class);
	public static final Method damageShield = ObfuscationReflectionHelper.findMethod(PLAYER_CLASS, "func_184590_k", void.class, float.class);
	public static final Method blockUsingShield = ObfuscationReflectionHelper.findMethod(PLAYER_CLASS, "func_190629_c", void.class, EntityLivingBase.class);
	public static final Method damageEntity = ObfuscationReflectionHelper.findMethod(PLAYER_CLASS, "func_70665_d", void.class, DamageSource.class, float.class);
	public static final Method markVelocityChanged = ObfuscationReflectionHelper.findMethod(ENTITY_CLASS, "func_70018_K", void.class);
	public static final Method checkTotemDeathProtection = ObfuscationReflectionHelper.findMethod(ENTITY_CLASS, "func_190628_d", boolean.class, DamageSource.class);
	public static final Method getDeathSound = ObfuscationReflectionHelper.findMethod(PLAYER_CLASS, "func_184615_bR", SoundEvent.class);
	public static final Method getSoundVolume = ObfuscationReflectionHelper.findMethod(ENTITY_CLASS, "func_70599_aP", float.class);
	public static final Method getSoundPitch = ObfuscationReflectionHelper.findMethod(ENTITY_CLASS, "func_70647_i", float.class);
	public static final Method playHurtSound = ObfuscationReflectionHelper.findMethod(ENTITY_CLASS, "func_184581_c", void.class, DamageSource.class);
	public static final Method spawnShoulderEntities = ObfuscationReflectionHelper.findMethod(PLAYER_CLASS, "func_192030_dh", void.class);
	
	public static final Field lastDamage = ObfuscationReflectionHelper.findField(ENTITY_CLASS, "field_110153_bc");
	public static final Field recentlyHit = ObfuscationReflectionHelper.findField(ENTITY_CLASS, "field_70718_bc");
	public static final Field attackingPlayer = ObfuscationReflectionHelper.findField(ENTITY_CLASS, "field_70717_bb");
	public static final Field lastDamageSource = ObfuscationReflectionHelper.findField(ENTITY_CLASS, "field_189750_bF");
	public static final Field lastDamageStamp = ObfuscationReflectionHelper.findField(ENTITY_CLASS, "field_189751_bG");
	public static final Field idleTime = ObfuscationReflectionHelper.findField(ENTITY_CLASS, "field_70708_bq");
	
	public static boolean attackEntityFrom(EntityPlayer entity, DamageSource source, float amount) throws IllegalAccessException, InvocationTargetException
    {
        if (entity.isEntityInvulnerable(source))
        {
            return false;
        }
        else if (entity.capabilities.disableDamage && !source.canHarmInCreative())
        {
            return false;
        }
        else
        {
        	idleTime.set(entity, 0);

            if (entity.getHealth() <= 0.0F)
            {
                return false;
            }
            else
            {
                if (entity.isPlayerSleeping() && !entity.world.isRemote)
                {
                	entity.wakeUpPlayer(true, true, false);
                }

                spawnShoulderEntities.invoke(entity);

                if (source.isDifficultyScaled())
                {
                    if (entity.world.getDifficulty() == EnumDifficulty.PEACEFUL)
                    {
                        amount = 0.0F;
                    }

                    if (entity.world.getDifficulty() == EnumDifficulty.EASY)
                    {
                        amount = Math.min(amount / 2.0F + 1.0F, amount);
                    }

                    if (entity.world.getDifficulty() == EnumDifficulty.HARD)
                    {
                        amount = amount * 3.0F / 2.0F;
                    }
                }

                return amount == 0.0F ? false : attackEntityFromInternal(entity, source, amount);
            }
        }
    }
	
	public static boolean attackEntityFromInternal(EntityPlayer entity, DamageSource source, float amount) throws IllegalAccessException, InvocationTargetException
    {
		if (entity.isEntityInvulnerable(source))
        {
            return false;
        }
        else if (entity.world.isRemote)
        {
            return false;
        }
        else
        {
        	idleTime.set(entity, 0);

            if (entity.getHealth() <= 0.0F)
            {
                return false;
            }
            else
            {
		        float f = amount;
		
		        boolean flag = false;
		
		        if (amount > 0.0F && (boolean)canBlockDamageSource.invoke(entity, source))
		        {
		            damageShield.invoke(entity, amount);
		            amount = 0.0F;
		
		            if (!source.isProjectile())
		            {
		                Entity sourceEntity = source.getImmediateSource();
		
		                if (sourceEntity instanceof EntityLivingBase)
		                {
		                    blockUsingShield.invoke(entity, (EntityLivingBase)sourceEntity);
		                }
		            }
		
		            flag = true;
		        }
		        //
		
		        entity.limbSwingAmount = 1.5F;
		        boolean flag1 = true;
		
		        if ((float)entity.hurtResistantTime > (float)entity.maxHurtResistantTime / 2.0F)
		        {
		            if (amount <= (float)lastDamage.get(entity))
		            {
		                return false;
		            }
		
		            damageEntity.invoke(entity, source, amount - (float)lastDamage.get(entity));
		            lastDamage.set(entity, amount);
		            flag1 = false;
		        }
		        else
		        {
		        	lastDamage.set(entity, amount);
		            entity.hurtResistantTime = entity.maxHurtResistantTime;
		            damageEntity.invoke(entity, source, amount);
		            entity.maxHurtTime = 10;
		            entity.hurtTime = entity.maxHurtTime;
		        }
		
		        entity.attackedAtYaw = 0.0F;
		        Entity entity1 = source.getTrueSource();
		
		        if (entity1 != null)
		        {
		            if (entity1 instanceof EntityLivingBase)
		            {
		                entity.setRevengeTarget((EntityLivingBase)entity1);
		            }
		
		            if (entity1 instanceof EntityPlayer)
		            {
		                recentlyHit.set(entity, 100);
		                attackingPlayer.set(entity, (EntityPlayer)entity1);
		            }
		            else if (entity1 instanceof net.minecraft.entity.passive.EntityTameable)
		            {
		                net.minecraft.entity.passive.EntityTameable entitywolf = (net.minecraft.entity.passive.EntityTameable)entity1;
		
		                if (entitywolf.isTamed())
		                {
		                	recentlyHit.set(entity, 100);
		                	attackingPlayer.set(entity, null);
		                }
		            }
		        }
		
		        if (flag1)
		        {
		            if (flag)
		            {
		                entity.world.setEntityState(entity, (byte)29);
		            }
		            else
		            {
		                byte b0;
		                b0 = 37;
		
		                entity.world.setEntityState(entity, b0);
		            }
		
		            if (source != DamageSource.DROWN && (!flag || amount > 0.0F))
		            {
		                markVelocityChanged.invoke(entity);
		            }
		
		            if (entity1 != null)
		            {
		                double d1 = entity1.posX - entity.posX;
		                double d0;
		
		                for (d0 = entity1.posZ - entity.posZ; d1 * d1 + d0 * d0 < 1.0E-4D; d0 = (Math.random() - Math.random()) * 0.01D)
		                {
		                    d1 = (Math.random() - Math.random()) * 0.01D;
		                }
		
		                entity.attackedAtYaw = (float)(MathHelper.atan2(d0, d1) * (180D / Math.PI) - (double)entity.rotationYaw);
		                entity.knockBack(entity1, 0.4F, d1, d0);
		            }
		            else
		            {
		                entity.attackedAtYaw = (float)((int)(Math.random() * 2.0D) * 180);
		            }
		        }
		
		        if (entity.getHealth() <= 0.0F)
		        {
		            if (!(boolean)checkTotemDeathProtection.invoke(entity, source))
		            {
		                SoundEvent soundevent = (SoundEvent)getDeathSound.invoke(entity);
		
		                if (flag1 && soundevent != null)
		                {
		                    entity.playSound(soundevent, (float)getSoundVolume.invoke(entity), (float)getSoundPitch.invoke(entity));
		                }
		
		                entity.onDeath(source);
		            }
		        }
		        else if (flag1)
		        {
		            playHurtSound.invoke(entity, source);
		        }
		
		        boolean flag2 = !flag || amount > 0.0F;
		
		        if (flag2)
		        {
		            lastDamageSource.set(entity, source);
		            lastDamageStamp.set(entity, entity.world.getTotalWorldTime());
		        }
		
		        if (entity instanceof EntityPlayerMP)
		        {
		            CriteriaTriggers.ENTITY_HURT_PLAYER.trigger((EntityPlayerMP)entity, source, f, amount, flag);
		        }
		
		        if (entity1 instanceof EntityPlayerMP)
		        {
		            CriteriaTriggers.PLAYER_HURT_ENTITY.trigger((EntityPlayerMP)entity1, entity, source, f, amount, flag);
		        }
		
		        return flag2;
            }
        }
    }
}
