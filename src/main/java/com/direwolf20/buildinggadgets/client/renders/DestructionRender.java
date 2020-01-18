package com.direwolf20.buildinggadgets.client.renders;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.gadgets.DestructionGadget;
import com.direwolf20.buildinggadgets.common.utils.MagicHelpers;
import com.direwolf20.buildinggadgets.common.utils.UniqueItemStack;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class DestructionRender extends AbstractRender {
    private static Cache<Pair<UniqueItemStack, BlockPos>, Integer> renderCache = CacheBuilder.newBuilder().maximumSize(1).
            expireAfterWrite(1, TimeUnit.SECONDS).removalListener(removal -> GLAllocation.deleteDisplayLists((int) removal.getValue())).build();

    @Override
    public void gadgetRender(Tessellator tessellator, BufferBuilder bufferBuilder, RayTraceResult rayTraceResult, ItemStack gadget, List<BlockPos> existingLocations) {
        if (!DestructionGadget.getOverlay(gadget))
            return;

        List<BlockPos> coordinates = DestructionGadget.getArea(mc.player.world, rayTraceResult, mc.player, gadget, existingLocations);

        if( coordinates.size() == 0 )
            return;

        GlStateManager.pushMatrix();
        try {
            GlStateManager.callList(renderCache.get(new ImmutablePair<>(new UniqueItemStack(gadget), existingLocations.size() > 0 ? existingLocations.get(0) : rayTraceResult.getBlockPos()), () -> {
                int displayList = GLAllocation.generateDisplayLists(1);
                GlStateManager.glNewList(displayList, GL11.GL_COMPILE);

                // Render the cache or the actual render.
                renderOverlay(tessellator, bufferBuilder, mc.player, coordinates);

                GlStateManager.glEndList();
                return displayList;
            }));
        } catch (ExecutionException e) {
            BuildingGadgets.logger.error("Error encountered while rendering destruction gadget overlay", e);
        }

        GlStateManager.popMatrix();
    }

    private void renderOverlay(Tessellator tessellator, BufferBuilder bufferBuilder, EntityPlayer player, List<BlockPos> coordinates) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        List<BlockPos> sortedCoordinates = MagicHelpers.byDistance(coordinates, player);
        for (BlockPos coordinate : sortedCoordinates) {
            boolean invisible = true;
            IBlockState state = player.world.getBlockState(coordinate);
            for (EnumFacing side : EnumFacing.values()) {
                if (state.shouldSideBeRendered(player.world, coordinate, side)) {
                    invisible = false;
                    break;
                }
            }

            if (invisible)
                continue;

            renderSingleBlock(tessellator, bufferBuilder, coordinate, 1f, 0, 0, .55f);
        }

        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        ForgeHooksClient.setRenderLayer(MinecraftForgeClient.getRenderLayer());

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
