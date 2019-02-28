package com.direwolf20.buildinggadgets.common.integration;

import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.direwolf20.buildinggadgets.common.integration.IntegrationHandler.IntegratedMod;
import com.direwolf20.buildinggadgets.common.tools.NetworkIO;
import com.direwolf20.buildinggadgets.common.tools.NetworkIO.Operation;

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

    public static class NetworkSimpleStorageNetworkIO extends NetworkIO {
        private INetworkMaster network;

        public NetworkSimpleStorageNetworkIO(EntityPlayer player, INetworkMaster network, Operation operation) {
            super(player, operation == Operation.INSERT ? null :
                network.getStacks().stream().map(stack -> new StackProviderVanilla(stack)).collect(Collectors.toList()));
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
        @Nullable
        public ItemStack extractItemInternal(int slot, int amount, boolean simulate) {
            return network.request(SimpleStorageNetworkAPI.createItemStackMatcher(getStackInSlot(slot)), amount, simulate);
        }
    }
}