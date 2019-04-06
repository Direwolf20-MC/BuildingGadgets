package com.direwolf20.buildinggadgets.client.gui.materiallist;

import com.direwolf20.buildinggadgets.client.gui.base.ListExtended;
import com.direwolf20.buildinggadgets.client.utils.AlignmentUtil;
import com.direwolf20.buildinggadgets.client.utils.RenderUtil;
import com.direwolf20.buildinggadgets.common.tools.UniqueItem;
import com.direwolf20.buildinggadgets.common.utils.helpers.InventoryHelper;
import com.google.common.collect.Multiset;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.awt.*;

import static com.direwolf20.buildinggadgets.client.gui.materiallist.ScrollingMaterialList.Entry;
import static com.direwolf20.buildinggadgets.client.utils.RenderUtil.getFontRenderer;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

class ScrollingMaterialList extends ListExtended<Entry> {

    static final int TOP = 16;
    static final int BOTTOM = 32;

    private static final int SLOT_SIZE = 18;
    private static final int MARGIN = 2;
    private static final int ENTRY_HEIGHT = Math.max(SLOT_SIZE + MARGIN * 2, getFontRenderer().FONT_HEIGHT * 2 + MARGIN * 3);
    private static final int LINE_SIDE_MARGIN = 8;

    private static final int TEXT_STATUS_Y_OFFSET;
    private static final int TEXT_AMOUNT_Y_OFFSET;

    private static final int SCROLL_BAR_WIDTH = 6;

    private static final String TRANSLATION_KEY_AVAILABLE = "gui.buildinggadgets.materialList.message.available";
    private static final String TRANSLATION_KEY_MISSING = "gui.buildinggadgets.materialList.message.missing";

    static {
        int usableHeight = ENTRY_HEIGHT - MARGIN * 2;
        int centerX = usableHeight / 2;
        TEXT_STATUS_Y_OFFSET = centerX - MARGIN / 2 - getFontRenderer().FONT_HEIGHT;
        TEXT_AMOUNT_Y_OFFSET = centerX + MARGIN / 2;
    }

    private MaterialListGUI gui;

    private SortingModes sortingMode;

    private String messageAvailable;
    private String messageMissing;

    public ScrollingMaterialList(MaterialListGUI gui) {
        super(gui.getWindowLeftX(),
                gui.getWindowTopY() + TOP,
                gui.getWindowWidth(),
                gui.getWindowHeight() - TOP - BOTTOM,
                ENTRY_HEIGHT);
        this.gui = gui;
        this.messageAvailable = I18n.format(TRANSLATION_KEY_AVAILABLE);
        this.messageMissing = I18n.format(TRANSLATION_KEY_MISSING);

        Multiset<UniqueItem> materials = gui.getTemplateItem().getItemCountMap(gui.getTemplate());
        EntityPlayer player = Minecraft.getInstance().player;
        World world = Minecraft.getInstance().world;
        for (Multiset.Entry<UniqueItem> entry : materials.entrySet()) {
            UniqueItem item = entry.getElement();
            addEntry(new Entry(this, item, entry.getCount(), InventoryHelper.countItem(item.toItemStack(), player, world)));
        }
        this.setSortingMode(SortingModes.NAME);
    }

    @Override
    protected int getScrollBarX() {
        return right - MARGIN - SCROLL_BAR_WIDTH;
    }

    static class Entry extends GuiListExtended.IGuiListEntry<Entry> {

        private ScrollingMaterialList parent;
        private int required;
        private int available;

        private ItemStack stack;

        private String itemName;
        private String amount;
        private String status;

        private int widthItemName;
        private int widthAmount;
        private int widthStatus;

        public Entry(ScrollingMaterialList parent, UniqueItem item, int required, int available) {
            this.parent = parent;
            this.required = required;
            this.available = MathHelper.clamp(available, 0, required);

            this.stack = new ItemStack(item.getItem());
            this.itemName = stack.getDisplayName().getString();
            // Use this.available since the parameter is not clamped
            this.amount = this.available + "/" + required;
            this.status = hasEnoughItems() ? parent.messageAvailable : parent.messageMissing;
            this.widthItemName = Minecraft.getInstance().fontRenderer.getStringWidth(itemName);
            this.widthAmount = Minecraft.getInstance().fontRenderer.getStringWidth(amount);
            this.widthStatus = Minecraft.getInstance().fontRenderer.getStringWidth(status);
        }

        @Override
        public void drawEntry(int entryWidth, int entryHeight, int mouseX, int mouseY, boolean selected, float partialTicks) {
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

            RenderUtil.renderTextHorizontalRight(status, right, top + TEXT_STATUS_Y_OFFSET, getTextStatusColor());
            RenderUtil.renderTextHorizontalRight(amount, right, top + TEXT_AMOUNT_Y_OFFSET, Color.WHITE.getRGB());

            drawGuidingLine(right, top, bottom, itemNameX, widthItemName, widthAmount, widthStatus);
        }

        private void drawGuidingLine(int right, int top, int bottom, int itemNameX, int widthItemName, int widthAmount, int widthStatus) {
            if (!parent.isSelected(index)) {
                int lineXStart = itemNameX + widthItemName + LINE_SIDE_MARGIN;
                int lineXEnd = right - Math.max(widthAmount, widthStatus) - LINE_SIDE_MARGIN;
                int lineY = AlignmentUtil.getYForAlignedCenter(1, top, bottom - 1);
                GlStateManager.enableAlphaTest();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                parent.drawHorizontalLine(lineXStart, lineXEnd, lineY, 0x22FFFFFF);
            }
        }

        private void drawHoveringText(ItemStack item, int slotX, int slotY, int mouseX, int mouseY) {
            if (mouseX > slotX && mouseY > slotY && mouseX <= slotX + 18 && mouseY <= slotY + 18) {
                parent.gui.renderToolTip(item, mouseX, mouseY);
                GlStateManager.disableLighting();
            }
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

        private int getTextStatusColor() {
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

    }

    public SortingModes getSortingMode() {
        return sortingMode;
    }

    public void setSortingMode(SortingModes sortingMode) {
        this.sortingMode = sortingMode;
        getChildren().sort(sortingMode.getComparator());
    }

}