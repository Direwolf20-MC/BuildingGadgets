package com.direwolf20.buildinggadgets.common.items.gadgets.renderers;

import com.direwolf20.buildinggadgets.common.items.gadgets.AbstractGadget;
import com.direwolf20.buildinggadgets.common.registry.objects.BGBlocks;
import com.direwolf20.buildinggadgets.common.util.CapabilityUtil;
import com.direwolf20.buildinggadgets.common.util.exceptions.CapabilityNotPresentException;
import com.direwolf20.buildinggadgets.common.util.helpers.InventoryHelper;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.tools.modes.ExchangingMode;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.direwolf20.buildinggadgets.common.util.GadgetUtils.getAnchor;
import static com.direwolf20.buildinggadgets.common.util.GadgetUtils.getToolBlock;

public class ExchangerRender extends BaseRenderer {

    @Override
    public void render(RenderWorldLastEvent evt, PlayerEntity player, ItemStack heldItem) {
        super.render(evt, player, heldItem);

        BlockRayTraceResult lookingAt = VectorHelper.getLookingAt(player, heldItem);
        Vec3d playerPos = BaseRenderer.getPlayerPos();

        BlockState state = AIR;
        List<BlockPos> coordinates = getAnchor(heldItem);
        //if (lookingAt == null) {
        //    coordinates.size();
        //}
        if ((lookingAt == null || (player.world.getBlockState(VectorHelper.getLookingAt(player, heldItem).getPos()) == AIR)) && coordinates.size() == 0)
            return;
        World world = player.world;
        BlockState startBlock = AIR;
        startBlock = world.getBlockState(new BlockPos(lookingAt.getPos()));
        if (startBlock != BGBlocks.effectBlock.getDefaultState()) {
            BlockState renderBlockState = getToolBlock(heldItem).getState();

            getMc().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            if (renderBlockState == AIR) {//Don't render anything if there is no block selected (Air)
                return;
            }
            if (coordinates.size() == 0) { //Build a list of coordinates based on the tool mode and range
                coordinates = ExchangingMode
                        .collectPlacementPos(world, player, lookingAt.getPos(), lookingAt.getFace(), heldItem, lookingAt.getPos());
            }

            //Figure out how many of the block we're rendering we have in the inventory of the player.
            //ItemStack itemStack = renderBlockState.getBlock().getPickBlock(renderBlockState, null, world, new BlockPos(0, 0, 0), player);
            //ItemStack itemStack = InventoryHelper.getSilkTouchDrop(renderBlockState);
            ItemStack itemStack;
            //TODO handle LootTables
            if (true/*renderBlockState.getBlock().canSilkHarvest(renderBlockState, world, new BlockPos(0, 0, 0), player)*/) {
                itemStack = InventoryHelper.getSilkTouchDrop(renderBlockState);
            } else {
                itemStack = renderBlockState.getBlock().getPickBlock(renderBlockState, null, world, new BlockPos(0, 0, 0), player);
            }
            if (itemStack.getItem().equals(Items.AIR)) {
                itemStack = renderBlockState.getBlock().getPickBlock(renderBlockState, null, world, new BlockPos(0, 0, 0), player);
            }
            long hasBlocks = InventoryHelper.countItem(itemStack, player, getCacheInventory());
            if (! renderBlockState.hasTileEntity()) {
                hasBlocks = hasBlocks + InventoryHelper.countPaste(player);
            }
            int hasEnergy = 0;

            LazyOptional<IEnergyStorage> energy = CapabilityUtil.EnergyUtil.getCap(heldItem);
            if (energy.isPresent()) {
                hasEnergy = energy.orElseThrow(CapabilityNotPresentException::new).getEnergyStored();
            } else {
                hasEnergy = heldItem.getMaxDamage() - heldItem.getDamage();
            }
            if (player.isCreative() || (energy.isPresent() && ! heldItem.isDamageable())) {
                hasEnergy = 1000000;
            }
            //Prepare the block rendering
            BlockRenderLayer origLayer = MinecraftForgeClient.getRenderLayer();

            //Prepare the fake world -- using a fake world lets us render things properly, like fences connecting.
            Set<BlockPos> coords = new HashSet<BlockPos>(coordinates);
            getBuilderWorld().setWorldAndState(player.world, renderBlockState, coords);

            //Save the current position that is being rendered (I think)
            GlStateManager.pushMatrix();
            //Enable Blending (So we can have transparent effect)
            GlStateManager.enableBlend();
            //This blend function allows you to use a constant alpha, which is defined later
            GlStateManager.blendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);

            //List<BlockPos> sortedCoordinates = ExchangingMode.sortByDistance(coordinates, player); //Sort the coords by distance to player.

            for (BlockPos coordinate : coordinates) {
                GlStateManager.pushMatrix();//Push matrix again just because
                GlStateManager.translated(-playerPos.getX(), -playerPos.getY(), - playerPos.getZ());
                GlStateManager.translatef(coordinate.getX(), coordinate.getY(), coordinate.getZ());//Now move the render position to the coordinates we want to render at
                GlStateManager.rotatef(- 90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
                GlStateManager.translatef(- 0.005f, - 0.005f, 0.005f);
                GlStateManager.scalef(1.01f, 1.01f, 1.01f);//Slightly Larger block to avoid z-fighting.
                GL14.glBlendColor(1F, 1F, 1F, 0.55f); //Set the alpha of the blocks we are rendering
                if (getBuilderWorld().getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) { //Get the block state in the fake world
                    try {
                        state = renderBlockState;
                    } catch (Exception var8) {
                        var8.printStackTrace();
                    }
                }
                //state = state.getBlock().getExtendedState(state, fakeWorld, coordinate); //Get the extended block state in the fake world (Disabled to fix chisel, not sure why.)
                if (renderBlockState.getRenderType() != BlockRenderType.INVISIBLE) {
                    try {
                        getMc().getBlockRendererDispatcher().renderBlockBrightness(state, 1f);//Render the defined block
                    } catch (Throwable t) {
                        Tessellator tessellator = Tessellator.getInstance();
                        BufferBuilder bufferBuilder = tessellator.getBuffer();
                        bufferBuilder.finishDrawing();

                    }
                    GlStateManager.rotatef(- 90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
                }
                GL14.glBlendColor(1F, 1F, 1F, 0.1f); //Set the alpha of the blocks we are rendering
                //GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
                getMc().getBlockRendererDispatcher().renderBlockBrightness(Blocks.WHITE_STAINED_GLASS.getDefaultState(), 1f);//Render the defined block - White glass to show non-full block renders (Example: Torch)
                //Move the render position back to where it was
                GlStateManager.popMatrix();
            }

            for (BlockPos coordinate : coordinates) { //Now run through the UNSORTED list of coords, to show which blocks won't place if you don't have enough of them.
                GlStateManager.pushMatrix();//Push matrix again just because
                GlStateManager.translated(-playerPos.getX(), -playerPos.getY(), - playerPos.getZ());
                GlStateManager.translatef(coordinate.getX(), coordinate.getY(), coordinate.getZ());//Now move the render position to the coordinates we want to render at
                GlStateManager.rotatef(- 90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
                GlStateManager.translatef(- 0.01f, - 0.01f, 0.01f);
                GlStateManager.scalef(1.02f, 1.02f, 1.02f);//Slightly Larger block to avoid z-fighting.
                GL14.glBlendColor(1F, 1F, 1F, 0.55f); //Set the alpha of the blocks we are rendering
                hasBlocks--;
                if (energy.isPresent()) {
                    hasEnergy -= (((AbstractGadget) heldItem.getItem())).getEnergyCost(heldItem);
                }
                if (hasBlocks < 0 || hasEnergy < 0) {
                    getMc().getBlockRendererDispatcher().renderBlockBrightness(Blocks.RED_STAINED_GLASS.getDefaultState(), 1f);
                }
                //Move the render position back to where it was
                GlStateManager.popMatrix();
            }

            if (state.hasTileEntity()) {
                TileEntity te = getTileEntityWorld().getTE(state, world);
                TileEntityRenderer<TileEntity> teRender = getTileEntityWorld().getTER(state, world);
                if (teRender != null && ! getInvalidTileEntities().contains(te)) {
                    for (BlockPos coordinate : coordinates) {
                        te.setPos(coordinate);
                        GlStateManager.pushMatrix();
                        GlStateManager.color4f(1F, 1F, 1F, 1F);
                        GlStateManager.translated(-playerPos.getX(), -playerPos.getY(), - playerPos.getZ());
                        GlStateManager.translatef(coordinate.getX(), coordinate.getY(), coordinate.getZ());
                        GlStateManager.scalef(1.0f, 1.0f, 1.0f); //Block scale 1 = full sized block
                        GlStateManager.enableBlend(); //We have to do this in the loop because the TE Render removes blend when its done
                        GlStateManager.blendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);
                        try {
                            TileEntityRendererDispatcher.instance.render(te, 0, 0, 0, evt.getPartialTicks(), - 1, true);
                        } catch (Exception e) {
                            System.out.println("TER Exception with block type: " + state);
                            getInvalidTileEntities().add(te);
                            GlStateManager.disableFog();
                            GlStateManager.popMatrix();
                            break;
                        }
                        GlStateManager.disableFog();
                        GlStateManager.popMatrix();
                    }
                }
            }
            //Set blending back to the default mode
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            ForgeHooksClient.setRenderLayer(origLayer);
            //Disable blend
            GlStateManager.disableBlend();
            //Pop from the original push in this method
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean isLinkable() {
        return true;
    }
}
