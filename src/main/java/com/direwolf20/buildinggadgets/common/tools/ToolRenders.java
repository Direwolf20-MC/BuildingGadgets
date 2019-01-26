package com.direwolf20.buildinggadgets.common.tools;

import com.direwolf20.buildinggadgets.common.blocks.ModBlocks;
import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import com.direwolf20.buildinggadgets.common.items.FakeBuilderWorld;
import com.direwolf20.buildinggadgets.common.items.FakeBuilderWorld;
import com.direwolf20.buildinggadgets.common.items.ModItems;
import com.direwolf20.buildinggadgets.common.items.capability.CapabilityProviderEnergy;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetExchanger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.direwolf20.buildinggadgets.common.tools.GadgetUtils.getAnchor;
import static com.direwolf20.buildinggadgets.common.tools.GadgetUtils.getToolBlock;
import static net.minecraft.block.BlockStainedGlass.COLOR;

public class ToolRenders {
    private static final FakeBuilderWorld fakeWorld = new FakeBuilderWorld();

    public static void renderBuilderOverlay(RenderWorldLastEvent evt, EntityPlayer player, ItemStack stack) {
        RayTraceResult lookingAt = VectorTools.getLookingAt(player);
        IBlockState state = Blocks.AIR.getDefaultState();
        List<BlockPos> coordinates = getAnchor(stack);
        if (lookingAt != null || coordinates.size() > 0) {
            World world = player.world;
            IBlockState startBlock = Blocks.AIR.getDefaultState();
            if (!(lookingAt == null)) {
                startBlock = world.getBlockState(lookingAt.getBlockPos());
            }
            if (startBlock != ModBlocks.effectBlock.getDefaultState()) {

                ItemStack heldItem = GadgetBuilding.getGadget(player);
                if (heldItem.isEmpty()) return;

                IBlockState renderBlockState = getToolBlock(heldItem);
                Minecraft mc = Minecraft.getMinecraft();
                mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                if (renderBlockState == Blocks.AIR.getDefaultState()) {//Don't render anything if there is no block selected (Air)
                    return;
                }
                if (coordinates.size() == 0 && lookingAt != null) { //Build a list of coordinates based on the tool mode and range
                    coordinates = BuildingModes.getBuildOrders(world, player, lookingAt.getBlockPos(), lookingAt.sideHit, heldItem);
                }

                //Figure out how many of the block we're rendering we have in the inventory of the player.
                ItemStack itemStack;
                if (renderBlockState.getBlock().canSilkHarvest(world, new BlockPos(0, 0, 0), renderBlockState, player)) {
                    itemStack = InventoryManipulation.getSilkTouchDrop(renderBlockState);
                } else {
                    itemStack = renderBlockState.getBlock().getPickBlock(renderBlockState, null, world, new BlockPos(0, 0, 0), player);
                }
                if (itemStack.getItem().equals(Items.AIR)) {
                    itemStack = renderBlockState.getBlock().getPickBlock(renderBlockState, null, world, new BlockPos(0, 0, 0), player);
                }

                int hasBlocks = InventoryManipulation.countItem(itemStack, player, world);
                hasBlocks = hasBlocks + InventoryManipulation.countPaste(player);
                int hasEnergy = 0;
                if (SyncedConfig.poweredByFE) {
                    hasEnergy = CapabilityProviderEnergy.getCap(stack).getEnergyStored();
                } else {
                    hasEnergy = stack.getMaxDamage() - stack.getItemDamage();
                }
                if (player.capabilities.isCreativeMode || (!SyncedConfig.poweredByFE && !stack.isItemStackDamageable())) {
                    hasEnergy = 1000000;
                }
                //Prepare the block rendering
                BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
                BlockRenderLayer origLayer = MinecraftForgeClient.getRenderLayer();

                //Prepare the fake world -- using a fake world lets us render things properly, like fences connecting.
                Set<BlockPos> coords = new HashSet<BlockPos>(coordinates);
                fakeWorld.setWorldAndState(player.world, renderBlockState, coords);

                //Calculate the players current position, which is needed later
                double doubleX = player.lastTickPosX + (player.posX - player.lastTickPosX) * evt.getPartialTicks();
                double doubleY = player.lastTickPosY + (player.posY - player.lastTickPosY) * evt.getPartialTicks();
                double doubleZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * evt.getPartialTicks();

                //Save the current position that is being rendered (I think)
                GlStateManager.pushMatrix();
                //Enable Blending (So we can have transparent effect)
                GlStateManager.enableBlend();
                //This blend function allows you to use a constant alpha, which is defined later
                GlStateManager.blendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);

                List<BlockPos> sortedCoordinates = BuildingModes.sortByDistance(coordinates, player); //Sort the coords by distance to player.

                for (BlockPos coordinate : sortedCoordinates) {
                    GlStateManager.pushMatrix();//Push matrix again just because
                    GlStateManager.translate(-doubleX, -doubleY, -doubleZ);//The render starts at the player, so we subtract the player coords and move the render to 0,0,0
                    GlStateManager.translate(coordinate.getX(), coordinate.getY(), coordinate.getZ());//Now move the render position to the coordinates we want to render at
                    GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
                    GlStateManager.scale(1.0f, 1.0f, 1.0f); //Block scale 1 = full sized block
                    GL14.glBlendColor(1F, 1F, 1F, 0.55f); //Set the alpha of the blocks we are rendering
                    if (fakeWorld.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) { //Get the block state in the fake world
                        try {
                            state = renderBlockState.getActualState(fakeWorld, coordinate);
                        } catch (Exception var8) {
                        }
                    }
                    //state = state.getBlock().getExtendedState(state, fakeWorld, coordinate); //Get the extended block state in the fake world (Disabled to fix chisel, not sure why.)
                    try {
                        dispatcher.renderBlockBrightness(state, 1f);//Render the defined block
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
                    GlStateManager.translate(-doubleX, -doubleY, -doubleZ);//The render starts at the player, so we subtract the player coords and move the render to 0,0,0
                    GlStateManager.translate(coordinate.getX(), coordinate.getY(), coordinate.getZ());//Now move the render position to the coordinates we want to render at
                    GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
                    GlStateManager.translate(-0.005f, -0.005f, 0.005f);
                    GlStateManager.scale(1.01f, 1.01f, 1.01f);
                    GL14.glBlendColor(1F, 1F, 1F, 0.35f); //Set the alpha of the blocks we are rendering
                    hasBlocks--;
                    if (SyncedConfig.poweredByFE) {
                        hasEnergy = hasEnergy - ModItems.gadgetBuilding.getEnergyCost();
                    } else {
                        hasEnergy--;
                    }

                    if (hasBlocks < 0 || hasEnergy < 0) {
                        dispatcher.renderBlockBrightness(Blocks.STAINED_GLASS.getDefaultState().withProperty(COLOR, EnumDyeColor.RED), 1f);
                    }
                    //Move the render position back to where it was
                    GlStateManager.popMatrix();
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
    }

    public static void renderExchangerOverlay(RenderWorldLastEvent evt, EntityPlayer player, ItemStack stack) {
//        int range = getToolRange(stack);
//        GadgetExchanger.ToolMode mode = GadgetExchanger.getToolMode(stack);
        RayTraceResult lookingAt = VectorTools.getLookingAt(player);
        IBlockState state = Blocks.AIR.getDefaultState();
        List<BlockPos> coordinates = getAnchor(stack);
        if (lookingAt != null || coordinates.size() > 0) {
            World world = player.world;
            IBlockState startBlock = Blocks.AIR.getDefaultState();
            if (!(lookingAt == null)) {
                startBlock = world.getBlockState(lookingAt.getBlockPos());
            }
            if (startBlock != ModBlocks.effectBlock.getDefaultState()) {
                ItemStack heldItem = GadgetExchanger.getGadget(player);
                if (heldItem.isEmpty()) return;

                IBlockState renderBlockState = getToolBlock(heldItem);
                Minecraft mc = Minecraft.getMinecraft();
                mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                if (renderBlockState == Blocks.AIR.getDefaultState()) {//Don't render anything if there is no block selected (Air)
                    return;
                }
                if (coordinates.size() == 0 && lookingAt != null) { //Build a list of coordinates based on the tool mode and range
                    coordinates = ExchangingModes.getBuildOrders(world, player, lookingAt.getBlockPos(), lookingAt.sideHit, stack);
                }

                //Figure out how many of the block we're rendering we have in the inventory of the player.
                //ItemStack itemStack = renderBlockState.getBlock().getPickBlock(renderBlockState, null, world, new BlockPos(0, 0, 0), player);
                //ItemStack itemStack = InventoryManipulation.getSilkTouchDrop(renderBlockState);
                ItemStack itemStack;
                if (renderBlockState.getBlock().canSilkHarvest(world, new BlockPos(0, 0, 0), renderBlockState, player)) {
                    itemStack = InventoryManipulation.getSilkTouchDrop(renderBlockState);
                } else {
                    itemStack = renderBlockState.getBlock().getPickBlock(renderBlockState, null, world, new BlockPos(0, 0, 0), player);
                }
                if (itemStack.getItem().equals(Items.AIR)) {
                    itemStack = renderBlockState.getBlock().getPickBlock(renderBlockState, null, world, new BlockPos(0, 0, 0), player);
                }
                int hasBlocks = InventoryManipulation.countItem(itemStack, player, world);
                hasBlocks = hasBlocks + InventoryManipulation.countPaste(player);
                int hasEnergy = 0;
                if (SyncedConfig.poweredByFE) {
                    hasEnergy = CapabilityProviderEnergy.getCap(stack).getEnergyStored();
                } else {
                    hasEnergy = stack.getMaxDamage() - stack.getItemDamage();
                }
                if (player.capabilities.isCreativeMode || (!SyncedConfig.poweredByFE && !stack.isItemStackDamageable())) {
                    hasEnergy = 1000000;
                }
                //Prepare the block rendering
                BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
                BlockRenderLayer origLayer = MinecraftForgeClient.getRenderLayer();

                //Prepare the fake world -- using a fake world lets us render things properly, like fences connecting.
                Set<BlockPos> coords = new HashSet<BlockPos>(coordinates);
                fakeWorld.setWorldAndState(player.world, renderBlockState, coords);

                //Calculate the players current position, which is needed later
                double doubleX = player.lastTickPosX + (player.posX - player.lastTickPosX) * evt.getPartialTicks();
                double doubleY = player.lastTickPosY + (player.posY - player.lastTickPosY) * evt.getPartialTicks();
                double doubleZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * evt.getPartialTicks();

                //Save the current position that is being rendered (I think)
                GlStateManager.pushMatrix();
                //Enable Blending (So we can have transparent effect)
                GlStateManager.enableBlend();
                //This blend function allows you to use a constant alpha, which is defined later
                GlStateManager.blendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);

                //List<BlockPos> sortedCoordinates = ExchangingModes.sortByDistance(coordinates, player); //Sort the coords by distance to player.

                for (BlockPos coordinate : coordinates) {
                    GlStateManager.pushMatrix();//Push matrix again just because
                    GlStateManager.translate(-doubleX, -doubleY, -doubleZ);//The render starts at the player, so we subtract the player coords and move the render to 0,0,0
                    GlStateManager.translate(coordinate.getX(), coordinate.getY(), coordinate.getZ());//Now move the render position to the coordinates we want to render at
                    GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
                    GlStateManager.translate(-0.005f, -0.005f, 0.005f);
                    GlStateManager.scale(1.01f, 1.01f, 1.01f);//Slightly Larger block to avoid z-fighting.
                    GL14.glBlendColor(1F, 1F, 1F, 0.55f); //Set the alpha of the blocks we are rendering
                    if (fakeWorld.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) { //Get the block state in the fake world
                        try {
                            state = renderBlockState.getActualState(fakeWorld, coordinate);
                        } catch (Exception var8) {
                        }
                    }
                    //state = state.getBlock().getExtendedState(state, fakeWorld, coordinate); //Get the extended block state in the fake world (Disabled to fix chisel, not sure why.)
                    if (renderBlockState.getRenderType() != EnumBlockRenderType.INVISIBLE) {
                        try {
                            dispatcher.renderBlockBrightness(state, 1f);//Render the defined block
                        } catch (Throwable t) {
                            Tessellator tessellator = Tessellator.getInstance();
                            BufferBuilder bufferBuilder = tessellator.getBuffer();
                            bufferBuilder.finishDrawing();

                        }
                        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
                    }
                    GL14.glBlendColor(1F, 1F, 1F, 0.1f); //Set the alpha of the blocks we are rendering
                    //GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
                    dispatcher.renderBlockBrightness(Blocks.STAINED_GLASS.getDefaultState().withProperty(COLOR, EnumDyeColor.WHITE), 1f);//Render the defined block - White glass to show non-full block renders (Example: Torch)
                    //Move the render position back to where it was
                    GlStateManager.popMatrix();
                }

                for (BlockPos coordinate : coordinates) { //Now run through the UNSORTED list of coords, to show which blocks won't place if you don't have enough of them.
                    GlStateManager.pushMatrix();//Push matrix again just because
                    GlStateManager.translate(-doubleX, -doubleY, -doubleZ);//The render starts at the player, so we subtract the player coords and move the render to 0,0,0
                    GlStateManager.translate(coordinate.getX(), coordinate.getY(), coordinate.getZ());//Now move the render position to the coordinates we want to render at
                    GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
                    GlStateManager.translate(-0.01f, -0.01f, 0.01f);
                    GlStateManager.scale(1.02f, 1.02f, 1.02f);//Slightly Larger block to avoid z-fighting.
                    GL14.glBlendColor(1F, 1F, 1F, 0.55f); //Set the alpha of the blocks we are rendering
                    hasBlocks--;
                    if (SyncedConfig.poweredByFE) {
                        hasEnergy = hasEnergy - ModItems.gadgetExchanger.getEnergyCost();
                    } else {
                        hasEnergy = hasEnergy - 2;
                    }
                    if (hasBlocks < 0 || hasEnergy < 0) {
                        dispatcher.renderBlockBrightness(Blocks.STAINED_GLASS.getDefaultState().withProperty(COLOR, EnumDyeColor.RED), 1f);
                    }
                    //Move the render position back to where it was
                    GlStateManager.popMatrix();
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
    }

    public static void renderDestructionOverlay(RenderWorldLastEvent evt, EntityPlayer player, ItemStack stack) {
        RayTraceResult lookingAt = VectorTools.getLookingAt(player);
        if (lookingAt == null && GadgetDestruction.getAnchor(stack) == null) return;
        World world = player.world;
        BlockPos startBlock = (GadgetDestruction.getAnchor(stack) == null) ? lookingAt.getBlockPos() : GadgetDestruction.getAnchor(stack);
        EnumFacing facing = (GadgetDestruction.getAnchorSide(stack) == null) ? lookingAt.sideHit : GadgetDestruction.getAnchorSide(stack);
        if (startBlock == ModBlocks.effectBlock.getDefaultState()) return;

        ItemStack heldItem = GadgetDestruction.getGadget(player);
        if (heldItem.isEmpty()) return;

        if (!GadgetDestruction.getOverlay(heldItem)) return;
        Minecraft mc = Minecraft.getMinecraft();
        mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        ArrayList<BlockPos> coordinates = GadgetDestruction.getArea(world, startBlock, facing, player, heldItem);

        //Prepare the block rendering
        BlockRenderLayer origLayer = MinecraftForgeClient.getRenderLayer();

        //Calculate the players current position, which is needed later
        double doubleX = player.lastTickPosX + (player.posX - player.lastTickPosX) * evt.getPartialTicks();
        double doubleY = player.lastTickPosY + (player.posY - player.lastTickPosY) * evt.getPartialTicks();
        double doubleZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * evt.getPartialTicks();

        //Save the current position that is being rendered (I think)
        GlStateManager.pushMatrix();
        //Enable Blending (So we can have transparent effect)
        GlStateManager.enableBlend();
        //This blend function allows you to use a constant alpha, which is defined later
        //GlStateManager.blendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        List<BlockPos> sortedCoordinates = BuildingModes.sortByDistance(coordinates, player); //Sort the coords by distance to player.

        Tessellator t = Tessellator.getInstance();
        BufferBuilder bufferBuilder = t.getBuffer();
        /*ArrayList<EnumFacing> directions = GadgetDestruction.assignDirections(facing, player);
        BlockPos a = new BlockPos(0,0,0);
        a = a.offset(directions.get(0), GadgetDestruction.getToolValue(stack, "left"));
        a = a.offset(directions.get(2), GadgetDestruction.getToolValue(stack, "up"));
        BlockPos b = new BlockPos(0,0,0);
        b = b.offset(directions.get(1), GadgetDestruction.getToolValue(stack, "right"));
        b = b.offset(directions.get(2), GadgetDestruction.getToolValue(stack, "down"));
        b = b.offset(directions.get(4), GadgetDestruction.getToolValue(stack, "depth"));*/

        for (BlockPos coordinate : sortedCoordinates) {
            boolean invisible = true;
            IBlockState state = world.getBlockState(coordinate);
            for (EnumFacing side : EnumFacing.values()) {
                if (state.shouldSideBeRendered(world, coordinate, side)) {
                    invisible = false;
                    break;
                }
            }
            if (invisible) continue;
            GlStateManager.pushMatrix();//Push matrix again just because
            GlStateManager.translate(-doubleX, -doubleY, -doubleZ);//The render starts at the player, so we subtract the player coords and move the render to 0,0,0
            GlStateManager.translate(coordinate.getX(), coordinate.getY(), coordinate.getZ());//Now move the render position to the coordinates we want to render at
            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
            GlStateManager.translate(-0.005f, -0.005f, 0.005f);
            GlStateManager.scale(1.01f, 1.01f, 1.01f);//Slightly Larger block to avoid z-fighting.
            //GL14.glBlendColor(1F, 1F, 1F, 0.55f); //Set the alpha of the blocks we are rendering
            //GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
            //GlStateManager.disableCull();
            GlStateManager.disableLighting();
            GlStateManager.disableTexture2D();
            //renderBoxSolid(t, bufferBuilder, a.getX(), a.getY(), a.getZ()+1, b.getX()+1, b.getY()+1, b.getZ()+1, 1, 1, 1);
            renderBoxSolid(t, bufferBuilder, 0, 0, -1, 1, 1, 0, 1, 0, 0, 0.5f);
            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
            //GlStateManager.enableCull();
            //dispatcher.renderBlockBrightness(Blocks.STAINED_GLASS.getDefaultState().withProperty(COLOR, EnumDyeColor.RED), 1f);//Render the defined block - White glass to show non-full block renders (Example: Torch)
            //Move the render position back to where it was
            GlStateManager.popMatrix();
        }
        //Set blending back to the default mode
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        ForgeHooksClient.setRenderLayer(origLayer);
        //Disable blend
        GlStateManager.disableBlend();
        //Pop from the original push in this method
        GlStateManager.popMatrix();
    }

    public static void renderPasteOverlay(RenderWorldLastEvent evt, EntityPlayer player, ItemStack stack) {
        String UUID = ModItems.gadgetCopyPaste.getUUID(stack);
        World world = player.world;
        if (ModItems.gadgetCopyPaste.getStartPos(stack) == null) return;
        if (ModItems.gadgetCopyPaste.getEndPos(stack) == null) return;
        if (GadgetCopyPaste.getToolMode(stack) == GadgetCopyPaste.ToolMode.Paste) {
            //First check if we have an anchor, if not check if we're looking at a block, if not, exit
            BlockPos startPos = GadgetCopyPaste.getAnchor(stack);
            if (startPos == null) {
                startPos = VectorTools.getPosLookingAt(player);
                if (startPos == null) return;
                startPos = startPos.up(GadgetCopyPaste.getY(stack));
                startPos = startPos.east(GadgetCopyPaste.getX(stack));
                startPos = startPos.south(GadgetCopyPaste.getZ(stack));
            } else {
                startPos = startPos.up(GadgetCopyPaste.getY(stack));
                startPos = startPos.east(GadgetCopyPaste.getX(stack));
                startPos = startPos.south(GadgetCopyPaste.getZ(stack));
            }

            //We store our buffers in PasteToolBufferBuilder (A client only class) -- retrieve the buffer from this locally cache'd map
            ToolDireBuffer toolDireBuffer = PasteToolBufferBuilder.getBufferFromMap(UUID);
            if (toolDireBuffer == null) {
                return;
            }
            //Also get the blockMapList from the local cache - If either the buffer or the blockmap list are empty, exit.
            List<BlockMap> blockMapList = GadgetCopyPaste.getBlockMapList(PasteToolBufferBuilder.getTagFromUUID(UUID));
            if (toolDireBuffer.getVertexCount() == 0 || blockMapList.size() == 0) {
                return;
            }

            //Don't draw on top of blocks being built by our tools.
            IBlockState startBlock = world.getBlockState(startPos);
            if (startBlock == ModBlocks.effectBlock.getDefaultState()) return;

            Minecraft mc = Minecraft.getMinecraft();
            mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

            //Prepare the block rendering
            //BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

            //Calculate the players current position, which is needed later
            double doubleX = player.lastTickPosX + (player.posX - player.lastTickPosX) * evt.getPartialTicks();
            double doubleY = player.lastTickPosY + (player.posY - player.lastTickPosY) * evt.getPartialTicks();
            double doubleZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * evt.getPartialTicks();

            //Save the current position that is being rendered
            GlStateManager.pushMatrix();

            //Enable Blending (So we can have transparent effect)
            GlStateManager.enableBlend();
            //This blend function allows you to use a constant alpha, which is defined later
            GlStateManager.blendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);

            GlStateManager.pushMatrix();//Push matrix again just because
            GlStateManager.translate(-doubleX, -doubleY, -doubleZ);//The render starts at the player, so we subtract the player coords and move the render to 0,0,0
            GlStateManager.translate(startPos.getX(), startPos.getY(), startPos.getZ()); //Move the render to the startingBlockPos
            GL14.glBlendColor(1F, 1F, 1F, 0.55f); //Set the alpha of the blocks we are rendering
            //GlStateManager.translate(-0.0005f, -0.0005f, 0.0005f);
            //GlStateManager.scale(1.001f, 1.001f, 1.001f);//Slightly Larger block to avoid z-fighting.
            GlStateManager.translate(0.0005f, 0.0005f, -0.0005f);
            GlStateManager.scale(0.999f, 0.999f, 0.999f);//Slightly Larger block to avoid z-fighting.
            //GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
            PasteToolBufferBuilder.draw(player, doubleX, doubleY, doubleZ, startPos, UUID); //Draw the cached buffer in the world.

            GlStateManager.popMatrix();
            //Set blending back to the default mode
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            //Disable blend
            GlStateManager.disableBlend();
            //Pop from the original push in this method
            GlStateManager.popMatrix();

        } else {
            BlockPos startPos = ModItems.gadgetCopyPaste.getStartPos(stack);
            BlockPos endPos = ModItems.gadgetCopyPaste.getEndPos(stack);
            BlockPos blankPos = new BlockPos(0, 0, 0);
            if (startPos == null || endPos == null || startPos.equals(blankPos) || endPos.equals(blankPos)) {
                return;
            }

            List<BlockMap> blockMapList = GadgetCopyPaste.getBlockMapList(PasteToolBufferBuilder.getTagFromUUID(UUID));
            if (blockMapList.size() == 0) {
                //return;
            }

            Minecraft mc = Minecraft.getMinecraft();
            mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

            //Calculate the players current position, which is needed later
            double doubleX = player.lastTickPosX + (player.posX - player.lastTickPosX) * evt.getPartialTicks();
            double doubleY = player.lastTickPosY + (player.posY - player.lastTickPosY) * evt.getPartialTicks();
            double doubleZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * evt.getPartialTicks();

            //We want to draw from the starting position to the (ending position)+1
            int x = (startPos.getX() <= endPos.getX()) ? startPos.getX() : endPos.getX();
            int y = (startPos.getY() <= endPos.getY()) ? startPos.getY() : endPos.getY();
            int z = (startPos.getZ() <= endPos.getZ()) ? startPos.getZ() : endPos.getZ();
            int dx = (startPos.getX() > endPos.getX()) ? startPos.getX() + 1 : endPos.getX() + 1;
            int dy = (startPos.getY() > endPos.getY()) ? startPos.getY() + 1 : endPos.getY() + 1;
            int dz = (startPos.getZ() > endPos.getZ()) ? startPos.getZ() + 1 : endPos.getZ() + 1;

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            GlStateManager.pushMatrix();
            GlStateManager.translate(-doubleX, -doubleY, -doubleZ);//The render starts at the player, so we subtract the player coords and move the render to 0,0,0

            GlStateManager.disableLighting();
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            renderBox(tessellator, bufferbuilder, x, y, z, dx, dy, dz, 255, 223, 127); // Draw the box around the blocks we've copied.

            GlStateManager.glLineWidth(1.0F);
            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);

            GlStateManager.popMatrix();
        }
    }

    private static void renderBox(Tessellator tessellator, BufferBuilder bufferBuilder, double startX, double startY, double startZ, double endX, double endY, double endZ, int R, int G, int B) {
        GlStateManager.glLineWidth(2.0F);
        bufferBuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
        bufferBuilder.pos(startX, startY, startZ).color(G, G, G, 0.0F).endVertex();
        bufferBuilder.pos(startX, startY, startZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(endX, startY, startZ).color(G, B, B, R).endVertex();
        bufferBuilder.pos(endX, startY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(startX, startY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(startX, startY, startZ).color(B, B, G, R).endVertex();
        bufferBuilder.pos(startX, endY, startZ).color(B, G, B, R).endVertex();
        bufferBuilder.pos(endX, endY, startZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(endX, endY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(startX, endY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(startX, endY, startZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(startX, endY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(startX, startY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(endX, startY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(endX, endY, endZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(endX, endY, startZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(endX, startY, startZ).color(G, G, G, R).endVertex();
        bufferBuilder.pos(endX, startY, startZ).color(G, G, G, 0.0F).endVertex();
        tessellator.draw();
        GlStateManager.glLineWidth(1.0F);
    }

    private static void renderBoxSolid(Tessellator tessellator, BufferBuilder bufferBuilder, double startX, double startY, double startZ, double endX, double endY, double endZ, float red, float green, float blue, float alpha) {
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        double minX = startX;
        double minY = startY;
        double minZ = startZ;
        double maxX = endX;
        double maxY = endY;
        double maxZ = endZ;

        //down
        bufferBuilder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();

        //up
        bufferBuilder.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();

        //east
        bufferBuilder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();

        //west
        bufferBuilder.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();

        //south
        bufferBuilder.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
        
        //north
        bufferBuilder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
    }

}
