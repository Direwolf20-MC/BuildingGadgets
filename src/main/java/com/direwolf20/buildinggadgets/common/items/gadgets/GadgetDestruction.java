package com.direwolf20.buildinggadgets.common.items.gadgets;

import com.direwolf20.buildinggadgets.client.gui.GuiProxy;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.blocks.ModBlocks;
import com.direwolf20.buildinggadgets.common.config.InGameConfig;
import com.direwolf20.buildinggadgets.common.entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.common.tools.BlockMapIntState;
import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import com.direwolf20.buildinggadgets.common.tools.VectorTools;
import com.direwolf20.buildinggadgets.common.tools.WorldSave;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;

import javax.annotation.Nullable;
import java.util.*;

import static com.direwolf20.buildinggadgets.common.tools.GadgetUtils.withSuffix;

public class GadgetDestruction extends GadgetGeneric {

    public GadgetDestruction() {
        setRegistryName("destructiontool");        // The unique name (within your mod) that identifies this item
        setUnlocalizedName(BuildingGadgets.MODID + ".destructiontool");     // Used for localization (en_US.lang)
        setMaxStackSize(1);
        if (!InGameConfig.poweredByFE) {
            setMaxDamage(InGameConfig.durabilityDestruction);
        }
    }

    @Override
    public int getEnergyMax() {
        return InGameConfig.energyMaxDestruction;
    }

    @Override
    public int getEnergyCost() {
        return InGameConfig.energyCostDestruction;
    }

    @Override
    public int getDamagePerUse() {
        return 2;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag b) {
        super.addInformation(stack, world, list, b);
        list.add(TextFormatting.RED + I18n.format("tooltip.gadget.destroywarning"));
        list.add(TextFormatting.AQUA + I18n.format("tooltip.gadget.destroyshowoverlay") + ": " + getOverlay(stack));
        if (InGameConfig.poweredByFE) {
            IEnergyStorage energy = stack.getCapability(CapabilityEnergy.ENERGY, null);
            if (energy != null)
                list.add(TextFormatting.WHITE + I18n.format("tooltip.gadget.energy") + ": " + withSuffix(energy.getEnergyStored()) + "/" + withSuffix(energy.getMaxEnergyStored()));
        }
    }

    @Nullable
    public static String getUUID(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        String uuid = tagCompound.getString("UUID");
        if (uuid.equals("")) {
            UUID uid = UUID.randomUUID();
            tagCompound.setString("UUID", uid.toString());
            stack.setTagCompound(tagCompound);
            uuid = uid.toString();
        }
        return uuid;
    }

    public static void setAnchor(ItemStack stack, BlockPos pos) {
        GadgetUtils.writePOSToNBT(stack, pos, "anchor");
    }

    public static BlockPos getAnchor(ItemStack stack) {
        return GadgetUtils.getPOSFromNBT(stack, "anchor");
    }

    public static void setAnchorSide(ItemStack stack, EnumFacing side) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        if (side == null) {
            if (tagCompound.getTag("anchorSide") != null) {
                tagCompound.removeTag("anchorSide");
                stack.setTagCompound(tagCompound);
            }
            return;
        }
        tagCompound.setString("anchorSide", side.getName());
        stack.setTagCompound(tagCompound);
    }

    public static EnumFacing getAnchorSide(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            return null;
        }
        String facing = tagCompound.getString("anchorSide");
        if (facing.equals("") || facing.isEmpty()) return null;
        return EnumFacing.byName(facing);
    }

    public static void setToolValue(ItemStack stack, int value, String valueName) {
        //Store the tool's range in NBT as an Integer
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        tagCompound.setInteger(valueName, value);
        stack.setTagCompound(tagCompound);
    }

    public static int getToolValue(ItemStack stack, String valueName) {
        //Store the tool's range in NBT as an Integer
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        return tagCompound.getInteger(valueName);
    }

    public static boolean getOverlay(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            tagCompound.setBoolean("overlay", true);
            stack.setTagCompound(tagCompound);
            return true;
        }
        if (tagCompound.hasKey("overlay")) {
            return tagCompound.getBoolean("overlay");
        }
        tagCompound.setBoolean("overlay", true);
        stack.setTagCompound(tagCompound);
        return true;
    }

    public static void setOverlay(ItemStack stack, boolean showOverlay) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        tagCompound.setBoolean("overlay", showOverlay);
        stack.setTagCompound(tagCompound);
    }

    public void switchOverlay(ItemStack stack) {
        setOverlay(stack, !getOverlay(stack));
    }

    public static ArrayList<EnumFacing> assignDirections(EnumFacing side, EntityPlayer player) {
        ArrayList<EnumFacing> dirs = new ArrayList<EnumFacing>();
        EnumFacing left;
        EnumFacing right;
        EnumFacing up;
        EnumFacing down;
        EnumFacing depth = side.getOpposite();

        if (side.equals(EnumFacing.NORTH) || side.equals(EnumFacing.SOUTH) || side.equals(EnumFacing.EAST) || side.equals(EnumFacing.WEST)) {
            up = EnumFacing.UP;
        } else {
            up = player.getHorizontalFacing();
        }
        down = up.getOpposite();

        if (side.equals(EnumFacing.WEST)) {
            left = EnumFacing.SOUTH;
        } else if (side.equals(EnumFacing.EAST)) {
            left = EnumFacing.NORTH;
        } else if (side.equals(EnumFacing.NORTH)) {
            left = EnumFacing.WEST;
        } else if (side.equals(EnumFacing.SOUTH)) {
            left = EnumFacing.EAST;
        } else {
            left = player.getHorizontalFacing().rotateYCCW().getOpposite();
        }
        right = left.getOpposite();
        dirs.add(left);
        dirs.add(right);
        dirs.add(up);
        dirs.add(down);
        dirs.add(depth);

        return dirs;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        player.setActiveHand(hand);
        if (!world.isRemote) {
            if (!player.isSneaking()) {
                clearArea(world, pos, side, player, stack);
                if (getAnchor(stack) != null) {
                    setAnchor(stack, null);
                    setAnchorSide(stack, null);
                    player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.anchorremove").getUnformattedComponentText()), true);
                }
            }
        } else {
            if (player.isSneaking()) {
                player.openGui(BuildingGadgets.instance, GuiProxy.DestructionID, world, hand.ordinal(), 0, 0);
                return EnumActionResult.SUCCESS;
            }
        }

        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        player.setActiveHand(hand);
        if (!world.isRemote) {
            if (!player.isSneaking()) {
                RayTraceResult lookingAt = VectorTools.getLookingAt(player);
                if (lookingAt == null && getAnchor(stack) == null) { //If we aren't looking at anything, exit
                    return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
                }
                BlockPos startBlock = (getAnchor(stack) == null) ? lookingAt.getBlockPos() : getAnchor(stack);
                EnumFacing sideHit = (getAnchorSide(stack) == null) ? lookingAt.sideHit : getAnchorSide(stack);
                clearArea(world, startBlock, sideHit, player, stack);
                if (getAnchor(stack) != null) {
                    setAnchor(stack, null);
                    setAnchorSide(stack, null);
                    player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.anchorremove").getUnformattedComponentText()), true);
                }
            }
        } else {
            if (player.isSneaking()) {
                player.openGui(BuildingGadgets.instance, GuiProxy.DestructionID, world, hand.ordinal(), 0, 0);
                return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
            }
        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    public static void anchorBlocks(EntityPlayer player, ItemStack stack) {
        BlockPos currentAnchor = getAnchor(stack);
        if (currentAnchor == null) {
            RayTraceResult lookingAt = VectorTools.getLookingAt(player);
            if (lookingAt == null) {
                return;
            }
            currentAnchor = lookingAt.getBlockPos();
            setAnchor(stack, currentAnchor);
            setAnchorSide(stack, lookingAt.sideHit);
            player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.anchorrender").getUnformattedComponentText()), true);
        } else {
            setAnchor(stack, null);
            setAnchorSide(stack, null);
            player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.anchorremove").getUnformattedComponentText()), true);
        }
    }

    public static ArrayList<BlockPos> getArea(World world, BlockPos pos, EnumFacing incomingSide, EntityPlayer player, ItemStack stack) {
        ArrayList<BlockPos> voidPosArray = new ArrayList<BlockPos>();
        BlockPos startPos = (getAnchor(stack) == null) ? pos : getAnchor(stack);
        EnumFacing side = (getAnchorSide(stack) == null) ? incomingSide : getAnchorSide(stack);
        ArrayList<EnumFacing> directions = assignDirections(side, player);
        for (int d = 0; d < getToolValue(stack, "depth"); d++) {
            for (int x = getToolValue(stack, "left") * -1; x <= getToolValue(stack, "right"); x++) {
                for (int y = getToolValue(stack, "down") * -1; y <= getToolValue(stack, "up"); y++) {
                    BlockPos voidPos = new BlockPos(startPos);
                    voidPos = voidPos.offset(directions.get(0), x);
                    voidPos = voidPos.offset(directions.get(2), y);
                    voidPos = voidPos.offset(directions.get(4), d);
                    if (validBlock(world, voidPos, player)) {
                        voidPosArray.add(voidPos);
                    }
                }
            }
        }
        return voidPosArray;
    }

    public static boolean validBlock(World world, BlockPos voidPos, EntityPlayer player) {
        IBlockState currentBlock = world.getBlockState(voidPos);
        TileEntity te = world.getTileEntity(voidPos);
        if (currentBlock.getMaterial() == Material.AIR) return false;
        //if (currentBlock.getBlock().getMaterial(currentBlock).isLiquid()) return false;
        if (currentBlock.equals(ModBlocks.effectBlock.getDefaultState())) return false;
        if ((te != null) && !(te instanceof ConstructionBlockTileEntity)) return false;
        if (currentBlock.getBlockHardness(world, voidPos) < 0) return false;

        ItemStack tool = getGadget(player);
        if (tool.isEmpty()) return false;

        if (!player.isAllowEdit()) {
            return false;
        }
        if (!world.isBlockModifiable(player, voidPos)) {
            return false;
        }
        if (!world.isRemote) {
            BlockSnapshot blockSnapshot = BlockSnapshot.getBlockSnapshot(world, voidPos);
            if (ForgeEventFactory.onPlayerBlockPlace(player, blockSnapshot, EnumFacing.UP, EnumHand.MAIN_HAND).isCanceled()) {
                return false;
            }
            BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(world, voidPos, currentBlock, player);
            if (MinecraftForge.EVENT_BUS.post(e)) {
                return false;
            }
        }
        return true;
    }

    public void clearArea(World world, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack) {
        ArrayList<BlockPos> voidPosArray = getArea(world, pos, side, player, stack);
        Map<BlockPos, IBlockState> posStateMap = new HashMap<BlockPos, IBlockState>();
        Map<BlockPos, IBlockState> pasteStateMap = new HashMap<BlockPos, IBlockState>();
        for (BlockPos voidPos : voidPosArray) {
            IBlockState blockState = world.getBlockState(voidPos);
            IBlockState pasteState = Blocks.AIR.getDefaultState();
            if (blockState.getBlock() == ModBlocks.constructionBlock) {
                TileEntity te = world.getTileEntity(voidPos);
                if (te instanceof ConstructionBlockTileEntity) {
                    pasteState = ((ConstructionBlockTileEntity) te).getActualBlockState();
                }
            }
            boolean success = destroyBlock(world, voidPos, player);
            if (success)
                posStateMap.put(voidPos, blockState);
            if (pasteState != Blocks.AIR.getDefaultState()) {
                pasteStateMap.put(voidPos, pasteState);
            }
        }
        if (posStateMap.size() > 0) {
            BlockPos startPos = (getAnchor(stack) == null) ? pos : getAnchor(stack);
            storeUndo(world, posStateMap, pasteStateMap, startPos, stack, player);
        }
    }

    public static void storeUndo(World world, Map<BlockPos, IBlockState> posStateMap, Map<BlockPos, IBlockState> pasteStateMap, BlockPos startBlock, ItemStack stack, EntityPlayer player) {
        WorldSave worldSave = WorldSave.getWorldSaveDestruction(world);
        NBTTagCompound tagCompound = new NBTTagCompound();
        List<Integer> posIntArrayList = new ArrayList<Integer>();
        List<Integer> stateIntArrayList = new ArrayList<Integer>();
        List<Integer> pastePosArrayList = new ArrayList<Integer>();
        List<Integer> pasteStateArrayList = new ArrayList<Integer>();
        BlockMapIntState blockMapIntState = new BlockMapIntState();
        String UUID = getUUID(stack);

        for (Map.Entry<BlockPos, IBlockState> entry : posStateMap.entrySet()) {
            posIntArrayList.add(GadgetUtils.relPosToInt(startBlock, entry.getKey()));
            blockMapIntState.addToMap(entry.getValue());
            stateIntArrayList.add((int) blockMapIntState.findSlot(entry.getValue()));
            if (pasteStateMap.containsKey(entry.getKey())) {
                pastePosArrayList.add(GadgetUtils.relPosToInt(startBlock, entry.getKey()));
                IBlockState pasteBlockState = pasteStateMap.get(entry.getKey());
                blockMapIntState.addToMap(pasteBlockState);
                pasteStateArrayList.add((int) blockMapIntState.findSlot(pasteBlockState));
            }
        }
        tagCompound.setTag("mapIntState", blockMapIntState.putIntStateMapIntoNBT());
        int[] posIntArray = posIntArrayList.stream().mapToInt(i -> i).toArray();
        int[] stateIntArray = stateIntArrayList.stream().mapToInt(i -> i).toArray();
        int[] posPasteArray = pastePosArrayList.stream().mapToInt(i -> i).toArray();
        int[] statePasteArray = pasteStateArrayList.stream().mapToInt(i -> i).toArray();
        tagCompound.setIntArray("posIntArray", posIntArray);
        tagCompound.setIntArray("stateIntArray", stateIntArray);
        tagCompound.setIntArray("posPasteArray", posPasteArray);
        tagCompound.setIntArray("statePasteArray", statePasteArray);
        tagCompound.setTag("startPos", NBTUtil.createPosTag(startBlock));
        tagCompound.setInteger("dim", player.dimension);
        tagCompound.setString("UUID", UUID);
        worldSave.addToMap(UUID, tagCompound);
        worldSave.markForSaving();
    }

    public void undo(EntityPlayer player, ItemStack stack) {
        World world = player.world;
        WorldSave worldSave = WorldSave.getWorldSaveDestruction(world);
        NBTTagCompound tagCompound = worldSave.getCompoundFromUUID(getUUID(stack));
        if (tagCompound == null) return;
        BlockPos startPos = NBTUtil.getPosFromTag(tagCompound.getCompoundTag("startPos"));
        if (startPos == null) return;
        int[] posIntArray = tagCompound.getIntArray("posIntArray");
        int[] stateIntArray = tagCompound.getIntArray("stateIntArray");
        int[] posPasteArray = tagCompound.getIntArray("posPasteArray");
        int[] statePasteArray = tagCompound.getIntArray("statePasteArray");

        NBTTagList MapIntStateTag = (NBTTagList) tagCompound.getTag("mapIntState");
        if (MapIntStateTag == null) {
            return;
        }
        BlockMapIntState MapIntState = new BlockMapIntState();
        MapIntState.getIntStateMapFromNBT(MapIntStateTag);
        boolean success = false;
        for (int i = 0; i < posIntArray.length; i++) {
            BlockPos placePos = GadgetUtils.relIntToPos(startPos, posIntArray[i]);
            IBlockState currentState = world.getBlockState(placePos);
            if (currentState.getMaterial() == Material.AIR || currentState.getMaterial().isLiquid()) {
                IBlockState placeState = MapIntState.getStateFromSlot((short) stateIntArray[i]);
                if (placeState.getBlock() == ModBlocks.constructionBlock) {
                    IBlockState pasteState = Blocks.AIR.getDefaultState();
                    for (int j = 0; j < posPasteArray.length; j++) {
                        if (posPasteArray[j] == posIntArray[i]) {
                            pasteState = MapIntState.getStateFromSlot((short) statePasteArray[j]);
                            break;
                        }
                    }
                    if (pasteState != Blocks.AIR.getDefaultState()) {
                        world.spawnEntity(new BlockBuildEntity(world, placePos, player, pasteState, 1, pasteState, true));
                        success = true;
                    }
                } else {
                    world.spawnEntity(new BlockBuildEntity(world, placePos, player, placeState, 1, placeState, false));
                    success = true;
                }
            }
        }
        if (success) {
            NBTTagCompound newTag = new NBTTagCompound();
            worldSave.addToMap(getUUID(stack), newTag);
            worldSave.markForSaving();
        }
    }

    private boolean destroyBlock(World world, BlockPos voidPos, EntityPlayer player) {
        ItemStack tool = getGadget(player);
        if (tool.isEmpty())
            return false;

        if( !this.canUse(tool, player) )
            return false;

        this.applyDamage(tool, player);

        world.spawnEntity(new BlockBuildEntity(world, voidPos, player, world.getBlockState(voidPos), 2, Blocks.AIR.getDefaultState(), false));
        return true;
    }

    public static ItemStack getGadget(EntityPlayer player) {
        ItemStack stack = GadgetGeneric.getGadget(player);
        if (!(stack.getItem() instanceof GadgetDestruction))
            return ItemStack.EMPTY;

        return stack;
    }
}
