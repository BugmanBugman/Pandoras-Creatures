package andrews.pandoras_creatures.entities;

import andrews.pandoras_creatures.util.interfaces.IBucketableEntity;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.WaterMobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;

public abstract class BucketableMobEntity extends WaterMobEntity implements IBucketableEntity
{
    private static final DataParameter<Boolean> FROM_BUCKET = EntityDataManager.createKey(BucketableMobEntity.class, DataSerializers.BOOLEAN);

    public BucketableMobEntity(EntityType<? extends BucketableMobEntity> type, World world)
    {
        super(type, world);
    }

    protected void registerData()
    {
        super.registerData();
        this.dataManager.register(FROM_BUCKET, false);
    }

    protected void setBucketData(ItemStack bucket)
    {
        if(this.hasCustomName())
        {
            bucket.setDisplayName(this.getCustomName());
        }
    }

    public void writeAdditional(CompoundNBT compound)
    {
        super.writeAdditional(compound);
        compound.putBoolean("FromBucket", this.isFromBucket());
    }

    public void readAdditional(CompoundNBT compound)
    {
        super.readAdditional(compound);
        this.setFromBucket(compound.getBoolean("FromBucket"));
    }

    public boolean isFromBucket()
    {
        return this.dataManager.get(FROM_BUCKET);
    }

    public void setFromBucket(boolean value)
    {
        this.dataManager.set(FROM_BUCKET, value);
    }

    @Override
    protected boolean processInteract(PlayerEntity player, Hand hand)
    {
        ItemStack itemstack = player.getHeldItem(hand);
        if(itemstack.getItem() == Items.WATER_BUCKET && this.isAlive())
        {
            this.playSound(SoundEvents.ITEM_BUCKET_FILL_FISH, 1.0F, 1.0F);
            itemstack.shrink(1);
            ItemStack itemstack1 = this.getBucket();
            this.setBucketData(itemstack1);
            if(!this.world.isRemote)
            {
                CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayerEntity)player, itemstack1);
            }

            if(itemstack.isEmpty())
            {
                player.setHeldItem(hand, itemstack1);
            }
            else if(!player.inventory.addItemStackToInventory(itemstack1))
            {
                player.dropItem(itemstack1, false);
            }
            this.remove();
            return true;
        }
        else
        {
            return super.processInteract(player, hand);
        }
    }

    @Override
    public boolean canDespawn(double distanceToClosestPlayer)
    {
        return !this.isFromBucket() && !this.hasCustomName();
    }

    @Override
    public boolean preventDespawn()
    {
    	return this.isFromBucket();
    } 
}