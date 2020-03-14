package com.direwolf20.buildinggadgets.common.items.gadgets.renderers;

import com.direwolf20.buildinggadgets.client.renderer.MyRenderType;
import com.direwolf20.buildinggadgets.common.items.gadgets.AbstractGadget;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.registry.OurBlocks;
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
import net.minecraft.util.math.Vec3d;
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

        if ((lookingAt == null || (world.getBlockState(VectorHelper.getLookingAt(player, heldItem).getPos()) == AIR)) && anchor == null)
            return;

        BlockPos startBlock = (anchor == null) ? lookingAt.getPos() : anchor;
        Direction facing = (GadgetDestruction.getAnchorSide(heldItem) == null) ? lookingAt.getFace() : GadgetDestruction.getAnchorSide(heldItem);
        if (world.getBlockState(startBlock) == OurBlocks.effectBlock.getDefaultState())
            return;

        Vec3d playerPos = getMc().gameRenderer.getActiveRenderInfo().getProjectedView();

        MatrixStack stack = evt.getMatrixStack();
        stack.push();
        stack.translate(-playerPos.getX(), -playerPos.getY(), -playerPos.getZ());

        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        IVertexBuilder builder = buffer.getBuffer(MyRenderType.MissingBlockOverlay);

        GadgetDestruction.getClearingPositionsForRendering(world, startBlock, facing, player, heldItem).forEach(coord ->
                renderMissingBlock(stack.getLast().getMatrix(), builder, coord)
        );

        stack.pop();
        RenderSystem.disableDepthTest();
        buffer.finish();
    }
}
