package com.direwolf20.buildinggadgets.client.renderer;

import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.tileentities.EffectBlockTileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Matrix4f;

public class EffectBlockTER implements BlockEntityRenderer<EffectBlockTileEntity> {

    public EffectBlockTER(BlockEntityRendererProvider.Context p_173540_) {
    }

    @Override
    public void render(EffectBlockTileEntity tile, float partialTicks, PoseStack stack, MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        BlockData renderData = tile.getRenderedBlock();
        if (renderData == null)
            return;
        VertexConsumer builder;

        MultiBufferSource.BufferSource buffer2 = Minecraft.getInstance().renderBuffers().bufferSource();
        EffectBlock.Mode toolMode = tile.getReplacementMode();
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();

        int teCounter = tile.getTicksExisted();
        int maxLife = tile.getLifespan();
        teCounter = Math.min(teCounter, maxLife);

        float scale = (float) (teCounter) / (float) maxLife;
        if (scale >= 1.0f)
            scale = 0.99f;
        if (toolMode == EffectBlock.Mode.REMOVE || toolMode == EffectBlock.Mode.REPLACE)
            scale = (float) (maxLife - teCounter) / maxLife;

        float trans = (1 - scale) / 2;

        stack.pushPose();
        stack.translate(trans, trans, trans);
        stack.scale(scale, scale, scale);

        BlockState renderBlockState = renderData.getState();

        if (tile.isUsingPaste() && toolMode == EffectBlock.Mode.PLACE)
            renderBlockState = OurBlocks.CONSTRUCTION_DENSE_BLOCK.get().defaultBlockState();

        OurRenderTypes.MultiplyAlphaRenderTypeBuffer mutatedBuffer = new OurRenderTypes.MultiplyAlphaRenderTypeBuffer(Minecraft.getInstance().renderBuffers().bufferSource(), .55f);
        try {
            dispatcher.renderSingleBlock(
                    renderBlockState, stack, mutatedBuffer, 15728640, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.solid()
            );
        } catch (Exception ignored) {} // if it fails to render then we'll get a bug report I'm sure.

        stack.popPose();
        stack.pushPose();

        builder = buffer.getBuffer(OurRenderTypes.MissingBlockOverlay);

        float x = 0,
                y = 0,
                z = 0,
                maxX = 1,
                maxY = 1,
                maxZ = 1,
                red = 0f,
                green = 1f,
                blue = 1f;

        if (toolMode == EffectBlock.Mode.REMOVE || toolMode == EffectBlock.Mode.REPLACE) {
            red = 1f;
            green = 0.25f;
            blue = 0.25f;
        }

        float alpha = (1f - (scale));
        if (alpha < 0.051f)
            alpha = 0.051f;

        if (alpha > 0.33f)
            alpha = 0.33f;

        Matrix4f matrix = stack.last().pose();

        // Down
        if (tile.getLevel().getBlockState(tile.getBlockPos().below()).getBlock() != OurBlocks.EFFECT_BLOCK.get()) {
            builder.vertex(matrix, x, y, z).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, maxX, y, z).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, maxX, y, maxZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, x, y, maxZ).color(red, green, blue, alpha).endVertex();
        }
        // Up
        if (tile.getLevel().getBlockState(tile.getBlockPos().above()).getBlock() != OurBlocks.EFFECT_BLOCK.get()) {
            builder.vertex(matrix, x, maxY, z).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, x, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, maxX, maxY, z).color(red, green, blue, alpha).endVertex();
        }
        // North
        if (tile.getLevel().getBlockState(tile.getBlockPos().north()).getBlock() != OurBlocks.EFFECT_BLOCK.get()) {
            builder.vertex(matrix, x, y, z).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, x, maxY, z).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, maxX, maxY, z).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, maxX, y, z).color(red, green, blue, alpha).endVertex();
        }
        // South
        if (tile.getLevel().getBlockState(tile.getBlockPos().south()).getBlock() != OurBlocks.EFFECT_BLOCK.get()) {
            builder.vertex(matrix, x, y, maxZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, maxX, y, maxZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, x, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        }
        // East
        if (tile.getLevel().getBlockState(tile.getBlockPos().east()).getBlock() != OurBlocks.EFFECT_BLOCK.get()) {
            builder.vertex(matrix, maxX, y, z).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, maxX, maxY, z).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, maxX, y, maxZ).color(red, green, blue, alpha).endVertex();
        }
        // West
        if (tile.getLevel().getBlockState(tile.getBlockPos().west()).getBlock() != OurBlocks.EFFECT_BLOCK.get()) {
            builder.vertex(matrix, x, y, z).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, x, y, maxZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, x, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.vertex(matrix, x, maxY, z).color(red, green, blue, alpha).endVertex();
        }
        stack.popPose();
        buffer2.endBatch(); // @mcp: draw (yarn) = finish (mcp)
    }
}
