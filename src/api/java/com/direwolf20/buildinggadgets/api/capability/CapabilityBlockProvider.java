package com.direwolf20.buildinggadgets.api.capability;

import com.direwolf20.buildinggadgets.api.building.IBlockProvider;
import com.direwolf20.buildinggadgets.api.building.SingleTypeProvider;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nonnull;

public final class CapabilityBlockProvider {

    @CapabilityInject(IBlockProvider.class)
    public static Capability<IBlockProvider> BLOCK_PROVIDER = null;

    private static IBlockProvider DEFAULT_AIR_PROVIDER = new SingleTypeProvider(Blocks.AIR.getDefaultState());

    public static IBlockProvider getDefaultAirProvider() {
        return DEFAULT_AIR_PROVIDER;
    }

    private CapabilityBlockProvider() {
    }

    public static void register() {
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
