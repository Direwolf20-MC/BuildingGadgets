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
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class DestructionGUI extends GuiScreen {

    private GuiTextField left;
    private GuiTextField right;
    private GuiTextField up;
    private GuiTextField down;
    private GuiTextField depth;

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

        this.addButton(new GuiButton(1, this.guiLeft + 145, this.guiTop + 125, 40, 20, I18n.format("singles.buildinggadgets.confirm")){
            @Override
            public void onClick(double mouseX, double mouseY) {
                nullCheckTextBoxes();
                if (sizeCheckBoxes()) {
                    PacketHandler.sendToServer(new PacketDestructionGUI(Integer.parseInt(left.getText()), Integer.parseInt(right.getText()), Integer.parseInt(up.getText()), Integer.parseInt(down.getText()), Integer.parseInt(depth.getText())));
                    mc.displayGuiScreen(null);
                } else {
                    Minecraft.getInstance().player.sendStatusMessage(new TextComponentString(TextFormatting.RED + new TextComponentTranslation("message.gadget.destroysizeerror").getUnformattedComponentText()), true);
                }

                super.onClick(mouseX, mouseY);
            }
        });
        this.addButton(new GuiButton(2, this.guiLeft + 245, this.guiTop + 125, 40, 20, I18n.format("singles.buildinggadgets.cancel")){
            @Override
            public void onClick(double mouseX, double mouseY) {
                mc.displayGuiScreen(null);
                super.onClick(mouseX, mouseY);
            }
        });
        this.addButton(new DireButton(5, this.guiLeft + 65, this.guiTop + 59, 10, 10, "-"){
            @Override
            public void onClick(double mouseX, double mouseY) {
                fieldChange(left, -1);
                super.onClick(mouseX, mouseY);
            }
        });
        this.addButton(new DireButton(6, this.guiLeft + 125, this.guiTop + 59, 10, 10, "+"){
            @Override
            public void onClick(double mouseX, double mouseY) {
                fieldChange(left, 1);
                super.onClick(mouseX, mouseY);
            }
        });
        this.addButton(new DireButton(7, this.guiLeft + 305, this.guiTop + 59, 10, 10, "-"){
            @Override
            public void onClick(double mouseX, double mouseY) {
                fieldChange(right, -1);
                super.onClick(mouseX, mouseY);
            }
        });
        this.addButton(new DireButton(8, this.guiLeft + 365, this.guiTop + 59, 10, 10, "+"){
            @Override
            public void onClick(double mouseX, double mouseY) {
                fieldChange(right, 1);
                super.onClick(mouseX, mouseY);
            }
        });
        this.addButton(new DireButton(9, this.guiLeft + 185, this.guiTop + 29, 10, 10, "-"){
            @Override
            public void onClick(double mouseX, double mouseY) {
                fieldChange(up, -1);
                super.onClick(mouseX, mouseY);
            }
        });
        this.addButton(new DireButton(10, this.guiLeft + 245, this.guiTop + 29, 10, 10, "+"){
            @Override
            public void onClick(double mouseX, double mouseY) {
                fieldChange(up, 1);
                super.onClick(mouseX, mouseY);
            }
        });
        this.addButton(new DireButton(11, this.guiLeft + 185, this.guiTop + 89, 10, 10, "-"){
            @Override
            public void onClick(double mouseX, double mouseY) {
                fieldChange(down, -1);
                super.onClick(mouseX, mouseY);
            }
        });
        this.addButton(new DireButton(12, this.guiLeft + 245, this.guiTop + 89, 10, 10, "+"){
            @Override
            public void onClick(double mouseX, double mouseY) {
                fieldChange(down, 1);
                super.onClick(mouseX, mouseY);
            }
        });
        this.addButton(new DireButton(13, this.guiLeft + 185, this.guiTop + 59, 10, 10, "-"){
            @Override
            public void onClick(double mouseX, double mouseY) {
                fieldChange(depth, -1);
                super.onClick(mouseX, mouseY);
            }
        });
        this.addButton(new DireButton(14, this.guiLeft + 245, this.guiTop + 59, 10, 10, "+"){
            @Override
            public void onClick(double mouseX, double mouseY) {
                fieldChange(depth, 1);
                super.onClick(mouseX, mouseY);
            }
        });
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
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        mc.getTextureManager().bindTexture(background);

        this.left.drawTextField(mouseX, mouseY, partialTicks);
        this.right.drawTextField(mouseX, mouseY, partialTicks);
        this.up.drawTextField(mouseX, mouseY, partialTicks);
        this.down.drawTextField(mouseX, mouseY, partialTicks);
        this.depth.drawTextField(mouseX, mouseY, partialTicks);

        fontRenderer.drawStringWithShadow(I18n.format("singles.buildinggadgets.left"), this.guiLeft + 35, this.guiTop + 60, 0xFFFFFF);
        fontRenderer.drawStringWithShadow(I18n.format("singles.buildinggadgets.right"), this.guiLeft + 278, this.guiTop + 60, 0xFFFFFF);
        fontRenderer.drawStringWithShadow(I18n.format("singles.buildinggadgets.up"), this.guiLeft + 170, this.guiTop + 30, 0xFFFFFF);
        fontRenderer.drawStringWithShadow(I18n.format("singles.buildinggadgets.down"), this.guiLeft + 158, this.guiTop + 90, 0xFFFFFF);
        fontRenderer.drawStringWithShadow(I18n.format("singles.buildinggadgets.depth"), this.guiLeft + 155, this.guiTop + 60, 0xFFFFFF);

        super.render(mouseX, mouseY, partialTicks);
    }

    private void nullCheckTextBoxes() {
        if (left.getText().equals("")) {
            left.setText(String.valueOf(GadgetDestruction.getToolValue(destructionTool, "left")));
        }
        if (right.getText().equals("")) {
            right.setText(String.valueOf(GadgetDestruction.getToolValue(destructionTool, "right")));
        }
        if (up.getText().equals("")) {
            up.setText(String.valueOf(GadgetDestruction.getToolValue(destructionTool, "up")));
        }
        if (down.getText().equals("")) {
            down.setText(String.valueOf(GadgetDestruction.getToolValue(destructionTool, "down")));
        }
        if (depth.getText().equals("")) {
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
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
