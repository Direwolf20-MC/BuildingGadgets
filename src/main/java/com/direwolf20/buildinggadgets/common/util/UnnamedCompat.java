package com.direwolf20.buildinggadgets.common.util;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.world.IWorldWriter;

import java.util.Optional;

@Deprecated //TODO remove once named
public final class UnnamedCompat {
    @Deprecated
    public static final class World {
        public static boolean spawnEntity(IWorldWriter worldWriter, Entity entity) {
            return worldWriter.func_217376_c(entity);
        }
    }

    @Deprecated
    public static final class DataSerializer {
        public static final IDataSerializer<Integer> VARINT = DataSerializers.field_187192_b;
        public static final IDataSerializer<Optional<BlockState>> OPTIONAL_BLOCK_STATE = DataSerializers.field_187197_g;
        public static final IDataSerializer<Boolean> BOOLEAN = DataSerializers.field_187198_h;
    }
}
