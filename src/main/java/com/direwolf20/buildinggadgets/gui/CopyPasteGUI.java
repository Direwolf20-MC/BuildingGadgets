/**
 * Parts of this class were adapted from code written by TTerrag for the Chisel mod: https://github.com/Chisel-Team/Chisel
 * Chisel is Open Source and distributed under GNU GPL v2
 */

package com.direwolf20.buildinggadgets.gui;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class CopyPasteGUI extends GuiScreen {
    public static final int WIDTH = 256;
    public static final int HEIGHT = 256;

    private GuiTextField startX;
    private GuiTextField startY;
    private GuiTextField startZ;
    private GuiTextField endX;
    private GuiTextField endY;
    private GuiTextField endZ;

    int guiLeft = 15;
    int guiTop = 15;

    ItemStack copyPasteTool;

    private static final ResourceLocation background = new ResourceLocation(BuildingGadgets.MODID, "textures/gui/testcontainer.png");

    public CopyPasteGUI(ItemStack tool) {
        super();
        this.copyPasteTool = tool;
    }

    /*@Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }*/

    private static void setupTextField(GuiTextField textField, int componentID, FontRenderer fontRenderer, int startX, int startY, int width, int fontHeight, int maxLength, boolean visible) {

    }

    @Override
    public void initGui() {
        super.initGui();
        //The parameters of GuiButton are(id, x, y, width, height, text);
        /*this.buttonList.add(new GuiButton(1, this.guiLeft + 87, this.guiTop + 11, 30, 20, "Save"));
        this.buttonList.add(new GuiButton(2, this.guiLeft + 136, this.guiTop + 11, 30, 20, "Load"));
        this.buttonList.add(new GuiButton(3, this.guiLeft + 87, this.guiTop + 55, 30, 20, "Copy"));
        this.buttonList.add(new GuiButton(4, this.guiLeft + 134, this.guiTop + 55, 35, 20, "Paste"));*/
        //setupTextField(startX, 0, this.fontRenderer, this.guiLeft + 25, this.guiTop + 15, 80, this.fontRenderer.FONT_HEIGHT, 50, true);
        startX = new GuiTextField(0, this.fontRenderer, this.guiLeft + 25, this.guiTop + 15, 80, this.fontRenderer.FONT_HEIGHT);
        startX.setMaxStringLength(50);
        startX.setVisible(true);
        /*setupTextField(startY, 1, this.fontRenderer, this.guiLeft + 45, this.guiTop + 15, 80, this.fontRenderer.FONT_HEIGHT, 50, true);
        setupTextField(startZ, 2, this.fontRenderer, this.guiLeft + 65, this.guiTop + 15, 80, this.fontRenderer.FONT_HEIGHT, 50, true);
        setupTextField(endX, 3, this.fontRenderer, this.guiLeft + 25, this.guiTop + 35, 80, this.fontRenderer.FONT_HEIGHT, 50, true);
        setupTextField(endY, 4, this.fontRenderer, this.guiLeft + 45, this.guiTop + 35, 80, this.fontRenderer.FONT_HEIGHT, 50, true);
        setupTextField(endZ, 5, this.fontRenderer, this.guiLeft + 65, this.guiTop + 35, 80, this.fontRenderer.FONT_HEIGHT, 50, true);*/

        //NOTE: the id always has to be different or else it might get called twice or never!
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        mc.getTextureManager().bindTexture(background);
        //drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        this.startX.drawTextBox();
        /*this.startY.drawTextBox();
        this.startZ.drawTextBox();
        this.endX.drawTextBox();
        this.endY.drawTextBox();
        this.endZ.drawTextBox();*/
    }

    @Override
    protected void actionPerformed(GuiButton b) {
    }

    /*public static void sendSplitArrays(int[] stateArray, int[] posArray, Map<Short, IBlockState> stateMap) {

        System.out.println("PosArray Length: " + posArray.length);
    }*/

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.startX.textboxKeyTyped(typedChar, keyCode)) {

        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (this.startX.mouseClicked(mouseX, mouseY, mouseButton)) {
            startX.setFocused(true);
        } else {
            startX.setFocused(false);
            super.mouseClicked(mouseX, mouseY, mouseButton);
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
}
