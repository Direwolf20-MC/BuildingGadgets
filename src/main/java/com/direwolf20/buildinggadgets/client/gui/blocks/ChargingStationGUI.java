package com.direwolf20.buildinggadgets.client.gui.blocks;

import com.direwolf20.buildinggadgets.common.containers.ChargingStationContainer;
import com.direwolf20.buildinggadgets.common.tiles.ChargingStationTileEntity;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class ChargingStationGUI extends ContainerScreen<ChargingStationContainer> {

    private ChargingStationTileEntity te;
    private ChargingStationContainer container;

    private static final ResourceLocation background = new ResourceLocation(Reference.MODID, "textures/gui/template_manager.png");

    public ChargingStationGUI(ChargingStationContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.container = container;
        this.te = container.getTe();
    }

    public ChargingStationGUI(ChargingStationTileEntity tileEntity, ChargingStationContainer container, PlayerInventory inv) {
        super(container, inv, null); //TODO find out the usage of this TextComponent
        //this.te = tileEntity;
        //this.container = container;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1, 1, 1, 1);
        getMinecraft().getTextureManager().bindTexture(background);
        blit(guiLeft, guiTop, 0, 0, xSize, ySize);

    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawString(Minecraft.getInstance().fontRenderer, "Energy: " + container.getEnergy(), 10, 10, 0xffffff);
    }
}
