package com.direwolf20.buildinggadgets.client.screen;

import com.direwolf20.buildinggadgets.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.items.TemplateItem;
import com.direwolf20.buildinggadgets.common.util.lang.LangUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;
import java.util.function.Supplier;

public enum GuiMod {
    COPY(GadgetCopyPaste::getGadget, stack -> () -> new CopyGUI(stack)),
    PASTE(GadgetCopyPaste::getGadget, stack -> () -> new PasteGUI(stack)),
    DESTRUCTION(GadgetDestruction::getGadget, stack -> () -> new DestructionGUI(stack)),
    MATERIAL_LIST(TemplateItem::getTemplateItem, stack -> () -> new MaterialListGUI(stack));

    private final Function<Player, ItemStack> stackReader;
    private final Function<ItemStack, Supplier<? extends Screen>> clientScreenProvider;

    GuiMod(Function<Player, ItemStack> stackReader, Function<ItemStack, Supplier<? extends Screen>> clientScreenProvider) {
        this.stackReader = stackReader;
        this.clientScreenProvider = clientScreenProvider;
    }

    public boolean openScreen(Player player) {
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
}
