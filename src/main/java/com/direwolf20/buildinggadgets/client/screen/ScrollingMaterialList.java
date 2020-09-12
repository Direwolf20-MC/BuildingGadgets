package com.direwolf20.buildinggadgets.client.screen;

import com.direwolf20.buildinggadgets.client.screen.components.EntryList;
import com.direwolf20.buildinggadgets.common.tainted.inventory.IItemIndex;
import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.tainted.inventory.MatchResult;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.IUniqueObject;
import com.direwolf20.buildinggadgets.common.util.lang.ITranslationProvider;
import com.direwolf20.buildinggadgets.common.util.lang.MaterialListTranslation;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Iterators;
import com.google.common.collect.Multiset;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.Comparator;
import java.util.Iterator;

import static com.direwolf20.buildinggadgets.client.screen.MaterialListGUI.*;
import static com.direwolf20.buildinggadgets.client.screen.ScrollingMaterialList.Entry;
import static org.lwjgl.opengl.GL11.*;

// Todo change to AbstractList as it's an easy fix compared to duping the class
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
    private Iterator<ImmutableMultiset<IUniqueObject<?>>> multisetIterator;

    public ScrollingMaterialList(MaterialListGUI gui) {
        super(gui.getWindowLeftX(), gui.getWindowTopY() + TOP, gui.getWindowWidth(), gui.getWindowHeight() - TOP - BOTTOM, ENTRY_HEIGHT);

        this.gui = gui;
        this.setSortingMode(SortingModes.NAME);

        updateEntries();
    }

    private void updateEntries() {
        this.lastUpdate = System.currentTimeMillis();
        this.clearEntries();

        if (multisetIterator == null || !multisetIterator.hasNext()) {
            MaterialList list = gui.getHeader().getRequiredItems();
            multisetIterator = list != null ? list.iterator() : Iterators.singletonIterator(ImmutableMultiset.of());
        }

        PlayerEntity player = Minecraft.getInstance().player;

        // Could likely just assert
        if( player == null )
            return;

        IItemIndex index = InventoryHelper.index(gui.getTemplateItem(), player);
        MatchResult result = index.tryMatch(multisetIterator.next());

        for (Multiset.Entry<IUniqueObject<?>> entry : result.getChosenOption().entrySet()) {
            IUniqueObject<?> item = entry.getElement();
            addEntry(new Entry(this, item, entry.getCount(), result.getFoundItems().count(entry.getElement())));
        }

        sort();
    }

    @Override
    protected int getScrollbarPositionX() {
        return getRight() - MARGIN - SCROLL_BAR_WIDTH;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_E) {
            assert Minecraft.getInstance().player != null;

            Minecraft.getInstance().player.closeScreen();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        if (lastUpdate + UPDATE_MILLIS < System.currentTimeMillis())
            updateEntries();

        super.render(matrices, mouseX, mouseY, partialTicks);
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

        public Entry(ScrollingMaterialList parent, IUniqueObject<?> item, int required, int available) {
            this.parent = parent;
            this.required = required;
            this.available = MathHelper.clamp(available, 0, required);

            this.stack = item.createStack();
            this.itemName = stack.getDisplayName().getString();

            // Use this.available since the parameter is not clamped
            this.amount = this.available + "/" + required;
            this.widthItemName = Minecraft.getInstance().fontRenderer.getStringWidth(itemName);
            this.widthAmount = Minecraft.getInstance().fontRenderer.getStringWidth(amount);
        }

        @Override
        public void render(MatrixStack matrices, int index, int topY, int leftX, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float particleTicks) {
            // Weird render issue with GuiSlot where the right border is slightly offset
            // MARGIN * 2 is just a magic number that made it look nice
            int right = leftX + entryWidth - MARGIN * 2;
            // Centralize entry vertically, for some reason this.getY() is not inclusive on the bottom
            int bottom = topY + entryHeight;

            int slotX = leftX + MARGIN;
            int slotY = topY + MARGIN;

            drawIcon(stack, slotX, slotY);
            drawTextOverlay(matrices, right, topY, bottom, slotX);
            drawHoveringText(stack, slotX, slotY, mouseX, mouseY);
        }

        private void drawTextOverlay(MatrixStack matrices, int right, int top, int bottom, int slotX) {
            int itemNameX = slotX + SLOT_SIZE + MARGIN;
            // -1 because the bottom x coordinate is exclusive
            renderTextVerticalCenter(matrices, itemName, itemNameX, top, bottom, Color.WHITE.getRGB());
            renderTextHorizontalRight(matrices, amount, right, getYForAlignedCenter(top, bottom, Minecraft.getInstance().fontRenderer.FONT_HEIGHT), getTextColor());

            drawGuidingLine(right, top, bottom, itemNameX, widthItemName, widthAmount);
        }

        private void drawGuidingLine(int right, int top, int bottom, int itemNameX, int widthItemName, int widthAmount) {
            if (!isSelected()) {
                int lineXStart = itemNameX + widthItemName + LINE_SIDE_MARGIN;
                int lineXEnd = right - widthAmount - LINE_SIDE_MARGIN;
                int lineY = getYForAlignedCenter(top, bottom - 1, 1);
                RenderSystem.enableBlend();
                RenderSystem.disableTexture();
                RenderSystem.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                RenderSystem.color4f(255, 255, 255, 34);

                glLineWidth(1);
                glBegin(GL_LINES);
                glVertex3f(lineXStart, lineY, 0);
                glVertex3f(lineXEnd, lineY, 0);
                glEnd();

                RenderSystem.enableTexture();
            }
        }

        private void drawHoveringText(ItemStack item, int slotX, int slotY, int mouseX, int mouseY) {
            if (isPointInBox(mouseX, mouseY, slotX, slotY, 18, 18))
                parent.gui.setTaskHoveringText(mouseX, mouseY, parent.gui.getTooltipFromItem(item));
        }

        private void drawIcon(ItemStack item, int slotX, int slotY) {
            RenderSystem.pushMatrix();
            RenderHelper.enableGuiDepthLighting();
            Minecraft.getInstance().getItemRenderer().renderItemAndEffectIntoGUI(item, slotX, slotY);
            RenderSystem.disableLighting();
            RenderSystem.color3f(1, 1, 1);
            RenderSystem.popMatrix();
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

    enum SortingModes {

        NAME(Comparator.comparing(Entry::getItemName), MaterialListTranslation.BUTTON_SORTING_NAMEAZ),
        NAME_REVERSED(NAME.getComparator().reversed(), MaterialListTranslation.BUTTON_SORTING_NAMEZA),
        REQUIRED(Comparator.comparingInt(Entry::getRequired), MaterialListTranslation.BUTTON_SORTING_REQUIREDACSE),
        REQUIRED_REVERSED(REQUIRED.getComparator().reversed(), MaterialListTranslation.BUTTON_SORTING_MISSINGDESC),
        MISSING(Comparator.comparingInt(Entry::getMissing), MaterialListTranslation.BUTTON_SORTING_MISSINGACSE),
        MISSING_REVERSED(MISSING.getComparator().reversed(), MaterialListTranslation.BUTTON_SORTING_MISSINGDESC);

        private final Comparator<Entry> comparator;
        private final ITranslationProvider translationProvider;

        SortingModes(Comparator<Entry> comparator, ITranslationProvider provider) {
            this.comparator = comparator;
            this.translationProvider = provider;
        }

        public Comparator<Entry> getComparator() {
            return comparator;
        }

        public String getLocalizedName() {
            return translationProvider.format();
        }

        public ITranslationProvider getTranslationProvider() {
            return translationProvider;
        }

        public SortingModes next() {
            int nextIndex = ordinal() + 1;
            return VALUES[nextIndex >= VALUES.length ? 0 : nextIndex];
        }

        public static final SortingModes[] VALUES = SortingModes.values();

    }
}