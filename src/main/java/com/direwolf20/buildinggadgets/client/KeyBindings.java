package com.direwolf20.buildinggadgets.client;

import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import com.direwolf20.buildinggadgets.common.utils.Reference;
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
        modeSwitch = createBinding("mode_switch", GLFW.GLFW_KEY_G);
        rangeChange = createBinding("range_change", GLFW.GLFW_KEY_R);
        undoKey = createBinding("undo", GLFW.GLFW_KEY_U);
        anchorKey = createBinding("anchor", GLFW.GLFW_KEY_H);
        fuzzyKey = createBinding("fuzzy", GLFW.GLFW_KEY_UNKNOWN);
        connectedAreaKey = createBinding("connected_area", GLFW.GLFW_KEY_UNKNOWN);
    }

    private static KeyBinding createBinding(String name, int key) {
        KeyBinding keyBinding = new KeyBinding(getKey(name), CONFLICT_CONTEXT_GADGET, InputMappings.Type.KEYSYM.getOrMakeInput(key), getKey("category"));
        ClientRegistry.registerKeyBinding(keyBinding);
        return keyBinding;
    }

    private static String getKey(String name) {
        return String.join(".", "key", Reference.MODID, name);
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