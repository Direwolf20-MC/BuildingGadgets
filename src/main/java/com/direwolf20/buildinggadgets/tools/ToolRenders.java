package com.direwolf20.buildinggadgets.tools;

import com.direwolf20.buildinggadgets.ModBlocks;
import com.direwolf20.buildinggadgets.items.BuildingTool;
import com.direwolf20.buildinggadgets.items.ExchangerTool;
import com.direwolf20.buildinggadgets.items.FakeBuilderWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
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
import java.util.Set;

import static net.minecraft.block.BlockStainedGlass.COLOR;

public class ToolRenders {
    private static final FakeBuilderWorld fakeWorld = new FakeBuilderWorld();

    public static void renderBuilderOverlay(RenderWorldLastEvent evt, EntityPlayer player, ItemStack stack) {
        int range = BuildingTool.getToolRange(stack);
        BuildingTool.Mode mode = BuildingTool.getToolMode(stack);
        RayTraceResult lookingAt = VectorTools.getLookingAt(player);
        IBlockState state = Blocks.AIR.getDefaultState();
        ArrayList<BlockPos> coordinates = BuildingTool.getAnchor(stack);
        if (lookingAt != null || coordinates.size() > 0) {
            World world = player.world;
            IBlockState startBlock = Blocks.AIR.getDefaultState();
            if (!(lookingAt == null)) {
                startBlock = world.getBlockState(lookingAt.getBlockPos());
            }
            if (startBlock != ModBlocks.effectBlock.getDefaultState()) {
                ItemStack heldItem = player.getHeldItemMainhand(); //Get the item stack and the block that we'll be rendering (From the Itemstack's NBT)
                if (!(heldItem.getItem() instanceof BuildingTool)) {
                    heldItem = player.getHeldItemOffhand();
                    if (!(heldItem.getItem() instanceof BuildingTool)) {return;}
                }
                IBlockState renderBlockState = BuildingTool.getToolBlock(heldItem);
                Minecraft mc = Minecraft.getMinecraft();
                mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                if (renderBlockState == Blocks.AIR.getDefaultState()) {//Don't render anything if there is no block selected (Air)
                    return;
                }
                if (coordinates.size() == 0) { //Build a list of coordinates based on the tool mode and range
                    coordinates = BuildingModes.getBuildOrders(world, player, lookingAt.getBlockPos(), lookingAt.sideHit, range, mode, renderBlockState);
                }

                //Figure out how many of the block we're rendering we have in the inventory of the player.
                //ItemStack itemStack = renderBlockState.getBlock().getPickBlock(renderBlockState, null, world, new BlockPos(0, 0, 0), player);
                //ItemStack itemStack = InventoryManipulation.getSilkTouchDrop(renderBlockState);
                ItemStack itemStack;
                if (renderBlockState.getBlock().canSilkHarvest(world,new BlockPos(0,0,0),renderBlockState,player)) {
                    itemStack = InventoryManipulation.getSilkTouchDrop(renderBlockState);
                } else {
                    itemStack = renderBlockState.getBlock().getPickBlock(renderBlockState, null, world, new BlockPos(0, 0, 0), player);
                }

                int hasBlocks = InventoryManipulation.countItem(itemStack, player);

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

                ArrayList<BlockPos> sortedCoordinates = BuildingModes.sortByDistance(coordinates, player); //Sort the coords by distance to player.

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
                    dispatcher.renderBlockBrightness(state, 1f);//Render the defined block
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
                    if (hasBlocks < 0) {
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
                RenderHelper.enableStandardItemLighting();
                //Pop from the original push in this method
                GlStateManager.popMatrix();
            }
        }
    }

    public static void renderExchangerOverlay(RenderWorldLastEvent evt, EntityPlayer player, ItemStack stack) {
        int range = ExchangerTool.getToolRange(stack);
        ExchangerTool.Mode mode = ExchangerTool.getToolMode(stack);
        RayTraceResult lookingAt = VectorTools.getLookingAt(player);
        IBlockState state = Blocks.AIR.getDefaultState();
        ArrayList<BlockPos> coordinates = ExchangerTool.getAnchor(stack);
        if (lookingAt != null || coordinates.size() > 0) {
            World world = player.world;
            IBlockState startBlock = Blocks.AIR.getDefaultState();
            if (!(lookingAt == null)) {
                startBlock = world.getBlockState(lookingAt.getBlockPos());
            }
            if (startBlock != ModBlocks.effectBlock.getDefaultState()) {
                ItemStack heldItem = player.getHeldItemMainhand(); //Get the item stack and the block that we'll be rendering (From the Itemstack's NBT)
                if (!(heldItem.getItem() instanceof ExchangerTool)) {
                    heldItem = player.getHeldItemOffhand();
                    if (!(heldItem.getItem() instanceof ExchangerTool)) {return;}
                }
                IBlockState renderBlockState = ExchangerTool.getToolBlock(heldItem);
                Minecraft mc = Minecraft.getMinecraft();
                mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                if (renderBlockState == Blocks.AIR.getDefaultState()) {//Don't render anything if there is no block selected (Air)
                    return;
                }
                if (coordinates.size() == 0) { //Build a list of coordinates based on the tool mode and range
                    coordinates = ExchangingModes.getBuildOrders(world, player, lookingAt.getBlockPos(), lookingAt.sideHit, range, mode, renderBlockState, ExchangerTool.getFuzzy(stack));
                }

                //Figure out how many of the block we're rendering we have in the inventory of the player.
                //ItemStack itemStack = renderBlockState.getBlock().getPickBlock(renderBlockState, null, world, new BlockPos(0, 0, 0), player);
                //ItemStack itemStack = InventoryManipulation.getSilkTouchDrop(renderBlockState);
                ItemStack itemStack;
                if (renderBlockState.getBlock().canSilkHarvest(world,new BlockPos(0,0,0),renderBlockState,player)) {
                    itemStack = InventoryManipulation.getSilkTouchDrop(renderBlockState);
                } else {
                    itemStack = renderBlockState.getBlock().getPickBlock(renderBlockState, null, world, new BlockPos(0, 0, 0), player);
                }
                int hasBlocks = InventoryManipulation.countItem(itemStack, player);

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

                //ArrayList<BlockPos> sortedCoordinates = ExchangingModes.sortByDistance(coordinates, player); //Sort the coords by distance to player.

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
                    dispatcher.renderBlockBrightness(state, 1f);//Render the defined block
                    GL14.glBlendColor(1F, 1F, 1F, 0.1f); //Set the alpha of the blocks we are rendering
                    GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F); //Rotate it because i'm not sure why but we need to
                    dispatcher.renderBlockBrightness(Blocks.STAINED_GLASS.getDefaultState().withProperty(COLOR,EnumDyeColor.WHITE), 1f);//Render the defined block - White glass to show non-full block renders (Example: Torch)
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
                    if (hasBlocks < 0) {
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
                RenderHelper.enableStandardItemLighting();
                //Pop from the original push in this method
                GlStateManager.popMatrix();
            }
        }
    }
}
