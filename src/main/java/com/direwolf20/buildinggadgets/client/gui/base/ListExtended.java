package com.direwolf20.buildinggadgets.client.gui.base;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiListExtended.IGuiListEntry;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;

/**
 * A preset of {@link GuiListExtended} where
 * <ul>
 * <li>No overlays will be drawn
 * <li>No dirt background, but a graident rectangle instead
 * <li>Use {@link GL11#glScissor(int, int, int, int)} instead of overlay pieces to cover overflows
 * <li>Allow selection
 * </ul>
 */
public class ListExtended<E extends IGuiListEntry<E>> extends GuiListExtended<E> {

    private double scaleFactor;

    public ListExtended(int left, int top, int width, int height, int slotHeight) {
        super(Minecraft.getInstance(), width, height, top, top + height, slotHeight);
        // Set left x and right x, somehow MCP gave it a weird name
        this.setSlotXBoundsFromLeft(left);
        // Precomputed as it is pretty costly
        this.scaleFactor = Minecraft.getInstance().mainWindow.getScaleFactor(Minecraft.getInstance().gameSettings.guiScale);

        this.amountScrolled += 0.1D;
        this.bindAmountScrolled();
    }

    @Override
    public void drawScreen(int mouseXIn, int mouseYIn, float partialTicks) {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int) (left * scaleFactor),
                (int) (Minecraft.getInstance().mainWindow.getHeight() - (bottom * scaleFactor)),
                (int) (width * scaleFactor),
                (int) (height * scaleFactor));
        super.drawScreen(mouseXIn, mouseYIn, partialTicks);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
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

    @Override
    protected boolean mouseClicked(int index, int button, double mouseX, double mouseY) {
        setSelectedEntry(index);
        return super.mouseClicked(index, button, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double p_mouseDragged_6_, double p_mouseDragged_8_) {
        return super.mouseDragged(mouseX, mouseY, mouseButton, p_mouseDragged_6_, p_mouseDragged_8_);
    }

    @Override
    public boolean mouseScrolled(double scrolled) {
        return super.mouseScrolled(scrolled);
    }

}
