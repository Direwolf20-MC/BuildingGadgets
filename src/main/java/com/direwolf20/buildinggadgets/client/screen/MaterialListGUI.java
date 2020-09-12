package com.direwolf20.buildinggadgets.client.screen;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.capability.CapabilityTemplate;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateKey;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateProvider;
import com.direwolf20.buildinggadgets.common.tainted.template.Template;
import com.direwolf20.buildinggadgets.common.tainted.template.TemplateHeader;
import com.direwolf20.buildinggadgets.common.util.lang.MaterialListTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.awt.*;
import java.util.List;
import java.util.UUID;

public class MaterialListGUI extends Screen implements ITemplateProvider.IUpdateListener {

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
    private List<ITextComponent> hoveringText;
    private TemplateHeader header;

    public MaterialListGUI(ItemStack item) {
        super(MaterialListTranslation.TITLE.componentTranslation());
        Preconditions.checkArgument(item.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).isPresent());
        this.item = item;
    }

    @Override
    public void init() {
        this.backgroundX = getXForAlignedCenter(0, width, BACKGROUND_WIDTH);
        this.backgroundY = getYForAlignedCenter(0, height, BACKGROUND_HEIGHT);

        header = evaluateTemplateHeader();
        evaluateTitle();

        this.scrollingList = new ScrollingMaterialList(this);
        // Make it receive mouse scroll events, so that the player can use his mouse wheel at the start
        this.setFocused(scrollingList);
        this.children.add(scrollingList);

        int buttonY = getWindowBottomY() - (ScrollingMaterialList.BOTTOM / 2 + BUTTON_HEIGHT / 2);
        this.buttonClose = new Button(0, buttonY, 0, BUTTON_HEIGHT, MaterialListTranslation.BUTTON_CLOSE.componentTranslation(), b -> getMinecraft().player.closeScreen());
        this.buttonSortingModes = new Button(0, buttonY, 0, BUTTON_HEIGHT, scrollingList.getSortingMode().getTranslationProvider().componentTranslation(), (button) -> {
            scrollingList.setSortingMode(scrollingList.getSortingMode().next());
            buttonSortingModes.setMessage(scrollingList.getSortingMode().getTranslationProvider().componentTranslation());
        });

        this.buttonCopyList = new Button(0, buttonY, 0, BUTTON_HEIGHT, MaterialListTranslation.BUTTON_COPY.componentTranslation(), (button) -> {
            getMinecraft().keyboardListener.setClipboardString(evaluateTemplateHeader().toJson(false, hasControlDown()));

            if( getMinecraft().player != null )
                getMinecraft().player.sendStatusMessage(new TranslationTextComponent(MaterialListTranslation.MESSAGE_COPY_SUCCESS.getTranslationKey()), true);
        });

        // Buttons will be placed left to right in this order
        this.addButton(buttonSortingModes);
        this.addButton(buttonCopyList);
        this.addButton(buttonClose);

        this.children.add(scrollingList);
        this.calculateButtonsWidthAndX();
    }

    public TemplateHeader evaluateTemplateHeader() {
        Template template = getTemplateCapability();

        BuildContext context = BuildContext.builder()
                .player(getMinecraft().player)
                .stack(getTemplateItem())
                .build(getMinecraft().world);

        return template.getHeaderAndForceMaterials(context);
    }

    public TemplateHeader getHeader() {
        return header;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float particleTicks) {
        getMinecraft().getTextureManager().bindTexture(BACKGROUND_TEXTURE);
        GuiUtils.drawTexturedModalRect(backgroundX, backgroundY, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, 0F);

        scrollingList.render(matrices, mouseX, mouseY, particleTicks);
        drawStringWithShadow(matrices, textRenderer, title, titleLeft, titleTop, Color.WHITE.getRGB());
        super.render(matrices, mouseX, mouseY, particleTicks);

        if (buttonCopyList.isMouseOver(mouseX, mouseY)) {
            renderTooltip(matrices, ImmutableList.of(MaterialListTranslation.HELP_COPY_LIST.componentTranslation()), mouseX, mouseY);
//            GuiUtils.drawHoveringText(matrices, ImmutableList.of(MaterialListTranslation.HELP_COPY_LIST.componentTranslation()), mouseX, mouseY, width, height, Integer.MAX_VALUE, textRenderer);
        } else if (hoveringText != null) {
            renderTooltip(matrices, hoveringText, mouseX, mouseY);

//            GuiUtils.drawHoveringText(matrices, hoveringText, hoveringTextX, hoveringTextY, width, height, Integer.MAX_VALUE, textRenderer);
            hoveringText = null;
        }
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

    public Template getTemplateCapability() {
        if( getMinecraft().world == null || getMinecraft().player == null )
            return null;

        LazyOptional<ITemplateProvider> providerCap = getMinecraft().world.getCapability(CapabilityTemplate.TEMPLATE_PROVIDER_CAPABILITY);
        if (providerCap.isPresent()) {
            LazyOptional<ITemplateKey> keyCap = item.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY);
            ITemplateProvider provider = providerCap.orElseThrow(RuntimeException::new);
            if (keyCap.isPresent()) {
                provider.registerUpdateListener(this);
                ITemplateKey key = keyCap.orElseThrow(RuntimeException::new);
                return provider.getTemplateForKey(key);
            }
            BuildingGadgets.LOG.warn("Item used for material list does not have an ITemplateKey capability!");
            getMinecraft().player.closeScreen();
            return null;
        }

        BuildingGadgets.LOG.warn("Client world used for material list does not have an ITemplateProvider capability!");
        getMinecraft().player.closeScreen();
        return null;
    }

    public void setTaskHoveringText(int x, int y, List<ITextComponent> text) {
        hoveringTextX = x;
        hoveringTextY = y;
        hoveringText = text;
    }

    @Override
    public void onTemplateUpdate(ITemplateProvider provider, ITemplateKey key, Template template) {
        item.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).ifPresent(itemKey -> {
            UUID keyId = provider.getId(key);
            UUID itemId = provider.getId(itemKey);
            if (keyId.equals(itemId)) {
                header = evaluateTemplateHeader();
                evaluateTitle();
                scrollingList.reset();
            }
        });
    }

    private void evaluateTitle() {
        String name = getHeader().getName();
        String author = getHeader().getAuthor();

        this.title = name == null && author == null ? MaterialListTranslation.TITLE_EMPTY.format()
                : name == null ? MaterialListTranslation.TITLE_AUTHOR_ONLY.format(author)
                : author == null ? MaterialListTranslation.TITLE_NAME_ONLY.format(name)
                : MaterialListTranslation.TITLE.format(name, author);

        this.titleTop = getYForAlignedCenter(backgroundY, getWindowTopY() + ScrollingMaterialList.TOP, textRenderer.FONT_HEIGHT);
        this.titleLeft = getXForAlignedCenter(backgroundX, getWindowRightX(), textRenderer.getStringWidth(title));
    }

    @Override
    public boolean isPauseScreen() {
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

    public ItemStack getTemplateItem() {
        return item;
    }

    public static int getXForAlignedRight(int right, int width) {
        return right - width;
    }

    public static int getXForAlignedCenter(int left, int right, int width) {
        return left + (right - left) / 2 - width / 2;
    }

    public static int getYForAlignedCenter(int top, int bottom, int height) {
        return top + (bottom - top) / 2 - height / 2;
    }

    public static void renderTextVerticalCenter(MatrixStack matrices, String text, int leftX, int top, int bottom, int color) {
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        int y = getYForAlignedCenter(top, bottom, fontRenderer.FONT_HEIGHT);
        RenderSystem.enableTexture();
        fontRenderer.draw(matrices, text, leftX, y, color);
    }

    public static void renderTextHorizontalRight(MatrixStack matrices, String text, int right, int y, int color) {
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        int x = getXForAlignedRight(right, fontRenderer.getStringWidth(text));
        RenderSystem.enableTexture();
        fontRenderer.draw(matrices, text, x, y, color);
    }

    public static boolean isPointInBox(double x, double y, int bx, int by, int width, int height) {
        return x >= bx &&
                y >= by &&
                x < bx + width &&
                y < by + height;
    }
}
