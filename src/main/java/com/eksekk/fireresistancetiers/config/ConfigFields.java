package com.eksekk.fireresistancetiers.config;

import java.util.ArrayList;
import java.util.List;

import com.eksekk.fireresistancetiers.util.Reference;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.RequiresMcRestart;

@Config(modid = Reference.MOD_ID, name = Reference.MOD_ID)
public class ConfigFields
{
	@Comment({"Number of fire resistance tiers",
			  "one tier gives (1 / MAX_TIERS) * 100% damage reduction",
			  "final tier or more gives immunity"})
	public static int numberOfTiers = 1;
	
	@Comment({"Vanilla fire resistance potion amplifier (starting from 0)"})
	@RequiresMcRestart
	public static int potionAmplifier = 0;
	
	@Comment({"Vanilla fire resistance strengthened potion amplifier (starting from 0)",
			  "Set to potionAmplifier value to disable",
			  "If disabled, no brewing recipe will be available"})
	@RequiresMcRestart
	public static int strongPotionAmplifier = 0;
}
