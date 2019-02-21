package com.direwolf20.buildinggadgets.building;

import com.direwolf20.buildinggadgets.building.placement.SingleTypeProvider;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nonnull;

public class CapabilityBlockProvider {

    @CapabilityInject(IBlockProvider.class)
    public static Capability<IBlockProvider> BLOCK_PROVIDER = null;

    static IBlockProvider DEFAULT_AIR_PROVIDER = new SingleTypeProvider(Blocks.AIR.getDefaultState());

    public CapabilityBlockProvider() {}

    public static void register() {
        CapabilityManager.INSTANCE.register(IBlockProvider.class, new IStorage<IBlockProvider>() {
            @Nonnull
            @Override
            public NBTBase writeNBT(Capability<IBlockProvider> capability, IBlockProvider provider, EnumFacing facing) {
                if (provider instanceof TranslationWrapper) {
                    provider = ((TranslationWrapper) provider).getHandle();
                }

                if (provider instanceof SingleTypeProvider) {
                    return NBTUtil.writeBlockState(new NBTTagCompound(), ((SingleTypeProvider) provider).getBlockState());
                }
                throw new IllegalArgumentException("Cannot deserialize a non-default implementation.");
            }

            @Override
            public void readNBT(Capability<IBlockProvider> capability, IBlockProvider provider, EnumFacing facing, NBTBase nbt) {
                if (provider instanceof TranslationWrapper) {
                    provider = ((TranslationWrapper) provider).getHandle();
                }

                if (provider instanceof SingleTypeProvider) {
                    ((SingleTypeProvider) provider).deserializeNBT((NBTTagCompound) nbt);
                }
                throw new IllegalArgumentException("Cannot serialize a non-default implementation.");
            }
        }, () -> DEFAULT_AIR_PROVIDER);
    }

}
