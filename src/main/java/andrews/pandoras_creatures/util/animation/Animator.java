package andrews.pandoras_creatures.util.animation;

import java.util.Map;

import com.google.common.collect.Maps;

import andrews.pandoras_creatures.entities.bases.AnimatedCreatureEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Copied Library Functions and Classes from Endergetic
 * see {@link <a href="https://www.curseforge.com/minecraft/mc-mods/endergetic"> Mod Page</a>}.
 * @author SmellyModder(Luke Tonon)
 */
@OnlyIn(Dist.CLIENT)
public class Animator
{
	private int tempTick;
	private int prevTempTick;
	private boolean correctAnimation;
	public AnimatedCreatureEntity animatedEntity;
	private Map<PCRendererModel, float[]> boxValues;
	private Map<PCRendererModel, float[]> prevBoxValues;
    
	public Animator()
	{
		this.tempTick = 0;
		this.prevTempTick = 0;
		this.correctAnimation = false;
		this.boxValues = Maps.newHashMap();
		this.prevBoxValues = Maps.newHashMap();
	}
    
	/**
	 * Sets the animation for this Animator instance to play
	 * @param animationToPlay - The animation to play
	 * @return - Is this the correct animation to play
	 */
	public boolean setAnimationToPlay(Animation animationToPlay)
	{
	    this.tempTick = this.prevTempTick = 0;
	    this.correctAnimation = this.animatedEntity.getPlayingAnimation() == animationToPlay;
	    this.prevBoxValues.clear();
	    this.prevBoxValues.putAll(this.boxValues);
	    this.boxValues.clear();
	    return this.correctAnimation;
	}
    
	/**
	 * Updates the entity for this Animator instance
	 * @param animatedEntity - The entity to update
	 */
	public void updateAnimations(AnimatedCreatureEntity animatedEntity)
	{
		this.animatedEntity = animatedEntity;
	}
	
	/**
	 * Starts a Keyframe for a set amount of ticks
	 * @param tickDuration - The duration of the keyframe; measured in ticks
	 */
	public void startKeyframe(int tickDuration)
	{
		if(!this.correctAnimation) return;
		this.prevTempTick = this.tempTick;
		this.tempTick += tickDuration;
	}
    
	/**
	 * Ends the current Keyframe
	 */
	public void endKeyframe()
	{
		this.endKeyframe(false);
	}
	
	/**
	 * Starts a Keyframe that holds the most recent box values for a set duration
	 * @param tickDuration - The duration of the Keyframe; measured in ticks
	 */
	public void setStaticKeyframe(int tickDuration)
	{
		this.startKeyframe(tickDuration);
		this.endKeyframe(true);
	}
    
	/**
	 * Resets the current Keyframe to its default values
	 * @param tickDuration - The duration of the Keyframe; measured in ticks
	 */
	public void resetKeyframe(int tickDuration)
	{
		this.startKeyframe(tickDuration);
		this.endKeyframe();
	}
	
	/**
	 * Gets the values of a box stored from a map
	 * @param model - The PCRendererModel to look up in the box values map
	 * @return - The PCRendererModel's float array of box values from the box values map
	 */
	public float[] getBoxValues(PCRendererModel model)
	{
		float[] empty = {0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F};
		return this.boxValues.computeIfAbsent(model, a -> empty);
	}
	
	/**
	 * Moves an PCRendererModel in the current Keyframe
	 * @param model - EndimatorRendererModel to move
	 * @param x - The x point
	 * @param y - The y point
	 * @param z - The z point
	 */
	public void move(PCRendererModel model, float x, float y, float z)
	{
		if(!this.correctAnimation) return;
		this.getBoxValues(model)[0] = x;
		this.getBoxValues(model)[1] = y;
		this.getBoxValues(model)[2] = z;
	}
	
	/**
	 * Rotates an PCRendererModel in the current Keyframe
	 * @param model - EndimatorRendererModel to rotate
	 * @param x - The x rotation
	 * @param y - The y rotation
	 * @param z - The z rotation
	 */
	public void rotate(PCRendererModel model, float x, float y, float z)
	{
		if(!this.correctAnimation) return;
		this.getBoxValues(model)[3] = x;
		this.getBoxValues(model)[4] = y;
		this.getBoxValues(model)[5] = z;
	}
	
	private void endKeyframe(boolean stationary)
	{
		if(!this.correctAnimation) return;
        
		int animationTick = this.animatedEntity.getAnimationTick();
		
		if(animationTick >= this.prevTempTick && animationTick < this.tempTick)
		{
			if(stationary)
			{
				for(PCRendererModel box : this.prevBoxValues.keySet())
				{
					float[] transform = this.prevBoxValues.get(box);
					box.rotationPointX += transform[0];
					box.rotationPointY += transform[1];
					box.rotationPointZ += transform[2];
					box.rotateAngleX += transform[3];
					box.rotateAngleY += transform[4];
					box.rotateAngleZ += transform[5];
				}
			}
			else
			{
				float tick = (animationTick - this.prevTempTick + getPartialTicks()) / (this.tempTick - this.prevTempTick);
				float increment = MathHelper.sin((float) (tick * Math.PI / 2.0F));
				float decrement = 1.0F - increment;	
				for(PCRendererModel box : this.prevBoxValues.keySet())
				{
					float[] transform = this.prevBoxValues.get(box);
					box.rotationPointX += decrement * transform[0];
					box.rotationPointY += decrement * transform[1];
					box.rotationPointZ += decrement * transform[2];
					box.rotateAngleX += decrement * transform[3];
					box.rotateAngleY += decrement * transform[4];
					box.rotateAngleZ += decrement * transform[5];
				}
				for(PCRendererModel box : this.boxValues.keySet())
				{
					float[] transform = this.boxValues.get(box);       
					box.rotationPointX += increment * transform[0];
					box.rotationPointY += increment * transform[1];
					box.rotationPointZ += increment * transform[2];
					box.rotateAngleX += increment * transform[3];
					box.rotateAngleY += increment * transform[4];
					box.rotateAngleZ += increment * transform[5];
				}
			}
		}
		if(!stationary)
		{
			this.prevBoxValues.clear();
			this.prevBoxValues.putAll(this.boxValues);
			this.boxValues.clear();
		}
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