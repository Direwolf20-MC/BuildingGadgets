package com.direwolf20.buildinggadgets.client.gui.materiallist;

import com.direwolf20.buildinggadgets.client.gui.DireButton;
import com.direwolf20.buildinggadgets.client.utils.AlignmentUtil;
import com.direwolf20.buildinggadgets.client.utils.RenderUtil;
import com.direwolf20.buildinggadgets.common.items.Template;
import com.direwolf20.buildinggadgets.common.utils.ref.Reference;
import com.google.common.base.Preconditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class MaterialListGUI extends GuiScreen {

    public static final int BUTTON_HEIGHT = 20;
    public static final int BUTTONS_PADDING = 4;

    public static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(Reference.MODID, "textures/gui/material_list.png");
    public static final int BACKGROUND_WIDTH = 256;
    public static final int BACKGROUND_HEIGHT = 200;
    public static final int BORDER_SIZE = 4;

    public static final int WINDOW_WIDTH = BACKGROUND_WIDTH - BORDER_SIZE * 2;
    public static final int WINDOW_HEIGHT = BACKGROUND_HEIGHT - BORDER_SIZE * 2;

    private int backgroundX;
    private int backgroundY;
    private ItemStack template;

    private String title;
    private int titleLeft;
    private int titleTop;

    private ScrollingMaterialList scrollingList;

    private DireButton buttonClose;
    private DireButton buttonRefreshCount;
    private DireButton buttonSortingModes;

    public MaterialListGUI(ItemStack template) {
        Preconditions.checkArgument(template.getItem() instanceof Template);
        this.template = template;
    }

    @Override
    public void initGui() {
        this.backgroundX = AlignmentUtil.getXForAlignedCenter(BACKGROUND_WIDTH, 0, width);
        this.backgroundY = AlignmentUtil.getYForAlignedCenter(BACKGROUND_HEIGHT, 0, height);

        this.title = I18n.format("gui.buildinggadgets.materialList.title");
        this.titleTop = AlignmentUtil.getYForAlignedCenter(fontRenderer.FONT_HEIGHT, backgroundY, getWindowTopY() + ScrollingMaterialList.TOP);
        this.titleLeft = AlignmentUtil.getXForAlignedCenter(fontRenderer.getStringWidth(title), backgroundX, getWindowRightX());

        this.scrollingList = new ScrollingMaterialList(this);
        this.children.add(scrollingList);

        int buttonY = getWindowBottomY() - (ScrollingMaterialList.BOTTOM / 2 + BUTTON_HEIGHT / 2);
        this.buttonClose = new DireButton(0, buttonY, 0, BUTTON_HEIGHT, I18n.format("gui.buildinggadgets.materialList.button.close"), () -> {
            Minecraft.getInstance().player.closeScreen();
        });
        this.buttonRefreshCount = new DireButton(0, buttonY, 0, BUTTON_HEIGHT, I18n.format("gui.buildinggadgets.materialList.button.refresh"), () -> {
            // scrollingList.re
        });
        this.buttonSortingModes = new DireButton(0, buttonY, 0, BUTTON_HEIGHT, scrollingList.getSortingMode().getLocalizedName(), () -> {
            scrollingList.setSortingMode(scrollingList.getSortingMode().next());
            buttonSortingModes.displayString = scrollingList.getSortingMode().getLocalizedName();
        });
        this.addButton(buttonSortingModes);
        this.addButton(buttonRefreshCount);
        this.addButton(buttonClose);
        this.calculateButtonsWidthAndX();
    }

    @Override
    public void render(int mouseX, int mouseY, float particleTicks) {
        // drawDefaultBackground();

        Minecraft.getInstance().getTextureManager().bindTexture(BACKGROUND_TEXTURE);
        RenderUtil.drawCompleteTexture(backgroundX, backgroundY, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);

        this.scrollingList.drawScreen(mouseX, mouseY, particleTicks);
        this.drawString(fontRenderer, title, titleLeft, titleTop, Color.WHITE.getRGB());
        super.render(mouseX, mouseY, particleTicks);
    }

    @Override
    public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
        return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
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

    private void calculateButtonsWidthAndX() {
        // This part would can create narrower buttons when there are too few of them, due to the vanilla button texture is 200 pixels wide
        int amountButtons = buttons.size();
        int amountMargins = amountButtons - 1;
        int totalMarginWidth = amountMargins * BUTTONS_PADDING;
        int usableWidth = getWindowWidth();
        int buttonWidth = (usableWidth - totalMarginWidth) / amountButtons;

        // Align the box of buttons in the center, and start from the left
        int nextX = getWindowLeftX();

        for (GuiButton button : buttons) {
            button.width = buttonWidth;
            button.x = nextX;
            nextX += buttonWidth + BUTTONS_PADDING;
        }
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

    public ItemStack getTemplate() {
        return template;
    }

    public Template getTemplateItem() {
        return (Template) template.getItem();
    }

}
