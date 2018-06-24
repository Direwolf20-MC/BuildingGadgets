package com.direwolf20.buildinggadgets.Entities;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
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
import net.minecraftforge.fml.client.registry.IRenderFactory;

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
        boolean entExchangeMode = entity.getExchangeMode();
        mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        int teCounter = entity.getTicksExisted();
        int maxLife = entity.maxLife;
        if (teCounter > maxLife) {teCounter = maxLife;}
        float scale = (float) teCounter / (float) maxLife;
        if (entExchangeMode) {
            scale = (float) (maxLife-teCounter) / maxLife;
        }
        float trans = (1-scale)/2;
        GlStateManager.translate(x,y,z);
        GlStateManager.translate(trans,trans,trans);
        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(scale,scale,scale);


        //IBlockState renderBlockState = Blocks.COBBLESTONE.getDefaultState();
        IBlockState renderBlockState = entity.getSetBlock();
        if (renderBlockState == null) {
            renderBlockState = Blocks.COBBLESTONE.getDefaultState();
        }
        blockrendererdispatcher.renderBlockBrightness(renderBlockState, 1.0f);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        //GlStateManager.glLineWidth(1.0f);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        Tessellator t = Tessellator.getInstance();
        BufferBuilder bufferBuilder = t.getBuffer();
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        double minX = x;
        double minY = y;
        double minZ = z;
        double maxX = x+1;
        double maxY = y+1;
        double maxZ = z+1;
        float red = 0f;
        float green = 1f;
        float blue = 1f;
        if (entExchangeMode) {
            red = 1f;
            green = 0f;
            blue = 0f;
        }
        float alpha = (1f-(scale));
        if (alpha <0.25f) {alpha = 0.25f;}
        //down
        bufferBuilder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();

        //up
        bufferBuilder.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();

        //north
        bufferBuilder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();

        //south
        bufferBuilder.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();

        //east
        bufferBuilder.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();

        //west
        bufferBuilder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
        t.draw();

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);

        GlStateManager.popMatrix();
        GlStateManager.popAttrib();

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
