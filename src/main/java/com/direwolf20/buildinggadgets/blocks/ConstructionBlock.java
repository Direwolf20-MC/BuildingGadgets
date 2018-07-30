package com.direwolf20.buildinggadgets.blocks;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.blocks.Models.ConstructionBakedModel;
import com.direwolf20.buildinggadgets.blocks.Models.ConstructionID;
import com.direwolf20.buildinggadgets.blocks.Models.ConstructionProperty;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class ConstructionBlock extends Block implements ITileEntityProvider {
    public static final ConstructionProperty FACADEID = new ConstructionProperty("facadeid");

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
        StateMapperBase ignoreState = new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState iBlockState) {
                return ConstructionBakedModel.modelFacade;
            }
        };
        ModelLoader.setCustomStateMapper(this, ignoreState);
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
        IBlockState newState = Block.getBlockFromItem(heldItem.getItem()).getStateFromMeta(heldItem.getMetadata());
        if (newState != null && newState != Blocks.AIR.getDefaultState()) {
            te.setBlockState(newState);
            return true;
        }
        System.out.println("Failed: " + newState + ":" + te.getBlockState()+":"+world.isRemote);
        return false;
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        IExtendedBlockState extendedBlockState = (IExtendedBlockState) super.getExtendedState(state, world, pos);
        IBlockState mimicBlock = getMimicBlock(world, pos);
        if (mimicBlock != null) {
            ConstructionID mimicID = new ConstructionID(mimicBlock);
            return extendedBlockState.withProperty(FACADEID, mimicID);
        } else {
            return extendedBlockState;
        }
    }

    @Nullable
    protected IBlockState getMimicBlock(IBlockAccess blockAccess, BlockPos pos) {
        TileEntity te = blockAccess.getTileEntity(pos);
        if (te instanceof ConstructionBlockTileEntity) {
            return ((ConstructionBlockTileEntity) te).getBlockState();
        } else {
            return null;
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        IProperty<?>[] listedProperties = new IProperty<?>[]{};
        IUnlistedProperty<?>[] unlistedProperties = new IUnlistedProperty<?>[]{FACADEID};
        return new ExtendedBlockState(this, listedProperties, unlistedProperties);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean isOpaqueCube(IBlockState p_isFullBlock_1_) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return true; // delegated to FacadeBakedModel#getQuads
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        IBlockState mimicBlock = getMimicBlock(blockAccess, pos);
        return mimicBlock == null ? true : mimicBlock.shouldSideBeRendered(blockAccess, pos, side);
    }

    @Override
    public boolean isBlockNormalCube(IBlockState blockState) {
        return true;
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        IBlockState mimicBlock = getMimicBlock(world, pos);
        return mimicBlock == null ? true : mimicBlock.doesSideBlockRendering(world, pos, face);
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public void initColorHandler(BlockColors blockColors) {
        blockColors.registerBlockColorHandler((state, world, pos, tintIndex) -> {
            IBlockState mimicBlock = getMimicBlock(world, pos);
            return mimicBlock != null ? blockColors.colorMultiplier(mimicBlock, world, pos, tintIndex) : -1;
        }, this);
    }

}