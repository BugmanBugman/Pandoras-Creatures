package andrews.pandoras_creatures.entities;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import andrews.pandoras_creatures.registry.PCEntities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

public class EndTrollBulletPoisonEntity extends Entity
{
	private LivingEntity owner;
	private Entity target;
   	@Nullable
   	private Direction direction;
   	private int steps;
   	private double targetDeltaX;
   	private double targetDeltaY;
   	private double targetDeltaZ;
   	@Nullable
   	private UUID ownerUniqueId;
   	private BlockPos ownerBlockPos;
   	@Nullable
   	private UUID targetUniqueId;
   	private BlockPos targetBlockPos;
	
	public EndTrollBulletPoisonEntity(EntityType<? extends EndTrollBulletPoisonEntity> entity, World world)
	{
		super(entity, world);
		this.noClip = true;
	}

	public EndTrollBulletPoisonEntity(FMLPlayMessages.SpawnEntity spawnEntity, World world)
	{
		this(PCEntities.END_TROLL_BULLET_POISON.get(), world);
	}
	
	public EndTrollBulletPoisonEntity(World worldIn, LivingEntity ownerIn, Entity targetIn, Direction.Axis directionAxis)
	{
		this(PCEntities.END_TROLL_BULLET_POISON.get(), worldIn);
      	this.owner = ownerIn;
      	BlockPos blockpos = ownerIn.getPosition();
        double posX = (double)blockpos.getX() + 0.5D;
      	double posY = owner.getPosition().getY() + owner.getEyeHeight() + 0.7D;
      	double posZ = (double)blockpos.getZ() + 0.5D;
      	this.setLocationAndAngles(posX, posY, posZ, this.rotationYaw, this.rotationPitch);
      	this.target = targetIn;
      	this.direction = Direction.UP;
      	this.selectNextMoveDirection(directionAxis);
	}

	@Override
	protected void writeAdditional(CompoundNBT compound)
	{
		if(this.owner != null)
		{
			BlockPos blockpos = owner.getPosition();
			CompoundNBT compoundnbt = new CompoundNBT();
			compoundnbt.putUniqueId("OwnerId", this.owner.getUniqueID());
			compoundnbt.putInt("X", blockpos.getX());
			compoundnbt.putInt("Y", blockpos.getY());
			compoundnbt.putInt("Z", blockpos.getZ());
			compound.put("Owner", compoundnbt);
		}

		if(this.target != null)
		{
			BlockPos blockpos1 = target.getPosition();
			CompoundNBT compoundnbt1 = new CompoundNBT();
			compoundnbt1.putUniqueId("TargetId", this.target.getUniqueID());
			compoundnbt1.putInt("X", blockpos1.getX());
			compoundnbt1.putInt("Y", blockpos1.getY());
         	compoundnbt1.putInt("Z", blockpos1.getZ());
         	compound.put("Target", compoundnbt1);
		}

		if(this.direction != null)
		{
			compound.putInt("Dir", this.direction.getIndex());
		}

		compound.putInt("Steps", this.steps);
		compound.putDouble("TXD", this.targetDeltaX);
		compound.putDouble("TYD", this.targetDeltaY);
		compound.putDouble("TZD", this.targetDeltaZ);
	}
	
	@Override
	protected void readAdditional(CompoundNBT compound)
	{
		this.steps = compound.getInt("Steps");
		this.targetDeltaX = compound.getDouble("TXD");
		this.targetDeltaY = compound.getDouble("TYD");
		this.targetDeltaZ = compound.getDouble("TZD");
			
		if(compound.contains("Dir", NBT.TAG_ANY_NUMERIC))
		{
			this.direction = Direction.byIndex(compound.getInt("Dir"));
		}

		if(compound.contains("Owner", NBT.TAG_COMPOUND))
		{
			CompoundNBT compoundnbt = compound.getCompound("Owner");
			this.ownerUniqueId = compoundnbt.getUniqueId("OwnerId");
			this.ownerBlockPos = new BlockPos(compoundnbt.getInt("X"), compoundnbt.getInt("Y"), compoundnbt.getInt("Z"));
		}

		if(compound.contains("Target", NBT.TAG_COMPOUND))
		{
			CompoundNBT compoundnbt1 = compound.getCompound("Target");
			this.targetUniqueId = compoundnbt1.getUniqueId("TargetId");
			this.targetBlockPos = new BlockPos(compoundnbt1.getInt("X"), compoundnbt1.getInt("Y"), compoundnbt1.getInt("Z"));
		}
	}
	
	@Override
	protected void registerData() {}
	
	@Override
	public void tick()
	{
		super.tick();
		if(!this.world.isRemote)
		{
			if(this.target == null && this.targetUniqueId != null)
			{
				for(LivingEntity livingentity : this.world.getEntitiesWithinAABB(LivingEntity.class, new AxisAlignedBB(this.targetBlockPos.add(-2, -2, -2), this.targetBlockPos.add(2, 2, 2))))
				{
					if(livingentity.getUniqueID().equals(this.targetUniqueId))
					{
						this.target = livingentity;
						break;
					}
				}

				this.targetUniqueId = null;
			}

			if(this.owner == null && this.ownerUniqueId != null)
			{
				for(LivingEntity livingentity1 : this.world.getEntitiesWithinAABB(LivingEntity.class, new AxisAlignedBB(this.ownerBlockPos.add(-2, -2, -2), this.ownerBlockPos.add(2, 2, 2))))
				{
					if(livingentity1.getUniqueID().equals(this.ownerUniqueId))
					{
						this.owner = livingentity1;
						break;
					}
				}

				this.ownerUniqueId = null;
			}

			if(this.target == null || !this.target.isAlive() || this.target instanceof PlayerEntity && ((PlayerEntity)this.target).isSpectator())
			{
				if(!this.hasNoGravity())
				{
					this.setMotion(this.getMotion().add(0.0D, -0.04D, 0.0D));
				}
			}
			else
			{
				this.targetDeltaX = MathHelper.clamp(this.targetDeltaX * 1.025D, -1.0D, 1.0D);
				this.targetDeltaY = MathHelper.clamp(this.targetDeltaY * 1.025D, -1.0D, 1.0D);
				this.targetDeltaZ = MathHelper.clamp(this.targetDeltaZ * 1.025D, -1.0D, 1.0D);
				Vector3d vec3d = this.getMotion();
				this.setMotion(vec3d.add((this.targetDeltaX - vec3d.x) * 0.2D, (this.targetDeltaY - vec3d.y) * 0.2D, (this.targetDeltaZ - vec3d.z) * 0.2D));
			}

			RayTraceResult raytraceresult = ProjectileHelper.func_234618_a_(this, this::entityHitAble);
			if(raytraceresult.getType() != RayTraceResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, raytraceresult))
			{
				this.bulletHit(raytraceresult);
			}
		}

		Vector3d vec3d1 = this.getMotion();
		this.setPosition(this.getPosX() + vec3d1.x, this.getPosY() + vec3d1.y, this.getPosZ() + vec3d1.z);
     	ProjectileHelper.rotateTowardsMovement(this, 0.5F);
     	if(this.world.isRemote)
     	{
     		this.world.addParticle(new RedstoneParticleData(0, 255, 0, 1.0F), this.getPosX() - vec3d1.x, this.getPosY() - vec3d1.y + 0.15D, this.getPosZ() - vec3d1.z, 0.0D, 0.0D, 0.0D);
     	}
     	else if(this.target != null && this.target.isAlive())
     	{
     		if(this.steps > 0)
     		{
     			--this.steps;
     			if(this.steps == 0)
     			{
     				this.selectNextMoveDirection(this.direction == null ? null : this.direction.getAxis());
     			}
     		}

     		if(this.direction != null)
     		{
     			BlockPos blockpos1 = this.getPosition();
     			Direction.Axis direction$axis = this.direction.getAxis();
     			if(this.world.isTopSolid(blockpos1.offset(this.direction), this))
     			{
     				this.selectNextMoveDirection(direction$axis);
     			}
     			else
     			{
     				BlockPos blockpos = target.getPosition();
     				if(direction$axis == Direction.Axis.X && blockpos1.getX() == blockpos.getX() || direction$axis == Direction.Axis.Z && blockpos1.getZ() == blockpos.getZ() || direction$axis == Direction.Axis.Y && blockpos1.getY() == blockpos.getY())
     				{
     					this.selectNextMoveDirection(direction$axis);
     				}
     			}
     		}
     	}
	}
	
	private boolean entityHitAble(Entity entity)
	{
		return entity != null && !(entity instanceof EndTrollEntity) && !entity.isSpectator() && entity.isAlive() && entity.canBeCollidedWith() && !entity.noClip;
	}
	
	/**
	 * Returns true if the entity is on fire. Used by render to add the fire effect on rendering.
	 */
	@Override
	public boolean isBurning()
	{
		return false;
	}	
	
	/**
	 * Gets how bright this entity is.
	 */
	@Override
	public float getBrightness()
	{
		return 1.0F;
	}
	
	/**
	 * Checks if the entity is in range to render.
	 */
	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean isInRangeToRenderDist(double distance)
	{
		return distance < 16384.0D;
	}
	
	/**
	 * Returns true if other Entities should be prevented from moving through this Entity.
	 */
	@Override
	public boolean canBeCollidedWith()
	{
		return true;
	}
	
	/**
	 * Called when the entity is attacked.
	 */
	@Override
	public boolean attackEntityFrom(DamageSource source, float amount)
	{
		if(!this.world.isRemote)
		{
			this.playSound(SoundEvents.ENTITY_SHULKER_BULLET_HURT, 1.0F, 1.0F);
			((ServerWorld)this.world).spawnParticle(ParticleTypes.CRIT, this.getPosX(), this.getPosY(), this.getPosZ(), 15, 0.2D, 0.2D, 0.2D, 0.0D);
			this.remove();
		}

		return true;
	}
	
	/**
	 * The Sound Category that the Sounds made by the Entity, should be in.
	 */
	@Override
	public SoundCategory getSoundCategory()
	{
		return SoundCategory.HOSTILE;
	}
	
	@Override
	public IPacket<?> createSpawnPacket()
	{
		return NetworkHooks.getEntitySpawningPacket(this);
	}
	
	/**
	 * Used to set the Direction.
	 */
	private void setDirection(@Nullable Direction directionIn)
	{
		this.direction = directionIn;
	}
	
	/**
	 * Used to select the next Movement Direction.
	 */
	private void selectNextMoveDirection(@Nullable Direction.Axis directionAxis)
	{
		double hightModifier = 0.5D;
		BlockPos blockpos;
		if(this.target == null)
		{
			blockpos = this.getPosition().down();
		}
		else
		{
			hightModifier = (double)this.target.getHeight() * 0.5D;
			blockpos = new BlockPos(this.target.getPosX(), this.target.getPosY() + hightModifier, this.target.getPosZ());
		}

		double posX = (double)blockpos.getX() + 0.5D;
		double posY = (double)blockpos.getY() + hightModifier;
		double posZ = (double)blockpos.getZ() + 0.5D;
		Direction direction = null;
		
		if(!blockpos.withinDistance(this.getPositionVec(), 2.0D))
		{
			BlockPos blockpos1 = this.getPosition();
			List<Direction> list = Lists.newArrayList();
			if(directionAxis != Direction.Axis.X)
			{
				if(blockpos1.getX() < blockpos.getX() && this.world.isAirBlock(blockpos1.east()))
				{
					list.add(Direction.EAST);
				}
				else if(blockpos1.getX() > blockpos.getX() && this.world.isAirBlock(blockpos1.west()))
				{
					list.add(Direction.WEST);
				}
			}

			if(directionAxis != Direction.Axis.Y)
			{
				if(blockpos1.getY() < blockpos.getY() && this.world.isAirBlock(blockpos1.up()))
				{
					list.add(Direction.UP);
				}
				else if(blockpos1.getY() > blockpos.getY() && this.world.isAirBlock(blockpos1.down()))
				{
					list.add(Direction.DOWN);
				}
			}

			if(directionAxis != Direction.Axis.Z)
			{
				if(blockpos1.getZ() < blockpos.getZ() && this.world.isAirBlock(blockpos1.south()))
				{
					list.add(Direction.SOUTH);
				}
				else if(blockpos1.getZ() > blockpos.getZ() && this.world.isAirBlock(blockpos1.north()))
				{
					list.add(Direction.NORTH);
				}	
			}

			direction = Direction.getRandomDirection(this.rand);
			if(list.isEmpty())
			{
				for(int i = 5; !this.world.isAirBlock(blockpos1.offset(direction)) && i > 0; --i)
				{
					direction = Direction.getRandomDirection(this.rand);
				}
			}
			else
			{
				direction = list.get(this.rand.nextInt(list.size()));
			}

			posX = this.getPosX() + (double)direction.getXOffset();
			posY = this.getPosY() + (double)direction.getYOffset();
         	posZ = this.getPosZ() + (double)direction.getZOffset();
		}

		this.setDirection(direction);
		double totalPosX = posX - this.getPosX();
		double totalPosY = posY - this.getPosY();
		double totalPosZ = posZ - this.getPosZ();
		double deltaTotal = (double)MathHelper.sqrt(totalPosX * totalPosX + totalPosY * totalPosY + totalPosZ * totalPosZ);
		if(deltaTotal == 0.0D)
		{
			this.targetDeltaX = 0.0D;
			this.targetDeltaY = 0.0D;
         	this.targetDeltaZ = 0.0D;
		}
		else
		{
			this.targetDeltaX = totalPosX / deltaTotal * 0.15D;
			this.targetDeltaY = totalPosY / deltaTotal * 0.15D;
			this.targetDeltaZ = totalPosZ / deltaTotal * 0.15D;
		}

		this.isAirBorne = true;
		this.steps = 10 + this.rand.nextInt(5) * 10;
	}
	
	/**
	 * Handles the logic that gets called once the Bullet Entity hits something.
	 */
	protected void bulletHit(RayTraceResult result)
	{
		if(result.getType() == RayTraceResult.Type.ENTITY)
		{
			Entity entity = ((EntityRayTraceResult)result).getEntity();
			boolean flag = entity.attackEntityFrom(DamageSource.causeIndirectDamage(this, this.owner).setProjectile(), 4.0F);
			if(flag)
			{
				this.applyEnchantments(this.owner, entity);
				if(entity instanceof LivingEntity)
				{
					((LivingEntity)entity).addPotionEffect(new EffectInstance(Effects.POISON, 200, 1));
				}
			}
		}
		else
		{
			((ServerWorld)this.world).spawnParticle(ParticleTypes.EXPLOSION, this.getPosX(), this.getPosY(), this.getPosZ(), 2, 0.2D, 0.2D, 0.2D, 0.0D);
			this.playSound(SoundEvents.ENTITY_SHULKER_BULLET_HIT, 1.0F, 1.0F);
		}

		this.remove();
	}
}