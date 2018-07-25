/**
 * This class was adapted from code written by Vazkii for the PSI mod: https://github.com/Vazkii/Psi
 */

package com.direwolf20.buildinggadgets.GUIs;

import com.direwolf20.buildinggadgets.KeyBindings;
import com.direwolf20.buildinggadgets.KeyInputHandler;
import com.direwolf20.buildinggadgets.items.BuildingTool;
import com.direwolf20.buildinggadgets.items.ExchangerTool;
import com.direwolf20.buildinggadgets.items.GenericGadget;
import com.direwolf20.buildinggadgets.network.PacketHandler;
import com.direwolf20.buildinggadgets.network.PacketToggleMode;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ModeRadialMenu extends GuiScreen {

    private static final ResourceLocation[] signs = new ResourceLocation[]{
            new ResourceLocation("buildinggadgets:textures/items/buildingtool.png")
    };

    int timeIn = 0;
    int slotSelected = -1;

    //ItemStack controllerStack;
    //ItemStack[] controlledStacks;
    //int controlSlot;

    ItemStack itemStack;
    List<Integer> slots;

    BuildingTool buildingTool;
    ExchangerTool exchangerTool;

    public ModeRadialMenu(ItemStack stack) {
        mc = Minecraft.getMinecraft();

        //controllerStack = ItemStack.EMPTY;
        itemStack = ItemStack.EMPTY;

        if(stack.getItem() instanceof BuildingTool) {
            setSocketable(stack);
        }
        else if(stack.getItem() instanceof ExchangerTool) {
            setSocketable(stack);
        }
    }

    public void setSocketable(ItemStack stack) {
        slots = new ArrayList();
        if(stack.isEmpty())
            return;
        itemStack = stack;
        if (stack.getItem() instanceof BuildingTool) {
            for (int i = 0; i < BuildingTool.toolModes.values().length; i++)
                slots.add(i);
        }
        else if (stack.getItem() instanceof ExchangerTool) {
            for (int i = 0; i < ExchangerTool.toolModes.values().length; i++)
                slots.add(i);
        }
    }

    @Override
    public void drawScreen(int mx, int my, float partialTicks) {
        super.drawScreen(mx, my, partialTicks);

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();

        int x = width / 2;
        int y = height / 2;
        int maxRadius = 80;

        boolean mouseIn = true;
        float angle = mouseAngle(x, y, mx, my);

        int highlight = 5;

        GlStateManager.enableBlend();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        int segments = slots.size();
        float totalDeg = 0;
        float degPer = 360F / segments;

        List<int[]> stringPositions = new ArrayList();

        EntityPlayer player = Minecraft.getMinecraft().player;
        ItemStack tool = player.getHeldItemMainhand();
        if (!(tool.getItem() instanceof GenericGadget)) {
            tool = player.getHeldItemOffhand();
            if (!(tool.getItem() instanceof GenericGadget)) {
                return;
            }
        }
        slotSelected = -1;

        for(int seg = 0; seg < segments; seg++) {
            boolean mouseInSector = mouseIn && angle > totalDeg && angle < totalDeg + degPer;
            float radius = Math.max(0F, Math.min((timeIn + partialTicks - seg * 6F / segments) * 40F, maxRadius));

            GL11.glBegin(GL11.GL_TRIANGLE_FAN);

            float gs = 0.25F;
            if(seg % 2 == 0)
                gs += 0.1F;
            float r = gs;
            float g = gs;
            float b = gs;
            float a = 0.4F;
            if(mouseInSector) {
                slotSelected = seg;

                if(!tool.isEmpty()) {
                    Color color = new Color(255, 255, 255);
                    r = color.getRed() / 255F;
                    g = color.getGreen() / 255F;
                    b = color.getBlue() / 255F;
                }
            }

            GlStateManager.color(r, g, b, a);
            GL11.glVertex2i(x, y);

            for(float i = degPer; i >= 0; i--) {
                float rad = (float) ((i + totalDeg) / 180F * Math.PI);
                double xp = x + Math.cos(rad) * radius;
                double yp = y + Math.sin(rad) * radius;
                if((int)i == (int) (degPer / 2))
                    stringPositions.add(new int[] { seg, (int) xp, (int) yp, mouseInSector ? 'n' : 'r' });

                GL11.glVertex2d(xp, yp);
            }
            totalDeg += degPer;

            GL11.glVertex2i(x, y);
            GL11.glEnd();

            if(mouseInSector)
                radius -= highlight;
        }
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableTexture2D();

        for(int[] pos : stringPositions) {
            int slot = slots.get(pos[0]);
            int xp = pos[1];
            int yp = pos[2];
            char c = (char) pos[3];

            //ItemStack stack = socketable.getBulletInSocket(socketableStack, slot);
            BuildingTool.toolModes mode = BuildingTool.toolModes.values()[slot];
            //if() {
                int xsp = xp - 4;
                int ysp = yp;
                String name = "\u00a7" + c + mode.name();
                int width = fontRenderer.getStringWidth(name);

                double mod = 0.6;
                int xdp = (int) ((xp - x) * mod + x);
                int ydp = (int) ((yp - y) * mod + y);

                //mc.getRenderItem().renderItemIntoGUI(tool, xdp - 8, ydp - 8);

                if(xsp < x)
                    xsp -= width - 8;
                if(ysp < y)
                    ysp -= 9;

                fontRenderer.drawStringWithShadow(name, xsp, ysp, 0xFFFFFF);

                mod = 0.8;
                xdp = (int) ((xp - x) * mod + x);
                ydp = (int) ((yp - y) * mod + y);

                mc.renderEngine.bindTexture(signs[0]); //@Todo Inject Graphics from Rorax
                drawModalRectWithCustomSizedTexture(xdp - 8, ydp - 8, 0, 0, 16, 16, 16, 16);
            //}
        }

        float stime = 5F;
        float fract = Math.min(stime, timeIn + partialTicks) / stime;
        float s = 3F * fract;
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderHelper.enableGUIStandardItemLighting();

        /*if(controlledStacks != null && controlledStacks.length > 0) {
            int xs = width / 2 - 18 * controlledStacks.length / 2;
            int ys = height / 2;

            for(int i = 0; i < controlledStacks.length; i++) {
                float yoff = 25F + maxRadius;
                if(i == controlSlot)
                    yoff += 5F;

                GlStateManager.translate(0, -yoff * fract, 0F);
                mc.getRenderItem().renderItemAndEffectIntoGUI(controlledStacks[i], xs + i * 18, ys);
                GlStateManager.translate(0, yoff * fract, 0F);
            }

        }*/

        if(!tool.isEmpty()) {
            GlStateManager.scale(s, s, s);
            GlStateManager.translate(x / s - 8, y / s - 8, 0);
            mc.getRenderItem().renderItemAndEffectIntoGUI(tool, 0, 0);
        }
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableBlend();
        GlStateManager.disableRescaleNormal();

        GlStateManager.popMatrix();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        /*if(!controllerStack.isEmpty() && controlledStacks.length > 0) {
            if(mouseButton == 0) {
                controlSlot++;
                if(controlSlot >= controlledStacks.length)
                    controlSlot = 0;
            } else if(mouseButton == 1) {
                controlSlot--;
                if(controlSlot < 0)
                    controlSlot = controlledStacks.length - 1;
            }

            setSocketable(controlledStacks[controlSlot]);
        }*/
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        if(!isKeyDown(KeyBindings.modeSwitch)) {
            mc.displayGuiScreen(null);
            if(slotSelected != -1) {
                int slot = slots.get(slotSelected);
                PacketHandler.INSTANCE.sendToServer(new PacketToggleMode(slotSelected));
                /*NetworkMessage message = null;
                if(!controllerStack.isEmpty())
                    message = new MessageChangeControllerSlot(controlSlot, slot);
                else message = new MessageChangeSocketableSlot(slot);
                NetworkHandler.INSTANCE.sendToServer(message);*/
            }
        }

        ImmutableSet<KeyBinding> set = ImmutableSet.of(mc.gameSettings.keyBindForward, mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindBack, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindSneak, mc.gameSettings.keyBindSprint, mc.gameSettings.keyBindJump);
        for(KeyBinding k : set)
            KeyBinding.setKeyBindState(k.getKeyCode(), isKeyDown(k));

        timeIn++;
    }

    public boolean isKeyDown(KeyBinding keybind) {
        int key = keybind.getKeyCode();
        if(key < 0) {
            int button = 100 + key;
            return Mouse.isButtonDown(button);
        }
        return Keyboard.isKeyDown(key);
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
}
