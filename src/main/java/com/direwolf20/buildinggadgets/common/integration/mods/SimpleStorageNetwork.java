package com.direwolf20.buildinggadgets.common.integration.mods;

import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.direwolf20.buildinggadgets.common.integration.IntegrationHandler.IntegratedMod;
import com.direwolf20.buildinggadgets.common.integration.NetworkProvider;
import com.direwolf20.buildinggadgets.common.tools.NetworkIO;
import com.direwolf20.buildinggadgets.common.tools.NetworkIO.Operation;
import com.direwolf20.buildinggadgets.common.tools.NetworkIO.StackProviderVanilla;

import mrriegel.storagenetwork.api.network.INetworkMaster;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandler;

@IntegratedMod("storagenetwork")
public class SimpleStorageNetwork extends NetworkProvider {

    @Override
    @Nullable
    protected IItemHandler getWrappedNetworkInternal(TileEntity te, EntityPlayer player, Operation operation) {
        if (te instanceof INetworkMaster)
            return new NetworkSimpleStorageNetworkIO(player, (INetworkMaster) te, operation);

        return null;
    }

    public static class NetworkSimpleStorageNetworkIO extends NetworkIO<StackProviderVanilla> {
        private INetworkMaster network;

        public NetworkSimpleStorageNetworkIO(EntityPlayer player, INetworkMaster network, Operation operation) {
            super(player, operation == Operation.INSERT ? null :
                network.getStacks().stream().map(stack -> new StackProviderVanilla(stack.copy())).collect(Collectors.toList()));
            this.network = network;
        }

        @Override
        @Nullable
        public ItemStack insertItemInternal(ItemStack stack, boolean simulate) {
            ItemStack copy = stack.copy();
            int remainder = network.insertStack(stack, simulate);
            if (remainder == 0)
                return null;

            copy.setCount(remainder);
            return copy;
        }

        @Override
        @Nonnull
        protected IStackProvider extractItemInternal(StackProviderVanilla stackProvider, int amount, boolean simulate) {
            return new StackProviderVanilla(network.request(SimpleStorageNetworkAPI.createItemStackMatcher(stackProvider.getStack()), amount, simulate));
        }
    }
}