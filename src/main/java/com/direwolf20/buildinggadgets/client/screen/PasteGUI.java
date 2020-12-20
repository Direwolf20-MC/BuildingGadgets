/**
 * Parts of this class were adapted from code written by TTerrag for the Chisel mod: https://github.com/Chisel-Team/Chisel
 * Chisel is Open Source and distributed under GNU GPL v2
 */

package com.direwolf20.buildinggadgets.client.screen;

import com.direwolf20.buildinggadgets.client.screen.components.GuiIncrementer;
import com.direwolf20.buildinggadgets.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketPasteGUI;
import com.direwolf20.buildinggadgets.common.util.lang.GuiTranslation;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;

public class PasteGUI extends Screen {
    private GuiIncrementer X, Y, Z;
    private List<GuiIncrementer> fields = new ArrayList<>();
    private ItemStack copyPasteTool;

    PasteGUI(ItemStack tool) {
        super(new StringTextComponent(""));
        this.copyPasteTool = tool;
    }

    @Override
    public void init() {
        super.init();

        int x = width / 2;
        int y = height / 2;

        fields.add(X = new GuiIncrementer(x - (GuiIncrementer.WIDTH + (GuiIncrementer.WIDTH / 2)) - 10, y - 10, -16, 16, this::onChange));
        fields.add(Y = new GuiIncrementer(x - GuiIncrementer.WIDTH / 2, y - 10, -16, 16, this::onChange));
        fields.add(Z = new GuiIncrementer(x + (GuiIncrementer.WIDTH / 2) + 10, y - 10, -16, 16, this::onChange));

        BlockPos currentOffset = GadgetCopyPaste.getRelativeVector(this.copyPasteTool);
        X.setValue(currentOffset.getX());
        Y.setValue(currentOffset.getY());
        Z.setValue(currentOffset.getZ());

        List<AbstractButton> buttons = new ArrayList<AbstractButton>() {{
            add(new CopyGUI.CenteredButton(y + 20, 70, GuiTranslation.SINGLE_CONFIRM.componentTranslation(), (button) -> {
                PacketHandler.sendToServer(new PacketPasteGUI(X.getValue(), Y.getValue(), Z.getValue()));
                closeScreen();
            }));

            add(new CopyGUI.CenteredButton(y + 20, 40, GuiTranslation.SINGLE_RESET.componentTranslation(), (button) -> {
                X.setValue(0);
                Y.setValue(0);
                Z.setValue(0);
                sendPacket();
            }));
        }};

        CopyGUI.CenteredButton.centerButtonList(buttons, x);

        buttons.forEach(this::addButton);
        fields.forEach(this::addButton);
    }

    private void sendPacket() {
        PacketHandler.sendToServer(new PacketPasteGUI(X.getValue(), Y.getValue(), Z.getValue()));
    }

    private void onChange(int value) {
        PacketHandler.sendToServer(new PacketPasteGUI(X.getValue(), Y.getValue(), Z.getValue()));
    }

    @Override
    public boolean keyPressed(int mouseX, int mouseY, int __unused) {
        fields.forEach(button -> button.keyPressed(mouseX, mouseY, __unused));
        return super.keyPressed(mouseX, mouseY, __unused);
    }

    @Override
    public boolean charTyped(char charTyped, int __unused) {
        fields.forEach(button -> button.charTyped(charTyped, __unused));
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        drawLabel(matrices, "X", -75);
        drawLabel(matrices, "Y", 0);
        drawLabel(matrices, "Z", 75);

        drawCenteredString(matrices, Minecraft.getInstance().fontRenderer, I18n.format(GuiTranslation.COPY_LABEL_HEADING.getTranslationKey()), (int)(width / 2f), (int)(height / 2f) - 60, 0xFFFFFF);

        super.render(matrices, mouseX, mouseY, partialTicks);
    }

    private void drawLabel(MatrixStack matrices, String name, int x) {
        font.drawStringWithShadow(matrices, name, (width / 2f) + x, (height / 2f) - 30, 0xFFFFFF);
    }
}