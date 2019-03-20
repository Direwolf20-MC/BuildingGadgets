package com.direwolf20.buildinggadgets.common.items.gadgets;

import com.direwolf20.buildinggadgets.client.events.EventTooltip;
import com.direwolf20.buildinggadgets.client.gui.GuiMod;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlock;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.common.items.ITemplate;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketBlockMap;
import com.direwolf20.buildinggadgets.common.network.packets.PacketRotateMirror;
import com.direwolf20.buildinggadgets.common.registry.objects.BGItems;
import com.direwolf20.buildinggadgets.common.tools.NetworkIO;
import com.direwolf20.buildinggadgets.common.tools.ToolRenders;
import com.direwolf20.buildinggadgets.common.tools.UniqueItem;
import com.direwolf20.buildinggadgets.common.utils.CapabilityUtil.EnergyUtil;
import com.direwolf20.buildinggadgets.common.utils.GadgetUtils;
import com.direwolf20.buildinggadgets.common.utils.blocks.BlockMap;
import com.direwolf20.buildinggadgets.common.utils.blocks.BlockMapIntState;
import com.direwolf20.buildinggadgets.common.utils.helpers.InventoryHelper;
import com.direwolf20.buildinggadgets.common.utils.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.utils.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.world.WorldSave;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GadgetCopyPaste extends GadgetPlacing implements ITemplate {

    public enum ToolMode {
        Copy, Paste;
        private static ToolMode[] vals = values();

        public ToolMode next() {
            return vals[(this.ordinal() + 1) % vals.length];
        }
    }

    public GadgetCopyPaste(Properties builder) {
        super(builder);
    }

    @Override
    public int getEnergyMax() {
        return Config.GADGETS.GADGET_COPY_PASTE.maxEnergy.get();
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return Config.GADGETS.poweredByFE.get() ? 0 : Config.GADGETS.GADGET_COPY_PASTE.durability.get();
    }

    @Override
    public int getEnergyCost(ItemStack tool) {
        return Config.GADGETS.GADGET_COPY_PASTE.energyCost.get();
    }

    @Override
    public int getDamageCost(ItemStack tool) {
        return Config.GADGETS.GADGET_COPY_PASTE.durabilityCost.get();
    }

    private static void setAnchor(ItemStack stack, BlockPos anchorPos) {
        GadgetUtils.writePOSToNBT(stack, anchorPos, NBTKeys.GADGET_ANCHOR);
    }

    public static void setX(ItemStack stack, int horz) {
        GadgetUtils.writeIntToNBT(stack, horz, NBTKeys.POSITION_X);
    }

    public static void setY(ItemStack stack, int vert) {
        GadgetUtils.writeIntToNBT(stack, vert, NBTKeys.POSITION_Y);
    }

    public static void setZ(ItemStack stack, int depth) {
        GadgetUtils.writeIntToNBT(stack, depth, NBTKeys.POSITION_Z);
    }

    public static int getX(ItemStack stack) {
        return GadgetUtils.getIntFromNBT(stack, NBTKeys.POSITION_X);
    }

    public static int getY(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTag();
        return (tagCompound == null || !tagCompound.hasKey(NBTKeys.POSITION_Y)) ? 1 : tagCompound.getInt(NBTKeys.POSITION_Y);
    }

    public static int getZ(ItemStack stack) {
        return GadgetUtils.getIntFromNBT(stack, NBTKeys.POSITION_Z);
    }

    public static BlockPos getAnchor(ItemStack stack) {
        return GadgetUtils.getPOSFromNBT(stack, NBTKeys.GADGET_ANCHOR);
    }

    @Override
    public WorldSave getWorldSave(World world) {
        return WorldSave.getWorldSave(world);
    }

    @Override
    @Nullable
    public String getUUID(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTag();
        if (tagCompound == null) {
            return null;
        }
        String uuid = tagCompound.getString(NBTKeys.GADGET_UUID);
        if (uuid.isEmpty()) {
            if (getStartPos(stack) == null && getEndPos(stack) == null) {
                return null;
            }
            UUID uid = UUID.randomUUID();
            tagCompound.setString(NBTKeys.GADGET_UUID, uid.toString());
            stack.setTag(tagCompound);
            uuid = uid.toString();
        }
        return uuid;
    }

    private static void setLastBuild(ItemStack stack, BlockPos anchorPos, DimensionType dim) {
        GadgetUtils.writePOSToNBT(stack, anchorPos, NBTKeys.GADGET_LAST_BUILD_POS, dim);
    }

    private static BlockPos getLastBuild(ItemStack stack) {
        return GadgetUtils.getPOSFromNBT(stack, NBTKeys.GADGET_LAST_BUILD_POS);
    }

    @Nullable
    private static ResourceLocation getLastBuildDim(ItemStack stack) {
        return GadgetUtils.getDIMFromNBT(stack, NBTKeys.GADGET_LAST_BUILD_DIM);
    }

    public static List<BlockMap> getBlockMapList(@Nullable NBTTagCompound tagCompound) {
        return getBlockMapList(tagCompound, GadgetUtils.getPOSFromNBT(tagCompound, NBTKeys.GADGET_START_POS));
    }

    private static List<BlockMap> getBlockMapList(@Nullable NBTTagCompound tagCompound, BlockPos startBlock) {
        List<BlockMap> blockMap = new ArrayList<BlockMap>();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        NBTTagList MapIntStateTag = (NBTTagList) tagCompound.getTag(NBTKeys.MAP_INT_STATE);
        if (MapIntStateTag == null) {
            MapIntStateTag = new NBTTagList();
        }
        BlockMapIntState MapIntState = new BlockMapIntState();
        MapIntState.getIntStateMapFromNBT(MapIntStateTag);
        int[] posIntArray = tagCompound.getIntArray(NBTKeys.MAP_POS_INT);
        int[] stateIntArray = tagCompound.getIntArray(NBTKeys.MAP_STATE_INT);
        for (int i = 0; i < posIntArray.length; i++) {
            int p = posIntArray[i];
            BlockPos pos = GadgetUtils.relIntToPos(startBlock, p);
            short IntState = (short) stateIntArray[i];
            blockMap.add(new BlockMap(pos, MapIntState.getStateFromSlot(IntState), (byte) ((p & 0xff0000) >> 16), (byte) ((p & 0x00ff00) >> 8), (byte) (p & 0x0000ff)));
        }
        return blockMap;
    }

    public static BlockMapIntState getBlockMapIntState(@Nullable NBTTagCompound tagCompound) {
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        NBTTagList MapIntStateTag = (NBTTagList) tagCompound.getTag(NBTKeys.MAP_INT_STATE);
        if (MapIntStateTag == null) {
            MapIntStateTag = new NBTTagList();
        }
        NBTTagList MapIntStackTag = (NBTTagList) tagCompound.getTag(NBTKeys.MAP_INT_STACK);
        if (MapIntStackTag == null) {
            MapIntStackTag = new NBTTagList();
        }
        BlockMapIntState MapIntState = new BlockMapIntState();
        MapIntState.getIntStateMapFromNBT(MapIntStateTag);
        MapIntState.getIntStackMapFromNBT(MapIntStackTag);
        return MapIntState;
    }

    private static void setToolMode(ItemStack stack, ToolMode mode) {
        NBTTagCompound tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        tagCompound.setString(NBTKeys.GADGET_MODE, mode.name());
        stack.setTag(tagCompound);
    }

    public static ToolMode getToolMode(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTag();
        ToolMode mode = ToolMode.Copy;
        if (tagCompound == null) {
            setToolMode(stack, mode);
            return mode;
        }
        try {
            mode = ToolMode.valueOf(tagCompound.getString(NBTKeys.GADGET_MODE));
        } catch (Exception e) {
            setToolMode(stack, mode);
        }
        return mode;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        tooltip.add(new TextComponentString(TextFormatting.AQUA + I18n.format("tooltip.gadget.mode") + ": " + getToolMode(stack)));
        addEnergyInformation(tooltip, stack);
        addInformationRayTraceFluid(tooltip, stack);
        EventTooltip.addTemplatePadding(stack, tooltip);
    }


    public void setMode(EntityPlayer player, ItemStack heldItem, int modeInt) {
        //Called when we specify a mode with the radial menu
        ToolMode mode = ToolMode.values()[modeInt];
        setToolMode(heldItem, mode);
        player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.toolmode").getUnformattedComponentText() + ": " + mode), true);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        player.setActiveHand(hand);
        BlockPos pos = VectorHelper.getPosLookingAt(player, stack);
        if (!world.isRemote) {
            if (pos != null && player.isSneaking() && GadgetUtils.setRemoteInventory(stack, player, world, pos, false) == EnumActionResult.SUCCESS)
                return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);

            if (getToolMode(stack) == ToolMode.Copy) {
                if (pos == null) {
                    //TODO Remove debug code
                    EnergyUtil.getCap(stack).ifPresent(energy -> energy.receiveEnergy(105000, false));
                    //setStartPos(stack, null);
                    //setEndPos(stack, null);
                    //player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.areareset").getUnformattedComponentText()), true);
                    return new ActionResult<>(EnumActionResult.SUCCESS, stack);
                }
                if (player.isSneaking()) {
                    if (getStartPos(stack) != null)
                        copyBlocks(stack, player, world, getStartPos(stack), pos);
                    else
                        setEndPos(stack, pos);
                } else {
                    if (getEndPos(stack) != null)
                        copyBlocks(stack, player, world, pos, getEndPos(stack));
                    else
                        setStartPos(stack, pos);
                }
            } else if (getToolMode(stack) == ToolMode.Paste) {
                if (!player.isSneaking()) {
                    if (getAnchor(stack) == null) {
                        if (pos == null) return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
                        buildBlockMap(world, pos, stack, player);
                    } else {
                        BlockPos startPos = getAnchor(stack);
                        buildBlockMap(world, startPos, stack, player);
                    }
                }
            }
        } else {
            if (pos != null && player.isSneaking()) {
                if (GadgetUtils.getRemoteInventory(pos, world, NetworkIO.Operation.EXTRACT) != null)
                    return new ActionResult<>(EnumActionResult.SUCCESS, stack);
            }
            if (getToolMode(stack) == ToolMode.Copy) {
                if (pos == null && player.isSneaking())
                    GuiMod.COPY.openScreen(player);
            } else if (player.isSneaking()) {
                GuiMod.PASTE.openScreen(player);
            } else {
                ToolRenders.updateInventoryCache();
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    public static void rotateOrMirrorBlocks(ItemStack stack, EntityPlayer player, PacketRotateMirror.Operation operation) {
        if (!(getToolMode(stack) == ToolMode.Paste)) return;
        if (player.world.isRemote) {
            return;
        }
        GadgetCopyPaste tool = BGItems.gadgetCopyPaste;
        List<BlockMap> blockMapList;
        WorldSave worldSave = WorldSave.getWorldSave(player.world);
        NBTTagCompound tagCompound = worldSave.getCompoundFromUUID(tool.getUUID(stack));
        BlockPos startPos = tool.getStartPos(stack);
        if (startPos == null) return;
        blockMapList = getBlockMapList(tagCompound);
        List<Integer> posIntArrayList = new ArrayList<Integer>();
        List<Integer> stateIntArrayList = new ArrayList<Integer>();
        BlockMapIntState blockMapIntState = new BlockMapIntState();

        for (BlockMap blockMap : blockMapList) {
            BlockPos tempPos = blockMap.pos;

            int px = (tempPos.getX() - startPos.getX());
            int pz = (tempPos.getZ() - startPos.getZ());
            int nx, nz;
            IBlockState alteredState = GadgetUtils.rotateOrMirrorBlock(player, operation, blockMap.state);
            if (operation == PacketRotateMirror.Operation.MIRROR) {
                if (player.getHorizontalFacing().getAxis() == Axis.X) {
                    nx = px;
                    nz = -pz;
                } else {
                    nx = -px;
                    nz = pz;
                }
            } else {
                nx = -pz;
                nz = px;
            }
            BlockPos newPos = new BlockPos(startPos.getX() + nx, tempPos.getY(), startPos.getZ() + nz);
            posIntArrayList.add(GadgetUtils.relPosToInt(startPos, newPos));
            blockMapIntState.addToMap(alteredState);
            stateIntArrayList.add((int) blockMapIntState.findSlot(alteredState));
            UniqueItem uniqueItem = BlockMapIntState.blockStateToUniqueItem(alteredState, player, tempPos);
            blockMapIntState.addToStackMap(uniqueItem, alteredState);
        }
        int[] posIntArray = posIntArrayList.stream().mapToInt(i -> i).toArray();
        int[] stateIntArray = stateIntArrayList.stream().mapToInt(i -> i).toArray();
        tagCompound.setTag(NBTKeys.MAP_INT_STATE, blockMapIntState.putIntStateMapIntoNBT());
        tagCompound.setTag(NBTKeys.MAP_INT_STACK, blockMapIntState.putIntStackMapIntoNBT());
        tagCompound.setIntArray(NBTKeys.MAP_POS_INT, posIntArray);
        tagCompound.setIntArray(NBTKeys.MAP_STATE_INT, stateIntArray);
        tool.incrementCopyCounter(stack);
        tagCompound.setInt(NBTKeys.TEMPLATE_COPY_COUNT, tool.getCopyCounter(stack));
        worldSave.addToMap(tool.getUUID(stack), tagCompound);
        worldSave.markForSaving();
        PacketHandler.sendTo(new PacketBlockMap(tagCompound), (EntityPlayerMP) player);
        player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA
                + new TextComponentTranslation("message.gadget." + (player.isSneaking() ? "mirrored" : "rotated")).getUnformattedComponentText()), true);
    }

    public static void copyBlocks(ItemStack stack, EntityPlayer player, World world, BlockPos startPos, BlockPos endPos) {
        if (startPos != null && endPos != null) {
            GadgetCopyPaste tool = BGItems.gadgetCopyPaste;
            if (findBlocks(world, startPos, endPos, stack, player, tool)) {
                tool.setStartPos(stack, startPos);
                tool.setEndPos(stack, endPos);
            }
        }
    }

    private static boolean findBlocks(World world, BlockPos start, BlockPos end, ItemStack stack, EntityPlayer player, GadgetCopyPaste tool) {
        setLastBuild(stack, null, DimensionType.OVERWORLD);
        int foundTE = 0;
        int startX = start.getX();
        int startY = start.getY();
        int startZ = start.getZ();

        int endX = end.getX();
        int endY = end.getY();
        int endZ = end.getZ();

        if (Math.abs(startX - endX) >= 125 || Math.abs(startY - endY) >= 125 || Math.abs(startZ - endZ) >= 125) {
            player.sendStatusMessage(new TextComponentString(TextFormatting.RED + new TextComponentTranslation("message.gadget.toobigarea").getUnformattedComponentText()), true);
            return false;
        }

        int iStartX = startX < endX ? startX : endX;
        int iStartY = startY < endY ? startY : endY;
        int iStartZ = startZ < endZ ? startZ : endZ;
        int iEndX = startX < endX ? endX : startX;
        int iEndY = startY < endY ? endY : startY;
        int iEndZ = startZ < endZ ? endZ : startZ;
        WorldSave worldSave = WorldSave.getWorldSave(world);
        NBTTagCompound tagCompound = new NBTTagCompound();
        List<Integer> posIntArrayList = new ArrayList<Integer>();
        List<Integer> stateIntArrayList = new ArrayList<Integer>();
        BlockMapIntState blockMapIntState = new BlockMapIntState();
        Multiset<UniqueItem> itemCountMap = HashMultiset.create();

        int blockCount = 0;

        for (int x = iStartX; x <= iEndX; x++) {
            for (int y = iStartY; y <= iEndY; y++) {
                for (int z = iStartZ; z <= iEndZ; z++) {
                    BlockPos tempPos = new BlockPos(x, y, z);
                    IBlockState tempState = world.getBlockState(tempPos);
                    if (tempState != Blocks.AIR.getDefaultState() && (world.getTileEntity(tempPos) == null || world.getTileEntity(tempPos) instanceof ConstructionBlockTileEntity) && !tempState.getMaterial().isLiquid() && Config.BLACKLIST.isAllowedBlock(tempState.getBlock())) {
                        TileEntity te = world.getTileEntity(tempPos);
                        IBlockState assignState = InventoryHelper.getSpecificStates(tempState, world, player, tempPos, stack);
                        IBlockState actualState = assignState.getExtendedState(world, tempPos);
                        if (te instanceof ConstructionBlockTileEntity) {
                            actualState = ((ConstructionBlockTileEntity) te).getActualBlockState();
                        }
                        if (actualState != null) {
                            UniqueItem uniqueItem = BlockMapIntState.blockStateToUniqueItem(actualState, player, tempPos);
                            if (uniqueItem.getItem() != Items.AIR) {
                                posIntArrayList.add(GadgetUtils.relPosToInt(start, tempPos));
                                blockMapIntState.addToMap(actualState);
                                stateIntArrayList.add((int) blockMapIntState.findSlot(actualState));

                                blockMapIntState.addToStackMap(uniqueItem, actualState);
                                blockCount++;
                                if (blockCount > 32768) {
                                    player.sendStatusMessage(new TextComponentString(TextFormatting.RED + new TextComponentTranslation("message.gadget.toomanyblocks").getUnformattedComponentText()), true);
                                    return false;
                                }
                                NonNullList<ItemStack> drops = NonNullList.create();
                                if (actualState != null)
                                    actualState.getBlock().getDrops(actualState, drops, world, new BlockPos(0, 0, 0), 0);

                                int neededItems = 0;
                                for (ItemStack drop : drops) {
                                    if (drop.getItem().equals(uniqueItem.getItem())) {
                                        neededItems++;
                                    }
                                }
                                if (neededItems == 0) {
                                    neededItems = 1;
                                }
                                itemCountMap.add(uniqueItem,neededItems);
                            }
                        }
                    } else if ((world.getTileEntity(tempPos) != null) && !(world.getTileEntity(tempPos) instanceof ConstructionBlockTileEntity)) {
                        foundTE++;
                    }
                }
            }
        }
        tool.setItemCountMap(stack, itemCountMap);
        tagCompound.setTag(NBTKeys.MAP_INT_STATE, blockMapIntState.putIntStateMapIntoNBT());
        tagCompound.setTag(NBTKeys.MAP_INT_STACK, blockMapIntState.putIntStackMapIntoNBT());
        int[] posIntArray = posIntArrayList.stream().mapToInt(i -> i).toArray();
        int[] stateIntArray = stateIntArrayList.stream().mapToInt(i -> i).toArray();
        tagCompound.setIntArray(NBTKeys.MAP_POS_INT, posIntArray);
        tagCompound.setIntArray(NBTKeys.MAP_STATE_INT, stateIntArray);

        tagCompound.setTag(NBTKeys.GADGET_START_POS, NBTUtil.writeBlockPos(start));
        tagCompound.setTag(NBTKeys.GADGET_END_POS, NBTUtil.writeBlockPos(end));
        tagCompound.setString(NBTKeys.GADGET_DIM, player.dimension.toString());
        tagCompound.setString(NBTKeys.GADGET_UUID, tool.getUUID(stack));
        tool.incrementCopyCounter(stack);
        tagCompound.setInt(NBTKeys.TEMPLATE_COPY_COUNT, tool.getCopyCounter(stack));

        worldSave.addToMap(tool.getUUID(stack), tagCompound);
        worldSave.markForSaving();
        PacketHandler.sendTo(new PacketBlockMap(tagCompound), (EntityPlayerMP) player);

        if (foundTE > 0) {
            player.sendStatusMessage(new TextComponentString(TextFormatting.YELLOW + new TextComponentTranslation("message.gadget.TEinCopy").getUnformattedComponentText() + ": " + foundTE), true);
        } else {
            player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.copied").getUnformattedComponentText()), true);
        }
        return true;
    }

    private void buildBlockMap(World world, BlockPos startPos, ItemStack stack, EntityPlayer player) {
//        long time = System.nanoTime();

        BlockPos anchorPos = getAnchor(stack);
        BlockPos pos = anchorPos == null ? startPos : anchorPos;
        NBTTagCompound tagCompound = WorldSave.getWorldSave(world).getCompoundFromUUID(getUUID(stack));

        pos = pos.up(GadgetCopyPaste.getY(stack));
        pos = pos.east(GadgetCopyPaste.getX(stack));
        pos = pos.south(GadgetCopyPaste.getZ(stack));

        List<BlockMap> blockMapList = getBlockMapList(tagCompound, pos);
        setLastBuild(stack, pos, player.dimension);

        for (BlockMap blockMap : blockMapList)
            placeBlock(world, blockMap.pos, player, blockMap.state, getBlockMapIntState(tagCompound).getIntStackMap());

        setAnchor(stack, null);
        //System.out.printf("Built %d Blocks in %.2f ms%n", blockMapList.size(), (System.nanoTime() - time) * 1e-6);
    }

    private void placeBlock(World world, BlockPos pos, EntityPlayer player, IBlockState state, Map<IBlockState, UniqueItem> IntStackMap) {
        IBlockState testState = world.getBlockState(pos);
        // @warning: this has been replaced without knowing how to construct a BlockItemUseContent
        //TODO Put this back into place once someone figures out how its supposed to work :)
        // if ((Config.GENERAL.allowOverwriteBlocks.get() && !testState.isReplaceable(new BlockItemUseContext(world, player, new ItemStack(testState.getBlock()), pos, EnumFacing.DOWN, 0.5F, 0.0F, 0.5F))) ||
        //     (!Config.GENERAL.allowOverwriteBlocks.get() && world.getBlockState(pos).getMaterial() != Material.AIR))
        if (world.getBlockState(pos).getMaterial() != Material.AIR)
            return;

        if (pos.getY() < 0 || state.equals(Blocks.AIR.getDefaultState()) || !player.isAllowEdit())
            return;

        ItemStack heldItem = getGadget(player);
        if (heldItem.isEmpty())
            return;

        if (BGItems.gadgetCopyPaste.getStartPos(heldItem) == null ||BGItems.gadgetCopyPaste.getEndPos(heldItem) == null)
            return;

        UniqueItem uniqueItem = IntStackMap.get(state);
        if (uniqueItem == null) return; //This shouldn't happen I hope!
        ItemStack itemStack = new ItemStack(uniqueItem.getItem(), 1);
        NonNullList<ItemStack> drops = NonNullList.create();
        state.getBlock().getDrops(state, drops, world, pos, 0);
        int neededItems = 0;
        for (ItemStack drop : drops) {
            if (drop.getItem().equals(itemStack.getItem())) {
                neededItems++;
            }
        }
        if (neededItems == 0) {
            neededItems = 1;
        }
        if (!world.isBlockModifiable(player, pos)) {
            return;
        }
        BlockSnapshot blockSnapshot = BlockSnapshot.getBlockSnapshot(world, pos);
        if (ForgeEventFactory.onPlayerBlockPlace(player, blockSnapshot, EnumFacing.UP, EnumHand.MAIN_HAND).isCanceled()) {
            return;
        }
        ItemStack constructionPaste = new ItemStack(BGItems.constructionPaste);
        boolean useConstructionPaste = false;
        if (InventoryHelper.countItem(itemStack, player, world) < neededItems) {
            if (InventoryHelper.countPaste(player) < neededItems) {
                return;
            }
            itemStack = constructionPaste.copy();
            useConstructionPaste = true;
        }

        if (!this.canUse(heldItem, player))
            return;

        this.applyDamage(heldItem, player);

        boolean useItemSuccess;
        if (useConstructionPaste) {
            useItemSuccess = InventoryHelper.usePaste(player, 1);
        } else {
            useItemSuccess = InventoryHelper.useItem(itemStack, player, neededItems, world);
        }
        if (useItemSuccess) {
            world.spawnEntity(new BlockBuildEntity(world, pos, player, state, 1, state, useConstructionPaste));
        }

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
            player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.anchorrender").getUnformattedComponentText()), true);
        } else {
            setAnchor(stack, null);
            player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.anchorremove").getUnformattedComponentText()), true);
        }
    }

    public static void undoBuild(EntityPlayer player, ItemStack heldItem) {
//        long time = System.nanoTime();
        NBTTagCompound tagCompound = WorldSave.getWorldSave(player.world).getCompoundFromUUID(BGItems.gadgetCopyPaste.getUUID(heldItem));
        World world = player.world;
        if (world.isRemote) {
            return;
        }
        BlockPos startPos = getLastBuild(heldItem);
        if (startPos == null)
            return;

        ItemStack silkTool = heldItem.copy(); //Setup a Silk Touch version of the tool so we can return stone instead of cobblestone, etc.
        silkTool.addEnchantment(Enchantments.SILK_TOUCH, 1);
        List<BlockMap> blockMapList = getBlockMapList(tagCompound, startPos);
        boolean success = true;
        boolean sameDim = (player.dimension == DimensionType.byName(getLastBuildDim(heldItem)));
        for (BlockMap blockMap : blockMapList) {
            double distance = blockMap.pos.getDistance(player.getPosition());

            IBlockState currentBlock = world.getBlockState(blockMap.pos);
            BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(world, blockMap.pos, currentBlock, player);

            boolean cancelled = MinecraftForge.EVENT_BUS.post(e);
            if (distance < 256 && !cancelled && sameDim) { //Don't allow us to undo a block while its still being placed or too far away
                if (currentBlock.getBlock() == blockMap.state.getBlock() || currentBlock.getBlock() instanceof ConstructionBlock) {
                    if (currentBlock.getBlockHardness(world, blockMap.pos) >= 0) {
                        currentBlock.getBlock().harvestBlock(world, player, blockMap.pos, currentBlock, world.getTileEntity(blockMap.pos), silkTool);
                        world.spawnEntity(new BlockBuildEntity(world, blockMap.pos, player, currentBlock, 2, currentBlock, false));
                    }
                }
            } else {
                player.sendStatusMessage(new TextComponentString(TextFormatting.RED + new TextComponentTranslation("message.gadget.undofailed").getUnformattedComponentText()), true);
                success = false;
            }
            //System.out.printf("Undid %d Blocks in %.2f ms%n", blockMapList.size(), (System.nanoTime() - time) * 1e-6);
        }
        if (success) setLastBuild(heldItem, null, DimensionType.OVERWORLD);
    }

    public static ItemStack getGadget(EntityPlayer player) {
        ItemStack stack = GadgetGeneric.getGadget(player);
        if (!(stack.getItem() instanceof GadgetCopyPaste))
            return ItemStack.EMPTY;

        return stack;
    }
}