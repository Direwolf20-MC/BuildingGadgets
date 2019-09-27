package com.direwolf20.buildinggadgets.client.gui.base;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.gui.widget.list.ExtendedList.AbstractListEntry;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;

import static org.lwjgl.opengl.GL11.*;

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
        renderBackground();
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

        renderList(k, l, mouseX, mouseY, partialTicks);
        GlStateManager.disableDepthTest();

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
        GlStateManager.disableBlend();
    }

    protected void renderContentBackground(Tessellator tessellator, BufferBuilder bufferbuilder) {
        fillGradient(getLeft(), getTop(), getRight(), getBottom(), 0xC0101010, 0xD0101010);
    }

    // No dirt top/bottom background
    @Override
    protected void renderHoleBackground(int p_renderHoleBackground_1_, int p_renderHoleBackground_2_, int p_renderHoleBackground_3_, int p_renderHoleBackground_4_) {
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

        // Dragging elements in panel
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
