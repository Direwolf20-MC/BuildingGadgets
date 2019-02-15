package com.direwolf20.buildinggadgets.client;

import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    private static final KeyConflictContextGadget CONFLICT_CONTEXT_GADGET = new KeyConflictContextGadget();
    public static KeyBinding modeSwitch;
    public static KeyBinding rangeChange;
    public static KeyBinding undoKey;
    public static KeyBinding anchorKey;
    public static KeyBinding fuzzyKey;
    public static KeyBinding connectedAreaKey;

    public static void init() {
        modeSwitch = createBinding("key.modeSwitch", GLFW.GLFW_KEY_G);
        rangeChange = createBinding("key.rangeChange", GLFW.GLFW_KEY_R);
        undoKey = createBinding("key.undoKey", GLFW.GLFW_KEY_U);
        anchorKey = createBinding("key.anchorKey", GLFW.GLFW_KEY_H);
        fuzzyKey = createBinding("key.fuzzyKey", GLFW.GLFW_KEY_UNKNOWN);
        connectedAreaKey = createBinding("key.connectedarea", GLFW.GLFW_KEY_UNKNOWN);
    }

    private static KeyBinding createBinding(String name, int key) {
        KeyBinding keyBinding = new KeyBinding("key" + name, CONFLICT_CONTEXT_GADGET, InputMappings.Type.KEYSYM.getOrMakeInput(key), "key.categories.buildingGadgets");
        ClientRegistry.registerKeyBinding(keyBinding);
        return keyBinding;
    }

    public static class KeyConflictContextGadget implements IKeyConflictContext
    {
        @Override
        public boolean isActive() {
            return !KeyConflictContext.GUI.isActive() && Minecraft.getInstance().player != null
                    && !GadgetGeneric.getGadget(Minecraft.getInstance().player).isEmpty();
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return other == this || other == KeyConflictContext.IN_GAME;
        }
    }
}