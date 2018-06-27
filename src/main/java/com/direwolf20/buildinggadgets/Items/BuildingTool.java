package com.direwolf20.buildinggadgets.Items;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.Entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.ModBlocks;
import com.direwolf20.buildinggadgets.Tools.BuildingModes;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.HashSet;
import java.util.Set;


public class BuildingTool extends Item {

    public enum toolModes {BuildToMe,PerpWall,VertCol,HorzCol}
    public static toolModes mode;
    public static int range = 3;

    public BuildingTool() {
        setRegistryName("buildingtool");        // The unique name (within your mod) that identifies this item
        setUnlocalizedName(BuildingGadgets.MODID + ".buildingtool");     // Used for localization (en_US.lang)
        //mode = toolModes.BuildToMe;
        mode = toolModes.PerpWall;
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            if (player.isSneaking()) {
                selectBlock(stack, player, world, pos);
            } else {
                build(world, player, pos,side);
            }
        }
        return EnumActionResult.SUCCESS;
    }

    private void selectBlock(ItemStack stack, EntityPlayer player, World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);

        if (state != null) {
            //Block block = state.getBlock();
            NBTTagCompound tagCompound = stack.getTagCompound();
            if (tagCompound == null){
                tagCompound = new NBTTagCompound();
                stack.setTagCompound(tagCompound);
            }
            NBTTagCompound stateTag = new NBTTagCompound();
            NBTUtil.writeBlockState(stateTag, state);
            //ItemStack item = block.getPickBlock(state, null, world, pos, player);
            //int meta = item.getMetadata();
            //NBTTagCompound tagCompound = Tools.getTagCompound(stack);
            //String name = item.getDisplayName();
            //if (name == null) {

            //} else {
            //int id = Block.REGISTRY.getIDForObject(block);
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
        //System.out.println(lookingAt.sideHit);
        if (!world.isRemote) {
            if (world.getBlockState(lookingAt.getBlockPos()) != Blocks.AIR.getDefaultState()) {
                build(world, player, lookingAt.getBlockPos(),lookingAt.sideHit);
                //world.spawnEntity(new BlockBuildEntity(world, lookingAt.getBlockPos().up(), player,Blocks.COBBLESTONE.getDefaultState()));
            }
            else {
                if (player.isSneaking()) {
                    if (mode == toolModes.PerpWall) {
                        mode = toolModes.BuildToMe;
                    } else if (mode == toolModes.BuildToMe) {
                        mode = toolModes.VertCol;
                    } else if (mode == toolModes.VertCol) {
                        mode = toolModes.HorzCol;
                    } else if (mode == toolModes.HorzCol) {
                        mode = toolModes.PerpWall;
                    }
                }
            }
        }
        else {

        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
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
        if (world.getBlockState(pos).getBlock().isReplaceable(world,pos)) {
            world.spawnEntity(new BlockBuildEntity(world, pos, player, setBlock,false));
        }
        return true;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 20;
    }


    @SideOnly(Side.CLIENT)
    public static void renderOverlay(RenderWorldLastEvent evt, EntityPlayer player, ItemStack buildingTool) {
        RayTraceResult lookingAt = player.rayTrace(20, 1.0F);
        if (lookingAt != null) {
            World world = player.world;
            IBlockState startBlock = world.getBlockState(lookingAt.getBlockPos());
            if ((startBlock != null) && (startBlock != Blocks.AIR.getDefaultState()) && (startBlock != ModBlocks.effectBlock.getDefaultState())) {
                Set<BlockPos> coordinates = BuildingModes.getBuildOrders(world,player,lookingAt.getBlockPos(),lookingAt.sideHit, range, mode);
                for (BlockPos coordinate : coordinates) {
                    renderOutlines(evt, player, coordinate);
                }
            }
        }
    }

    protected static void renderOutlines(RenderWorldLastEvent evt, EntityPlayer p, BlockPos pos) {


        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        //Get player position, since the render starts at player position
        double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX) * evt.getPartialTicks();
        double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY) * evt.getPartialTicks();
        double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * evt.getPartialTicks();

        //Define the values for the GL Cube we're about to render
        double minX = pos.getX();
        double minY = pos.getY();
        double minZ = pos.getZ();
        double maxX = pos.getX()+1;
        double maxY = pos.getY()+1;
        double maxZ = pos.getZ()+1;
        float red = 0f;
        float green = 0f;
        float blue = 0f;
        float alpha = 0.5f;

        //Temporarily render just lapis, will eventually render tool selection
        //IBlockState renderBlockState = Blocks.LAPIS_BLOCK.getDefaultState();
        IBlockState renderBlockState = Blocks.AIR.getDefaultState();
        ItemStack heldItem = p.getHeldItemMainhand();
        NBTTagCompound tagCompound = heldItem.getTagCompound();
        if (tagCompound == null){
            tagCompound = new NBTTagCompound();
            heldItem.setTagCompound(tagCompound);
        }
        renderBlockState = NBTUtil.readBlockState(tagCompound.getCompoundTag("blockstate"));
        if (renderBlockState == null) {
            renderBlockState = Blocks.AIR.getDefaultState();
        }

        //Prep GL for rendering fancy stuff
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);

        //Move the render from player position to 0,0,0
        GlStateManager.translate(-doubleX,-doubleY,-doubleZ);

        //Prepare to render a GL cube
        Tessellator t = Tessellator.getInstance();
        BufferBuilder bufferBuilder = t.getBuffer();
        bufferBuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);

        //Define GL Cube points and render them with t.Draw()
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

        //north
        bufferBuilder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();

        //south
        bufferBuilder.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();

        //east
        bufferBuilder.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();

        //west
        bufferBuilder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
        bufferBuilder.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
        t.draw();

        //Set GL state back to the way it was
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);
        //GlStateManager.popAttrib();
        GlStateManager.popMatrix();

        //Prep GL for a new render
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        //GlStateManager.disableDepth();
        //GlStateManager.depthMask(false);

        //Move block position to the X,Y,Z of the rendering spot, rotate and scale the block
        GlStateManager.translate(-doubleX,-doubleY,-doubleZ);
        GlStateManager.translate(pos.getX(),pos.getY(),pos.getZ());
        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(1.0f,1.0f,1.0f);
        GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_ONE);

        //Render the defined block
        blockrendererdispatcher.renderBlockBrightness(renderBlockState, 0.75f);

        //Cleanup GL
        //GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        //GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    private static void renderBlockOutline(Tessellator tessellator, float mx, float my, float mz, float o) {

    }

}
