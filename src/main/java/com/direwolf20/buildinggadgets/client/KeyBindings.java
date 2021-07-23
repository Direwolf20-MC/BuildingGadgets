package com.direwolf20.buildinggadgets.client;

import com.direwolf20.buildinggadgets.common.items.TemplateItem;
import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    private static final KeyConflictContextGadget CONFLICT_CONTEXT_GADGET = new KeyConflictContextGadget();
    public static KeyMapping menuSettings;
    public static KeyMapping range;
    public static KeyMapping rotateMirror;
    public static KeyMapping undo;
    public static KeyMapping anchor;
    public static KeyMapping fuzzy;
    public static KeyMapping connectedArea;
    public static KeyMapping materialList;

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

    private static KeyMapping createBinding(String name, int key) {
        KeyMapping keyBinding = new KeyMapping(getKey(name), CONFLICT_CONTEXT_GADGET, InputConstants.Type.KEYSYM.getOrCreate(key), getKey("category"));
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
            Player player = Minecraft.getInstance().player;
            return !KeyConflictContext.GUI.isActive() && player != null
                    && (!AbstractGadget.getGadget(player).isEmpty()
                        || (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof TemplateItem || player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof TemplateItem));
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return other == this || other == KeyConflictContext.IN_GAME;
        }
    }
}