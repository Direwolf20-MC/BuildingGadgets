package com.direwolf20.buildinggadgets.client.gui.materiallist;

import com.direwolf20.buildinggadgets.client.gui.DireButton;
import com.direwolf20.buildinggadgets.client.util.AlignmentUtil;
import com.direwolf20.buildinggadgets.client.util.RenderUtil;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.items.Template;
import com.direwolf20.buildinggadgets.common.tools.InventoryManipulation;
import com.direwolf20.buildinggadgets.common.tools.UniqueItem;
import com.google.common.collect.Multiset;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is adapted from Lunatrius's Schematica mod, 1.12.2 version.
 * <a href="github.com/Lunatrius/Schematica/blob/master/src/main/java/com/github/lunatrius/schematica/client/gui/control/GuiSchematicMaterials.java">Github</a>
 */

public class MaterialListGUI extends GuiScreen {

    public static final int BUTTON_HEIGHT = 20;
    public static final int BUTTONS_PADDING = 4;

    public static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(BuildingGadgets.MODID, "textures/gui/material_list.png");
    public static final int BACKGROUND_WIDTH = 256;
    public static final int BACKGROUND_HEIGHT = 200;
    public static final int BORDER_SIZE = 4;

    public static final int WINDOW_WIDTH = BACKGROUND_WIDTH - BORDER_SIZE * 2;
    public static final int WINDOW_HEIGHT = BACKGROUND_HEIGHT - BORDER_SIZE * 2;

    private static List<ItemStack> toInternalMaterialList(Multiset<UniqueItem> templateList) {
        List<ItemStack> result = new ArrayList<>();
        for (Multiset.Entry<UniqueItem> entry : templateList.entrySet()) {
            // ItemStack implementation ignores the stack limit
            ItemStack itemStack = new ItemStack(entry.getElement().item, entry.getCount(), entry.getElement().meta);
            result.add(itemStack);
        }
        return result;
    }

    int backgroundX;
    int backgroundY;

    private String title;
    private int titleLeft;
    private int titleTop;

    private ItemStack template;
    List<ItemStack> materials;
    IntList available;
    private ScrollingMaterialList scrollingList;
    private SortingModes sortingMode = SortingModes.NAME;

    private DireButton buttonClose;
    private DireButton buttonRefreshCount;
    private DireButton buttonSortingModes;

    public MaterialListGUI(ItemStack template) {
        this.template = template;
    }

    @Override
    public void initGui() {
        Template item = (Template) template.getItem();

        this.backgroundX = AlignmentUtil.getXForAlignedCenter(BACKGROUND_WIDTH, 0, width);
        this.backgroundY = AlignmentUtil.getYForAlignedCenter(BACKGROUND_HEIGHT, 0, height);

        this.title = I18n.format("gui.buildinggadgets.materialList.title");
        this.titleTop = AlignmentUtil.getYForAlignedCenter(fontRenderer.FONT_HEIGHT, backgroundY, getWindowTopY() + ScrollingMaterialList.TOP);
        this.titleLeft = AlignmentUtil.getXForAlignedCenter(fontRenderer.getStringWidth(title), backgroundX, getWindowRightX());

        this.materials = toInternalMaterialList(item.getItemCountMap(template));
        this.sortMaterialList();
        this.updateAvailableMaterials();
        this.scrollingList = new ScrollingMaterialList(this, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);

        int buttonID = -1;
        int buttonY = getWindowBottomY() - (ScrollingMaterialList.BOTTOM / 2 + BUTTON_HEIGHT / 2);
        this.buttonClose = new DireButton(++buttonID, 0, buttonY, 0, BUTTON_HEIGHT, I18n.format("gui.buildinggadgets.materialList.button.close"));
        this.buttonRefreshCount = new DireButton(++buttonID, 0, buttonY, 0, BUTTON_HEIGHT, I18n.format("gui.buildinggadgets.materialList.button.refreshCount"));
        this.buttonSortingModes = new DireButton(++buttonID, 0, buttonY, 0, BUTTON_HEIGHT, sortingMode.getLocalizedName());
        this.addButton(buttonSortingModes);
        this.addButton(buttonRefreshCount);
        this.addButton(buttonClose);

        this.calculateButtonsWidthAndX();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float particleTicks) {
        // drawDefaultBackground();

        Minecraft.getMinecraft().getTextureManager().bindTexture(BACKGROUND_TEXTURE);
        RenderUtil.drawTexturedModalRect(backgroundX, backgroundY, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);

        this.scrollingList.drawScreen(mouseX, mouseY, particleTicks);
        this.drawString(fontRenderer, title, titleLeft, titleTop, Color.WHITE.getRGB());
        super.drawScreen(mouseX, mouseY, particleTicks);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int mx = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int my = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        this.scrollingList.handleMouseInput(mx, my);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0:
                Minecraft.getMinecraft().player.closeScreen();
                return;
            case 1:
                updateAvailableMaterials();
                return;
            case 2:
                sortingMode = sortingMode.next();
                buttonSortingModes.displayString = sortingMode.getLocalizedName();
                sortMaterialList();
                updateAvailableMaterials();
                return;
        }
        this.scrollingList.actionPerformed(button);
    }

    /**
     * {@inheritDoc} Override to make it visible to outside.
     */
    @Override
    public void renderToolTip(ItemStack stack, int x, int y) {
        super.renderToolTip(stack, x, y);
    }

    /**
     * {@inheritDoc} Override to make it visible to outside.
     */
    @Override
    public void drawHorizontalLine(int startX, int endX, int y, int color) {
        super.drawHorizontalLine(startX, endX, y, color);
    }

    private void updateAvailableMaterials() {
        this.available = InventoryManipulation.countItems(materials, Minecraft.getMinecraft().player);
    }

    private void calculateButtonsWidthAndX() {
        // This part would can create narrower buttons when there are too few of them, due to the vanilla button texture is 200 pixels wide
        int amountButtons = buttonList.size();
        int amountMargins = amountButtons - 1;
        int totalMarginWidth = amountMargins * BUTTONS_PADDING;
        int usableWidth = getWindowWidth() ;
        int buttonWidth = (usableWidth - totalMarginWidth) / amountButtons;

        // Align the box of buttons in the center, and start from the left
        int nextX = getWindowLeftX();

        for (GuiButton button : buttonList) {
            button.width = buttonWidth;
            button.x = nextX;
            nextX += buttonWidth + BUTTONS_PADDING;
        }
    }

    private void sortMaterialList() {
        sortingMode.sortInplace(materials);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    int getWindowLeftX() {
        return backgroundX + BORDER_SIZE;
    }

    int getWindowRightX() {
        return backgroundX + BACKGROUND_WIDTH - BORDER_SIZE;
    }

    int getWindowTopY() {
        return backgroundY + BORDER_SIZE;
    }

    int getWindowBottomY() {
        return backgroundY + BACKGROUND_HEIGHT - BORDER_SIZE;
    }

    int getWindowWidth() {
        return WINDOW_WIDTH;
    }

    int getWindowHeight() {
        return WINDOW_HEIGHT;
    }

}
