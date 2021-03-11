package com.direwolf20.buildinggadgets.client.screen;

import com.direwolf20.buildinggadgets.common.items.TemplateItem;
import com.direwolf20.buildinggadgets.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.util.lang.LangUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.function.Function;
import java.util.function.Supplier;

public enum GuiMod {
    COPY(GadgetCopyPaste::getGadget, stack -> () -> new CopyGUI(stack)),
    PASTE(GadgetCopyPaste::getGadget, stack -> () -> new PasteGUI(stack)),
    DESTRUCTION(GadgetDestruction::getGadget, stack -> () -> new DestructionGUI(stack)),
    MATERIAL_LIST(TemplateItem::getTemplateItem, stack -> () -> new ChangeMaterialsScreen(stack));

    private Function<PlayerEntity, ItemStack> stackReader;
    private Function<ItemStack, Supplier<? extends Screen>> clientScreenProvider;

    GuiMod(Function<PlayerEntity, ItemStack> stackReader, Function<ItemStack, Supplier<? extends Screen>> clientScreenProvider) {
        this.stackReader = stackReader;
        this.clientScreenProvider = clientScreenProvider;
    }

    // fixme: 1.14 requires this but I'm not sure on how to implement it.
    public static Screen openScreen(Minecraft minecraft, Screen screen) {
        return screen;
    }

    public boolean openScreen(PlayerEntity player) {
        if (clientScreenProvider == null)
            return false;

        ItemStack stack = stackReader.apply(player);
        if (stack == null || stack.isEmpty())
            return false;

        Screen screen = clientScreenProvider.apply(stack).get();
        Minecraft.getInstance().setScreen(screen);
        return screen == null;
    }

    public static String getLangKeySingle(String name) {
        return LangUtil.getLangKey("gui", "single", name);
    }

    public static Color getColor(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }
}
