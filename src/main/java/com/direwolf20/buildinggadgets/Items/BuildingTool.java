package com.direwolf20.buildinggadgets.Items;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
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
        if (!world.isRemote) {
            buildToMe(world, player,lookingAt.getBlockPos());
        }
        else {

        }
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
    }

    public boolean buildToMe(World world, EntityPlayer player, BlockPos startBlock) {
        BlockPos playerPos = player.getPosition();
        Block airBlock = Blocks.AIR;
        Block cobbleBlock = Blocks.COBBLESTONE;
        BlockPos changePos;
        if (playerPos.getX() == startBlock.getX()) {
            for (int i = startBlock.getZ();i<= playerPos.getZ();i++) {
                changePos = new BlockPos(startBlock.getX(),startBlock.getY(),i);
                if (world.getBlockState(changePos) == airBlock.getDefaultState()) {
                    world.setBlockState(changePos,cobbleBlock.getDefaultState());
                }

            }
        }
        return true;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 20;
    }
}
