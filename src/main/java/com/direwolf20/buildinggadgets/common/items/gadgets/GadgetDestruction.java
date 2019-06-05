package com.direwolf20.buildinggadgets.common.items.gadgets;

import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.client.gui.GuiMod;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.common.registry.objects.BGBlocks;
import com.direwolf20.buildinggadgets.common.util.CapabilityUtil.EnergyUtil;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.blocks.BlockMapIntState;
import com.direwolf20.buildinggadgets.common.util.blocks.RegionSnapshot;
import com.direwolf20.buildinggadgets.common.util.exceptions.PaletteOverflowException;
import com.direwolf20.buildinggadgets.common.util.helpers.NBTHelper;
import com.direwolf20.buildinggadgets.common.util.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.world.WorldSave;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.*;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class GadgetDestruction extends GadgetSwapping {

    public static void restoreSnapshotWithBuilder(World world, RegionSnapshot snapshot) {
        Set<BlockPos> pastePositions = snapshot.getTileData().stream()
                .map(Pair::getLeft)
                .collect(Collectors.toSet());
        int index = 0;
        for (BlockPos pos : snapshot.getRegion()) {
            // TODO remove spawnBy field in BlockBuildEntity
            snapshot.getBlockStates().get(index).ifPresent(state -> world.spawnEntity(new BlockBuildEntity(world, pos, null, state, BlockBuildEntity.Mode.PLACE, pastePositions.contains(pos))));
            index++;
        }
    }

    public GadgetDestruction(Properties builder) {
        super(builder);
    }

    @Override
    public int getEnergyMax() {
        return Config.GADGETS.GADGET_DESTRUCTION.maxEnergy.get();
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return Config.GADGETS.poweredByFE.get() ? 0 : Config.GADGETS.GADGET_DESTRUCTION.durability.get();
    }

    @Override
    public int getEnergyCost(ItemStack tool) {
        return Config.GADGETS.GADGET_DESTRUCTION.energyCost.get() * getCostMultiplier(tool);
    }

    @Override
    public int getDamageCost(ItemStack tool) {
        return Config.GADGETS.GADGET_DESTRUCTION.durabilityCost.get() * getCostMultiplier(tool);
    }

    private int getCostMultiplier(ItemStack tool) {
        return (int) (Config.GADGETS.poweredByFE.get() && !getFuzzy(tool) ? Config.GADGETS.GADGET_DESTRUCTION.nonFuzzyMultiplier.get() : 1);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        tooltip.add(TooltipTranslation.GADGET_DESTROYWARNING
                            .componentTranslation()
                            .setStyle(Styles.RED));
        tooltip.add(TooltipTranslation.GADGET_DESTROYSHOWOVERLAY
                            .componentTranslation(String.valueOf(getOverlay(stack)))
                            .setStyle(Styles.AQUA));
        tooltip.add(TooltipTranslation.GADGET_BUILDING_PLACE_ATOP
                            .componentTranslation(String.valueOf(getConnectedArea(stack)))
                            .setStyle(Styles.YELLOW));
        if (Config.isServerConfigLoaded() && Config.GADGETS.GADGET_DESTRUCTION.nonFuzzyEnabled.get())
            tooltip.add(TooltipTranslation.GADGET_FUZZY
                                .componentTranslation(String.valueOf(getFuzzy(stack)))
                                .setStyle(Styles.GOLD));

        addInformationRayTraceFluid(tooltip, stack);
        addEnergyInformation(tooltip, stack);
    }

    @Nullable
    public static String getUUID(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        String uuid = tagCompound.getString(NBTKeys.GADGET_UUID);
        if (uuid.isEmpty()) {
            UUID uid = UUID.randomUUID();
            tagCompound.setString(NBTKeys.GADGET_UUID, uid.toString());
            stack.setTag(tagCompound);
            uuid = uid.toString();
        }
        return uuid;
    }

    public static void setAnchor(ItemStack stack, BlockPos pos) {
        GadgetUtils.writePOSToNBT(stack, pos, NBTKeys.GADGET_ANCHOR);
    }

    public static BlockPos getAnchor(ItemStack stack) {
        return GadgetUtils.getPOSFromNBT(stack, NBTKeys.GADGET_ANCHOR);
    }

    public static void setAnchorSide(ItemStack stack, EnumFacing side) {
        NBTTagCompound tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        if (side == null) {
            if (tagCompound.getTag(NBTKeys.GADGET_ANCHOR_SIDE) != null) {
                tagCompound.removeTag(NBTKeys.GADGET_ANCHOR_SIDE);
                stack.setTag(tagCompound);
            }
            return;
        }
        tagCompound.setString(NBTKeys.GADGET_ANCHOR_SIDE, side.getName());
        stack.setTag(tagCompound);
    }

    public static EnumFacing getAnchorSide(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTag();
        if (tagCompound == null) {
            return null;
        }
        String facing = tagCompound.getString(NBTKeys.GADGET_ANCHOR_SIDE);
        if (facing.isEmpty()) return null;
        return EnumFacing.byName(facing);
    }

    public static void setToolValue(ItemStack stack, int value, String valueName) {
        //Store the tool's range in NBT as an Integer
        NBTTagCompound tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        tagCompound.setInt(valueName, value);
        stack.setTag(tagCompound);
    }

    public static int getToolValue(ItemStack stack, String valueName) {
        //Store the tool's range in NBT as an Integer
        NBTTagCompound tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        return tagCompound.getInt(valueName);
    }

    public static boolean getOverlay(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            tagCompound.setBoolean(NBTKeys.GADGET_OVERLAY, true);
            tagCompound.setBoolean(NBTKeys.GADGET_FUZZY, true);
            stack.setTag(tagCompound);
            return true;
        }
        if (tagCompound.hasKey(NBTKeys.GADGET_OVERLAY)) {
            return tagCompound.getBoolean(NBTKeys.GADGET_OVERLAY);
        }
        tagCompound.setBoolean(NBTKeys.GADGET_OVERLAY, true);
        stack.setTag(tagCompound);
        return true;
    }

    public static void setOverlay(ItemStack stack, boolean showOverlay) {
        NBTTagCompound tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        tagCompound.setBoolean(NBTKeys.GADGET_OVERLAY, showOverlay);
        stack.setTag(tagCompound);
    }

    public static void switchOverlay(EntityPlayer player, ItemStack stack) {
        boolean overlay = !getOverlay(stack);
        setOverlay(stack, overlay);
        player.sendStatusMessage(TooltipTranslation.GADGET_DESTROYSHOWOVERLAY
                                         .componentTranslation(String.valueOf(overlay)).setStyle(Styles.AQUA), true);
    }

    public static List<EnumFacing> assignDirections(EnumFacing side, EntityPlayer player) {
        List<EnumFacing> dirs = new ArrayList<EnumFacing>();
        EnumFacing depth = side.getOpposite();
        boolean vertical = side.getAxis() == Axis.Y;
        EnumFacing up = vertical ? player.getHorizontalFacing() : EnumFacing.UP;
        EnumFacing left = vertical ? up.rotateY() : side.rotateYCCW();
        EnumFacing right = left.getOpposite();
        if (side == EnumFacing.DOWN)
            up = up.getOpposite();

        EnumFacing down = up.getOpposite();
        dirs.add(left);
        dirs.add(right);
        dirs.add(up);
        dirs.add(down);
        dirs.add(depth);
        return dirs;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        player.setActiveHand(hand);
        if (!world.isRemote) {
            if (!player.isSneaking()) {
                RayTraceResult lookingAt = VectorHelper.getLookingAt(player, stack);
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
            } else {
                //TODO Remove debug code
                EnergyUtil.getCap(stack).ifPresent(energy -> energy.receiveEnergy(105000, false));
            }
        } else {
            if (player.isSneaking()) {
                GuiMod.DESTRUCTION.openScreen(player);
                return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
            }
        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    public static void anchorBlocks(EntityPlayer player, ItemStack stack) {
        BlockPos currentAnchor = getAnchor(stack);
        if (currentAnchor == null) {
            RayTraceResult lookingAt = VectorHelper.getLookingAt(player, stack);
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

    public static SortedSet<BlockPos> getArea(World world, BlockPos pos, EnumFacing incomingSide, EntityPlayer player, ItemStack stack) {
        SortedSet<BlockPos> voidPositions = new TreeSet<>(Comparator.comparingInt(Vec3i::getX).thenComparingInt(Vec3i::getY).thenComparingInt(Vec3i::getZ));
        int depth = getToolValue(stack, NBTKeys.GADGET_VALUE_DEPTH);
        if (depth == 0)
            return voidPositions;

        BlockPos startPos = (getAnchor(stack) == null) ? pos : getAnchor(stack);
        EnumFacing side = (getAnchorSide(stack) == null) ? incomingSide : getAnchorSide(stack);
        List<EnumFacing> directions = assignDirections(side, player);
        IBlockState stateTarget = !Config.GADGETS.GADGET_DESTRUCTION.nonFuzzyEnabled.get() || GadgetGeneric.getFuzzy(stack) ? null : world.getBlockState(pos);
        if (GadgetGeneric.getConnectedArea(stack)) {
            String[] directionNames = NBTKeys.GADGET_VALUES;
            AxisAlignedBB area = new AxisAlignedBB(pos);
            for (int i = 0; i < directionNames.length; i++)
                area = area.union(new AxisAlignedBB(pos.offset(directions.get(i), getToolValue(stack, directionNames[i]) - (i == 4 ? 1 : 0))));

            addConnectedCoords(world, player, startPos, stateTarget, voidPositions,
                    (int) area.minX, (int) area.minY, (int) area.minZ, (int) area.maxX - 1, (int) area.maxY - 1, (int) area.maxZ - 1);
        } else {
            int left = -getToolValue(stack, NBTKeys.GADGET_VALUE_LEFT);
            int right = getToolValue(stack, NBTKeys.GADGET_VALUE_RIGHT);
            int down = -getToolValue(stack, NBTKeys.GADGET_VALUE_DOWN);
            int up = getToolValue(stack, NBTKeys.GADGET_VALUE_UP);
            for (int d = 0; d < depth; d++) {
                for (int x = left; x <= right; x++) {
                    for (int y = down; y <= up; y++) {
                        BlockPos voidPos = new BlockPos(startPos);
                        voidPos = voidPos.offset(directions.get(0), x);
                        voidPos = voidPos.offset(directions.get(2), y);
                        voidPos = voidPos.offset(directions.get(4), d);
                        if (validBlock(world, voidPos, player, stateTarget))
                            voidPositions.add(voidPos);
                    }
                }
            }
        }
        return voidPositions;
    }

    public static void addConnectedCoords(World world, EntityPlayer player, BlockPos loc, IBlockState state,
            SortedSet<BlockPos> coords, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        if (coords.contains(loc) || loc.getX() < minX || loc.getY() < minY || loc.getZ() < minZ || loc.getX() > maxX || loc.getY() > maxY || loc.getZ() > maxZ)
            return;

        if (!validBlock(world, loc, player, state))
            return;

        coords.add(loc);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    addConnectedCoords(world, player, loc.add(x, y, z), state, coords, minX, minY, minZ, maxX, maxY, maxZ);
                }
            }
        }
    }

    public static boolean validBlock(World world, BlockPos voidPos, EntityPlayer player, @Nullable IBlockState stateTarget) {
        IBlockState currentBlock = world.getBlockState(voidPos);
        if (stateTarget != null && currentBlock != stateTarget) return false;
        TileEntity te = world.getTileEntity(voidPos);
        if (currentBlock.getBlock().isAir(currentBlock, world, voidPos)) return false;
        //if (currentBlock.getBlock().getMaterial(currentBlock).isLiquid()) return false;
        if (currentBlock.equals(BGBlocks.effectBlock.getDefaultState())) return false;
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
            if (ForgeEventFactory.onBlockPlace(player, blockSnapshot, EnumFacing.UP)) {
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
        EnumFacing directionLeft = side.rotateYCCW();
        EnumFacing directionRight = side.rotateY();
        BlockPos first = pos.offset(directionLeft, getToolValue(stack, NBTKeys.GADGET_VALUE_LEFT));
        BlockPos second = pos.offset(directionRight, getToolValue(stack, NBTKeys.GADGET_VALUE_RIGHT))
                .offset(side, getToolValue(stack, NBTKeys.GADGET_VALUE_DEPTH));
        // The number are not necessarily sorted min and max, but the constructor will do it for us
        Region region = new Region(
                first.getX(),
                pos.getY() - getToolValue(stack, NBTKeys.GADGET_VALUE_DOWN),
                first.getZ(),
                second.getX(),
                pos.getY() + getToolValue(stack, NBTKeys.GADGET_VALUE_UP),
                second.getZ());

        RegionSnapshot snapshot;
        try {
            snapshot = RegionSnapshot.select(world, region)
                    .excludeAir()
                    .checkTiles((p, state, tile) -> state.getBlock() == BGBlocks.constructionBlock && tile instanceof ConstructionBlockTileEntity)
                    .build();
        } catch (PaletteOverflowException e) {
            player.sendMessage(new TextComponentTranslation(TooltipTranslation.GADGET_PALETTE_OVERFLOW.getTranslationKey()));
            return;
        }
        snapshot.getRegion().forEach(p -> world.setBlockState(p, Blocks.AIR.getDefaultState()));

        WorldSave worldSave = WorldSave.getWorldSaveDestruction(world);
        worldSave.addToMap(getUUID(stack), snapshot.serialize());

        // SortedSet<BlockPos> voidPosArray = getArea(world, pos, side, player, stack);
        // Map<BlockPos, IBlockState> posStateMap = new HashMap<BlockPos, IBlockState>();
        // Map<BlockPos, IBlockState> pasteStateMap = new HashMap<BlockPos, IBlockState>();
        // for (BlockPos voidPos : voidPosArray) {
        //     IBlockState blockState = world.getBlockState(voidPos);
        //     IBlockState pasteState = Blocks.AIR.getDefaultState();
        //     if (blockState.getBlock() == BGBlocks.constructionBlock) {
        //         TileEntity te = world.getTileEntity(voidPos);
        //         if (te instanceof ConstructionBlockTileEntity) {
        //             pasteState = ((ConstructionBlockTileEntity) te).getActualBlockState();
        //         }
        //     }
        //     boolean success = destroyBlock(world, voidPos, player);
        //     if (success)
        //         posStateMap.put(voidPos, blockState);
        //     if (pasteState != Blocks.AIR.getDefaultState()) {
        //         pasteStateMap.put(voidPos, pasteState);
        //     }
        // }
        // if (posStateMap.size() > 0) {
        //     BlockPos startPos = (getAnchor(stack) == null) ? pos : getAnchor(stack);
        //     storeUndo(world, posStateMap, pasteStateMap, startPos, stack, player);
        // }
    }

    public static void storeUndo(World world, Map<BlockPos, IBlockState> posStateMap, Map<BlockPos, IBlockState> pasteStateMap, BlockPos startBlock, ItemStack stack, EntityPlayer player) {
        WorldSave worldSave = WorldSave.getWorldSaveDestruction(world);
        NBTTagCompound tagCompound = new NBTTagCompound();
        IntList posIntArrayList = new IntArrayList();
        List<IBlockState> intStateArrayList = new ArrayList<>();
        IntList stateIntArrayList = new IntArrayList();
        IntList pastePosArrayList = new IntArrayList();
        IntList pasteStateArrayList = new IntArrayList();
        BlockMapIntState blockMapIntState = new BlockMapIntState();
        String UUID = getUUID(stack);

        for (Map.Entry<BlockPos, IBlockState> entry : posStateMap.entrySet()) {
            posIntArrayList.add(GadgetUtils.relPosToInt(startBlock, entry.getKey()));
            blockMapIntState.addToMap(entry.getValue());
            stateIntArrayList.add((int) blockMapIntState.findSlot(entry.getValue()));
            intStateArrayList.add(entry.getValue());
            if (pasteStateMap.containsKey(entry.getKey())) {
                pastePosArrayList.add(GadgetUtils.relPosToInt(startBlock, entry.getKey()));
                IBlockState pasteBlockState = pasteStateMap.get(entry.getKey());
                blockMapIntState.addToMap(pasteBlockState);
                pasteStateArrayList.add((int) blockMapIntState.findSlot(pasteBlockState));
            }
        }
        tagCompound.setTag(NBTKeys.MAP_INDEX2STATE_ID, blockMapIntState.putIntStateMapIntoNBT());
        tagCompound.setIntArray(NBTKeys.MAP_INDEX2POS, posIntArrayList.toIntArray());
        tagCompound.setIntArray(NBTKeys.MAP_INDEX2STATE_ID, stateIntArrayList.toIntArray());
        tagCompound.setTag(NBTKeys.MAP_PALETTE, NBTHelper.writeIterable(intStateArrayList, (state, i) -> {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setInt(NBTKeys.MAP_SLOT, i);
            entry.setTag(NBTKeys.MAP_STATE, NBTUtil.writeBlockState(intStateArrayList.get(i)));
            return entry;
        }));
        tagCompound.setIntArray(NBTKeys.MAP_POS_PASTE, pastePosArrayList.toIntArray());
        tagCompound.setIntArray(NBTKeys.MAP_STATE_PASTE, pasteStateArrayList.toIntArray());
        tagCompound.setTag(NBTKeys.GADGET_START_POS, NBTUtil.writeBlockPos(startBlock));
        tagCompound.setString(NBTKeys.GADGET_DIM, DimensionType.func_212678_a(player.dimension).toString());
        tagCompound.setString(NBTKeys.GADGET_UUID, UUID);
        worldSave.addToMap(UUID, tagCompound);
        worldSave.markForSaving();
    }

    public static void undo(EntityPlayer player, ItemStack stack) {
        World world = player.world;
        WorldSave worldSave = WorldSave.getWorldSaveDestruction(world);
        // NBTTagCompound tag = worldSave.getCompoundFromUUID(getUUID(stack));
        // if (tag == null) return;
        //
        // BlockPos startPos = NBTUtil.readBlockPos(tag.getCompound(NBTKeys.GADGET_START_POS));
        // int[] indexPosArray = tag.getIntArray(NBTKeys.MAP_INDEX2POS);
        // int[] indexStateIDArray = tag.getIntArray(NBTKeys.MAP_INDEX2STATE_ID);
        // int[] posPasteArray = tag.getIntArray(NBTKeys.MAP_POS_PASTE);
        // int[] statePasteArray = tag.getIntArray(NBTKeys.MAP_STATE_PASTE);
        //
        // BlockMapIntState intState = new BlockMapIntState();
        // intState.getIntStateMapFromNBT((NBTTagList) tag.getTag(NBTKeys.MAP_PALETTE));
        //
        // boolean success = false;
        // for (int i = 0; i < indexPosArray.length; i++) {
        //     BlockPos placePos = GadgetUtils.relIntToPos(startPos, indexPosArray[i]);
        //     IBlockState currentState = world.getBlockState(placePos);
        //     if (currentState.getBlock().isAir(currentState, world, placePos) || currentState.getMaterial().isLiquid()) {
        //         IBlockState placeState = intState.getStateFromSlot((short) indexStateIDArray[i]);
        //         if (placeState.getBlock() == BGBlocks.constructionBlock) {
        //             IBlockState pasteState = Blocks.AIR.getDefaultState();
        //             for (int j = 0; j < posPasteArray.length; j++) {
        //                 if (posPasteArray[j] == indexPosArray[i]) {
        //                     pasteState = intState.getStateFromSlot((short) statePasteArray[j]);
        //                     break;
        //                 }
        //             }
        //             if (pasteState != Blocks.AIR.getDefaultState()) {
        //                 world.spawnEntity(new BlockBuildEntity(world, placePos, player, pasteState, BlockBuildEntity.Mode.PLACE, true));
        //                 success = true;
        //             }
        //         } else {
        //             world.spawnEntity(new BlockBuildEntity(world, placePos, player, placeState, BlockBuildEntity.Mode.PLACE, false));
        //             success = true;
        //         }
        //     }
        // }
        NBTTagCompound serializedSnapshot = worldSave.getCompoundFromUUID(getUUID(stack));
        RegionSnapshot snapshot = RegionSnapshot.deserialize(serializedSnapshot);
        restoreSnapshotWithBuilder(world, snapshot);
        worldSave.addToMap(getUUID(stack), new NBTTagCompound());
        worldSave.markDirty();
    }

    private boolean destroyBlock(World world, BlockPos voidPos, EntityPlayer player) {
        ItemStack tool = getGadget(player);
        if (tool.isEmpty())
            return false;

        if(!this.canUse(tool, player))
            return false;

        this.applyDamage(tool, player);

        world.spawnEntity(new BlockBuildEntity(world, voidPos, player, world.getBlockState(voidPos), BlockBuildEntity.Mode.REMOVE, false));
        return true;
    }

    public static ItemStack getGadget(EntityPlayer player) {
        ItemStack stack = GadgetGeneric.getGadget(player);
        if (!(stack.getItem() instanceof GadgetDestruction))
            return ItemStack.EMPTY;

        return stack;
    }
}
