package com.direwolf20.buildinggadgets.client.renders;

import com.direwolf20.buildinggadgets.common.gadgets.BuildingGadget;
import com.direwolf20.buildinggadgets.common.gadgets.ExchangingGadget;
import com.direwolf20.buildinggadgets.common.items.MockBuildingWorld;
import com.direwolf20.buildinggadgets.common.items.ModItems;
import com.direwolf20.buildinggadgets.common.utils.GadgetUtils;
import com.direwolf20.buildinggadgets.common.utils.MagicHelpers;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
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
import java.util.Set;

public class ExchangerRender extends AbstractRender {
    private static final MockBuildingWorld mockBuildingWorld = new MockBuildingWorld();

    @Override
    public void gadgetRender(Tessellator tessellator, BufferBuilder bufferBuilder, RayTraceResult rayTraceResult, ItemStack gadget, List<BlockPos> existingLocations) {
        IBlockState renderBlockState = GadgetUtils.getToolBlock(gadget);

        //Don't render anything if there is no block selected (Air)
        if (renderBlockState == Blocks.AIR.getDefaultState())
            return;

        List<BlockPos> locations = existingLocations.size() != 0
                ? existingLocations
                : ExchangingGadget.getToolMode(gadget).getMode().getCollection(mc.player, mc.player.world, renderBlockState, rayTraceResult.getBlockPos(), rayTraceResult.sideHit, GadgetUtils.getToolRange(gadget), false, BuildingGadget.getFuzzy(gadget));

        //Prepare the fake world -- using a fake world lets us render things properly, like fences connecting.
        Set<BlockPos> coords = new HashSet<>(locations);
        mockBuildingWorld.setWorldAndState(mc.player.world, renderBlockState, coords);

        IBlockState state = emptyBlockState;
        List<BlockPos> sortedCoordinates = MagicHelpers.byDistance(locations, mc.player); //Sort the coords by distance to player.

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);
        for (BlockPos coordinate : sortedCoordinates) {
            GlStateManager.pushMatrix();//Push matrix again just because
            GlStateManager.translate(coordinate.getX(), coordinate.getY(), coordinate.getZ());//Now move the render position to the coordinates we want to render at
            GlStateManager.translate(-0.005f, -0.005f, 0.005f);
            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
            GlStateManager.scale(1.01f, 1.01f, 1.01f);//Slightly Larger block to avoid z-fighting.
            GL14.glBlendColor(1F, 1F, 1F, 0.55f);

            if (mockBuildingWorld.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) { //Get the block state in the fake world
                try {
                    state = renderBlockState.getActualState(mockBuildingWorld, coordinate);
                } catch (Exception ignored) {}
            }

            if (renderBlockState.getRenderType() != EnumBlockRenderType.INVISIBLE) {
                try {
                    rendererDispatcher.renderBlockBrightness(state, 1f);
                } catch (Throwable ignored) {}

                GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
            }

            GL14.glBlendColor(1F, 1F, 1F, 0.1f); //Set the alpha of the blocks we are rendering
            rendererDispatcher.renderBlockBrightness(Blocks.STAINED_GLASS.getDefaultState().withProperty(BlockStainedGlass.COLOR, EnumDyeColor.WHITE), 1f); // Render the defined block - White glass to show non-full block renders (Example: Torch)

            GlStateManager.popMatrix();
        }

        long playerItemCount = getItemCount(getItemWithSilk(renderBlockState));
        int remainingEnergy  = getGadgetEnergy(gadget);

        for (BlockPos coordinate : locations) { //Now run through the UNSORTED list of coords, to show which blocks won't place if you don't have enough of them.
            playerItemCount --;
            remainingEnergy -= gadget.hasCapability(CapabilityEnergy.ENERGY, null)
                    ? ModItems.exchangingGadget.getEnergyCost(gadget)
                    : ModItems.exchangingGadget.getDamageCost(gadget);

            if (playerItemCount < 0 || remainingEnergy < 0)
                renderSingleBlock(tessellator, bufferBuilder, coordinate, 1f, 0, 0, .33f);
        }

        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        ForgeHooksClient.setRenderLayer(MinecraftForgeClient.getRenderLayer());

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

    }
}
