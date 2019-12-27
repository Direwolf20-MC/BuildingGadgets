package com.direwolf20.buildinggadgets.client;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.gadgets.AbstractGadget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class KeyBindings {

    private static final KeyConflictContextGadget CONFLICT_CONTEXT_GADGET = new KeyConflictContextGadget();
    public static KeyBinding menuSettings;
    public static KeyBinding range;
    public static KeyBinding rotateMirror;
    public static KeyBinding undo;
    public static KeyBinding anchor;
    public static KeyBinding fuzzy;
    public static KeyBinding connectedArea;
    public static KeyBinding dev;

    public static void init() {
        menuSettings = createBinding("settings_menu", Keyboard.KEY_G);
        range = createBinding("range", Keyboard.KEY_R);
        undo = createBinding("undo", Keyboard.KEY_U);
        anchor = createBinding("anchor", Keyboard.KEY_H);
        fuzzy = createBinding("fuzzy", Keyboard.KEY_NONE);
        connectedArea = createBinding("connected_area", Keyboard.KEY_NONE);
        rotateMirror = createBinding("rotate_mirror", Keyboard.KEY_NONE);

        if(BuildingGadgets.getDev().isIsDev())
            dev = createBinding("dev", Keyboard.KEY_BACKSLASH);
    }

    private static KeyBinding createBinding(String name, int key) {
        KeyBinding keyBinding = new KeyBinding("key." + name, CONFLICT_CONTEXT_GADGET, key, "key.categories.buildingGadgets");
        ClientRegistry.registerKeyBinding(keyBinding);
        return keyBinding;
    }

    public static class KeyConflictContextGadget implements IKeyConflictContext
    {
        @Override
        public boolean isActive() {
            return !KeyConflictContext.GUI.isActive() && Minecraft.getMinecraft().player != null
                    && AbstractGadget.getGadget(Minecraft.getMinecraft().player).isPresent();
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return other == this || other == KeyConflictContext.IN_GAME;
        }
    }
}