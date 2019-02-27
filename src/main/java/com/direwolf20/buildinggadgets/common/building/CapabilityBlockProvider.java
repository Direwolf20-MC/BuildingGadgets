package com.direwolf20.buildinggadgets.common.building;

import com.direwolf20.buildinggadgets.common.building.placement.SingleTypeProvider;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;

public final class CapabilityBlockProvider {

    @CapabilityInject(IBlockProvider.class)
    public static Capability<IBlockProvider> BLOCK_PROVIDER = null;

    static IBlockProvider DEFAULT_AIR_PROVIDER = new SingleTypeProvider(Blocks.AIR.getDefaultState());

    private CapabilityBlockProvider() {}

    public static void register() {
        CapabilityManager.INSTANCE.register(IBlockProvider.class, new IStorage<IBlockProvider>() {
            @Nonnull
            @Override
            public NBTBase writeNBT(Capability<IBlockProvider> capability, IBlockProvider provider, EnumFacing facing) {
                if (provider instanceof TranslationWrapper) {
                    return ((TranslationWrapper) provider).serialize();
                }

                if (provider instanceof INBTSerializable) {
//                    @SuppressWarnings("unchecked") //Safe covariant cast
                    INBTSerializable serializable = (INBTSerializable<NBTBase>) provider;
                    return serializable.serializeNBT();
                }
                if (provider instanceof SingleTypeProvider) {
                    return NBTUtil.writeBlockState(new NBTTagCompound(), ((SingleTypeProvider) provider).getBlockState());
                }
                throw new IllegalArgumentException("Cannot deserialize a non-default implementation.");
            }

            @Override
            public void readNBT(Capability<IBlockProvider> capability, IBlockProvider provider, EnumFacing facing, NBTBase nbt) {
                if (provider instanceof TranslationWrapper) {
                    ((TranslationWrapper) provider).deserialize((NBTTagCompound) nbt);
                }

                if (provider instanceof INBTSerializable) {
//                    @SuppressWarnings("unchecked") //Safe covariant cast
                    INBTSerializable serializable = (INBTSerializable<NBTBase>) provider;

                    serializable.deserializeNBT(nbt);
                }
                if (provider instanceof SingleTypeProvider) {
                    ((SingleTypeProvider) provider).deserialize((NBTTagCompound) nbt);
                }
                throw new IllegalArgumentException("Cannot serialize a non-default implementation.");
            }
        }, () -> DEFAULT_AIR_PROVIDER);
    }

}
