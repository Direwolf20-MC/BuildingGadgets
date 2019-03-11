package com.direwolf20.buildinggadgets.common.tools;

import com.direwolf20.buildinggadgets.client.RemoteInventoryCache;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.items.gadgets.*;
import com.direwolf20.buildinggadgets.common.registry.objects.BGBlocks;
import com.direwolf20.buildinggadgets.common.registry.objects.BGItems;
import com.direwolf20.buildinggadgets.common.tools.modes.BuildingModes;
import com.direwolf20.buildinggadgets.common.tools.modes.ExchangingModes;
import com.direwolf20.buildinggadgets.common.utils.CapabilityUtil.EnergyUtil;
import com.direwolf20.buildinggadgets.common.utils.GadgetUtils;
import com.direwolf20.buildinggadgets.common.utils.blocks.BlockMap;
import com.direwolf20.buildinggadgets.common.utils.buffers.PasteToolBufferBuilder;
import com.direwolf20.buildinggadgets.common.utils.buffers.ToolBufferBuilder;
import com.direwolf20.buildinggadgets.common.utils.exceptions.CapabilityNotPresentException;
import com.direwolf20.buildinggadgets.common.utils.helpers.InventoryHelper;
import com.direwolf20.buildinggadgets.common.utils.helpers.SortingHelper;
import com.direwolf20.buildinggadgets.common.utils.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.utils.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.world.FakeBuilderWorld;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Multiset;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.direwolf20.buildinggadgets.common.utils.GadgetUtils.getAnchor;
import static com.direwolf20.buildinggadgets.common.utils.GadgetUtils.getToolBlock;

public class ToolRenders {
    private static final FakeBuilderWorld fakeWorld = new FakeBuilderWorld();
    private static RemoteInventoryCache cacheInventory = new RemoteInventoryCache(false);
    private static Cache<Triple<UniqueItemStack, BlockPos, Integer>, Integer> cacheDestructionOverlay = CacheBuilder.newBuilder().maximumSize(1).
            expireAfterWrite(1, TimeUnit.SECONDS).removalListener(removal -> GLAllocation.deleteDisplayLists((int) removal.getValue())).build();

    public static void setInventoryCache(Multiset<UniqueItem> cache) {
        ToolRenders.cacheInventory.setCache(cache);
    }

    public static void updateInventoryCache() {
        cacheInventory.forceUpdate();
    }

    public static void renderBuilderOverlay(RenderWorldLastEvent evt, EntityPlayer player, ItemStack stack) {
        ItemStack heldItem = GadgetBuilding.getGadget(player);
        if (heldItem.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        //Calculate the players current position, which is needed later
        double doubleX = player.lastTickPosX + (player.posX - player.lastTickPosX) * evt.getPartialTicks();
        double doubleY = player.lastTickPosY + (player.posY - player.lastTickPosY) * evt.getPartialTicks();
        double doubleZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * evt.getPartialTicks();

        //Prepare the block rendering
        BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
        BlockRenderLayer origLayer = MinecraftForgeClient.getRenderLayer();

        ResourceLocation dim = GadgetUtils.getDIMFromNBT(heldItem, NBTKeys.REMOTE_INVENTORY_DIM);
        BlockPos pos = GadgetUtils.getPOSFromNBT(heldItem, NBTKeys.REMOTE_INVENTORY_POS);

        if (dim != null && pos != null) {
            GlStateManager.pushMatrix();//Push matrix again just because
            GlStateManager.enableBlend();
            //This blend function allows you to use a constant alpha, which is defined later
            GlStateManager.blendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);
            GlStateManager.translated(-doubleX, -doubleY, -doubleZ);//The render starts at the player, so we subtract the player coords and move the render to 0,0,0
            GlStateManager.translatef(pos.getX(), pos.getY(), pos.getZ());//Now move the render position to the coordinates we want to render at
            GlStateManager.rotatef(-90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
            GlStateManager.translatef(-0.005f, -0.005f, 0.005f);
            GlStateManager.scalef(1.01f, 1.01f, 1.01f);
            GL14.glBlendColor(1F, 1F, 1F, 0.35f); //Set the alpha of the blocks we are rendering
            dispatcher.renderBlockBrightness(Blocks.YELLOW_STAINED_GLASS.getDefaultState(), 1f);
            GlStateManager.popMatrix();
        }

        RayTraceResult lookingAt = VectorHelper.getLookingAt(player, heldItem);
        IBlockState state = Blocks.AIR.getDefaultState();
        List<BlockPos> coordinates = getAnchor(stack);
        if (lookingAt != null || coordinates.size() > 0) {
            World world = player.world;
            IBlockState startBlock = Blocks.AIR.getDefaultState();
            if (!(lookingAt == null)) {
                startBlock = world.getBlockState(lookingAt.getBlockPos());
            }
            if (startBlock != BGBlocks.effectBlock.getDefaultState()) {

                IBlockState renderBlockState = getToolBlock(heldItem);
                if (renderBlockState == Blocks.AIR.getDefaultState()) {//Don't render anything if there is no block selected (Air)
                    return;
                }
                if (coordinates.size() == 0 && lookingAt != null) { //Build a list of coordinates based on the tool mode and range
                    coordinates = BuildingModes.getBuildOrders(world, player, lookingAt.getBlockPos(), lookingAt.sideHit, heldItem);
                }

                //Figure out how many of the block we're rendering we have in the inventory of the player.
                ItemStack itemStack;
                if (renderBlockState.getBlock().canSilkHarvest(renderBlockState, world, new BlockPos(0, 0, 0), player)) {
                    itemStack = InventoryHelper.getSilkTouchDrop(renderBlockState);
                } else {
                    itemStack = renderBlockState.getBlock().getPickBlock(renderBlockState, null, world, new BlockPos(0, 0, 0), player);
                }
                if (itemStack.getItem().equals(Items.AIR)) {
                    itemStack = renderBlockState.getBlock().getPickBlock(renderBlockState, null, world, new BlockPos(0, 0, 0), player);
                }

                long hasBlocks = InventoryHelper.countItem(itemStack, player, cacheInventory);
                hasBlocks = hasBlocks + InventoryHelper.countPaste(player);
                int hasEnergy = 0;
                LazyOptional<IEnergyStorage> energy = EnergyUtil.getCap(stack);
                if (energy.isPresent()) {
                    hasEnergy = energy.orElseThrow(CapabilityNotPresentException::new).getEnergyStored();
                } else {
                    hasEnergy = stack.getMaxDamage() - stack.getDamage();
                }
                if (player.isCreative() || (energy.isPresent() && !stack.isDamageable())) {
                    hasEnergy = 1000000;
                }

                //Prepare the fake world -- using a fake world lets us render things properly, like fences connecting.
                Set<BlockPos> coords = new HashSet<BlockPos>(coordinates);
                fakeWorld.setWorldAndState(player.world, renderBlockState, coords);

                //Save the current position that is being rendered (I think)
                GlStateManager.pushMatrix();
                //Enable Blending (So we can have transparent effect)
                GlStateManager.enableBlend();
                //This blend function allows you to use a constant alpha, which is defined later
                GlStateManager.blendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);

                List<BlockPos> sortedCoordinates = SortingHelper.Blocks.byDistance(coordinates, player); //Sort the coords by distance to player.

                for (BlockPos coordinate : sortedCoordinates) {
                    GlStateManager.pushMatrix();//Push matrix again just because
                    GlStateManager.translatef((float) -doubleX, (float) -doubleY, (float) -doubleZ);//The render starts at the player, so we subtract the player coords and move the render to 0,0,0
                    GlStateManager.translatef(coordinate.getX(), coordinate.getY(), coordinate.getZ());//Now move the render position to the coordinates we want to render at
                    GlStateManager.rotatef(-90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
                    GlStateManager.scalef(1.0f, 1.0f, 1.0f); //Block scale 1 = full sized block
                    GL14.glBlendColor(1F, 1F, 1F, 0.55f); //Set the alpha of the blocks we are rendering
                    if (fakeWorld.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) { //Get the block state in the fake world
                        try {
                            state = renderBlockState;
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
                    GlStateManager.translatef((float) -doubleX, (float) -doubleY, (float) -doubleZ);//The render starts at the player, so we subtract the player coords and move the render to 0,0,0
                    GlStateManager.translatef(coordinate.getX(), coordinate.getY(), coordinate.getZ());//Now move the render position to the coordinates we want to render at
                    GlStateManager.rotatef(-90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
                    GlStateManager.translatef(-0.005f, -0.005f, 0.005f);
                    GlStateManager.scalef(1.01f, 1.01f, 1.01f);
                    GL14.glBlendColor(1F, 1F, 1F, 0.35f); //Set the alpha of the blocks we are rendering
                    hasBlocks--;
                    if (energy.isPresent()) {
                        hasEnergy -= ((GadgetGeneric) stack.getItem()).getEnergyCost(heldItem);
                    } else {
                        hasEnergy -= ((GadgetGeneric) stack.getItem()).getDamageCost(heldItem);
                    }
                    if (hasBlocks < 0 || hasEnergy < 0) {
                        dispatcher.renderBlockBrightness(Blocks.RED_STAINED_GLASS.getDefaultState(), 1f);
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
        //Calculate the players current position, which is needed later
        double doubleX = player.lastTickPosX + (player.posX - player.lastTickPosX) * evt.getPartialTicks();
        double doubleY = player.lastTickPosY + (player.posY - player.lastTickPosY) * evt.getPartialTicks();
        double doubleZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * evt.getPartialTicks();

        BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
        ResourceLocation dim = GadgetUtils.getDIMFromNBT(stack, NBTKeys.REMOTE_INVENTORY_DIM);
        BlockPos pos = GadgetUtils.getPOSFromNBT(stack, NBTKeys.REMOTE_INVENTORY_POS);

        if (dim != null && pos != null) {
            GlStateManager.pushMatrix();//Push matrix again just because
            //This blend function allows you to use a constant alpha, which is defined later
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);

            GlStateManager.translated(-doubleX, -doubleY, -doubleZ);//The render starts at the player, so we subtract the player coords and move the render to 0,0,0
            GlStateManager.translatef(pos.getX(), pos.getY(), pos.getZ());//Now move the render position to the coordinates we want to render at
            GlStateManager.rotatef(-90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
            GlStateManager.translatef(-0.005f, -0.005f, 0.005f);
            GlStateManager.scalef(1.01f, 1.01f, 1.01f);
            GL14.glBlendColor(1F, 1F, 1F, 0.35f); //Set the alpha of the blocks we are rendering
            dispatcher.renderBlockBrightness(Blocks.YELLOW_STAINED_GLASS.getDefaultState(), 1f);
            GlStateManager.popMatrix();
        }

        RayTraceResult lookingAt = VectorHelper.getLookingAt(player, stack);
        IBlockState state = Blocks.AIR.getDefaultState();
        List<BlockPos> coordinates = getAnchor(stack);
        if (lookingAt != null || coordinates.size() > 0) {
            World world = player.world;
            IBlockState startBlock = Blocks.AIR.getDefaultState();
            if (!(lookingAt == null)) {
                startBlock = world.getBlockState(lookingAt.getBlockPos());
            }
            if (startBlock != BGBlocks.effectBlock.getDefaultState()) {
                IBlockState renderBlockState = getToolBlock(stack);
                Minecraft mc = Minecraft.getInstance();
                mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                if (renderBlockState == Blocks.AIR.getDefaultState()) {//Don't render anything if there is no block selected (Air)
                    return;
                }
                if (coordinates.size() == 0 && lookingAt != null) { //Build a list of coordinates based on the tool mode and range
                    coordinates = ExchangingModes.getBuildOrders(world, player, lookingAt.getBlockPos(), lookingAt.sideHit, stack);
                }

                //Figure out how many of the block we're rendering we have in the inventory of the player.
                //ItemStack itemStack = renderBlockState.getBlock().getPickBlock(renderBlockState, null, world, new BlockPos(0, 0, 0), player);
                //ItemStack itemStack = InventoryHelper.getSilkTouchDrop(renderBlockState);
                ItemStack itemStack;
                if (renderBlockState.getBlock().canSilkHarvest(renderBlockState, world, new BlockPos(0, 0, 0), player)) {
                    itemStack = InventoryHelper.getSilkTouchDrop(renderBlockState);
                } else {
                    itemStack = renderBlockState.getBlock().getPickBlock(renderBlockState, null, world, new BlockPos(0, 0, 0), player);
                }
                if (itemStack.getItem().equals(Items.AIR)) {
                    itemStack = renderBlockState.getBlock().getPickBlock(renderBlockState, null, world, new BlockPos(0, 0, 0), player);
                }
                long hasBlocks = InventoryHelper.countItem(itemStack, player, cacheInventory);
                hasBlocks = hasBlocks + InventoryHelper.countPaste(player);
                int hasEnergy = 0;

                LazyOptional<IEnergyStorage> energy = EnergyUtil.getCap(stack);
                if (energy.isPresent()) {
                    hasEnergy = energy.orElseThrow(CapabilityNotPresentException::new).getEnergyStored();
                } else {
                    hasEnergy = stack.getMaxDamage() - stack.getDamage();
                }
                if (player.isCreative() || (energy.isPresent() && !stack.isDamageable())) {
                    hasEnergy = 1000000;
                }
                //Prepare the block rendering
                BlockRenderLayer origLayer = MinecraftForgeClient.getRenderLayer();

                //Prepare the fake world -- using a fake world lets us render things properly, like fences connecting.
                Set<BlockPos> coords = new HashSet<BlockPos>(coordinates);
                fakeWorld.setWorldAndState(player.world, renderBlockState, coords);

                //Save the current position that is being rendered (I think)
                GlStateManager.pushMatrix();
                //Enable Blending (So we can have transparent effect)
                GlStateManager.enableBlend();
                //This blend function allows you to use a constant alpha, which is defined later
                GlStateManager.blendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);

                //List<BlockPos> sortedCoordinates = ExchangingModes.sortByDistance(coordinates, player); //Sort the coords by distance to player.

                for (BlockPos coordinate : coordinates) {
                    GlStateManager.pushMatrix();//Push matrix again just because
                    GlStateManager.translatef((float) -doubleX, (float) -doubleY, (float) -doubleZ);//The render starts at the player, so we subtract the player coords and move the render to 0,0,0
                    GlStateManager.translatef(coordinate.getX(), coordinate.getY(), coordinate.getZ());//Now move the render position to the coordinates we want to render at
                    GlStateManager.rotatef(-90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
                    GlStateManager.translatef(-0.005f, -0.005f, 0.005f);
                    GlStateManager.scalef(1.01f, 1.01f, 1.01f);//Slightly Larger block to avoid z-fighting.
                    GL14.glBlendColor(1F, 1F, 1F, 0.55f); //Set the alpha of the blocks we are rendering
                    if (fakeWorld.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) { //Get the block state in the fake world
                        try {
                            state = renderBlockState;
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
                        GlStateManager.rotatef(-90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
                    }
                    GL14.glBlendColor(1F, 1F, 1F, 0.1f); //Set the alpha of the blocks we are rendering
                    //GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
                    dispatcher.renderBlockBrightness(Blocks.WHITE_STAINED_GLASS.getDefaultState(), 1f);//Render the defined block - White glass to show non-full block renders (Example: Torch)
                    //Move the render position back to where it was
                    GlStateManager.popMatrix();
                }

                for (BlockPos coordinate : coordinates) { //Now run through the UNSORTED list of coords, to show which blocks won't place if you don't have enough of them.
                    GlStateManager.pushMatrix();//Push matrix again just because
                    GlStateManager.translatef((float) -doubleX, (float) -doubleY, (float) -doubleZ);//The render starts at the player, so we subtract the player coords and move the render to 0,0,0
                    GlStateManager.translatef(coordinate.getX(), coordinate.getY(), coordinate.getZ());//Now move the render position to the coordinates we want to render at
                    GlStateManager.rotatef(-90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
                    GlStateManager.translatef(-0.01f, -0.01f, 0.01f);
                    GlStateManager.scalef(1.02f, 1.02f, 1.02f);//Slightly Larger block to avoid z-fighting.
                    GL14.glBlendColor(1F, 1F, 1F, 0.55f); //Set the alpha of the blocks we are rendering
                    hasBlocks--;
                    if (energy.isPresent()) {
                        hasEnergy -= (((GadgetGeneric) stack.getItem())).getEnergyCost(stack);
                    } else {
                        hasEnergy -= (((GadgetGeneric) stack.getItem())).getDamageCost(stack);
                    }
                    if (hasBlocks < 0 || hasEnergy < 0) {
                        dispatcher.renderBlockBrightness(Blocks.RED_STAINED_GLASS.getDefaultState(), 1f);
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
        RayTraceResult lookingAt = VectorHelper.getLookingAt(player, stack);
        if (lookingAt == null && GadgetDestruction.getAnchor(stack) == null) return;
        World world = player.world;
        BlockPos startBlock = (GadgetDestruction.getAnchor(stack) == null) ? lookingAt.getBlockPos() : GadgetDestruction.getAnchor(stack);
        EnumFacing facing = (GadgetDestruction.getAnchorSide(stack) == null) ? lookingAt.sideHit : GadgetDestruction.getAnchorSide(stack);
        if (startBlock == BGBlocks.effectBlock.getDefaultState()) return;

        if (!GadgetDestruction.getOverlay(stack)) return;
        GlStateManager.pushMatrix();
        double doubleX = player.lastTickPosX + (player.posX - player.lastTickPosX) * evt.getPartialTicks();
        double doubleY = player.lastTickPosY + (player.posY - player.lastTickPosY) * evt.getPartialTicks();
        double doubleZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * evt.getPartialTicks();
        GlStateManager.translated(-doubleX, -doubleY, -doubleZ);
        try {
            GlStateManager.callList(cacheDestructionOverlay.get(new ImmutableTriple<UniqueItemStack, BlockPos, Integer>(new UniqueItemStack(stack), startBlock, facing.ordinal()), () -> {
                int displayList = GLAllocation.generateDisplayLists(1);
                GlStateManager.newList(displayList, GL11.GL_COMPILE);
                renderDestructionOverlay(player, world, startBlock, facing, stack);
                GlStateManager.endList();
                return displayList;
            }));
        } catch (ExecutionException e) {
            BuildingGadgets.LOG.error("Error encountered while rendering destruction gadget overlay", e);
        }
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private static void renderDestructionOverlay(EntityPlayer player, World world, BlockPos startBlock, EnumFacing facing, ItemStack heldItem) {
        Minecraft mc = Minecraft.getInstance();
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        SortedSet<BlockPos> coordinates = GadgetDestruction.getArea(world, startBlock, facing, player, heldItem);

        //Prepare the block rendering
        BlockRenderLayer origLayer = MinecraftForgeClient.getRenderLayer();

        //Save the current position that is being rendered (I think)
        GlStateManager.pushMatrix();
        //Enable Blending (So we can have transparent effect)
        GlStateManager.enableBlend();
        //This blend function allows you to use a constant alpha, which is defined later
        //GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        List<BlockPos> sortedCoordinates = SortingHelper.Blocks.byDistance(coordinates, player); //Sort the coords by distance to player.

        Tessellator t = Tessellator.getInstance();
        BufferBuilder bufferBuilder = t.getBuffer();
        /*ArrayList<EnumFacing> directions = GadgetDestruction.assignDirections(facing, player);
        BlockPos a = new BlockPos(0,0,0);
        a = a.offset(directions.get(0), GadgetDestruction.getToolValue(stack, NBTKeys.GADGET_VALUE_LEFT));
        a = a.offset(directions.get(2), GadgetDestruction.getToolValue(stack, NBTKeys.GADGET_VALUE_UP));
        BlockPos b = new BlockPos(0,0,0);
        b = b.offset(directions.get(1), GadgetDestruction.getToolValue(stack, NBTKeys.GADGET_VALUE_RIGHT));
        b = b.offset(directions.get(2), GadgetDestruction.getToolValue(stack, NBTKeys.GADGET_VALUE_DOWN));
        b = b.offset(directions.get(4), GadgetDestruction.getToolValue(stack, NBTKeys.GADGET_VALUE_DEPTH));*/

        for (BlockPos coordinate : sortedCoordinates) {
            boolean invisible = true;
            IBlockState state = world.getBlockState(coordinate);
            for (EnumFacing side : EnumFacing.values()) {
                if (!state.isSideInvisible(state, side)) {
                    invisible = false;
                    break;
                }
            }
            if (invisible) continue;
            GlStateManager.pushMatrix();//Push matrix again just because
            GlStateManager.translatef(coordinate.getX(), coordinate.getY(), coordinate.getZ());//Now move the render position to the coordinates we want to render at
            GlStateManager.rotatef(-90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
            GlStateManager.translatef(-0.005f, -0.005f, 0.005f);
            GlStateManager.scalef(1.01f, 1.01f, 1.01f);//Slightly Larger block to avoid z-fighting.
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
        ResourceLocation dim = GadgetUtils.getDIMFromNBT(stack, NBTKeys.REMOTE_INVENTORY_DIM);
        BlockPos pos = GadgetUtils.getPOSFromNBT(stack, NBTKeys.REMOTE_INVENTORY_POS);

        //Calculate the players current position, which is needed later
        double doubleX = player.lastTickPosX + (player.posX - player.lastTickPosX) * evt.getPartialTicks();
        double doubleY = player.lastTickPosY + (player.posY - player.lastTickPosY) * evt.getPartialTicks();
        double doubleZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * evt.getPartialTicks();

        Minecraft mc = Minecraft.getInstance();
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        if (dim != null && pos != null) {
            BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
            GlStateManager.enableBlend();
            //This blend function allows you to use a constant alpha, which is defined later
            GlStateManager.blendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);
            GlStateManager.pushMatrix();//Push matrix again just because
            GlStateManager.translated(-doubleX, -doubleY, -doubleZ);//The render starts at the player, so we subtract the player coords and move the render to 0,0,0
            GlStateManager.translatef(pos.getX(), pos.getY(), pos.getZ());//Now move the render position to the coordinates we want to render at
            GlStateManager.rotatef(-90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
            GlStateManager.translatef(-0.005f, -0.005f, 0.005f);
            GlStateManager.scalef(1.01f, 1.01f, 1.01f);
            GL14.glBlendColor(1F, 1F, 1F, 0.35f); //Set the alpha of the blocks we are rendering
            dispatcher.renderBlockBrightness(Blocks.YELLOW_STAINED_GLASS.getDefaultState(), 1f);
            GlStateManager.popMatrix();
        }

        String UUID = BGItems.gadgetCopyPaste.getUUID(stack);
        World world = player.world;
        if (BGItems.gadgetCopyPaste.getStartPos(stack) == null) return;
        if (BGItems.gadgetCopyPaste.getEndPos(stack) == null) return;
        if (GadgetCopyPaste.getToolMode(stack) == GadgetCopyPaste.ToolMode.Paste) {
            //First check if we have an anchor, if not check if we're looking at a block, if not, exit
            BlockPos startPos = GadgetCopyPaste.getAnchor(stack);
            if (startPos == null) {
                startPos = VectorHelper.getPosLookingAt(player, stack);
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
            ToolBufferBuilder toolBufferBuilder = PasteToolBufferBuilder.getBufferFromMap(UUID);
            if (toolBufferBuilder == null) {
                return;
            }
            //Also get the blockMapList from the local cache - If either the buffer or the blockmap list are empty, exit.
            List<BlockMap> blockMapList = GadgetCopyPaste.getBlockMapList(PasteToolBufferBuilder.getTagFromUUID(UUID));
            if (toolBufferBuilder.getVertexCount() == 0 || blockMapList.size() == 0) {
                return;
            }

            //Don't draw on top of blocks being built by our tools.
            IBlockState startBlock = world.getBlockState(startPos);
            if (startBlock == BGBlocks.effectBlock.getDefaultState()) return;

            //Prepare the block rendering
            //BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();

            //Save the current position that is being rendered
            GlStateManager.pushMatrix();

            //Enable Blending (So we can have transparent effect)
            GlStateManager.enableBlend();
            //This blend function allows you to use a constant alpha, which is defined later
            GlStateManager.blendFunc(GL14.GL_CONSTANT_ALPHA, GL14.GL_ONE_MINUS_CONSTANT_ALPHA);

            GlStateManager.pushMatrix();//Push matrix again just because
            GlStateManager.translatef((float)-doubleX, (float)-doubleY, (float)-doubleZ);//The render starts at the player, so we subtract the player coords and move the render to 0,0,0
            GlStateManager.translatef(startPos.getX(), startPos.getY(), startPos.getZ()); //Move the render to the startingBlockPos
            GL14.glBlendColor(1F, 1F, 1F, 0.55f); //Set the alpha of the blocks we are rendering
            //GlStateManager.translate(-0.0005f, -0.0005f, 0.0005f);
            //GlStateManager.scale(1.001f, 1.001f, 1.001f);//Slightly Larger block to avoid z-fighting.
            GlStateManager.translatef(0.0005f, 0.0005f, -0.0005f);
            GlStateManager.scalef(0.999f, 0.999f, 0.999f);//Slightly Larger block to avoid z-fighting.
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
            BlockPos startPos = BGItems.gadgetCopyPaste.getStartPos(stack);
            BlockPos endPos = BGItems.gadgetCopyPaste.getEndPos(stack);
            BlockPos blankPos = new BlockPos(0, 0, 0);
            if (startPos == null || endPos == null || startPos.equals(blankPos) || endPos.equals(blankPos)) {
                return;
            }


            List<BlockMap> blockMapList = GadgetCopyPaste.getBlockMapList(PasteToolBufferBuilder.getTagFromUUID(UUID));
            if (blockMapList.size() == 0) {
                //return;
            }

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
            GlStateManager.translatef((float) -doubleX, (float) -doubleY, (float) -doubleZ);//The render starts at the player, so we subtract the player coords and move the render to 0,0,0

            GlStateManager.disableLighting();
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            renderBox(tessellator, bufferbuilder, x, y, z, dx, dy, dz, 255, 223, 127); // Draw the box around the blocks we've copied.

            GlStateManager.lineWidth(1.0F);
            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();
            GlStateManager.enableDepthTest();
            GlStateManager.depthMask(true);

            GlStateManager.popMatrix();

        }
    }

    private static void renderBox(Tessellator tessellator, BufferBuilder bufferBuilder, double startX, double startY, double startZ, double endX, double endY, double endZ, int R, int G, int B) {
        GlStateManager.lineWidth(2.0F);
        bufferBuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
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
        GlStateManager.lineWidth(1.0F);
    }

    private static void renderBoxSolid(Tessellator tessellator, BufferBuilder bufferBuilder, double startX, double startY, double startZ, double endX, double endY, double endZ, float red, float green, float blue, float alpha) {
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        //down
        bufferBuilder.pos(startX, startY, startZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, startY, startZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, startY, endZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(startX, startY, endZ).color(red, green, blue, alpha).endVertex();

        //up
        bufferBuilder.pos(startX, endY, startZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(startX, endY, endZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, endY, endZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, endY, startZ).color(red, green, blue, alpha).endVertex();

        //east
        bufferBuilder.pos(startX, startY, startZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(startX, endY, startZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, endY, startZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, startY, startZ).color(red, green, blue, alpha).endVertex();

        //west
        bufferBuilder.pos(startX, startY, endZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, startY, endZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, endY, endZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(startX, endY, endZ).color(red, green, blue, alpha).endVertex();

        //south
        bufferBuilder.pos(endX, startY, startZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, endY, startZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, endY, endZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(endX, startY, endZ).color(red, green, blue, alpha).endVertex();
        
        //north
        bufferBuilder.pos(startX, startY, startZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(startX, startY, endZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(startX, endY, endZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(startX, endY, startZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
    }

}
