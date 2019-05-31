package com.direwolf20.buildinggadgets.client.gui.materiallist;

import com.direwolf20.buildinggadgets.client.util.AlignmentUtil;
import com.direwolf20.buildinggadgets.client.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.GuiScrollingList;

import java.awt.*;

import static com.direwolf20.buildinggadgets.client.util.AlignmentUtil.SLOT_SIZE;
import static com.direwolf20.buildinggadgets.client.util.RenderUtil.getFontRenderer;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

class ScrollingMaterialList extends GuiScrollingList {

    static final int MARGIN = 2;
    static final int ENTRY_HEIGHT = Math.max(SLOT_SIZE + MARGIN * 2, getFontRenderer().FONT_HEIGHT * 2 + MARGIN * 3);
    static final int TOP = 24;
    static final int BOTTOM = 32;
    static final int LINE_SIDE_MARGIN = 8;

    private MaterialListGUI parent;

    public ScrollingMaterialList(MaterialListGUI parent, int width, int height) {
        super(Minecraft.getMinecraft(),
                parent.getWindowWidth(),
                height,
                parent.getWindowTopY() + TOP,
                parent.getWindowBottomY() - BOTTOM,
                parent.getWindowLeftX(),
                ENTRY_HEIGHT,
                parent.width,
                parent.height);
        this.parent = parent;
    }

    @Override
    protected int getSize() {
        return parent.getMaterials().size();
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick) {
        selectedIndex = index;
    }

    @Override
    protected boolean isSelected(int slotIndex) {
        // No need to select entries because there is no use for it yet
        return false;
    }

    @Override
    protected void drawBackground() {
    }

    @Override
    protected void drawSlot(int index, int rightIn, int top, int entryHeight, Tessellator tess) {
        ItemStack item = parent.getMaterials().get(index);
        // We don't want our content to be exactly aligned with the border
        int right = rightIn - 2;
        int bottom = top + entryHeight;
        int slotX = left + MARGIN;
        int slotY = top + MARGIN;

        drawIcon(item, slotX, slotY);
        drawTextOverlay(index, right, top, item, bottom, slotX);
        drawHoveringText(item, slotX, slotY);
    }

    private void drawTextOverlay(int index, int right, int top, ItemStack item, int bottom, int slotX) {
        String itemName = item.getDisplayName();
        int itemNameX = slotX + SLOT_SIZE + MARGIN;
        // -1 because the bottom x coordinate is exclusive
        RenderUtil.renderTextVerticalCenter(itemName, itemNameX, top, bottom - 1, Color.WHITE.getRGB());

        int required = item.getCount();
        int available = MathHelper.clamp(parent.getAvailable().getInt(index), 0, required);
        boolean fulfilled = available == required;
        int color = fulfilled ? Color.GREEN.getRGB() : Color.RED.getRGB();
        String amount = I18n.format("gui.buildinggadgets.materialList.text.statusTemplate", available, required);
        RenderUtil.renderTextHorizontalRight(amount, right, AlignmentUtil.getYForAlignedCenter(getFontRenderer().FONT_HEIGHT, top, bottom), color);

        int widthItemName = Minecraft.getMinecraft().fontRenderer.getStringWidth(itemName);
        int widthAmount = Minecraft.getMinecraft().fontRenderer.getStringWidth(amount);
        drawGuidingLine(index, right, top, bottom, itemNameX, widthItemName, widthAmount);
    }

    private void drawGuidingLine(int index, int right, int top, int bottom, int itemNameX, int widthItemName, int widthAmount) {
        if (!isSelected(index)) {
            int lineXStart = itemNameX + widthItemName + LINE_SIDE_MARGIN;
            int lineXEnd = right - widthAmount - LINE_SIDE_MARGIN;
            int lineY = AlignmentUtil.getYForAlignedCenter(1, top, bottom) - 1;
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            parent.drawHorizontalLine(lineXStart, lineXEnd, lineY, 0x22FFFFFF);
        }
    }

    private void drawHoveringText(ItemStack item, int slotX, int slotY) {
        if (mouseX > slotX && mouseY > slotY && mouseX <= slotX + 18 && mouseY <= slotY + 18) {
            parent.setTaskHoveringText(mouseX, mouseY, parent.getItemToolTip(item));
        }
    }

    private void drawIcon(ItemStack item, int slotX, int slotY) {
        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(item, slotX, slotY);
        GlStateManager.disableLighting();
        GlStateManager.color(1, 1, 1);
        GlStateManager.popMatrix();
    }

}