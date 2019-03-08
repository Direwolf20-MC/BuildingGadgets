package com.direwolf20.buildinggadgets.common.integration;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.direwolf20.buildinggadgets.common.integration.IntegrationHandler.IIntegratedMod;
import com.direwolf20.buildinggadgets.common.tools.NetworkIO.Operation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandler;

public abstract class NetworkProvider implements IIntegratedMod {
    private boolean isLoaded = true;
    private static final Set<NetworkProvider> PROVIDERS = new HashSet<>();

    @Override
    public void preInit() {
        isLoaded = true;
        PROVIDERS.add(this);
    }

    @Nullable
    protected abstract IItemHandler getWrappedNetworkInternal(TileEntity te, EntityPlayer player, Operation operation);

    @Nullable
    private IItemHandler getWrappedNetworkIfLoaded(TileEntity te, EntityPlayer player, Operation operation) {
        return !isLoaded ? null : getWrappedNetworkInternal(te, player, operation);
    }

    @Nullable
    public static IItemHandler getWrappedNetwork(TileEntity te, EntityPlayer player, Operation operation) {
        IItemHandler network = null;
        for (NetworkProvider provider : PROVIDERS) {
            network = provider.getWrappedNetworkIfLoaded(te, player, operation);
            if (network != null) break;
        }
        return network;
    }
}