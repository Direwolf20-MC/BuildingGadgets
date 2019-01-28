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
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class PasteGUI extends GuiScreen {
    public static final int WIDTH = 256;
    public static final int HEIGHT = 256;

    private GuiTextField X;
    private GuiTextField Y;
    private GuiTextField Z;


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

        X = new GuiTextField(0, this.fontRenderer, this.guiLeft + 80, this.guiTop + 100, 40, this.fontRenderer.FONT_HEIGHT);
        X.setMaxStringLength(50);
        X.setVisible(true);

        Y = new GuiTextField(1, this.fontRenderer, this.guiLeft + 200, this.guiTop + 100, 40, this.fontRenderer.FONT_HEIGHT);
        Y.setMaxStringLength(50);
        Y.setVisible(true);

        Z = new GuiTextField(2, this.fontRenderer, this.guiLeft + 320, this.guiTop + 100, 40, this.fontRenderer.FONT_HEIGHT);
        Z.setMaxStringLength(50);
        Z.setVisible(true);

        nullCheckTextBoxes();

        this.buttons.add(new GuiButton(1, this.guiLeft + 80, this.guiTop + 125, 40, 20, "Ok") {
            @Override
            public void onClick(double mouseX, double mouseY) {
                nullCheckTextBoxes();
                if (sizeCheckBoxes()) {
                PacketHandler.sendToServer(new PacketPasteGUI(Integer.parseInt(X.getText()), Integer.parseInt(Y.getText()), Integer.parseInt(Z.getText())));
                    mc.displayGuiScreen(null);
                } else {
                    Minecraft.getInstance().player.sendStatusMessage(new TextComponentString(TextFormatting.RED + new TextComponentTranslation("message.gadget.destroysizeerror").getUnformattedComponentText()), true);
                }
                super.onClick(mouseX, mouseY);
            }
        });
        //this.buttonList.add(new GuiButton(2, this.guiLeft + 200, this.guiTop + 125, 40, 20, "Cancel"));
        this.buttons.add(new DireButton(3, this.guiLeft + 65, this.guiTop + 99, 10, 10, "-") {
            @Override
            public void onClick(double mouseX, double mouseY) {
                fieldChange(X, -1);
                PacketHandler.sendToServer(new PacketPasteGUI(Integer.parseInt(X.getText()), Integer.parseInt(Y.getText()), Integer.parseInt(Z.getText())));
                super.onClick(mouseX, mouseY);
            }
        });
        this.buttons.add(new DireButton(4, this.guiLeft + 125, this.guiTop + 99, 10, 10, "+"){
            @Override
            public void onClick(double mouseX, double mouseY) {
                fieldChange(X, 1);
                PacketHandler.sendToServer(new PacketPasteGUI(Integer.parseInt(X.getText()), Integer.parseInt(Y.getText()), Integer.parseInt(Z.getText())));
                super.onClick(mouseX, mouseY);
            }
        });
        this.buttons.add(new DireButton(5, this.guiLeft + 185, this.guiTop + 99, 10, 10, "-"){
            @Override
            public void onClick(double mouseX, double mouseY) {
                fieldChange(Y, -1);
                PacketHandler.sendToServer(new PacketPasteGUI(Integer.parseInt(X.getText()), Integer.parseInt(Y.getText()), Integer.parseInt(Z.getText())));
                super.onClick(mouseX, mouseY);
            }
        });
        this.buttons.add(new DireButton(6, this.guiLeft + 245, this.guiTop + 99, 10, 10, "+"){
            @Override
            public void onClick(double mouseX, double mouseY) {
                fieldChange(X, 1);
                PacketHandler.sendToServer(new PacketPasteGUI(Integer.parseInt(X.getText()), Integer.parseInt(Y.getText()), Integer.parseInt(Z.getText())));
                super.onClick(mouseX, mouseY);
            }
        });
        this.buttons.add(new DireButton(7, this.guiLeft + 305, this.guiTop + 99, 10, 10, "-"){
            @Override
            public void onClick(double mouseX, double mouseY) {
                fieldChange(Z, -1);
                PacketHandler.sendToServer(new PacketPasteGUI(Integer.parseInt(X.getText()), Integer.parseInt(Y.getText()), Integer.parseInt(Z.getText())));
                super.onClick(mouseX, mouseY);
            }
        });
        this.buttons.add(new DireButton(8, this.guiLeft + 365, this.guiTop + 99, 10, 10, "+"){
            @Override
            public void onClick(double mouseX, double mouseY) {
                fieldChange(Z, 1);
                PacketHandler.sendToServer(new PacketPasteGUI(Integer.parseInt(X.getText()), Integer.parseInt(Y.getText()), Integer.parseInt(Z.getText())));
                super.onClick(mouseX, mouseY);
            }
        });
        this.buttons.add(new GuiButton(9, this.guiLeft + 320, this.guiTop + 125, 40, 20, "Reset") {
            @Override
            public void onClick(double mouseX, double mouseY) {
                X.setText(String.valueOf(0));
                Y.setText(String.valueOf(1));
                Z.setText(String.valueOf(0));
                PacketHandler.sendToServer(new PacketPasteGUI(Integer.parseInt(X.getText()), Integer.parseInt(Y.getText()), Integer.parseInt(Z.getText())));

                super.onClick(mouseX, mouseY);
            }
        });
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
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        mc.getTextureManager().bindTexture(background);
        //drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        this.X.drawTextField(mouseX, mouseY, partialTicks);
        this.Y.drawTextField(mouseX, mouseY, partialTicks);
        this.Z.drawTextField(mouseX, mouseY, partialTicks);
        fontRenderer.drawStringWithShadow("X", this.guiLeft + 55, this.guiTop + 100, 0xFFFFFF);
        fontRenderer.drawStringWithShadow("Y", this.guiLeft + 175, this.guiTop + 100, 0xFFFFFF);
        fontRenderer.drawStringWithShadow("Z", this.guiLeft + 296, this.guiTop + 100, 0xFFFFFF);

        super.render(mouseX, mouseY, partialTicks);
    }

    private void nullCheckTextBoxes() {
        if (X.getText().equals("")) {
            X.setText(String.valueOf(GadgetCopyPaste.getX(tool)));
        }
        if (Y.getText().equals("")) {
            Y.setText(String.valueOf(GadgetCopyPaste.getY(tool)));
        }
        if (Z.getText().equals("")) {
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

    @Override
    public boolean keyPressed(int p1, int p2, int p3) {
        if (this.X.keyPressed(p1, p2, p3) || this.Y.keyPressed(p1, p2, p3) || this.Z.keyPressed(p1, p2, p3)) {
            return false;
        } else {
            return super.keyPressed(p1, p2, p3);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == 1) {
            if (this.X.mouseClicked(mouseX, mouseY, 0)) {
                X.setText("");
            } else if (this.Y.mouseClicked(mouseX, mouseY, 0)) {
                Y.setText("");
            } else if (this.Z.mouseClicked(mouseX, mouseY, 0)) {
                Z.setText("");
            } else {
                //startX.setFocused(false);
                super.mouseClicked(mouseX, mouseY, mouseButton);
            }
        } else {
            if (this.X.mouseClicked(mouseX, mouseY, mouseButton)) {
                X.setFocused(true);
            } else if (this.Y.mouseClicked(mouseX, mouseY, mouseButton)) {
                Y.setFocused(true);
            } else if (this.Z.mouseClicked(mouseX, mouseY, mouseButton)) {
                Z.setFocused(true);
            } else {
                //startX.setFocused(false);
                super.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }

        return true;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
