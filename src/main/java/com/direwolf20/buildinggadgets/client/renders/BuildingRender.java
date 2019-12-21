package com.direwolf20.buildinggadgets.client.renders;

import com.direwolf20.buildinggadgets.common.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.gadgets.building.BuildingModes;
import com.direwolf20.buildinggadgets.common.items.MockBuildingWorld;
import com.direwolf20.buildinggadgets.common.items.ModItems;
import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import com.direwolf20.buildinggadgets.common.tools.Sorter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.energy.CapabilityEnergy;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.util.HashSet;
import java.util.List;

public class BuildingRender extends AbstractRender {
    private static final MockBuildingWorld mockBuildingWorld = new MockBuildingWorld();
    
    @Override
    public void gadgetRender(Tessellator tessellator, BufferBuilder bufferBuilder, RayTraceResult rayTraceResult, IBlockState traceBlock, ItemStack gadget, List<BlockPos> existingLocations) {
        IBlockState renderBlockState = GadgetUtils.getToolBlock(gadget);

        // Don't render anything if there is no block selected (Air)
        if (renderBlockState == Blocks.AIR.getDefaultState())
            return;

        // todo: replace with actual mode methods
        List<BlockPos> locations = existingLocations.size() != 0
                ? existingLocations
                : BuildingModes.GRID.getMode().getCollection(mc.player, mc.player.world, renderBlockState, rayTraceResult.getBlockPos(), mc.player.getPosition(), rayTraceResult.sideHit, GadgetUtils.getToolRange(gadget), GadgetBuilding.shouldPlaceAtop(gadget), GadgetBuilding.getFuzzy(gadget));

        IBlockState state = emptyBlockState;
        // Prepare the mock world using a mock world lets us render things properly, like fences connecting.
        mockBuildingWorld.setWorldAndState(mc.player.world, renderBlockState, new HashSet<>(locations));

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);

        // Sort the coords by distance to player.
        List<BlockPos> sortedCoordinates = Sorter.Blocks.byDistance(locations, mc.player);
        for (BlockPos coordinate : sortedCoordinates) {
            GlStateManager.pushMatrix();//Push matrix again just because
            GlStateManager.translate(coordinate.getX(), coordinate.getY(), coordinate.getZ());//Now move the render position to the locations we want to render at
            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
            GlStateManager.scale(1.0f, 1.0f, 1.0f); //Block scale 1 = full sized block
            GL14.glBlendColor(1F, 1F, 1F, 0.55f); //Set the alpha of the blocks we are rendering

            if (mockBuildingWorld.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
                try {
                    state = renderBlockState.getActualState(mockBuildingWorld, coordinate);
                } catch (Exception ignored) {}
            }

            try {
                rendererDispatcher.renderBlockBrightness(state, 1f);//Render the defined block
            } catch (Throwable ignored) {}

            //Move the render position back to where it was
            GlStateManager.popMatrix();
        }

        long playerItemCount = getItemCount(getItemWithSilk(renderBlockState));
        int remainingEnergy  = getGadgetEnergy(gadget);

        for (BlockPos coordinate : locations) { //Now run through the UNSORTED list of coords, to show which blocks won't place if you don't have enough of them.
            playerItemCount --;
            remainingEnergy -= gadget.hasCapability(CapabilityEnergy.ENERGY, null)
                    ? ModItems.gadgetBuilding.getEnergyCost(gadget)
                    : ModItems.gadgetBuilding.getDamageCost(gadget);

            if (playerItemCount < 0 || remainingEnergy < 0)
                renderSingleBlock(tessellator, bufferBuilder, coordinate, 1f, 0, 0, .33f);
        }

        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        ForgeHooksClient.setRenderLayer(MinecraftForgeClient.getRenderLayer());

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
