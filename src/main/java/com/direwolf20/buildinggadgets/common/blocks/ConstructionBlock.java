package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.common.blocks.Models.BlockstateProperty;
import com.direwolf20.buildinggadgets.common.items.FakeRenderWorld;
import com.direwolf20.buildinggadgets.common.items.ModItems;
import javafx.geometry.Side;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraftforge.common.property.ExtendedStateContainer;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

//@Optional.Interface(iface = "team.chisel.ctm.api.IFacade", modid = "ctm-api")
public class ConstructionBlock extends Block /*implements IFacade*/ {

    //public static final ConstructionProperty FACADEID = new ConstructionProperty("facadeid");
    public static final IProperty<Boolean> BRIGHT = BooleanProperty.create("bright");
    public static final IProperty<Boolean> NEIGHBOR_BRIGHTNESS = BooleanProperty.create("neighbor_brightness");

    public static final IUnlistedProperty<IBlockState> FACADE_ID = new BlockstateProperty("facadestate");
    public static final IUnlistedProperty<IBlockState> FACADE_EXT_STATE = new BlockstateProperty("facadeextstate");

    public ConstructionBlock() {
        super(Block.Builder.create(Material.ROCK).hardnessAndResistance(2.0f));
        setRegistryName("construction_block");        // The unique name (within your mod) that identifies this block
        setDefaultState(createBlockState().getBaseState().withProperty(BRIGHT, true).withProperty(NEIGHBOR_BRIGHTNESS, false));
    }
/*
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
        StateMapperBase ignoreState = new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState iBlockState) {
                return ConstructionBakedModel.modelFacade;
            }
        };
        ModelLoader.setCustomStateMapper(this, ignoreState);
    }*/

    @Override
    public IItemProvider getItemDropped(IBlockState state, World worldIn, BlockPos pos, int fortune) {
        return ModItems.constructionPaste;
    }

    @Override
    public boolean canSilkHarvest(IBlockState state, IWorldReader world, BlockPos pos, EntityPlayer player) {
        return false;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    /*private static ConstructionBlockTileEntity getTE(World world, BlockPos pos) {
        return (ConstructionBlockTileEntity) world.getTileEntity(pos);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        //super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
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
    }*/

    /**
     * Can return IExtendedBlockState
     *
     * @param state
     * @param world
     * @param pos
     */
    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockReader world, BlockPos pos) {
        IExtendedBlockState extendedBlockState = (IExtendedBlockState) super.getExtendedState(state, world, pos);
        IBlockState mimicBlock = getActualMimicBlock(world, pos);
        if (mimicBlock != null) {
            FakeRenderWorld fakeRenderWorld = new FakeRenderWorld(); //TODO Update Render world
            //fakeRenderWorld.setState(world, mimicBlock, pos);
            //IBlockState extState = mimicBlock.getBlock().getExtendedState(mimicBlock, fakeRenderWorld, pos);
            //ConstructionID mimicID = new ConstructionID(mimicBlock);
            //return extendedBlockState.withProperty(FACADE_ID, mimicBlock).withProperty(FACADE_EXT_STATE, extState);
        }
        return extendedBlockState;
    }

    @Nullable
    private IBlockState getActualMimicBlock(IBlockReader blockAccess, BlockPos pos) {
        try {
            TileEntity te = blockAccess.getTileEntity(pos);
            if (te instanceof ConstructionBlockTileEntity) {
                return ((ConstructionBlockTileEntity) te).getActualBlockState();
            }
            return null;
        } catch (Exception var8) {
            return null;
        }
    }

    protected IExtendedBlockState createBlockState(Block block) {
        IProperty<?>[] listedProperties = new IProperty<?>[]{};
        IUnlistedProperty<?>[] unlistedProperties = new IUnlistedProperty<?>[]{};
        //There's this little comment saying that extended States no longer work... Cause States are now all saved...
        //it doesn't work, beause ExtendedStateHolder is protected
        return new ExtendedStateContainer.Builder<>(block).add(BRIGHT, NEIGHBOR_BRIGHTNESS)/*.add(FACADE_ID, FACADE_EXT_STATE)*/.create((b, map) -> new BlockStateContainer<>());
    }

    @Override
    @Deprecated
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    //@SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return true; // delegated to FacadeBakedModel#getQuads
    }

    /*@Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        IBlockState mimicBlock = getActualMimicBlock(blockAccess, pos);
        return mimicBlock == null ? true : mimicBlock.getBlock().shouldSideBeRendered(mimicBlock, blockAccess, pos, side);
    }*/

    @Override
    @Deprecated
    public boolean isOpaqueCube(IBlockState p_isFullBlock_1_) {
        return false;
    }

    @Override
    @Deprecated
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

    @Override
    @Deprecated
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        IBlockState mimicBlock = getActualMimicBlock(worldIn, pos);
        try {
            return mimicBlock == null ? BlockFaceShape.SOLID : mimicBlock.getBlock().getBlockFaceShape(worldIn, mimicBlock, pos, face);
        } catch (Exception var8) {
            return BlockFaceShape.SOLID;
        }
    }

    @Override
    @Deprecated
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        IBlockState mimicBlock = getActualMimicBlock(worldIn, pos);
        if (mimicBlock == null) {
            super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
        } else {
            try {
                mimicBlock.getBlock().addCollisionBoxToList(mimicBlock, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
            } catch (Exception var8) {
                super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
            }
        }
    }

    @Override
    @Nullable
    @Deprecated
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        IBlockState mimicBlock = getActualMimicBlock(worldIn, pos);
        if (mimicBlock == null) {
            return super.getBoundingBox(blockState, worldIn, pos);
        }
        try {
            return mimicBlock.getBlock().getBoundingBox(mimicBlock, worldIn, pos);
        } catch (Exception var8) {
            return super.getBoundingBox(blockState, worldIn, pos);
        }
    }

    @Override
    @Deprecated
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        FakeRenderWorld fakeWorld = new FakeRenderWorld();

        IBlockState mimicBlock = getActualMimicBlock(blockAccess, pos);
        if (mimicBlock == null) {
            return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
        }
        IBlockState sideBlockState = blockAccess.getBlockState(pos.offset(side));
        if (sideBlockState.getBlock().equals(ModBlocks.constructionBlock)) {
            if (!(getActualMimicBlock(blockAccess, pos.offset(side)) == null)) {
                sideBlockState = getActualMimicBlock(blockAccess, pos.offset(side));
            }
        }

        fakeWorld.setState(blockAccess, mimicBlock, pos);
        fakeWorld.setState(blockAccess, sideBlockState, pos.offset(side));

        try {
            return mimicBlock.getBlock().shouldSideBeRendered(mimicBlock, fakeWorld, pos, side);
        } catch (Exception var8) {
            return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
        }
    }

    @Override
    @Deprecated
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        IBlockState mimicBlock = getActualMimicBlock(source, pos);
        if (mimicBlock == null) {
            return super.getBoundingBox(state, source, pos);
        }
        try {
            return mimicBlock.getBlock().getBoundingBox(mimicBlock, source, pos);
        } catch (Exception var8) {
            return super.getBoundingBox(state, source, pos);
        }
    }

    @Override
    @Deprecated
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
        IBlockState mimicBlock = getActualMimicBlock(worldIn, pos);
        if (mimicBlock == null) {
            return super.getSelectedBoundingBox(state, worldIn, pos);
        }
        try {
            return mimicBlock.getBlock().getSelectedBoundingBox(mimicBlock, worldIn, pos);
        } catch (Exception var8) {
            return super.getSelectedBoundingBox(state, worldIn, pos);
        }
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        IBlockState mimicBlock = getActualMimicBlock(world, pos);
        if (mimicBlock == null) {
            return super.isNormalCube(state, world, pos);
        }
        try {
            return mimicBlock.getBlock().isNormalCube(mimicBlock, world, pos);
        } catch (Exception var8) {
            return super.isNormalCube(state, world, pos);
        }
    }

    @Override
    @Deprecated
    @SideOnly(Side.CLIENT)
    public float getAmbientOcclusionLightValue(IBlockState state) {
        Boolean bright = state.getValue(ConstructionBlock.BRIGHT);
        Boolean neighborBrightness = state.getValue(ConstructionBlock.NEIGHBOR_BRIGHTNESS);
        if (bright || neighborBrightness) {
            return 1f;
        }
        return 0.2f;
    }

    @Override
    @Deprecated
    public boolean getUseNeighborBrightness(IBlockState state) {
        return state.getValue(ConstructionBlock.NEIGHBOR_BRIGHTNESS);
    }

    @Override
    @Deprecated
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(BRIGHT, (meta % 2 == 1))
                .withProperty(NEIGHBOR_BRIGHTNESS, (meta / 2 == 1));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int value = state.getValue(BRIGHT) ? 1 : 0;
        return state.getValue(NEIGHBOR_BRIGHTNESS) ? value + 2 : value;
    }

    /**
     * The below implements support for CTM's Connected Textures to work properly
     *
     * @param world IBlockAccess
     * @param pos BlockPos
     * @param side EnumFacing
     * @return IBlockState
     *
     * @deprecated see {@link IFacade#getFacade(IBlockAccess, BlockPos, EnumFacing, BlockPos)}
     */
    @Override
    @Nonnull
    @Deprecated
    public IBlockState getFacade(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable EnumFacing side) {
        IBlockState mimicBlock = getActualMimicBlock(world, pos);
        return mimicBlock != null ? mimicBlock : world.getBlockState(pos);
        //return mimicBlock;
    }
}