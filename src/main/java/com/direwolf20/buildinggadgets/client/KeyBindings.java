package com.direwolf20.buildinggadgets.client;

import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import com.direwolf20.buildinggadgets.common.utils.ref.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    private static final KeyConflictContextGadget CONFLICT_CONTEXT_GADGET = new KeyConflictContextGadget();
    public static KeyBinding menuSettings;
    public static KeyBinding range;
    public static KeyBinding rotateMirror;
    public static KeyBinding undo;
    public static KeyBinding anchor;
    public static KeyBinding fuzzy;
    public static KeyBinding connectedArea;
    public static KeyBinding materialList;

    public static void init() {
        menuSettings = createBinding("settings_menu", GLFW.GLFW_KEY_G);
        range = createBinding("range", GLFW.GLFW_KEY_R);
        undo = createBinding("undo", GLFW.GLFW_KEY_U);
        anchor = createBinding("anchor", GLFW.GLFW_KEY_H);
        fuzzy = createBinding("fuzzy", GLFW.GLFW_KEY_UNKNOWN);
        connectedArea = createBinding("connected_area", GLFW.GLFW_KEY_UNKNOWN);
        rotateMirror = createBinding("rotate_mirror", GLFW.GLFW_KEY_UNKNOWN);
        materialList = createBinding("material_list", GLFW.GLFW_KEY_M);
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