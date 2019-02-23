package com.direwolf20.buildinggadgets.common.items.gadgets;

import com.direwolf20.buildinggadgets.client.gui.GuiMod;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.common.items.capability.CapabilityProviderEnergy;
import com.direwolf20.buildinggadgets.common.registry.objects.BGBlocks;
import com.direwolf20.buildinggadgets.common.utils.blocks.BlockMapIntState;
import com.direwolf20.buildinggadgets.common.utils.GadgetUtils;
import com.direwolf20.buildinggadgets.common.utils.Reference;
import com.direwolf20.buildinggadgets.common.utils.helpers.VectorHelper;
import com.direwolf20.buildinggadgets.common.world.WorldSave;
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
import net.minecraft.util.*;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;

import javax.annotation.Nullable;
import java.util.*;

public class GadgetDestruction extends GadgetGeneric {

    public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Reference.MODID,"gadget_destruction");

    public GadgetDestruction(Properties builder) {
        super(builder.defaultMaxDamage(Config.GADGETS.GADGET_DESTRUCTION.durability.get()));
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
        tooltip.add(new TextComponentString(TextFormatting.RED + I18n.format("tooltip.gadget.destroywarning")));
        tooltip.add(new TextComponentString(TextFormatting.AQUA + I18n.format("tooltip.gadget.destroyshowoverlay") + ": " + getOverlay(stack)));
        tooltip.add(new TextComponentString(TextFormatting.YELLOW + I18n.format("tooltip.gadget.connectedarea") + ": " + getConnectedArea(stack)));
        if (Config.GADGETS.GADGET_DESTRUCTION.nonFuzzyEnabled.get())
            tooltip.add(new TextComponentString(TextFormatting.GOLD + I18n.format("tooltip.gadget.fuzzy") + ": " + getFuzzy(stack)));

        addEnergyInformation(tooltip, stack);
    }

    @Nullable
    public static String getUUID(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        String uuid = tagCompound.getString("UUID");
        if (uuid.isEmpty()) {
            UUID uid = UUID.randomUUID();
            tagCompound.setString("UUID", uid.toString());
            stack.setTag(tagCompound);
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
        NBTTagCompound tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        if (side == null) {
            if (tagCompound.getTag("anchorSide") != null) {
                tagCompound.removeTag("anchorSide");
                stack.setTag(tagCompound);
            }
            return;
        }
        tagCompound.setString("anchorSide", side.getName());
        stack.setTag(tagCompound);
    }

    public static EnumFacing getAnchorSide(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTag();
        if (tagCompound == null) {
            return null;
        }
        String facing = tagCompound.getString("anchorSide");
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
            tagCompound.setBoolean("overlay", true);
            tagCompound.setBoolean("fuzzy", true);
            stack.setTag(tagCompound);
            return true;
        }
        if (tagCompound.hasKey("overlay")) {
            return tagCompound.getBoolean("overlay");
        }
        tagCompound.setBoolean("overlay", true);
        stack.setTag(tagCompound);
        return true;
    }

    public static void setOverlay(ItemStack stack, boolean showOverlay) {
        NBTTagCompound tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        tagCompound.setBoolean("overlay", showOverlay);
        stack.setTag(tagCompound);
    }

    public void switchOverlay(EntityPlayer player, ItemStack stack) {
        boolean overlay = !getOverlay(stack);
        setOverlay(stack, overlay);
        player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("tooltip.gadget.destroyshowoverlay").getUnformattedComponentText() + ": " + overlay), true);
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
                RayTraceResult lookingAt = VectorHelper.getLookingAt(player);
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
                IEnergyStorage energy = CapabilityProviderEnergy.getCap(stack).orElseThrow(NullPointerException::new);
                int accepted = energy.receiveEnergy(105000, false);
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
            RayTraceResult lookingAt = VectorHelper.getLookingAt(player);
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
        BlockPos startPos = (getAnchor(stack) == null) ? pos : getAnchor(stack);
        EnumFacing side = (getAnchorSide(stack) == null) ? incomingSide : getAnchorSide(stack);
        List<EnumFacing> directions = assignDirections(side, player);
        IBlockState stateTarget = !Config.GADGETS.GADGET_DESTRUCTION.nonFuzzyEnabled.get() || GadgetGeneric.getFuzzy(stack) ? null : world.getBlockState(pos);
        if (GadgetGeneric.getConnectedArea(stack)) {
            String[] directionNames = new String[] {"right", "left", "up", "down", "depth"};
            AxisAlignedBB area = new AxisAlignedBB(pos);
            for (int i = 0; i < directionNames.length; i++)
                area = area.union(new AxisAlignedBB(pos.offset(directions.get(i), getToolValue(stack, directionNames[i]) - (i == 4 ? 1 : 0))));

            addConnectedCoords(world, player, startPos, stateTarget, voidPositions,
                    (int) area.minX, (int) area.minY, (int) area.minZ, (int) area.maxX - 1, (int) area.maxY - 1, (int) area.maxZ - 1);
        } else {
            for (int d = 0; d < getToolValue(stack, "depth"); d++) {
                for (int x = getToolValue(stack, "left") * -1; x <= getToolValue(stack, "right"); x++) {
                    for (int y = getToolValue(stack, "down") * -1; y <= getToolValue(stack, "up"); y++) {
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
        if (currentBlock.getMaterial() == Material.AIR) return false;
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
        SortedSet<BlockPos> voidPosArray = getArea(world, pos, side, player, stack);
        Map<BlockPos, IBlockState> posStateMap = new HashMap<BlockPos, IBlockState>();
        Map<BlockPos, IBlockState> pasteStateMap = new HashMap<BlockPos, IBlockState>();
        for (BlockPos voidPos : voidPosArray) {
            IBlockState blockState = world.getBlockState(voidPos);
            IBlockState pasteState = Blocks.AIR.getDefaultState();
            if (blockState.getBlock() == BGBlocks.constructionBlock) {
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
        tagCompound.setTag("startPos", NBTUtil.writeBlockPos(startBlock));
        tagCompound.setString("dim", DimensionType.func_212678_a(player.dimension).toString());
        tagCompound.setString("UUID", UUID);
        worldSave.addToMap(UUID, tagCompound);
        worldSave.markForSaving();
    }

    public static void undo(EntityPlayer player, ItemStack stack) {
        World world = player.world;
        WorldSave worldSave = WorldSave.getWorldSaveDestruction(world);
        NBTTagCompound tagCompound = worldSave.getCompoundFromUUID(getUUID(stack));
        if (tagCompound == null) return;
        BlockPos startPos = NBTUtil.readBlockPos(tagCompound.getCompound("startPos"));
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
                if (placeState.getBlock() == BGBlocks.constructionBlock) {
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
