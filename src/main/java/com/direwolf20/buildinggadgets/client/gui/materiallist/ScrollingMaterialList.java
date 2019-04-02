package com.direwolf20.buildinggadgets.client.gui.materiallist;

import com.direwolf20.buildinggadgets.client.utils.AlignmentUtil;
import com.direwolf20.buildinggadgets.client.utils.RenderUtil;
import com.direwolf20.buildinggadgets.common.items.Template;
import com.direwolf20.buildinggadgets.common.tools.UniqueItem;
import com.direwolf20.buildinggadgets.common.utils.helpers.InventoryHelper;
import com.google.common.collect.Multiset;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.awt.*;
import java.nio.Buffer;

import static com.direwolf20.buildinggadgets.client.gui.materiallist.ScrollingMaterialList.Entry;
import static com.direwolf20.buildinggadgets.client.utils.RenderUtil.getFontRenderer;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

class ScrollingMaterialList extends GuiListExtended<Entry> {

    private static final int SLOT_SIZE = 18;
    private static final int MARGIN = 2;
    private static final int ENTRY_HEIGHT = Math.max(SLOT_SIZE + MARGIN * 2, getFontRenderer().FONT_HEIGHT * 2 + MARGIN * 3);
    static final int TOP = 24;
    static final int BOTTOM = 32;
    private static final int LINE_SIDE_MARGIN = 8;

    //TODO calculate them based on font height
    private static final int TEXT_STATUS_Y_OFFSET = 0;
    private static final int TEXT_AMOUNT_Y_OFFSET = 12;

    private static final int SCROLL_BAR_WIDTH = 6;

    private static final String TRANSLATION_KEY_AVAILABLE = "gui.buildinggadgets.materialList.message.available";
    private static final String TRANSLATION_KEU_MISSING = "gui.buildinggadgets.materialList.message.missing";

    private MaterialListGUI gui;

    private SortingModes sortingMode;

    private String messageAvailable;
    private String messageMissing;

    public ScrollingMaterialList(MaterialListGUI gui) {
        super(Minecraft.getInstance(),
                gui.getWindowWidth(),
                gui.getWindowHeight() - TOP - BOTTOM,
                gui.getWindowTopY() + TOP,
                gui.getWindowBottomY() - BOTTOM,
                ENTRY_HEIGHT);
        this.gui = gui;
        this.messageAvailable = I18n.format(TRANSLATION_KEY_AVAILABLE);
        this.messageMissing = I18n.format(TRANSLATION_KEU_MISSING);

        this.setSlotXBoundsFromLeft(gui.getWindowLeftX());

        Multiset<UniqueItem> materials = ((Template) gui.template.getItem()).getItemCountMap(gui.template);
        EntityPlayer player = Minecraft.getInstance().player;
        World world = Minecraft.getInstance().world;
        for (Multiset.Entry<UniqueItem> entry : materials.entrySet()) {
            UniqueItem item = entry.getElement();
            addEntry(new Entry(this, item, entry.getCount(), InventoryHelper.countItem(item.toItemStack(), player, world)));
        }
        this.setSortingMode(SortingModes.NAME);
    }

    @Override
    protected boolean isSelected(int slotIndex) {
        return selectedElement == slotIndex;
    }

    @Override
    public void setSelectedEntry(int index) {
        selectedElement = index;
    }

    @Override
    protected int getScrollBarX() {
        return right - MARGIN - SCROLL_BAR_WIDTH;
    }

    @Override
    public void drawScreen(int mouseXIn, int mouseYIn, float partialTicks) {
        // GL11.glEnable(GL11.GL_SCISSOR_TEST);
        // GL11.glScissor(left, bottom, width, height);
        super.drawScreen(mouseXIn, mouseYIn, partialTicks);
        // GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    protected void drawBackground() {
    }

    /**
     * Center background.
     */
    @Override
    protected void drawContainerBackground(Tessellator tessellator) {
        drawGradientRect(left, top, right, bottom, 0xC0101010, 0xD0101010);
    }

    /**
     * Top and bottom overlay pieces.
     */
    @Override
    protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha) {
    }

    static class Entry extends GuiListExtended.IGuiListEntry<Entry> {

        private ScrollingMaterialList parent;
        private UniqueItem item;
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
            this.item = item;
            this.required = required;
            this.available = MathHelper.clamp(available, 0, required);

            this.stack = new ItemStack(item.getItem());
            this.itemName = stack.getDisplayName().getString();
            this.amount = available + "/" + required;
            this.status = hasEnoughItems() ? parent.messageAvailable : parent.messageMissing;
            this.widthItemName = Minecraft.getInstance().fontRenderer.getStringWidth(itemName);
            this.widthAmount = Minecraft.getInstance().fontRenderer.getStringWidth(amount);
            this.widthStatus = Minecraft.getInstance().fontRenderer.getStringWidth(status);
        }

        @Override
        public void drawEntry(int entryWidth, int entryHeight, int mouseX, int mouseY, boolean selected, float partialTicks) {
            int left = getX();
            int top = getY();
            int right = left + entryWidth + 5;
            int bottom = top + entryHeight;

            int slotX = left + MARGIN;
            int slotY = top + MARGIN;

            drawIcon(stack, slotX, slotY);
            drawTextOverlay(selected, right, top, bottom, slotX);
            drawHoveringText(stack, slotX, slotY, mouseX, mouseY);
        }

        private void drawTextOverlay(boolean selected, int right, int top, int bottom, int slotX) {
            int itemNameX = slotX + SLOT_SIZE + MARGIN;
            // -1 because the bottom x coordinate is exclusive
            RenderUtil.renderTextVerticalCenter(itemName, itemNameX, top, bottom - 1, Color.WHITE.getRGB());

            RenderUtil.renderTextHorizontalRight(status, right, top + TEXT_STATUS_Y_OFFSET, getTextStatusColor());
            RenderUtil.renderTextHorizontalRight(amount, right, top + TEXT_AMOUNT_Y_OFFSET, Color.WHITE.getRGB());

            drawGuidingLine(selected, right, top, bottom, itemNameX, widthItemName, widthAmount, widthStatus);
        }

        private void drawGuidingLine(boolean selected, int right, int top, int bottom, int itemNameX, int widthItemName, int widthAmount, int widthStatus) {
            if (!parent.isSelected(index)) {
                int lineXStart = itemNameX + widthItemName + LINE_SIDE_MARGIN;
                int lineXEnd = right - Math.max(widthAmount, widthStatus) - LINE_SIDE_MARGIN;
                int lineY = AlignmentUtil.getYForAlignedCenter(1, top, bottom - 1) - 1;
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