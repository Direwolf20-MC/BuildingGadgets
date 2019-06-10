/**
 * Parts of this class were adapted from code written by TTerrag for the Chisel mod: https://github.com/Chisel-Team/Chisel
 * Chisel is Open Source and distributed under GNU GPL v2
 */

package com.direwolf20.buildinggadgets.client.gui;

import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketPasteGUI;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.Button.IPressable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class PasteGUI extends GuiScreenTextFields {
    public static final int WIDTH = 256;
    public static final int HEIGHT = 256;

    private GuiTextFieldBase X, Y, Z;

    private int guiLeft = 15;
    private int guiTop = 50;

    private ItemStack tool;

    private static final ResourceLocation background = new ResourceLocation(Reference.MODID, "textures/gui/testcontainer.png");

    PasteGUI(ItemStack tool) {
        super();
        this.tool = tool;
    }

    @Override
    public void init() {
        super.init();

        X = addField(80);
        Y = addField(200);
        Z = addField(320);

        nullCheckTextBoxes();

        addButton(new Button(guiLeft + 80, guiTop + 125, 40, 20, "Ok", (button) -> {
            nullCheckTextBoxes();
            if (GuiMod.sizeCheckBoxes(getFieldIterator(), -16, 16)) {
                PacketHandler.sendToServer(new PacketPasteGUI(Integer.parseInt(X.getText()), Integer.parseInt(Y.getText()), Integer.parseInt(Z.getText())));
                onClose();
            } else {
                Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(TextFormatting.RED + new TranslationTextComponent("message.gadget.destroysizeerror").getUnformattedComponentText()), true);
            }
        }));

        addButton(65, "-", (button) -> fieldChange(X, -1));
        addButton(125, "+", (button) -> fieldChange(X, 1));
        addButton(185, "-", (button) -> fieldChange(Y, -1));
        addButton(245, "+", (button) -> fieldChange(Y, 1));
        addButton(305, "-", (button) -> fieldChange(Z, -1));
        addButton(365, "+", (button) -> fieldChange(Z, 1));
        addButton(new Button(guiLeft + 320, guiTop + 125, 40, 20, "Reset", (button) -> {
            X.setText("0");
            Y.setText("1");
            Z.setText("0");
            sendPacket();
        }));
    }

    private void addButton(int x, String text, IPressable action) {
        addButton(new DireButton(guiLeft + x, guiTop + 99, 10, 10, text, action));
    }

    private GuiTextFieldBase addField(int x) {
        return addField(new GuiTextFieldBase(font, guiLeft + x, guiTop + 100, 40).restrictToNumeric());
    }

    private void sendPacket() {
        PacketHandler.sendToServer(new PacketPasteGUI(X.getInt(), Y.getInt(), Z.getInt()));
    }

    public void fieldChange(GuiTextFieldBase field, int amount) {
        nullCheckTextBoxes();
        if (Screen.hasShiftDown()) amount *= 10;
        int i = MathHelper.clamp(field.getInt() + amount, -16, 16);
        field.setText(String.valueOf(i));
        sendPacket();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        getMinecraft().getTextureManager().bindTexture(background);
        drawFieldLable("X", 55);
        drawFieldLable("Y", 175);
        drawFieldLable("Z", 296);

        super.render(mouseX, mouseY, partialTicks);
    }

    private void drawFieldLable(String name, int x) {
        font.drawStringWithShadow(name, guiLeft + x, guiTop + 100, 0xFFFFFF);
    }

    protected void nullCheckTextBoxes() {
        GuiMod.setEmptyField(X, () -> GadgetCopyPaste.getX(tool));
        GuiMod.setEmptyField(Y, () -> GadgetCopyPaste.getY(tool));
        GuiMod.setEmptyField(Z, () -> GadgetCopyPaste.getZ(tool));
    }
}