package com.direwolf20.buildinggadgets.common.entities;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.registry.objects.BGBlocks;
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

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

public class BlockBuildEntityRender extends Render<BlockBuildEntity> {

    public BlockBuildEntityRender(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 0F;
    }

    @Override
    public void doRender(BlockBuildEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
        Minecraft mc = Minecraft.getInstance();
        GlStateManager.pushMatrix();

        BlockBuildEntity.Mode toolMode = entity.getToolMode();
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        int teCounter = entity.getTicksExisted();
        int maxLife = entity.getMaxLife();
        teCounter = teCounter > maxLife ? maxLife : teCounter;
        float scale = (float) (teCounter) / (float) maxLife;
        if (scale >= 1.0f)
            scale = 0.99f;
        if (toolMode == BlockBuildEntity.Mode.REMOVE || toolMode == BlockBuildEntity.Mode.REPLACE)
            scale = (float) (maxLife - teCounter) / maxLife;
        float trans = (1 - scale) / 2;
        GlStateManager.translated(x, y, z);
        GlStateManager.translatef(trans, trans, trans);
        GlStateManager.rotatef(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.scalef(scale, scale, scale);


        //IBlockState renderBlockState = blocks.COBBLESTONE.getDefaultState();

        IBlockState renderBlockState = entity.getSetBlock();
        if (entity.isUsingPaste() && toolMode == BlockBuildEntity.Mode.PLACE)
            renderBlockState = BGBlocks.constructionBlock.getDefaultState();
        if (renderBlockState == null) {
            renderBlockState = Blocks.COBBLESTONE.getDefaultState();
        }
        try {
            blockrendererdispatcher.renderBlockBrightness(renderBlockState, 1.0f);
        } catch (Throwable t) {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            try {
                // If the buffer is already not drawing then it'll throw
                // and IllegalStateException... Very rare
                bufferBuilder.finishDrawing();
            } catch (IllegalStateException ex) {
                BuildingGadgets.LOG.error(ex);
            }
        }
        GlStateManager.popMatrix();


        GlStateManager.pushMatrix();
        GlStateManager.pushLightingAttrib();

        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        Tessellator t = Tessellator.getInstance();
        BufferBuilder bufferBuilder = t.getBuffer();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        double maxX = x + 1;
        double maxY = y + 1;
        double maxZ = z + 1;
        float red = 0f;
        float green = 1f;
        float blue = 1f;
        if (toolMode == BlockBuildEntity.Mode.REMOVE || toolMode == BlockBuildEntity.Mode.REPLACE) {
            red = 1f;
            green = 0.25f;
            blue = 0.25f;
        }
        float alpha = (1f - (scale));
        if (alpha < 0.051f) {
            alpha = 0.051f;
        }
        if (alpha > 0.33f) {
            alpha = 0.33f;
        }
        //down
        bufferBuilder.pos(x, y, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, y, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, y, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, y, maxZ).color(red, green, blue, alpha).endVertex();

        //up
        bufferBuilder.pos(x, maxY, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, maxY, z).color(red, green, blue, alpha).endVertex();

        //north
        bufferBuilder.pos(x, y, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, maxY, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, maxY, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, y, z).color(red, green, blue, alpha).endVertex();

        //south
        bufferBuilder.pos(x, y, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, y, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, maxY, maxZ).color(red, green, blue, alpha).endVertex();

        //east
        bufferBuilder.pos(maxX, y, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, maxY, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, y, maxZ).color(red, green, blue, alpha).endVertex();

        //west
        bufferBuilder.pos(x, y, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, y, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, maxY, z).color(red, green, blue, alpha).endVertex();
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

}
