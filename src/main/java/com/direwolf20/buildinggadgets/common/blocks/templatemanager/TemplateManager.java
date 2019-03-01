package com.direwolf20.buildinggadgets.common.blocks.templatemanager;

import com.direwolf20.buildinggadgets.client.gui.GuiMod;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;

public class TemplateManager extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public TemplateManager(Properties builder) {
        super(builder);

        this.setDefaultState(this.getStateContainer().getBaseState().with(FACING, EnumFacing.NORTH));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(FACING);
    }

    @Nullable
    @Override
    public IBlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(FACING, context.getPlayer().getHorizontalFacing().getOpposite());
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(IBlockState state, IBlockReader world) {
        return new TemplateManagerTileEntity();
    }

    @Override
    public boolean onBlockActivated(IBlockState state, World worldIn, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        // Only execute on the server
        if (worldIn.isRemote) {
            return true;
        }
        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof TemplateManagerTileEntity)) {
            return false;
        }
//      TODO 1.13
//        TemplateManagerContainer container = ((TemplateManagerTileEntity) te).getContainer(player);
//        for (int i = 0; i <= 1; i++) {
//            ItemStack itemStack = container.getSlot(i).getStack();
//            if (!(itemStack.getItem() instanceof ITemplate)) continue;
//
//            ITemplate template = (ITemplate) itemStack.getItem();
//            String UUID = template.getUUID(itemStack);
//            if (UUID == null) continue;
//
//            NBTTagCompound tagCompound = template.getWorldSave(worldIn).getCompoundFromUUID(UUID);
//            if (tagCompound != null) {
//                PacketHandler.sendTo(new PacketBlockMap(tagCompound), (EntityPlayerMP) player);
//            }
//        }
        GuiMod.TEMPLATE_MANAGER.openContainer(player, worldIn, pos);
        return true;
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TemplateManagerTileEntity)
        {
            tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(iItemHandler -> {
                for (int i = 0; i < iItemHandler.getSlots(); i++)
                {
                    ItemStack stack = iItemHandler.getStackInSlot(i);
                    if (!stack.isEmpty())
                        InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack);
                }
            });
        }

        super.onBlockHarvested(worldIn, pos, state, player);
    }
}
