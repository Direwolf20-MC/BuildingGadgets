package com.direwolf20.buildinggadgets.client.gui.materiallist;

import com.direwolf20.buildinggadgets.client.gui.DireButton;
import com.direwolf20.buildinggadgets.client.gui.base.GuiBase;
import com.direwolf20.buildinggadgets.client.util.AlignmentUtil;
import com.direwolf20.buildinggadgets.client.util.RenderUtil;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.items.ITemplate;
import com.direwolf20.buildinggadgets.common.tools.InventoryManipulation;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is adapted from Lunatrius's Schematica mod, 1.12.2 version.
 * <a href="github.com/Lunatrius/Schematica/blob/master/src/main/java/com/github/lunatrius/schematica/client/gui/control/GuiSchematicMaterials.java">Github</a>
 */

public class MaterialListGUI extends GuiBase {

    public static final int BUTTON_HEIGHT = 20;
    public static final int BUTTONS_PADDING = 4;

    public static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(BuildingGadgets.MODID, "textures/gui/material_list.png");
    public static final int BACKGROUND_WIDTH = 256;
    public static final int BACKGROUND_HEIGHT = 200;
    public static final int BORDER_SIZE = 4;

    public static final int WINDOW_WIDTH = BACKGROUND_WIDTH - BORDER_SIZE * 2;
    public static final int WINDOW_HEIGHT = BACKGROUND_HEIGHT - BORDER_SIZE * 2;

    public static final int BUTTON_CLOSE_ID = 0;
    public static final int BUTTON_SORTING_MODES_ID = 1;
    public static final int BUTTON_COPY_LIST_ID = 2;

    /**
     * <ol>
     * <li>Item name (localized)
     * <li>Item count
     * </ol>
     */
    public static final String PATTERN_SIMPLE = "%s: %d";
    /**
     * <ol>
     * <li>Item name (localized)
     * <li>Item count
     * <li>Item registry name
     * <li>Formatted stack count, e.g. 5x64+2
     * </ol>
     */
    public static final String PATTERN_DETAILED = "%s: %d (%s, %s)";

    private int backgroundX;
    private int backgroundY;

    private String title;
    private int titleLeft;
    private int titleTop;

    private ItemStack template;
    private List<ItemStack> materials;
    private IntList available;
    private ScrollingMaterialList scrollingList;
    private SortingModes sortingMode = SortingModes.NAME;

    private DireButton buttonClose;
    private DireButton buttonSortingModes;
    private DireButton buttonCopyList;

    private int hoveringTextX;
    private int hoveringTextY;
    private List<String> hoveringText;

    public MaterialListGUI(ItemStack template) {
        this.template = template;
    }

    @Override
    public void initGui() {
        ITemplate item = (ITemplate) template.getItem();

        this.backgroundX = AlignmentUtil.getXForAlignedCenter(BACKGROUND_WIDTH, 0, width);
        this.backgroundY = AlignmentUtil.getYForAlignedCenter(BACKGROUND_HEIGHT, 0, height);

        this.title = I18n.format("gui.buildinggadgets.materialList.title");
        this.titleTop = AlignmentUtil.getYForAlignedCenter(fontRenderer.FONT_HEIGHT, backgroundY, getWindowTopY() + ScrollingMaterialList.TOP);
        this.titleLeft = AlignmentUtil.getXForAlignedCenter(fontRenderer.getStringWidth(title), backgroundX, getWindowRightX());

        this.materials = item.getItemCountMap(template).entrySet().stream()
                .map(e -> new ItemStack(e.getElement().item, e.getCount(), e.getElement().meta))
                .collect(Collectors.toList());
        this.sortMaterialList();
        this.updateAvailableMaterials();
        this.scrollingList = new ScrollingMaterialList(this, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);

        int buttonY = getWindowBottomY() - (ScrollingMaterialList.BOTTOM / 2 + BUTTON_HEIGHT / 2);
        this.buttonClose = new DireButton(BUTTON_CLOSE_ID, 0, buttonY, 0, BUTTON_HEIGHT, I18n.format("gui.buildinggadgets.materialList.button.close"));
        this.buttonSortingModes = new DireButton(BUTTON_SORTING_MODES_ID, 0, buttonY, 0, BUTTON_HEIGHT, sortingMode.getLocalizedName());
        this.buttonCopyList = new DireButton(BUTTON_COPY_LIST_ID, 0, buttonY, 0, BUTTON_HEIGHT, I18n.format("gui.buildinggadgets.materialList.button.copyList"));

        this.addButton(buttonSortingModes);
        this.addButton(buttonCopyList);
        this.addButton(buttonClose);
        this.calculateButtonsWidthAndX();
    }

    private String stringify(boolean detailed) {
        if (detailed)
            return stringifyDetailed();
        return stringifySimple();
    }

    private String stringifyDetailed() {
        return materials.stream()
                .map(item -> String.format(PATTERN_DETAILED,
                        item.getDisplayName(),
                        item.getCount(),
                        item.getItem().getRegistryName(),
                        InventoryManipulation.formatItemCount(item.getMaxStackSize(), item.getCount())))
                .collect(Collectors.joining("\n"));
    }

    private String stringifySimple() {
        return materials.stream()
                .map(item -> String.format(PATTERN_SIMPLE,
                        item.getDisplayName(),
                        item.getCount()))
                .collect(Collectors.joining("\n"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float particleTicks) {
        // drawDefaultBackground();

        Minecraft.getMinecraft().getTextureManager().bindTexture(BACKGROUND_TEXTURE);
        RenderUtil.drawTexturedModalRect(backgroundX, backgroundY, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);

        this.scrollingList.drawScreen(mouseX, mouseY, particleTicks);
        this.drawString(fontRenderer, title, titleLeft, titleTop, Color.WHITE.getRGB());
        super.drawScreen(mouseX, mouseY, particleTicks);

        if (hoveringText != null) {
            RenderHelper.enableGUIStandardItemLighting();
            drawHoveringText(hoveringText, hoveringTextX, hoveringTextY);
            GlStateManager.disableLighting();
            hoveringText = null;
        }
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
            case BUTTON_CLOSE_ID:
                Minecraft.getMinecraft().player.closeScreen();
                return;
            case BUTTON_SORTING_MODES_ID:
                sortingMode = sortingMode.next();
                buttonSortingModes.displayString = sortingMode.getLocalizedName();
                sortMaterialList();
                updateAvailableMaterials();
                return;
            case BUTTON_COPY_LIST_ID:
                boolean detailed = GuiScreen.isCtrlKeyDown();
                GuiScreen.setClipboardString(stringify(detailed));
                String type;
                if (detailed)
                    type = I18n.format("gui.buildinggadgets.materialList.message.copiedMaterialList.detailed");
                else
                    type = I18n.format("gui.buildinggadgets.materialList.message.copiedMaterialList.simple");
                mc.player.sendStatusMessage(new TextComponentTranslation("gui.buildinggadgets.materialList.message.copiedMaterialList", type), true);
                return;
        }
        this.scrollingList.actionPerformed(button);
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
        int usableWidth = getWindowWidth();
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

    public int getWindowLeftX() {
        return backgroundX + BORDER_SIZE;
    }

    public int getWindowRightX() {
        return backgroundX + BACKGROUND_WIDTH - BORDER_SIZE;
    }

    public int getWindowTopY() {
        return backgroundY + BORDER_SIZE;
    }

    public int getWindowBottomY() {
        return backgroundY + BACKGROUND_HEIGHT - BORDER_SIZE;
    }

    public int getWindowWidth() {
        return WINDOW_WIDTH;
    }

    public int getWindowHeight() {
        return WINDOW_HEIGHT;
    }

    public List<ItemStack> getMaterials() {
        return materials;
    }

    public IntList getAvailable() {
        return available;
    }

    public void setTaskHoveringText(int x, int y, List<String> text) {
        hoveringTextX = x;
        hoveringTextY = y;
        hoveringText = text;
    }

}
