package com.direwolf20.buildinggadgets.client.renderer;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.EffectBlock;
import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.client.renders.BaseRenderer;
import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import com.direwolf20.buildinggadgets.common.tileentities.EffectBlockTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.Random;

import static com.direwolf20.buildinggadgets.client.renderer.MyRenderMethods.renderModelBrightnessColorQuads;

public class EffectBlockTER extends TileEntityRenderer<EffectBlockTileEntity> {

    public EffectBlockTER(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(EffectBlockTileEntity tile, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffer, int combinedLightIn, int combinedOverlayIn) {
        BlockData renderData = tile.getRenderedBlock();
        if (renderData == null)
            return;
        IVertexBuilder builder;

        IRenderTypeBuffer.Impl buffer2 = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        EffectBlock.Mode toolMode = tile.getReplacementMode();
        BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();

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
        stack.translate(trans, trans, trans);
        stack.scale(scale, scale, scale);

        BlockState renderBlockState = renderData.getState();

        BlockColors blockColors = Minecraft.getInstance().getBlockColors();
        int color = blockColors.getColor(renderBlockState, tile.getWorld(), tile.getPos(), 0);
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;

        if (tile.isUsingPaste() && toolMode == EffectBlock.Mode.PLACE)
            renderBlockState = OurBlocks.CONSTRUCTION_DENSE_BLOCK.get().getDefaultState();

        OurRenderTypes.MultiplyAlphaRenderTypeBuffer mutatedBuffer = new OurRenderTypes.MultiplyAlphaRenderTypeBuffer(Minecraft.getInstance().getRenderTypeBuffers().getBufferSource(), .55f);
        try {
            dispatcher.renderBlock(
                    renderBlockState, stack, mutatedBuffer, 15728640, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE
            );
        } catch (Exception ignored) {} // if it fails to render then we'll get a bug report I'm sure.

        stack.pop();
        stack.push();

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
        
        Matrix4f matrix = stack.getLast().getMatrix();

        // Down
        if (tile.getWorld().getBlockState(tile.getPos().down()).getBlock() != OurBlocks.EFFECT_BLOCK.get()) {
            builder.pos(matrix, x, y, z).color(red, green, blue, alpha).endVertex();
            builder.pos(matrix, maxX, y, z).color(red, green, blue, alpha).endVertex();
            builder.pos(matrix, maxX, y, maxZ).color(red, green, blue, alpha).endVertex();
            builder.pos(matrix, x, y, maxZ).color(red, green, blue, alpha).endVertex();
        }
        // Up
        if (tile.getWorld().getBlockState(tile.getPos().up()).getBlock() != OurBlocks.EFFECT_BLOCK.get()) {
            builder.pos(matrix, x, maxY, z).color(red, green, blue, alpha).endVertex();
            builder.pos(matrix, x, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.pos(matrix, maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.pos(matrix, maxX, maxY, z).color(red, green, blue, alpha).endVertex();
        }
        // North
        if (tile.getWorld().getBlockState(tile.getPos().north()).getBlock() != OurBlocks.EFFECT_BLOCK.get()) {
            builder.pos(matrix, x, y, z).color(red, green, blue, alpha).endVertex();
            builder.pos(matrix, x, maxY, z).color(red, green, blue, alpha).endVertex();
            builder.pos(matrix, maxX, maxY, z).color(red, green, blue, alpha).endVertex();
            builder.pos(matrix, maxX, y, z).color(red, green, blue, alpha).endVertex();
        }
        // South
        if (tile.getWorld().getBlockState(tile.getPos().south()).getBlock() != OurBlocks.EFFECT_BLOCK.get()) {
            builder.pos(matrix, x, y, maxZ).color(red, green, blue, alpha).endVertex();
            builder.pos(matrix, maxX, y, maxZ).color(red, green, blue, alpha).endVertex();
            builder.pos(matrix, maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.pos(matrix, x, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        }
        // East
        if (tile.getWorld().getBlockState(tile.getPos().east()).getBlock() != OurBlocks.EFFECT_BLOCK.get()) {
            builder.pos(matrix, maxX, y, z).color(red, green, blue, alpha).endVertex();
            builder.pos(matrix, maxX, maxY, z).color(red, green, blue, alpha).endVertex();
            builder.pos(matrix, maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.pos(matrix, maxX, y, maxZ).color(red, green, blue, alpha).endVertex();
        }
        // West
        if (tile.getWorld().getBlockState(tile.getPos().west()).getBlock() != OurBlocks.EFFECT_BLOCK.get()) {
            builder.pos(matrix, x, y, z).color(red, green, blue, alpha).endVertex();
            builder.pos(matrix, x, y, maxZ).color(red, green, blue, alpha).endVertex();
            builder.pos(matrix, x, maxY, maxZ).color(red, green, blue, alpha).endVertex();
            builder.pos(matrix, x, maxY, z).color(red, green, blue, alpha).endVertex();
        }
        stack.pop();
        buffer2.finish(); // @mcp: draw (yarn) = finish (mcp)
    }
}
