package com.direwolf20.buildinggadgets.common.items.gadgets;

import com.direwolf20.buildinggadgets.api.*;
import com.direwolf20.buildinggadgets.client.gui.GuiProxy;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.Config;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlock;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.capability.CapabilityProviderEnergy;
import com.direwolf20.buildinggadgets.common.capability.CapabilityProviderTemplate;
import com.direwolf20.buildinggadgets.common.capability.WrappingCapabilityProvider;
import com.direwolf20.buildinggadgets.common.entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.common.items.ModItems;
import com.direwolf20.buildinggadgets.common.network.PacketBlockMap;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.tools.BlacklistBlocks;
import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import com.direwolf20.buildinggadgets.common.tools.InventoryManipulation;
import com.direwolf20.buildinggadgets.common.tools.VectorTools;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
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
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.direwolf20.buildinggadgets.common.tools.GadgetUtils.withSuffix;

public class GadgetCopyPaste extends GadgetGeneric implements ITemplateOld {

    public enum ToolMode {
        Copy, Paste;
        private static ToolMode[] vals = values();

        public ToolMode next() {
            return vals[(this.ordinal() + 1) % vals.length];
        }
    }

    public GadgetCopyPaste() {
        setRegistryName("copypastetool");        // The unique name (within your mod) that identifies this item
        setUnlocalizedName(BuildingGadgets.MODID + ".copypastetool");     // Used for localization (en_US.lang)
        setMaxStackSize(1);
        if (!Config.poweredByFE) {
            setMaxDamage(Config.durabilityCopyPaste);
        }
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

    public static List<BlockMap> getBlockMapList(@Nullable NBTTagCompound tagCompound) {
        return getBlockMapList(tagCompound, GadgetUtils.getPOSFromNBT(tagCompound, "startPos"));
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

    public static void rotateBlocks(ItemStack stack, EntityPlayer player) {
        if (!(getToolMode(stack) == ToolMode.Paste)) return;
        if (player.world.isRemote) {
            return;
        }
        GadgetCopyPaste tool = ModItems.gadgetCopyPaste;

        WorldSave worldSave = WorldSave.getWorldSave(player.world);
        NBTTagCompound tagCompound = worldSave.getCompoundFromUUID(tool.getUUID(stack));
        BlockPos startPos = tool.getStartPos(stack);
        if (startPos == null) return;
        List<BlockMap> blockMapList = getBlockMapList(tagCompound);
        IntList posIntArrayList = new IntArrayList();
        IntList stateIntArrayList = new IntArrayList();
        BlockState2ItemMap blockMapIntState = new BlockState2ItemMap();

        for (BlockMap blockMap : blockMapList) {
            BlockPos tempPos = blockMap.getPos();

            int px = (tempPos.getX() - startPos.getX());
            int pz = (tempPos.getZ() - startPos.getZ());

            int nx = -pz;

            BlockPos newPos = new BlockPos(startPos.getX() + nx, tempPos.getY(), startPos.getZ() + px);
            IBlockState rotatedState = blockMap.getState().withRotation(Rotation.CLOCKWISE_90);
            UniqueItem uniqueItem = UniqueItem.fromBlockState(rotatedState, player, tempPos);
            blockMapIntState.addToMap(uniqueItem, rotatedState);
            posIntArrayList.add(GadgetUtils.relPosToInt(startPos, newPos));
            stateIntArrayList.add((int) blockMapIntState.getSlot(rotatedState));
        }
        int[] posIntArray = posIntArrayList.toIntArray();
        int[] stateIntArray = stateIntArrayList.toIntArray();
        blockMapIntState.writeToNBT(tagCompound);
        tagCompound.setIntArray("posIntArray", posIntArray);
        tagCompound.setIntArray("stateIntArray", stateIntArray);
        tool.incrementCopyCounter(stack);
        tagCompound.setInteger("copycounter", tool.getCopyCounter(stack));
        worldSave.addToMap(tool.getUUID(stack), tagCompound);
        worldSave.markForSaving();
        PacketHandler.INSTANCE.sendTo(new PacketBlockMap(tagCompound), (EntityPlayerMP) player);
        player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.rotated").getUnformattedComponentText()), true);
    }

    public static void copyBlocks(ItemStack stack, EntityPlayer player, World world, BlockPos startPos, BlockPos endPos) {
        if (startPos != null && endPos != null) {
            GadgetCopyPaste tool = ModItems.gadgetCopyPaste;
            if (findBlocks(world, startPos, endPos, stack, player, tool)) {
                tool.setStartPos(stack, startPos);
                tool.setEndPos(stack, endPos);
            }
        }
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
            player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.anchorrender").getUnformattedComponentText()), true);
        } else {
            setAnchor(stack, null);
            player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.anchorremove").getUnformattedComponentText()), true);
        }
    }

    public static ItemStack getGadget(EntityPlayer player) {
        ItemStack stack = GadgetGeneric.getGadget(player);
        if (!(stack.getItem() instanceof GadgetCopyPaste)) {
            return ItemStack.EMPTY;
        }

        return stack;
    }

    private static void setAnchor(ItemStack stack, BlockPos anchorPos) {
        GadgetUtils.writePOSToNBT(stack, anchorPos, "anchor");
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

    private static List<BlockMap> getBlockMapList(@Nullable NBTTagCompound tagCompound, BlockPos startBlock) {
        List<BlockMap> blockMap = new ArrayList<BlockMap>();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        BlockState2ShortMap MapIntState = BlockState2ShortMap.readFromNBT(tagCompound);
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

    private static void setToolMode(ItemStack stack, ToolMode mode) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        tagCompound.setString("mode", mode.name());
        stack.setTagCompound(tagCompound);
    }

    private static boolean findBlocks(World world, BlockPos start, BlockPos end, ItemStack stack, EntityPlayer player, GadgetCopyPaste tool) {
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
        IntList posIntArrayList = new IntArrayList();
        IntList stateIntArrayList = new IntArrayList();
        BlockState2ItemMap blockMapIntState = new BlockState2ItemMap();
        Multiset<UniqueItem> itemCountMap = HashMultiset.create();

        int blockCount = 0;

        for (int x = iStartX; x <= iEndX; x++) {
            for (int y = iStartY; y <= iEndY; y++) {
                for (int z = iStartZ; z <= iEndZ; z++) {
                    BlockPos tempPos = new BlockPos(x, y, z);
                    IBlockState tempState = world.getBlockState(tempPos);
                    if (tempState != Blocks.AIR.getDefaultState() && (world.getTileEntity(tempPos) == null || world.getTileEntity(tempPos) instanceof ConstructionBlockTileEntity) && !tempState.getMaterial().isLiquid() && !BlacklistBlocks.checkBlacklist(tempState.getBlock())) {
                        TileEntity te = world.getTileEntity(tempPos);
                        IBlockState assignState = InventoryManipulation.getSpecificStates(tempState, world, player, tempPos, stack);
                        IBlockState actualState = assignState.getActualState(world, tempPos);
                        if (te instanceof ConstructionBlockTileEntity) {
                            actualState = ((ConstructionBlockTileEntity) te).getActualBlockState();
                        }
                        if (actualState != null) {
                            UniqueItem uniqueItem = UniqueItem.fromBlockState(actualState, player, tempPos);
                            if (uniqueItem.getItem() != Items.AIR) {
                                posIntArrayList.add(GadgetUtils.relPosToInt(start, tempPos));
                                blockMapIntState.addToMap(uniqueItem, actualState);
                                stateIntArrayList.add((int) blockMapIntState.getSlot(actualState));

                                blockCount++;
                                if (blockCount > Short.MAX_VALUE) {
                                    player.sendStatusMessage(new TextComponentString(TextFormatting.RED + new TextComponentTranslation("message.gadget.toomanyblocks").getUnformattedComponentText()), true);
                                    return false;
                                }
                                NonNullList<ItemStack> drops = NonNullList.create();
                                actualState.getBlock().getDrops(drops, world, new BlockPos(0, 0, 0), actualState, 0);

                                int neededItems = 0;
                                for (ItemStack drop : drops) {
                                    if (drop.getItem().equals(uniqueItem.getItem())) {
                                        neededItems++;
                                    }
                                }
                                if (neededItems == 0) {
                                    neededItems = 1;
                                }
                                itemCountMap.add(uniqueItem, neededItems);
                            }
                        }
                    } else if ((world.getTileEntity(tempPos) != null) && !(world.getTileEntity(tempPos) instanceof ConstructionBlockTileEntity)) {
                        foundTE++;
                    }
                }
            }
        }
        tool.setItemCountMap(stack, itemCountMap);
        int[] posIntArray = posIntArrayList.stream().mapToInt(i -> i).toArray();
        int[] stateIntArray = stateIntArrayList.stream().mapToInt(i -> i).toArray();
        tagCompound.setIntArray("posIntArray", posIntArray);
        tagCompound.setIntArray("stateIntArray", stateIntArray);
        blockMapIntState.writeToNBT(tagCompound);
        tagCompound.setTag("startPos", NBTUtil.createPosTag(start));
        tagCompound.setTag("endPos", NBTUtil.createPosTag(end));
        tagCompound.setInteger("dim", player.dimension);
        ITemplateOld.setUUID(tool.getUUID(stack),tagCompound);
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

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound tag) {
        return new WrappingCapabilityProvider(super.initCapabilities(stack, tag), new CapabilityProviderTemplate(stack));
    }

    @Override
    public int getEnergyCost() {
        return Config.energyCostBuilder;
    }

    @Override
    public int getDamagePerUse() {
        return 1;
    }

    @Override
    public WorldSave getWorldSave(World world) {
        return WorldSave.getWorldSave(world);
    }

    public void setMode(EntityPlayer player, ItemStack heldItem, int modeInt) {
        //Called when we specify a mode with the radial menu
        ToolMode mode = ToolMode.values()[modeInt];
        setToolMode(heldItem, mode);
        player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.toolmode").getUnformattedComponentText() + ": " + mode.name()), true);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        player.setActiveHand(hand);
        if (!world.isRemote) {
            if (getToolMode(stack) == ToolMode.Copy) {
                BlockPos pos = VectorTools.getPosLookingAt(player);
                if (pos == null) {
                    //setStartPos(stack, null);
                    //setEndPos(stack, null);
                    //player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.areareset").getUnformattedComponentText()), true);
                    return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
                }
                if (player.isSneaking()) {
                    if (getStartPos(stack) != null) {
                        copyBlocks(stack, player, world, getStartPos(stack), pos);
                    } else {
                        setEndPos(stack, pos);
                    }
                } else {
                    if (getEndPos(stack) != null) {
                        copyBlocks(stack, player, world, pos, getEndPos(stack));
                    } else {
                        setStartPos(stack, pos);
                    }
                }
            } else if (getToolMode(stack) == ToolMode.Paste) {
                if (!player.isSneaking()) {
                    if (getAnchor(stack) == null) {
                        BlockPos pos = VectorTools.getPosLookingAt(player);
                        if (pos == null) return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
                        buildBlockMap(world, pos, stack, player);
                    } else {
                        BlockPos startPos = getAnchor(stack);
                        buildBlockMap(world, startPos, stack, player);
                    }
                }
            }
        } else {
            if (getToolMode(stack) == ToolMode.Copy) {
                BlockPos pos = VectorTools.getPosLookingAt(player);
                if (pos == null) {
                    if (player.isSneaking()) {
                        player.openGui(BuildingGadgets.instance, GuiProxy.CopyPasteID, world, hand.ordinal(), 0, 0);
                        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
                    }
                }
            } else {
                if (player.isSneaking()) {
                    player.openGui(BuildingGadgets.instance, GuiProxy.PasteID, world, hand.ordinal(), 0, 0);
                    return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
                }
            }
        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag b) {
        super.addInformation(stack, world, list, b);
        list.add(TextFormatting.AQUA + I18n.format("tooltip.gadget.mode") + ": " + getToolMode(stack));
        if (Config.poweredByFE) {
            IEnergyStorage energy = CapabilityProviderEnergy.getCap(stack);
            list.add(TextFormatting.WHITE + I18n.format("tooltip.gadget.energy") + ": " + withSuffix(energy.getEnergyStored()) + "/" + withSuffix(energy.getMaxEnergyStored()));
        }
    }

    public void undoBuild(EntityPlayer player, ItemStack heldItem) {
//        long time = System.nanoTime();
        NBTTagCompound tagCompound = WorldSave.getWorldSave(player.world).getCompoundFromUUID(ModItems.gadgetCopyPaste.getUUID(heldItem));
        World world = player.world;
        if (world.isRemote) {
            return;
        }
        BlockPos startPos = getLastBuild(heldItem);
        if (startPos == null) {
            return;
        }

        Integer dimension = getLastBuildDim(heldItem);
        ItemStack silkTool = heldItem.copy(); //Setup a Silk Touch version of the tool so we can return stone instead of cobblestone, etc.
        silkTool.addEnchantment(Enchantments.SILK_TOUCH, 1);
        List<BlockMap> blockMapList = getBlockMapList(tagCompound, startPos);
        boolean success = true;
        for (BlockMap blockMap : blockMapList) {
            double distance = blockMap.getPos().getDistance(player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ());
            boolean sameDim = (player.dimension == dimension);
            IBlockState currentBlock = world.getBlockState(blockMap.getPos());
            BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(world, blockMap.getPos(), currentBlock, player);
            boolean cancelled = MinecraftForge.EVENT_BUS.post(e);
            if (distance < 256 && !cancelled && sameDim) { //Don't allow us to undo a block while its still being placed or too far away
                if (currentBlock.getBlock() == blockMap.getState().getBlock() || currentBlock.getBlock() instanceof ConstructionBlock) {
                    if (currentBlock.getBlockHardness(world, blockMap.getPos()) >= 0) {
                        currentBlock.getBlock().harvestBlock(world, player, blockMap.getPos(), currentBlock, world.getTileEntity(blockMap.getPos()), silkTool);
                        world.spawnEntity(new BlockBuildEntity(world, blockMap.getPos(), player, currentBlock, 2, currentBlock, false));
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
        BlockState2ItemMap itemMap = BlockState2ItemMap.readFromNBT(tagCompound);
        for (BlockMap blockMap : blockMapList)
            placeBlock(world, blockMap.getPos(), player, blockMap.getState(), itemMap);

        setAnchor(stack, null);
        //System.out.printf("Built %d Blocks in %.2f ms%n", blockMapList.size(), (System.nanoTime() - time) * 1e-6);
    }

    private void placeBlock(World world, BlockPos pos, EntityPlayer player, IBlockState state, BlockState2ItemMap IntStackMap) {
        IBlockState testState = world.getBlockState(pos);
        if (Config.canOverwriteBlocks && !testState.getBlock().isReplaceable(world, pos) || world.getBlockState(pos).getMaterial() != Material.AIR)
            return;

        if (pos.getY() < 0 || state.equals(Blocks.AIR.getDefaultState()) || !player.isAllowEdit())
            return;

        ItemStack heldItem = getGadget(player);
        if (heldItem.isEmpty())
            return;

        if (ModItems.gadgetCopyPaste.getStartPos(heldItem) == null || ModItems.gadgetCopyPaste.getEndPos(heldItem) == null)
            return;

        UniqueItem uniqueItem = IntStackMap.getItemForState(state);
        if (uniqueItem == null) return; //This shouldn't happen I hope!
        ItemStack itemStack = new ItemStack(uniqueItem.getItem(), 1, uniqueItem.getMeta());
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
        if (InventoryManipulation.countItem(itemStack, player) < neededItems) {
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
            useItemSuccess = InventoryManipulation.useItem(itemStack, player, neededItems);
        }
        if (useItemSuccess) {
            world.spawnEntity(new BlockBuildEntity(world, pos, player, state, 1, state, useConstructionPaste));
        }

    }
}