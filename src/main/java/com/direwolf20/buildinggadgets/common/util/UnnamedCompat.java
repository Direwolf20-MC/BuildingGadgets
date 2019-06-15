package com.direwolf20.buildinggadgets.common.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.Entity;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldWriter;

import java.util.Optional;
import java.util.function.Supplier;

@Deprecated //TODO remove once named
public final class UnnamedCompat {
    @Deprecated
    public static final class World {
        public static boolean spawnEntity(IWorldWriter worldWriter, Entity entity) {
            return worldWriter.addEntity(entity);
        }
    }

    @Deprecated
    public static final class DataSerializer {
        public static final IDataSerializer<Integer> VARINT = DataSerializers.VARINT;
        public static final IDataSerializer<Optional<BlockState>> OPTIONAL_BLOCK_STATE = DataSerializers.OPTIONAL_BLOCK_STATE;
        public static final IDataSerializer<Boolean> BOOLEAN = DataSerializers.BOOLEAN;
    }

    @Deprecated
    public static class Mc {
        private static Minecraft minecraft = Minecraft.getInstance();

        public static Screen getCurrentScreen() {
            return minecraft.currentScreen;
        }
    }

    @Deprecated
    public static class Input {
        // This works but I think I'm using the raw method and not
        // the actual isKeyDown method that we used in 1.13
        public static boolean isKeyDown( long handle, int keyCode ) {
            return InputMappings.isKeyDown(handle, keyCode);
        }
    }

    @Deprecated
    public static class BlockPosition {
        public static double distanceSqToCenter(BlockPos pos, double x, double y, double z) {
            // no clue what true does, but it keeps is the same as the old method so :+1:
            return pos.distanceSq(x, y, z, true);
        }
    }

    @Deprecated
    public static class TileEntityType {
        public static <T extends TileEntity> net.minecraft.tileentity.TileEntityType.Builder<T> builder(Supplier<? extends T> factory, Block... validBlocks) {
            return net.minecraft.tileentity.TileEntityType.Builder.create(factory, validBlocks);
        }
    }
}
