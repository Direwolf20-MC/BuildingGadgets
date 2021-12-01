package com.direwolf20.buildinggadgets.client.renders;

import com.direwolf20.buildinggadgets.client.renderer.OurRenderTypes;
import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.items.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.RenderWorldLastEvent;

public class DestructionRender extends BaseRenderer {

    @Override
    public void render(RenderWorldLastEvent evt, Player player, ItemStack heldItem) {
        if (!GadgetDestruction.getOverlay(heldItem))
            return;

        BlockHitResult lookingAt = VectorHelper.getLookingAt(player, heldItem);
        Level world = player.level;
        BlockPos anchor = ((AbstractGadget) heldItem.getItem()).getAnchor(heldItem);

        if (world.getBlockState(VectorHelper.getLookingAt(player, heldItem).getBlockPos()) == AIR && anchor == null)
            return;

        BlockPos startBlock = (anchor == null) ? lookingAt.getBlockPos() : anchor;
        Direction facing = (GadgetDestruction.getAnchorSide(heldItem) == null) ? lookingAt.getDirection() : GadgetDestruction.getAnchorSide(heldItem);
        if (world.getBlockState(startBlock) == OurBlocks.EFFECT_BLOCK.get().defaultBlockState())
            return;

        Vec3 playerPos = getMc().gameRenderer.getMainCamera().getPosition();

        PoseStack stack = evt.getMatrixStack();
        stack.pushPose();
        stack.translate(-playerPos.x(), -playerPos.y(), -playerPos.z());

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer builder = buffer.getBuffer(OurRenderTypes.MissingBlockOverlay);

        GadgetDestruction.getArea(world, startBlock, facing, player, heldItem)
                .forEach(pos -> renderMissingBlock(stack.last().pose(), builder, pos));

        stack.popPose();
        RenderSystem.disableDepthTest();
        buffer.endBatch(); // @mcp: draw = finish
    }
}
