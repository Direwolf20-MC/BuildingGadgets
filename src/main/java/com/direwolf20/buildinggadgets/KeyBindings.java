package com.direwolf20.buildinggadgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import com.direwolf20.buildinggadgets.items.GenericGadget;

@SideOnly(Side.CLIENT)
public class KeyBindings {

    public static KeyBinding modeSwitch;
    public static KeyBinding rangeChange;
    public static KeyBinding undoKey;
    public static KeyBinding anchorKey;
    private static final KeyConflictContextGadget CONFLICT_CONTEXT_GADGET = new KeyConflictContextGadget();

    public static void init() {
        modeSwitch = new KeyBinding("key.modeSwitch", CONFLICT_CONTEXT_GADGET, Keyboard.KEY_G, "key.categories.buildingGadgets");
        rangeChange = new KeyBinding("key.rangeChange", CONFLICT_CONTEXT_GADGET, Keyboard.KEY_R, "key.categories.buildingGadgets");
        undoKey = new KeyBinding("key.undoKey", CONFLICT_CONTEXT_GADGET, Keyboard.KEY_U, "key.categories.buildingGadgets");
        anchorKey = new KeyBinding("key.anchorKey", CONFLICT_CONTEXT_GADGET, Keyboard.KEY_H, "key.categories.buildingGadgets");
        ClientRegistry.registerKeyBinding(modeSwitch);
        ClientRegistry.registerKeyBinding(rangeChange);
        ClientRegistry.registerKeyBinding(undoKey);
        ClientRegistry.registerKeyBinding(anchorKey);
    }

    public static class KeyConflictContextGadget implements IKeyConflictContext
    {
        @Override
        public boolean isActive() {
            if (!KeyConflictContext.GUI.isActive() && Minecraft.getMinecraft().player != null) {
                for (EnumHand hand : EnumHand.values()) {
                    if (Minecraft.getMinecraft().player.getHeldItem(hand).getItem() instanceof GenericGadget) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return other == this || other == KeyConflictContext.IN_GAME;
        }
    }
}