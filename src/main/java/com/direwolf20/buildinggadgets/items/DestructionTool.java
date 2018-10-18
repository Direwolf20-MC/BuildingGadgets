package com.direwolf20.buildinggadgets.items;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.Config;
import com.direwolf20.buildinggadgets.ModBlocks;
import com.direwolf20.buildinggadgets.blocks.ConstructionBlockTileEntity;
import com.direwolf20.buildinggadgets.entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.gui.GuiProxy;
import com.direwolf20.buildinggadgets.tools.VectorTools;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import static com.direwolf20.buildinggadgets.tools.GadgetUtils.useEnergy;
import static com.direwolf20.buildinggadgets.tools.GadgetUtils.withSuffix;

public class DestructionTool extends GenericGadget {

    public DestructionTool() {
        setRegistryName("destructiontool");        // The unique name (within your mod) that identifies this item
        setUnlocalizedName(BuildingGadgets.MODID + ".destructiontool");     // Used for localization (en_US.lang)
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.TOOLS);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag b) {
        super.addInformation(stack, world, list, b);
        //list.add(TextFormatting.AQUA + I18n.format("tooltip.gadget.mode") + ": " + getToolMode(stack));
        if (Config.poweredByFE) {
            IEnergyStorage energy = stack.getCapability(CapabilityEnergy.ENERGY, null);
            if (energy != null)
                list.add(TextFormatting.WHITE + I18n.format("tooltip.gadget.energy") + ": " + withSuffix(energy.getEnergyStored()) + "/" + withSuffix(energy.getMaxEnergyStored()));
        }
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
            left = EnumFacing.NORTH;
        } else if (side.equals(EnumFacing.EAST)) {
            left = EnumFacing.SOUTH;
        } else if (side.equals(EnumFacing.NORTH)) {
            left = EnumFacing.EAST;
        } else if (side.equals(EnumFacing.SOUTH)) {
            left = EnumFacing.WEST;
        } else {
            left = player.getHorizontalFacing().rotateYCCW();
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
                if (lookingAt == null) { //If we aren't looking at anything, exit
                    return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
                }
                BlockPos startBlock = lookingAt.getBlockPos();
                EnumFacing sideHit = lookingAt.sideHit;
                clearArea(world, startBlock, sideHit, player, stack);
            }
        } else {
            if (player.isSneaking()) {
                player.openGui(BuildingGadgets.instance, GuiProxy.DestructionID, world, hand.ordinal(), 0, 0);
                return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
            }
        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    public static void clearArea(World world, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack) {
        ArrayList<EnumFacing> directions = assignDirections(side, player);
        for (int d = 0; d < getToolValue(stack, "depth"); d++) {
            for (int x = getToolValue(stack, "left") * -1; x <= getToolValue(stack, "right"); x++) {
                for (int y = getToolValue(stack, "down") * -1; y <= getToolValue(stack, "up"); y++) {
                    BlockPos voidPos = new BlockPos(pos);
                    voidPos = voidPos.offset(directions.get(0), x);
                    voidPos = voidPos.offset(directions.get(2), y);
                    voidPos = voidPos.offset(directions.get(4), d);
                    boolean success = destroyBlock(world, voidPos, player);

                }
            }
        }
    }

    public static boolean destroyBlock(World world, BlockPos voidPos, EntityPlayer player) {
        IBlockState currentBlock = world.getBlockState(voidPos);
        TileEntity te = world.getTileEntity(voidPos);
        if (currentBlock.getMaterial() == Material.AIR) return false;
        if (currentBlock.equals(ModBlocks.effectBlock.getDefaultState())) return false;
        if ((te != null) && !(te instanceof ConstructionBlockTileEntity)) return false;
        if (currentBlock.getBlock().getBlockHardness(currentBlock, world, voidPos) < 0) return false;
        ItemStack tool = player.getHeldItemMainhand();
        if (!(tool.getItem() instanceof DestructionTool)) {
            tool = player.getHeldItemOffhand();
            if (!(tool.getItem() instanceof DestructionTool)) {
                return false;
            }
        }
        if (!player.isAllowEdit()) {
            return false;
        }
        if (!world.isBlockModifiable(player, voidPos)) {
            return false;
        }
        BlockSnapshot blockSnapshot = BlockSnapshot.getBlockSnapshot(world, voidPos);
        if (ForgeEventFactory.onPlayerBlockPlace(player, blockSnapshot, EnumFacing.UP, EnumHand.MAIN_HAND).isCanceled()) {
            return false;
        }
        BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(world, voidPos, currentBlock, player);
        if (MinecraftForge.EVENT_BUS.post(e)) {
            return false;
        }
        if (Config.poweredByFE) {
            if (!useEnergy(tool, Config.energyCostDestruction, player)) {
                return false;
            }
        } else {
            if (tool.getItemDamage() >= tool.getMaxDamage()) {
                if (tool.isItemStackDamageable()) {
                    return false;
                }
            } else {
                tool.damageItem(2, player);
            }
        }
        world.spawnEntity(new BlockBuildEntity(world, voidPos, player, world.getBlockState(voidPos), 2, Blocks.AIR.getDefaultState(), false));
        return true;
    }
}
