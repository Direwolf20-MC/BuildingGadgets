//package com.direwolf20.buildinggadgets.common.integration;
//
//import com.direwolf20.buildinggadgets.common.integration.IntegrationHandler.IIntegratedMod;
//import com.direwolf20.buildinggadgets.common.integration.IntegrationHandler.IntegratedMod;
//import com.direwolf20.buildinggadgets.common.util.tools.NetworkIO;
//import net.minecraft.tileentity.TileEntity;
//import net.minecraftforge.old_items.IItemHandler;
//
//import javax.annotation.Nullable;
//
///*
//import com.direwolf20.buildinggadgets.common.util.tools.NetworkIO.NetworkRefinedStorageIO;
//import com.raoulvdberge.refinedstorage.api.network.INetwork;
//import com.raoulvdberge.refinedstorage.api.network.node.INetworkNodeProxy;
//*/
//
//@IntegratedMod("refinedstorage")
//public class RefinedStorage implements IIntegratedMod {
//    private static boolean isLoaded;
//
//    @Override
//    public void initialize() {
//        isLoaded = true;
//    }
//
//    @Nullable
//    public static IItemHandler getWrappedNetwork(TileEntity te, NetworkIO.Operation operation) {
//        /*if (isLoaded && te instanceof INetworkNodeProxy) {
//            INetwork network = ((INetworkNodeProxy) te).getNode().getNetwork();
//            if (network != null)
//                return new NetworkRefinedStorageIO(network, operation);
//        }*/
//        return null;
//    }
//}
