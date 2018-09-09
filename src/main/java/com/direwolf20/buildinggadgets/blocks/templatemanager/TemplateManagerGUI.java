package com.direwolf20.buildinggadgets.blocks.templatemanager;

import com.direwolf20.buildinggadgets.BuildingGadgets;
import com.direwolf20.buildinggadgets.network.PacketHandler;
import com.direwolf20.buildinggadgets.network.PacketTemplateManagerSave;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;

public class TemplateManagerGUI extends GuiContainer {
    public static final int WIDTH = 180;
    public static final int HEIGHT = 152;

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
        //NOTE: the id always has to be different or else it might get called twice or never!
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(background);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    protected void actionPerformed(GuiButton b) {
        //If the button id is different, or you have mrs buttons, create another if block for that too!
        if (b.id == 1) {
            //System.out.println("My Button is Clicked!");
            PacketHandler.INSTANCE.sendToServer(new PacketTemplateManagerSave(te.getPos()));
        } else if (b.id == 2) {
            //loadTemplate();
            //System.out.println("My OTHER Button is Clicked!");
        }
    }
}
