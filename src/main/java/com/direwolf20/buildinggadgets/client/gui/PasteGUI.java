/**
 * Parts of this class were adapted from code written by TTerrag for the Chisel mod: https://github.com/Chisel-Team/Chisel
 * Chisel is Open Source and distributed under GNU GPL v2
 */

package com.direwolf20.buildinggadgets.client.gui;

import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketPasteGUI;

import com.direwolf20.buildinggadgets.common.utils.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
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

    private static final ResourceLocation background = new ResourceLocation(Reference.MODID, "textures/gui/testcontainer.png");

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
            if (GuiMod.sizeCheckBoxes(getFieldIterator(), -16, 16)) {
                PacketHandler.sendToServer(new PacketPasteGUI(Integer.parseInt(X.getText()), Integer.parseInt(Y.getText()), Integer.parseInt(Z.getText())));
                close();
            } else {
                Minecraft.getInstance().player.sendStatusMessage(new TextComponentString(TextFormatting.RED + new TextComponentTranslation("message.gadget.destroysizeerror").getUnformattedComponentText()), true);
            }
        }));
        addButton(65, "-", () -> fieldChange(X, -1));
        addButton(125, "+", () -> fieldChange(X, 1));
        addButton(185, "-", () -> fieldChange(Y, -1));
        addButton(245, "+", () -> fieldChange(Y, 1));
        addButton(305, "-", () -> fieldChange(Z, -1));
        addButton(365, "+", () -> fieldChange(Z, 1));
        addButton(new GuiButtonAction(guiLeft + 320, guiTop + 125, 40, 20, "Reset", () -> {
            X.setText("0");
            Y.setText("1");
            Z.setText("0");
            sendPacket();
        }));
    }

    private void addButton(int x, String text, Runnable action) {
        addButton(new DireButton(guiLeft + x, guiTop + 99, 10, 10, text, action));
    }

    private GuiTextFieldBase addField(int x) {
        return addField(new GuiTextFieldBase(fontRenderer, guiLeft + x, guiTop + 100, 40).restrictToNumeric());
    }

    private void sendPacket() {
        PacketHandler.sendToServer(new PacketPasteGUI(X.getInt(), Y.getInt(), Z.getInt()));
    }

    public void fieldChange(GuiTextFieldBase field, int amount) {
        nullCheckTextBoxes();
        if (GuiScreen.isShiftKeyDown()) amount *= 10;
        int i = MathHelper.clamp(field.getInt() + amount, -16, 16);
        field.setText(String.valueOf(i));
        sendPacket();
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
        GuiMod.setEmptyField(X, () -> GadgetCopyPaste.getX(tool));
        GuiMod.setEmptyField(Y, () -> GadgetCopyPaste.getY(tool));
        GuiMod.setEmptyField(Z, () -> GadgetCopyPaste.getZ(tool));
    }
}