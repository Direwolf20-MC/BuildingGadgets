package com.direwolf20.buildinggadgets.common.entities;

import com.direwolf20.buildinggadgets.common.blocks.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import javax.annotation.Nullable;

public class ConstructionBlockEntityRender extends Render<ConstructionBlockEntity> {

    public ConstructionBlockEntityRender(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 0F;
    }

    @Override
    public void doRender(ConstructionBlockEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
        Minecraft mc = Minecraft.getInstance();
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        int teCounter = entity.getTicksExisted();
        int maxLife = entity.maxLife;
        if (teCounter > maxLife) {
            teCounter = maxLife;
        }
        float scale = (float) (maxLife - teCounter) / maxLife;
        if (entity.getMakingPaste()) {
            scale = (float) teCounter / maxLife;
        }
        GlStateManager.translated(x, y, z);
        GlStateManager.translatef(-0.0005f, -0.0005f, -0.0005f);
        GlStateManager.scalef(1.001f, 1.001f, 1.001f);//Slightly Larger block to avoid z-fighting.
        GlStateManager.rotatef(-90.0F, 0.0F, 1.0F, 0.0F);

        GL14.glBlendColor(1F, 1F, 1F, scale); //Set the alpha of the blocks we are rendering
        IBlockState renderBlockState = ModBlocks.constructionBlock.getDefaultState();
        if (renderBlockState == null) {
            renderBlockState = Blocks.COBBLESTONE.getDefaultState();
        }

        blockrendererdispatcher.renderBlockBrightness(renderBlockState, 1.0f);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(ConstructionBlockEntity entity) {
        return null;
    }


    public static class Factory implements IRenderFactory<ConstructionBlockEntity> {

        @Override
        public Render<? super ConstructionBlockEntity> createRenderFor(RenderManager manager) {
            return new ConstructionBlockEntityRender(manager);
        }

    }
}
