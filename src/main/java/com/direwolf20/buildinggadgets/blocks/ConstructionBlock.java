package com.direwolf20.buildinggadgets.blocks;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ConstructionBlock extends Block implements ITileEntityProvider {

    public ConstructionBlock() {
        super(Material.ROCK);
        setHardness(5.0f);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setUnlocalizedName(BuildingGadgets.MODID + ".constructionblock");     // Used for localization (en_US.lang)
        setRegistryName("constructionblock");        // The unique name (within your mod) that identifies this block
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new ConstructionBlockTileEntity();
    }

    private static ConstructionBlockTileEntity getTE(World world, BlockPos pos) {
        return (ConstructionBlockTileEntity) world.getTileEntity(pos);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        ConstructionBlockTileEntity te = getTE(world, pos);
        ItemStack heldItem = player.getHeldItem(hand);
        IBlockState newState = Block.getBlockFromItem(heldItem.getItem()).getDefaultState();
        if (newState != null && newState != Blocks.AIR.getDefaultState()) {
            te.setBlockState(newState);
            return true;
        }
        System.out.println("Failed: " + newState);
        return false;
    }
}