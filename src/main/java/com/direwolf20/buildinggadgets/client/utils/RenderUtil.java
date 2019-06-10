package com.direwolf20.buildinggadgets.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import static com.direwolf20.buildinggadgets.client.utils.AlignmentUtil.*;

public final class RenderUtil {

    private RenderUtil() { }

    public static FontRenderer getFontRenderer() {
        return Minecraft.getInstance().fontRenderer;
    }

    public static TextureManager getTextureManager() {
        return Minecraft.getInstance().getTextureManager();
    }

    public static void renderTextHorizontalLeft(String text, int leftX, int y, int color) {
        getFontRenderer().drawString(text, leftX, y, color);
    }

    public static void renderTextHorizontalRight(String text, int rightX, int y, int color) {
        getFontRenderer().drawString(text, getXForAlignedRight(getFontRenderer().getStringWidth(text), rightX), y, color);
    }

    public static void renderTextHorizontalMiddle(String text, int leftX, int rightX, int y, int color) {
        getFontRenderer().drawString(text, getXForAlignedCenter(getFontRenderer().getStringWidth(text), leftX, rightX), y, color);
    }

    public static void renderTextVerticalTop(String text, int x, int topY, int color) {
        getFontRenderer().drawString(text, x, topY, color);
    }

    public static void renderTextVerticalBottom(String text, int x, int bottomY, int color) {
        getFontRenderer().drawString(text, x, getYForAlignedBottom(getFontRenderer().FONT_HEIGHT, bottomY), color);
    }

    public static void renderTextVerticalCenter(String text, int x, int topY, int bottomY, int color) {
        getFontRenderer().drawString(text, x, getYForAlignedCenter(getFontRenderer().FONT_HEIGHT, topY, bottomY), color);
    }

    public static void drawCompleteTexture(int x, int y, int width, int height) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, 0).tex(0, 1).endVertex();
        buffer.pos(x + width, y + height, 0).tex(1, 1).endVertex();
        buffer.pos(x + width, y, 0).tex(1, 0).endVertex();
        buffer.pos(x, y, 0).tex(0, 0).endVertex();
        tessellator.draw();
    }

}