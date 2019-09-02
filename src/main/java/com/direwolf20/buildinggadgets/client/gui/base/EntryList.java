package com.direwolf20.buildinggadgets.client.gui.base;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.gui.widget.list.ExtendedList.AbstractListEntry;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

/**
 * A preset of {@link ExtendedList} where
 * <ul>
 * <li>No overlays will be drawn
 * <li>No dirt background, but a gradient rectangle instead
 * <li>Use {@link GL11#glScissor(int, int, int, int)} instead of overlay pieces to cover overflows
 * <li>Allow selection
 * <li>Ability to disable the transitioning black rectangles (override {@link #renderTransition(Tessellator,
 * BufferBuilder, int, int)}
 * </ul>
 */
public class EntryList<E extends AbstractListEntry<E>> extends ExtendedList<E> {

    public static final int SCROLL_BAR_WIDTH = 6;

    public EntryList(int left, int top, int width, int height, int slotHeight) {
        super(Minecraft.getInstance(), width, height, top, top + height, slotHeight);
        // Set left x and right x, somehow MCP gave it a weird name
        this.setLeftPos(left);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        glEnable(GL_SCISSOR_TEST);
        double guiScaleFactor = Minecraft.getInstance().mainWindow.getGuiScaleFactor();
        glScissor((int) (getLeft() * guiScaleFactor),
                (int) (Minecraft.getInstance().mainWindow.getHeight() - (getBottom() * guiScaleFactor)),
                (int) (width * guiScaleFactor),
                (int) (height * guiScaleFactor));
        renderParts(mouseX, mouseY, partialTicks);
        glDisable(GL_SCISSOR_TEST);
    }

    // Copied and modified from AbstractLists#render(int, int, float)
    private void renderParts(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        renderContentBackground(tessellator, bufferbuilder);

        int k = getRowLeft();
        int l = y0 + 4 - (int) getScrollAmount();
        if (renderHeader) {
            renderHeader(k, l, tessellator);
        }

        renderList(k, l, mouseX, mouseX, partialTicks);
        GlStateManager.disableDepthTest();

        renderHoleBackground(0, y0, 255, 255);
        renderHoleBackground(y1, height, 255, 255);

        renderTransition(tessellator, bufferbuilder, y0, y0 + 4);
        renderTransition(tessellator, bufferbuilder, y1 - 4, y1);
        GlStateManager.shadeModel(GL_FLAT);

        int j1 = getMaxScroll();
        if (j1 > 0) {
            int k1 = (int) ((float) ((y1 - y0) * (y1 - y0)) / (float) getMaxPosition());
            k1 = MathHelper.clamp(k1, 32, y1 - y0 - 8);
            int l1 = (int) getScrollAmount() * (y1 - y0 - k1) / j1 + y0;
            if (l1 < y0) {
                l1 = y0;
            }
            int x1 = getScrollbarPosition();
            int x2 = x1 + 6;

            GlStateManager.disableTexture();
            bufferbuilder.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            bufferbuilder.pos(x1, y1, 0.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(x2, y1, 0.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(x2, y0, 0.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos(x1, y0, 0.0D).color(0, 0, 0, 255).endVertex();

            bufferbuilder.pos(x1, (l1 + k1), 0.0D).color(128, 128, 128, 255).endVertex();
            bufferbuilder.pos(x2, (l1 + k1), 0.0D).color(128, 128, 128, 255).endVertex();
            bufferbuilder.pos(x2, l1, 0.0D).color(128, 128, 128, 255).endVertex();
            bufferbuilder.pos(x1, l1, 0.0D).color(128, 128, 128, 255).endVertex();

            bufferbuilder.pos(x1, (l1 + k1 - 1), 0.0D).color(192, 192, 192, 255).endVertex();
            bufferbuilder.pos((x2 - 1), (l1 + k1 - 1), 0.0D).color(192, 192, 192, 255).endVertex();
            bufferbuilder.pos((x2 - 1), l1, 0.0D).color(192, 192, 192, 255).endVertex();
            bufferbuilder.pos(x1, l1, 0.0D).color(192, 192, 192, 255).endVertex();
            tessellator.draw();
        }

        renderDecorations(mouseX, mouseX);
        GlStateManager.enableTexture();
        GlStateManager.shadeModel(GL_FLAT);
        GlStateManager.enableAlphaTest();
        GlStateManager.disableBlend();
    }

    protected void renderTransition(Tessellator tessellator, BufferBuilder buffer, int transitionTop, int transitionBottom) {
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ZERO, DestFactor.ONE);
        GlStateManager.disableAlphaTest();
        GlStateManager.shadeModel(GL_SMOOTH);
        buffer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x0, transitionTop, 0.0D).color(0, 0, 0, 255).endVertex();
        buffer.pos(x1, transitionTop, 0.0D).color(0, 0, 0, 255).endVertex();
        buffer.pos(x1, transitionBottom, 0.0D).color(0, 0, 0, 0).endVertex();
        buffer.pos(x0, transitionBottom, 0.0D).color(0, 0, 0, 0).endVertex();
        tessellator.draw();
    }

    protected void renderContentBackground(Tessellator tessellator, BufferBuilder bufferbuilder) {
        fillGradient(getLeft(), getTop(), getRight(), getBottom(), 0xC0101010, 0xD0101010);
    }

    protected final void renderDefaultContentBackground(Tessellator tessellator, BufferBuilder bufferbuilder) {
        this.minecraft.getTextureManager().bindTexture(AbstractGui.BACKGROUND_LOCATION);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        bufferbuilder.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        float v1 = (float) (y1 + (int) getScrollAmount()) / 32.0F;
        float v2 = (float) (y0 + (int) getScrollAmount()) / 32.0F;
        float u1 = (float) x0 / 32.0F;
        float u2 = (float) x1 / 32.0F;
        bufferbuilder.pos(x0, y1, 0.0D).tex(u1, v1).color(32, 32, 32, 255).endVertex();
        bufferbuilder.pos(x1, y1, 0.0D).tex(u2, v1).color(32, 32, 32, 255).endVertex();
        bufferbuilder.pos(x1, y0, 0.0D).tex(u2, v2).color(32, 32, 32, 255).endVertex();
        bufferbuilder.pos(x0, y0, 0.0D).tex(u1, v2).color(32, 32, 32, 255).endVertex();
        tessellator.draw();
    }

    // No dirt top/bottom background
    @Override
    protected void renderHoleBackground(int p_renderHoleBackground_1_, int p_renderHoleBackground_2_, int p_renderHoleBackground_3_, int p_renderHoleBackground_4_) {
    }

    protected final void renderDefaultHoldBackground(int p1, int p2, int p3, int p4) {
        super.renderHoleBackground(p1, p2, p3, p4);
    }

    @Override
    protected int getScrollbarPosition() {
        return getRight() - SCROLL_BAR_WIDTH;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        setDragging(true);
        super.mouseClicked(x, y, button);
        return isMouseOver(x, y);
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        setDragging(false);
        return super.mouseReleased(x, y, button);
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        if (super.mouseDragged(x, y, button, dx, dy))
            return true;

        if (isMouseOver(x, y)) {
            setScrollAmount(getScrollAmount() - dy);
        }
        return true;
    }

    // Copied from AbstractList#getMaxScroll because it is private
    public final int getMaxScroll() {
        return Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
    }

    // TODO wait until mcp fixes naming error, remove
    public int getLeft() {
        return this.x0;
    }

    public int getRight() {
        return this.x1;
    }
}
