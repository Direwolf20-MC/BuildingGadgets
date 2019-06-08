package com.direwolf20.buildinggadgets.client.gui;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import com.mojang.blaze3d.platform.GlStateManager;

public class DireButton extends GuiButtonHelpText {

    public DireButton(int x, int y, int widthIn, int heightIn, String buttonText, @Nullable Runnable action) {
        this(x, y, widthIn, heightIn, buttonText, "", action);
    }

    public DireButton(int x, int y, int widthIn, int heightIn, String buttonText, String helpTextKey, @Nullable Runnable action) {
        super(x, y, widthIn, heightIn, buttonText, helpTextKey, action);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            FontRenderer fontrenderer = Minecraft.getInstance().fontRenderer;
            Minecraft.getInstance().getTextureManager().bindTexture(BUTTON_TEXTURES);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int i = this.getHoverState(this.hovered);
            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            this.drawTexturedModalRect(this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
            this.drawTexturedModalRect(this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);


            int bottomToDraw = 2;
            this.drawTexturedModalRect(this.x, this.y + this.height - bottomToDraw, 0, 66 - bottomToDraw + i * 20, this.width / 2, bottomToDraw);
            this.drawTexturedModalRect(this.x + this.width / 2, this.y + this.height - bottomToDraw, 200 - this.width / 2, 66 - bottomToDraw + i * 20, this.width / 2, bottomToDraw);

            int j = 14737632;

            if (this.packedFGColor != 0) {
                j = this.packedFGColor;
            } else if (!this.enabled) {
                j = 10526880;
            } else if (this.hovered) {
                j = 16777120;
            }

            this.drawCenteredString(fontrenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, j);
        }
    }
}
