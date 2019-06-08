package com.direwolf20.buildinggadgets.client.gui.base;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.gui.widget.list.ExtendedList.AbstractListEntry;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

/**
 * A preset of {@link ExtendedList} where
 * <ul>
 * <li>No overlays will be drawn
 * <li>No dirt background, but a gradient rectangle instead
 * <li>Use {@link GL11#glScissor(int, int, int, int)} instead of overlay pieces to cover overflows
 * <li>Allow selection
 * <li>Ability to disable the transitioning black rectangles (override {@link #drawTransition(Tessellator,
 * BufferBuilder, int, int)}
 * </ul>
 */
public class GuiEntryList<E extends AbstractListEntry<E>> extends ExtendedList<E> {

    private double scaleFactor;

    public GuiEntryList(int left, int top, int width, int height, int slotHeight) {
        super(Minecraft.getInstance(), width, height, top, top + height, slotHeight);
        // Set left x and right x, somehow MCP gave it a weird name
        this.setSlotXBoundsFromLeft(left);
        // Precomputed as it is pretty costly
        this.scaleFactor = Minecraft.getInstance().mainWindow.getScaleFactor(Minecraft.getInstance().gameSettings.guiScale);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int) (left * scaleFactor),
                (int) (Minecraft.getInstance().mainWindow.getHeight() - (bottom * scaleFactor)),
                (int) (width * scaleFactor),
                (int) (height * scaleFactor));
        renderParts(mouseX, mouseY, partialTicks);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private void renderParts(int mouseX, int mouseY, float partialTicks) {
        if (visible) {
            drawBackground();
            int i = getScrollBarX();
            int j = i + 6;
            bindAmountScrolled();
            GlStateManager.disableLighting();
            GlStateManager.disableFog();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            // Forge: background rendering moved into separate method.
            drawContainerBackground(tessellator);
            int k = left + width / 2 - getListWidth() / 2 + 2;
            int l = top + 4 - (int) amountScrolled;
            if (hasListHeader) {
                drawListHeader(k, l, tessellator);
            }

            drawSelectionBox(k, l, mouseX, mouseY, partialTicks);
            GlStateManager.disableDepthTest();
            overlayBackground(0, top, 255, 255);
            overlayBackground(bottom, height, 255, 255);
            GlStateManager.disableTexture2D();
            drawTransition(tessellator, buffer, top, top + 4);
            drawTransition(tessellator, buffer, bottom, bottom + 4);

            int j1 = getMaxScroll();
            if (j1 > 0) {
                int k1 = (int) ((float) ((bottom - top) * (bottom - top)) / (float) getContentHeight());
                k1 = MathHelper.clamp(k1, 32, bottom - top - 8);
                int l1 = (int) amountScrolled * (bottom - top - k1) / j1 + top;
                if (l1 < top) {
                    l1 = top;
                }

                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
                buffer.pos((double) i, (double) bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                buffer.pos((double) j, (double) bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                buffer.pos((double) j, (double) top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                buffer.pos((double) i, (double) top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();

                buffer.pos((double) i, (double) (l1 + k1), 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                buffer.pos((double) j, (double) (l1 + k1), 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                buffer.pos((double) j, (double) l1, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                buffer.pos((double) i, (double) l1, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();

                buffer.pos((double) i, (double) (l1 + k1 - 1), 0.0D).tex(0.0D, 1.0D).color(192, 192, 192, 255).endVertex();
                buffer.pos((double) (j - 1), (double) (l1 + k1 - 1), 0.0D).tex(1.0D, 1.0D).color(192, 192, 192, 255).endVertex();
                buffer.pos((double) (j - 1), (double) l1, 0.0D).tex(1.0D, 0.0D).color(192, 192, 192, 255).endVertex();
                buffer.pos((double) i, (double) l1, 0.0D).tex(0.0D, 0.0D).color(192, 192, 192, 255).endVertex();
                tessellator.draw();
            }

            renderDecorations(mouseX, mouseY);
            GlStateManager.enableTexture2D();
            GlStateManager.shadeModel(GL11.GL_FLAT);
            GlStateManager.enableAlphaTest();
            GlStateManager.disableBlend();
        }
    }

    protected void drawTransition(Tessellator tessellator, BufferBuilder buffer, int transitionTop, int transitionBottom) {
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        GlStateManager.disableAlphaTest();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos((double) this.left, transitionTop, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        buffer.pos((double) this.right, transitionTop, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
        buffer.pos((double) this.right, transitionBottom, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 0).endVertex();
        buffer.pos((double) this.left, transitionBottom, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 0).endVertex();
        tessellator.draw();
    }

    @Override
    protected void drawSelectionBox(int insideLeft, int insideTop, int mouseXIn, int mouseYIn, float partialTicks) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        for (int i = 0; i < getSize(); ++i) {
            int entryY = insideTop + i * slotHeight + headerPadding;
            // Height of an entry without the borders
            int actualHeight = slotHeight - 4;

            if (entryY + actualHeight < top || entryY > bottom) {
                updateItemPos(i, insideLeft, entryY, partialTicks);
                continue;
            }

            if (showSelectionBox && isSelected(i)) {
                int i1 = left + width / 2 - getListWidth() / 2;
                int j1 = left + width / 2 + getListWidth() / 2;
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableTexture2D();
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
                buffer.pos((double) i1, (double) (entryY + actualHeight + 2), 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                buffer.pos((double) j1, (double) (entryY + actualHeight + 2), 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                buffer.pos((double) j1, (double) (entryY - 2), 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                buffer.pos((double) i1, (double) (entryY - 2), 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                buffer.pos((double) (i1 + 1), (double) (entryY + actualHeight + 1), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                buffer.pos((double) (j1 - 1), (double) (entryY + actualHeight + 1), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                buffer.pos((double) (j1 - 1), (double) (entryY - 1), 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                buffer.pos((double) (i1 + 1), (double) (entryY - 1), 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                tessellator.draw();
                GlStateManager.enableTexture2D();
            }

            drawSlot(i, insideLeft, entryY, actualHeight, mouseXIn, mouseYIn, partialTicks);
        }
    }

    @Override
    protected void drawBackground() {
    }

    /**
     * Center background. Default setting is to draw a gradient rectangle from {@code 0xC0101010} to {@code
     * 0xD0101010}.
     */
    @Override
    protected void drawContainerBackground(Tessellator tessellator) {
        drawGradientRect(left, top, right, bottom, 0xC0101010, 0xD0101010);
    }

    /**
     * Top and bottom overlay pieces. Default setting is draw nothing
     */
    @Override
    protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha) {
    }

    @Override
    protected boolean isSelected(int slotIndex) {
        return selectedElement == slotIndex;
    }

    /**
     * Triggers when the player clicks on an entry.
     */
    @Override
    protected boolean mouseClicked(int index, int button, double x, double y) {
        return super.mouseClicked(index, button, x, y);
    }

    /**
     * @param x      Mouse x
     * @param y      Mouse y
     * @param button Button released, {@code 0} represents the primary button, {@code 1} represents the secondary
     *               button.
     */
    @Override
    public boolean mouseClicked(double x, double y, int button) {
        setDragging(true);
        super.mouseClicked(x, y, button);
        return isMouseInList(x, y);
    }

    /**
     * @param x      Mouse x
     * @param y      Mouse y
     * @param button Button released, {@code 0} represents the primary button, {@code 1} represents the secondary
     *               button.
     */
    @Override
    public boolean mouseReleased(double x, double y, int button) {
        setDragging(false);
        return super.mouseReleased(x, y, button);
    }

    /**
     * Called in intervals when mouse is pressed and moved around.
     *
     * @param x      Mouse x
     * @param y      Mouse y
     * @param button Button pressed, {@code 0} represents the primary button, {@code 1} represents the secondary
     * @param dx     Change in mouse x compared to the last time the function was called
     * @param dy     Change in mouse y compared to the last time the function was called
     */
    @Override
    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        if (super.mouseDragged(x, y, button, dx, dy))
            return true;

        if (isMouseInList(x, y)) {
            amountScrolled -= dy;
            bindAmountScrolled();
        }
        return true;
    }

}
