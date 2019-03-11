/**
 * Parts of this class were adapted from code written by TTerrag for the Chisel mod: https://github.com/Chisel-Team/Chisel
 * Chisel is Open Source and distributed under GNU GPL v2
 */

package com.direwolf20.buildinggadgets.client.gui;

import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketCopyCoords;
import com.direwolf20.buildinggadgets.common.registry.objects.BGItems;
import com.direwolf20.buildinggadgets.common.utils.ref.Reference;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class CopyGUI extends GuiScreenTextFields {
    private GuiTextFieldBase startX, startY, startZ, endX, endY, endZ;

    private boolean absoluteCoords = Config.GENERAL.absoluteCoordDefault.get();

    private int guiLeft = 15;
    private int guiTop = 15;

    private ItemStack copyPasteTool;
    private BlockPos startPos;
    private BlockPos endPos;

    private static final ResourceLocation background = new ResourceLocation(Reference.MODID, "textures/gui/testcontainer.png");

    public CopyGUI(ItemStack tool) {
        super();
        this.copyPasteTool = tool;
    }

    @Override
    public void initGui() {
        super.initGui();

        startPos = BGItems.gadgetCopyPaste.getStartPos(copyPasteTool);
        endPos = BGItems.gadgetCopyPaste.getEndPos(copyPasteTool);

        if (startPos == null) startPos = new BlockPos(0, 0, 0);
        if (endPos == null) endPos = new BlockPos(0, 0, 0);

        startX = addField(65, 15, startPos.getX());
        startY = addField(165, 15, startPos.getY());
        startZ = addField(265, 15, startPos.getZ());
        endX = addField(65, 35, endPos.getX());
        endY = addField(165, 35, endPos.getY());
        endZ = addField(265, 35, endPos.getZ());

        updateTextFields();
        //NOTE: the id always has to be different or else it might get called twice or never!
        addButton(new GuiButtonAction(guiLeft + 45, guiTop + 60, 40, 20, "Ok", () -> {
            clearTextBoxes();
            if (absoluteCoords) {
                startPos = new BlockPos(startX.getInt(), startY.getInt(), startZ.getInt());
                endPos = new BlockPos(endX.getInt(), endY.getInt(), endZ.getInt());
            } else {
                startPos = new BlockPos(startPos.getX() + startX.getInt(), startPos.getY() + startY.getInt(), startPos.getZ() + startZ.getInt());
                endPos = new BlockPos(startPos.getX() + endX.getInt(), startPos.getY() + endY.getInt(), startPos.getZ() + endZ.getInt());
            }
            PacketHandler.sendToServer(new PacketCopyCoords(startPos, endPos));
        }));
        addButton(new GuiButtonAction(guiLeft + 145, guiTop + 60, 40, 20, "Cancel", () -> close()));
        addButton(new GuiButtonAction(guiLeft + 245, guiTop + 60, 40, 20, "Clear", () -> {
            PacketHandler.sendToServer(new PacketCopyCoords(BlockPos.ORIGIN, BlockPos.ORIGIN));
            close();
        }));
        addButton(new GuiButtonAction(guiLeft + 325, guiTop + 60, 80, 20, "CoordsMode", () -> {
            coordsModeSwitch();
            updateTextFields();
        }));
        addButton(50, 14, "-", () -> fieldChange(startX, -1));
        addButton(110, 14, "+", () -> fieldChange(startX, 1));
        addButton(150, 14, "-", () -> fieldChange(startY, -1));
        addButton(210, 14, "+", () -> fieldChange(startY, 1));
        addButton(250, 14, "-", () -> fieldChange(startZ, -1));
        addButton(310, 14, "+", () -> fieldChange(startZ, 1));
        addButton(50, 34, "-", () -> fieldChange(endX, -1));
        addButton(110, 34, "+", () -> fieldChange(endX, 1));
        addButton(150, 34, "-", () -> fieldChange(endY, -1));
        addButton(210, 34, "+", () -> fieldChange(endY, 1));
        addButton(250, 34, "-", () -> fieldChange(endZ, -1));
        addButton(310, 34, "+", () -> fieldChange(endZ, 1));
    }

    private void addButton(int x, int y, String text, Runnable action) {
        addButton(new DireButton(guiLeft + x, guiTop + y, 10, 10, text, action));
    }

    private GuiTextFieldBase addField(int x, int y, int defaultint) {
        return addField(new GuiTextFieldBase(fontRenderer, guiLeft + x, guiTop + y, 40).setDefaultInt(defaultint).restrictToNumeric());
    }

    private void fieldChange(GuiTextFieldBase textField, int amount) {
        clearTextBoxes();
        if (GuiScreen.isShiftKeyDown()) amount *= 10;
        textField.setText(String.valueOf(textField.getInt() + amount));
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        mc.getTextureManager().bindTexture(background);
        drawFieldLable("Start X", 0, 15);
        drawFieldLable("Y", 131, 15);
        drawFieldLable("Z", 231, 15);
        drawFieldLable("End X", 8, 35);
        drawFieldLable("Y", 131, 35);
        drawFieldLable("Z", 231, 35);
        super.render(mouseX, mouseY, partialTicks);
    }

    private void drawFieldLable(String name, int x, int y) {
        fontRenderer.drawStringWithShadow(name, guiLeft + x, guiTop + y, 0xFFFFFF);
    }

    private void clearTextBoxes() {
        forEachField(field -> {
            if (field.getText().equals(""))
                field.setText(String.valueOf(absoluteCoords ? field.getDefaultValue() : "0"));
        });
    }

    private void coordsModeSwitch() {
        absoluteCoords = !absoluteCoords;
    }

    private void updateTextFields() {
        String x, y, z;
        if (absoluteCoords) {
            BlockPos start = startX.getText() != "" ? new BlockPos(startPos.getX() + startX.getInt(), startPos.getY() + startY.getInt(), startPos.getZ() + startZ.getInt()) : startPos;
            BlockPos end = endX.getText() != "" ? new BlockPos(startPos.getX() + endX.getInt(), startPos.getY() + endY.getInt(), startPos.getZ() + endZ.getInt()) : endPos;
            startX.setText(String.valueOf(start.getX()));
            startY.setText(String.valueOf(start.getY()));
            startZ.setText(String.valueOf(start.getZ()));
            endX.setText(String.valueOf(end.getX()));
            endY.setText(String.valueOf(end.getY()));
            endZ.setText(String.valueOf(end.getZ()));
        } else {
            x = startX.getText() != "" ? String.valueOf(startX.getInt() - startPos.getX()) : "0";
            startX.setText(x);
            y = startY.getText() != "" ? String.valueOf(startY.getInt() - startPos.getY()) : "0";
            startY.setText(y);
            z = startZ.getText() != "" ? String.valueOf(startZ.getInt() - startPos.getZ()) : "0";
            startZ.setText(z);
            x = endX.getText() != "" ? String.valueOf(endX.getInt() - startPos.getX()) : String.valueOf(endPos.getX() - startPos.getX());
            endX.setText(x);
            y = endY.getText() != "" ? String.valueOf(endY.getInt() - startPos.getY()) : String.valueOf(endPos.getY() - startPos.getY());
            endY.setText(y);
            z = endZ.getText() != "" ? String.valueOf(endZ.getInt() - startPos.getZ()) : String.valueOf(endPos.getZ() - startPos.getZ());
            endZ.setText(z);
        }
    }
}