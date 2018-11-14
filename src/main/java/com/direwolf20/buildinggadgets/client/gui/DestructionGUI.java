/**
 * Parts of this class were adapted from code written by TTerrag for the Chisel mod: https://github.com/Chisel-Team/Chisel
 * Chisel is Open Source and distributed under GNU GPL v2
 */

package com.direwolf20.buildinggadgets.client.gui;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.network.PacketDestructionGUI;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
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

public class DestructionGUI extends GuiScreen {
    public static final int WIDTH = 256;
    public static final int HEIGHT = 256;

    private GuiTextField left;
    private GuiTextField right;
    private GuiTextField up;
    private GuiTextField down;
    private GuiTextField depth;
    //private GuiTextField endZ;

    //private boolean absoluteCoords = Config.absoluteCoordDefault;

    int guiLeft = 15;
    int guiTop = 50;

    ItemStack destructionTool;

    private static final ResourceLocation background = new ResourceLocation(BuildingGadgets.MODID, "textures/gui/testcontainer.png");

    public DestructionGUI(ItemStack tool) {
        super();
        this.destructionTool = tool;
    }

    /*@Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }*/

    @Override
    public void initGui() {
        super.initGui();

        left = new GuiTextField(0, this.fontRenderer, this.guiLeft + 80, this.guiTop + 60, 40, this.fontRenderer.FONT_HEIGHT);
        left.setMaxStringLength(50);
        left.setVisible(true);

        right = new GuiTextField(1, this.fontRenderer, this.guiLeft + 320, this.guiTop + 60, 40, this.fontRenderer.FONT_HEIGHT);
        right.setMaxStringLength(50);
        right.setVisible(true);

        up = new GuiTextField(2, this.fontRenderer, this.guiLeft + 200, this.guiTop + 30, 40, this.fontRenderer.FONT_HEIGHT);
        up.setMaxStringLength(50);
        up.setVisible(true);


        down = new GuiTextField(3, this.fontRenderer, this.guiLeft + 200, this.guiTop + 90, 40, this.fontRenderer.FONT_HEIGHT);
        down.setMaxStringLength(50);
        down.setVisible(true);

        depth = new GuiTextField(4, this.fontRenderer, this.guiLeft + 200, this.guiTop + 60, 40, this.fontRenderer.FONT_HEIGHT);
        depth.setMaxStringLength(50);
        depth.setVisible(true);

        nullCheckTextBoxes();

        //NOTE: the id always has to be different or else it might get called twice or never!
        this.buttonList.add(new GuiButton(1, this.guiLeft + 145, this.guiTop + 125, 40, 20, "Ok"));
        this.buttonList.add(new GuiButton(2, this.guiLeft + 245, this.guiTop + 125, 40, 20, "Cancel"));
        //this.buttonList.add(new GuiButton(3, this.guiLeft + 245, this.guiTop + 60, 40, 20, "Clear"));
        //this.buttonList.add(new GuiButton(4, this.guiLeft + 325, this.guiTop + 60, 80, 20, "CoordsMode"));
        this.buttonList.add(new DireButton(5, this.guiLeft + 65, this.guiTop + 59, 10, 10, "-"));
        this.buttonList.add(new DireButton(6, this.guiLeft + 125, this.guiTop + 59, 10, 10, "+"));
        this.buttonList.add(new DireButton(7, this.guiLeft + 305, this.guiTop + 59, 10, 10, "-"));
        this.buttonList.add(new DireButton(8, this.guiLeft + 365, this.guiTop + 59, 10, 10, "+"));
        this.buttonList.add(new DireButton(9, this.guiLeft + 185, this.guiTop + 29, 10, 10, "-"));
        this.buttonList.add(new DireButton(10, this.guiLeft + 245, this.guiTop + 29, 10, 10, "+"));
        this.buttonList.add(new DireButton(11, this.guiLeft + 185, this.guiTop + 89, 10, 10, "-"));
        this.buttonList.add(new DireButton(12, this.guiLeft + 245, this.guiTop + 89, 10, 10, "+"));
        this.buttonList.add(new DireButton(13, this.guiLeft + 185, this.guiTop + 59, 10, 10, "-"));
        this.buttonList.add(new DireButton(14, this.guiLeft + 245, this.guiTop + 59, 10, 10, "+"));
        //this.buttonList.add(new DireButton(15, this.guiLeft + 250, this.guiTop + 34, 10, 10, "-"));
        //this.buttonList.add(new DireButton(16, this.guiLeft + 310, this.guiTop + 34, 10, 10, "+"));
    }

    public void fieldChange(GuiTextField textField, int amount) {
        nullCheckTextBoxes();
        if (GuiScreen.isShiftKeyDown()) amount = amount * 10;
        try {
            int i = Integer.valueOf(textField.getText());
            i = i + amount;
            if (i < 0) i = 0;
            if (i > 16) i = 16;
            textField.setText(String.valueOf(i));
        } catch (Throwable t) {
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        mc.getTextureManager().bindTexture(background);
        //drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        this.left.drawTextBox();
        this.right.drawTextBox();
        this.up.drawTextBox();
        this.down.drawTextBox();
        this.depth.drawTextBox();
        fontRenderer.drawStringWithShadow("Left", this.guiLeft + 35, this.guiTop + 60, 0xFFFFFF);
        fontRenderer.drawStringWithShadow("Right", this.guiLeft + 278, this.guiTop + 60, 0xFFFFFF);
        fontRenderer.drawStringWithShadow("Up", this.guiLeft + 170, this.guiTop + 30, 0xFFFFFF);
        fontRenderer.drawStringWithShadow("Down", this.guiLeft + 158, this.guiTop + 90, 0xFFFFFF);
        fontRenderer.drawStringWithShadow("Depth", this.guiLeft + 155, this.guiTop + 60, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    protected void nullCheckTextBoxes() {
        if (left.getText() == "") {
            left.setText(String.valueOf(GadgetDestruction.getToolValue(destructionTool, "left")));
        }
        if (right.getText() == "") {
            right.setText(String.valueOf(GadgetDestruction.getToolValue(destructionTool, "right")));
        }
        if (up.getText() == "") {
            up.setText(String.valueOf(GadgetDestruction.getToolValue(destructionTool, "up")));
        }
        if (down.getText() == "") {
            down.setText(String.valueOf(GadgetDestruction.getToolValue(destructionTool, "down")));
        }
        if (depth.getText() == "") {
            depth.setText(String.valueOf(GadgetDestruction.getToolValue(destructionTool, "depth")));
        }
    }

    protected boolean sizeCheckBoxes() {
        if( left.getText().equals("") || right.getText().equals("") || down.getText().equals("") || up.getText().equals("") || depth.getText().equals(""))
            return false;

        if (Integer.parseInt(left.getText()) + Integer.parseInt(right.getText()) > 16) return false;
        if (Integer.parseInt(up.getText()) + Integer.parseInt(down.getText()) > 16) return false;
        if (Integer.parseInt(depth.getText()) > 16) return false;
        if (Integer.parseInt(left.getText()) < 0) return false;
        if (Integer.parseInt(right.getText()) < 0) return false;
        if (Integer.parseInt(up.getText()) < 0) return false;
        if (Integer.parseInt(down.getText()) < 0) return false;
        if (Integer.parseInt(depth.getText()) < 0) return false;
        return true;
    }

    @Override
    protected void actionPerformed(GuiButton b) {
        if (b.id == 1) {
            nullCheckTextBoxes();
            if (sizeCheckBoxes()) {
                PacketHandler.INSTANCE.sendToServer(new PacketDestructionGUI(Integer.parseInt(left.getText()), Integer.parseInt(right.getText()), Integer.parseInt(up.getText()), Integer.parseInt(down.getText()), Integer.parseInt(depth.getText())));
                this.mc.displayGuiScreen(null);
            } else {
                Minecraft.getMinecraft().player.sendStatusMessage(new TextComponentString(TextFormatting.RED + new TextComponentTranslation("message.gadget.destroysizeerror").getUnformattedComponentText()), true);
            }

        } else if (b.id == 2) {
            this.mc.displayGuiScreen(null);
        } else if (b.id == 5) {
            fieldChange(left, -1);
        } else if (b.id == 6) {
            fieldChange(left, 1);
        } else if (b.id == 7) {
            fieldChange(right, -1);
        } else if (b.id == 8) {
            fieldChange(right, 1);
        } else if (b.id == 9) {
            fieldChange(up, -1);
        } else if (b.id == 10) {
            fieldChange(up, 1);
        } else if (b.id == 11) {
            fieldChange(down, -1);
        } else if (b.id == 12) {
            fieldChange(down, 1);
        } else if (b.id == 13) {
            fieldChange(depth, -1);
        } else if (b.id == 14) {
            fieldChange(depth, 1);
        }

    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.left.textboxKeyTyped(typedChar, keyCode) || this.right.textboxKeyTyped(typedChar, keyCode) || this.up.textboxKeyTyped(typedChar, keyCode) || this.down.textboxKeyTyped(typedChar, keyCode) || this.depth.textboxKeyTyped(typedChar, keyCode)) {

        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 1) {
            if (this.left.mouseClicked(mouseX, mouseY, 0)) {
                left.setText("");
            } else if (this.right.mouseClicked(mouseX, mouseY, 0)) {
                right.setText("");
            } else if (this.up.mouseClicked(mouseX, mouseY, 0)) {
                up.setText("");
            } else if (this.down.mouseClicked(mouseX, mouseY, 0)) {
                down.setText("");
            } else if (this.depth.mouseClicked(mouseX, mouseY, 0)) {
                depth.setText("");
            } else {
                //startX.setFocused(false);
                super.mouseClicked(mouseX, mouseY, mouseButton);
            }
        } else {
            if (this.left.mouseClicked(mouseX, mouseY, mouseButton)) {
                left.setFocused(true);
            } else if (this.right.mouseClicked(mouseX, mouseY, mouseButton)) {
                right.setFocused(true);
            } else if (this.up.mouseClicked(mouseX, mouseY, mouseButton)) {
                up.setFocused(true);
            } else if (this.down.mouseClicked(mouseX, mouseY, mouseButton)) {
                down.setFocused(true);
            } else if (this.depth.mouseClicked(mouseX, mouseY, mouseButton)) {
                depth.setFocused(true);
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
