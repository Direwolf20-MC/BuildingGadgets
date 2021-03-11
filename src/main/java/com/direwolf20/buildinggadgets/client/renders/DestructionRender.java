package com.direwolf20.buildinggadgets.client.renders;

import com.direwolf20.buildinggadgets.client.renderer.OurRenderTypes;
import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.items.GadgetDestruction;
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
        World world = player.level;
        BlockPos anchor = ((AbstractGadget) heldItem.getItem()).getAnchor(heldItem);

        if (world.getBlockState(VectorHelper.getLookingAt(player, heldItem).getBlockPos()) == AIR && anchor == null)
            return;

        BlockPos startBlock = (anchor == null) ? lookingAt.getBlockPos() : anchor;
        Direction facing = (GadgetDestruction.getAnchorSide(heldItem) == null) ? lookingAt.getDirection() : GadgetDestruction.getAnchorSide(heldItem);
        if (world.getBlockState(startBlock) == OurBlocks.EFFECT_BLOCK.get().defaultBlockState())
            return;

        Vector3d playerPos = getMc().gameRenderer.getMainCamera().getPosition();

        MatrixStack stack = evt.getMatrixStack();
        stack.pushPose();
        stack.translate(-playerPos.x(), -playerPos.y(), -playerPos.z());

        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        IVertexBuilder builder = buffer.getBuffer(OurRenderTypes.MissingBlockOverlay);

        GadgetDestruction.getArea(world, startBlock, facing, player, heldItem)
                .forEach(pos -> renderMissingBlock(stack.last().pose(), builder, pos));

        stack.popPose();
        RenderSystem.disableDepthTest();
        buffer.endBatch(); // @mcp: draw = finish
    }
}
