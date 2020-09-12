package com.direwolf20.buildinggadgets.client.renders;

import com.direwolf20.buildinggadgets.client.renderer.OurRenderTypes;
import com.direwolf20.buildinggadgets.common.blocks.OurBlocks;
import com.direwolf20.buildinggadgets.common.tainted.building.BlockData;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.tainted.inventory.IItemIndex;
import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.tainted.inventory.MatchResult;
import com.direwolf20.buildinggadgets.common.tainted.inventory.RecordingItemIndex;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.items.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.GadgetExchanger;
import com.direwolf20.buildinggadgets.common.items.modes.AbstractMode;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.List;
import java.util.Optional;

import static com.direwolf20.buildinggadgets.common.util.GadgetUtils.getAnchor;
import static com.direwolf20.buildinggadgets.common.util.GadgetUtils.getToolBlock;

public class BuildRender extends BaseRenderer {
    private final boolean isExchanger;
    private static final BlockState DEFAULT_EFFECT_BLOCK = OurBlocks.EFFECT_BLOCK.get().getDefaultState();

    public BuildRender(boolean isExchanger) {
        this.isExchanger = isExchanger;
    }

    @Override
    public void render(RenderWorldLastEvent evt, PlayerEntity player, ItemStack heldItem) {
        super.render(evt, player, heldItem);

        BlockRayTraceResult lookingAt = VectorHelper.getLookingAt(player, heldItem);

        BlockState state = AIR;
        Optional<List<BlockPos>> anchor = getAnchor(heldItem);

        BlockState startBlock = player.world.getBlockState(lookingAt.getPos());
        if( (player.world.isAirBlock(lookingAt.getPos()) && !anchor.isPresent()) || startBlock == DEFAULT_EFFECT_BLOCK )
            return;

        BlockData data = getToolBlock(heldItem);
        BlockState renderBlockState = data.getState();
        if (renderBlockState == BaseRenderer.AIR)
            return;

        // Get the coordinates from the anchor. If the anchor isn't present then build the collector.
        List<BlockPos> coordinates = anchor.orElseGet(() -> {
            AbstractMode mode = !this.isExchanger ? GadgetBuilding.getToolMode(heldItem).getMode() : GadgetExchanger.getToolMode(heldItem).getMode();
            return mode.getCollection(
                    new AbstractMode.UseContext(player.world, renderBlockState, lookingAt.getPos(), heldItem, lookingAt.getFace(), !this.isExchanger && GadgetBuilding.shouldPlaceAtop(heldItem)),
                    player
            );
        });

        // Sort them on a new line for readability
//        coordinates = SortingHelper.Blocks.byDistance(coordinates, player);

        //Prepare the fake world -- using a fake world lets us render things properly, like fences connecting.
        getBuilderWorld().setWorldAndState(player.world, renderBlockState, coordinates);

        Vector3d playerPos = getMc().gameRenderer.getActiveRenderInfo().getProjectedView();
        IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();

        //Save the current position that is being rendered (I think)
        MatrixStack matrix = evt.getMatrixStack();
        matrix.push();
        matrix.translate(-playerPos.getX(), -playerPos.getY(), -playerPos.getZ());

        BlockRendererDispatcher dispatcher = getMc().getBlockRendererDispatcher();

        for (BlockPos coordinate : coordinates) {
            matrix.push();
            matrix.translate(coordinate.getX(), coordinate.getY(), coordinate.getZ());
            if( this.isExchanger ) {
                matrix.translate(-0.0005f, -0.0005f, -0.0005f);
                matrix.scale(1.001f, 1.001f, 1.001f);
            }

            // todo: add back from 1.16 port
//            if (getBuilderWorld().getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) { //Get the block state in the fake world
//                try {
                    state = renderBlockState;
//                } catch (Exception ignored) {}
//            }

            OurRenderTypes.MultiplyAlphaRenderTypeBuffer mutatedBuffer = new OurRenderTypes.MultiplyAlphaRenderTypeBuffer(Minecraft.getInstance().getRenderTypeBuffers().getBufferSource(), .55f);
            dispatcher.renderBlock(
                    state, matrix, mutatedBuffer, 15728640, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE
            );

            //Move the render position back to where it was
            matrix.pop();
            RenderSystem.disableDepthTest();
            buffer.finish(); // @mcp: finish (mcp) = draw (yarn)
        }

        // Don't even waste the time checking to see if we have the right energy, items, etc for creative mode
        if (!player.isCreative()) {
            IVertexBuilder builder;

            // Figure out how many of the block we're rendering we have in the inventory of the player.
            IItemIndex index = new RecordingItemIndex(InventoryHelper.index(heldItem, player));
            MaterialList materials = data.getRequiredItems(new BuildContext(player.world, player, heldItem), null, null);
            int hasEnergy = getEnergy(player, heldItem);

            LazyOptional<IEnergyStorage> energyCap = heldItem.getCapability(CapabilityEnergy.ENERGY);

            for (BlockPos coordinate : coordinates) { //Now run through the UNSORTED list of coords, to show which blocks won't place if you don't have enough of them.
                if (energyCap.isPresent())
                    hasEnergy -= ((AbstractGadget) heldItem.getItem()).getEnergyCost(heldItem);

                builder = buffer.getBuffer(OurRenderTypes.MissingBlockOverlay);
                MatchResult match = index.tryMatch(materials);
                if (!match.isSuccess())
                    match = index.tryMatch(InventoryHelper.PASTE_LIST);
                if (!match.isSuccess() || hasEnergy < 0) {
                    renderMissingBlock(matrix.getLast().getMatrix(), builder, coordinate);
                } else {
                    index.applyMatch(match); //notify the recording index that this counts
                    renderBoxSolid(matrix.getLast().getMatrix(), builder, coordinate, .97f, 1f, .99f, .1f);
                }
            }
        }

        matrix.pop();
        RenderSystem.disableDepthTest();
        buffer.finish(); // @mcp: finish (mcp) = draw (yarn)
    }

    @Override
    public boolean isLinkable() {
        return true;
    }

}
