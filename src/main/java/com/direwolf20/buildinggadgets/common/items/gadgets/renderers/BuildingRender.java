package com.direwolf20.buildinggadgets.common.items.gadgets.renderers;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.building.BlockData;
import com.direwolf20.buildinggadgets.common.building.modes.BuildingMode;
import com.direwolf20.buildinggadgets.common.building.modes.ExchangingMode;
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
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static com.direwolf20.buildinggadgets.common.util.GadgetUtils.getAnchor;
import static com.direwolf20.buildinggadgets.common.util.GadgetUtils.getToolBlock;

public class BuildingRender extends BaseRenderer {
    private boolean isExchanger;

    public BuildingRender(boolean isExchanger) {
        this.isExchanger = isExchanger;
    }

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

                BlockData data = getToolBlock(heldItem);
                BlockState renderBlockState = data.getState();
                if (renderBlockState == BaseRenderer.AIR) {//Don't render anything if there is no block selected (Air)
                    return;
                }
                List<BlockPos> renderCoordinates;

                if (coordinates.size() == 0) { //Build a list of coordinates based on the tool mode and range
                    if (isExchanger) {
                        coordinates = ExchangingMode
                                .collectPlacementPos(world, player, lookingAt.getPos(), lookingAt.getFace(), heldItem, lookingAt.getPos());
                    } else {
                        coordinates = BuildingMode
                                .collectPlacementPos(world, player, lookingAt.getPos(), lookingAt.getFace(), heldItem, lookingAt.getPos());
                    }

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

                LazyOptional<IEnergyStorage> energyCap = CapabilityUtil.EnergyUtil.getCap(heldItem);

                //Prepare the fake world -- using a fake world lets us render things properly, like fences connecting.
                getBuilderWorld().setWorldAndState(player.world, renderBlockState, coordinates);

                //Save the current position that is being rendered (I think)
                GlStateManager.pushMatrix();
                GlStateManager.translated(- playerPos.getX(), - playerPos.getY(), - playerPos.getZ());//The render starts at the player, so we subtract the player coords and move the render to 0,0,0
                GlStateManager.pushTextureAttributes();
                //Enable Blending (So we can have transparent effect)
                GlStateManager.enableBlend();

                //This blend function allows you to use a constant alpha, which is defined later
                GlStateManager.blendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);

                state = renderBlockState;
                BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();

                for (BlockPos coordinate : renderCoordinates) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translatef(coordinate.getX() - 0.001f, coordinate.getY() - 0.001f, coordinate.getZ() - 0.001f);
                    GlStateManager.rotatef(-90f, 0f, 1f, 0f);
                    if (isExchanger) {
                        GlStateManager.scalef(1.002f, 1.002f, 1.002f);
                    }
                    GL14.glBlendColor(1F, 1F, 1F, 0.55f);

                    try {
                        blockrendererdispatcher.renderBlockBrightness(state, 1.0f);
                    } catch (Throwable t) {
                        BuildingGadgets.LOG.trace("Block at {} with state {} threw exception, whilst rendering", coordinate, state, t);
                    }

                    GlStateManager.popMatrix();
                }

                // Render missing and valid overlays
                BufferBuilder bufferBuilder = setupMissingRender();
                for (BlockPos coordinate : coordinates) { //Now run through the UNSORTED list of coords, to show which blocks won't place if you don't have enough of them.

                    if (energyCap.isPresent()) {
                        hasEnergy -= ((AbstractGadget) heldItem.getItem()).getEnergyCost(heldItem);
                    }
                    MatchResult match = index.tryMatch(materials);
                    if (! match.isSuccess())
                        match = index.tryMatch(InventoryHelper.PASTE_LIST);
                    if (! match.isSuccess() || hasEnergy < 0) {
                        renderMissingBlock(bufferBuilder, coordinate);
                    } else {
                        index.applyMatch(match); //notify the recording index that this counts
                        renderBlockOverlay(bufferBuilder, coordinate, .97f, 1f, .99f, .1f);//(bufferBuilder, 0, 0, 0, 1, 1 ,1, 0, 1, 0, .3f);
                    }
                    //Move the render position back to where it was
                }
                teardownMissingRender();

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
