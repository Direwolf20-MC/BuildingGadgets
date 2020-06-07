package com.direwolf20.buildinggadgets.client;

import com.direwolf20.buildinggadgets.common.helpers.LangHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.ForgeI18n;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

public final class KeyBindings {

    private static final KeyConflicts CONFLICT_CONTEXT = new KeyConflicts();
    public static KeyBinding menuSettings;
    public static KeyBinding range;

    /**
     * Register the keybindings we need for the mod.
     */
    public static void init() {
        menuSettings = createBinding("settings_menu", GLFW.GLFW_KEY_G);
        range = createBinding("range", GLFW.GLFW_KEY_R);

//        undo = createBinding("undo", GLFW.GLFW_KEY_U);
//        anchor = createBinding("anchor", GLFW.GLFW_KEY_H);
//        fuzzy = createBinding("fuzzy", GLFW.GLFW_KEY_UNKNOWN);
//        connectedArea = createBinding("connected_area", GLFW.GLFW_KEY_UNKNOWN);
//        rotateMirror = createBinding("rotate_mirror", GLFW.GLFW_KEY_UNKNOWN);
//        materialList = createBinding("material_list", GLFW.GLFW_KEY_M);
    }

    /**
     * Helper method to create key bindings and register them at the same time
     *
     * @param name Lang name
     * @param key  Key to be pressed
     * @return     Registered Key Binding Object
     */
    private static KeyBinding createBinding(String name, int key) {
        KeyBinding keyBinding = new KeyBinding(String.format("key.%s", name), CONFLICT_CONTEXT, InputMappings.Type.KEYSYM.getOrMakeInput(key), "category");
        ClientRegistry.registerKeyBinding(keyBinding);
        return keyBinding;
    }

    /**
     * Called from {@link Events} which handles valid player checking
     *
     * @param event key event
     */
    public static void onKeyPressed(InputEvent.KeyInputEvent event) {
        System.out.println(menuSettings.isPressed());
    }

    /**
     * Handles Key conflicts between mods and gives us a simple location to add validation
     * on if they key should be triggered or not.
     */
    private final static class KeyConflicts implements IKeyConflictContext
    {
        @Override
        public boolean isActive() {
            PlayerEntity player = Minecraft.getInstance().player;
            return !KeyConflictContext.GUI.isActive() && player != null;
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return other == this || other == KeyConflictContext.IN_GAME;
        }
    }
}
