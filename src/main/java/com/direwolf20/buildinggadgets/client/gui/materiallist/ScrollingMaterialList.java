package com.direwolf20.buildinggadgets.client.gui.materiallist;

import com.direwolf20.buildinggadgets.client.gui.base.GuiEntryList;
import com.direwolf20.buildinggadgets.client.utils.AlignmentUtil;
import com.direwolf20.buildinggadgets.client.utils.RenderUtil;
import com.direwolf20.buildinggadgets.common.util.helpers.InventoryHelper;
import com.direwolf20.buildinggadgets.common.util.tools.UniqueItem;
import com.google.common.collect.Multiset;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.awt.*;

import static com.direwolf20.buildinggadgets.client.gui.materiallist.ScrollingMaterialList.Entry;
import static com.direwolf20.buildinggadgets.client.utils.RenderUtil.getFontRenderer;
import static net.minecraft.client.Minecraft.getInstance;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

class ScrollingMaterialList extends GuiEntryList<Entry> {

    static final int TOP = 16;
    static final int BOTTOM = 32;

    private static final int SLOT_SIZE = 18;
    private static final int MARGIN = 2;
    private static final int ENTRY_HEIGHT = Math.max(SLOT_SIZE + MARGIN * 2, getFontRenderer().FONT_HEIGHT * 2 + MARGIN * 3);
    private static final int LINE_SIDE_MARGIN = 8;

    private static final int SCROLL_BAR_WIDTH = 6;

    private MaterialListGUI gui;

    private SortingModes sortingMode;

    public ScrollingMaterialList(MaterialListGUI gui) {
        super(gui.getWindowLeftX(),
                gui.getWindowTopY() + TOP,
                gui.getWindowWidth(),
                gui.getWindowHeight() - TOP - BOTTOM,
                ENTRY_HEIGHT);
        this.gui = gui;

        Multiset<UniqueItem> materials = gui.getTemplateItem().getItemCountMap(gui.getTemplate());
        this.setSortingMode(SortingModes.NAME);

        PlayerEntity player = getInstance().player;
        World world = Minecraft.getInstance().world;
        for (Multiset.Entry<UniqueItem> entry : materials.entrySet()) {
            UniqueItem item = entry.getElement();
            addEntry(new Entry(this, item, entry.getCount(), InventoryHelper.countItem(item.toItemStack(), player, world)));
        }
    }

    @Override
    protected int getScrollbarPosition() {
        return getRight() - MARGIN - SCROLL_BAR_WIDTH;
    }


    static class Entry extends ExtendedList.AbstractListEntry<Entry> {

        private ScrollingMaterialList parent;
        private int required;
        private int available;

        private ItemStack stack;

        private String itemName;
        private String amount;

        private int widthItemName;
        private int widthAmount;

        public Entry(ScrollingMaterialList parent, UniqueItem item, int required, int available) {
            this.parent = parent;
            this.required = required;
            this.available = MathHelper.clamp(available, 0, required);

            this.stack = new ItemStack(item.getItem());
            this.itemName = stack.getDisplayName().getString();
            // Use this.available since the parameter is not clamped
            this.amount = this.available + "/" + required;
            this.widthItemName = Minecraft.getInstance().fontRenderer.getStringWidth(itemName);
            this.widthAmount = Minecraft.getInstance().fontRenderer.getStringWidth(amount);
        }

        @Override
        public void render(int p_render_1_, int p_render_2_, int p_render_3_, int p_render_4_, int p_render_5_, int p_render_6_, int p_render_7_, boolean p_render_8_, float p_render_9_) {
            int left = getX();
            int top = getY();
            // Weird render issue with GuiSlot where the right border is slightly offset
            // MARGIN * 2 is just a magic number that made it look nice
            int right = left + entryWidth - MARGIN * 2;
            // Centralize entry vertically, for some reason this.getY() is not inclusive on the bottom
            int bottom = top + entryHeight;

            int slotX = left + MARGIN;
            int slotY = top + MARGIN;

            drawIcon(stack, slotX, slotY);
            drawTextOverlay(right, top, bottom, slotX);
            drawHoveringText(stack, slotX, slotY, mouseX, mouseY);
        }

        private void drawTextOverlay(int right, int top, int bottom, int slotX) {
            int itemNameX = slotX + SLOT_SIZE + MARGIN;
            // -1 because the bottom x coordinate is exclusive
            RenderUtil.renderTextVerticalCenter(itemName, itemNameX, top, bottom, Color.WHITE.getRGB());

            RenderUtil.renderTextHorizontalRight(amount, right, AlignmentUtil.getYForAlignedCenter(getFontRenderer().FONT_HEIGHT, top, bottom), getTextColor());

            drawGuidingLine(right, top, bottom, itemNameX, widthItemName, widthAmount);
        }

        private void drawGuidingLine(int right, int top, int bottom, int itemNameX, int widthItemName, int widthAmount) {
            if (!parent.isSelected(index)) {
                int lineXStart = itemNameX + widthItemName + LINE_SIDE_MARGIN;
                int lineXEnd = right - widthAmount - LINE_SIDE_MARGIN;
                int lineY = AlignmentUtil.getYForAlignedCenter(1, top, bottom - 1);
                GlStateManager.enableAlphaTest();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                parent.hLine(lineXStart, lineXEnd, lineY, 0x22FFFFFF);
            }
        }

        private void drawHoveringText(ItemStack item, int slotX, int slotY, int mouseX, int mouseY) {
            if (parent.isMouseOver(mouseX, mouseY) && AlignmentUtil.isPointInBox(mouseX, mouseY, slotX, slotY, 18, 18))
                parent.gui.setTaskHoveringText(mouseX, mouseY, parent.gui.getTooltipFromItem(item));
        }

        private void drawIcon(ItemStack item, int slotX, int slotY) {
            GlStateManager.pushMatrix();
            RenderHelper.enableGUIStandardItemLighting();
            Minecraft.getInstance().getItemRenderer().renderItemAndEffectIntoGUI(item, slotX, slotY);
            GlStateManager.disableLighting();
            GlStateManager.color3f(1, 1, 1);
            GlStateManager.popMatrix();
        }

        private boolean hasEnoughItems() {
            return required == available;
        }

        private int getTextColor() {
            return hasEnoughItems() ? Color.GREEN.getRGB() : Color.RED.getRGB();
        }

        public int getRequired() {
            return required;
        }

        public int getAvailable() {
            return available;
        }

        public int getMissing() {
            return required - available;
        }

        public ItemStack getStack() {
            return stack;
        }

        public String getItemName() {
            return itemName;
        }

        public String getFormattedRequired() {
            int maxSize = stack.getMaxStackSize();
            int stacks = required / maxSize; // Integer division automatically floors
            int leftover = required % maxSize;
            if (stacks == 0)
                return String.valueOf(leftover);
            return stacks + "×" + maxSize + "+" + leftover;
        }

        @Override
        public boolean mouseClicked(double x, double y, int button) {
            // Always select the entry
            return true;
        }

    }

    @Override
    protected void drawTransition(Tessellator tessellator, BufferBuilder buffer, int transitionTop, int transitionBottom) {
    }

    //TODO add replacement function and make entries selectable
    @Override
    protected boolean isSelected(int slotIndex) {
        return false;
    }

    public SortingModes getSortingMode() {
        return sortingMode;
    }

    public void setSortingMode(SortingModes sortingMode) {
        this.sortingMode = sortingMode;
        children().sort(sortingMode.getComparator());
    }

}