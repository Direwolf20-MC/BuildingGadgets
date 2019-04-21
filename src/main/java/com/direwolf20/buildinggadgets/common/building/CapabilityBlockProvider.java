package com.direwolf20.buildinggadgets.common.building;

import com.direwolf20.buildinggadgets.common.building.placement.SingleTypeProvider;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.INBTBase;
import net.minecraft.nbt.NBTTagCompound;
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

            @Override
            public void readNBT(Capability<IBlockProvider> capability, IBlockProvider instance, EnumFacing side, INBTBase nbt) {
                instance.deserialize((NBTTagCompound)nbt);
            }

            @Nonnull
            @Override
            public INBTBase writeNBT(Capability<IBlockProvider> capability, IBlockProvider provider, EnumFacing facing) {
                return provider.serialize();
            }
        }, () -> DEFAULT_AIR_PROVIDER);
    }

}
