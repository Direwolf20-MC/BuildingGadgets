package com.direwolf20.buildinggadgets.common.items.gadgets.renderers;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.items.gadgets.AbstractGadget;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.registry.OurBlocks;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.tools.UniqueItemStack;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.platform.GlStateManager;
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
        BlockPos anchor = ((AbstractGadget) heldItem.getItem()).getAnchor(heldItem);
        if ((lookingAt == null || (world.getBlockState(VectorHelper.getLookingAt(player, heldItem).getPos()) == AIR)) && anchor == null)
            return;

        BlockPos startBlock = (anchor == null) ? lookingAt.getPos() : anchor;
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
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0001F);
        GlStateManager.disableTexture();
        GlStateManager.depthMask(false);
        GlStateManager.color4f(1f, 1f, 1f, 0.5f);
        for (BlockPos coordinate : GadgetDestruction.getClearingPositionsForRendering(world, startBlock, facing, player, heldItem)) {
            /*boolean invisible = true;
            BlockState state = world.getBlockState(coordinate);
            for (Direction side : Direction.values()) {
                if (!state.isSideInvisible(state, side)) {
                    invisible = false;
                    break;
                }
            }
            if (invisible) continue;*/
            renderMissingBlock(bufferBuilder, coordinate);

        }
        Tessellator.getInstance().draw();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture();
        GlStateManager.color4f(1, 1, 1, 1);
        //Set blending back to the default mode
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        ForgeHooksClient.setRenderLayer(MinecraftForgeClient.getRenderLayer());
        //Disable blend
        GlStateManager.disableBlend();
        //Pop from the original push in this method
        GlStateManager.popMatrix();
    }

}
