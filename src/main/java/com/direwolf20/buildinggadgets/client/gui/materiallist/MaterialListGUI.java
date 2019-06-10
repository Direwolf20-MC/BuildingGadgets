package com.direwolf20.buildinggadgets.client.gui.materiallist;

import com.direwolf20.buildinggadgets.client.gui.base.BasicGUIBase;
import com.direwolf20.buildinggadgets.client.utils.RenderUtil;
import com.direwolf20.buildinggadgets.common.items.ITemplate;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class MaterialListGUI extends BasicGUIBase {

    public static final int BUTTON_HEIGHT = 20;
    public static final int BUTTONS_PADDING = 4;

    public static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(Reference.MODID, "textures/gui/material_list.png");
    public static final int BACKGROUND_WIDTH = 256;
    public static final int BACKGROUND_HEIGHT = 200;
    public static final int BORDER_SIZE = 4;

    public static final int WINDOW_WIDTH = BACKGROUND_WIDTH - BORDER_SIZE * 2;
    public static final int WINDOW_HEIGHT = BACKGROUND_HEIGHT - BORDER_SIZE * 2;

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
    private ItemStack template;

    private String title;
    private int titleLeft;
    private int titleTop;

    private ScrollingMaterialList scrollingList;

    private Button buttonClose;
    private Button buttonSortingModes;
    private Button buttonCopyList;

    private int hoveringTextX;
    private int hoveringTextY;
    private List<String> hoveringText;

    public MaterialListGUI(ItemStack template) {
        super(new StringTextComponent("MaterialListGui"));
        Preconditions.checkArgument(template.getItem() instanceof ITemplate);
        this.template = template;
    }

    /* //TODO find replacement
    @Override
    public void initGui() {
        this.backgroundX = AlignmentUtil.getXForAlignedCenter(BACKGROUND_WIDTH, 0, width);
        this.backgroundY = AlignmentUtil.getYForAlignedCenter(BACKGROUND_HEIGHT, 0, height);

        this.title = MaterialListTranslation.TITLE.format();
        this.titleTop = AlignmentUtil.getYForAlignedCenter(font.FONT_HEIGHT, backgroundY, getWindowTopY() + ScrollingMaterialList.TOP);
        this.titleLeft = AlignmentUtil.getXForAlignedCenter(font.getStringWidth(title), backgroundX, getWindowRightX());

        this.scrollingList = new ScrollingMaterialList(this);
        // Make it receive mouse scroll events, so that the player can use his mouse wheel at the start
        this.setFocused(scrollingList);
        this.children.add(scrollingList);

        int buttonY = getWindowBottomY() - (ScrollingMaterialList.BOTTOM / 2 + BUTTON_HEIGHT / 2);
        this.buttonClose = new Button(0, buttonY, 0, BUTTON_HEIGHT, MaterialListTranslation.BUTTON_CLOSE.format(), b -> Minecraft.getInstance().player.closeScreen());
        this.buttonSortingModes = new Button(0, buttonY, 0, BUTTON_HEIGHT, scrollingList.getSortingMode().getLocalizedName(), () -> {
            scrollingList.setSortingMode(scrollingList.getSortingMode().next());
            buttonSortingModes.setMessage(scrollingList.getSortingMode().getLocalizedName());
        });
        this.buttonCopyList = new Button(0, buttonY, 0, BUTTON_HEIGHT, MaterialListTranslation.BUTTON_COPY.format(), () -> {
            getMinecraft().keyboardListener.setClipboardString(stringify(Screen.hasControlDown()));
            getMinecraft().player.sendStatusMessage(new TranslationTextComponent(MaterialListTranslation.MESSAGE_COPY_SUCCESS.getTranslationKey()), true);
        });

        // Buttons will be placed left to right in this order
        this.addButton(buttonSortingModes);
        this.addButton(buttonCopyList);
        this.addButton(buttonClose);
        this.calculateButtonsWidthAndX();
    }*/

    private String stringify(boolean detailed) {
        if (detailed)
            return stringifyDetailed();
        return stringifySimple();
    }

    private String stringifyDetailed() {
        return scrollingList.getChildren().stream()
                .map(entry -> String.format(PATTERN_DETAILED,
                        entry.getItemName(),
                        entry.getRequired(),
                        entry.getStack().getItem().getRegistryName(),
                        entry.getFormattedRequired()))
                .collect(Collectors.joining("\n"));
    }

    private String stringifySimple() {
        return scrollingList.getChildren().stream()
                .map(entry -> String.format(PATTERN_SIMPLE,
                        entry.getItemName(),
                        entry.getRequired()))
                .collect(Collectors.joining("\n"));
    }

    @Override
    public void render(int mouseX, int mouseY, float particleTicks) {
        Minecraft.getInstance().getTextureManager().bindTexture(BACKGROUND_TEXTURE);
        RenderUtil.drawCompleteTexture(backgroundX, backgroundY, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);

        scrollingList.render(mouseX, mouseY, particleTicks);
        drawString(font, title, titleLeft, titleTop, Color.WHITE.getRGB());
        super.render(mouseX, mouseY, particleTicks);

        if (hoveringText != null) {
            RenderHelper.enableGUIStandardItemLighting();
            setTaskHoveringText(hoveringTextX, hoveringTextY, hoveringText);
            GlStateManager.disableLighting();
            hoveringText = null;
        }
    }


    /**
     * {@inheritDoc} Override to make it visible to outside.
     */
    @Override
    public void hLine(int p_hLine_1_, int p_hLine_2_, int p_hLine_3_, int p_hLine_4_) {
        super.hLine(p_hLine_1_, p_hLine_2_, p_hLine_3_, p_hLine_4_);
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

        for (Widget widget : buttons) {
            widget.setWidth(buttonWidth);
            widget.x = nextX;
            nextX += buttonWidth + BUTTONS_PADDING;
        }
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

    public ITemplate getTemplateItem() {
        return (ITemplate) template.getItem();
    }

    public void setTaskHoveringText(int x, int y, List<String> text) {
        hoveringTextX = x;
        hoveringTextY = y;
        hoveringText = text;
    }

}
