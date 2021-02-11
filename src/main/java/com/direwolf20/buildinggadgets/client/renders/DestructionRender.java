package com.direwolf20.buildinggadgets.client.renders;

import com.direwolf20.buildinggadgets.client.renderer.OurRenderTypes;
import com.direwolf20.buildinggadgets.common.old_items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.old_items.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;

public class DestructionRender extends BaseRenderer {

    @Override
    public void render(RenderWorldLastEvent evt, PlayerEntity player, ItemStack heldItem) {
        if (!GadgetDestruction.getOverlay(heldItem))
            return;

        BlockRayTraceResult lookingAt = VectorHelper.getLookingAt(player, heldItem);
        World world = player.world;
        BlockPos anchor = ((AbstractGadget) heldItem.getItem()).getAnchor(heldItem);

        if (world.getBlockState(VectorHelper.getLookingAt(player, heldItem).getPos()) == AIR && anchor == null)
            return;

        BlockPos startBlock = (anchor == null) ? lookingAt.getPos() : anchor;
        Direction facing = (GadgetDestruction.getAnchorSide(heldItem) == null) ? lookingAt.getFace() : GadgetDestruction.getAnchorSide(heldItem);
        if (world.getBlockState(startBlock) == OurBlocks.EFFECT_BLOCK.get().getDefaultState())
            return;

        Vector3d playerPos = getMc().gameRenderer.getActiveRenderInfo().getProjectedView();

        MatrixStack stack = evt.getMatrixStack();
        stack.push();
        stack.translate(-playerPos.getX(), -playerPos.getY(), -playerPos.getZ());

        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        IVertexBuilder builder = buffer.getBuffer(OurRenderTypes.MissingBlockOverlay);

        GadgetDestruction.getArea(world, startBlock, facing, player, heldItem)
                .forEach(pos -> renderMissingBlock(stack.getLast().getMatrix(), builder, pos));

        stack.pop();
        RenderSystem.disableDepthTest();
        buffer.finish(); // @mcp: draw = finish
    }
}
