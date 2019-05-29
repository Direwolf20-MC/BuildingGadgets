package com.direwolf20.buildinggadgets.common.building;

import com.direwolf20.buildinggadgets.common.building.placement.SingleTypeProvider;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nonnull;

public final class CapabilityBlockProvider {

    @CapabilityInject(IBlockProvider.class)
    public static Capability<IBlockProvider> BLOCK_PROVIDER = null;

    static IBlockProvider DEFAULT_AIR_PROVIDER = new SingleTypeProvider(Blocks.AIR.getDefaultState());

    private CapabilityBlockProvider() {
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(IBlockProvider.class, new IStorage<IBlockProvider>() {
            @Nonnull
            @Override
            public NBTBase writeNBT(Capability<IBlockProvider> capability, IBlockProvider provider, EnumFacing facing) {
                return provider.serialize();
            }

            @Override
            public void readNBT(Capability<IBlockProvider> capability, IBlockProvider provider, EnumFacing facing, NBTBase nbt) {

            }
        }, () -> DEFAULT_AIR_PROVIDER);
    }

}
