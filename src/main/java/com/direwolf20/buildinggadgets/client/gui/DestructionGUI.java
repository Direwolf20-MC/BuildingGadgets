/**
 * Parts of this class were adapted from code written by TTerrag for the Chisel mod: https://github.com/Chisel-Team/Chisel
 * Chisel is Open Source and distributed under GNU GPL v2
 */

package com.direwolf20.buildinggadgets.client.gui;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketDestructionGUI;
import com.direwolf20.buildinggadgets.common.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class DestructionGUI extends GuiScreenTextFields {

    private GuiTextFieldBase left, right, up, down, depth;

    private int guiLeft = 15;
    private int guiTop = 50;

    private ItemStack destructionTool;

    private static final ResourceLocation background = new ResourceLocation(BuildingGadgets.MODID, "textures/gui/testcontainer.png");

    DestructionGUI(ItemStack tool) {
        super();
        this.destructionTool = tool;
    }

    @Override
    public void initGui() {
        super.initGui();

        left = addField(80, 60);
        right = addField(320, 60);
        up = addField(200, 30);
        down = addField(200, 90);
        depth = addField(200, 60);

        nullCheckTextBoxes();

        addButton(new GuiButtonAction(guiLeft + 145, guiTop + 125, 40, 20, I18n.format(GuiMod.getLangKeyButton("destruction", "confirm")), () -> {
            nullCheckTextBoxes();
            if (sizeCheckBoxes()) {
                PacketHandler.sendToServer(new PacketDestructionGUI(Integer.parseInt(left.getText()), Integer.parseInt(right.getText()), Integer.parseInt(up.getText()), Integer.parseInt(down.getText()), Integer.parseInt(depth.getText())));
                mc.displayGuiScreen(null);
            } else {
                Minecraft.getInstance().player.sendStatusMessage(new TextComponentString(TextFormatting.RED + new TextComponentTranslation("message.gadget.destroysizeerror").getUnformattedComponentText()), true);
            }
        }));
        addButton(new GuiButtonAction(guiLeft + 245, guiTop + 125, 40, 20, I18n.format(GuiMod.getLangKeyButton("destruction", "cancel")), () -> close()));
        addButton(65, 59, "-", () -> fieldChange(left, -1));
        addButton(125, 59, "+", () -> fieldChange(left, 1));
        addButton(305, 59, "-", () -> fieldChange(right, -1));
        addButton(365, 59, "+", () -> fieldChange(right, 1));
        addButton(185, 29, "-", () -> fieldChange(up, -1));
        addButton(245, 29, "+", () -> fieldChange(up, 1));
        addButton(185, 89, "-", () -> fieldChange(down, -1));
        addButton(245, 89, "+", () -> fieldChange(down, 1));
        addButton(185, 59, "-", () -> fieldChange(depth, -1));
        addButton(245, 59, "+", () -> fieldChange(depth, 1));
    }

    private void addButton(int x, int y, String text, Runnable action) {
        addButton(new DireButton(guiLeft + x, guiTop + y, 10, 10, text, action));
    }

    private GuiTextFieldBase addField(int x, int y) {
        return addField(new GuiTextFieldBase(fontRenderer, guiLeft + x, guiTop + y, 40));
    }

    private void fieldChange(GuiTextField textField, int amount) {
        nullCheckTextBoxes();
        if (GuiScreen.isShiftKeyDown()) amount = amount * 10;
        try {
            int i = Integer.valueOf(textField.getText());
            i = i + amount;
            if (i < 0) i = 0;
            if (i > 16) i = 16;
            textField.setText(String.valueOf(i));
        } catch (NumberFormatException t) {
            close();
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        mc.getTextureManager().bindTexture(background);

        drawFieldLable("left", 35, 60);
        drawFieldLable("right", 278, 60);
        drawFieldLable("up", 170, 30);
        drawFieldLable("down", 158, 90);
        drawFieldLable("depth", 155, 60);

        super.render(mouseX, mouseY, partialTicks);
    }

    private void drawFieldLable(String name, int x, int y) {
        fontRenderer.drawStringWithShadow(I18n.format(GuiMod.getLangKeyField("destruction", name)), this.guiLeft + x, this.guiTop + y, 0xFFFFFF);
    }

    private void nullCheckTextBoxes() {
        if (left.getText().isEmpty()) {
            left.setText(String.valueOf(GadgetDestruction.getToolValue(destructionTool, "left")));
        }
        if (right.getText().isEmpty()) {
            right.setText(String.valueOf(GadgetDestruction.getToolValue(destructionTool, "right")));
        }
        if (up.getText().isEmpty()) {
            up.setText(String.valueOf(GadgetDestruction.getToolValue(destructionTool, "up")));
        }
        if (down.getText().isEmpty()) {
            down.setText(String.valueOf(GadgetDestruction.getToolValue(destructionTool, "down")));
        }
        if (depth.getText().isEmpty()) {
            depth.setText(String.valueOf(GadgetDestruction.getToolValue(destructionTool, "depth")));
        }
    }

    private boolean sizeCheckBoxes() {
        if( !Utils.isStringNumeric(left.getText()) || !Utils.isStringNumeric(right.getText()) || !Utils.isStringNumeric(up.getText()) || !Utils.isStringNumeric(down.getText()) || !Utils.isStringNumeric(depth.getText()) )
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
}