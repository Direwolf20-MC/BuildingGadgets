package com.direwolf20.buildinggadgets.common.blocks;

import com.direwolf20.buildinggadgets.common.capability.CapabilityTemplate;
import com.direwolf20.buildinggadgets.common.registry.OurBlocks;
import com.direwolf20.buildinggadgets.common.tiles.TemplateManagerTileEntity;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;

public class TemplateManager extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public TemplateManager(Properties builder) {
        super(builder); //all mc blocks use south as the default...
        setDefaultState(getStateContainer().getBaseState().with(FACING, Direction.SOUTH));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(FACING);
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() != this) {
            GadgetUtils.dropTileEntityInventory(worldIn, pos);
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return OurBlocks.OurTileEntities.TEMPLATE_MANAGER_TYPE.create();
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult blockRayTraceResult) {
        // Only execute on the server
        TileEntity te = worldIn.getTileEntity(pos);
        if (! (te instanceof TemplateManagerTileEntity))
            return false;
        if (worldIn.isRemote)
            return true;
        te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(handler -> {
            worldIn.getCapability(CapabilityTemplate.TEMPLATE_PROVIDER_CAPABILITY).ifPresent(provider -> {
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack itemStack = handler.getStackInSlot(i);
                    itemStack.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).ifPresent(key -> {
                        provider.requestRemoteUpdate(key, PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player));
                    });
                }
            });
        });

        NetworkHooks.openGui((ServerPlayerEntity) player, (TemplateManagerTileEntity) te, pos);
        return true;
    }
}
