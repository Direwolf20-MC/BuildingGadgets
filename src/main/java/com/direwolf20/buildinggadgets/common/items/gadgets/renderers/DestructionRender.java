package com.direwolf20.buildinggadgets.common.items.gadgets.renderers;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.registry.OurBlocks;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.tools.UniqueItemStack;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.lwjgl.opengl.GL11;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class DestructionRender extends BaseRenderer {
    private static Cache<Triple<UniqueItemStack, BlockPos, Integer>, Integer> cacheDestructionOverlay = CacheBuilder.newBuilder().maximumSize(1).
            expireAfterWrite(1, TimeUnit.SECONDS).removalListener(removal -> GLAllocation.deleteDisplayLists((int) removal.getValue())).build();

    @Override
    public void render(RenderWorldLastEvent evt, PlayerEntity player, ItemStack heldItem) {
        if (!GadgetDestruction.getOverlay(heldItem))
            return;

        BlockRayTraceResult lookingAt = VectorHelper.getLookingAt(player, heldItem);
        World world = player.world;
        if ((lookingAt == null || (world.getBlockState(VectorHelper.getLookingAt(player, heldItem).getPos()) == AIR)) && GadgetDestruction.getAnchor(heldItem) == null)
            return;

        BlockPos startBlock = (GadgetDestruction.getAnchor(heldItem) == null) ? lookingAt.getPos() : GadgetDestruction.getAnchor(heldItem);
        Direction facing = (GadgetDestruction.getAnchorSide(heldItem) == null) ? lookingAt.getFace() : GadgetDestruction.getAnchorSide(heldItem);

        if (world.getBlockState(startBlock) == OurBlocks.effectBlock.getDefaultState())
            return;

        Vec3d playerPos = getPlayerPos();

        GlStateManager.pushMatrix();
        GlStateManager.translated(-playerPos.getX(), -playerPos.getY(), -playerPos.getZ());

        try {
            GlStateManager.callList(cacheDestructionOverlay.get(new ImmutableTriple<>(new UniqueItemStack(heldItem), startBlock, facing.ordinal()), () -> {
                int displayList = GLAllocation.generateDisplayLists(1);
                GlStateManager.newList(displayList, GL11.GL_COMPILE);
                this.renderOverlay(player, world, startBlock, facing, heldItem);
                GlStateManager.endList();
                return displayList;
            }));
        } catch (ExecutionException e) {
            BuildingGadgets.LOG.error("Error encountered while rendering destruction gadget overlay", e);
        }

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private void renderOverlay(PlayerEntity player, World world, BlockPos startBlock, Direction facing, ItemStack heldItem) {
        //Save the current position that is being rendered (I think)
        GlStateManager.pushMatrix();
        //Enable Blending (So we can have transparent effect)
        GlStateManager.enableBlend();
        //This blend function allows you to use a constant alpha, which is defined later
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        Tessellator t = Tessellator.getInstance();
        BufferBuilder bufferBuilder = t.getBuffer();

        for (BlockPos coordinate : GadgetDestruction.getClearingPositionsForRendering(world, startBlock, facing, player, heldItem)) {
            boolean invisible = true;
            BlockState state = world.getBlockState(coordinate);
            for (Direction side : Direction.values()) {
                if (!state.isSideInvisible(state, side)) {
                    invisible = false;
                    break;
                }
            }
            if (invisible) continue;

            GlStateManager.pushMatrix();//Push matrix again just because
            GlStateManager.translatef(coordinate.getX(), coordinate.getY(), coordinate.getZ());//Now move the render position to the coordinates we want to render at
            GlStateManager.rotatef(-90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
            GlStateManager.translatef(-0.005f, -0.005f, 0.005f);
            GlStateManager.scalef(1.01f, 1.01f, 1.01f);//Slightly Larger block to avoid z-fighting.

            GlStateManager.disableLighting();
            GlStateManager.disableTexture();
            //renderBoxSolid(t, bufferBuilder, a.getX(), a.getY(), a.getZ()+1, b.getX()+1, b.getY()+1, b.getZ()+1, 1, 1, 1);
            renderBoxSolid(t, bufferBuilder, 0, 0, -1, 1, 1, 0, 1, 0, 0, 0.5f);
            GlStateManager.enableTexture();
            GlStateManager.enableLighting();
            //Move the render position back to where it was
            GlStateManager.popMatrix();
        }
        //Set blending back to the default mode
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        ForgeHooksClient.setRenderLayer(MinecraftForgeClient.getRenderLayer());
        //Disable blend
        GlStateManager.disableBlend();
        //Pop from the original push in this method
        GlStateManager.popMatrix();
    }

    private static void renderBoxSolid(Tessellator tessellator, BufferBuilder bufferBuilder, double startX, double startY, double startZ, double endX, double endY, double endZ, float red, float green, float blue, float alpha) {
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        //down
        bufferBuilder.pos(startX, startY, startZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, startY, startZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, startY, endZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(startX, startY, endZ).color(red, green, blue, alpha).endVertex();

        //up
        bufferBuilder.pos(startX, endY, startZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(startX, endY, endZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, endY, endZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, endY, startZ).color(red, green, blue, alpha).endVertex();

        //east
        bufferBuilder.pos(startX, startY, startZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(startX, endY, startZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, endY, startZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, startY, startZ).color(red, green, blue, alpha).endVertex();

        //west
        bufferBuilder.pos(startX, startY, endZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, startY, endZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, endY, endZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(startX, endY, endZ).color(red, green, blue, alpha).endVertex();

        //south
        bufferBuilder.pos(endX, startY, startZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, endY, startZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, endY, endZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, startY, endZ).color(red, green, blue, alpha).endVertex();

        //north
        bufferBuilder.pos(startX, startY, startZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(startX, startY, endZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(startX, endY, endZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(startX, endY, startZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
    }
}
