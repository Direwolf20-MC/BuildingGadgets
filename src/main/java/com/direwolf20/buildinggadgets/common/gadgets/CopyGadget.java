package com.direwolf20.buildinggadgets.common.gadgets;

import com.direwolf20.buildinggadgets.client.events.EventTooltip;
import com.direwolf20.buildinggadgets.client.screens.GuiProxy;
import com.direwolf20.buildinggadgets.client.renders.AbstractRender;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlock;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import com.direwolf20.buildinggadgets.common.entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.common.items.ITemplate;
import com.direwolf20.buildinggadgets.common.items.ModItems;
import com.direwolf20.buildinggadgets.common.network.PacketBlockMap;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.PacketRotateMirror;
import com.direwolf20.buildinggadgets.common.tools.*;
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
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;

import javax.annotation.Nullable;
import java.util.*;

public class CopyGadget extends AbstractGadget implements ITemplate {

    public enum ToolMode {
        Copy, Paste;
        private static ToolMode[] vals = values();

        public ToolMode next() {
            return vals[(this.ordinal() + 1) % vals.length];
        }
    }

    public CopyGadget() {
        super(
                "copypastetool",
                SyncedConfig.durabilityCopyPaste,
                SyncedConfig.energyCostCopyPaste,
                SyncedConfig.damageCostCopyPaste
        );
    }

    private static void setAnchor(ItemStack stack, BlockPos anchorPos) {
        GadgetUtils.writePOSToNBT(stack, anchorPos, "anchor");
    }

    public static void setX(ItemStack stack, int horz) {
        GadgetUtils.writeIntToNBT(stack, horz, "X");
    }

    public static void setY(ItemStack stack, int vert) {
        GadgetUtils.writeIntToNBT(stack, vert, "Y");
    }

    public static void setZ(ItemStack stack, int depth) {
        GadgetUtils.writeIntToNBT(stack, depth, "Z");
    }

    public static int getX(ItemStack stack) {
        return GadgetUtils.getIntFromNBT(stack, "X");
    }

    public static int getY(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) return 1;
        if (!tagCompound.hasKey("Y")) return 1;
        Integer tagInt = tagCompound.getInteger("Y");
        return tagInt;
    }

    public static int getZ(ItemStack stack) {
        return GadgetUtils.getIntFromNBT(stack, "Z");
    }

    public static BlockPos getAnchor(ItemStack stack) {
        return GadgetUtils.getPOSFromNBT(stack, "anchor");
    }

    @Override
    public WorldSave getWorldSave(World world) {
        return WorldSave.getWorldSave(world);
    }

    @Override
    @Nullable
    public String getUUID(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            return null;
        }
        String uuid = tagCompound.getString("UUID");
        if (uuid.isEmpty()) {
            if (getStartPos(stack) == null && getEndPos(stack) == null) {
                return null;
            }
            UUID uid = UUID.randomUUID();
            tagCompound.setString("UUID", uid.toString());
            stack.setTagCompound(tagCompound);
            uuid = uid.toString();
        }
        return uuid;
    }

    private static void setLastBuild(ItemStack stack, BlockPos anchorPos, Integer dim) {
        GadgetUtils.writePOSToNBT(stack, anchorPos, "lastBuild", dim);
    }

    private static BlockPos getLastBuild(ItemStack stack) {
        return GadgetUtils.getPOSFromNBT(stack, "lastBuild");
    }

    private static Integer getLastBuildDim(ItemStack stack) {
        return GadgetUtils.getDIMFromNBT(stack, "lastBuild");
    }

    public static List<BlockMap> getBlockMapList(@Nullable NBTTagCompound tagCompound) {
        return getBlockMapList(tagCompound, GadgetUtils.getPOSFromNBT(tagCompound, "startPos"));
    }

    private static List<BlockMap> getBlockMapList(@Nullable NBTTagCompound tagCompound, BlockPos startBlock) {
        List<BlockMap> blockMap = new ArrayList<BlockMap>();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        NBTTagList MapIntStateTag = (NBTTagList) tagCompound.getTag("mapIntState");
        if (MapIntStateTag == null) {
            MapIntStateTag = new NBTTagList();
        }
        BlockMapIntState MapIntState = new BlockMapIntState();
        MapIntState.getIntStateMapFromNBT(MapIntStateTag);
        int[] posIntArray = tagCompound.getIntArray("posIntArray");
        int[] stateIntArray = tagCompound.getIntArray("stateIntArray");
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
        NBTTagList MapIntStateTag = (NBTTagList) tagCompound.getTag("mapIntState");
        if (MapIntStateTag == null) {
            MapIntStateTag = new NBTTagList();
        }
        NBTTagList MapIntStackTag = (NBTTagList) tagCompound.getTag("mapIntStack");
        if (MapIntStackTag == null) {
            MapIntStackTag = new NBTTagList();
        }
        BlockMapIntState MapIntState = new BlockMapIntState();
        MapIntState.getIntStateMapFromNBT(MapIntStateTag);
        MapIntState.getIntStackMapFromNBT(MapIntStackTag);
        return MapIntState;
    }

    private static void setToolMode(ItemStack stack, ToolMode mode) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        tagCompound.setString("mode", mode.name());
        stack.setTagCompound(tagCompound);
    }

    public static ToolMode getToolMode(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        ToolMode mode = ToolMode.Copy;
        if (tagCompound == null) {
            setToolMode(stack, mode);
            return mode;
        }
        try {
            mode = ToolMode.valueOf(tagCompound.getString("mode"));
        } catch (Exception e) {
            setToolMode(stack, mode);
        }
        return mode;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag b) {
        super.addInformation(stack, world, list, b);
        list.add(TextFormatting.AQUA + I18n.format("tooltip.gadget.mode") + ": " + getToolMode(stack));

        EventTooltip.addTemplatePadding(stack, list);
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
        RayTraceResult trace = RayTraceHelper.rayTrace(player, AbstractGadget.shouldRayTraceFluid(stack));

        if (!world.isRemote) {
            if (trace != null && player.isSneaking() && GadgetUtils.setRemoteInventory(stack, player, world, trace.getBlockPos(), false) == EnumActionResult.SUCCESS)
                return new ActionResult<>(EnumActionResult.SUCCESS, stack);

            if (getToolMode(stack) == ToolMode.Copy) {
                if( trace != null ) {
                    if (player.isSneaking()) {
                        if (getStartPos(stack) != null)
                            copyBlocks(stack, player, world, getStartPos(stack), trace.getBlockPos());
                        else
                            setEndPos(stack, trace.getBlockPos());
                    } else {
                        if (getEndPos(stack) != null)
                            copyBlocks(stack, player, world, trace.getBlockPos(), getEndPos(stack));
                        else
                            setStartPos(stack, trace.getBlockPos());
                    }
                }
            } else if (getToolMode(stack) == ToolMode.Paste) {
                if (!player.isSneaking()) {
                    if (getAnchor(stack) == null && trace != null) {
                        buildBlockMap(world, trace.getBlockPos(), stack, player);
                    } else {
                        BlockPos startPos = getAnchor(stack);
                        buildBlockMap(world, startPos, stack, player);
                    }
                }
            }
        } else {
            if (player.isSneaking() && trace != null) {
                if (GadgetUtils.getRemoteInventory(trace.getBlockPos(), world, player, NetworkIO.Operation.EXTRACT) != null)
                    return new ActionResult<>(EnumActionResult.SUCCESS, stack);
            }
            if (getToolMode(stack) == ToolMode.Copy) {
                if (player.isSneaking() && trace == null)
                    player.openGui(BuildingGadgets.instance, GuiProxy.CopyPasteID, world, hand.ordinal(), 0, 0);
            } else if (player.isSneaking()) {
                player.openGui(BuildingGadgets.instance, GuiProxy.PasteID, world, hand.ordinal(), 0, 0);
            } else {
                AbstractRender.updateInventoryCache();
            }
        }

        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    public static void rotateOrMirrorBlocks(ItemStack stack, EntityPlayer player, PacketRotateMirror.Operation operation) {
        if (!(getToolMode(stack) == ToolMode.Paste)) return;
        if (player.world.isRemote) {
            return;
        }
        CopyGadget tool = ModItems.copyGadget;
        List<BlockMap> blockMapList;
        WorldSave worldSave = WorldSave.getWorldSave(player.world);
        NBTTagCompound tagCompound = worldSave.getCompoundFromUUID(tool.getUUID(stack));
        BlockPos startPos = tool.getStartPos(stack);
        if (startPos == null) return;
        blockMapList = getBlockMapList(tagCompound);
        List<Integer> posIntArrayList = new ArrayList<>();
        List<Integer> stateIntArrayList = new ArrayList<>();
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
        tagCompound.setTag("mapIntState", blockMapIntState.putIntStateMapIntoNBT());
        tagCompound.setTag("mapIntStack", blockMapIntState.putIntStackMapIntoNBT());
        tagCompound.setIntArray("posIntArray", posIntArray);
        tagCompound.setIntArray("stateIntArray", stateIntArray);
        tool.incrementCopyCounter(stack);
        tagCompound.setInteger("copycounter", tool.getCopyCounter(stack));
        worldSave.addToMap(tool.getUUID(stack), tagCompound);
        worldSave.markForSaving();
        PacketHandler.INSTANCE.sendTo(new PacketBlockMap(tagCompound), (EntityPlayerMP) player);
        player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA
                + new TextComponentTranslation("message.gadget." + (player.isSneaking() ? "mirrored" : "rotated")).getUnformattedComponentText()), true);
    }

    public static void copyBlocks(ItemStack stack, EntityPlayer player, World world, BlockPos startPos, BlockPos endPos) {
        if (startPos != null && endPos != null) {
            CopyGadget tool = ModItems.copyGadget;
            if (findBlocks(world, startPos, endPos, stack, player, tool)) {
                tool.setStartPos(stack, startPos);
                tool.setEndPos(stack, endPos);
            }
        }
    }

    private static boolean findBlocks(World world, BlockPos start, BlockPos end, ItemStack stack, EntityPlayer player, CopyGadget tool) {
        setLastBuild(stack, null, 0);
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
                    if (tempState != Blocks.AIR.getDefaultState() && (world.getTileEntity(tempPos) == null || world.getTileEntity(tempPos) instanceof ConstructionBlockTileEntity) && !tempState.getMaterial().isLiquid() && !SyncedConfig.blockBlacklist.contains(tempState.getBlock())) {
                        TileEntity te = world.getTileEntity(tempPos);
                        IBlockState assignState = InventoryManipulation.getSpecificStates(tempState, world, player, tempPos, stack);
                        IBlockState actualState = assignState.getActualState(world, tempPos);
                        if (te instanceof ConstructionBlockTileEntity) {
                            actualState = ((ConstructionBlockTileEntity) te).getActualBlockState();
                        }
                        if (actualState != null) {
                            UniqueItem uniqueItem = BlockMapIntState.blockStateToUniqueItem(actualState, player, tempPos);
                            if (uniqueItem.item != Items.AIR) {
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
                                    actualState.getBlock().getDrops(drops, world, BlockPos.ORIGIN, actualState, 0);

                                int neededItems = 0;
                                for (ItemStack drop : drops) {
                                    if (drop.getItem().equals(uniqueItem.item)) {
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
        tagCompound.setTag("mapIntState", blockMapIntState.putIntStateMapIntoNBT());
        tagCompound.setTag("mapIntStack", blockMapIntState.putIntStackMapIntoNBT());
        int[] posIntArray = posIntArrayList.stream().mapToInt(i -> i).toArray();
        int[] stateIntArray = stateIntArrayList.stream().mapToInt(i -> i).toArray();
        tagCompound.setIntArray("posIntArray", posIntArray);
        tagCompound.setIntArray("stateIntArray", stateIntArray);

        tagCompound.setTag("startPos", NBTUtil.createPosTag(start));
        tagCompound.setTag("endPos", NBTUtil.createPosTag(end));
        tagCompound.setInteger("dim", player.dimension);
        tagCompound.setString("UUID", tool.getUUID(stack));
        tagCompound.setString("owner", player.getName());
        tool.incrementCopyCounter(stack);
        tagCompound.setInteger("copycounter", tool.getCopyCounter(stack));

        worldSave.addToMap(tool.getUUID(stack), tagCompound);
        worldSave.markForSaving();
        PacketHandler.INSTANCE.sendTo(new PacketBlockMap(tagCompound), (EntityPlayerMP) player);

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

        pos = pos.up(CopyGadget.getY(stack));
        pos = pos.east(CopyGadget.getX(stack));
        pos = pos.south(CopyGadget.getZ(stack));

        List<BlockMap> blockMapList = getBlockMapList(tagCompound, pos);
        setLastBuild(stack, pos, player.dimension);

        for (BlockMap blockMap : blockMapList)
            placeBlock(world, blockMap.pos, player, blockMap.state, getBlockMapIntState(tagCompound).getIntStackMap());

        GadgetUtils.clearCachedRemoteInventory();
        setAnchor(stack, null);
        //System.out.printf("Built %d Blocks in %.2f ms%n", blockMapList.size(), (System.nanoTime() - time) * 1e-6);
    }

    private void placeBlock(World world, BlockPos pos, EntityPlayer player, IBlockState state, Map<IBlockState, UniqueItem> IntStackMap) {
        IBlockState testState = world.getBlockState(pos);
        if ((SyncedConfig.canOverwriteBlocks && !testState.getBlock().isReplaceable(world, pos)) ||
            (!SyncedConfig.canOverwriteBlocks && world.getBlockState(pos).getMaterial() != Material.AIR))
            return;

        if (pos.getY() < 0 || state.equals(Blocks.AIR.getDefaultState()) || !player.isAllowEdit())
            return;

        ItemStack heldItem = getAsStack(player);
        if (heldItem.isEmpty())
            return;

        if (ModItems.copyGadget.getStartPos(heldItem) == null || ModItems.copyGadget.getEndPos(heldItem) == null)
            return;

        UniqueItem uniqueItem = IntStackMap.get(state);
        if (uniqueItem == null) return; //This shouldn't happen I hope!
        ItemStack itemStack = new ItemStack(uniqueItem.item, 1, uniqueItem.meta);
        NonNullList<ItemStack> drops = NonNullList.create();
        state.getBlock().getDrops(drops, world, pos, state, 0);
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
        ItemStack constructionPaste = new ItemStack(ModItems.constructionPaste);
        boolean useConstructionPaste = false;
        if (InventoryManipulation.countItem(itemStack, player, world) < neededItems) {
            if (InventoryManipulation.countPaste(player) < neededItems) {
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
            useItemSuccess = InventoryManipulation.usePaste(player, 1);
        } else {
            useItemSuccess = InventoryManipulation.useItem(itemStack, player, neededItems, world);
        }
        if (useItemSuccess) {
            world.spawnEntity(new BlockBuildEntity(world, pos, player, state, 1, state, useConstructionPaste));
        }

    }

    public static void anchorBlocks(EntityPlayer player, ItemStack stack) {
        BlockPos currentAnchor = getAnchor(stack);
        if (currentAnchor == null) {
            RayTraceResult lookingAt = RayTraceHelper.rayTrace(player, AbstractGadget.shouldRayTraceFluid(stack));
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

    public void undoBuild(EntityPlayer player, ItemStack heldItem) {
//        long time = System.nanoTime();
        NBTTagCompound tagCompound = WorldSave.getWorldSave(player.world).getCompoundFromUUID(ModItems.copyGadget.getUUID(heldItem));
        World world = player.world;
        if (world.isRemote) {
            return;
        }
        BlockPos startPos = getLastBuild(heldItem);
        if (startPos == null)
            return;

        Integer dimension = getLastBuildDim(heldItem);
        ItemStack silkTool = heldItem.copy(); //Setup a Silk Touch version of the tool so we can return stone instead of cobblestone, etc.
        silkTool.addEnchantment(Enchantments.SILK_TOUCH, 1);
        List<BlockMap> blockMapList = getBlockMapList(tagCompound, startPos);
        boolean success = true;
        for (BlockMap blockMap : blockMapList) {
            double distance = blockMap.pos.getDistance(player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ());
            boolean sameDim = (player.dimension == dimension);
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
        if (success) setLastBuild(heldItem, null, 0);
    }

    public static ItemStack getAsStack(EntityPlayer player) {
        Optional<ItemStack> stack = AbstractGadget.getGadget(player);
        if (!stack.isPresent() || !(stack.get().getItem() instanceof CopyGadget))
            return ItemStack.EMPTY;

        return stack.get();
    }
}