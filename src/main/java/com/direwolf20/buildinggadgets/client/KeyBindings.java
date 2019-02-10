package com.direwolf20.buildinggadgets.client;

import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
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
    public static KeyBinding modeSwitch;
    public static KeyBinding rangeChange;
    public static KeyBinding undoKey;
    public static KeyBinding anchorKey;
    public static KeyBinding fuzzyKey;
    public static KeyBinding connectedAreaKey;

    public static void init() {
        modeSwitch = createBinding("key.modeSwitch", Keyboard.KEY_G);
        rangeChange = createBinding("key.rangeChange", Keyboard.KEY_R);
        undoKey = createBinding("key.undoKey", Keyboard.KEY_U);
        anchorKey = createBinding("key.anchorKey", Keyboard.KEY_H);
        fuzzyKey = createBinding("key.fuzzyKey", Keyboard.KEY_NONE);
        connectedAreaKey = createBinding("key.connectedarea", Keyboard.KEY_NONE);
    }

    private static KeyBinding createBinding(String name, int key) {
        KeyBinding keyBinding = new KeyBinding(name, CONFLICT_CONTEXT_GADGET, key, "key.categories.buildingGadgets");
        ClientRegistry.registerKeyBinding(keyBinding);
        return keyBinding;
    }

    public static class KeyConflictContextGadget implements IKeyConflictContext
    {
        @Override
        public boolean isActive() {
            return !KeyConflictContext.GUI.isActive() && Minecraft.getMinecraft().player != null
                    && !GadgetGeneric.getGadget(Minecraft.getMinecraft().player).isEmpty();
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return other == this || other == KeyConflictContext.IN_GAME;
        }
    }
}