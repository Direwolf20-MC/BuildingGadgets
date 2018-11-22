/**
 * Parts of this class were adapted from code written by TTerrag for the Chisel mod: https://github.com/Chisel-Team/Chisel
 * Chisel is Open Source and distributed under GNU GPL v2
 */

package com.direwolf20.buildinggadgets.client.gui;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.PacketPasteGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;

public class PasteGUI extends GuiScreen {
    public static final int WIDTH = 256;
    public static final int HEIGHT = 256;

    private GuiTextField X;
    private GuiTextField Y;
    private GuiTextField Z;


    int guiLeft = 15;
    int guiTop = 50;

    ItemStack tool;

    private static final ResourceLocation background = new ResourceLocation(BuildingGadgets.MODID, "textures/gui/testcontainer.png");

    public PasteGUI(ItemStack tool) {
        super();
        this.tool = tool;
    }

    /*@Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }*/

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

        //NOTE: the id always has to be different or else it might get called twice or never!
        this.buttonList.add(new GuiButton(1, this.guiLeft + 80, this.guiTop + 125, 40, 20, "Ok"));
        //this.buttonList.add(new GuiButton(2, this.guiLeft + 200, this.guiTop + 125, 40, 20, "Cancel"));
        this.buttonList.add(new DireButton(3, this.guiLeft + 65, this.guiTop + 99, 10, 10, "-"));
        this.buttonList.add(new DireButton(4, this.guiLeft + 125, this.guiTop + 99, 10, 10, "+"));
        this.buttonList.add(new DireButton(5, this.guiLeft + 185, this.guiTop + 99, 10, 10, "-"));
        this.buttonList.add(new DireButton(6, this.guiLeft + 245, this.guiTop + 99, 10, 10, "+"));
        this.buttonList.add(new DireButton(7, this.guiLeft + 305, this.guiTop + 99, 10, 10, "-"));
        this.buttonList.add(new DireButton(8, this.guiLeft + 365, this.guiTop + 99, 10, 10, "+"));
        this.buttonList.add(new GuiButton(9, this.guiLeft + 320, this.guiTop + 125, 40, 20, "Reset"));
    }

    public void fieldChange(GuiTextField textField, int amount) {
        nullCheckTextBoxes();
        if (GuiScreen.isShiftKeyDown()) amount = amount * 10;
        try {
            int i = Integer.valueOf(textField.getText());
            i = i + amount;
            if (i < -16) i = -16;
            if (i > 16) i = 16;
            textField.setText(String.valueOf(i));
        } catch (NumberFormatException t) {
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        mc.getTextureManager().bindTexture(background);
        //drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        this.X.drawTextBox();
        this.Y.drawTextBox();
        this.Z.drawTextBox();
        fontRenderer.drawStringWithShadow("X", this.guiLeft + 55, this.guiTop + 100, 0xFFFFFF);
        fontRenderer.drawStringWithShadow("Y", this.guiLeft + 175, this.guiTop + 100, 0xFFFFFF);
        fontRenderer.drawStringWithShadow("Z", this.guiLeft + 296, this.guiTop + 100, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    protected void nullCheckTextBoxes() {
        if (X.getText() == "") {
            X.setText(String.valueOf(GadgetCopyPaste.getX(tool)));
        }
        if (Y.getText() == "") {
            Y.setText(String.valueOf(GadgetCopyPaste.getY(tool)));
        }
        if (Z.getText() == "") {
            Z.setText(String.valueOf(GadgetCopyPaste.getZ(tool)));
        }
    }

    protected boolean sizeCheckBoxes() {
        if (Integer.parseInt(Z.getText()) > 16) return false;
        if (Integer.parseInt(Z.getText()) < -16) return false;
        if (Integer.parseInt(Y.getText()) < -16) return false;
        if (Integer.parseInt(Y.getText()) > 16) return false;
        if (Integer.parseInt(X.getText()) < -16) return false;
        if (Integer.parseInt(X.getText()) > 16) return false;
        return true;
    }

    @Override
    protected void actionPerformed(GuiButton b) {
        if (b.id == 1) {
            nullCheckTextBoxes();
            if (sizeCheckBoxes()) {
                PacketHandler.INSTANCE.sendToServer(new PacketPasteGUI(Integer.parseInt(X.getText()), Integer.parseInt(Y.getText()), Integer.parseInt(Z.getText())));
                this.mc.displayGuiScreen(null);
            } else {
                Minecraft.getMinecraft().player.sendStatusMessage(new TextComponentString(TextFormatting.RED + new TextComponentTranslation("message.gadget.destroysizeerror").getUnformattedComponentText()), true);
            }

        } else if (b.id == 2) {
            this.mc.displayGuiScreen(null);
        } else if (b.id == 3) {
            fieldChange(X, -1);
            PacketHandler.INSTANCE.sendToServer(new PacketPasteGUI(Integer.parseInt(X.getText()), Integer.parseInt(Y.getText()), Integer.parseInt(Z.getText())));
        } else if (b.id == 4) {
            fieldChange(X, 1);
            PacketHandler.INSTANCE.sendToServer(new PacketPasteGUI(Integer.parseInt(X.getText()), Integer.parseInt(Y.getText()), Integer.parseInt(Z.getText())));
        } else if (b.id == 5) {
            fieldChange(Y, -1);
            PacketHandler.INSTANCE.sendToServer(new PacketPasteGUI(Integer.parseInt(X.getText()), Integer.parseInt(Y.getText()), Integer.parseInt(Z.getText())));
        } else if (b.id == 6) {
            fieldChange(Y, 1);
            PacketHandler.INSTANCE.sendToServer(new PacketPasteGUI(Integer.parseInt(X.getText()), Integer.parseInt(Y.getText()), Integer.parseInt(Z.getText())));
        } else if (b.id == 7) {
            fieldChange(Z, -1);
            PacketHandler.INSTANCE.sendToServer(new PacketPasteGUI(Integer.parseInt(X.getText()), Integer.parseInt(Y.getText()), Integer.parseInt(Z.getText())));
        } else if (b.id == 8) {
            fieldChange(Z, 1);
            PacketHandler.INSTANCE.sendToServer(new PacketPasteGUI(Integer.parseInt(X.getText()), Integer.parseInt(Y.getText()), Integer.parseInt(Z.getText())));
        } else if (b.id == 9) {
            X.setText(String.valueOf(0));
            Y.setText(String.valueOf(1));
            Z.setText(String.valueOf(0));
            PacketHandler.INSTANCE.sendToServer(new PacketPasteGUI(Integer.parseInt(X.getText()), Integer.parseInt(Y.getText()), Integer.parseInt(Z.getText())));
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.X.textboxKeyTyped(typedChar, keyCode) || this.Y.textboxKeyTyped(typedChar, keyCode) || this.Z.textboxKeyTyped(typedChar, keyCode)) {

        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
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
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
    }


    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        //System.out.println(Mouse.getEventDWheel());
        //System.out.println(zoom);

    }

    @Override
    public void updateScreen() {
        super.updateScreen();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
