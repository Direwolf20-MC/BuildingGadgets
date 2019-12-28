package com.direwolf20.buildinggadgets.common.integration.mods;

import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.direwolf20.buildinggadgets.common.integration.IntegrationHandler.IntegratedMod;
import com.direwolf20.buildinggadgets.common.integration.NetworkProvider;
import com.direwolf20.buildinggadgets.common.utils.NetworkIO;
import com.direwolf20.buildinggadgets.common.utils.NetworkIO.Operation;
import com.direwolf20.buildinggadgets.common.utils.NetworkIO.StackProviderVanilla;
import com.raoulvdberge.refinedstorage.api.network.INetwork;
import com.raoulvdberge.refinedstorage.api.network.node.INetworkNodeProxy;
import com.raoulvdberge.refinedstorage.api.network.security.Permission;
import com.raoulvdberge.refinedstorage.api.util.Action;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandler;

@IntegratedMod("refinedstorage")
public class RefinedStorage extends NetworkProvider {

    @Override
    @Nullable
    protected IItemHandler getWrappedNetworkInternal(TileEntity te, EntityPlayer player, Operation operation) {
        if (te instanceof INetworkNodeProxy) {
            INetwork network = ((INetworkNodeProxy) te).getNode().getNetwork();
            if (network != null && network.getSecurityManager().hasPermission(operation == Operation.EXTRACT ? Permission.EXTRACT : Permission.INSERT, player))
                return new NetworkRefinedStorageIO(player, network, operation);
        }
        return null;
    }

    private static class NetworkRefinedStorageIO extends NetworkIO<StackProviderVanilla> {
        private INetwork network;

        public NetworkRefinedStorageIO(EntityPlayer player, INetwork network, Operation operation) {
            super(player, operation == Operation.INSERT ? null :
                network.getItemStorageCache().getList().getStacks().stream().map(stack -> new StackProviderVanilla(stack.copy())).collect(Collectors.toList()));
            this.network = network;
        }

        @Override
        @Nullable
        public ItemStack insertItemInternal(ItemStack stack, boolean simulate) {
            return network.insertItem(stack, stack.getCount(), getAction(simulate));
        }

        @Override
        @Nonnull
        protected IStackProvider extractItemInternal(StackProviderVanilla stackProvider, int amount, boolean simulate) {
            return new StackProviderVanilla(network.extractItem(stackProvider.getStack(), amount, getAction(simulate)));
        }

        private Action getAction(boolean simulate) {
            return simulate ? Action.SIMULATE : Action.PERFORM;
        }
    }
}