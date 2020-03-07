package com.direwolf20.buildinggadgets.client.renderer;

import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.building.BlockData;
import com.direwolf20.buildinggadgets.common.registry.OurBlocks;
import com.direwolf20.buildinggadgets.common.tiles.EffectBlockTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.Random;

import static com.direwolf20.buildinggadgets.client.renderer.MyRenderMethods.renderModelBrightnessColorQuads;

public class EffectBlockTER<T extends TileEntity> extends TileEntityRenderer<EffectBlockTileEntity> {

    public EffectBlockTER(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

// todo: fix
//    @Override
//    public void render(EffectBlockTileEntity tile, double x, double y, double z, float partialTicks, int destroyStage) {
//        BlockData renderData = tile.getRenderedBlock();
//        if (renderData == null)
//            return;
//        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
//        Minecraft mc = Minecraft.getInstance();
//        GlStateManager.pushMatrix();
//
//        EffectBlock.Mode toolMode = tile.getReplacementMode();
//        mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
//        int teCounter = tile.getTicksExisted();
//        int maxLife = tile.getLifespan();
//        teCounter = Math.min(teCounter, maxLife);
//        float scale = (float) (teCounter) / (float) maxLife;
//        if (scale >= 1.0f)
//            scale = 0.99f;
//        if (toolMode == EffectBlock.Mode.REMOVE || toolMode == EffectBlock.Mode.REPLACE)
//            scale = (float) (maxLife - teCounter) / maxLife;
//        float trans = (1 - scale) / 2;
//
//        GlStateManager.translated(x, y, z);
//        GlStateManager.translatef(trans, trans, trans);
//        GlStateManager.rotatef(-90.0F, 0.0F, 1.0F, 0.0F);
//        GlStateManager.scalef(scale, scale, scale);
//
//        BlockState renderBlockState = renderData.getState();
//        if (tile.isUsingPaste() && toolMode == EffectBlock.Mode.PLACE)
//            renderBlockState = OurBlocks.constructionBlockDense.getDefaultState();
//
//        try {
//            blockrendererdispatcher.renderBlockBrightness(renderBlockState, 1.0f);
//        } catch (Throwable t) {
//            Tessellator tessellator = Tessellator.getInstance();
//            BufferBuilder bufferBuilder = tessellator.getBuffer();
//            try {
//                // If the buffer is already not drawing then it'll throw
//                // and IllegalStateException... Very rare
//                bufferBuilder.finishDrawing();
//            } catch (IllegalStateException ex) {
//                BuildingGadgets.LOG.error(ex);
//            }
//        }
//        GlStateManager.popMatrix();
//
//        GlStateManager.pushMatrix();
//        GlStateManager.pushLightingAttributes();
//
//        GlStateManager.enableBlend();
//        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//        GlStateManager.disableTexture();
//        GlStateManager.depthMask(false);
//        Tessellator t = Tessellator.getInstance();
//        BufferBuilder bufferBuilder = t.getBuffer();
//        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
//
//        double maxX = x + 1;
//        double maxY = y + 1;
//        double maxZ = z + 1;
//        float red = 0f;
//        float green = 1f;
//        float blue = 1f;
//        if (toolMode == EffectBlock.Mode.REMOVE || toolMode == EffectBlock.Mode.REPLACE) {
//            red = 1f;
//            green = 0.25f;
//            blue = 0.25f;
//        }
//        float alpha = (1f - (scale));
//        if (alpha < 0.051f) {
//            alpha = 0.051f;
//        }
//        if (alpha > 0.33f) {
//            alpha = 0.33f;
//        }
//        // Down
//        bufferBuilder.pos(x, y, z).color(red, green, blue, alpha).endVertex();
//        bufferBuilder.pos(maxX, y, z).color(red, green, blue, alpha).endVertex();
//        bufferBuilder.pos(maxX, y, maxZ).color(red, green, blue, alpha).endVertex();
//        bufferBuilder.pos(x, y, maxZ).color(red, green, blue, alpha).endVertex();
//
//        // Up
//        bufferBuilder.pos(x, maxY, z).color(red, green, blue, alpha).endVertex();
//        bufferBuilder.pos(x, maxY, maxZ).color(red, green, blue, alpha).endVertex();
//        bufferBuilder.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
//        bufferBuilder.pos(maxX, maxY, z).color(red, green, blue, alpha).endVertex();
//
//        // North
//        bufferBuilder.pos(x, y, z).color(red, green, blue, alpha).endVertex();
//        bufferBuilder.pos(x, maxY, z).color(red, green, blue, alpha).endVertex();
//        bufferBuilder.pos(maxX, maxY, z).color(red, green, blue, alpha).endVertex();
//        bufferBuilder.pos(maxX, y, z).color(red, green, blue, alpha).endVertex();
//
//        // South
//        bufferBuilder.pos(x, y, maxZ).color(red, green, blue, alpha).endVertex();
//        bufferBuilder.pos(maxX, y, maxZ).color(red, green, blue, alpha).endVertex();
//        bufferBuilder.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
//        bufferBuilder.pos(x, maxY, maxZ).color(red, green, blue, alpha).endVertex();
//
//        // East
//        bufferBuilder.pos(maxX, y, z).color(red, green, blue, alpha).endVertex();
//        bufferBuilder.pos(maxX, maxY, z).color(red, green, blue, alpha).endVertex();
//        bufferBuilder.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
//        bufferBuilder.pos(maxX, y, maxZ).color(red, green, blue, alpha).endVertex();
//
//        // West
//        bufferBuilder.pos(x, y, z).color(red, green, blue, alpha).endVertex();
//        bufferBuilder.pos(x, y, maxZ).color(red, green, blue, alpha).endVertex();
//        bufferBuilder.pos(x, maxY, maxZ).color(red, green, blue, alpha).endVertex();
//        bufferBuilder.pos(x, maxY, z).color(red, green, blue, alpha).endVertex();
//        t.draw();
//
//        GlStateManager.disableBlend();
//        GlStateManager.enableTexture();
//        GlStateManager.depthMask(true);
//
//        GlStateManager.popMatrix();
//        GlStateManager.popAttributes();
//    }

    @Override
    public void render(EffectBlockTileEntity tile, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn) {
        BlockData renderData = tile.getRenderedBlock();
        if (renderData == null)
            return;
        IVertexBuilder builder;
        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
        Minecraft mc = Minecraft.getInstance();
//        GlStateManager.pushMatrix();
        IRenderTypeBuffer.Impl buffer2 = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        EffectBlock.Mode toolMode = tile.getReplacementMode();
        BlockRendererDispatcher dispatcher = mc.getBlockRendererDispatcher();
        int teCounter = tile.getTicksExisted();
        int maxLife = tile.getLifespan();
        teCounter = Math.min(teCounter, maxLife);
        float scale = (float) (teCounter) / (float) maxLife;
        if (scale >= 1.0f)
            scale = 0.99f;
        if (toolMode == EffectBlock.Mode.REMOVE || toolMode == EffectBlock.Mode.REPLACE)
            scale = (float) (maxLife - teCounter) / maxLife;
        float trans = (1 - scale) / 2;


        stack.push();
        //stack.translate(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ());
        stack.translate(trans, trans, trans);
        //stack.rotate(new Quaternion(-90.0F, 0.0F, 1.0F, 0.0F));
        stack.scale(scale, scale, scale);

        BlockState renderBlockState = renderData.getState();

        BlockColors blockColors = Minecraft.getInstance().getBlockColors();
        int color = blockColors.getColor(renderBlockState, tile.getWorld(), tile.getPos(), 0);
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;

        if (tile.isUsingPaste() && toolMode == EffectBlock.Mode.PLACE)
            renderBlockState = OurBlocks.constructionBlockDense.getDefaultState();
        builder = buffer2.getBuffer(MyRenderType.RenderBlock);
        IBakedModel ibakedmodel = dispatcher.getModelForState(renderBlockState);
//        try {
        for (Direction direction : Direction.values()) {
            renderModelBrightnessColorQuads(stack.getLast(), builder, f, f1, f2, 0.7f, ibakedmodel.getQuads(renderBlockState, direction, new Random(MathHelper.getPositionRandom(tile.getPos())), EmptyModelData.INSTANCE), 15728640, 655360);
        }


        stack.pop();
        stack.push();

        builder = buffer.getBuffer(MyRenderType.MissingBlockOverlay);
        float x = 0;
        float y = 0;
        float z = 0;
        float maxX = 1;
        float maxY = 1;
        float maxZ = 1;
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
        alpha = 0.125f;
        Matrix4f matrix = stack.getLast().getMatrix();
//        IVertexBuilder b = buffer.getBuffer(RenderType.getSolid());
        // Down
        builder.pos(matrix, x, y, z).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, maxX, y, z).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, maxX, y, maxZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, x, y, maxZ).color(red, green, blue, alpha).endVertex();

        // Up
        builder.pos(matrix, x, maxY, z).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, x, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, maxX, maxY, z).color(red, green, blue, alpha).endVertex();

        // North
        builder.pos(matrix, x, y, z).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, x, maxY, z).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, maxX, maxY, z).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, maxX, y, z).color(red, green, blue, alpha).endVertex();

        // South
        builder.pos(matrix, x, y, maxZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, maxX, y, maxZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, x, maxY, maxZ).color(red, green, blue, alpha).endVertex();

        // East
        builder.pos(matrix, maxX, y, z).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, maxX, maxY, z).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, maxX, y, maxZ).color(red, green, blue, alpha).endVertex();

        // West
        builder.pos(matrix, x, y, z).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, x, y, maxZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, x, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        builder.pos(matrix, x, maxY, z).color(red, green, blue, alpha).endVertex();


        stack.pop();
        //RenderSystem.disableDepthTest();
        buffer2.finish();
//        GlStateManager.popAttributes();
    }
}
