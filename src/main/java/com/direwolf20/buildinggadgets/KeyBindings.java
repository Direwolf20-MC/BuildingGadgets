package com.direwolf20.buildinggadgets;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class KeyBindings {

    public static KeyBinding modeSwitch;
    public static KeyBinding rangeChange;
    public static KeyBinding undoKey;

    public static void init() {
        modeSwitch = new KeyBinding("key.modeSwitch", KeyConflictContext.IN_GAME, Keyboard.KEY_SEMICOLON, "key.categories.buildingGadgets");
        rangeChange = new KeyBinding("key.rangeChange", KeyConflictContext.IN_GAME, Keyboard.KEY_APOSTROPHE, "key.categories.buildingGadgets");
        undoKey = new KeyBinding("key.undoKey", KeyConflictContext.IN_GAME, Keyboard.KEY_U, "key.categories.buildingGadgets");
        ClientRegistry.registerKeyBinding(modeSwitch);
        ClientRegistry.registerKeyBinding(rangeChange);
        ClientRegistry.registerKeyBinding(undoKey);
    }
}