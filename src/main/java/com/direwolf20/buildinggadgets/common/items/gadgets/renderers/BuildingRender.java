package com.direwolf20.buildinggadgets.common.items.gadgets.renderers;

import com.direwolf20.buildinggadgets.client.renderer.MyRenderType;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.building.BlockData;
import com.direwolf20.buildinggadgets.common.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.common.building.view.SimpleBuildContext;
import com.direwolf20.buildinggadgets.common.inventory.IItemIndex;
import com.direwolf20.buildinggadgets.common.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.inventory.MatchResult;
import com.direwolf20.buildinggadgets.common.inventory.RecordingItemIndex;
import com.direwolf20.buildinggadgets.common.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.items.gadgets.AbstractGadget;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.gadgets.modes.AbstractMode;
import com.direwolf20.buildinggadgets.common.registry.OurBlocks;
import com.direwolf20.buildinggadgets.common.util.helpers.SortingHelper;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.List;
import java.util.Random;

import static com.direwolf20.buildinggadgets.client.renderer.MyRenderMethods.renderModelBrightnessColorQuads;
import static com.direwolf20.buildinggadgets.common.util.GadgetUtils.getAnchor;
import static com.direwolf20.buildinggadgets.common.util.GadgetUtils.getToolBlock;

public class BuildingRender extends BaseRenderer {

    @Override
    public void render(RenderWorldLastEvent evt, PlayerEntity player, ItemStack heldItem) {
        super.render(evt, player, heldItem);
        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        IVertexBuilder builder;
        BlockRayTraceResult lookingAt = VectorHelper.getLookingAt(player, heldItem);
        Vec3d playerPos = getMc().gameRenderer.getActiveRenderInfo().getProjectedView();

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
                List<BlockPos> renderCoordinates;
                if (coordinates.size() == 0) { //Build a list of coordinates based on the tool mode and range
                    coordinates = GadgetBuilding.getToolMode(heldItem).getMode().getCollection(
                            player,
                            new AbstractMode.UseContext(
                                    world,
                                    renderBlockState,
                                    lookingAt.getPos(),
                                    heldItem,
                                    GadgetBuilding.shouldPlaceAtop(heldItem)
                            ),
                            lookingAt.getFace()
                    );
                    renderCoordinates = coordinates;
                } else { //anchors need to be resorted
                    renderCoordinates = SortingHelper.Blocks.byDistance(coordinates, player);
                }

                IBuildContext buildContext = SimpleBuildContext.builder()
                        .usedStack(heldItem)
                        .buildingPlayer(player)
                        .build(world);

                // Figure out how many of the block we're rendering we have in the inventory of the player.
                IItemIndex index = new RecordingItemIndex(InventoryHelper.index(heldItem, player));
                MaterialList materials = data.getRequiredItems(buildContext, null, null);
                int hasEnergy = getEnergy(player, heldItem);

                LazyOptional<IEnergyStorage> energyCap = heldItem.getCapability(CapabilityEnergy.ENERGY);

                //Prepare the fake world -- using a fake world lets us render things properly, like fences connecting.
                getBuilderWorld().setWorldAndState(player.world, renderBlockState, coordinates);



                //Save the current position that is being rendered (I think)
                builder = buffer.getBuffer(MyRenderType.RenderBlock);
                MatrixStack matrix = evt.getMatrixStack();
                matrix.push();
                matrix.translate(-playerPos.getX(), -playerPos.getY(), -playerPos.getZ());
                Random rand = new Random();
                BlockRendererDispatcher dispatcher = getMc().getBlockRendererDispatcher();

                for (BlockPos coordinate : renderCoordinates) {
                    matrix.push();
                    matrix.translate(coordinate.getX(), coordinate.getY(), coordinate.getZ());
                    if (getBuilderWorld().getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) { //Get the block state in the fake world
                        try {
                            state = renderBlockState;
                        } catch (Exception var8) {
                        }
                    }
                    IBakedModel ibakedmodel = dispatcher.getModelForState(state);
                    BlockColors blockColors = Minecraft.getInstance().getBlockColors();
                    int color = blockColors.getColor(renderBlockState, world, coordinate, 0);
                    float f = (float) (color >> 16 & 255) / 255.0F;
                    float f1 = (float) (color >> 8 & 255) / 255.0F;
                    float f2 = (float) (color & 255) / 255.0F;
                    try {
                        if (state.getRenderType() == BlockRenderType.MODEL)
                            for (Direction direction : Direction.values()) {
                                renderModelBrightnessColorQuads(matrix.getLast(), builder, f, f1, f2, 0.7f, ibakedmodel.getQuads(state, direction, new Random(MathHelper.getPositionRandom(coordinate)), EmptyModelData.INSTANCE), 15728640, 655360);
                            }
                    } catch (Throwable t) {
                        BuildingGadgets.LOG.trace("Block at {} with state {} threw exception, whilst rendering", coordinate, state, t);
                    }
                    //Move the render position back to where it was
                    matrix.pop();
                }


                for (BlockPos coordinate : coordinates) { //Now run through the UNSORTED list of coords, to show which blocks won't place if you don't have enough of them.

                    if (energyCap.isPresent()) {
                        hasEnergy -= ((AbstractGadget) heldItem.getItem()).getEnergyCost(heldItem);
                    }
                    MatchResult match = index.tryMatch(materials);
                    if (! match.isSuccess())
                        match = index.tryMatch(InventoryHelper.PASTE_LIST);
                    if (! match.isSuccess() || hasEnergy < 0) {
                        builder = buffer.getBuffer(MyRenderType.MissingBlockOverlay);
                        renderMissingBlock(matrix.getLast().getMatrix(), builder, coordinate);
                    } else {
                        index.applyMatch(match); //notify the recording index that this counts
                    }
                }
                //TODO Bring Back TE Rendering.
                /*if (state.hasTileEntity()) {
                    TileEntity te = getTileEntityWorld().getTE(state, world);
                    TileEntityRenderer<TileEntity> teRender = getTileEntityWorld().getTER(state, world);

                    if (teRender != null && ! getInvalidTileEntities().contains(te)) {
                        for (BlockPos coordinate : coordinates) {
                            te.setPos(coordinate);
                            matrix.push();
                            matrix.translate(coordinate.getX(), coordinate.getY(), coordinate.getZ());
                            RenderSystem.multMatrix(matrix.getLast().getMatrix());
                            RenderSystem.color4f(1F, 1F, 1F, 1F);
                            RenderSystem.scalef(1.0f, 1.0f, 1.0f); //Block scale 1 = full sized block
                            RenderSystem.enableBlend(); //We have to do this in the loop because the TE Render removes blend when its done
                            RenderSystem.blendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);
                            try {
                                teRender.render(te, evt.getPartialTicks(), evt.getMatrixStack(), IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer()), 0, 0);
                            } catch (Exception e) {
                                BuildingGadgets.LOG.warn("TER Exception with block type: " + state);
                                getInvalidTileEntities().add(te);
                                RenderSystem.disableFog();
                                RenderSystem.popMatrix();
                                break;
                            }
                            RenderSystem.disableFog();
                            RenderSystem.popMatrix();
                            matrix.pop();
                        }
                    }
                }*/
                matrix.pop();
                RenderSystem.disableDepthTest();
                buffer.finish();
            }
        }
    }

    @Override
    public boolean isLinkable() {
        return true;
    }
}
