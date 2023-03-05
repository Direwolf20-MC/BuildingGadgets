package com.direwolf20.buildinggadgets.client.screen;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.capability.CapabilityTemplate;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateKey;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateProvider;
import com.direwolf20.buildinggadgets.common.tainted.template.Template;
import com.direwolf20.buildinggadgets.common.tainted.template.TemplateHeader;
import com.direwolf20.buildinggadgets.common.util.lang.MaterialListTranslation;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;

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
    private List<Component> hoveringText;
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
        this.addRenderableWidget(scrollingList);

        int buttonY = getWindowBottomY() - (ScrollingMaterialList.BOTTOM / 2 + BUTTON_HEIGHT / 2);
        this.buttonClose = Button.builder(MaterialListTranslation.BUTTON_CLOSE.componentTranslation(), b -> getMinecraft().player.closeContainer())
                .pos(0, buttonY)
                .size( 0, BUTTON_HEIGHT)
                .build();

        this.buttonSortingModes = Button.builder(scrollingList.getSortingMode().getTranslationProvider().componentTranslation(), (button) -> {
            scrollingList.setSortingMode(scrollingList.getSortingMode().next());
            buttonSortingModes.setMessage(scrollingList.getSortingMode().getTranslationProvider().componentTranslation());
        })
                .pos(0, buttonY)
                .size(0, BUTTON_HEIGHT)
                .build();

        this.buttonCopyList = Button.builder(MaterialListTranslation.BUTTON_COPY.componentTranslation(), (button) -> {
            getMinecraft().keyboardHandler.setClipboard(evaluateTemplateHeader().toJson(false, hasControlDown()));

            if (getMinecraft().player != null)
                getMinecraft().player.displayClientMessage(Component.translatable(MaterialListTranslation.MESSAGE_COPY_SUCCESS.getTranslationKey()), true);
        })
                .pos(0, buttonY)
                .size(0, BUTTON_HEIGHT)
                .build();

        // Buttons will be placed left to right in this order
        this.addRenderableWidget(buttonSortingModes);
        this.addRenderableWidget(buttonCopyList);
        this.addRenderableWidget(buttonClose);

        this.calculateButtonsWidthAndX();
    }

    public TemplateHeader evaluateTemplateHeader() {
        Template template = getTemplateCapability();

        BuildContext context = BuildContext.builder()
                .player(getMinecraft().player)
                .stack(getTemplateItem())
                .build(getMinecraft().level);

        return template.getHeaderAndForceMaterials(context);
    }

    public TemplateHeader getHeader() {
        return header;
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float particleTicks) {
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        blit(matrices, backgroundX, backgroundY, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT); // TODO: Might be wrong

        scrollingList.render(matrices, mouseX, mouseY, particleTicks);
        drawString(matrices, font, title, titleLeft, titleTop, Color.WHITE.getRGB());
        super.render(matrices, mouseX, mouseY, particleTicks);

        if (buttonCopyList.isMouseOver(mouseX, mouseY)) {
            renderTooltip(matrices, Lists.transform(ImmutableList.of(MaterialListTranslation.HELP_COPY_LIST.componentTranslation()), Component::getVisualOrderText), mouseX, mouseY);
//            GuiUtils.drawHoveringText(matrices, ImmutableList.of(MaterialListTranslation.HELP_COPY_LIST.componentTranslation()), mouseX, mouseY, width, height, Integer.MAX_VALUE, textRenderer);
        } else if (hoveringText != null) {
            renderTooltip(matrices, Lists.transform(hoveringText, Component::getVisualOrderText), mouseX, mouseY);

//            GuiUtils.drawHoveringText(matrices, hoveringText, hoveringTextX, hoveringTextY, width, height, Integer.MAX_VALUE, textRenderer);
            hoveringText = null;
        }
    }

    private void calculateButtonsWidthAndX() {
        // This part would can create narrower buttons when there are too few of them, due to the vanilla button texture is 200 pixels wide
        int amountButtons = (int) children().stream().filter(e -> e instanceof Button).count();
        int amountMargins = amountButtons - 1;
        int totalMarginWidth = amountMargins * BUTTONS_PADDING;
        int usableWidth = getWindowWidth();
        int buttonWidth = (usableWidth - totalMarginWidth) / amountButtons;

        // Align the box of buttons in the center, and start from the left
        int nextX = getWindowLeftX();

        for (GuiEventListener widget : children()) {
            if (widget instanceof Button btn) {
                btn.setWidth(buttonWidth);
                btn.setX(nextX);
                nextX += buttonWidth + BUTTONS_PADDING;
            }
        }
    }

    public Template getTemplateCapability() {
        if (getMinecraft().level == null || getMinecraft().player == null)
            return null;

        LazyOptional<ITemplateProvider> providerCap = getMinecraft().level.getCapability(CapabilityTemplate.TEMPLATE_PROVIDER_CAPABILITY);
        if (providerCap.isPresent()) {
            LazyOptional<ITemplateKey> keyCap = item.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY);
            ITemplateProvider provider = providerCap.orElseThrow(RuntimeException::new);
            if (keyCap.isPresent()) {
                provider.registerUpdateListener(this);
                ITemplateKey key = keyCap.orElseThrow(RuntimeException::new);
                return provider.getTemplateForKey(key);
            }
            BuildingGadgets.LOG.warn("Item used for material list does not have an ITemplateKey capability!");
            getMinecraft().player.closeContainer();
            return null;
        }

        BuildingGadgets.LOG.warn("Client world used for material list does not have an ITemplateProvider capability!");
        getMinecraft().player.closeContainer();
        return null;
    }

    public void setTaskHoveringText(int x, int y, List<Component> text) {
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

        this.titleTop = getYForAlignedCenter(backgroundY, getWindowTopY() + ScrollingMaterialList.TOP, font.lineHeight);
        this.titleLeft = getXForAlignedCenter(backgroundX, getWindowRightX(), font.width(title));
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

    public static void renderTextVerticalCenter(PoseStack matrices, String text, int leftX, int top, int bottom, int color) {
        Font fontRenderer = Minecraft.getInstance().font;
        int y = getYForAlignedCenter(top, bottom, fontRenderer.lineHeight);
        RenderSystem.enableTexture();
        fontRenderer.draw(matrices, text, leftX, y, color);
    }

    public static void renderTextHorizontalRight(PoseStack matrices, String text, int right, int y, int color) {
        Font fontRenderer = Minecraft.getInstance().font;
        int x = getXForAlignedRight(right, fontRenderer.width(text));
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
