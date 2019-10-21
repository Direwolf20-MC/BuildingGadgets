package com.direwolf20.buildinggadgets.common.items.gadgets.renderers;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.building.BlockData;
import com.direwolf20.buildinggadgets.common.building.modes.BuildingMode;
import com.direwolf20.buildinggadgets.common.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.common.building.view.SimpleBuildContext;
import com.direwolf20.buildinggadgets.common.inventory.IItemIndex;
import com.direwolf20.buildinggadgets.common.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.inventory.MatchResult;
import com.direwolf20.buildinggadgets.common.inventory.RecordingItemIndex;
import com.direwolf20.buildinggadgets.common.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.items.gadgets.AbstractGadget;
import com.direwolf20.buildinggadgets.common.registry.OurBlocks;
import com.direwolf20.buildinggadgets.common.util.helpers.SortingHelper;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.tools.CapabilityUtil;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
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

import java.util.List;

import static com.direwolf20.buildinggadgets.common.util.GadgetUtils.getAnchor;
import static com.direwolf20.buildinggadgets.common.util.GadgetUtils.getToolBlock;

public class BuildingRender extends BaseRenderer {

    @Override
    public void render(RenderWorldLastEvent evt, PlayerEntity player, ItemStack heldItem) {
        super.render(evt, player, heldItem);

        BlockRayTraceResult lookingAt = VectorHelper.getLookingAt(player, heldItem);
        Vec3d playerPos = BaseRenderer.getPlayerPos();

        BlockState state = BaseRenderer.AIR;
        List<BlockPos> coordinates = getAnchor(heldItem);
        if ((lookingAt != null && (player.world.getBlockState(VectorHelper.getLookingAt(player, heldItem).getPos()) != BaseRenderer.AIR)) || coordinates.size() > 0) {
            World world = player.world;
            BlockState startBlock = BaseRenderer.AIR;
            if (!(lookingAt == null)) {
                startBlock = world.getBlockState(lookingAt.getPos());
            }
            if (startBlock != OurBlocks.effectBlock.getDefaultState()) {

                //TODO handle TileEntities
                BlockData data = getToolBlock(heldItem);
                BlockState renderBlockState = data.getState();
                if (renderBlockState == BaseRenderer.AIR) {//Don't render anything if there is no block selected (Air)
                    return;
                }
                if (coordinates.size() == 0 && lookingAt != null) { //Build a list of coordinates based on the tool mode and range
                    coordinates = BuildingMode
                            .collectPlacementPos(world, player, lookingAt.getPos(), lookingAt.getFace(), heldItem, lookingAt.getPos());
                }

                IBuildContext buildContext = SimpleBuildContext.builder()
                        .usedStack(heldItem)
                        .buildingPlayer(player)
                        .build(world);
                // Figure out how many of the block we're rendering we have in the inventory of the player.
                IItemIndex index = new RecordingItemIndex(InventoryHelper.index(heldItem, player));
                MaterialList materials = data.getRequiredItems(buildContext, null, null);
                int hasEnergy = getEnergy(player, heldItem);

                LazyOptional<IEnergyStorage> energyCap = CapabilityUtil.EnergyUtil.getCap(heldItem);

                //Prepare the fake world -- using a fake world lets us render things properly, like fences connecting.
                getBuilderWorld().setWorldAndState(player.world, renderBlockState, coordinates);

                //Save the current position that is being rendered (I think)
                GlStateManager.pushMatrix();
                GlStateManager.pushTextureAttributes();
                //Enable Blending (So we can have transparent effect)
                GlStateManager.enableBlend();
                //This blend function allows you to use a constant alpha, which is defined later
                GlStateManager.blendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);

                List<BlockPos> sortedCoordinates = SortingHelper.Blocks.byDistance(coordinates, player); //Sort the coords by distance to player.

                for (BlockPos coordinate : sortedCoordinates) {
                    GlStateManager.pushMatrix();//Push matrix again just because
                    GlStateManager.translated(-playerPos.getX(), -playerPos.getY(), -playerPos.getZ());//The render starts at the player, so we subtract the player coords and move the render to 0,0,0
                    GlStateManager.translatef(coordinate.getX(), coordinate.getY(), coordinate.getZ());//Now move the render position to the coordinates we want to render at
                    GlStateManager.rotatef(-90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
                    GlStateManager.scalef(1.0f, 1.0f, 1.0f); //Block scale 1 = full sized block
                    GL14.glBlendColor(1F, 1F, 1F, 0.55f); //Set the alpha of the blocks we are rendering
                    if (getBuilderWorld().getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) { //Get the block state in the fake world
                        try {
                            state = renderBlockState;
                        } catch (Exception var8) {
                        }
                    }
                    //state = state.getBlock().getExtendedState(state, fakeWorld, coordinate); //Get the extended block state in the fake world (Disabled to fix chisel, not sure why.)
                    try {
                        getMc().getBlockRendererDispatcher().renderBlockBrightness(state, 1f);//Render the defined block
                    } catch (Throwable t) {
                        Tessellator tessellator = Tessellator.getInstance();
                        BufferBuilder bufferBuilder = tessellator.getBuffer();
                        bufferBuilder.finishDrawing();

                    }
                    //Move the render position back to where it was
                    GlStateManager.popMatrix();


                }

                for (BlockPos coordinate : coordinates) { //Now run through the UNSORTED list of coords, to show which blocks won't place if you don't have enough of them.
                    GlStateManager.pushMatrix();//Push matrix again just because
                    GlStateManager.translated(-playerPos.getX(), -playerPos.getY(), -playerPos.getZ());//The render starts at the player, so we subtract the player coords and move the render to 0,0,0
                    GlStateManager.translatef(coordinate.getX(), coordinate.getY(), coordinate.getZ());//Now move the render position to the coordinates we want to render at
                    GlStateManager.rotatef(-90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
                    GlStateManager.translatef(-0.005f, -0.005f, 0.005f);
                    GlStateManager.scalef(1.01f, 1.01f, 1.01f);
                    GL14.glBlendColor(1F, 1F, 1F, 0.35f); //Set the alpha of the blocks we are rendering
                    if (energyCap.isPresent()) {
                        hasEnergy -= ((AbstractGadget) heldItem.getItem()).getEnergyCost(heldItem);
                    }
                    MatchResult match = index.tryMatch(materials);
                    if (! match.isSuccess() || hasEnergy < 0) {
                        getMc().getBlockRendererDispatcher().renderBlockBrightness(Blocks.RED_STAINED_GLASS.getDefaultState(), 1f);
                    } else {
                        index.applyMatch(match); //notify the recording index that this counts
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
                            GlStateManager.translated(-playerPos.getX(), -playerPos.getY(), -playerPos.getZ());//The render starts at the player, so we subtract the player coords and move the render to 0,0,0
                            GlStateManager.translatef(coordinate.getX(), coordinate.getY(), coordinate.getZ());
                            GlStateManager.scalef(1.0f, 1.0f, 1.0f); //Block scale 1 = full sized block
                            GlStateManager.enableBlend(); //We have to do this in the loop because the TE Render removes blend when its done
                            GlStateManager.blendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);
                            try {
                                TileEntityRendererDispatcher.instance.render(te, 0, 0, 0, evt.getPartialTicks(), - 1, true);
                            } catch (Exception e) {
                                BuildingGadgets.LOG.warn("TER Exception with block type: " + state);
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
                ForgeHooksClient.setRenderLayer(MinecraftForgeClient.getRenderLayer());
                //Disable blend
                GlStateManager.disableBlend();
                //Pop from the original push in this method
                GlStateManager.popAttributes();
                GlStateManager.popMatrix();
            }
        }
    }

    @Override
    public boolean isLinkable() {
        return true;
    }
}
