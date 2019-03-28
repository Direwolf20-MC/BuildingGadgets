/**
 * This class was adapted from code written by Vazkii for the PSI mod: https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 */

package com.direwolf20.buildinggadgets.client.gui;

import com.direwolf20.buildinggadgets.client.KeyBindings;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.ModSounds;
import com.direwolf20.buildinggadgets.common.config.SyncedConfig;
import com.direwolf20.buildinggadgets.common.items.gadgets.*;
import com.direwolf20.buildinggadgets.common.network.*;
import com.direwolf20.buildinggadgets.common.tools.BuildingModes;
import com.direwolf20.buildinggadgets.common.tools.ExchangingModes;
import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ModeRadialMenu extends GuiScreen {

    //TODO move to a enum of modes of Copy-Paste Gadget
    private static final ImmutableList<ResourceLocation> signsCopyPaste = ImmutableList.of(
            new ResourceLocation(BuildingGadgets.MODID, "textures/gui/mode/copy.png"),
            new ResourceLocation(BuildingGadgets.MODID, "textures/gui/mode/paste.png")
    );

    private int timeIn = 0;
    private int slotSelected = -1;
    private int segments;
    private GuiSliderInt sliderRange;
    private final List<GuiButton> conditionalButtons = new ArrayList<>();

    public ModeRadialMenu(ItemStack stack) {
        mc = Minecraft.getMinecraft();
        if (stack.getItem() instanceof GadgetGeneric)
            setSocketable(stack);
    }

    public void setSocketable(ItemStack stack) {
        if (stack.getItem() instanceof GadgetBuilding)
            segments = BuildingModes.values().length;
        else if (stack.getItem() instanceof GadgetExchanger)
            segments = ExchangingModes.values().length;
        else if (stack.getItem() instanceof GadgetCopyPaste)
            segments = GadgetCopyPaste.ToolMode.values().length;
    }

    @Override
    public void initGui() {
        conditionalButtons.clear();
        ItemStack tool = getGadget();
        boolean isDestruction = tool.getItem() instanceof GadgetDestruction;
        ScreenPosition right = isDestruction ? ScreenPosition.TOP : ScreenPosition.RIGHT;
        ScreenPosition left = isDestruction ? ScreenPosition.BOTTOM : ScreenPosition.LEFT;
        if (isDestruction) {
            addButton(new GuiButtonActionCallback("destroy.overlay", right, send -> {
                if (send)
                    PacketHandler.INSTANCE.sendToServer(new PacketChangeRange());

                return GadgetDestruction.getOverlay(getGadget());
            }));
        } else {
            addButton(new GuiButtonActionCallback("rotate", left, send -> {
                if (send)
                    PacketHandler.INSTANCE.sendToServer(new PacketRotateMirror(PacketRotateMirror.Operation.ROTATE));

                return false;
            }).setTogglable(false));
            addButton(new GuiButtonActionCallback("mirror", left, send -> {
                if (send)
                    PacketHandler.INSTANCE.sendToServer(new PacketRotateMirror(PacketRotateMirror.Operation.MIRROR));

                return false;
            }).setTogglable(false));
        }
        if (!(tool.getItem() instanceof GadgetCopyPaste)) {
            if (!isDestruction || SyncedConfig.nonFuzzyEnabledDestruction) {
                GuiButton button = new GuiButtonActionCallback("fuzzy", right, send -> {
                    if (send)
                        PacketHandler.INSTANCE.sendToServer(new PacketToggleFuzzy());

                    return GadgetGeneric.getFuzzy(getGadget());
                });
                addButton(button);
                conditionalButtons.add(button);
            }
            GuiButton button = new GuiButtonActionCallback("connected_" + (isDestruction ? "area" : "surface"), right, send -> {
                if (send)
                    PacketHandler.INSTANCE.sendToServer(new PacketToggleConnectedArea());

                return GadgetGeneric.getConnectedArea(getGadget());
            });
            addButton(button);
            conditionalButtons.add(button);
            if (!isDestruction) {
                int widthSlider = 82;
                sliderRange = new GuiSliderInt(width / 2 - widthSlider / 2, height / 2 + 72, widthSlider, 14, "Range ", "", 1, SyncedConfig.maxRange,
                    GadgetUtils.getToolRange(tool), false, true, Color.DARK_GRAY, slider -> {
                        if (slider.getValueInt() != GadgetUtils.getToolRange(getGadget()))
                            PacketHandler.INSTANCE.sendToServer(new PacketChangeRange(slider.getValueInt()));
                    }, (slider, amount) -> {
                        int value = slider.getValueInt();
                        int valueNew = MathHelper.clamp(value + amount, 1, SyncedConfig.maxRange);
                        slider.setValue(valueNew);
                        slider.updateSlider();
                    });
                sliderRange.precision = 1;
                sliderRange.getComponents().forEach(component -> addButton(component));
            }
        }
        addButton(new GuiButtonActionCallback("raytrace_fluid", right, send -> {
            if (send)
                PacketHandler.INSTANCE.sendToServer(new PacketToggleRayTraceFluid());

            return GadgetGeneric.shouldRayTraceFluid(getGadget());
        }));
        if (tool.getItem() instanceof GadgetBuilding) {
            addButton(new GuiButtonActionCallback("building.place_atop", right, send -> {
                if (send)
                    PacketHandler.INSTANCE.sendToServer(new PacketToggleBlockPlacement());

                return GadgetBuilding.shouldPlaceAtop(getGadget());
            }));
        }
        addButton(new GuiButtonActionCallback("anchor", left, send -> {
            if (send)
                PacketHandler.INSTANCE.sendToServer(new PacketAnchor());

            ItemStack stack = getGadget();
            if (stack.getItem() instanceof GadgetCopyPaste)
                return GadgetCopyPaste.getAnchor(stack) != null;
            else if (stack.getItem() instanceof GadgetDestruction)
                return GadgetDestruction.getAnchor(stack) != null;

            return !GadgetUtils.getAnchor(stack).isEmpty();
        }));
        if (!(tool.getItem() instanceof GadgetExchanger)) {
            addButton(new GuiButtonActionCallback("undo", left, send -> {
                if (send)
                    PacketHandler.INSTANCE.sendToServer(new PacketUndo());

                return false;
            }).setTogglable(false));
        }
        updateButtons(tool);
    }

    private void updateButtons(ItemStack tool) {
        int posRight = 0;
        int posLeft = 0;
        int dim = 22;
        int padding = 10;
        boolean isDestruction = tool.getItem() instanceof GadgetDestruction;
        ScreenPosition right = isDestruction ? ScreenPosition.BOTTOM : ScreenPosition.RIGHT;

        for (GuiButton guiButton : buttonList) {
            if (!(guiButton instanceof GuiButtonActionCallback))
                continue;

            GuiButtonActionCallback button = (GuiButtonActionCallback) guiButton;
            SoundEvent sound = ModSounds.BEEP.getSound();
            button.setSounds(sound, sound, 1F, 0.6F);
            if (!button.visible) continue;
            int offset;
            boolean isRight = button.getScreenPosition() == right;
            if (isRight) {
                posRight += dim + padding;
                offset = 70;
            } else {
                posLeft += dim + padding;
                offset = -70 - dim;
            }
            button.width = dim;
            button.height = dim;
            if (isDestruction)
                button.y = height / 2 + (isRight ? 10 : -button.height - 10);
            else
                button.x = width / 2 + offset;
        }
        posRight = resetPos(tool, padding, posRight);
        posLeft = resetPos(tool, padding, posLeft);
        for (GuiButton guiButton : buttonList) {
            if (!(guiButton instanceof GuiButtonActionCallback))
                continue;

            GuiButtonActionCallback button = (GuiButtonActionCallback) guiButton;
            if (!button.visible) continue;
            boolean isRight = button.getScreenPosition() == right;
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
        return GadgetGeneric.getGadget(Minecraft.getMinecraft().player);
    }

    @Override
    public void drawScreen(int mx, int my, float partialTicks) {
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
            for (GuiButton button : buttonList) {
                if (button instanceof GuiButtonActionCallback)
                    ((GuiButtonActionCallback) button).setFaded(inRange);
            }
        }
        GlStateManager.pushMatrix();
        GlStateManager.translate((1 - fract) * x, (1 - fract) * y, 0);
        GlStateManager.scale(fract, fract, fract);
        super.drawScreen(mx, my, partialTicks);
        GlStateManager.popMatrix();
        if (segments == 0) {
            renderHoverHelpText(mx, my);
            return;
        }
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();

        float angle = mouseAngle(x, y, mx, my);

//        int highlight = 5;

        GlStateManager.enableBlend();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
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
            signs = BuildingModes.getIcons();
        } else if (tool.getItem() instanceof GadgetExchanger) {
            modeIndex = GadgetExchanger.getToolMode(tool).ordinal();
            signs = ExchangingModes.getIcons();
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

            GlStateManager.color(r, g, b, a);

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

//            if (mouseInSector)
//                radius -= highlight;
        }
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();

        for (int i = 0; i < nameData.size(); i++) {
            NameDisplayData data = nameData.get(i);
            int xp = data.getX();
            int yp = data.getY();

            String name;
            if (tool.getItem() instanceof GadgetBuilding)
                name = BuildingModes.values()[i].toString();
            else if (tool.getItem() instanceof GadgetExchanger)
                name = ExchangingModes.values()[i].toString();
            else
                name = GadgetCopyPaste.ToolMode.values()[i].toString();

            int xsp = xp - 4;
            int ysp = yp;
            int width = fontRenderer.getStringWidth(name);

            if (xsp < x)
                xsp -= width - 8;
            if (ysp < y)
                ysp -= 9;

            Color color = i == modeIndex ? Color.GREEN : Color.WHITE;
            if (data.isSelected())
                fontRenderer.drawStringWithShadow(name, xsp + (data.isCentralized() ? width / 2 - 4 : 0), ysp, color.getRGB());

            double mod = 0.7;
            int xdp = (int) ((xp - x) * mod + x);
            int ydp = (int) ((yp - y) * mod + y);

            mc.renderEngine.bindTexture(signs.get(i));
            GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
            mc.renderEngine.bindTexture(signs.get(i));
            drawModalRectWithCustomSizedTexture(xdp - 8, ydp - 8, 0, 0, 16, 16, 16, 16);

        }

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderHelper.enableGUIStandardItemLighting();

        float s = 2.25F * fract;
        GlStateManager.scale(s, s, s);
        GlStateManager.translate(x / s - (tool.getItem() instanceof GadgetCopyPaste ? 8F : 8.5F), y / s - 8, 0);
        mc.getRenderItem().renderItemAndEffectIntoGUI(tool, 0, 0);

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableBlend();
        GlStateManager.disableRescaleNormal();

        GlStateManager.popMatrix();
        renderHoverHelpText(mx, my);
    }

    private boolean isCursorInSlice(float angle, float totalDeg, float degPer, boolean inRange) {
        return inRange && angle > totalDeg && angle < totalDeg + degPer;
    }

    private void renderHoverHelpText(int mx, int my) {
        buttonList.forEach(button -> {
            if (!(button instanceof GuiButtonActionCallback))
                return;

            GuiButtonActionCallback helpTextProvider = (GuiButtonActionCallback) button;
            if (helpTextProvider.isHovered(mx, my)) {
                Color color = button instanceof GuiButtonSelect && ((GuiButtonSelect) button).isSelected() ? Color.GREEN : Color.WHITE;
                String text = helpTextProvider.getHoverHelpText();
                int x = helpTextProvider.getScreenPosition() == ScreenPosition.LEFT ? mx - fontRenderer.getStringWidth(text): mx;
                fontRenderer.drawStringWithShadow(text, x, my - fontRenderer.FONT_HEIGHT, color.getRGB());
            }
        });
    }

    private void changeMode() {
        if (slotSelected >= 0) {
            PacketHandler.INSTANCE.sendToServer(new PacketToggleMode(slotSelected));
            ModSounds.BEEP.playSound();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        changeMode();
    }

    @Override
    public void updateScreen() {
        if (!GameSettings.isKeyDown(KeyBindings.menuSettings)) {
            mc.displayGuiScreen(null);
            changeMode();
        }

        ImmutableSet<KeyBinding> set = ImmutableSet.of(mc.gameSettings.keyBindForward, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindSneak, mc.gameSettings.keyBindSprint, mc.gameSettings.keyBindJump);
        for (KeyBinding k : set)
            KeyBinding.setKeyBindState(k.getKeyCode(), GameSettings.isKeyDown(k));

        timeIn++;
        ItemStack tool = getGadget();
        boolean builder = tool.getItem() instanceof GadgetBuilding;
        if (!builder && !(tool.getItem() instanceof GadgetExchanger))
            return;

        boolean curent;
        boolean changed = false;
        for (int i = 0; i < conditionalButtons.size(); i++) {
            GuiButton button = conditionalButtons.get(i);
            if (builder)
                curent = GadgetBuilding.getToolMode(tool) == BuildingModes.Surface;
            else
                curent = i == 0 || GadgetExchanger.getToolMode(tool) == ExchangingModes.Surface;

            if (button.visible != curent) {
                button.visible = curent;
                changed = true;
            }
        }
        if (changed)
            updateButtons(tool);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private static float mouseAngle(int x, int y, int mx, int my) {
        Vector2f baseVec = new Vector2f(1F, 0F);
        Vector2f mouseVec = new Vector2f(mx - x, my - y);

        float ang = (float) (Math.acos(Vector2f.dot(baseVec, mouseVec) / (baseVec.length() * mouseVec.length())) * (180F / Math.PI));
        return my < y ? 360F - ang : ang;
    }

    public static enum ScreenPosition {
        RIGHT, LEFT, BOTTOM, TOP;
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

}
