package com.direwolf20.buildinggadgets.common.integration;

import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.direwolf20.buildinggadgets.common.integration.IntegrationHandler.IntegratedMod;
import com.direwolf20.buildinggadgets.common.tools.NetworkIO;
import com.direwolf20.buildinggadgets.common.tools.NetworkIO.IStackProvider;
import com.direwolf20.buildinggadgets.common.tools.NetworkIO.Operation;
import com.google.common.collect.Streams;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AEPartLocation;
import appeng.me.helpers.PlayerSource;
import appeng.util.item.AEItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandler;

@IntegratedMod("appliedenergistics2")
public class AppliedEnergistics2 extends NetworkProvider {

    @Override
    @Nullable
    protected IItemHandler getWrappedNetworkInternal(TileEntity te, EntityPlayer player, Operation operation) {
        if (te instanceof IGridHost) {
            IGridNode node = ((IGridHost) te).getGridNode(AEPartLocation.INTERNAL);
            if (node != null) {
                ISecurityGrid security = node.getGrid().getCache(ISecurityGrid.class);
                if (security.hasPermission(player, operation == Operation.EXTRACT ? SecurityPermissions.EXTRACT : SecurityPermissions.INJECT)) {
                    IStorageGrid grid = node.getGrid().getCache(IStorageGrid.class);
                    IMEMonitor<IAEItemStack> network = grid.getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
                    return new NetworkAppliedEnergistics2IO(player, network, operation);
                }
            }
        }
        return null;
    }

    public static class StackProviderAE2 implements IStackProvider {
        @Nonnull
        private IAEItemStack aeStack;

        public StackProviderAE2(@Nonnull IAEItemStack aeStack) {
            this.aeStack = aeStack;
        }

        @Override
        @Nonnull
        public ItemStack getStack() {
            return aeStack.createItemStack();
        }

        public IAEItemStack withSize(int amount) {
            return aeStack.copy().setStackSize(amount);
        }
    }

    private static class NetworkAppliedEnergistics2IO extends NetworkIO<StackProviderAE2> {
        private IMEMonitor<IAEItemStack> network;

        public NetworkAppliedEnergistics2IO(EntityPlayer player, IMEMonitor<IAEItemStack> network, Operation operation) {
            super(player, operation == Operation.INSERT ? null :
                Streams.stream(network.getStorageList()).map(aeStack -> new StackProviderAE2(aeStack)).collect(Collectors.toList()));
            this.network = network;
        }

        @Override
        @Nullable
        public ItemStack insertItemInternal(ItemStack stack, boolean simulate) {
            AEItemStack aeStack = AEItemStack.fromItemStack(stack);
            if (aeStack == null)
                return ItemStack.EMPTY;

            IAEItemStack remainder = network.injectItems(aeStack, getAction(simulate), new PlayerSource(player, null));
            return remainder == null ? null : remainder.createItemStack();
        }

        @Override
        @Nullable
        public ItemStack extractItemInternal(int slot, int amount, boolean simulate) {
            IAEItemStack remainder = network.extractItems(getStackProviderInSlot(slot).withSize(amount), getAction(simulate), new PlayerSource(player, null));
            return remainder == null ? null : remainder.createItemStack();
        }

        private Actionable getAction(boolean simulate) {
            return simulate ? Actionable.SIMULATE : Actionable.MODULATE;
        }
    }
}