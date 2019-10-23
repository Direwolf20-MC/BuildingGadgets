package com.direwolf20.buildinggadgets.client.gui.blocks;

import com.direwolf20.buildinggadgets.common.containers.ChargingStationContainer;
import com.direwolf20.buildinggadgets.common.tiles.ChargingStationTileEntity;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.lang.TooltipTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.energy.CapabilityEnergy;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChargingStationGUI extends ContainerScreen<ChargingStationContainer> {

    private static final ResourceLocation background = new ResourceLocation(Reference.MODID, "textures/gui/charging_station.png");

    private ChargingStationContainer container;
    private List<String> toolTip = new ArrayList<>();

    public ChargingStationGUI(ChargingStationContainer container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.container = container;
    }

    public ChargingStationGUI(ChargingStationTileEntity tileEntity, ChargingStationContainer container, PlayerInventory inv) {
        super(container, inv, new StringTextComponent("Charging station"));
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);

        if( mouseX > (guiLeft + 7) && mouseX < (guiLeft + 7) + 18 && mouseY > (guiTop + 7) && mouseY < (guiTop + 7) + 73 )
            this.renderTooltip(Arrays.asList(
                    TooltipTranslation.CHARGER_ENERGY.format(GadgetUtils.withSuffix(this.container.getEnergy())),
                    this.container.getTe().getRemainingBurn() <= 0 ?
                        TooltipTranslation.CHARGER_EMPTY.format() :
                            TooltipTranslation.CHARGER_BURN.format(GadgetUtils.ticksInSeconds(this.container.getTe().getRemainingBurn()))
            ), mouseX, mouseY);

        toolTip.clear();
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

        int maxHeight = 13;
        if( this.container.getTe().getMaxBurn() > 0 ) {
            int remaining = (this.container.getTe().getRemainingBurn() * maxHeight) / this.container.getTe().getMaxBurn();
            this.blit(guiLeft + 66, guiTop + 26 + 13 - remaining, 176, 13 - remaining, 14, remaining + 1);
        }

        this.container.getTe().getCapability(CapabilityEnergy.ENERGY).ifPresent( energy -> {
            int height = 68;
            if (energy.getMaxEnergyStored() > 0) {
                int remaining = (energy.getEnergyStored() * height) / energy.getMaxEnergyStored();
                this.blit(guiLeft + 8, guiTop + 76 - remaining, 176, 83 - remaining, 16, remaining + 1);
            }
        });
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        Minecraft.getInstance().fontRenderer.drawString(
                I18n.format("block.buildinggadgets.charging_station"),
                55,
                8,
                Color.DARK_GRAY.getRGB()
        );
   }
}
