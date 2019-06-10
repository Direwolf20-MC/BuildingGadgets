package com.direwolf20.buildinggadgets.common.util.helpers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

/**
 * This is a temp class to handle anything that doesn't
 * get have a real method name, variable name, etc.
 *
 * Please use subclass's to make it clear where
 * everything comes from.
 */
public class PortHelper {
    public static class Mc {
        private static Minecraft minecraft = Minecraft.getInstance();

        /**
         * Original .currentScreen
         */
        public static Screen getCurrentScreen() {
            return minecraft.field_71462_r;
        }
    }

    public static class World {
        /**
         * Unfortunately I had to add World as an arg. Please note that
         * the original function `SpawnEntity` did not have this in it.
         *
         * Original ClientWorld.spawnEntity
         */
        public static void spawnEntity(net.minecraft.world.World world, Entity entity) {
           world.func_217376_c(entity);
        }

        /**
         * To be used when IWorld is required
         */
        public static void spawnEntity(IWorld world, Entity entity) {
            world.func_217376_c(entity);
        }
    }
}
