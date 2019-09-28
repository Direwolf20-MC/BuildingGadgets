package com.direwolf20.buildinggadgets.client.gui.materiallist;

import com.direwolf20.buildinggadgets.api.materials.MaterialList;
import com.direwolf20.buildinggadgets.api.materials.UniqueItem;
import com.direwolf20.buildinggadgets.client.gui.base.EntryList;
import com.direwolf20.buildinggadgets.common.util.helpers.InventoryHelper;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Iterators;
import com.google.common.collect.Multiset;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.Iterator;

import static com.direwolf20.buildinggadgets.client.gui.materiallist.MaterialListGUI.*;
import static com.direwolf20.buildinggadgets.client.gui.materiallist.ScrollingMaterialList.Entry;
import static org.lwjgl.opengl.GL11.*;

class ScrollingMaterialList extends EntryList<Entry> {
    private static final int UPDATE_MILLIS = 1000;
    static final int TOP = 16;
    static final int BOTTOM = 32;

    private static final int SLOT_SIZE = 18;
    private static final int MARGIN = 2;
    private static final int ENTRY_HEIGHT = Math.max(SLOT_SIZE + MARGIN * 2, Minecraft.getInstance().fontRenderer.FONT_HEIGHT * 2 + MARGIN * 3);
    private static final int LINE_SIDE_MARGIN = 8;

    private MaterialListGUI gui;

    private SortingModes sortingMode;
    private long lastUpdate;
    private Iterator<ImmutableMultiset<UniqueItem>> multisetIterator;

    public ScrollingMaterialList(MaterialListGUI gui) {
        super(gui.getWindowLeftX(),
                gui.getWindowTopY() + TOP,
                gui.getWindowWidth(),
                gui.getWindowHeight() - TOP - BOTTOM,
                ENTRY_HEIGHT);
        this.gui = gui;
        this.setSortingMode(SortingModes.NAME);
        updateEntries();
    }

    private void updateEntries() {
        this.lastUpdate = System.currentTimeMillis();
        this.clearEntries();
        if (multisetIterator == null || ! multisetIterator.hasNext()) {
            MaterialList list = gui.getHeader().getRequiredItems();
            multisetIterator = list != null ? list.iterator() : Iterators.singletonIterator(ImmutableMultiset.of());
        }
        PlayerEntity player = Minecraft.getInstance().player;
        World world = Minecraft.getInstance().world;
        for (Multiset.Entry<UniqueItem> entry : multisetIterator.next().entrySet()) {
            UniqueItem item = entry.getElement();
            addEntry(new Entry(this, item, entry.getCount(), InventoryHelper.countItem(item.toItemStack(), player, world)));
        }
        sort();
    }

    @Override
    protected int getScrollbarPosition() {
        return getRight() - MARGIN - SCROLL_BAR_WIDTH;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_E) {
            Minecraft.getInstance().player.closeScreen();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (lastUpdate + UPDATE_MILLIS < System.currentTimeMillis())
            updateEntries();
        super.render(mouseX, mouseY, partialTicks);
    }

    public void reset() {
        multisetIterator = null;
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
        public void render(int index, int topY, int leftX, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float particleTicks) {
            // Weird render issue with GuiSlot where the right border is slightly offset
            // MARGIN * 2 is just a magic number that made it look nice
            int right = leftX + entryWidth - MARGIN * 2;
            // Centralize entry vertically, for some reason this.getY() is not inclusive on the bottom
            int bottom = topY + entryHeight;

            int slotX = leftX + MARGIN;
            int slotY = topY + MARGIN;

            drawIcon(stack, slotX, slotY);
            drawTextOverlay(right, topY, bottom, slotX);
            drawHoveringText(stack, slotX, slotY, mouseX, mouseY);
        }

        private void drawTextOverlay(int right, int top, int bottom, int slotX) {
            int itemNameX = slotX + SLOT_SIZE + MARGIN;
            // -1 because the bottom x coordinate is exclusive
            renderTextVerticalCenter(itemName, itemNameX, top, bottom, Color.WHITE.getRGB());
            renderTextHorizontalRight(amount, right, getYForAlignedCenter(top, bottom, Minecraft.getInstance().fontRenderer.FONT_HEIGHT), getTextColor());

            drawGuidingLine(right, top, bottom, itemNameX, widthItemName, widthAmount);
        }

        private void drawGuidingLine(int right, int top, int bottom, int itemNameX, int widthItemName, int widthAmount) {
            if (!isSelected()) {
                int lineXStart = itemNameX + widthItemName + LINE_SIDE_MARGIN;
                int lineXEnd = right - widthAmount - LINE_SIDE_MARGIN;
                int lineY = getYForAlignedCenter(top, bottom - 1, 1);
                GlStateManager.enableBlend();
                GlStateManager.disableTexture();
                GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                GlStateManager.color4f(255, 255, 255, 34);
                glLineWidth(1);
                glBegin(GL_LINES);
                glVertex3f(lineXStart, lineY, 0);
                glVertex3f(lineXEnd, lineY, 0);
                glEnd();
                GlStateManager.enableTexture();
            }
        }

        private void drawHoveringText(ItemStack item, int slotX, int slotY, int mouseX, int mouseY) {
            if (isPointInBox(mouseX, mouseY, slotX, slotY, 18, 18))
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
            // TODO add replacement function and make entries selectable
//            if (isMouseOver(x, y)) {
//                parent.setSelected(this);
//                return true;
//            }
            return false;
        }

        public boolean isSelected() {
            return parent.getSelected() == this;
        }
    }

    public SortingModes getSortingMode() {
        return sortingMode;
    }

    public void setSortingMode(SortingModes sortingMode) {
        this.sortingMode = sortingMode;
        sort();
    }

    private void sort() {
        children().sort(sortingMode.getComparator());
    }
}