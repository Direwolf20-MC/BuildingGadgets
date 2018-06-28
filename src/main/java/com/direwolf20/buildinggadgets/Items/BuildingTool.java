package com.direwolf20.buildinggadgets.Items;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.Entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.ModBlocks;
import com.direwolf20.buildinggadgets.Tools.BuildingModes;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
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
import net.minecraft.world.IBlockAccess;
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
                Set<BlockPos> coordinates = BuildingModes.getBuildOrders(world,player,lookingAt.getBlockPos(),lookingAt.sideHit, range, mode);
                BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
                BlockRenderLayer origLayer = MinecraftForgeClient.getRenderLayer();
                fakeWorld.setWorldAndState(player.world,renderBlockState,coordinates);

                double doubleX = player.lastTickPosX + (player.posX - player.lastTickPosX) * evt.getPartialTicks();
                double doubleY = player.lastTickPosY + (player.posY - player.lastTickPosY) * evt.getPartialTicks();
                double doubleZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * evt.getPartialTicks();

                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);


                for (BlockRenderLayer layer : LAYERS) {
                        ForgeHooksClient.setRenderLayer(layer);


                        for (BlockPos coordinate : coordinates) {
                            GlStateManager.pushMatrix();
                            GlStateManager.translate(-doubleX, -doubleY, -doubleZ);
                            GlStateManager.translate(coordinate.getX(), coordinate.getY(), coordinate.getZ());
                            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
                            GlStateManager.scale(1.0f, 1.0f, 1.0f);
                            GL14.glBlendColor(1F, 1F, 1F, 0.17f);
                            if (fakeWorld.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
                                try {
                                    state = renderBlockState.getActualState(fakeWorld, coordinate);
                                } catch (Exception var8) {
                                }
                            }

                            state = state.getBlock().getExtendedState(state, fakeWorld, coordinate);
                            //Render the defined block
                            dispatcher.renderBlockBrightness(state, 1f);
                            GlStateManager.popMatrix();
                        }
                }
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                ForgeHooksClient.setRenderLayer(origLayer);
                GlStateManager.disableBlend();
                RenderHelper.enableStandardItemLighting();
                GlStateManager.popMatrix();
            }
        }
    }

    protected static boolean renderOutlines2(BlockRendererDispatcher dispatcher, IBlockState state, BlockPos pos, IBlockAccess blockAccess, BufferBuilder worldRendererIn) {
        try {
            EnumBlockRenderType enumblockrendertype = state.getRenderType();

            if (enumblockrendertype == EnumBlockRenderType.INVISIBLE) {
                return false;
            } else {
                if (blockAccess.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
                    try {
                        state = state.getActualState(blockAccess, pos);
                    } catch (Exception var8) {
                    }
                }

                switch (enumblockrendertype) {
                    case MODEL:
                        IBakedModel model = dispatcher.getModelForState(state);
                        state = state.getBlock().getExtendedState(state, blockAccess, pos);
                        return dispatcher.getBlockModelRenderer().renderModel(blockAccess, model, state, pos, worldRendererIn, false);
                    case ENTITYBLOCK_ANIMATED:
                        return false;
                    default:
                        return false;
                }
            }
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Tesselating block in world");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being tesselated");
            CrashReportCategory.addBlockInfo(crashreportcategory, pos, state.getBlock(), state.getBlock().getMetaFromState(state));
            throw new ReportedException(crashreport);
        }
    }

    protected static void renderOutlines(RenderWorldLastEvent evt, EntityPlayer p, BlockPos pos) {


        BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

        //Get player position, since the render starts at player position
        double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX) * evt.getPartialTicks();
        double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY) * evt.getPartialTicks();
        double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * evt.getPartialTicks();
/*
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
*/
        //Get Block to be rendered from the tool's data
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

        //Prep GL for a new render
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();

        //GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.ONE,GlStateManager.DestFactor.ONE,GlStateManager.SourceFactor.CONSTANT_ALPHA,GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA);
        //GlStateManager.disableDepth();
        //GlStateManager.disableLighting();
        //GlStateManager.disableDepth();
        //GlStateManager.depthMask(false);

        //Move block position to the X,Y,Z of the rendering spot, rotate and scale the block
        GlStateManager.translate(-doubleX,-doubleY,-doubleZ);
        GlStateManager.translate(pos.getX(),pos.getY(),pos.getZ());
        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(1.0f,1.0f,1.0f);

        //GlStateManager.color(1F, 1F, 1F, 1F);
        //GL14.glBlendFuncSeparate(GL11.GL_ONE,GL11.GL_ONE,GL11.GL_ONE, GL11.GL_ONE);
        GlStateManager.blendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);
        GL14.glBlendColor(1F, 1F, 1F, 0.7f);

        //Render the defined block
        blockrendererdispatcher.renderBlockBrightness(renderBlockState, 1f);

        //Cleanup GL
        //GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        //GlStateManager.enableDepth();
        //GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();

    }


}
