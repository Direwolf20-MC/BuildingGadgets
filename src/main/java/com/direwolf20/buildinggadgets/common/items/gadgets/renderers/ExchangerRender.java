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
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetExchanger;
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

public class ExchangerRender extends BaseRenderer {

    @Override
    public void render(RenderWorldLastEvent evt, PlayerEntity player, ItemStack heldItem) {
        super.render(evt, player, heldItem);
        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        IVertexBuilder builder;

        BlockRayTraceResult lookingAt = VectorHelper.getLookingAt(player, heldItem);
        Vec3d playerPos = getMc().gameRenderer.getActiveRenderInfo().getProjectedView();

        BlockState state = AIR;
        List<BlockPos> coordinates = getAnchor(heldItem);
        //if (lookingAt == null) {
        //    coordinates.size();
        //}
        if ((lookingAt == null || (player.world.getBlockState(VectorHelper.getLookingAt(player, heldItem).getPos()) == AIR)) && coordinates.size() == 0)
            return;
        World world = player.world;
        BlockState startBlock = AIR;
        startBlock = world.getBlockState(lookingAt.getPos());
        if (startBlock != OurBlocks.effectBlock.getDefaultState()) {
            BlockData data = getToolBlock(heldItem);
            BlockState renderBlockState = data.getState();

            //getMc().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            if (renderBlockState == AIR) {//Don't render anything if there is no block selected (Air)
                return;
            }
            List<BlockPos> renderCoordinates;
            if (coordinates.size() == 0) { //Build a list of coordinates based on the tool mode and range
                coordinates = GadgetExchanger.getToolMode(heldItem).getMode().getCollection(
                        new AbstractMode.UseContext(
                                world,
                                renderBlockState,
                                lookingAt.getPos(),
                                heldItem
                        ), player,
                        lookingAt.getFace()
                );
                renderCoordinates = coordinates;
            } else { //anchors need to be resorted
                renderCoordinates = SortingHelper.Blocks.byDistance(coordinates, player);
            }

            //Figure out how many of the block we're rendering we have in the inventory of the player.
            //ItemStack itemStack = renderBlockState.getBlock().getPickBlock(renderBlockState, null, world, new BlockPos(0, 0, 0), player);
            //ItemStack itemStack = InventoryHelper.getSilkTouchDrop(renderBlockState);

            IBuildContext buildContext = new SimpleBuildContext(world, player, heldItem);

            // Figure out how many of the block we're rendering we have in the inventory of the player.
            IItemIndex index = new RecordingItemIndex(InventoryHelper.index(heldItem, player));
            MaterialList materials = data.getRequiredItems(buildContext, null, null);

            int hasEnergy = getEnergy(player, heldItem);
            LazyOptional<IEnergyStorage> energy = heldItem.getCapability(CapabilityEnergy.ENERGY);

            //Prepare the fake world -- using a fake world lets us render things properly, like fences connecting.
            getBuilderWorld().setWorldAndState(player.world, renderBlockState, coordinates);
            builder = buffer.getBuffer(MyRenderType.RenderBlock);
            MatrixStack matrix = evt.getMatrixStack();
            matrix.push();
            matrix.translate(-playerPos.getX(), -playerPos.getY(), -playerPos.getZ());
            Random rand = new Random();
            BlockRendererDispatcher dispatcher = getMc().getBlockRendererDispatcher();

            for (BlockPos coordinate : renderCoordinates) {
                matrix.push();
                matrix.translate(coordinate.getX(), coordinate.getY(), coordinate.getZ());
                matrix.translate(-0.0005f, -0.0005f, -0.0005f);
                matrix.scale(1.001f, 1.001f, 1.001f);


                state = renderBlockState;

                IBakedModel ibakedmodel = dispatcher.getModelForState(state);
                BlockColors blockColors = Minecraft.getInstance().getBlockColors();
                int color = blockColors.getColor(renderBlockState, world, coordinate, 0);
                float f = (float) (color >> 16 & 255) / 255.0F;
                float f1 = (float) (color >> 8 & 255) / 255.0F;
                float f2 = (float) (color & 255) / 255.0F;
                try {
                    //cannot render into buffer, because we can't scale it in that case, which causes z-Fighting
                    if (state.getRenderType() == BlockRenderType.MODEL)
                        for (Direction direction : Direction.values()) {
                            renderModelBrightnessColorQuads(matrix.getLast(), builder, f, f1, f2, 0.65f, ibakedmodel.getQuads(state, direction, new Random(MathHelper.getPositionRandom(coordinate)), EmptyModelData.INSTANCE), 15728640, 655360);
                        }
                } catch (Throwable t) {
                    BuildingGadgets.LOG.trace("Block at {} with state {} threw exception, whilst rendering", coordinate, state, t);
                }
                //Move the render position back to where it was
                matrix.pop();
            }

            for (BlockPos coordinate : coordinates) {
                if (energy.isPresent()) {
                    hasEnergy -= (((AbstractGadget) heldItem.getItem())).getEnergyCost(heldItem);
                }
                MatchResult match = index.tryMatch(materials);
                if (!match.isSuccess())
                    match = index.tryMatch(InventoryHelper.PASTE_LIST);
                if (!match.isSuccess() || hasEnergy < 0) {
                    builder = buffer.getBuffer(MyRenderType.MissingBlockOverlay);
                    renderMissingBlock(matrix.getLast().getMatrix(), builder, coordinate);
                } else {
                    index.applyMatch(match); //notify the recording index that this counts
                }
                //Move the render position back to where it was
            }

//
//            if (state.hasTileEntity()) {
//                TileEntity te = getTileEntityWorld().getTE(state, world);
//                TileEntityRenderer<TileEntity> teRender = getTileEntityWorld().getTER(state, world);
//                if (teRender != null && ! getInvalidTileEntities().contains(te)) {
//                    for (BlockPos coordinate : coordinates) {
//                        te.setPos(coordinate);
//                        RenderSystem.pushMatrix();
//                        RenderSystem.color4f(1F, 1F, 1F, 1F);
//                        RenderSystem.translated(-playerPos.getX(), -playerPos.getY(), - playerPos.getZ());
//                        RenderSystem.translatef(coordinate.getX(), coordinate.getY(), coordinate.getZ());
//                        RenderSystem.scalef(1.0f, 1.0f, 1.0f); //Block scale 1 = full sized block
//                        RenderSystem.enableBlend(); //We have to do this in the loop because the TE Render removes blend when its done
//                        RenderSystem.blendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);
//                        try {
//                            // 0 0 is not right for this method todo: fix!
//                            teRender.render(te, evt.getPartialTicks(), evt.getMatrixStack(), IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer()), 0, 0);
//                        } catch (Exception e) {
//                            getInvalidTileEntities().add(te);
//                            RenderSystem.disableFog();
//                            RenderSystem.popMatrix();
//                            break;
//                        }
//                        RenderSystem.disableFog();
//                        RenderSystem.popMatrix();
//                    }
//                }
//            }
//            //Set blending back to the default mode
//            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//            ForgeHooksClient.setRenderLayer(MinecraftForgeClient.getRenderLayer());
//            //Disable blend
//            RenderSystem.disableBlend();
//            //Pop from the original push in this method
            matrix.pop();
            RenderSystem.disableDepthTest();
            buffer.finish();
        }
    }

    @Override
    public boolean isLinkable() {
        return true;
    }
}
