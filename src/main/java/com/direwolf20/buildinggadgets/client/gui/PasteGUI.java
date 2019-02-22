/**
 * Parts of this class were adapted from code written by TTerrag for the Chisel mod: https://github.com/Chisel-Team/Chisel
 * Chisel is Open Source and distributed under GNU GPL v2
 */

package com.direwolf20.buildinggadgets.client.gui;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketPasteGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class PasteGUI extends GuiScreenTextFields {
    public static final int WIDTH = 256;
    public static final int HEIGHT = 256;

    private GuiTextFieldBase X, Y, Z;

    private int guiLeft = 15;
    private int guiTop = 50;

    private ItemStack tool;

    private static final ResourceLocation background = new ResourceLocation(BuildingGadgets.MODID, "textures/gui/testcontainer.png");

    PasteGUI(ItemStack tool) {
        super();
        this.tool = tool;
    }

    @Override
    public void initGui() {
        super.initGui();

        X = addField(80);
        Y = addField(200);
        Z = addField(320);

        nullCheckTextBoxes();

        addButton(new GuiButtonAction(guiLeft + 80, guiTop + 125, 40, 20, "Ok", () -> {
            nullCheckTextBoxes();
            if (sizeCheckBoxes()) {
                PacketHandler.sendToServer(new PacketPasteGUI(Integer.parseInt(X.getText()), Integer.parseInt(Y.getText()), Integer.parseInt(Z.getText())));
                close();
            } else {
                Minecraft.getInstance().player.sendStatusMessage(new TextComponentString(TextFormatting.RED + new TextComponentTranslation("message.gadget.destroysizeerror").getUnformattedComponentText()), true);
            }
        }));
        addButton(new DireButton(guiLeft + 65, guiTop + 99, 10, 10, "-", () -> {
            fieldChange(X, -1);
            PacketHandler.sendToServer(new PacketPasteGUI(Integer.parseInt(X.getText()), Integer.parseInt(Y.getText()), Integer.parseInt(Z.getText())));
        }));
        addButton(new DireButton(guiLeft + 125, guiTop + 99, 10, 10, "+", () -> {
            fieldChange(X, 1);
            PacketHandler.sendToServer(new PacketPasteGUI(Integer.parseInt(X.getText()), Integer.parseInt(Y.getText()), Integer.parseInt(Z.getText())));
        }));
        addButton(new DireButton(guiLeft + 185, guiTop + 99, 10, 10, "-", () -> {
            fieldChange(Y, -1);
            PacketHandler.sendToServer(new PacketPasteGUI(Integer.parseInt(X.getText()), Integer.parseInt(Y.getText()), Integer.parseInt(Z.getText())));
        }));
        addButton(new DireButton(guiLeft + 245, guiTop + 99, 10, 10, "+", () -> {
            fieldChange(Y, 1);
            PacketHandler.sendToServer(new PacketPasteGUI(Integer.parseInt(X.getText()), Integer.parseInt(Y.getText()), Integer.parseInt(Z.getText())));
        }));
        addButton(new DireButton(guiLeft + 305, guiTop + 99, 10, 10, "-", () -> {
            fieldChange(Z, -1);
            PacketHandler.sendToServer(new PacketPasteGUI(Integer.parseInt(X.getText()), Integer.parseInt(Y.getText()), Integer.parseInt(Z.getText())));
        }));
        addButton(new DireButton(guiLeft + 365, guiTop + 99, 10, 10, "+", () -> {
            fieldChange(Z, 1);
            PacketHandler.sendToServer(new PacketPasteGUI(Integer.parseInt(X.getText()), Integer.parseInt(Y.getText()), Integer.parseInt(Z.getText())));
        }));
        addButton(new GuiButtonAction(guiLeft + 320, guiTop + 125, 40, 20, "Reset", () -> {
            X.setText(String.valueOf(0));
            Y.setText(String.valueOf(1));
            Z.setText(String.valueOf(0));
            PacketHandler.sendToServer(new PacketPasteGUI(Integer.parseInt(X.getText()), Integer.parseInt(Y.getText()), Integer.parseInt(Z.getText())));
        }));
    }

    private GuiTextFieldBase addField(int x) {
        return addField(new GuiTextFieldBase(fontRenderer, guiLeft + x, guiTop + 100, 40));
    }

    public void fieldChange(GuiTextField textField, int amount) {
        nullCheckTextBoxes();
        if (GuiScreen.isShiftKeyDown()) amount = amount * 10;
        try {
            int i = Integer.valueOf(textField.getText()) + amount;
            if (i < -16) i = -16;
            if (i > 16) i = 16;
            textField.setText(String.valueOf(i));
        } catch (NumberFormatException t) {
            close();
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        mc.getTextureManager().bindTexture(background);
        drawFieldLable("X", 55);
        drawFieldLable("Y", 175);
        drawFieldLable("Z", 296);

        super.render(mouseX, mouseY, partialTicks);
    }

    private void drawFieldLable(String name, int x) {
        fontRenderer.drawStringWithShadow(name, guiLeft + x, guiTop + 100, 0xFFFFFF);
    }

    protected void nullCheckTextBoxes() {
        if (X.getText().isEmpty()) {
            X.setText(String.valueOf(GadgetCopyPaste.getX(tool)));
        }
        if (Y.getText().isEmpty()) {
            Y.setText(String.valueOf(GadgetCopyPaste.getY(tool)));
        }
        if (Z.getText().isEmpty()) {
            Z.setText(String.valueOf(GadgetCopyPaste.getZ(tool)));
        }
    }

    private boolean sizeCheckBoxes() {
        if (Integer.parseInt(Z.getText()) > 16) return false;
        if (Integer.parseInt(Z.getText()) < -16) return false;
        if (Integer.parseInt(Y.getText()) < -16) return false;
        if (Integer.parseInt(Y.getText()) > 16) return false;
        if (Integer.parseInt(X.getText()) < -16) return false;
        return Integer.parseInt(X.getText()) <= 16;
    }
}