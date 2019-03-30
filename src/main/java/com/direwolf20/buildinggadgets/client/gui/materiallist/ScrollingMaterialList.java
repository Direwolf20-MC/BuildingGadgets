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

    //TODO calculate them based on font height
    private static final int TEXT_STATUS_Y_OFFSET = 0;
    private static final int TEXT_AMOUNT_Y_OFFSET = 12;

    private static final String TRANSLATION_KEY_AVAILABLE = "gui.buildinggadgets.materialList.message.available";
    public static final String TRANSLATION_KEU_MISSING = "gui.buildinggadgets.materialList.message.missing";

    private MaterialListGUI parent;

    private String messageAvailable;
    private String messageMissing;

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
        this.messageAvailable = I18n.format(TRANSLATION_KEY_AVAILABLE);
        this.messageMissing = I18n.format(TRANSLATION_KEU_MISSING);
    }

    @Override
    protected int getSize() {
        return parent.materials.size();
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick) {
        selectedIndex = index;
    }

    @Override
    protected boolean isSelected(int slotIndex) {
        return selectedIndex == slotIndex;
    }

    @Override
    public void drawScreen(int mouseXIn, int mouseYIn, float partialTicks) {
        // GlStateManager.translate(0, parent.backgroundY, 0);
        // GL11.glScissor(left, top, width, height);
        super.drawScreen(mouseXIn, mouseYIn, partialTicks);
        // GL11.glScissor(0, 0, 0, 0);
    }

    @Override
    protected void drawBackground() {
    }

    @Override
    protected void drawSlot(int index, int right, int top, int entryHeight, Tessellator tess) {
        ItemStack item = parent.materials.get(index);
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
        int available = MathHelper.clamp(parent.available.getInt(index), 0, required);
        boolean fulfilled = available == required;
        int color = fulfilled ? Color.GREEN.getRGB() : Color.RED.getRGB();
        String amount = available + "/" + required;
        String status = fulfilled ? messageAvailable : messageMissing;
        RenderUtil.renderTextHorizontalRight(status, right, top + TEXT_STATUS_Y_OFFSET, color);
        RenderUtil.renderTextHorizontalRight(amount, right, top + TEXT_AMOUNT_Y_OFFSET, Color.WHITE.getRGB());

        int widthItemName = Minecraft.getMinecraft().fontRenderer.getStringWidth(itemName);
        int widthAmount = Minecraft.getMinecraft().fontRenderer.getStringWidth(amount);
        int widthStatus = Minecraft.getMinecraft().fontRenderer.getStringWidth(status);
        drawGuidingLine(index, right, top, bottom, itemNameX, widthItemName, widthAmount, widthStatus);
    }

    private void drawGuidingLine(int index, int right, int top, int bottom, int itemNameX, int widthItemName, int widthAmount, int widthStatus) {
        if (!isSelected(index)) {
            int lineXStart = itemNameX + widthItemName + LINE_SIDE_MARGIN;
            int lineXEnd = right - Math.max(widthAmount, widthStatus) - LINE_SIDE_MARGIN;
            int lineY = AlignmentUtil.getYForAlignedCenter(1, top, bottom - 1) - 1;
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            parent.drawHorizontalLine(lineXStart, lineXEnd, lineY, 0x22FFFFFF);
        }
    }

    private void drawHoveringText(ItemStack item, int slotX, int slotY) {
        if (mouseX > slotX && mouseY > slotY && mouseX <= slotX + 18 && mouseY <= slotY + 18) {
            parent.renderToolTip(item, mouseX, mouseY);
            GlStateManager.disableLighting();
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