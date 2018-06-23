package com.direwolf20.buildinggadgets.Entities;

import com.direwolf20.buildinggadgets.client.RenderHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;


public class BlockBuildEntityRender extends Render<BlockBuildEntity> {

    public BlockBuildEntityRender(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 0F;
    }

    @Override
    public void doRender(BlockBuildEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {

        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.pushMatrix();

        //GlStateManager.scale(1.01F, 1.01F, 1.01F);
        /*float scale2 = 0.025f;
        GlStateManager.translate((1-scale2)/2,(1-scale2)/2,(1-scale2)/2);
        GlStateManager.scale(scale2,scale2,scale2);
        */

        mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        int teCounter = entity.getTicksExisted();
        int maxLife = entity.maxLife;
        if (teCounter > maxLife) {teCounter = maxLife;}
        float scale = (float) teCounter / (float) maxLife;
        float trans = (1-scale)/2;
        GlStateManager.translate(x,y,z);
        GlStateManager.translate(trans,trans,trans);
        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(scale,scale,scale);


        IBlockState renderBlockState = Blocks.COBBLESTONE.getDefaultState();
        blockrendererdispatcher.renderBlockBrightness(renderBlockState, 1.0f);
        GlStateManager.popMatrix();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(BlockBuildEntity entity) {
        return null;
    }


    public static class Factory implements IRenderFactory<BlockBuildEntity> {

        @Override
        public Render<? super BlockBuildEntity> createRenderFor(RenderManager manager) {
            return new BlockBuildEntityRender(manager);
        }

    }

}
