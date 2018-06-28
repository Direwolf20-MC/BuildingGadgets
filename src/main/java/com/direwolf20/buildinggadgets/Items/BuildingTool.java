package com.direwolf20.buildinggadgets.Items;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.Entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.ModBlocks;
import com.direwolf20.buildinggadgets.Tools.BuildingModes;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.util.List;
import java.util.Set;


public class BuildingTool extends Item {

    private static final BlockRenderLayer[] LAYERS = BlockRenderLayer.values();
    private static final FakeBuilderWorld fakeWorld = new FakeBuilderWorld();

    public enum toolModes {BuildToMe,VertWall,HorzWall,VertCol,HorzCol}
    public static toolModes mode;
    public static int range;

    public BuildingTool() {
        setRegistryName("buildingtool");        // The unique name (within your mod) that identifies this item
        setUnlocalizedName(BuildingGadgets.MODID + ".buildingtool");     // Used for localization (en_US.lang)
        mode = toolModes.BuildToMe;
        range = 1;
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public void addInformation(ItemStack stack, World player, List<String> list, ITooltipFlag b) {
        super.addInformation(stack, player, list, b);
        IBlockState renderBlockState = Blocks.AIR.getDefaultState();
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null){
            tagCompound = new NBTTagCompound();
            stack.setTagCompound(tagCompound);
            tagCompound.setString("mode", mode.name());
            tagCompound.setInteger("range", range);
            stack.setTagCompound(tagCompound);
        }
        tagCompound = stack.getTagCompound();
        renderBlockState = NBTUtil.readBlockState(tagCompound.getCompoundTag("blockstate"));
        //mode = toolModes.valueOf(tagCompound.getString("mode"));
        //range = tagCompound.getInteger("range");
        list.add(TextFormatting.DARK_GREEN + "Block: " + renderBlockState.getBlock().getLocalizedName());
        list.add(TextFormatting.AQUA + "Mode: " + mode);
        list.add(TextFormatting.RED + "Range: " + range);

    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        player.setActiveHand(hand);
            if (!world.isRemote) {
                if (player.isSneaking()) {
                    selectBlock(stack, player, world, pos);
                } else {
                    build(world, player, pos, side);
                }
            }
        return EnumActionResult.SUCCESS;
    }

    private void selectBlock(ItemStack stack, EntityPlayer player, World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);

        if (state != null) {
            NBTTagCompound tagCompound = stack.getTagCompound();
            if (tagCompound == null){
                tagCompound = new NBTTagCompound();
                stack.setTagCompound(tagCompound);
            }
            NBTTagCompound stateTag = new NBTTagCompound();
            NBTUtil.writeBlockState(stateTag, state);
            tagCompound.setTag("blockstate", stateTag);
            //Tools.notify(player, "Selected block: " + name);
            //}
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        player.setActiveHand(hand);
        RayTraceResult lookingAt = player.rayTrace(20, 1.0F);
        if (!world.isRemote) {
            if (world.getBlockState(lookingAt.getBlockPos()) != Blocks.AIR.getDefaultState()) {
                build(world, player, lookingAt.getBlockPos(),lookingAt.sideHit);
            }
            else {
                if (player.isSneaking()) {
                    toggleMode(player, itemstack);
                }
            }
        }
        else {

        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
    }

    public static void toggleMode(EntityPlayer player, ItemStack heldItem) {
        if (mode == toolModes.VertWall) {
            mode = toolModes.BuildToMe;
        } else if (mode == toolModes.BuildToMe) {
            mode = toolModes.VertCol;
        } else if (mode == toolModes.VertCol) {
            mode = toolModes.HorzCol;
        } else if (mode == toolModes.HorzCol) {
            mode = toolModes.HorzWall;
        }else if (mode == toolModes.HorzWall) {
            mode = toolModes.VertWall;
        }
        NBTTagCompound tagCompound = heldItem.getTagCompound();
        tagCompound.setString("mode", mode.name());
        heldItem.setTagCompound(tagCompound);
    }

    public static void rangeChange(EntityPlayer player, ItemStack heldItem) {
        if (range >=10) {
            range = 1;
        }
        else {
            range++;
        }
        NBTTagCompound tagCompound = heldItem.getTagCompound();
        tagCompound.setInteger("range", range);
        heldItem.setTagCompound(tagCompound);
    }

    public static boolean build(World world, EntityPlayer player, BlockPos startBlock, EnumFacing sideHit) {
        Set<BlockPos> coordinates = BuildingModes.getBuildOrders(world,player,startBlock,sideHit,range,mode);
        IBlockState blockState = Blocks.AIR.getDefaultState();
        ItemStack heldItem = player.getHeldItemMainhand();
        NBTTagCompound tagCompound = heldItem.getTagCompound();
        blockState = NBTUtil.readBlockState(tagCompound.getCompoundTag("blockstate"));
        for (BlockPos coordinate : coordinates) {
            placeBlock(world, player, coordinate, blockState);
        }

        return true;
    }

    public static boolean placeBlock(World world, EntityPlayer player, BlockPos pos, IBlockState setBlock) {
        world.spawnEntity(new BlockBuildEntity(world, pos, player, setBlock,false));
        return true;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 20;
    }


    @SideOnly(Side.CLIENT)
    public static void renderOverlay(RenderWorldLastEvent evt, EntityPlayer player, ItemStack buildingTool) {
        RayTraceResult lookingAt = player.rayTrace(20, 1.0F);
        IBlockState state = Blocks.AIR.getDefaultState();
        if (lookingAt != null) {
            World world = player.world;
            IBlockState startBlock = world.getBlockState(lookingAt.getBlockPos());
            if ((startBlock != null) && (startBlock != Blocks.AIR.getDefaultState()) && (startBlock != ModBlocks.effectBlock.getDefaultState())) {
                IBlockState renderBlockState = Blocks.AIR.getDefaultState();
                ItemStack heldItem = player.getHeldItemMainhand();
                NBTTagCompound tagCompound = heldItem.getTagCompound();
                if (tagCompound == null){
                    tagCompound = new NBTTagCompound();
                    heldItem.setTagCompound(tagCompound);
                }
                renderBlockState = NBTUtil.readBlockState(tagCompound.getCompoundTag("blockstate"));
                if (renderBlockState == null) {
                    renderBlockState = Blocks.AIR.getDefaultState();
                }

                //Build a list of coordinates based on the tool mode and range
                Set<BlockPos> coordinates = BuildingModes.getBuildOrders(world,player,lookingAt.getBlockPos(),lookingAt.sideHit, range, mode);

                BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
                BlockRenderLayer origLayer = MinecraftForgeClient.getRenderLayer();
                fakeWorld.setWorldAndState(player.world,renderBlockState,coordinates);

                //Calculate the players current position, which is needed later
                double doubleX = player.lastTickPosX + (player.posX - player.lastTickPosX) * evt.getPartialTicks();
                double doubleY = player.lastTickPosY + (player.posY - player.lastTickPosY) * evt.getPartialTicks();
                double doubleZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * evt.getPartialTicks();

                //Save the current position thats being rendered (I think)
                GlStateManager.pushMatrix();
                //Enable Blending (So we can have transparent effect)
                GlStateManager.enableBlend();
                //This blend function allows you to use a constant alpha, which is defined later
                GlStateManager.blendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);


                for (BlockRenderLayer layer : LAYERS) {
                    //Technically we are rendering these blocks in all 4 render Layers
                    //I'm not sure why but its the only way it works properly
                        ForgeHooksClient.setRenderLayer(layer);


                        for (BlockPos coordinate : coordinates) {
                            //Push matrix again just because
                            GlStateManager.pushMatrix();
                            //The render starts at the player, so we subtract the player coords and move the render to 0,0,0
                            GlStateManager.translate(-doubleX, -doubleY, -doubleZ);
                            //Now move the render position to the coordinates we want to render at
                            GlStateManager.translate(coordinate.getX(), coordinate.getY(), coordinate.getZ());
                            //Rotate it because i'm not sure why but we need to
                            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
                            GlStateManager.scale(1.0f, 1.0f, 1.0f);
                            //Set the alpha of the blocks we are rendering -- 1/4 of what we really want because we render 4x over
                            GL14.glBlendColor(1F, 1F, 1F, 0.17f);
                            //Get the block state in the fake world
                            if (fakeWorld.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
                                try {
                                    state = renderBlockState.getActualState(fakeWorld, coordinate);
                                } catch (Exception var8) {
                                }
                            }
                            //Get the extended block state in the fake world
                            state = state.getBlock().getExtendedState(state, fakeWorld, coordinate);
                            //Render the defined block
                            dispatcher.renderBlockBrightness(state, 1f);
                            //Move the render position back to where it was
                            GlStateManager.popMatrix();
                        }
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
