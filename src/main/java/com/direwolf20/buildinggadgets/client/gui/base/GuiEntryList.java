package com.direwolf20.buildinggadgets.client.gui.base;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.gui.widget.list.ExtendedList.AbstractListEntry;
import net.minecraft.client.renderer.BufferBuilder;
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
        this.setLeftPos(left);
        // Precomputed as it is pretty costly
        this.scaleFactor = Minecraft.getInstance().mainWindow.getGuiScaleFactor();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int) (getLeft() * scaleFactor),
                (int) (Minecraft.getInstance().mainWindow.getHeight() - (getBottom() * scaleFactor)),
                (int) (width * scaleFactor),
                (int) (height * scaleFactor));
        renderParts(mouseX, mouseY, partialTicks);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    private void renderParts(int mouseX, int mouseY, float partialTicks) {
        if (visible) {
            drawBackground();
            int i = getScrollbarPosition();
            int j = i + 6;
            bindAmountScrolled();
            GlStateManager.disableLighting();
            GlStateManager.disableFog();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            // Forge: background rendering moved into separate method.
            drawContainerBackground(tessellator);
            int k = getLeft() + width / 2 - getRowWidth() / 2 + 2;
            int l = getTop() + 4 - (int) getScrollAmount();
            if (hasListHeader) {
                renderHeader(k, l, tessellator);
            }

            drawSelectionBox(k, l, mouseX, mouseY, partialTicks);
            GlStateManager.disableDepthTest();
            overlayBackground(0, getTop(), 255, 255);
            overlayBackground(getBottom(), height, 255, 255);
            GlStateManager.disableTexture();
            drawTransition(tessellator, buffer, getTop(), getTop() + 4);
            drawTransition(tessellator, buffer, getBottom(), getBottom() + 4);

            int j1 = getMaxScroll();
            if (j1 > 0) {
                int k1 = (int) ((float) ((getBottom() - getTop()) * (getBottom() - getTop())) / (float) getContentHeight());
                k1 = MathHelper.clamp(k1, 32, getBottom() - getTop() - 8);
                int l1 = (int) getScrollAmount() * (getBottom() - getTop() - k1) / j1 + getTop();
                if (l1 < getTop()) {
                    l1 = getTop();
                }

                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
                buffer.pos((double) i, (double) getBottom(), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                buffer.pos((double) j, (double) getBottom(), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                buffer.pos((double) j, (double) getTop(), 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                buffer.pos((double) i, (double) getTop(), 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();

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
            GlStateManager.enableTexture();
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

            if (entryY + actualHeight < getTop() || entryY > bottom) {
                updateItemPos(i, insideLeft, entryY, partialTicks);
                continue;
            }

            if (showSelectionBox && isSelected(i)) {
                int i1 = left + width / 2 - getListWidth() / 2;
                int j1 = left + width / 2 + getListWidth() / 2;
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableTexture();
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
                GlStateManager.enableTexture();
            }

            drawSlot(i, insideLeft, entryY, actualHeight, mouseXIn, mouseYIn, partialTicks);
        }
    }

    @Override
    protected void renderBackground() {
        fillGradient(getLeft(), getTop(), getRight(), getBottom(), 0xC0101010, 0xD0101010);
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
        return isMouseOver(x, y);
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

        if (isMouseOver(x, y)) {
            setScrollAmount( getScrollAmount() - dy );
            // fixme: is this still needed in 1.14?
            //            bindAmountScrolled();
        }
        return true;
    }

}
