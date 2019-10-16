package com.direwolf20.buildinggadgets.common.capability;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.building.BlockData;
import com.direwolf20.buildinggadgets.common.building.IBlockProvider;
import com.direwolf20.buildinggadgets.common.building.SingleTypeProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nonnull;

public final class CapabilityBlockProvider {

    @CapabilityInject(IBlockProvider.class)
    public static Capability<IBlockProvider> BLOCK_PROVIDER = null;

    private static IBlockProvider DEFAULT_AIR_PROVIDER = new SingleTypeProvider(BlockData.AIR);

    public static IBlockProvider getDefaultAirProvider() {
        return DEFAULT_AIR_PROVIDER;
    }

    private CapabilityBlockProvider() {
    }

    public static void register() {
        BuildingGadgets.LOG.debug("Registering BlockProvider Capability");
        CapabilityManager.INSTANCE.register(IBlockProvider.class, new IStorage<IBlockProvider>() {

            @Override
            public void readNBT(Capability<IBlockProvider> capability, IBlockProvider instance, Direction side, INBT nbt) {
                instance.deserialize((CompoundNBT)nbt);
            }

            @Nonnull
            @Override
            public INBT writeNBT(Capability<IBlockProvider> capability, IBlockProvider provider, Direction facing) {
                return provider.serialize();
            }
        }, () -> DEFAULT_AIR_PROVIDER);
    }

}
