package com.integral.enigmaticlegacy.items;

import java.util.List;

import javax.annotation.Nullable;

import com.integral.enigmaticlegacy.EnigmaticLegacy;
import com.integral.enigmaticlegacy.api.generic.SubscribeConfig;
import com.integral.enigmaticlegacy.api.items.ICursed;
import com.integral.enigmaticlegacy.handlers.SuperpositionHandler;
import com.integral.enigmaticlegacy.helpers.ItemLoreHelper;
import com.integral.enigmaticlegacy.items.generic.ItemBaseCurio;
import com.integral.omniconfig.wrappers.Omniconfig;
import com.integral.omniconfig.wrappers.OmniconfigWrapper;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AvariceScroll extends ItemBaseCurio implements ICursed {
	public static Omniconfig.IntParameter emeraldChance;

	@SubscribeConfig
	public static void onConfig(OmniconfigWrapper builder) {
		builder.pushPrefix("AvariceScroll");

		emeraldChance = builder
				.comment("Chance to get an Emerald when slaying any mob with Pact of Infinite Avarice equipped.")
				.max(100).getInt("EmeraldChance", 15);

		builder.popPrefix();
	}
	public AvariceScroll() {
		super(ItemBaseCurio.getDefaultProperties().rarity(Rarity.EPIC).stacksTo(1).fireResistant());
		this.setRegistryName(new ResourceLocation(EnigmaticLegacy.MODID, "avarice_scroll"));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> list, TooltipFlag flagIn) {
		ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");

		if (Screen.hasShiftDown()) {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.avariceScroll1", ChatFormatting.GOLD, 1);
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.avariceScroll2");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.avariceScroll3");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.avariceScroll4");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.avariceScroll5", ChatFormatting.GOLD, emeraldChance.getValue() + "%");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.avariceScroll6");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.avariceScroll7");
		} else {
			ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.holdShift");
		}

		ItemLoreHelper.addLocalizedString(list, "tooltip.enigmaticlegacy.void");
		ItemLoreHelper.indicateCursedOnesOnly(list);
	}

	@Override
	public boolean canEquip(String identifier, LivingEntity living, ItemStack stack) {
		return super.canEquip(identifier, living, stack) && living instanceof Player && SuperpositionHandler.isTheCursedOne((Player)living);
	}

	@Override
	public int getFortuneBonus(String identifier, LivingEntity livingEntity, ItemStack curio, int index) {
		return super.getFortuneBonus(identifier, livingEntity, curio, index) + 1;
	}

}
