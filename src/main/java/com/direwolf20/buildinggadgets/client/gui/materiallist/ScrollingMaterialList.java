package com.direwolf20.buildinggadgets.client.gui.materiallist;

import com.direwolf20.buildinggadgets.client.util.AlignmentUtil;
import com.direwolf20.buildinggadgets.client.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

import static com.direwolf20.buildinggadgets.client.util.AlignmentUtil.SLOT_SIZE;
import static com.direwolf20.buildinggadgets.client.util.RenderUtil.getFontRenderer;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

public class ScrollingMaterialList extends GuiSlot {

    static final int MARGIN = 2;
    static final int ENTRY_HEIGHT = Math.max(SLOT_SIZE + MARGIN * 2, getFontRenderer().FONT_HEIGHT * 2 + MARGIN * 3);
    static final int TOP = 24;
    static final int BOTTOM = 32;
    static final int LINE_SIDE_MARGIN = 8;

    //TODO calculate them based on font height
    private static final int TEXT_STATUS_Y_OFFSET = 0;
    private static final int TEXT_AMOUNT_Y_OFFSET = 12;

    private MaterialListGUI parent;

    public ScrollingMaterialList(MaterialListGUI parent, int width, int height) {
        super(Minecraft.getMinecraft(), width, height, TOP, height - BOTTOM, ENTRY_HEIGHT);
        this.parent = parent;
    }

    @Override
    protected int getSize() {
        return parent.materials.size();
    }

    @Override
    protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
        selectedElement = slotIndex;
    }

    @Override
    protected boolean isSelected(int slotIndex) {
        return selectedElement == slotIndex;
    }

    @Override
    protected void drawBackground() {
    }

    @Override
    protected void drawSlot(int id, int left, int top, int height, int mouseX, int mouseY, float partialTicks) {
        ItemStack item = parent.materials.get(id);
        // For some reason selection box is the width as entryWidth (did not consider border)
        int right = left + getListWidth() - 5;
        int bottom = top + ENTRY_HEIGHT;

        int slotX = left + MARGIN;
        int slotY = top + MARGIN;

        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(item, slotX, slotY);
        GlStateManager.disableLighting();
        GlStateManager.color(1, 1, 1);
        GlStateManager.popMatrix();

        String itemName = item.getDisplayName();
        int itemNameX = slotX + SLOT_SIZE + MARGIN;
        // -1 because the bottom x coordinate is exclusive
        RenderUtil.renderTextVerticalCenter(itemName, itemNameX, top, bottom - 1, Color.WHITE.getRGB());

        int required = item.getCount();
        int available = MathHelper.clamp(parent.available.getInt(id), 0, required);
        boolean fulfilled = available == required;
        int color = fulfilled ? Color.GREEN.getRGB() : Color.RED.getRGB();
        String amount = available + "/" + required;
        String status = fulfilled ? "Available" : "Missing";
        RenderUtil.renderTextHorizontalRight(status, right, top + TEXT_STATUS_Y_OFFSET, color);
        RenderUtil.renderTextHorizontalRight(amount, right, top + TEXT_AMOUNT_Y_OFFSET, Color.WHITE.getRGB());

        int lineXStart = itemNameX + Minecraft.getMinecraft().fontRenderer.getStringWidth(itemName) + LINE_SIDE_MARGIN;
        int lineXEnd = right - Math.max(Minecraft.getMinecraft().fontRenderer.getStringWidth(amount), Minecraft.getMinecraft().fontRenderer.getStringWidth(status)) - LINE_SIDE_MARGIN;
        int lineY = AlignmentUtil.getYForAlignedCenter(1, top, bottom - 1) - 1;
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        parent.drawHorizontalLine(lineXStart, lineXEnd, lineY, 0x22FFFFFF);

        if (mouseX > slotX && mouseY > slotY && mouseX <= slotX + 18 && mouseY <= slotY + 18) {
            parent.renderToolTip(item, mouseX, mouseY);
            GlStateManager.disableLighting();
        }
    }

}