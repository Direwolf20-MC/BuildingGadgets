/**
 * Parts of this class were adapted from code written by TTerrag for the Chisel mod: https://github.com/Chisel-Team/Chisel
 * Chisel is Open Source and distributed under GNU GPL v2
 */

package com.direwolf20.buildinggadgets.gui;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.items.CopyPasteTool;
import com.direwolf20.buildinggadgets.network.PacketCopyCoords;
import com.direwolf20.buildinggadgets.network.PacketHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

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
    BlockPos startPos;
    BlockPos endPos;

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
        startPos = CopyPasteTool.getStartPos(copyPasteTool);
        endPos = CopyPasteTool.getEndPos(copyPasteTool);
        if (startPos == null) startPos = new BlockPos(0, 0, 0);
        if (endPos == null) endPos = new BlockPos(0, 0, 0);
        //The parameters of GuiButton are(id, x, y, width, height, text);
        //this.buttonList.add(new GuiButton(4, this.guiLeft + 134, this.guiTop + 55, 35, 20, "Paste"));
        //setupTextField(startX, 0, this.fontRenderer, this.guiLeft + 25, this.guiTop + 15, 80, this.fontRenderer.FONT_HEIGHT, 50, true);
        startX = new GuiTextField(0, this.fontRenderer, this.guiLeft + 25, this.guiTop + 15, 80, this.fontRenderer.FONT_HEIGHT);
        startX.setMaxStringLength(50);
        startX.setVisible(true);
        startX.setText(String.valueOf(0));
        startY = new GuiTextField(1, this.fontRenderer, this.guiLeft + 125, this.guiTop + 15, 80, this.fontRenderer.FONT_HEIGHT);
        startY.setMaxStringLength(50);
        startY.setVisible(true);
        startY.setText(String.valueOf(0));
        startZ = new GuiTextField(2, this.fontRenderer, this.guiLeft + 225, this.guiTop + 15, 80, this.fontRenderer.FONT_HEIGHT);
        startZ.setMaxStringLength(50);
        startZ.setVisible(true);
        startZ.setText(String.valueOf(0));

        endX = new GuiTextField(3, this.fontRenderer, this.guiLeft + 25, this.guiTop + 35, 80, this.fontRenderer.FONT_HEIGHT);
        endX.setMaxStringLength(50);
        endX.setVisible(true);
        endX.setText(String.valueOf(endPos.getX() - startPos.getX()));
        endY = new GuiTextField(4, this.fontRenderer, this.guiLeft + 125, this.guiTop + 35, 80, this.fontRenderer.FONT_HEIGHT);
        endY.setMaxStringLength(50);
        endY.setVisible(true);
        endY.setText(String.valueOf(endPos.getY() - startPos.getY()));
        endZ = new GuiTextField(5, this.fontRenderer, this.guiLeft + 225, this.guiTop + 35, 80, this.fontRenderer.FONT_HEIGHT);
        endZ.setMaxStringLength(50);
        endZ.setVisible(true);
        endZ.setText(String.valueOf(endPos.getZ() - startPos.getZ()));
        //NOTE: the id always has to be different or else it might get called twice or never!
        this.buttonList.add(new GuiButton(1, this.guiLeft + 125, this.guiTop + 60, 40, 20, "Ok"));
        this.buttonList.add(new GuiButton(2, this.guiLeft + 225, this.guiTop + 60, 40, 20, "Cancel"));
        this.buttonList.add(new GuiButton(3, this.guiLeft + 325, this.guiTop + 60, 40, 20, "Clear"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        mc.getTextureManager().bindTexture(background);
        //drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        this.startX.drawTextBox();
        this.startY.drawTextBox();
        this.startZ.drawTextBox();
        this.endX.drawTextBox();
        this.endY.drawTextBox();
        this.endZ.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton b) {
        if (b.id == 1) {
            startPos = new BlockPos(startPos.getX() + Integer.parseInt(startX.getText()), startPos.getY() + Integer.parseInt(startY.getText()), startPos.getZ() + Integer.parseInt(startZ.getText()));
            endPos = new BlockPos(startPos.getX() + Integer.parseInt(endX.getText()), startPos.getY() + Integer.parseInt(endY.getText()), startPos.getZ() + Integer.parseInt(endZ.getText()));
            PacketHandler.INSTANCE.sendToServer(new PacketCopyCoords(startPos, endPos));
        } else if (b.id == 2) {

        } else if (b.id == 3) {
            PacketHandler.INSTANCE.sendToServer(new PacketCopyCoords(BlockPos.ORIGIN, BlockPos.ORIGIN));
        }
        this.mc.displayGuiScreen(null);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.startX.textboxKeyTyped(typedChar, keyCode) || this.startY.textboxKeyTyped(typedChar, keyCode) || this.startZ.textboxKeyTyped(typedChar, keyCode) || this.endX.textboxKeyTyped(typedChar, keyCode) || this.endY.textboxKeyTyped(typedChar, keyCode) || this.endZ.textboxKeyTyped(typedChar, keyCode)) {

        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (this.startX.mouseClicked(mouseX, mouseY, mouseButton)) {
            startX.setFocused(true);
        } else if (this.startY.mouseClicked(mouseX, mouseY, mouseButton)) {
            startY.setFocused(true);
        } else if (this.startZ.mouseClicked(mouseX, mouseY, mouseButton)) {
            startZ.setFocused(true);
        } else if (this.endX.mouseClicked(mouseX, mouseY, mouseButton)) {
            endX.setFocused(true);
        } else if (this.endY.mouseClicked(mouseX, mouseY, mouseButton)) {
            endY.setFocused(true);
        } else if (this.endZ.mouseClicked(mouseX, mouseY, mouseButton)) {
            endZ.setFocused(true);
        } else {
            //startX.setFocused(false);
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
