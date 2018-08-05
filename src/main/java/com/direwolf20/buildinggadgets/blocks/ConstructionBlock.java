package com.direwolf20.buildinggadgets.blocks;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.blocks.Models.ConstructionBakedModel;
import com.direwolf20.buildinggadgets.blocks.Models.ConstructionID;
import com.direwolf20.buildinggadgets.blocks.Models.ConstructionProperty;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
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
import java.util.List;

public class ConstructionBlock extends Block implements ITileEntityProvider {
    public static final ConstructionProperty FACADEID = new ConstructionProperty("facadeid");
    public static final PropertyBool BRIGHT = PropertyBool.create("bright");

    public ConstructionBlock() {
        super(Material.ROCK);
        setHardness(2.0f);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setUnlocalizedName(BuildingGadgets.MODID + ".constructionblock");     // Used for localization (en_US.lang)
        setRegistryName("constructionblock");        // The unique name (within your mod) that identifies this block
        setDefaultState(blockState.getBaseState().withProperty(BRIGHT, true));
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
            te.setActualBlockState(newState);
            return true;
        }
        System.out.println("Failed: " + newState + ":" + te.getBlockState() + ":" + world.isRemote + ":" + te.getActualBlockState());
        return false;
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        IExtendedBlockState extendedBlockState = (IExtendedBlockState) super.getExtendedState(state, world, pos);
        IBlockState mimicBlock = getActualMimicBlock(world, pos);
        if (mimicBlock != null) {
            ConstructionID mimicID = new ConstructionID(mimicBlock);
            return extendedBlockState.withProperty(FACADEID, mimicID);
        } else {
            return extendedBlockState;
        }
    }

    @Nullable
    protected IBlockState getActualMimicBlock(IBlockAccess blockAccess, BlockPos pos) {
        try {
            TileEntity te = blockAccess.getTileEntity(pos);
            if (te instanceof ConstructionBlockTileEntity) {
                return ((ConstructionBlockTileEntity) te).getActualBlockState();
            } else {
                return null;
            }
        } catch (Exception var8) {
            return null;
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        IProperty<?>[] listedProperties = new IProperty<?>[]{BRIGHT};
        IUnlistedProperty<?>[] unlistedProperties = new IUnlistedProperty<?>[]{FACADEID};
        return new ExtendedBlockState(this, listedProperties, unlistedProperties);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return true; // delegated to FacadeBakedModel#getQuads
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        IBlockState mimicBlock = getActualMimicBlock(blockAccess, pos);
        return mimicBlock == null ? true : mimicBlock.getBlock().shouldSideBeRendered(mimicBlock, blockAccess, pos, side);
    }

    @Override
    public boolean isOpaqueCube(IBlockState p_isFullBlock_1_) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
        Boolean bright = state.getValue(ConstructionBlock.BRIGHT);
        if (bright) {
            return 0;
        }
        return 255;
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
        IBlockState mimicBlock = getActualMimicBlock(world, pos);
        return mimicBlock == null ? true : mimicBlock.getBlock().doesSideBlockRendering(mimicBlock, world, pos, face);
    }

    @SideOnly(Side.CLIENT)
    public void initColorHandler(BlockColors blockColors) {
        blockColors.registerBlockColorHandler((state, world, pos, tintIndex) -> {
            IBlockState mimicBlock = getActualMimicBlock(world, pos);
            return mimicBlock != null ? blockColors.colorMultiplier(mimicBlock, world, pos, tintIndex) : -1;
        }, this);
    }

    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        IBlockState mimicBlock = getActualMimicBlock(worldIn, pos);
        try {
            return mimicBlock == null ? BlockFaceShape.SOLID : mimicBlock.getBlock().getBlockFaceShape(worldIn, state, pos, face);
        } catch (Exception var8) {
            return BlockFaceShape.SOLID;
        }
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        IBlockState mimicBlock = getActualMimicBlock(worldIn, pos);
        if (mimicBlock == null) {
            super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
        } else {
            mimicBlock.getBlock().addCollisionBoxToList(mimicBlock, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
        }
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        IBlockState mimicBlock = getActualMimicBlock(worldIn, pos);
        if (mimicBlock == null) {
            return super.getBoundingBox(blockState, worldIn, pos);
        } else {
            try {
                return mimicBlock.getBlock().getBoundingBox(blockState, worldIn, pos);
            } catch (Exception var8) {
                return super.getBoundingBox(blockState, worldIn, pos);
            }
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        IBlockState mimicBlock = getActualMimicBlock(source, pos);
        if (mimicBlock == null) {
            return super.getBoundingBox(state, source, pos);
        } else {
            try {
                return mimicBlock.getBlock().getBoundingBox(state, source, pos);
            } catch (Exception var8) {
                return super.getBoundingBox(state, source, pos);
            }
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(BRIGHT, (meta == 0) ? false : true);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return (state.getValue(BRIGHT) ? 1 : 0);
    }

}