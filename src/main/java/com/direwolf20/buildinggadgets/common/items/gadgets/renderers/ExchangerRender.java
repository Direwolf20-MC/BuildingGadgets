package com.direwolf20.buildinggadgets.common.items.gadgets.renderers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderWorldLastEvent;

public class ExchangerRender extends BaseRenderer {

    @Override
    public void render(RenderWorldLastEvent evt, PlayerEntity player, ItemStack heldItem) {
        super.render(evt, player, heldItem);
//
//        BlockRayTraceResult lookingAt = VectorHelper.getLookingAt(player, heldItem);
//        Vec3d playerPos = getMc().gameRenderer.getActiveRenderInfo().getProjectedView();
//
//        BlockState state = AIR;
//        List<BlockPos> coordinates = getAnchor(heldItem);
//        //if (lookingAt == null) {
//        //    coordinates.size();
//        //}
//        if ((lookingAt == null || (player.world.getBlockState(VectorHelper.getLookingAt(player, heldItem).getPos()) == AIR)) && coordinates.size() == 0)
//            return;
//        World world = player.world;
//        BlockState startBlock = AIR;
//        startBlock = world.getBlockState(new BlockPos(lookingAt.getPos()));
//        if (startBlock != OurBlocks.effectBlock.getDefaultState()) {
//            BlockData data = getToolBlock(heldItem);
//            BlockState renderBlockState = data.getState();
//
//            getMc().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
//            if (renderBlockState == AIR) {//Don't render anything if there is no block selected (Air)
//                return;
//            }
//            List<BlockPos> renderCoordinates;
//            if (coordinates.size() == 0) { //Build a list of coordinates based on the tool mode and range
//                coordinates = ExchangingMode
//                        .collectPlacementPos(world, player, lookingAt.getPos(), lookingAt.getFace(), heldItem, lookingAt.getPos());
//                renderCoordinates = coordinates;
//            } else { //anchors need to be resorted
//                renderCoordinates = SortingHelper.Blocks.byDistance(coordinates, player);
//            }
//
//            //Figure out how many of the block we're rendering we have in the inventory of the player.
//            //ItemStack itemStack = renderBlockState.getBlock().getPickBlock(renderBlockState, null, world, new BlockPos(0, 0, 0), player);
//            //ItemStack itemStack = InventoryHelper.getSilkTouchDrop(renderBlockState);
//
//            IBuildContext buildContext = SimpleBuildContext.builder()
//                    .usedStack(heldItem)
//                    .buildingPlayer(player)
//                    .build(world);
//            // Figure out how many of the block we're rendering we have in the inventory of the player.
//            IItemIndex index = new RecordingItemIndex(InventoryHelper.index(heldItem, player));
//            MaterialList materials = data.getRequiredItems(buildContext, null, null);
//            int hasEnergy       = getEnergy(player, heldItem);
//
//            LazyOptional<IEnergyStorage> energy = CapabilityUtil.EnergyUtil.getCap(heldItem);
//
//            //Prepare the fake world -- using a fake world lets us render things properly, like fences connecting.
//            getBuilderWorld().setWorldAndState(player.world, renderBlockState, coordinates);
//
//            //Save the current position that is being rendered (I think)
//            RenderSystem.pushMatrix();
//            RenderSystem.translated(- playerPos.getX(), - playerPos.getY(), - playerPos.getZ());
//            //Enable Blending (So we can have transparent effect)
//            RenderSystem.enableBlend();
//            //This blend function allows you to use a constant alpha, which is defined later
//            /*
//            GlStateManager.blendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);
//            BufferBuilder builder = Tessellator.getInstance().getBuffer();
//            builder.begin(GL14.GL_QUADS, DefaultVertexFormats.BLOCK);
//            Random rand = new Random();*/
//            BlockRendererDispatcher dispatcher = getMc().getBlockRendererDispatcher();
//            for (BlockPos coordinate : renderCoordinates) {
//                RenderSystem.pushMatrix();//Push matrix again just because
//                RenderSystem.translatef(coordinate.getX() - 0.001f, coordinate.getY() - 0.001f, coordinate.getZ() - 0.001f);
//                RenderSystem.rotatef(- 90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
//                RenderSystem.scalef(1.002f, 1.002f, 1.002f);
//                GL14.glBlendColor(1F, 1F, 1F, 0.55f); //Set the alpha of the blocks we are rendering
//                if (getBuilderWorld().getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) { //Get the block state in the fake world
//                    try {
//                        state = renderBlockState;
//                    } catch (Exception var8) {
//                        var8.printStackTrace();
//                    }
//                }
//                try {
//                    //cannot render into buffer, because we can't scale it in that case, which causes z-Fighting
//                    if (state.getRenderType() == BlockRenderType.MODEL)
//                        dispatcher.renderBlock(state, evt.getMatrixStack(), IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer()), 0, 0, EmptyModelData.INSTANCE);
//                } catch (Throwable t) {
//                    BuildingGadgets.LOG.trace("Block at {} with state {} threw exception, whilst rendering", coordinate, state, t);
//                }
//                //Move the render position back to where it was
//                RenderSystem.popMatrix();
//            }
//            //Tessellator.getInstance().draw();
//            BufferBuilder bufferBuilder = setupMissingRender();
//            for (BlockPos coordinate : coordinates) {
//                if (energy.isPresent()) {
//                    hasEnergy -= (((AbstractGadget) heldItem.getItem())).getEnergyCost(heldItem);
//                }
//                MatchResult match = index.tryMatch(materials);
//                if (! match.isSuccess())
//                    match = index.tryMatch(InventoryHelper.PASTE_LIST);
//                if (! match.isSuccess() || hasEnergy < 0) {
//                    renderMissingBlock(bufferBuilder, coordinate);
//                } else {
//                    index.applyMatch(match); //notify the recording index that this counts
//                }
//                //Move the render position back to where it was
//            }
//            teardownMissingRender();
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
//            RenderSystem.popMatrix();
//        }
    }

    @Override
    public boolean isLinkable() {
        return true;
    }
}
