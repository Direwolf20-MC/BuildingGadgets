package com.direwolf20.buildinggadgets.items;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.Config;
import com.direwolf20.buildinggadgets.entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.tools.BlockMap;
import com.direwolf20.buildinggadgets.tools.VectorTools;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.ArrayList;
import java.util.List;

import static com.direwolf20.buildinggadgets.tools.GadgetUtils.withSuffix;

public class CopyPasteTool extends GenericGadget {

    public enum toolModes {
        Copy, Paste;
        private static toolModes[] vals = values();

        public toolModes next() {
            return vals[(this.ordinal() + 1) % vals.length];
        }
    }

    public CopyPasteTool() {
        setRegistryName("copypastetool");        // The unique name (within your mod) that identifies this item
        setUnlocalizedName(BuildingGadgets.MODID + ".copypastetool");     // Used for localization (en_US.lang)
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.TOOLS);
    }

    public static void setAnchor(ItemStack stack, BlockPos anchorPos) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        if (anchorPos == null) {
            if (tagCompound.getTag("anchor") != null) {
                tagCompound.removeTag("anchor");
                stack.setTagCompound(tagCompound);
            }
            return;
        }
        NBTTagCompound pos = NBTUtil.createPosTag(anchorPos);
        tagCompound.setTag("anchor", pos);
        stack.setTagCompound(tagCompound);
    }

    public static BlockPos getAnchor(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            return null;
        }
        NBTTagCompound anchorPosTag = tagCompound.getCompoundTag("anchor");
        if (anchorPosTag.equals(new NBTTagCompound())) {
            return null;
        }
        return NBTUtil.getPosFromTag(tagCompound.getCompoundTag("anchor"));
    }

    public static void setLastBuild(ItemStack stack, BlockPos anchorPos) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        if (anchorPos == null) {
            if (tagCompound.getTag("lastBuild") != null) {
                tagCompound.removeTag("lastBuild");
                stack.setTagCompound(tagCompound);
            }
            return;
        }
        NBTTagCompound pos = NBTUtil.createPosTag(anchorPos);
        tagCompound.setTag("lastBuild", pos);
        stack.setTagCompound(tagCompound);
    }

    public static BlockPos getLastBuild(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            return null;
        }
        NBTTagCompound anchorPosTag = tagCompound.getCompoundTag("lastBuild");
        if (anchorPosTag.equals(new NBTTagCompound())) {
            return null;
        }
        return NBTUtil.getPosFromTag(tagCompound.getCompoundTag("lastBuild"));
    }

    public static void setStartPos(ItemStack stack, BlockPos startPos) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        NBTTagCompound pos = NBTUtil.createPosTag(startPos);
        tagCompound.setTag("startPos", pos);
        stack.setTagCompound(tagCompound);
    }

    public static BlockPos getStartPos(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            return null;
        }
        NBTTagCompound anchorPosTag = tagCompound.getCompoundTag("startPos");
        if (anchorPosTag == null) {
            return null;
        }
        return NBTUtil.getPosFromTag(tagCompound.getCompoundTag("startPos"));
    }

    public static void setEndPos(ItemStack stack, BlockPos startPos) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        NBTTagCompound pos = NBTUtil.createPosTag(startPos);
        tagCompound.setTag("endPos", pos);
        stack.setTagCompound(tagCompound);
    }

    public static void addBlockToMap(ItemStack stack, BlockMap map) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        NBTTagList blocks = (NBTTagList) tagCompound.getTag("blocksMapList");
        if (blocks == null) {
            blocks = new NBTTagList();
        }
        BlockPos startPos = getStartPos(stack);
        int px = (((map.pos.getX() - startPos.getX()) & 0xff) << 16);
        int py = (((map.pos.getY() - startPos.getY()) & 0xff) << 8);
        int pz = (((map.pos.getZ() - startPos.getZ()) & 0xff));
        int p = (px + py + pz);

        NBTTagCompound blockMap = new NBTTagCompound();
        //NBTTagCompound blockPos = NBTUtil.createPosTag(pos);

        NBTTagCompound blockState = new NBTTagCompound();
        NBTUtil.writeBlockState(blockState, map.state);
        //blockMap.setTag("pos", blockPos);
        blockMap.setTag("state", blockState);
        blockMap.setInteger("pos", p);
        blocks.appendTag(blockMap);
        tagCompound.setTag("blocksMapList", blocks);
        stack.setTagCompound(tagCompound);
    }

    public static ArrayList<BlockMap> getBlockMapList(ItemStack stack, BlockPos startBlock) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        ArrayList<BlockMap> blockMap = new ArrayList<BlockMap>();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        NBTTagList blocks = (NBTTagList) tagCompound.getTag("blocksMapList");
        if (blocks == null) {
            blocks = new NBTTagList();
        }
        if (blocks.tagCount() == 0) {
            return blockMap;
        }
        for (int i = 0; i < blocks.tagCount(); i++) {
            NBTTagCompound compound = blocks.getCompoundTagAt(i);
            int p = compound.getInteger("pos");
            int x = startBlock.getX() + (int) (byte) ((p & 0xff0000) >> 16);
            int y = startBlock.getY() + (int) (byte) ((p & 0x00ff00) >> 8);
            int z = startBlock.getZ() + (int) (byte) (p & 0x0000ff);
            blockMap.add(new BlockMap(new BlockPos(x, y, z), NBTUtil.readBlockState(compound.getCompoundTag("state"))));
        }
        return blockMap;
    }

    public static void resetBlockMap(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        NBTTagList blocks = new NBTTagList();
        tagCompound.setTag("blocksMapList", blocks);
        stack.setTagCompound(tagCompound);
    }

    public static BlockPos getEndPos(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            return null;
        }
        NBTTagCompound anchorPosTag = tagCompound.getCompoundTag("endPos");
        if (anchorPosTag == null) {
            return null;
        }
        return NBTUtil.getPosFromTag(tagCompound.getCompoundTag("endPos"));
    }

    public static void setToolMode(ItemStack stack, toolModes mode) {
        //Store the tool's mode in NBT as a string
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        tagCompound.setString("mode", mode.name());
        stack.setTagCompound(tagCompound);
    }

    public static toolModes getToolMode(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        toolModes mode = toolModes.Copy;
        if (tagCompound == null) {
            setToolMode(stack, mode);
            return mode;
        }
        try {
            mode = toolModes.valueOf(tagCompound.getString("mode"));
        } catch (Exception e) {
            setToolMode(stack, mode);
        }
        return mode;
    }

    @Override
    public void addInformation(ItemStack stack, World player, List<String> list, ITooltipFlag b) {
        //Add tool information to the tooltip
        super.addInformation(stack, player, list, b);
        list.add(TextFormatting.AQUA + I18n.format("tooltip.gadget.mode") + ": " + getToolMode(stack));
        if (Config.poweredByFE) {
            IEnergyStorage energy = stack.getCapability(CapabilityEnergy.ENERGY, null);
            list.add(TextFormatting.WHITE + I18n.format("tooltip.gadget.energy") + ": " + withSuffix(energy.getEnergyStored()) + "/" + withSuffix(energy.getMaxEnergyStored()));
        }
    }

    public void setMode(EntityPlayer player, ItemStack heldItem, int modeInt) {
        //Called when we specify a mode with the radial menu
        toolModes mode = toolModes.values()[modeInt];
        setToolMode(heldItem, mode);
        player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.toolmode").getUnformattedComponentText() + ": " + mode.name()), true);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        //On item use, if sneaking, select the block clicked on, else build -- This is called when a block in clicked on
        ItemStack stack = player.getHeldItem(hand);
        player.setActiveHand(hand);
        if (!world.isRemote) {
            if (getToolMode(stack) == toolModes.Copy) {
                if (player.isSneaking()) {
                    setEndPos(stack, pos);
                    if (getStartPos(stack) != null) {
                        findBlocks(world, getStartPos(stack), getEndPos(stack), stack);
                        player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.copied").getUnformattedComponentText()), true);
                    }
                } else {
                    setStartPos(stack, pos);
                    if (getEndPos(stack) != null) {
                        findBlocks(world, getStartPos(stack), getEndPos(stack), stack);
                        player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.copied").getUnformattedComponentText()), true);
                    }
                }
            } else if (getToolMode(stack) == toolModes.Paste) {
                if (getBlockMapList(stack, pos).size() > 0) {
                    buildBlockMap(world, pos.up(), stack, player);
                }
            }
        }
        return EnumActionResult.SUCCESS;
    }

    public void findBlocks(World world, BlockPos start, BlockPos end, ItemStack stack) {
        resetBlockMap(stack);
        setLastBuild(stack, null);

        int startX = start.getX();
        int startY = start.getY();
        int startZ = start.getZ();

        int endX = end.getX();
        int endY = end.getY();
        int endZ = end.getZ();

        int iStartX = startX < endX ? startX : endX;
        int iStartY = startY < endY ? startY : endY;
        int iStartZ = startZ < endZ ? startZ : endZ;
        int iEndX = startX < endX ? endX : startX;
        int iEndY = startY < endY ? endY : startY;
        int iEndZ = startZ < endZ ? endZ : startZ;

        for (int x = iStartX; x <= iEndX; x++) {
            for (int y = iStartY; y <= iEndY; y++) {
                for (int z = iStartZ; z <= iEndZ; z++) {
                    BlockPos tempPos = new BlockPos(x, y, z);
                    IBlockState tempState = world.getBlockState(tempPos);
                    if (tempState != Blocks.AIR.getDefaultState() && world.getTileEntity(tempPos) == null) {
                        BlockMap tempMap = new BlockMap(tempPos, tempState);
                        addBlockToMap(stack, tempMap);
                    }
                }
            }
        }
    }

    public void buildBlockMap(World world, BlockPos startPos, ItemStack stack, EntityPlayer player) {
        BlockPos anchorPos = getAnchor(stack);
        ArrayList<BlockMap> blockMapList = new ArrayList<BlockMap>();
        if (anchorPos == null) {
            blockMapList = getBlockMapList(stack, startPos);
            setLastBuild(stack, startPos);
        } else {
            blockMapList = getBlockMapList(stack, anchorPos);
            setLastBuild(stack, anchorPos);
        }
        for (BlockMap blockMap : blockMapList) {
            if (world.getBlockState(blockMap.pos) != Blocks.AIR.getDefaultState()) {
                world.spawnEntity(new BlockBuildEntity(world, blockMap.pos, player, blockMap.state, 3, blockMap.state, false));
            } else {
                world.spawnEntity(new BlockBuildEntity(world, blockMap.pos, player, blockMap.state, 1, blockMap.state, false));
            }
            //world.setBlockState(blockMap.pos, blockMap.state);
        }
        setAnchor(stack, null);
    }

    public static void anchorBlocks(EntityPlayer player, ItemStack stack) {
        World world = player.world;
        BlockPos currentAnchor = getAnchor(stack);
        if (currentAnchor == null) {
            RayTraceResult lookingAt = VectorTools.getLookingAt(player);
            if (lookingAt == null) {
                return;
            }
            currentAnchor = lookingAt.getBlockPos().up();
            setAnchor(stack, currentAnchor);
            player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.anchorrender").getUnformattedComponentText()), true);
        } else {
            setAnchor(stack, null);
            player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.anchorremove").getUnformattedComponentText()), true);
        }
    }

    public static void undoBuild(EntityPlayer player, ItemStack heldItem) {
        World world = player.world;
        if (world.isRemote) {
            return;
        }
        BlockPos startPos = getLastBuild(heldItem);
        if (startPos == null) {
            return;
        }
        ArrayList<BlockMap> blockMapList = getBlockMapList(heldItem, startPos);
        for (BlockMap blockMap : blockMapList) {
            if (world.getBlockState(blockMap.pos) == blockMap.state) {
                world.spawnEntity(new BlockBuildEntity(world, blockMap.pos, player, blockMap.state, 2, blockMap.state, false));
                //world.setBlockState(blockMap.pos, Blocks.AIR.getDefaultState());
            }
        }
    }
}
