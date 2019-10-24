package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.capability.CapabilityProviderEnergy;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.registry.OurBlocks;
import com.direwolf20.buildinggadgets.common.tiles.ChargingStationTileEntity;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nullable;
import java.util.List;

/**
 * This Item specifically only stores the energy of the ChargingStation, even though vanilla already supports keeping everything
 * using the {@link #setTileEntityNBT(World, PlayerEntity, BlockPos, ItemStack) "BlockEntityTag"}. This is to avoid making the
 * charging station a container for charged Gadgets and to avoid copying the current remaining burn time.
 */
public class ChargingStationItem extends BlockItem {
    public ChargingStationItem(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    public ChargingStationItem(Properties builder) {
        this(OurBlocks.chargingStation, builder);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        stack.getCapability(CapabilityEnergy.ENERGY).ifPresent(energy -> {
            tooltip.add(TooltipTranslation.CHARGER_ENERGY.componentTranslation(energy.getEnergyStored()));
        });
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new CapabilityProviderEnergy(stack, Config.CHARGING_STATION.capacity::get);
    }

    @Override
    public ActionResultType tryPlace(BlockItemUseContext context) {
        return super.tryPlace(context);
    }

    @Nullable
    @Override
    public BlockItemUseContext getBlockItemUseContext(BlockItemUseContext context) {
        return super.getBlockItemUseContext(context);
    }

    @Override
    protected boolean onBlockPlaced(BlockPos pos, World worldIn, @Nullable PlayerEntity player, ItemStack stack, BlockState state) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof ChargingStationTileEntity) {
            ChargingStationTileEntity station = (ChargingStationTileEntity) te;
            station.onInitEnergy(stack);
        }
        return super.onBlockPlaced(pos, worldIn, player, stack, state);
    }
}
