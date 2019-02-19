/**
 * Parts of this class were adapted from code written by TTerrag for the Chisel mod: https://github.com/Chisel-Team/Chisel
 * Chisel is Open Source and distributed under GNU GPL v2
 */

package com.direwolf20.buildinggadgets.client.gui;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketCopyCoords;
import com.direwolf20.buildinggadgets.common.registry.objects.BGItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

public class CopyGUI extends GuiScreen {
    private GuiTextFieldWithDefault startX;
    private GuiTextFieldWithDefault startY;
    private GuiTextFieldWithDefault startZ;
    private GuiTextFieldWithDefault endX;
    private GuiTextFieldWithDefault endY;
    private GuiTextFieldWithDefault endZ;

    private boolean absoluteCoords = Config.GENERAL.absoluteCoordDefault.get();

    private int guiLeft = 15;
    private int guiTop = 15;

    private ItemStack copyPasteTool;
    private BlockPos startPos;
    private BlockPos endPos;

    private static final ResourceLocation background = new ResourceLocation(BuildingGadgets.MODID, "textures/gui/testcontainer.png");
    private List<GuiTextFieldWithDefault> guiList = new ArrayList<>();

    public CopyGUI(ItemStack tool) {
        super();
        this.copyPasteTool = tool;
    }

    @Override
    public void initGui() {
        super.initGui();

        startPos = ((GadgetCopyPaste) BGItems.gadgetCopyPaste).getStartPos(copyPasteTool);
        endPos = ((GadgetCopyPaste) BGItems.gadgetCopyPaste).getEndPos(copyPasteTool);

        if (startPos == null) startPos = new BlockPos(0, 0, 0);
        if (endPos == null) endPos = new BlockPos(0, 0, 0);

        guiList.add( startX = new GuiTextFieldWithDefault(0, this.fontRenderer, this.guiLeft + 65, this.guiTop + 15, 40, this.fontRenderer.FONT_HEIGHT, String.valueOf(startPos.getX())) );
        guiList.add( startY = new GuiTextFieldWithDefault(1, this.fontRenderer, this.guiLeft + 165, this.guiTop + 15, 40, this.fontRenderer.FONT_HEIGHT, String.valueOf(startPos.getY())) );
        guiList.add( startZ = new GuiTextFieldWithDefault(2, this.fontRenderer, this.guiLeft + 265, this.guiTop + 15, 40, this.fontRenderer.FONT_HEIGHT, String.valueOf(startPos.getZ())) );
        guiList.add( endX = new GuiTextFieldWithDefault(3, this.fontRenderer, this.guiLeft + 65, this.guiTop + 35, 40, this.fontRenderer.FONT_HEIGHT, String.valueOf(endPos.getX())) );
        guiList.add( endY = new GuiTextFieldWithDefault(4, this.fontRenderer, this.guiLeft + 165, this.guiTop + 35, 40, this.fontRenderer.FONT_HEIGHT, String.valueOf(startPos.getY())) );
        guiList.add( endZ = new GuiTextFieldWithDefault(5, this.fontRenderer, this.guiLeft + 265, this.guiTop + 35, 40, this.fontRenderer.FONT_HEIGHT, String.valueOf(startPos.getZ())) );

        updateTextFields();
        //NOTE: the id always has to be different or else it might get called twice or never!
        this.addButton(new GuiButton(1, this.guiLeft + 45, this.guiTop + 60, 40, 20, "Ok"){
            @Override
            public void onClick(double mouseX, double mouseY) {
                clearTextBoxes();
                try {
                    if (absoluteCoords) {
                        startPos = new BlockPos(Integer.parseInt(startX.getText()), Integer.parseInt(startY.getText()), Integer.parseInt(startZ.getText()));
                        endPos = new BlockPos(Integer.parseInt(endX.getText()), Integer.parseInt(endY.getText()), Integer.parseInt(endZ.getText()));
                    } else {
                        startPos = new BlockPos(startPos.getX() + Integer.parseInt(startX.getText()), startPos.getY() + Integer.parseInt(startY.getText()), startPos.getZ() + Integer.parseInt(startZ.getText()));
                        endPos = new BlockPos(startPos.getX() + Integer.parseInt(endX.getText()), startPos.getY() + Integer.parseInt(endY.getText()), startPos.getZ() + Integer.parseInt(endZ.getText()));
                    }
                    PacketHandler.sendToServer(new PacketCopyCoords(startPos, endPos));
                } catch (Throwable t) {
                    Minecraft.getInstance().player.sendStatusMessage(new TextComponentString(TextFormatting.RED + new TextComponentTranslation("message.gadget.copyguierror").getUnformattedComponentText()), true);
                }
            }
        });
        this.addButton(new GuiButton(2, this.guiLeft + 145, this.guiTop + 60, 40, 20, "Cancel"){
            @Override
            public void onClick(double mouseX, double mouseY) {
                mc.displayGuiScreen(null);
            }
        });
        this.addButton(new GuiButton(3, this.guiLeft + 245, this.guiTop + 60, 40, 20, "Clear"){
            @Override
            public void onClick(double mouseX, double mouseY) {
                PacketHandler.sendToServer(new PacketCopyCoords(BlockPos.ORIGIN, BlockPos.ORIGIN));
                mc.displayGuiScreen(null);
            }
        });
        this.addButton(new GuiButton(4, this.guiLeft + 325, this.guiTop + 60, 80, 20, "CoordsMode"){
            @Override
            public void onClick(double mouseX, double mouseY) {
                coordsModeSwitch();
                updateTextFields();
            }
        });
        this.addButton(new DireButton(5, this.guiLeft + 50, this.guiTop + 14, 10, 10, "-"){
            @Override
            public void onClick(double mouseX, double mouseY) {
                fieldChange(startX, -1);
            }
        });
        this.addButton(new DireButton(6, this.guiLeft + 110, this.guiTop + 14, 10, 10, "+"){
            @Override
            public void onClick(double mouseX, double mouseY) {
                fieldChange(startX, 1);
            }
        });
        this.addButton(new DireButton(7, this.guiLeft + 150, this.guiTop + 14, 10, 10, "-"){
            @Override
            public void onClick(double mouseX, double mouseY) {
                fieldChange(startY, -1);
            }
        });
        this.addButton(new DireButton(8, this.guiLeft + 210, this.guiTop + 14, 10, 10, "+"){
            @Override
            public void onClick(double mouseX, double mouseY) {
                fieldChange(startY, 1);
            }
        });
        this.addButton(new DireButton(9, this.guiLeft + 250, this.guiTop + 14, 10, 10, "-"){
            @Override
            public void onClick(double mouseX, double mouseY) {
                fieldChange(startZ, -1);
            }
        });
        this.addButton(new DireButton(10, this.guiLeft + 310, this.guiTop + 14, 10, 10, "+"){
            @Override
            public void onClick(double mouseX, double mouseY) {
                fieldChange(startZ, 1);
            }
        });
        this.addButton(new DireButton(11, this.guiLeft + 50, this.guiTop + 34, 10, 10, "-"){
            @Override
            public void onClick(double mouseX, double mouseY) {
                fieldChange(endX, -1);
            }
        });
        this.addButton(new DireButton(12, this.guiLeft + 110, this.guiTop + 34, 10, 10, "+"){
            @Override
            public void onClick(double mouseX, double mouseY) {
                fieldChange(endX, 1);
            }
        });
        this.addButton(new DireButton(13, this.guiLeft + 150, this.guiTop + 34, 10, 10, "-"){
            @Override
            public void onClick(double mouseX, double mouseY) {
                fieldChange(endY, -1);
            }
        });
        this.addButton(new DireButton(14, this.guiLeft + 210, this.guiTop + 34, 10, 10, "+"){
            @Override
            public void onClick(double mouseX, double mouseY) {
                fieldChange(endY, 1);
            }
        });
        this.addButton(new DireButton(15, this.guiLeft + 250, this.guiTop + 34, 10, 10, "-"){
            @Override
            public void onClick(double mouseX, double mouseY) {
                fieldChange(endZ, -1);
            }
        });
        this.addButton(new DireButton(16, this.guiLeft + 310, this.guiTop + 34, 10, 10, "+"){
            @Override
            public void onClick(double mouseX, double mouseY) {
                fieldChange(endZ, 1);
            }
        });
    }

    private void fieldChange(GuiTextField textField, int amount) {
        clearTextBoxes();
        if (GuiScreen.isShiftKeyDown()) amount = amount * 10;
        try {
            int i = Integer.valueOf(textField.getText());
            i = i + amount;
            textField.setText(String.valueOf(i));
        } catch (Throwable t) {
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        mc.getTextureManager().bindTexture(background);
        //drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        this.startX.drawTextField(mouseX, mouseY, partialTicks);
        this.startY.drawTextField(mouseX, mouseY, partialTicks);
        this.startZ.drawTextField(mouseX, mouseY, partialTicks);
        this.endX.drawTextField(mouseX, mouseY, partialTicks);
        this.endY.drawTextField(mouseX, mouseY, partialTicks);
        this.endZ.drawTextField(mouseX, mouseY, partialTicks);
        fontRenderer.drawStringWithShadow("Start X", this.guiLeft, this.guiTop + 15, 0xFFFFFF);
        fontRenderer.drawStringWithShadow("Y", this.guiLeft + 131, this.guiTop + 15, 0xFFFFFF);
        fontRenderer.drawStringWithShadow("Z", this.guiLeft + 231, this.guiTop + 15, 0xFFFFFF);
        fontRenderer.drawStringWithShadow("End X", this.guiLeft + 8, this.guiTop + 35, 0xFFFFFF);
        fontRenderer.drawStringWithShadow("Y", this.guiLeft + 131, this.guiTop + 35, 0xFFFFFF);
        fontRenderer.drawStringWithShadow("Z", this.guiLeft + 231, this.guiTop + 35, 0xFFFFFF);
        super.render(mouseX, mouseY, partialTicks);
    }

    private void clearTextBoxes() {
        guiList.forEach(item -> {
            if (item.getText().equals(""))
                item.setText(String.valueOf(absoluteCoords ? item.getDefaultValue() : "0"));
        });
    }

    private void coordsModeSwitch() {
        absoluteCoords = !absoluteCoords;
    }

    private void updateTextFields() {
        String x, y, z;
        if (absoluteCoords) {
            BlockPos start = startX.getText() != "" ? new BlockPos(startPos.getX() + Integer.parseInt(startX.getText()), startPos.getY() + Integer.parseInt(startY.getText()), startPos.getZ() + Integer.parseInt(startZ.getText())) : startPos;
            BlockPos end = endX.getText() != "" ? new BlockPos(startPos.getX() + Integer.parseInt(endX.getText()), startPos.getY() + Integer.parseInt(endY.getText()), startPos.getZ() + Integer.parseInt(endZ.getText())) : endPos;
            startX.setText(String.valueOf(start.getX()));
            startY.setText(String.valueOf(start.getY()));
            startZ.setText(String.valueOf(start.getZ()));
            endX.setText(String.valueOf(end.getX()));
            endY.setText(String.valueOf(end.getY()));
            endZ.setText(String.valueOf(end.getZ()));
        } else {
            x = startX.getText() != "" ? String.valueOf(Integer.parseInt(startX.getText()) - startPos.getX()) : "0";
            startX.setText(x);
            y = startY.getText() != "" ? String.valueOf(Integer.parseInt(startY.getText()) - startPos.getY()) : "0";
            startY.setText(y);
            z = startZ.getText() != "" ? String.valueOf(Integer.parseInt(startZ.getText()) - startPos.getZ()) : "0";
            startZ.setText(z);
            x = endX.getText() != "" ? String.valueOf(Integer.parseInt(endX.getText()) - startPos.getX()) : String.valueOf(endPos.getX() - startPos.getX());
            endX.setText(x);
            y = endY.getText() != "" ? String.valueOf(Integer.parseInt(endY.getText()) - startPos.getY()) : String.valueOf(endPos.getY() - startPos.getY());
            endY.setText(y);
            z = endZ.getText() != "" ? String.valueOf(Integer.parseInt(endZ.getText()) - startPos.getZ()) : String.valueOf(endPos.getZ() - startPos.getZ());
            endZ.setText(z);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == 1) {
            if (this.startX.mouseClicked(mouseX, mouseY, 0)) {
                startX.setText("");
            } else if (this.startY.mouseClicked(mouseX, mouseY, 0)) {
                startY.setText("");
            } else if (this.startZ.mouseClicked(mouseX, mouseY, 0)) {
                startZ.setText("");
            } else if (this.endX.mouseClicked(mouseX, mouseY, 0)) {
                endX.setText("");
            } else if (this.endY.mouseClicked(mouseX, mouseY, 0)) {
                endY.setText("");
            } else if (this.endZ.mouseClicked(mouseX, mouseY, 0)) {
                endZ.setText("");
            } else {
                //startX.setFocused(false);
                super.mouseClicked(mouseX, mouseY, mouseButton);
            }
        } else {
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
        return true;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
