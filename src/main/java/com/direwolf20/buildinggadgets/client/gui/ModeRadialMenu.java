/**
 * This class was adapted from code written by Vazkii for the PSI mod: https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 */
package com.direwolf20.buildinggadgets.client.gui;

import com.direwolf20.buildinggadgets.client.KeyBindings;
import com.direwolf20.buildinggadgets.client.gui.components.GuiIconActionable;
import com.direwolf20.buildinggadgets.client.gui.components.GuiSliderInt;
import com.direwolf20.buildinggadgets.common.building.modes.BuildingMode;
import com.direwolf20.buildinggadgets.common.building.modes.ExchangingMode;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.gadgets.*;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.*;
import com.direwolf20.buildinggadgets.common.registry.OurSounds;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.lang.MessageTranslation;
import com.direwolf20.buildinggadgets.common.util.lang.RadialTranslation;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ModeRadialMenu extends Screen {

    //TODO move to a enum of modes of Copy-Paste Gadget
    private static final ImmutableList<ResourceLocation> signsCopyPaste = ImmutableList.of(
            new ResourceLocation(Reference.MODID, "textures/gui/mode/copy.png"),
            new ResourceLocation(Reference.MODID, "textures/gui/mode/paste.png")
    );

    private int timeIn = 0;
    private int slotSelected = -1;
    private int segments;
    private GuiSliderInt sliderRange;
    private final List<Button> conditionalButtons = new ArrayList<>();

    public ModeRadialMenu(ItemStack stack) {
        super(new StringTextComponent(""));

        if (stack.getItem() instanceof AbstractGadget)
            setSocketable(stack);
    }

    public void setSocketable(ItemStack stack) {
        if (stack.getItem() instanceof GadgetBuilding)
            segments = BuildingMode.values().length;
        else if (stack.getItem() instanceof GadgetExchanger)
            segments = ExchangingMode.values().length;
        else if (stack.getItem() instanceof GadgetCopyPaste)
            segments = GadgetCopyPaste.ToolMode.values().length;
    }

    @Override
    public void init() {
        conditionalButtons.clear();
        ItemStack tool = getGadget();
        boolean isDestruction = tool.getItem() instanceof GadgetDestruction;
        ScreenPosition right = isDestruction ? ScreenPosition.TOP : ScreenPosition.RIGHT;
        ScreenPosition left = isDestruction ? ScreenPosition.BOTTOM : ScreenPosition.LEFT;

        if (isDestruction) {
            addButton(new PositionedIconActionable(RadialTranslation.DESTRUCTION_OVERLAY, "destroy_overlay", right, send -> {
                if (send)
                    PacketHandler.sendToServer(new PacketChangeRange());

                return GadgetDestruction.getOverlay(getGadget());
            }));
        } else {
            addButton(new PositionedIconActionable(RadialTranslation.ROTATE, "rotate", left, false, send -> {
                if (send)
                    PacketHandler.sendToServer(new PacketRotateMirror(PacketRotateMirror.Operation.ROTATE));

                return false;
            }));
            addButton(new PositionedIconActionable(RadialTranslation.MIRROR, "mirror", left, false, send -> {
                if (send)
                    PacketHandler.sendToServer(new PacketRotateMirror(PacketRotateMirror.Operation.MIRROR));

                return false;
            }));
        }
        if (!(tool.getItem() instanceof GadgetCopyPaste)) {
            if (!isDestruction || Config.GADGETS.GADGET_DESTRUCTION.nonFuzzyEnabled.get()) {
                Button button = new PositionedIconActionable(RadialTranslation.FUZZY, "fuzzy", right, send -> {
                    if (send)
                        PacketHandler.sendToServer(new PacketToggleFuzzy());

                    return AbstractGadget.getFuzzy(getGadget());
                });
                addButton(button);
                conditionalButtons.add(button);
            }
            Button button = new PositionedIconActionable((isDestruction ? RadialTranslation.CONNECTED_AREA : RadialTranslation.CONNECTED_SURFACE), "connected_area", right, send -> {
                if (send)
                    PacketHandler.sendToServer(new PacketToggleConnectedArea());

                return AbstractGadget.getConnectedArea(getGadget());
            });
            addButton(button);
            conditionalButtons.add(button);
            if (!isDestruction) {
                int widthSlider = 82;
                sliderRange = new GuiSliderInt(width / 2 - widthSlider / 2, height / 2 + 72, widthSlider, 14, "Range ", "", 1, Config.GADGETS.maxRange.get(),
                        GadgetUtils.getToolRange(tool), false, true, Color.DARK_GRAY, slider -> {
                    GuiSliderInt sliderI = (GuiSliderInt) slider;
                    sendRangeUpdate(sliderI.getValueInt());
                }, (slider, amount) -> {
                    int value = slider.getValueInt();
                    int valueNew = MathHelper.clamp(value + amount, 1, Config.GADGETS.maxRange.get());
                    sendRangeUpdate(valueNew);
                    slider.setValue(valueNew);
                    slider.updateSlider();
                });
                sliderRange.precision = 1;
                sliderRange.getComponents().forEach(this::addButton);
            }
        } else {
            // Copy Paste specific
            addButton(new PositionedIconActionable(RadialTranslation.OPEN_GUI, "copypaste_opengui", right, send -> {
                if (!send)
                    return false;

                getMinecraft().player.closeScreen();
                if (GadgetCopyPaste.getToolMode(tool) == GadgetCopyPaste.ToolMode.COPY)
                    getMinecraft().displayGuiScreen(new CopyGUI(tool));
                else
                    getMinecraft().displayGuiScreen(new PasteGUI(tool));
                return true;
            }));
        }
        addButton(new PositionedIconActionable(RadialTranslation.RAYTRACE_FLUID, "raytrace_fluid", right, send -> {
            if (send)
                PacketHandler.sendToServer(new PacketToggleRayTraceFluid());

            return AbstractGadget.shouldRayTraceFluid(getGadget());
        }));
        if (tool.getItem() instanceof GadgetBuilding) {
            addButton(new PositionedIconActionable(RadialTranslation.PLACE_ON_TOP, "building_place_atop", right, send -> {
                if (send)
                    PacketHandler.sendToServer(new PacketToggleBlockPlacement());

                return GadgetBuilding.shouldPlaceAtop(getGadget());
            }));
        }
        addButton(new PositionedIconActionable(RadialTranslation.ANCHOR, "anchor", left, send -> {
            if (send)
                PacketHandler.sendToServer(new PacketAnchor());

            ItemStack stack = getGadget();
            if (stack.getItem() instanceof GadgetCopyPaste || stack.getItem() instanceof GadgetDestruction)
                return ((AbstractGadget) stack.getItem()).getAnchor(stack) != null;

            return !GadgetUtils.getAnchor(stack).isEmpty();
        }));
        if (!(tool.getItem() instanceof GadgetExchanger)) {
            addButton(new PositionedIconActionable(RadialTranslation.UNDO, "undo", left, false, send -> {
                if (send)
                    PacketHandler.sendToServer(new PacketUndo());

                return false;
            }));
        }
        updateButtons(tool);
    }

    private void updateButtons(ItemStack tool) {
        int posRight = 0;
        int posLeft = 0;
        int dim = 24;
        int padding = 10;
        boolean isDestruction = tool.getItem() instanceof GadgetDestruction;
        ScreenPosition right = isDestruction ? ScreenPosition.BOTTOM : ScreenPosition.RIGHT;
        for (int i = 0; i < buttons.size(); i++) {
            if (!(buttons.get(i) instanceof PositionedIconActionable))
                continue;

            PositionedIconActionable button = (PositionedIconActionable) buttons.get(i);

            if (!button.visible) continue;
            int offset;
            boolean isRight = button.position == right;
            if (isRight) {
                posRight += dim + padding;
                offset = 70;
            } else {
                posLeft += dim + padding;
                offset = -70 - dim;
            }
            button.setWidth(dim);
            button.setHeight(dim);
            if (isDestruction)
                button.y = height / 2 + (isRight ? 10 : -button.getHeight() - 10);
            else
                button.x = width / 2 + offset;
        }
        posRight = resetPos(tool, padding, posRight);
        posLeft = resetPos(tool, padding, posLeft);
        for (int i = 0; i < buttons.size(); i++) {
            if (!(buttons.get(i) instanceof PositionedIconActionable))
                continue;

            PositionedIconActionable button = (PositionedIconActionable) buttons.get(i);
            if (!button.visible) continue;
            boolean isRight = button.position == right;
            int pos = isRight ? posRight : posLeft;
            if (isDestruction)
                button.x = pos;
            else
                button.y = pos;

            if (isRight)
                posRight += dim + padding;
            else
                posLeft += dim + padding;
        }
    }

    private int resetPos(ItemStack tool, int padding, int pos) {
        return tool.getItem() instanceof GadgetDestruction ? width / 2 - (pos - padding) / 2 : height / 2 - (pos - padding) / 2;
    }

    private ItemStack getGadget() {
        return AbstractGadget.getGadget(Minecraft.getInstance().player);
    }

    @Override
    public void render(int mx, int my, float partialTicks) {
        float stime = 5F;
        float fract = Math.min(stime, timeIn + partialTicks) / stime;
        int x = width / 2;
        int y = height / 2;

        int radiusMin = 26;
        int radiusMax = 60;
        double dist = new Vec3d(x, y, 0).distanceTo(new Vec3d(mx, my, 0));
        boolean inRange = false;
        if (segments != 0) {
            inRange = dist > radiusMin && dist < radiusMax;
            for (Widget button : buttons) {
                if (button instanceof PositionedIconActionable)
                    ((PositionedIconActionable) button).setFaded(inRange);
            }
        }
        RenderSystem.pushMatrix();
        RenderSystem.translatef((1 - fract) * x, (1 - fract) * y, 0);
        RenderSystem.scalef(fract, fract, fract);
        super.render(mx, my, partialTicks);
        RenderSystem.popMatrix();
        if (segments == 0)
            return;

        RenderSystem.pushMatrix();
        RenderSystem.disableTexture();

        float angle = mouseAngle(x, y, mx, my);

//        int highlight = 5;

        RenderSystem.enableBlend();
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        float totalDeg = 0;
        float degPer = 360F / segments;

        List<NameDisplayData> nameData = new ArrayList<>();

        ItemStack tool = getGadget();
        if (tool.isEmpty())
            return;

        slotSelected = -1;
        float offset = 8.5F;

        ImmutableList<ResourceLocation> signs;
        int modeIndex;
        if (tool.getItem() instanceof GadgetBuilding) {
            modeIndex = GadgetBuilding.getToolMode(tool).ordinal();
            signs = BuildingMode.getIcons();
        } else if (tool.getItem() instanceof GadgetExchanger) {
            modeIndex = GadgetExchanger.getToolMode(tool).ordinal();
            signs = ExchangingMode.getIcons();
        } else {
            modeIndex = GadgetCopyPaste.getToolMode(tool).ordinal();
            signs = signsCopyPaste;
        }

        boolean shouldCenter = (segments + 2) % 4 == 0;
        int indexBottom = segments / 4;
        int indexTop = indexBottom + segments / 2;
        for (int seg = 0; seg < segments; seg++) {
            boolean mouseInSector = isCursorInSlice(angle, totalDeg, degPer, inRange);
            float radius = Math.max(0F, Math.min((timeIn + partialTicks - seg * 6F / segments) * 40F, radiusMax));

            GL11.glBegin(GL11.GL_TRIANGLE_STRIP);

            float gs = 0.25F;
            if (seg % 2 == 0)
                gs += 0.1F;

            float r = gs;
            float g = gs + (seg == modeIndex ? 1F : 0.0F);
            float b = gs;
            float a = 0.4F;
            if (mouseInSector) {
                slotSelected = seg;
                r = g = b = 1F;
            }

            RenderSystem.color4f(r, g, b, a);

            for (float i = degPer; i >= 0; i--) {
                float rad = (float) ((i + totalDeg) / 180F * Math.PI);
                double xp = x + Math.cos(rad) * radius;
                double yp = y + Math.sin(rad) * radius;
                if ((int) i == (int) (degPer / 2))
                    nameData.add(new NameDisplayData((int) xp, (int) yp, mouseInSector, shouldCenter && (seg == indexBottom || seg == indexTop)));

                GL11.glVertex2d(x + Math.cos(rad) * radius / 2.3F, y + Math.sin(rad) * radius / 2.3F);
                GL11.glVertex2d(xp, yp);
            }
            totalDeg += degPer;

            GL11.glEnd();
            RenderSystem.color4f(1, 1, 1, 1);

//            if (mouseInSector)
//                radius -= highlight;
        }
        RenderSystem.shadeModel(GL11.GL_FLAT);
        RenderSystem.enableTexture();

        for (int i = 0; i < nameData.size(); i++) {
            NameDisplayData data = nameData.get(i);
            int xp = data.getX();
            int yp = data.getY();

            String name;
            if (tool.getItem() instanceof GadgetBuilding)
                name = BuildingMode.values()[i].toString();
            else if (tool.getItem() instanceof GadgetExchanger)
                name = ExchangingMode.values()[i].toString();
            else
                name = GadgetCopyPaste.ToolMode.values()[i].format();

            int xsp = xp - 4;
            int ysp = yp;
            int width = font.getStringWidth(name);

            if (xsp < x)
                xsp -= width - 8;
            if (ysp < y)
                ysp -= 9;

            Color color = i == modeIndex ? Color.GREEN : Color.WHITE;
            if (data.isSelected())
                font.drawStringWithShadow(name, xsp + (data.isCentralized() ? width / 2 - 4 : 0), ysp, color.getRGB());

            double mod = 0.7;
            int xdp = (int) ((xp - x) * mod + x);
            int ydp = (int) ((yp - y) * mod + y);

            getMinecraft().getTextureManager().bindTexture(signs.get(i));
            RenderSystem.color4f(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 1);
            getMinecraft().getTextureManager().bindTexture(signs.get(i));
            blit(xdp - 8, ydp - 8, 0, 0, 16, 16, 16, 16);
        }

        RenderSystem.enableRescaleNormal();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderHelper.enableStandardItemLighting();

        float s = 2.25F * fract;
        RenderSystem.scalef(s, s, s);
        RenderSystem.translatef(x / s - (tool.getItem() instanceof GadgetCopyPaste ? 8F : 8.5F), y / s - 8, 0);
        itemRenderer.renderItemAndEffectIntoGUI(tool, 0, 0);

        RenderHelper.disableStandardItemLighting();
        RenderSystem.disableBlend();
        RenderSystem.disableRescaleNormal();

        RenderSystem.popMatrix();
    }

    private boolean isCursorInSlice(float angle, float totalDeg, float degPer, boolean inRange) {
        return inRange && angle > totalDeg && angle < totalDeg + degPer;
    }

    private void changeMode() {
        if (slotSelected >= 0) {
            Item gadget = getGadget().getItem();

            // This should logically never fail but implementing a way to ensure that would
            // be a pretty solid idea for the next guy to touch this code.
            String mode;
            if (gadget instanceof GadgetBuilding)
                mode = BuildingMode.values()[slotSelected].toString();
            else if (gadget instanceof GadgetExchanger)
                mode = ExchangingMode.values()[slotSelected].toString();
            else
                mode = GadgetCopyPaste.ToolMode.values()[slotSelected].toString();

            getMinecraft().player.sendStatusMessage(MessageTranslation.MODE_SET.componentTranslation(mode).setStyle(Styles.AQUA), true);

            PacketHandler.sendToServer(new PacketToggleMode(slotSelected));
            OurSounds.BEEP.playSound();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        changeMode();
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void tick() {
        if (!InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), KeyBindings.menuSettings.getKey().getKeyCode())) {
            onClose();
            changeMode();
        }

        ImmutableSet<KeyBinding> set = ImmutableSet.of(getMinecraft().gameSettings.keyBindForward, getMinecraft().gameSettings.keyBindLeft, getMinecraft().gameSettings.keyBindBack, getMinecraft().gameSettings.keyBindRight, getMinecraft().gameSettings.keyBindSneak, getMinecraft().gameSettings.keyBindSprint, getMinecraft().gameSettings.keyBindJump);
        for (KeyBinding k : set)
            KeyBinding.setKeyBindState(k.getKey(), k.isKeyDown());

        timeIn++;
        ItemStack tool = getGadget();
        boolean builder = tool.getItem() instanceof GadgetBuilding;
        if (!builder && !(tool.getItem() instanceof GadgetExchanger))
            return;

        boolean curent;
        boolean changed = false;
        for (int i = 0; i < conditionalButtons.size(); i++) {
            Button button = conditionalButtons.get(i);
            if (builder)
                curent = GadgetBuilding.getToolMode(tool) == BuildingMode.SURFACE;
            else
                curent = i == 0 || GadgetExchanger.getToolMode(tool) == ExchangingMode.SURFACE;

            if (button.visible != curent) {
                button.visible = curent;
                changed = true;
            }
        }
        if (changed)
            updateButtons(tool);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static float mouseAngle(int x, int y, int mx, int my) {
        Vector2f baseVec = new Vector2f(1F, 0F);
        Vector2f mouseVec = new Vector2f(mx - x, my - y);

        float ang = (float) (Math.acos(baseVec.dot(mouseVec) / (baseVec.length() * mouseVec.length())) * (180F / Math.PI));
        return my < y ? 360F - ang : ang;
    }

    private void sendRangeUpdate(int valueNew) {
        if (valueNew != GadgetUtils.getToolRange(getGadget()))
            PacketHandler.sendToServer(new PacketChangeRange(valueNew));
    }

    private static final class NameDisplayData {
        private final int x;
        private final int y;
        private final boolean selected;
        private final boolean centralize;

        private NameDisplayData(int x, int y, boolean selected, boolean centralize) {
            this.x = x;
            this.y = y;
            this.selected = selected;
            this.centralize = centralize;
        }

        private int getX() {
            return x;
        }

        private int getY() {
            return y;
        }

        private boolean isSelected() {
            return selected;
        }

        private boolean isCentralized() {
            return centralize;
        }
    }

    public enum ScreenPosition {
        RIGHT, LEFT, BOTTOM, TOP
    }

    private static class PositionedIconActionable extends GuiIconActionable {
        private ScreenPosition position;

        PositionedIconActionable(RadialTranslation message, String icon, ScreenPosition position, boolean isSelectable, Predicate<Boolean> action) {
            super(0, 0, icon, message.getString(), isSelectable, action);

            this.position = position;
        }

        PositionedIconActionable(RadialTranslation message, String icon, ScreenPosition position, Predicate<Boolean> action) {
            this(message, icon, position, true, action);
        }
    }

    private static class Vector2f {
        public float x;
        public float y;

        public Vector2f(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public final float dot(Vector2f v1) {
            return (this.x * v1.x + this.y * v1.y);
        }

        public final float length() {
            return (float) Math.sqrt(this.x * this.x + this.y * this.y);
        }
    }
}
