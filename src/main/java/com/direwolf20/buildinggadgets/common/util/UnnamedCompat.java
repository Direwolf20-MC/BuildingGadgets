package com.direwolf20.buildinggadgets.common.util;

import com.direwolf20.buildinggadgets.client.KeyBindings;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.Entity;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.world.IWorld;
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

    @Deprecated
    public static class Mc {
        private static Minecraft minecraft = Minecraft.getInstance();

        public static Screen getCurrentScreen() {
            return minecraft.field_71462_r;
        }
    }

    @Deprecated
    public static class Input {
        // This works but I think I'm using the raw method and not
        // the actual isKeyDown method that we used in 1.13
        public static boolean isKeyDown( long handle, int keyCode ) {
            return InputMappings.func_216506_a( handle, keyCode );
        }
    }
}
