package com.direwolf20.buildinggadgets.client.gui.materiallist;

import com.direwolf20.buildinggadgets.api.capability.CapabilityTemplate;
import com.direwolf20.buildinggadgets.api.template.ITemplate;
import com.direwolf20.buildinggadgets.api.template.provider.ITemplateKey;
import com.direwolf20.buildinggadgets.api.template.provider.ITemplateProvider;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.util.lang.MaterialListTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class MaterialListGUI extends Screen {

    public static int getXForAlignedRight(int right, int width) {
        return right - width;
    }

    public static int getXForAlignedCenter(int left, int right, int width) {
        return left + (right - left) / 2 - width / 2;
    }

    public static int getYForAlignedCenter(int top, int bottom, int height) {
        return top + (bottom - top) / 2 - height / 2;
    }

    public static int getYForAlignedBottom(int bottom, int height) {
        return bottom - height;
    }

    public static void renderTextVerticalCenter(String text, int leftX, int top, int bottom, int color) {
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        int y = getYForAlignedCenter(top, bottom, fontRenderer.FONT_HEIGHT);
        GlStateManager.enableTexture();
        fontRenderer.drawString(text, leftX, y, color);
    }

    public static void renderTextHorizontalRight(String text, int right, int y, int color) {
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        int x = getXForAlignedRight(right, fontRenderer.getStringWidth(text));
        GlStateManager.enableTexture();
        fontRenderer.drawString(text, x, y, color);
    }

    public static boolean isPointInBox(double x, double y, int bx, int by, int width, int height) {
        return x >= bx &&
                y >= by &&
                x < bx + width &&
                y < by + height;
    }

    public static final int BUTTON_HEIGHT = 20;
    public static final int BUTTONS_PADDING = 4;

    public static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(Reference.MODID, "textures/gui/material_list.png");
    public static final int BACKGROUND_WIDTH = 256;
    public static final int BACKGROUND_HEIGHT = 200;
    public static final int BORDER_SIZE = 4;

    public static final int WINDOW_WIDTH = BACKGROUND_WIDTH - BORDER_SIZE * 2;
    public static final int WINDOW_HEIGHT = BACKGROUND_HEIGHT - BORDER_SIZE * 2;

    // Item name (localized)
    // Item count
    public static final String PATTERN_SIMPLE = "%s: %d";
    // Item name (localized)
    // Item count
    // Item registry name
    // Formatted stack count, e.g. 5x64+2
    public static final String PATTERN_DETAILED = "%s: %d (%s, %s)";

    private int backgroundX;
    private int backgroundY;
    private ItemStack item;

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

    public MaterialListGUI(ItemStack item) {
        super(MaterialListTranslation.TITLE.componentTranslation());
        Preconditions.checkArgument(item.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).isPresent());
        this.item = item;
    }

    @Override
    public void init() {
        this.backgroundX = getXForAlignedCenter(0, width, BACKGROUND_WIDTH);
        this.backgroundY = getYForAlignedCenter(0, height, BACKGROUND_HEIGHT);

        this.title = MaterialListTranslation.TITLE.format();
        this.titleTop = getYForAlignedCenter(backgroundY, getWindowTopY() + ScrollingMaterialList.TOP, font.FONT_HEIGHT);
        this.titleLeft = getXForAlignedCenter(backgroundX, getWindowRightX(), font.getStringWidth(title));

        this.scrollingList = new ScrollingMaterialList(this);
        // Make it receive mouse scroll events, so that the player can use his mouse wheel at the start
        this.setFocused(scrollingList);
        this.children.add(scrollingList);

        int buttonY = getWindowBottomY() - (ScrollingMaterialList.BOTTOM / 2 + BUTTON_HEIGHT / 2);
        this.buttonClose = new Button(0, buttonY, 0, BUTTON_HEIGHT, MaterialListTranslation.BUTTON_CLOSE.format(), b -> Minecraft.getInstance().player.closeScreen());
        this.buttonSortingModes = new Button(0, buttonY, 0, BUTTON_HEIGHT, scrollingList.getSortingMode().getLocalizedName(), (button) -> {
            scrollingList.setSortingMode(scrollingList.getSortingMode().next());
            buttonSortingModes.setMessage(scrollingList.getSortingMode().getLocalizedName());
        });
        this.buttonCopyList = new Button(0, buttonY, 0, BUTTON_HEIGHT, MaterialListTranslation.BUTTON_COPY.format(), (button) -> {
            getMinecraft().keyboardListener.setClipboardString(stringify(Screen.hasControlDown()));
            getMinecraft().player.sendStatusMessage(new TranslationTextComponent(MaterialListTranslation.MESSAGE_COPY_SUCCESS.getTranslationKey()), true);
        });

        // Buttons will be placed left to right in this order
        this.addButton(buttonSortingModes);
        this.addButton(buttonCopyList);
        this.addButton(buttonClose);
        this.children.add(scrollingList);
        this.calculateButtonsWidthAndX();
    }

    private String stringify(boolean detailed) {
        if (detailed)
            return stringifyDetailed();
        return stringifySimple();
    }

    private String stringifyDetailed() {
        return scrollingList.children().stream()
                .map(entry -> String.format(PATTERN_DETAILED,
                        entry.getItemName(),
                        entry.getRequired(),
                        entry.getStack().getItem().getRegistryName(),
                        entry.getFormattedRequired()))
                .collect(Collectors.joining("\n"));
    }

    private String stringifySimple() {
        return scrollingList.children().stream()
                .map(entry -> String.format(PATTERN_SIMPLE,
                        entry.getItemName(),
                        entry.getRequired()))
                .collect(Collectors.joining("\n"));
    }

    @Override
    public void render(int mouseX, int mouseY, float particleTicks) {
        Minecraft.getInstance().getTextureManager().bindTexture(BACKGROUND_TEXTURE);
        GuiUtils.drawTexturedModalRect(backgroundX, backgroundY, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, 0F);

        scrollingList.render(mouseX, mouseY, particleTicks);
        drawString(font, title, titleLeft, titleTop, Color.WHITE.getRGB());
        super.render(mouseX, mouseY, particleTicks);

        if (buttonCopyList.isMouseOver(mouseX, mouseY)) {
            GuiUtils.drawHoveringText(ImmutableList.of(MaterialListTranslation.HELP_COPY_LIST.format()), mouseX, mouseY, width, height, Integer.MAX_VALUE, font);
        } else if (hoveringText != null) {
            GuiUtils.drawHoveringText(hoveringText, hoveringTextX, hoveringTextY, width, height, Integer.MAX_VALUE, font);
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

    public ItemStack getTemplateItem() {
        return item;
    }

    public ITemplate getTemplateCapability() {
        LazyOptional<ITemplateProvider> providerCap = Minecraft.getInstance().world.getCapability(CapabilityTemplate.TEMPLATE_PROVIDER_CAPABILITY);
        if (providerCap.isPresent()) {
            LazyOptional<ITemplateKey> keyCap = item.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY);
            if (keyCap.isPresent()) {
                ITemplateProvider provider = providerCap.orElseThrow(RuntimeException::new);
                ITemplateKey key = keyCap.orElseThrow(RuntimeException::new);
                return provider.getTemplateForKey(key);
            }
            BuildingGadgets.LOG.warn("Item used for material list does not have an ITemplateKey capability!");
            Minecraft.getInstance().player.closeScreen();
            return null;
        }
        BuildingGadgets.LOG.warn("Client world used for material list does not have an ITemplateProvider capability!");
        Minecraft.getInstance().player.closeScreen();
        return null;
    }

    public void setTaskHoveringText(int x, int y, List<String> text) {
        hoveringTextX = x;
        hoveringTextY = y;
        hoveringText = text;
    }

}
