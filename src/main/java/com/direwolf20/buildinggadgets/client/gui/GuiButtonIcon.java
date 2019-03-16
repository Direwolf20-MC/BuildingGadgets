package com.direwolf20.buildinggadgets.client.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import java.awt.Color;

import javax.annotation.Nullable;

public class GuiButtonIcon extends GuiButtonColor {
    private Icon iconSelected, iconDeselected;
    private ActionPressed action;
    private float alpha = 1F;

    public GuiButtonIcon(int x, int y, int width, int height, String helpTextKey, Color colorSelected, Color colorDeselected,
            @Nullable Color colorHovered, ResourceLocation textureSelected, @Nullable Runnable action) {
        super(0, x, y, width, height, "", helpTextKey, colorSelected, colorDeselected, colorHovered);
        iconDeselected = new Icon(textureSelected);
        this.action = new ActionPressed(action);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return action.pressed(super.mousePressed(mc, mouseX, mouseY));
    }

    protected void setFaded(boolean faded, int alphaFaded) {
        alpha = faded ? alphaFaded / 255F : 1F;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (!visible)
            return;

        super.drawButton(mc, mouseX, mouseY, partialTicks);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        if (iconSelected == null) {
            ResourceLocation texture = iconDeselected.getModifiedTexture("selected");
            iconSelected = iconDeselected.isTextureMissing(textureManager, texture) ? iconDeselected : new Icon(texture);
        }
        Icon icon = selected ? iconSelected : iconDeselected;
        if (selected)
            GlStateManager.color(colorSelected.getRed() / 255F, colorSelected.getGreen() / 255F, colorSelected.getBlue() / 255F, alpha);
        else
            GlStateManager.color(1F, 1F, 1F, alpha);

        icon.bindTextureColored(textureManager);
        drawTexturedModalRect(x, y, width, height);

        GlStateManager.color(1F, 1F, 1F, alpha);
        if (icon.bindTexture(textureManager))
            drawTexturedModalRect(x, y, width, height);
    }

    private void drawTexturedModalRect(int x, int y, int width, int height) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x + 0, y + height, zLevel).tex(0, 1).endVertex();
        buffer.pos(x + width, y + height, zLevel).tex(1, 1).endVertex();
        buffer.pos(x + width, y + 0, zLevel).tex(1, 0).endVertex();
        buffer.pos(x + 0, y + 0, zLevel).tex(0, 0).endVertex();
        tessellator.draw();
    }

    private static class Icon {
        private ResourceLocation texture, textureColored;

        public Icon(ResourceLocation texture) {
            this.texture = texture;
        }

        public ResourceLocation getModifiedTexture(String suffix) {
            return new ResourceLocation(texture.toString().replace(".png", String.format("_%s.png", suffix)));
        }

        public boolean isTextureMissing(TextureManager textureManager, ResourceLocation texture) {
            textureManager.bindTexture(texture);
            return textureManager.getTexture(texture) == TextureUtil.MISSING_TEXTURE;
        }

        public void bindTextureColored(TextureManager textureManager) {
            if (textureColored == null) {
                textureColored = getModifiedTexture("colored");
                if (isTextureMissing(textureManager, textureColored)) {
                    textureColored = texture;
                    texture = null;
                }
            }
            textureManager.bindTexture(textureColored);
        }

        public boolean bindTexture(TextureManager textureManager) {
            if (texture == null)
                return false;

            textureManager.bindTexture(texture);
            return true;
        }
    }
}