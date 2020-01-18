package com.direwolf20.buildinggadgets.client.screens.components;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.client.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.function.Predicate;

/**
 * A one stop shop for all your icon screens related needs. We support colors,
 * icons, selected and deselected states, sound and loads more. Come on
 * down!
 */
public class GuiIconActionable extends GuiButton {
    private Predicate<Boolean> action;
    private boolean selected;
    private boolean isSelectable;

    private Color selectedColor     = Color.GREEN;
    private Color deselectedColor   = new Color(255, 255, 255);
    private Color activeColor;

    private ResourceLocation selectedTexture;
    private ResourceLocation deselectedTexture;

    private float alpha = 1f;

    public GuiIconActionable(int x, int y, String texture, String message, boolean isSelectable, Predicate<Boolean> action) {
        super(0, x, y, 25, 25, message);
        this.activeColor = deselectedColor;
        this.isSelectable = isSelectable;
        this.action = action;

        this.setSelected(action.test(false));

        // Set the selected and deselected textures.
        String assetLocation = "textures/gui/setting/%s.png";

        this.deselectedTexture = new ResourceLocation(BuildingGadgets.MODID, String.format(assetLocation, texture));
        this.selectedTexture = !isSelectable ? this.deselectedTexture : new ResourceLocation(BuildingGadgets.MODID, String.format(assetLocation, texture + "_selected"));
    }

    /**
     * If yo do not need to be able to select / toggle something then use this constructor as
     * you'll hit missing texture issues if you don't have an active (_selected) texture.
     */
    public GuiIconActionable(int x, int y, String texture, String message, Predicate<Boolean> action) {
        this(x, y, texture, message, false, action);
    }

    public void setFaded(boolean faded) {
        alpha = faded ? .6f : 1f;
    }

    /**
     * This should be used when ever changing select.
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
        this.activeColor = selected ? selectedColor : deselectedColor;
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
        soundHandlerIn.playSound(PositionedSoundRecord.getMasterRecord(ModSounds.BEEP.getSound(), selected ? .6F: 1F));
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        super.mousePressed(mc, mouseX, mouseY);

        if( !(mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height) )
            return false;

        this.action.test(true);
        if (!this.isSelectable)
            return false;

        this.setSelected(!this.selected);
        return true;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if( !visible )
            return;

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        GlStateManager.disableTexture2D();
        GlStateManager.color(activeColor.getRed() / 255f, activeColor.getGreen() / 255f, activeColor.getBlue() / 255f, .15f);
//        blit(this.x, this.y, 0, 0, this.width, this.height, this.width, this.height);
        drawTexturedModalRect(this.x, this.y, 0, 0, this.width, this.height);
        GlStateManager.enableTexture2D();

        GlStateManager.color(1, 1, 1, alpha);
        Minecraft.getMinecraft().getTextureManager().bindTexture(selected ? selectedTexture : deselectedTexture);
//        blit(this.x, this.y, 0, 0, this.width, this.height, this.width, this.height);
        drawModalRectWithCustomSizedTexture(this.x, this.y, 0, 0, this.width, this.height, this.width, this.height);

        ScaledResolution scaledresolution = new ScaledResolution(mc);
        if( mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height )
            drawString(Minecraft.getMinecraft().fontRenderer, this.displayString, mouseX > (scaledresolution.getScaledWidth() / 2) ?  mouseX + 2 : mouseX - Minecraft.getMinecraft().fontRenderer.getStringWidth(this.displayString), mouseY - 10, activeColor.getRGB());
    }
}