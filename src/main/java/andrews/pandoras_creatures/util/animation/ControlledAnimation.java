package andrews.pandoras_creatures.util.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;

/**
 * Copied Library Functions and Classes from Endergetic
 * see {@link <a href="https://www.curseforge.com/minecraft/mc-mods/endergetic"> Mod Page</a>}.
 * @author SmellyModder(Luke Tonon)
 */
public class ControlledAnimation
{
	private int tick, prevTick;
	public int tickDuration;
	private boolean shouldDecrement;
	public boolean isPaused;
	
	public ControlledAnimation(int tickDuration, int startingValue)
	{
		this.tick = this.prevTick = startingValue;
		this.tickDuration = tickDuration;
	}
	
	public void update()
	{
		this.prevTick = this.tick;
	}
	
	public void tick()
	{
		if(this.isPaused) return;
		
		if(this.shouldDecrement)
		{
			if(this.tick > 0)
			{
				this.tick--;
			}
		}
		else
		{
			if(this.tick < this.tickDuration)
			{
				this.tick++;
			}
		}
	}
	
	public void setDecrementing(boolean shouldDecrement)
	{
		this.shouldDecrement = shouldDecrement;
	}
	
	public boolean isDescrementing()
	{
		return this.shouldDecrement;
	}
	
	public int getTick()
	{
		return this.tick;
	}
	
	public void setValue(int amount)
	{
		this.tick = this.prevTick = amount;
	}
	
	public void addValue(int amount)
	{
		this.tick += amount;
	}
	
	public void setTimerPaused(boolean paused)
	{
		this.isPaused = paused;
	}
	
	public void resetTimer()
	{
		this.tick = this.prevTick = 0;
		this.setTimerPaused(true);
	}
	
	public float getAnimationProgress()
	{
		return MathHelper.lerp(getPartialTicks(), this.prevTick, this.tick) / this.tickDuration;
	}
	
	public CompoundNBT write(CompoundNBT compound)
	{
		compound.putInt("Tick", this.tick);
		compound.putInt("PrevTick", this.prevTick);
		compound.putBoolean("ShouldDecrement", this.shouldDecrement);
		compound.putBoolean("IsPaused", this.isPaused);
		return compound;
	}
	
	public void read(CompoundNBT nbt)
	{
		this.tick = nbt.getInt("Tick");
		this.prevTick = nbt.getInt("PrevTick");
		this.shouldDecrement = nbt.getBoolean("ShouldDecrement");
		this.isPaused = nbt.getBoolean("IsPaused");	
	}
	
	/**
	 * @return - The partial ticks of the minecraft client
	 */
	private static float getPartialTicks()
	{
		if(Minecraft.getInstance().isGamePaused())
		{
			return Minecraft.getInstance().renderPartialTicksPaused;
		}
		else
		{
			return Minecraft.getInstance().getRenderPartialTicks();
		}
	}
}