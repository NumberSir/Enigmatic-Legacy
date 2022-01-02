package com.integral.enigmaticlegacy.entities;

import java.util.UUID;

import javax.annotation.Nullable;

import com.integral.enigmaticlegacy.EnigmaticLegacy;
import com.integral.enigmaticlegacy.helpers.ItemNBTHelper;
import com.integral.enigmaticlegacy.items.SoulCrystal;
import com.integral.enigmaticlegacy.items.StorageCrystal;
import com.integral.enigmaticlegacy.packets.clients.PacketHandleItemPickup;
import com.integral.enigmaticlegacy.packets.clients.PacketRecallParticles;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.PlayerEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.stats.Stats;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.registries.ObjectHolder;

/**
 * Modified copy of ItemEntity that has special properties
 * and is not recognized as it's instance.
 * @author Integral
 */

public class PermanentItemEntity extends Entity {
	private static final DataParameter<ItemStack> ITEM = EntityDataManager.defineId(PermanentItemEntity.class, DataSerializers.ITEM_STACK);
	private int age;
	private int pickupDelay;
	private int health = 5;
	private UUID thrower;
	private UUID owner;

	@ObjectHolder(EnigmaticLegacy.MODID + ":permanent_item_entity")
	public static EntityType<PermanentItemEntity> TYPE;

	public float hoverStart = (float) (Math.random() * Math.PI * 2.0D);

	public PermanentItemEntity(EntityType<PermanentItemEntity> type, World world) {
		super(type, world);
	}

	public PermanentItemEntity(World worldIn, double x, double y, double z) {
		this(PermanentItemEntity.TYPE, worldIn);
		this.setPos(x, y <= 0 ? 1 : y, z);
		this.yRot = this.random.nextFloat() * 360.0F;
		this.setInvulnerable(true);

		this.setNoGravity(true);
	}

	public PermanentItemEntity(World worldIn, double x, double y, double z, ItemStack stack) {
		this(worldIn, x, y, z);
		this.setItem(stack);
	}

	@OnlyIn(Dist.CLIENT)
	private PermanentItemEntity(PermanentItemEntity p_i231561_1_) {
		super(p_i231561_1_.getType(), p_i231561_1_.level);
		this.setItem(p_i231561_1_.getItem().copy());
		this.copyPosition(p_i231561_1_);
		this.age = p_i231561_1_.age;
		this.hoverStart = p_i231561_1_.hoverStart;
	}

	@OnlyIn(Dist.CLIENT)
	public PermanentItemEntity copy() {
		return new PermanentItemEntity(this);
	}

	@Override
	protected boolean isMovementNoisy() {
		return false;
	}

	@Override
	protected void defineSynchedData() {
		this.getEntityData().define(PermanentItemEntity.ITEM, ItemStack.EMPTY);
	}

	@Override
	public void tick() {

		if (this.getItem().isEmpty()) {
			this.remove();
		} else {
			super.tick();

			if (this.pickupDelay > 0 && this.pickupDelay != 32767) {
				--this.pickupDelay;
			}

			this.xo = this.getX();
			this.yo = this.getY();
			this.zo = this.getZ();
			Vector3d vec3d = this.getDeltaMovement();

			if (!this.isNoGravity()) {
				this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
			}

			if (this.level.isClientSide) {
				this.noPhysics = false;

				this.level.addParticle(ParticleTypes.PORTAL, this.getX(), this.getY() + (this.getBbHeight() / 2), this.getZ(), ((Math.random() - 0.5) * 2.0), ((Math.random() - 0.5) * 2.0), ((Math.random() - 0.5) * 2.0));
			}

			++this.age;

			if (!this.level.isClientSide) {
				double d0 = this.getDeltaMovement().subtract(vec3d).lengthSqr();
				if (d0 > 0.01D) {
					this.hasImpulse = true;
				}
			}

			ItemStack item = this.getItem();

			if (item.isEmpty()) {
				this.remove();
			}

			// Portal Cooldown
			this.portalCooldown = Short.MAX_VALUE;

		}
	}

	@Override
	public Entity changeDimension(ServerWorld server, ITeleporter teleporter) {
		return null;
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (this.level.isClientSide || !this.isAlive())
			return false;

		if (source.isBypassMagic()) {
			EnigmaticLegacy.logger.warn("[WARN] Attacked permanent item entity with absolute DamageSource: " + source);
			this.remove();
			return true;
		} else
			return false;

	}

	@Override
	public void remove() {
		EnigmaticLegacy.logger.warn("[WARN] Removing Permanent Item Entity: " + this);
		super.remove();
	}

	@Override
	public void addAdditionalSaveData(CompoundNBT compound) {
		compound.putShort("Health", (short) this.health);
		compound.putShort("Age", (short) this.age);
		compound.putShort("PickupDelay", (short) this.pickupDelay);
		if (this.getThrowerId() != null) {
			compound.putUUID("Thrower", this.getThrowerId());
		}

		if (this.getOwnerId() != null) {
			compound.putUUID("Owner", this.getOwnerId());
		}

		if (!this.getItem().isEmpty()) {
			compound.put("Item", this.getItem().save(new CompoundNBT()));
		}

	}

	@Override
	public void readAdditionalSaveData(CompoundNBT compound) {
		this.health = compound.getShort("Health");
		this.age = compound.getShort("Age");
		if (compound.contains("PickupDelay")) {
			this.pickupDelay = compound.getShort("PickupDelay");
		}

		if (compound.contains("Owner")) {
			this.owner = compound.getUUID("Owner");
		}

		if (compound.contains("Thrower")) {
			this.thrower = compound.getUUID("Thrower");
		}

		CompoundNBT compoundnbt = compound.getCompound("Item");
		this.setItem(ItemStack.of(compoundnbt));
		if (this.getItem().isEmpty()) {
			this.remove();
		}

	}

	@Override
	public void playerTouch(PlayerEntity player) {
		if (!this.level.isClientSide) {
			if (this.pickupDelay > 0)
				return;
			ItemStack itemstack = this.getItem();
			Item item = itemstack.getItem();
			int i = itemstack.getCount();

			ItemStack copy = itemstack.copy();
			boolean isPlayerOwner = player.getUUID().equals(this.getOwnerId());
			boolean allowPickUp = false;

			if (item instanceof StorageCrystal && (isPlayerOwner || !EnigmaticLegacy.enigmaticAmulet.isVesselOwnerOnly())) {
				allowPickUp = true;
			} else if (item instanceof SoulCrystal && isPlayerOwner) {
				allowPickUp = true;
			}

			if (allowPickUp) {

				if (item instanceof StorageCrystal) {
					CompoundNBT crystalNBT = ItemNBTHelper.getNBT(itemstack);
					ItemStack embeddedSoul = crystalNBT.contains("embeddedSoul") ? ItemStack.of(crystalNBT.getCompound("embeddedSoul")) : null;

					if (!isPlayerOwner && embeddedSoul != null)
						return;

					EnigmaticLegacy.storageCrystal.retrieveDropsFromCrystal(itemstack, player, embeddedSoul);
					/*
					if (!isPlayerOwner && embeddedSoul != null) {
						PermanentItemEntity droppedSoulCrystal = new PermanentItemEntity(this.world, this.getPosX(), this.getPosY(), this.getPosZ(), embeddedSoul);
						droppedSoulCrystal.setOwnerId(this.getOwnerId());
						this.world.addEntity(droppedSoulCrystal);
					}
					 */

				} else if (item instanceof SoulCrystal) {
					if (!EnigmaticLegacy.soulCrystal.retrieveSoulFromCrystal(player, itemstack))
						return;
				}

				EnigmaticLegacy.packetInstance.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(this.getX(), this.getY() + (this.getBbHeight() / 2), this.getZ(), 64, player.level.dimension())), new PacketRecallParticles(this.getX(), this.getY() + (this.getBbHeight() / 2), this.getZ(), 48, false));

				player.take(this, i);
				EnigmaticLegacy.packetInstance.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(this.getX(), this.getY(), this.getZ(), 64, this.level.dimension())), new PacketHandleItemPickup(player.getId(), this.getId()));

				EnigmaticLegacy.logger.info("Player " + player.getGameProfile().getName() + " picking up: " + this);
				this.remove();
				itemstack.setCount(0);

			} else if (this.pickupDelay == 0 && (this.owner == null || this.owner.equals(player.getUUID())) && (i <= 0 || player.inventory.add(itemstack))) {
				copy.setCount(copy.getCount() - this.getItem().getCount());
				if (itemstack.isEmpty()) {
					player.take(this, i);

					EnigmaticLegacy.packetInstance.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(this.getX(), this.getY(), this.getZ(), 64, this.level.dimension())), new PacketHandleItemPickup(player.getId(), this.getId()));

					EnigmaticLegacy.logger.info("Player " + player.getGameProfile().getName() + " picking up: " + this);
					this.remove();
					itemstack.setCount(i);
				}

				player.awardStat(Stats.ITEM_PICKED_UP.get(item), i);
			}

		}
	}

	@OnlyIn(Dist.CLIENT)
	public float getItemHover(float partialTicks) {
		return (this.getAge() + partialTicks) / 20.0F + this.hoverStart;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean shouldRenderAtSqrDistance(double distance) {
		double d0 = this.getBoundingBox().getSize() * 4.0D;
		if (Double.isNaN(d0)) {
			d0 = 4.0D;
		}

		d0 = d0 * 64.0D;
		return distance < d0 * d0;
	}

	@Override
	public ITextComponent getName() {
		ITextComponent itextcomponent = this.getCustomName();
		return itextcomponent != null ? itextcomponent : new TranslationTextComponent(this.getItem().getDescriptionId());
	}

	@Override
	public boolean isAttackable() {
		return false;
	}

	public ItemStack getItem() {
		return this.getEntityData().get(PermanentItemEntity.ITEM);
	}

	public void setItem(ItemStack stack) {
		this.getEntityData().set(PermanentItemEntity.ITEM, stack);
	}

	@Nullable
	public UUID getOwnerId() {
		return this.owner;
	}

	public void setOwnerId(@Nullable UUID ownerId) {
		this.owner = ownerId;
	}

	@Nullable
	public UUID getThrowerId() {
		return this.thrower;
	}

	public void setThrowerId(@Nullable UUID throwerId) {
		this.thrower = throwerId;
	}

	@OnlyIn(Dist.CLIENT)
	public int getAge() {
		return this.age;
	}

	public void setDefaultPickupDelay() {
		this.pickupDelay = 10;
	}

	public void setNoPickupDelay() {
		this.pickupDelay = 0;
	}

	public void setInfinitePickupDelay() {
		this.pickupDelay = 32767;
	}

	public void setPickupDelay(int ticks) {
		this.pickupDelay = ticks;
	}

	public boolean cannotPickup() {
		return this.pickupDelay > 0;
	}

	public void makeFakeItem() {
		this.setInfinitePickupDelay();
	}

	@Override
	public IPacket<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}