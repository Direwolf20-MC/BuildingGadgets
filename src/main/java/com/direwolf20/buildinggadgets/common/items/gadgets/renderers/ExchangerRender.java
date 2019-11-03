package com.direwolf20.buildinggadgets.common.items.gadgets.renderers;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.building.BlockData;
import com.direwolf20.buildinggadgets.common.building.modes.ExchangingMode;
import com.direwolf20.buildinggadgets.common.building.view.BuildContext;
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
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
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
        if (startBlock != OurBlocks.effectBlock.getDefaultState()) {
            BlockData data = getToolBlock(heldItem);
            BlockState renderBlockState = data.getState();

            getMc().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            if (renderBlockState == AIR) {//Don't render anything if there is no block selected (Air)
                return;
            }
            List<BlockPos> renderCoordinates;
            if (coordinates.size() == 0) { //Build a list of coordinates based on the tool mode and range
                coordinates = ExchangingMode
                        .collectPlacementPos(world, player, lookingAt.getPos(), lookingAt.getFace(), heldItem, lookingAt.getPos());
                renderCoordinates = coordinates;
            } else { //anchors need to be resorted
                renderCoordinates = SortingHelper.Blocks.byDistance(coordinates, player);
            }

            //Figure out how many of the block we're rendering we have in the inventory of the player.
            //ItemStack itemStack = renderBlockState.getBlock().getPickBlock(renderBlockState, null, world, new BlockPos(0, 0, 0), player);
            //ItemStack itemStack = InventoryHelper.getSilkTouchDrop(renderBlockState);

            BuildContext buildContext = BuildContext.builder()
                    .usedStack(heldItem)
                    .buildingPlayer(player)
                    .build(world);
            // Figure out how many of the block we're rendering we have in the inventory of the player.
            IItemIndex index = new RecordingItemIndex(InventoryHelper.index(heldItem, player));
            MaterialList materials = data.getRequiredItems(buildContext, null, null);
            int hasEnergy       = getEnergy(player, heldItem);

            LazyOptional<IEnergyStorage> energy = CapabilityUtil.EnergyUtil.getCap(heldItem);

            //Prepare the fake world -- using a fake world lets us render things properly, like fences connecting.
            getBuilderWorld().setWorldAndState(player.world, renderBlockState, coordinates);

            //Save the current position that is being rendered (I think)
            GlStateManager.pushMatrix();
            GlStateManager.translated(- playerPos.getX(), - playerPos.getY(), - playerPos.getZ());
            //Enable Blending (So we can have transparent effect)
            GlStateManager.enableBlend();
            //This blend function allows you to use a constant alpha, which is defined later
            /*
            GlStateManager.blendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);
            BufferBuilder builder = Tessellator.getInstance().getBuffer();
            builder.begin(GL14.GL_QUADS, DefaultVertexFormats.BLOCK);
            Random rand = new Random();*/
            BlockRendererDispatcher dispatcher = getMc().getBlockRendererDispatcher();
            for (BlockPos coordinate : renderCoordinates) {
                GlStateManager.pushMatrix();//Push matrix again just because
                GlStateManager.translatef(coordinate.getX() - 0.001f, coordinate.getY() - 0.001f, coordinate.getZ() - 0.001f);
                GlStateManager.rotatef(- 90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
                GlStateManager.scalef(1.002f, 1.002f, 1.002f);
                GL14.glBlendColor(1F, 1F, 1F, 0.55f); //Set the alpha of the blocks we are rendering
                if (getBuilderWorld().getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) { //Get the block state in the fake world
                    try {
                        state = renderBlockState;
                    } catch (Exception var8) {
                        var8.printStackTrace();
                    }
                }
                try {
                    //cannot render into buffer, because we can't scale it in that case, which causes z-Fighting
                    if (state.getRenderType() == BlockRenderType.MODEL)
                        dispatcher.renderBlockBrightness(state, 1f);
                    //dispatcher.renderBlock(state, coordinate, world, builder, rand, EmptyModelData.INSTANCE);
                } catch (Throwable t) {
                    BuildingGadgets.LOG.trace("Block at {} with state {} threw exception, whilst rendering", coordinate, state, t);
                }
                //Move the render position back to where it was
                GlStateManager.popMatrix();
            }
            //Tessellator.getInstance().draw();
            BufferBuilder bufferBuilder = setupMissingRender();
            for (BlockPos coordinate : coordinates) {
                if (energy.isPresent()) {
                    hasEnergy -= (((AbstractGadget) heldItem.getItem())).getEnergyCost(heldItem);
                }
                MatchResult match = index.tryMatch(materials);
                if (! match.isSuccess())
                    match = index.tryMatch(InventoryHelper.PASTE_LIST);
                if (! match.isSuccess() || hasEnergy < 0) {
                    renderMissingBlock(bufferBuilder, coordinate);
                } else {
                    index.applyMatch(match); //notify the recording index that this counts
                }
                //Move the render position back to where it was
            }
            teardownMissingRender();

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
                            teRender.render(te, 0, 0, 0, evt.getPartialTicks(), - 1);
                        } catch (Exception e) {
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
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean isLinkable() {
        return true;
    }
}
