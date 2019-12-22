package com.direwolf20.buildinggadgets.common.gadgets;

import com.direwolf20.buildinggadgets.client.gui.GuiProxy;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.common.blocks.ModBlocks;
import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import com.direwolf20.buildinggadgets.common.entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.common.gadgets.history.HistoryEntry;
import com.direwolf20.buildinggadgets.common.gadgets.history.HistoryStack;
import com.direwolf20.buildinggadgets.common.tools.BlockMapIntState;
import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import com.direwolf20.buildinggadgets.common.tools.RayTraceHelper;
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
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;
import org.lwjgl.Sys;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class GadgetDestruction extends GadgetGeneric {

    public GadgetDestruction() {
        super("destructiontool");
        setMaxDamage(SyncedConfig.durabilityDestruction);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return SyncedConfig.poweredByFE ? 0 : SyncedConfig.durabilityDestruction;
    }

    @Override
    public int getEnergyMax() {
        return SyncedConfig.energyMaxDestruction;
    }

    @Override
    public int getEnergyCost(ItemStack tool) {
        return SyncedConfig.energyCostDestruction * getCostMultiplier(tool);
    }

    @Override
    public int getDamageCost(ItemStack tool) {
        return SyncedConfig.damageCostDestruction * getCostMultiplier(tool);
    }

    private int getCostMultiplier(ItemStack tool) {
        return (int) (SyncedConfig.nonFuzzyEnabledDestruction && !getFuzzy(tool) ? SyncedConfig.nonFuzzyMultiplierDestruction : 1);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag b) {
        super.addInformation(stack, world, list, b);
        list.add(TextFormatting.RED + I18n.format("tooltip.gadget.destroywarning"));
        list.add(TextFormatting.AQUA + I18n.format("tooltip.gadget.destroyshowoverlay") + ": " + getOverlay(stack));
        list.add(TextFormatting.YELLOW + I18n.format("tooltip.gadget.connected_area") + ": " + getConnectedArea(stack));
        if (SyncedConfig.nonFuzzyEnabledDestruction)
            list.add(TextFormatting.GOLD + I18n.format("tooltip.gadget.fuzzy") + ": " + getFuzzy(stack));

        addInformationRayTraceFluid(list, stack);
        addEnergyInformation(list,stack);
    }

    @Nullable
    public static String getUUID(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
        }
        String uuid = tagCompound.getString("UUID");
        if (uuid.isEmpty()) {
            UUID uid = UUID.randomUUID();
            tagCompound.setString("UUID", uid.toString());
            stack.setTagCompound(tagCompound);
            uuid = uid.toString();
        }
        return uuid;
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
            tagCompound.setBoolean("fuzzy", true);
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
                RayTraceResult lookingAt = RayTraceHelper.rayTrace(player, GadgetGeneric.shouldRayTraceFluid(stack));

                List<BlockPos> anchorPositions = GadgetUtils.getAnchor(stack);
                if (lookingAt == null && anchorPositions.size() == 0)
                    return new ActionResult<>(EnumActionResult.FAIL, stack);

                clearArea(world, lookingAt, player, stack, anchorPositions);

                // Clear the current anchor
                if (anchorPositions.size() > 0) {
                    GadgetUtils.setAnchor(stack, new ArrayList<>());
                    player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.anchorremove").getUnformattedComponentText()), true);
                }
            }
        } else {
            if (player.isSneaking()) {
                player.openGui(BuildingGadgets.instance, GuiProxy.DestructionID, world, hand.ordinal(), 0, 0);
                return new ActionResult<>(EnumActionResult.SUCCESS, stack);
            }
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    public void clearArea(World world, RayTraceResult rayTraceResult, EntityPlayer player, ItemStack stack, List<BlockPos> anchorPositions) {
        List<BlockPos> posList = getArea(world, rayTraceResult, player, stack, anchorPositions);

        HistoryStack historyStack = new HistoryStack();
        posList.forEach(blockPos -> {
            TileEntity te = world.getTileEntity(blockPos);
            IBlockState state = world.getBlockState(blockPos);

            // Don't push to the stack if you can't do it
            if( !destroyBlock(world, blockPos, player) )
                return;

            historyStack.getHistory().add(new HistoryEntry(
                    blockPos,
                    state,
                    te instanceof ConstructionBlockTileEntity
                            ? ((ConstructionBlockTileEntity) te).getActualBlockState()
                            : null
            ));
        });

        // Don't continue if nothing was broken...
        if( historyStack.getHistory().size() == 0 )
            return;


        // Store into the world save
        WorldSave worldSave = WorldSave.getWorldSaveDestruction(world);
        NBTTagCompound tagCompound = new NBTTagCompound();
        String UUID = getUUID(stack);
        if( UUID == null )
            return;

        tagCompound.setTag("history", historyStack.serialize());
        tagCompound.setInteger("dim", player.dimension);
        tagCompound.setString("UUID", UUID);

        worldSave.addToMap(UUID, tagCompound);
        worldSave.markForSaving();
    }

    public static List<BlockPos> getArea(World world, RayTraceResult rayTraceResult, EntityPlayer player, ItemStack stack, List<BlockPos> existing) {
        // Avoid doing computation when an anchor is in place.
        if( existing != null && existing.size() > 0 )
            return existing;

        if( rayTraceResult == null )
            return new ArrayList<>();

        SortedSet<BlockPos> voidPositions = new TreeSet<>(Comparator.comparingInt(Vec3i::getX).thenComparingInt(Vec3i::getY).thenComparingInt(Vec3i::getZ));
        int depth = getToolValue(stack, "depth");
        if (depth == 0)
            return new ArrayList<>();

        BlockPos startPos = rayTraceResult.getBlockPos();

        List<EnumFacing> directions = assignDirections(rayTraceResult.sideHit, player);
        IBlockState stateTarget = !SyncedConfig.nonFuzzyEnabledDestruction || GadgetGeneric.getFuzzy(stack) ? null : world.getBlockState(startPos);

        int left = -getToolValue(stack, "left");
        int right = getToolValue(stack, "right");
        int down = -getToolValue(stack, "down");
        int up = getToolValue(stack, "up");
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

        return new ArrayList<>(voidPositions);
    }

    public static boolean validBlock(World world, BlockPos voidPos, EntityPlayer player, @Nullable IBlockState stateTarget) {
        IBlockState currentBlock = world.getBlockState(voidPos);
        if (stateTarget != null && currentBlock != stateTarget) return false;
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

    public void undo(EntityPlayer player, ItemStack stack) {
        World world = player.world;

        WorldSave worldSave = WorldSave.getWorldSaveDestruction(world);
        NBTTagCompound tagCompound = worldSave.getCompoundFromUUID(getUUID(stack));
        if (tagCompound == null || !tagCompound.hasKey("history"))
            return;

        HistoryStack historyStack = new HistoryStack();
        historyStack.deserialize(tagCompound.getCompoundTag("history"));

        historyStack.getHistory().forEach(entry -> {
            IBlockState existingState = world.getBlockState(entry.getPos());
            if( existingState.getMaterial() != Material.AIR && !existingState.getMaterial().isLiquid() )
                return;

            IBlockState pasteState = entry.getPasteState();
            IBlockState setState = pasteState == null ? entry.getState() : pasteState;

            world.spawnEntity(new BlockBuildEntity(world, entry.getPos(), player, setState, 1, setState, pasteState != null));
        });

        worldSave.addToMap(getUUID(stack), new NBTTagCompound());
        worldSave.markForSaving();
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
