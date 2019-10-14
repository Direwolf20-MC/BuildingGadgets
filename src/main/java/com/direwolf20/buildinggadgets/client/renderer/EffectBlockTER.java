package com.direwolf20.buildinggadgets.client.renderer;

import com.direwolf20.buildinggadgets.api.building.BlockData;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.registry.OurBlocks;
import com.direwolf20.buildinggadgets.common.tiles.EffectBlockTileEntity;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

public class EffectBlockTER extends TileEntityRenderer<EffectBlockTileEntity> {

    public EffectBlockTER() {}

    @Override
    public void render(EffectBlockTileEntity tile, double x, double y, double z, float partialTicks, int destroyStage) {
        BlockData renderData = tile.getRenderedBlock();
        if (renderData == null) {
            /*BuildingGadgets.LOG.error("Client did not receive Effect-Block Render Data. " +
                    "This is an error we know about and will try to fix as soon as possible! " +
                    "Aborting render for now to prevent Client-Crash.");*/
            return;
        }
        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
        Minecraft mc = Minecraft.getInstance();
        GlStateManager.pushMatrix();

        EffectBlock.Mode toolMode = tile.getReplacementMode();
        mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        int teCounter = tile.getTicksExisted();
        int maxLife = tile.getLifespan();
        teCounter = teCounter > maxLife ? maxLife : teCounter;
        float scale = (float) (teCounter) / (float) maxLife;
        if (scale >= 1.0f)
            scale = 0.99f;
        if (toolMode == EffectBlock.Mode.REMOVE || toolMode == EffectBlock.Mode.REPLACE)
            scale = (float) (maxLife - teCounter) / maxLife;
        float trans = (1 - scale) / 2;

        GlStateManager.translated(x, y, z);
        GlStateManager.translatef(trans, trans, trans);
        GlStateManager.rotatef(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.scalef(scale, scale, scale);

        BlockState renderBlockState = renderData.getState();
        if (tile.isUsingPaste() && toolMode == EffectBlock.Mode.PLACE)
            renderBlockState = OurBlocks.constructionBlockDense.getDefaultState();

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
        GlStateManager.pushLightingAttributes();

        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableTexture();
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
        if (toolMode == EffectBlock.Mode.REMOVE || toolMode == EffectBlock.Mode.REPLACE) {
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
        // Down
        bufferBuilder.pos(x, y, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, y, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, y, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, y, maxZ).color(red, green, blue, alpha).endVertex();

        // Up
        bufferBuilder.pos(x, maxY, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, maxY, z).color(red, green, blue, alpha).endVertex();

        // North
        bufferBuilder.pos(x, y, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, maxY, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, maxY, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, y, z).color(red, green, blue, alpha).endVertex();

        // South
        bufferBuilder.pos(x, y, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, y, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, maxY, maxZ).color(red, green, blue, alpha).endVertex();

        // East
        bufferBuilder.pos(maxX, y, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, maxY, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, y, maxZ).color(red, green, blue, alpha).endVertex();

        // West
        bufferBuilder.pos(x, y, z).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, y, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(x, maxY, z).color(red, green, blue, alpha).endVertex();
        t.draw();

        GlStateManager.disableBlend();
        GlStateManager.enableTexture();
        GlStateManager.depthMask(true);

        GlStateManager.popMatrix();
        GlStateManager.popAttributes();
    }
}
