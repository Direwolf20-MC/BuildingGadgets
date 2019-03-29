package com.direwolf20.buildinggadgets.client.gui.background;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.google.common.base.Preconditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class Background {

    private static final Tessellator TESSELLATOR = Tessellator.getInstance();
    private static final BufferBuilder BUFFER = TESSELLATOR.getBuffer();

    private static final ResourceLocation TEXTURE = new ResourceLocation(BuildingGadgets.MODID, "textures/gui/generic_gui_components.png");
    private static final int UNIT_LENGTH = 4;
    private static final float UV_MULTIPLIER = 0.00390625f;

    /**
     * Draw a vanilla styled GUI background on the given position with the given width and height.
     * <p>
     * {@code x} and {@code y} includes the top/left border; {@code width} and {@code height} also includes the borders.
     * Since a border is 4 pixels wide, {@code width} and {@code height} must be greater than 8.
     * <p>
     * The background will be drawn in 9 parts max: 4 corners, 4 borders, and a body piece. Only the 4 corners are
     * mandatory, the rest is optional depending on the size of the background to be drawn.
     *
     * @param x      left x of the result, including border
     * @param y      top y of the result, including border
     * @param width  width of the result, including both borders and must be larger than 8
     * @param height height of the result, including both borders and must be larger than 8
     */
    public static void drawVanillaStyle(int x, int y, int width, int height) {
        Preconditions.checkArgument(width >= 8 && height >= 8);

        BUFFER.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        int cornerXRight = x + width - UNIT_LENGTH;
        int cornerYBottom = y + height - UNIT_LENGTH;
        CornerPiece.drawTopLeft(x, y);
        CornerPiece.drawTopRight(cornerXRight, y);
        CornerPiece.drawBottomLeft(x, cornerYBottom);
        CornerPiece.drawBottomRight(cornerXRight, cornerYBottom);

        int bodyWidth = width - UNIT_LENGTH * 2;
        int bodyHeight = height - UNIT_LENGTH * 2;
        int bodyX = x + UNIT_LENGTH;
        int bodyY = y + UNIT_LENGTH;

        if (bodyWidth > 0) {
            EdgePiece.drawTop(bodyX, y, bodyWidth);
            EdgePiece.drawBottom(bodyX, bodyY + bodyHeight, bodyWidth);
        }
        if (bodyHeight > 0) {
            EdgePiece.drawLeft(x, bodyY, bodyHeight);
            EdgePiece.drawRight(bodyX + bodyWidth, bodyY, bodyHeight);
        }

        TESSELLATOR.draw();

        if (bodyWidth > 0 && bodyHeight > 0) {
            GlStateManager.disableTexture2D();
            BUFFER.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            BodyPiece.draw(bodyX, bodyY, bodyWidth, bodyHeight);
            TESSELLATOR.draw();
            GlStateManager.enableTexture2D();
        }
    }

    /**
     * All methods assume {@link #TEXTURE} is already bond with {@link net.minecraft.client.renderer.texture.TextureManager#bindTexture(ResourceLocation)}.
     */
    private static class CornerPiece {

        private static final int TX_TOP_LEFT = 0;
        private static final int TX_TOP_RIGHT = TX_TOP_LEFT + UNIT_LENGTH;
        private static final int TX_BOTTOM_LEFT = TX_TOP_LEFT + UNIT_LENGTH * 2;
        private static final int TX_BOTTOM_RIGHT = TX_TOP_LEFT + UNIT_LENGTH * 3;
        private static final int TY = 0;

        private static void drawTopLeft(int x, int y) {
            plotVertexesTex(x, y, UNIT_LENGTH, UNIT_LENGTH, TX_TOP_LEFT, TY);
        }

        private static void drawTopRight(int x, int y) {
            plotVertexesTex(x, y, UNIT_LENGTH, UNIT_LENGTH, TX_TOP_RIGHT, TY);
        }

        private static void drawBottomLeft(int x, int y) {
            plotVertexesTex(x, y, UNIT_LENGTH, UNIT_LENGTH, TX_BOTTOM_LEFT, TY);
        }

        private static void drawBottomRight(int x, int y) {
            plotVertexesTex(x, y, UNIT_LENGTH, UNIT_LENGTH, TX_BOTTOM_RIGHT, TY);
        }

    }

    /**
     * All methods assume {@link #TEXTURE} is already bond with {@link net.minecraft.client.renderer.texture.TextureManager#bindTexture(ResourceLocation)}.
     */
    private static class EdgePiece {

        private static final int TX_TOP = UNIT_LENGTH * 4;
        private static final int TX_BOTTOM = TX_TOP + UNIT_LENGTH;
        private static final int TX_LEFT = TX_TOP + UNIT_LENGTH * 2;
        private static final int TX_RIGHT = TX_TOP + UNIT_LENGTH * 3;
        private static final int TY = 0;

        private static void drawTop(int x, int y, int width) {
            plotVertexesTex(x, y, width, UNIT_LENGTH, TX_TOP, TY);
        }

        private static void drawBottom(int x, int y, int width) {
            plotVertexesTex(x, y, width, UNIT_LENGTH, TX_BOTTOM, TY);
        }

        private static void drawLeft(int x, int y, int height) {
            plotVertexesTex(x, y, UNIT_LENGTH, height, TX_LEFT, TY);
        }

        private static void drawRight(int x, int y, int height) {
            plotVertexesTex(x, y, UNIT_LENGTH, height, TX_RIGHT, TY);
        }

    }

    private static class BodyPiece {

        private static void draw(int x, int y, int width, int height) {
            plotVertexesColor(x, y, width, height, 0xC6, 0xC6, 0xC6, 0xFF);
        }

    }

    private static void plotVertexesTex(int x1, int y1, int width, int height, int tx, int ty) {
        int x2 = x1 + width;
        int y2 = y1 + height;
        int tx2 = tx + UNIT_LENGTH;
        int ty2 = ty + UNIT_LENGTH;

        float u1 = tx * UV_MULTIPLIER;
        float u2 = tx2 * UV_MULTIPLIER;
        float v1 = ty * UV_MULTIPLIER;
        float v2 = ty2 * UV_MULTIPLIER;

        // Bottom Left -> Top Left -> Top Right -> Bottom Right
        BUFFER.pos(x2, y1, 0).tex(u2, v1).endVertex();
        BUFFER.pos(x1, y1, 0).tex(u1, v1).endVertex();
        BUFFER.pos(x1, y2, 0).tex(u1, v2).endVertex();
        BUFFER.pos(x2, y2, 0).tex(u2, v2).endVertex();
    }

    private static void plotVertexesColor(int x1, int y1, int width, int height, int red, int green, int yellow, int alpha) {
        int x2 = x1 + width;
        int y2 = y1 + height;

        // Bottom Left -> Top Left -> Top Right -> Bottom Right
        BUFFER.pos(x2, y1, 0).color(red, green, yellow, alpha).endVertex();
        BUFFER.pos(x1, y1, 0).color(red, green, yellow, alpha).endVertex();
        BUFFER.pos(x1, y2, 0).color(red, green, yellow, alpha).endVertex();
        BUFFER.pos(x2, y2, 0).color(red, green, yellow, alpha).endVertex();
    }

}
