package com.direwolf20.buildinggadgets.Items;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.Entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.ModBlocks;
import com.direwolf20.buildinggadgets.Tools.BuildingModes;
import com.direwolf20.buildinggadgets.Tools.InventoryManipulation;
import com.direwolf20.buildinggadgets.Tools.UndoBuild;
import com.direwolf20.buildinggadgets.Tools.UndoState;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
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

import java.lang.reflect.Array;
import java.util.*;

import static net.minecraft.block.BlockStainedGlass.COLOR;


public class BuildingTool extends Item {

    private static final BlockRenderLayer[] LAYERS = BlockRenderLayer.values();
    private static final FakeBuilderWorld fakeWorld = new FakeBuilderWorld();

    //public static Stack<UndoState> undoList = new Stack<UndoState>();

    public enum toolModes {
        BuildToMe,VertWall,HorzWall,VertCol,HorzCol;
        private static toolModes[] vals = values();
        public toolModes next()
        {
            return vals[(this.ordinal()+1) % vals.length];
        }

    }
    //public static toolModes mode;
    //public static int range;

    public BuildingTool() {
        setRegistryName("buildingtool");        // The unique name (within your mod) that identifies this item
        setUnlocalizedName(BuildingGadgets.MODID + ".buildingtool");     // Used for localization (en_US.lang)
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    public NBTTagCompound initToolTag (ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null){
            tagCompound = new NBTTagCompound();
            stack.setTagCompound(tagCompound);
            tagCompound.setString("mode", toolModes.BuildToMe.name());
            tagCompound.setInteger("range", 1);
            stack.setTagCompound(tagCompound);
            NBTTagCompound stateTag = new NBTTagCompound();
            NBTUtil.writeBlockState(stateTag, Blocks.AIR.getDefaultState());
            tagCompound.setTag("blockstate", stateTag);
        }
        return tagCompound;
    }

    public void setToolMode(ItemStack stack, toolModes mode) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null){
            tagCompound = initToolTag(stack);
        }
        tagCompound.setString("mode", mode.name());
    }

    public void setToolRange(ItemStack stack, int range) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null){
            tagCompound = initToolTag(stack);
        }
        tagCompound.setInteger("range", range);
    }

    public void setToolBlock(ItemStack stack, IBlockState state) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null){
            tagCompound = initToolTag(stack);
        }
        if (state == null) {state = Blocks.AIR.getDefaultState();}
        NBTTagCompound stateTag = new NBTTagCompound();
        NBTUtil.writeBlockState(stateTag, state);
        tagCompound.setTag("blockstate", stateTag);
    }

    public toolModes getToolMode(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null){
            tagCompound = initToolTag(stack);
        }

        return toolModes.valueOf(tagCompound.getString("mode"));
    }

    public int getToolRange(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null){
            tagCompound = initToolTag(stack);
        }
        return tagCompound.getInteger("range");
    }

    public IBlockState getToolBlock(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null){
            tagCompound = initToolTag(stack);
        }
        return NBTUtil.readBlockState(tagCompound.getCompoundTag("blockstate"));
    }


    @Override
    public void addInformation(ItemStack stack, World player, List<String> list, ITooltipFlag b) {
        super.addInformation(stack, player, list, b);
        list.add(TextFormatting.DARK_GREEN + "Block: " + getToolBlock(stack).getBlock().getLocalizedName());
        list.add(TextFormatting.AQUA + "Mode: " + getToolMode(stack));
        list.add(TextFormatting.RED + "Range: " + getToolRange(stack));
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        player.setActiveHand(hand);
            if (!world.isRemote) {
                if (player.isSneaking()) {
                    selectBlock(stack, player, world, pos);
                } else {
                    build(world, player, pos, side,stack);
                }
            }
        return EnumActionResult.SUCCESS;
    }

    private void selectBlock(ItemStack stack, EntityPlayer player, World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (state != null) {
            setToolBlock(stack,state);
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        player.setActiveHand(hand);
        float rayTraceRange = 20f;
        Vec3d look = player.getLookVec();
        Vec3d start = new Vec3d(player.posX,player.posY+player.getEyeHeight(),player.posZ);
        Vec3d end = new Vec3d(player.posX+look.x * rayTraceRange,player.posY+player.getEyeHeight()+look.y*rayTraceRange,player.posZ+look.z*rayTraceRange);

        //RayTraceResult lookingAt = player.rayTrace(20, 1.0F);
        RayTraceResult lookingAt = world.rayTraceBlocks(start,end,false,true,false);

        if (!world.isRemote) {
            if (world.getBlockState(lookingAt.getBlockPos()) != Blocks.AIR.getDefaultState()) {
                build(world, player, lookingAt.getBlockPos(),lookingAt.sideHit,itemstack);
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

    public void toggleMode(EntityPlayer player, ItemStack heldItem) {
        toolModes mode = getToolMode(heldItem);
        mode = mode.next();
        setToolMode(heldItem,mode);
        player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + "Tool Mode: " + mode.name()), true);

    }

    public void rangeChange(EntityPlayer player, ItemStack heldItem) {
        int range = getToolRange(heldItem);
        if (range >=10) {
            range = 1;
        }
        else {
            range++;
        }
        setToolRange(heldItem,range);
        player.sendStatusMessage(new TextComponentString(TextFormatting.RED + "Tool range: " + range), true);
    }

    public boolean build(World world, EntityPlayer player, BlockPos startBlock, EnumFacing sideHit, ItemStack stack) {
        int range = getToolRange(stack);
        toolModes mode = getToolMode(stack);
        ArrayList<BlockPos> coords = BuildingModes.getBuildOrders(world,player,startBlock,sideHit,range,mode);
        ArrayList<BlockPos> undoCoords = new ArrayList<BlockPos>();

        Set<BlockPos> coordinates = new HashSet<BlockPos>(coords);
        IBlockState blockState = Blocks.AIR.getDefaultState();
        ItemStack heldItem = player.getHeldItemMainhand();
        NBTTagCompound tagCompound = heldItem.getTagCompound();
        blockState = NBTUtil.readBlockState(tagCompound.getCompoundTag("blockstate"));
        IBlockState state = Blocks.AIR.getDefaultState();
        for (BlockPos coordinate : coords) {
            fakeWorld.setWorldAndState(player.world,blockState,coordinates);
            if (fakeWorld.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
                try {
                    state = blockState.getActualState(fakeWorld, coordinate);
                } catch (Exception var8) {
                }
            }
            //Get the extended block state in the fake world
            state = state.getBlock().getExtendedState(state, fakeWorld, coordinate);
            if (placeBlock(world, player, coordinate, state)) {
                undoCoords.add(coordinate);
            }
        }
        if (undoCoords.size() > 0) {
            UndoState undoState = new UndoState(player.dimension,undoCoords);
            Stack<UndoState> undoStack = UndoBuild.getPlayerMap(player.getUniqueID());
            undoStack.push(undoState);
            UndoBuild.updatePlayerMap(player.getUniqueID(),undoStack);
            System.out.println(UndoBuild.getPlayerMap(player.getUniqueID()));
        }
        return true;
    }

    public static boolean undoBuild(EntityPlayer player) {
        Stack<UndoState> undoStack = UndoBuild.getPlayerMap(player.getUniqueID());
        if (undoStack.empty()) {return false;}
        World world = player.world;
        if (!world.isRemote) {
            IBlockState airBlock = Blocks.AIR.getDefaultState();
            IBlockState currentBlock = Blocks.AIR.getDefaultState();
            UndoState undoState = undoStack.pop();
            ArrayList<BlockPos> undoCoords = undoState.coordinates;
            int dimension = undoState.dimension;
            ArrayList<BlockPos> failedRemovals = new ArrayList<BlockPos>();
            for (BlockPos coord : undoCoords) {
                currentBlock = world.getBlockState(coord);
                ItemStack itemStack = currentBlock.getBlock().getPickBlock(currentBlock, null, world, coord, player);
                double distance = coord.getDistance(player.getPosition().getX(),player.getPosition().getY(),player.getPosition().getZ());
                boolean sameDim = (player.dimension == dimension);
                if (distance < 35 && sameDim) {
                    if (InventoryManipulation.giveItem(itemStack, player)) {
                        world.spawnEntity(new BlockBuildEntity(world, coord, player, currentBlock, 2));
                    } else {
                        failedRemovals.add(coord);
                    }
                }
                else {
                    failedRemovals.add(coord);
                }
            }
            if (failedRemovals.size() != 0) {
                UndoState failedState = new UndoState(player.dimension,failedRemovals);
                undoStack.push(failedState);
                UndoBuild.updatePlayerMap(player.getUniqueID(),undoStack);
            }
        }
        return true;
    }

    public static boolean placeBlock(World world, EntityPlayer player, BlockPos pos, IBlockState setBlock) {
        ItemStack itemStack = setBlock.getBlock().getPickBlock(setBlock,null,world,pos,player);
        if (InventoryManipulation.useItem(itemStack,player)) {
            world.spawnEntity(new BlockBuildEntity(world, pos, player, setBlock, 1));
        }
        return true;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 20;
    }


    @SideOnly(Side.CLIENT)
    public void renderOverlay(RenderWorldLastEvent evt, EntityPlayer player, ItemStack buildingTool) {
        int range = getToolRange(buildingTool);
        toolModes mode = getToolMode(buildingTool);
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
                ArrayList<BlockPos> coordinates = BuildingModes.getBuildOrders(world,player,lookingAt.getBlockPos(),lookingAt.sideHit, range, mode);

                //int neededBlocks = coordinates.size();
                ItemStack itemStack = renderBlockState.getBlock().getPickBlock(renderBlockState,null,world,new BlockPos(0,0,0),player);
                int hasBlocks = InventoryManipulation.countItem(itemStack,player);
                int tempHasBlocks = hasBlocks;

                BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
                BlockRenderLayer origLayer = MinecraftForgeClient.getRenderLayer();
                Set<BlockPos> coords = new HashSet<BlockPos>(coordinates);
                fakeWorld.setWorldAndState(player.world,renderBlockState,coords);

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
                            //System.out.println(state);
                            dispatcher.renderBlockBrightness(state,1f);
                            tempHasBlocks--;
                            //System.out.println(hasBlocks);
                            if (tempHasBlocks <0) {
                                GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
                                dispatcher.renderBlockBrightness(Blocks.STAINED_GLASS.getDefaultState().withProperty(COLOR, EnumDyeColor.RED), 1f);
                            }
                            //Move the render position back to where it was
                            GlStateManager.popMatrix();
                        }
                        tempHasBlocks = hasBlocks;
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
