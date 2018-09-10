package com.direwolf20.buildinggadgets.blocks.templatemanager;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.network.PacketHandler;
import com.direwolf20.buildinggadgets.network.PacketTemplateManagerLoad;
import com.direwolf20.buildinggadgets.network.PacketTemplateManagerSave;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class TemplateManagerGUI extends GuiContainer {
    public static final int WIDTH = 180;
    public static final int HEIGHT = 152;

    private GuiTextField nameField;

    private TemplateManagerTileEntity te;
    private TemplateManagerContainer container;

    private static final ResourceLocation background = new ResourceLocation(BuildingGadgets.MODID, "textures/gui/testcontainer.png");

    public TemplateManagerGUI(TemplateManagerTileEntity tileEntity, TemplateManagerContainer container) {
        super(container);
        this.te = tileEntity;
        this.container = container;
        xSize = WIDTH;
        ySize = HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();
        //The parameters of GuiButton are(id, x, y, width, height, text);
        this.buttonList.add(new GuiButton(1, 160, 45, 30, 20, "Save"));
        this.buttonList.add(new GuiButton(2, 160, 67, 30, 20, "Load"));
        this.buttonList.add(new GuiButton(3, 125, 45, 30, 20, "Copy"));
        this.buttonList.add(new GuiButton(4, 125, 67, 30, 20, "Paste"));
        this.nameField = new GuiTextField(0, this.fontRenderer, this.guiLeft + 82, this.guiTop + 6, 80, this.fontRenderer.FONT_HEIGHT);
        this.nameField.setMaxStringLength(50);
        this.nameField.setVisible(true);
        //NOTE: the id always has to be different or else it might get called twice or never!
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(background);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        this.nameField.drawTextBox();
    }

    @Override
    protected void actionPerformed(GuiButton b) {
        //If the button id is different, or you have mrs buttons, create another if block for that too!
        if (b.id == 1) {
            //System.out.println("My Button is Clicked!");
            PacketHandler.INSTANCE.sendToServer(new PacketTemplateManagerSave(te.getPos(), nameField.getText()));
        } else if (b.id == 2) {
            PacketHandler.INSTANCE.sendToServer(new PacketTemplateManagerLoad(te.getPos()));
            //loadTemplate();
            //System.out.println("My OTHER Button is Clicked!");
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.nameField.textboxKeyTyped(typedChar, keyCode)) {

        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (this.nameField.mouseClicked(mouseX, mouseY, mouseButton)) {
            nameField.setFocused(true);
        } else {
            nameField.setFocused(false);
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }
}
