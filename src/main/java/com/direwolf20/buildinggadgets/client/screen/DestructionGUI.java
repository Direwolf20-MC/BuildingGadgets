package com.direwolf20.buildinggadgets.client.screen;

import com.direwolf20.buildinggadgets.client.screen.widgets.IncrementalSliderWidget;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketDestructionGUI;
import com.direwolf20.buildinggadgets.common.util.lang.GuiTranslation;
import com.direwolf20.buildinggadgets.common.util.lang.MessageTranslation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class DestructionGUI extends Screen {
    private final Set<IncrementalSliderWidget> sliders = new HashSet<>();

    private IncrementalSliderWidget left;
    private IncrementalSliderWidget right;
    private IncrementalSliderWidget up;
    private IncrementalSliderWidget down;
    private IncrementalSliderWidget depth;

    private Button confirm;

    private String sizeString = "";
    private boolean isValidSize = true;

    private final ItemStack destructionGadget;

    public DestructionGUI(ItemStack tool) {
        super(Component.empty());
        this.destructionGadget = tool;
    }

    @Override
    public void init() {
        super.init();

        int x = width / 2;
        int y = height / 2;

        this.addRenderableWidget(confirm = Button.builder(Component.translatable(GuiMod.getLangKeySingle("confirm")), b -> {
            if (Minecraft.getInstance().player == null) {
                return;
            }

            if (isWithinBounds()) {
                PacketHandler.sendToServer(new PacketDestructionGUI(left.getValueInt(), right.getValueInt(), up.getValueInt(), down.getValueInt(), depth.getValueInt()));
                this.onClose();
            } else
                Minecraft.getInstance().player.displayClientMessage(MessageTranslation.DESTRCUT_TOO_LARGE.componentTranslation(Config.GADGETS.GADGET_DESTRUCTION.destroySize.get()), true);
        })
                .pos((x - 30) + 32, y + 65)
                .size(60, 20)
                .build());

        this.addRenderableWidget(Button.builder(Component.translatable(GuiMod.getLangKeySingle("cancel")), b -> onClose())
                .pos((x - 30) - 32, y + 65)
                .size(60, 20)
                .build());

        sliders.clear();
        sliders.add(depth = this.createSlider(x - (70 / 2), y - (14 / 2), GuiTranslation.SINGLE_DEPTH, GadgetDestruction.getToolValue(destructionGadget, "depth")));
        sliders.add(right = this.createSlider(x + (70 + 5), y - (14 / 2), GuiTranslation.SINGLE_RIGHT, GadgetDestruction.getToolValue(destructionGadget, "right")));
        sliders.add(left = this.createSlider(x - (70 * 2) - 5, y - (14 / 2), GuiTranslation.SINGLE_LEFT, GadgetDestruction.getToolValue(destructionGadget, "left")));
        sliders.add(up = this.createSlider(x - (70 / 2), y - 35, GuiTranslation.SINGLE_UP, GadgetDestruction.getToolValue(destructionGadget, "up")));
        sliders.add(down = this.createSlider(x - (70 / 2), y + 20, GuiTranslation.SINGLE_DOWN, GadgetDestruction.getToolValue(destructionGadget, "down")));

        updateSizeString();
        updateIsValid();

        // Adds their buttons to the gui
        sliders.forEach(gui -> gui.getComponents().forEach(this::addRenderableWidget));
    }

    public IncrementalSliderWidget createSlider(int x, int y, GuiTranslation prefix, int value) {
        return new IncrementalSliderWidget(x, y, 70, 14, 0D, 16D, prefix.componentTranslation().append(": "), value, this::onSliderUpdate);
    }

    public void onSliderUpdate(IncrementalSliderWidget widget) {
        this.updateSizeString();
        this.updateIsValid();
    }

    private boolean isWithinBounds() {
        int x = 1 + left.getValueInt() + right.getValueInt();
        int y = 1 + up.getValueInt() + down.getValueInt();
        int z = depth.getValueInt();
        int dim = Config.GADGETS.GADGET_DESTRUCTION.destroySize.get();

        return x <= (dim + 1) && y <= (dim + 1) && z <= dim;
    }

    private String getSizeString() {
        int x = 1 + left.getValueInt() + right.getValueInt();
        int y = 1 + up.getValueInt() + down.getValueInt();
        int z = depth.getValueInt();

        return String.format("%d x %d x %d", x, y, z);
    }

    private void updateIsValid() {
        this.isValidSize = isWithinBounds();
        if (!isValidSize && this.confirm.active) {
            this.confirm.setFGColor(0xFF2000);
            this.confirm.active = false;
        }

        if (isValidSize && !this.confirm.active) {
            this.confirm.clearFGColor();
            this.confirm.active = true;
        }
    }

    private void updateSizeString() {
        this.sizeString = getSizeString();
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        guiGraphics.drawCenteredString(font, this.sizeString, width / 2, (height / 2) + 40, this.isValidSize ? 0x00FF00 : 0xFF2000);
        if (!this.isValidSize) {
            guiGraphics.drawCenteredString(font, MessageTranslation.DESTRCUT_TOO_LARGE.format(Config.GADGETS.GADGET_DESTRUCTION.destroySize.get()), width / 2, (height / 2) + 50, 0xFF2000);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

}
