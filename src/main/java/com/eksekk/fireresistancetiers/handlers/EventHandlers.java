package com.eksekk.fireresistancetiers.handlers;

import java.lang.reflect.*;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.Float;

import com.eksekk.fireresistancetiers.config.ConfigFields;
import com.eksekk.fireresistancetiers.util.Reference;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraft.potion.PotionType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.registries.IForgeRegistry;

@EventBusSubscriber
public class EventHandlers
{
	@SubscribeEvent
	public static void onLivingAttack(LivingAttackEvent event) throws IllegalAccessException, InvocationTargetException
	{
		if (!event.getSource().isFireDamage() || event.getEntity().world.isRemote)
		{
			return;
		}
		
		EntityLivingBase entity = event.getEntityLiving();
		PotionEffect effect = entity.getActivePotionEffect(MobEffects.FIRE_RESISTANCE);
		if (effect == null)
		{
			return;
		}
		
		if (effect.getAmplifier() + 1 >= ConfigFields.numberOfTiers)
		{
			return; //will be immunized automatically by vanilla code
		}
		else
		{
			//System.out.println("before reduction = " + event.getAmount());
			float damage = event.getAmount() * (1 - ((effect.getAmplifier() + 1) / (float)ConfigFields.numberOfTiers));
			//System.out.println("after reduction = " + damage);

			if (entity instanceof EntityPlayer)
			{
				PlayerAttackHandler.attackEntityFrom((EntityPlayer)entity, event.getSource(), damage);
			}
			else
			{
				EntityAttackHandler.attackEntityFrom(entity, event.getSource(), damage);
			}
		}
	}
	
	@SubscribeEvent
	public static void registerPotionTypes(RegistryEvent.Register<PotionType> event)
	{
		IForgeRegistry<PotionType> registry = event.getRegistry();
		//public PotionEffect(Potion potionIn, int durationIn, int amplifierIn)
		PotionEffect normal = new PotionEffect(MobEffects.FIRE_RESISTANCE, 3600, ConfigFields.potionAmplifier);
		PotionEffect long_ = new PotionEffect(MobEffects.FIRE_RESISTANCE, 9600, ConfigFields.potionAmplifier);
		PotionEffect strong = new PotionEffect(MobEffects.FIRE_RESISTANCE, 1800, ConfigFields.strongPotionAmplifier);
		registry.registerAll(new PotionType("fire_resistance", normal).setRegistryName("minecraft:fire_resistance"),
							 new PotionType("fire_resistance", long_).setRegistryName("minecraft:long_fire_resistance"),
							 new PotionType("fire_resistance", strong).setRegistryName("strong_fire_resistance"));
		
		if (ConfigFields.strongPotionAmplifier > ConfigFields.potionAmplifier)
		{
			PotionHelper.addMix(PotionTypes.FIRE_RESISTANCE, Item.getByNameOrId("glowstone_dust"), registry.getValue(new ResourceLocation(Reference.MOD_ID, "strong_fire_resistance")));
		}
	}
	
	@SubscribeEvent
	public static void onConfigChanged(OnConfigChangedEvent event)
	{
		if (event.getModID().equals(Reference.MOD_ID))
		{
			ConfigManager.sync(Reference.MOD_ID, Config.Type.INSTANCE);
		}
	}
}
