package andrews.pandoras_creatures.util.animation;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.client.renderer.Matrix3f;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.Vector4f;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Copied Library Functions and Classes from Endergetic
 * see {@link <a href="https://www.curseforge.com/minecraft/mc-mods/endergetic"> Mod Page</a>}.
 * @author SmellyModder(Luke Tonon)
 */
@OnlyIn(Dist.CLIENT)
public class PCModelRenderer extends ModelRenderer
{
	public float defaultRotationPointX, defaultRotationPointY, defaultRotationPointZ;
	public float defaultRotateAngleX, defaultRotateAngleY, defaultRotateAngleZ;
    public int textureOffsetX, textureOffsetY;
    public float textureWidth, textureHeight;
    public boolean scaleChildren = true;
    public float[] scales = {1.0F, 1.0F, 1.0F};
    private final ObjectList<ModelBox> cubeList = new ObjectArrayList<>();
	private final ObjectList<PCModelRenderer> childModels = new ObjectArrayList<>();

    /**
	 * @param entityModel - Entity model this ModelRenderer belongs to
	 */
	public PCModelRenderer(PCEntityModel<? extends Entity> model)
	{
		super(model);
		model.addCuboid(this);
		model.accept(this);
		this.setTextureSize(model.textureWidth, model.textureHeight);
	}

	/**
	 * Texture offset constuctor 
	 * @param entityModel - Entity model this ModelRenderer belongs to
	 * @param textureOffsetX - X offset on the texture
	 * @param textureOffsetY - Y offset on the texture
	 */
	public PCModelRenderer(PCEntityModel<? extends Entity> model, int textureOffsetX, int textureOffsetY)
	{
		this(model.textureWidth, model.textureHeight, textureOffsetX, textureOffsetY);
		model.accept(this);
		model.addCuboid(this);
	}

	public PCModelRenderer(int textureWidthIn, int textureHeightIn, int textureOffsetXIn, int textureOffsetYIn)
	{
		super(textureWidthIn, textureHeightIn, textureOffsetXIn, textureOffsetYIn);
	}
	
	public void addChild(PCModelRenderer renderer)
	{
		this.childModels.add(renderer);
	}
	
	/**
	 * Performs the same function as vanilla's setTextureOffset
	 */
	@Override
	public PCModelRenderer setTextureOffset(int x, int y)
	{
		this.textureOffsetX = x;
		this.textureOffsetY = y;
		return this;
	}
	
	@Override
	public PCModelRenderer setTextureSize(int textureWidthIn, int textureHeightIn)
	{
		this.textureWidth = (float)textureWidthIn;
		this.textureHeight = (float)textureHeightIn;
		return this;
	}

	@Override
	public PCModelRenderer addBox(String partName, float x, float y, float z, int width, int height, int depth, float delta, int texX, int texY)
	{
		this.setTextureOffset(texX, texY);
		this.addBox(this.textureOffsetX, this.textureOffsetY, x, y, z, (float)width, (float)height, (float)depth, delta, delta, delta, this.mirror, false);
		return this;
	}

	@Override
	public PCModelRenderer addBox(float x, float y, float z, float width, float height, float depth)
	{
		this.addBox(this.textureOffsetX, this.textureOffsetY, x, y, z, width, height, depth, 0.0F, 0.0F, 0.0F, this.mirror, false);
		return this;
	}
	
	@Override
	public PCModelRenderer addBox(float x, float y, float z, float width, float height, float depth, boolean mirrorIn)
	{
		this.addBox(this.textureOffsetX, this.textureOffsetY, x, y, z, width, height, depth, 0.0F, 0.0F, 0.0F, mirrorIn, false);
		return this;
	}
	
	public void addBox(float x, float y, float z, float width, float height, float depth, float delta)
	{
		this.addBox(this.textureOffsetX, this.textureOffsetY, x, y, z, width, height, depth, delta, delta, delta, this.mirror, false);
	}

	public void addBox(float x, float y, float z, float width, float height, float depth, float deltaX, float deltaY, float deltaZ)
	{
		this.addBox(this.textureOffsetX, this.textureOffsetY, x, y, z, width, height, depth, deltaX, deltaY, deltaZ, this.mirror, false);
	}

	public void addBox(float x, float y, float z, float width, float height, float depth, float delta, boolean mirrorIn)
	{
		this.addBox(this.textureOffsetX, this.textureOffsetY, x, y, z, width, height, depth, delta, delta, delta, mirrorIn, false);
	}

	private void addBox(int texOffX, int texOffY, float x, float y, float z, float width, float height, float depth, float deltaX, float deltaY, float deltaZ, boolean mirorIn, boolean p_228305_13_)
	{
		this.cubeList.add(new ModelBox(texOffX, texOffY, x, y, z, width, height, depth, deltaX, deltaY, deltaZ, mirorIn, this.textureWidth, this.textureHeight));
	}
    
	/**
	 * A method that sets the default box's values
	 */
    public void setDefaultBoxValues()
    {
		this.defaultRotationPointX = this.rotationPointX;
		this.defaultRotationPointY = this.rotationPointY;
		this.defaultRotationPointZ = this.rotationPointZ;
		
		this.defaultRotateAngleX = this.rotateAngleX;
		this.defaultRotateAngleY = this.rotateAngleY;
		this.defaultRotateAngleZ = this.rotateAngleZ;
	}
	
    /**
	 * A method that reverts the current box's values back to the default values
	 */
	public void revertToDefaultBoxValues()
	{
		this.rotationPointX = this.defaultRotationPointX;
		this.rotationPointY = this.defaultRotationPointY;
		this.rotationPointZ = this.defaultRotationPointZ;
		
		this.rotateAngleX = this.defaultRotateAngleX;
		this.rotateAngleY = this.defaultRotateAngleY;
		this.rotateAngleZ = this.defaultRotateAngleZ;
	}
    
  //===============================================================================================================================================
  //Scaling Related Stuff Bellow.
  //===============================================================================================================================================
    
    /**
	 * Sets the scale
	 */
	public void setScale(float x, float y, float z)
	{
		this.scales[0] = x;
		this.scales[1] = y;
		this.scales[2] = z;
	}
	
	/**
	 * Sets the scale of the X axis on this ModelRenderer
	 * @param scaleX - Value of scale
	 */
	public void setScaleX(float scaleX)
	{
		this.scales[0] = scaleX;
	}
	
	/**
	 * Sets the scale of the Y axis on this ModelRenderer
	 * @param scaleY - Value of scale
	 */
	public void setScaleY(float scaleY)
	{
		this.scales[1] = scaleY;
	}
	
	/**
	 * Sets the scale of the Z axis on this ModelRenderer
	 * @param scaleZ - Value of scale
	 */
	public void setScaleZ(float scaleZ)
	{
		this.scales[2] = scaleZ;
	}
	
	public void setShouldScaleChildren(boolean scaleChildren)
	{
		this.scaleChildren = scaleChildren;
	}
	
	//===============================================================================================================================================
	//Looped Animation Methods
	//===============================================================================================================================================

    private float calculateRotation(float speed, float degree, boolean invert, float delay, float weight, float limbSwing, float limbSwingAmount)
    {
        if(invert == true)
        {
        	float rotation = (MathHelper.cos(limbSwing * speed + delay) * degree * limbSwingAmount) - (weight * limbSwingAmount);
        	return -rotation;
        }
        else
        {
        	float rotation = (MathHelper.cos(limbSwing * speed + delay) * degree * limbSwingAmount) + (weight * limbSwingAmount);
        	return rotation;
        }
    }

    /**
     * Rotates this box back and forth (rotateAngleX)
     */
    public void swing(float speed, float degree, boolean invert, float delay, float weight, float limbSwing, float limbSwingAmount)
    {
        this.rotateAngleX += this.calculateRotation(speed, degree, invert, delay, weight, limbSwing, limbSwingAmount);
    }

    /**
     * Rotates this box right and left (rotateAngleZ)
     */
    public void flap(float speed, float degree, boolean invert, float delay, float weight, float limbSwing, float limbSwingAmount)
    {
        this.rotateAngleZ += this.calculateRotation(speed, degree, invert, delay, weight, limbSwing, limbSwingAmount);
    }

    /**
     * Rotates this box side to side (rotateAngleY)
     */
    public void shake(float speed, float degree, boolean invert, float delay, float weight, float limbSwing, float limbSwingAmount)
    {
        this.rotateAngleY += this.calculateRotation(speed, degree, invert, delay, weight, limbSwing, limbSwingAmount);
    }

    /**
     * This makes the given Box move Up and Down on the Y Axis
     */
    public void bounce(float speed, float height, boolean extraBouncy, float limbSwing, float limbSwingAmount)
    {
        float bounce = (float) (Math.sin(limbSwing * speed) * limbSwingAmount * height - limbSwingAmount * height);
        if(extraBouncy == true)
        {
        	bounce = (float) -Math.abs((Math.sin(limbSwing * speed) * limbSwingAmount * height));
        }
        this.rotationPointY += bounce;
    }
    
    //===============================================================================================================================================
  	//Rendering Stuff Bellow
  	//===============================================================================================================================================
    
    @Override
	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    {
		if(this.showModel)
		{
			if(!this.cubeList.isEmpty() || !this.childModels.isEmpty())
			{
				matrixStackIn.push();
				this.translateRotate(matrixStackIn);
				
				if(this.scaleChildren)
				{
					matrixStackIn.scale(this.scales[0], this.scales[1], this.scales[2]);
					this.doRender(matrixStackIn.getLast(), bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
					
					for(PCModelRenderer modelrenderer : this.childModels)
					{
						modelrenderer.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
					}
				}
				else
				{
					matrixStackIn.push();
					matrixStackIn.scale(this.scales[0], this.scales[1], this.scales[2]);
					this.doRender(matrixStackIn.getLast(), bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
					matrixStackIn.pop();
					
					for(PCModelRenderer modelrenderer : this.childModels)
					{
						modelrenderer.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
					}
				}
				matrixStackIn.pop();
			}
		}
	}
	
	private void doRender(MatrixStack.Entry matrixEntryIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
	{
		Matrix4f matrix4f = matrixEntryIn.getMatrix();
		Matrix3f matrix3f = matrixEntryIn.getNormal();

		for(ModelBox modelrenderer$modelbox : this.cubeList)
		{
			for(TexturedQuad modelrenderer$texturedquad : modelrenderer$modelbox.quads)
			{
				Vector3f vector3f = modelrenderer$texturedquad.normal.copy();
				vector3f.transform(matrix3f);
				float f = vector3f.getX();
				float f1 = vector3f.getY();
				float f2 = vector3f.getZ();

				for(int i = 0; i < 4; ++i)
				{
					PositionTextureVertex modelrenderer$positiontexturevertex = modelrenderer$texturedquad.vertexPositions[i];
					float f3 = modelrenderer$positiontexturevertex.position.getX() / 16.0F;
					float f4 = modelrenderer$positiontexturevertex.position.getY() / 16.0F;
					float f5 = modelrenderer$positiontexturevertex.position.getZ() / 16.0F;
					Vector4f vector4f = new Vector4f(f3, f4, f5, 1.0F);
					vector4f.transform(matrix4f);
					bufferIn.addVertex(vector4f.getX(), vector4f.getY(), vector4f.getZ(), red, green, blue, alpha, modelrenderer$positiontexturevertex.textureU, modelrenderer$positiontexturevertex.textureV, packedOverlayIn, packedLightIn, f, f1, f2);
				}
			}
		}
	}
	
	public static class ModelBox
	{
		protected final TexturedQuad[] quads;
		public final float posX1;
		public final float posY1;
		public final float posZ1;
		public final float posX2;
		public final float posY2;
		public final float posZ2;

		public ModelBox(int texOffX, int texOffY, float x, float y, float z, float width, float height, float depth, float deltaX, float deltaY, float deltaZ, boolean mirorIn, float texWidth, float texHeight)
		{
			this.posX1 = x;
			this.posY1 = y;
			this.posZ1 = z;
			this.posX2 = x + width;
			this.posY2 = y + height;
			this.posZ2 = z + depth;
			this.quads = new TexturedQuad[6];
			float f = x + width;
			float f1 = y + height;
			float f2 = z + depth;
			x = x - deltaX;
			y = y - deltaY;
			z = z - deltaZ;
			f = f + deltaX;
			f1 = f1 + deltaY;
			f2 = f2 + deltaZ;
			if(mirorIn)
			{
				float f3 = f;
				f = x;
				x = f3;
			}

			PositionTextureVertex modelrenderer$positiontexturevertex7 = new PositionTextureVertex(x, y, z, 0.0F, 0.0F);
			PositionTextureVertex modelrenderer$positiontexturevertex = new PositionTextureVertex(f, y, z, 0.0F, 8.0F);
			PositionTextureVertex modelrenderer$positiontexturevertex1 = new PositionTextureVertex(f, f1, z, 8.0F, 8.0F);
			PositionTextureVertex modelrenderer$positiontexturevertex2 = new PositionTextureVertex(x, f1, z, 8.0F, 0.0F);
			PositionTextureVertex modelrenderer$positiontexturevertex3 = new PositionTextureVertex(x, y, f2, 0.0F, 0.0F);
			PositionTextureVertex modelrenderer$positiontexturevertex4 = new PositionTextureVertex(f, y, f2, 0.0F, 8.0F);
			PositionTextureVertex modelrenderer$positiontexturevertex5 = new PositionTextureVertex(f, f1, f2, 8.0F, 8.0F);
			PositionTextureVertex modelrenderer$positiontexturevertex6 = new PositionTextureVertex(x, f1, f2, 8.0F, 0.0F);
			float f4 = (float)texOffX;
			float f5 = (float)texOffX + depth;
			float f6 = (float)texOffX + depth + width;
			float f7 = (float)texOffX + depth + width + width;
			float f8 = (float)texOffX + depth + width + depth;
			float f9 = (float)texOffX + depth + width + depth + width;
			float f10 = (float)texOffY;
			float f11 = (float)texOffY + depth;
			float f12 = (float)texOffY + depth + height;
			this.quads[2] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex4, modelrenderer$positiontexturevertex3, modelrenderer$positiontexturevertex7, modelrenderer$positiontexturevertex}, f5, f10, f6, f11, texWidth, texHeight, mirorIn, Direction.DOWN);
			this.quads[3] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex1, modelrenderer$positiontexturevertex2, modelrenderer$positiontexturevertex6, modelrenderer$positiontexturevertex5}, f6, f11, f7, f10, texWidth, texHeight, mirorIn, Direction.UP);
			this.quads[1] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex7, modelrenderer$positiontexturevertex3, modelrenderer$positiontexturevertex6, modelrenderer$positiontexturevertex2}, f4, f11, f5, f12, texWidth, texHeight, mirorIn, Direction.WEST);
			this.quads[4] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex, modelrenderer$positiontexturevertex7, modelrenderer$positiontexturevertex2, modelrenderer$positiontexturevertex1}, f5, f11, f6, f12, texWidth, texHeight, mirorIn, Direction.NORTH);
			this.quads[0] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex4, modelrenderer$positiontexturevertex, modelrenderer$positiontexturevertex1, modelrenderer$positiontexturevertex5}, f6, f11, f8, f12, texWidth, texHeight, mirorIn, Direction.EAST);
			this.quads[5] = new TexturedQuad(new PositionTextureVertex[]{modelrenderer$positiontexturevertex3, modelrenderer$positiontexturevertex4, modelrenderer$positiontexturevertex5, modelrenderer$positiontexturevertex6}, f8, f11, f9, f12, texWidth, texHeight, mirorIn, Direction.SOUTH);
		}
	}
	
	protected static class PositionTextureVertex
	{
		public final Vector3f position;
		public final float textureU;
		public final float textureV;

		public PositionTextureVertex(float x, float y, float z, float texU, float texV)
		{
			this(new Vector3f(x, y, z), texU, texV);
		}

		public PositionTextureVertex setTextureUV(float texU, float texV)
		{
			return new PositionTextureVertex(this.position, texU, texV);
		}

		public PositionTextureVertex(Vector3f posIn, float texU, float texV)
		{
			this.position = posIn;
			this.textureU = texU;
			this.textureV = texV;
		}
	}
	
	protected static class TexturedQuad
	{
		public final PositionTextureVertex[] vertexPositions;
		public final Vector3f normal;

		public TexturedQuad(PositionTextureVertex[] positionsIn, float u1, float v1, float u2, float v2, float texWidth, float texHeight, boolean mirrorIn, Direction directionIn)
		{
			this.vertexPositions = positionsIn;
			float f = 0.0F / texWidth;
			float f1 = 0.0F / texHeight;
			positionsIn[0] = positionsIn[0].setTextureUV(u2 / texWidth - f, v1 / texHeight + f1);
			positionsIn[1] = positionsIn[1].setTextureUV(u1 / texWidth + f, v1 / texHeight + f1);
			positionsIn[2] = positionsIn[2].setTextureUV(u1 / texWidth + f, v2 / texHeight - f1);
			positionsIn[3] = positionsIn[3].setTextureUV(u2 / texWidth - f, v2 / texHeight - f1);
			if(mirrorIn)
			{
				int i = positionsIn.length;

				for(int j = 0; j < i / 2; ++j)
				{
	               PositionTextureVertex modelrenderer$positiontexturevertex = positionsIn[j];
	               positionsIn[j] = positionsIn[i - 1 - j];
	               positionsIn[i - 1 - j] = modelrenderer$positiontexturevertex;
	            }
			}

			this.normal = directionIn.toVector3f();
			if(mirrorIn)
			{
				this.normal.mul(-1.0F, 1.0F, 1.0F);
			}
		}
	}
}