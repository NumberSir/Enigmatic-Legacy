package com.integral.enigmaticlegacy.triggers;

import javax.annotation.Nonnull;

import com.google.gson.JsonObject;
import com.integral.enigmaticlegacy.EnigmaticLegacy;

import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.world.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.ResourceLocation;

/**
 * Special trigger that activates when player drinks from Unholy Grail.
 * @author Integral
 */

public class UseUnholyGrailTrigger extends AbstractCriterionTrigger<UseUnholyGrailTrigger.Instance> {
	public static final ResourceLocation ID = new ResourceLocation(EnigmaticLegacy.MODID, "unholy_grail_drink");
	public static final UseUnholyGrailTrigger INSTANCE = new UseUnholyGrailTrigger();

	private UseUnholyGrailTrigger() {}

	@Nonnull
	@Override
	public ResourceLocation getId() {
		return UseUnholyGrailTrigger.ID;
	}

	@Nonnull
	@Override
	public UseUnholyGrailTrigger.Instance createInstance(@Nonnull JsonObject json, @Nonnull EntityPredicate.AndPredicate playerPred, ConditionArrayParser conditions) {
		return new UseUnholyGrailTrigger.Instance(playerPred);
	}

	public void trigger(ServerPlayerEntity player) {
		this.trigger(player, instance -> instance.test());
	}

	static class Instance extends CriterionInstance {
		Instance(EntityPredicate.AndPredicate playerPred) {
			super(UseUnholyGrailTrigger.ID, playerPred);
		}

		@Nonnull
		@Override
		public ResourceLocation getCriterion() {
			return UseUnholyGrailTrigger.ID;
		}

		boolean test() {
			return true;
		}
	}
}