package com.direwolf20.buildinggadgets.Items;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.Entities.BlockBuildEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BuildingTool extends Item {
    public BuildingTool() {
        setRegistryName("buildingtool");        // The unique name (within your mod) that identifies this item
        setUnlocalizedName(BuildingGadgets.MODID + ".buildingtool");     // Used for localization (en_US.lang)
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        player.setActiveHand(hand);
        RayTraceResult lookingAt = player.rayTrace(20, 1.0F);
        //System.out.println(lookingAt.sideHit);
        if (!world.isRemote) {
            if (world.getBlockState(lookingAt.getBlockPos()) != Blocks.AIR.getDefaultState()) {
                buildToMe(world, player, lookingAt.getBlockPos(),lookingAt.sideHit);
                //world.spawnEntity(new BlockBuildEntity(world, lookingAt.getBlockPos().up(), player,Blocks.COBBLESTONE.getDefaultState()));
            }
        }
        else {

        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
    }

    public boolean buildToMe(World world, EntityPlayer player, BlockPos startBlock, EnumFacing sideHit) {
        BlockPos playerPos = player.getPosition();
        IBlockState cobbleBlock = Blocks.COBBLESTONE.getDefaultState();
        BlockPos changePos;
        if (sideHit == EnumFacing.SOUTH) {
            for (int i = startBlock.getZ(); i <= playerPos.getZ(); i++) {
                changePos = new BlockPos(startBlock.getX(), startBlock.getY(), i);
                placeBlock(world, player, changePos, cobbleBlock);
            }
        }
        else if (sideHit == EnumFacing.NORTH) {
            for (int i = startBlock.getZ(); i >= playerPos.getZ(); i--) {
                changePos = new BlockPos(startBlock.getX(), startBlock.getY(), i);
                placeBlock(world, player, changePos, cobbleBlock);
            }
        }
        else if (sideHit == EnumFacing.EAST) {
            for (int i = startBlock.getX(); i <= playerPos.getX(); i++) {
                changePos = new BlockPos(i, startBlock.getY(), startBlock.getZ());
                placeBlock(world, player, changePos, cobbleBlock);
            }
        }
        else if (sideHit == EnumFacing.WEST) {
            for (int i = startBlock.getX(); i >= playerPos.getX(); i--) {
                changePos = new BlockPos(i, startBlock.getY(), startBlock.getZ());
                placeBlock(world, player, changePos, cobbleBlock);
            }
        }
        else if (sideHit == EnumFacing.UP) {
            for (int i = startBlock.getY(); i <= playerPos.getY(); i++) {
                changePos = new BlockPos(startBlock.getX(), i, startBlock.getZ());
                placeBlock(world, player, changePos, cobbleBlock);
            }
        }
        else if (sideHit == EnumFacing.DOWN) {
            for (int i = startBlock.getY(); i >= playerPos.getY(); i--) {
                changePos = new BlockPos(startBlock.getX(), i, startBlock.getZ());
                placeBlock(world, player, changePos, cobbleBlock);
            }
        }
        return true;
    }

    public boolean placeBlock(World world, EntityPlayer player, BlockPos pos, IBlockState setBlock) {
        if (world.getBlockState(pos).getBlock().isReplaceable(world,pos)) {
            world.spawnEntity(new BlockBuildEntity(world, pos, player, setBlock,false));
        }
        return true;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 20;
    }
}
