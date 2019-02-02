package com.direwolf20.buildinggadgets.client.events;

import com.direwolf20.buildinggadgets.client.RemoteInventoryCache;

/**
 * This class was adapted from code written by Vazkii
 * Thanks Vazkii!!
 */

import com.direwolf20.buildinggadgets.common.BuildingObjects;
import com.direwolf20.buildinggadgets.common.items.ITemplate;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.tools.BlockMap;
import com.direwolf20.buildinggadgets.common.tools.InventoryManipulation;
import com.direwolf20.buildinggadgets.common.tools.PasteToolBufferBuilder;
import com.direwolf20.buildinggadgets.common.tools.UniqueItem;
import com.google.common.collect.Multiset;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.lwjgl.opengl.GL11;

import java.util.*;

@EventBusSubscriber(Dist.CLIENT)
public class EventTooltip {

    private static final int STACKS_PER_LINE = 8;
    private static RemoteInventoryCache cache = new RemoteInventoryCache(true);

    public static void setCache(Multiset<UniqueItem> cache) {
        EventTooltip.cache.setCache(cache);
    }

    @SideOnly(Side.CLIENT)
    private static void tooltipIfShift(@SuppressWarnings("unused") List<String> tooltip, Runnable r) {
        if (GuiScreen.isShiftKeyDown())
            r.run();
    }

    public static void addTemplatePadding(ItemStack stack, List<ITextComponent> tooltip) {
        //This method extends the tooltip box size to fit the item's we will render in onDrawTooltip
        Minecraft mc = Minecraft.getInstance();
        if (stack.getItem() instanceof ITemplate) {
            ITemplate template = (ITemplate) stack.getItem();
            String UUID = template.getUUID(stack);
            if (UUID == null) return;

            Multiset<UniqueItem> itemCountMap = template.getItemCountMap(stack);

            Map<ItemStack, Integer> itemStackCount = new HashMap<>();
            for (Multiset.Entry<UniqueItem> entry : itemCountMap.entrySet()) {
                ItemStack itemStack = new ItemStack(entry.getElement().item, 1);
                itemStackCount.put(itemStack, entry.getCount());
            }
            List<Map.Entry<ItemStack, Integer>> list = new ArrayList<>(itemStackCount.entrySet());

            int totalMissing = 0;
            //Look through all the ItemStacks and draw each one in the specified X/Y position
            for (Map.Entry<ItemStack, Integer> entry : list) {
                int hasAmt = InventoryManipulation.countItem(entry.getKey(), Minecraft.getMinecraft().player, cache);
                if (hasAmt < entry.getValue())
                    totalMissing = totalMissing + Math.abs(entry.getValue() - hasAmt);
            }

            int count = (totalMissing > 0) ? itemCountMap.size() + 1 : itemStackCount.size();
            if (count > 0)
                tooltipIfShift(tooltip, () -> {
                    int lines = (((count - 1) / STACKS_PER_LINE) + 1) * 2;
                    int width = Math.min(STACKS_PER_LINE, count) * 18;
                    String spaces = "\u00a7r\u00a7r\u00a7r\u00a7r\u00a7r";
                    while (mc.fontRenderer.getStringWidth(spaces) < width)
                        spaces += " ";

                    for (int j = 0; j < lines; j++)
                        tooltip.add(new TextComponentString(spaces));
                });
        }
    }

    @SubscribeEvent
    public static void onDrawTooltip(RenderTooltipEvent.PostText event) {
        //This method will draw items on the tooltip
        ItemStack stack = event.getStack();

        if ((stack.getItem() instanceof ITemplate) && GuiScreen.isShiftKeyDown()) {
            int totalMissing = 0;
            Multiset<UniqueItem> itemCountMap = ((ITemplate) stack.getItem()).getItemCountMap(stack);

            //Create an ItemStack -> Integer Map
            Map<ItemStack, Integer> itemStackCount = new HashMap<>();
            for (Multiset.Entry<UniqueItem> entry : itemCountMap.entrySet()) {
                ItemStack itemStack = new ItemStack(entry.getElement().item, 1);
                itemStackCount.put(itemStack, entry.getCount());
            }
            // Sort the ItemStack -> Integer map, first by Required Items, then ItemID, then Meta
            List<Map.Entry<ItemStack, Integer>> list = new ArrayList<>(itemStackCount.entrySet());
            Comparator<Map.Entry<ItemStack, Integer>> comparator = Comparator.comparing(entry -> entry.getValue());
            comparator = comparator.reversed();
            comparator = comparator.thenComparing(Comparator.comparing(entry -> Item.getIdFromItem(entry.getKey().getItem())));
            list.sort(comparator);

//            int count = itemStackCount.size();

            int bx = event.getX();
            int by = event.getY();

            List<String> tooltip = event.getLines();
//            int lines = (((count - 1) / STACKS_PER_LINE) + 1);
//            int width = Math.min(STACKS_PER_LINE, count) * 18;
//            int height = lines * 20 + 1;

            for (String s : tooltip) {
                if (s.trim().equals("\u00a77\u00a7r\u00a7r\u00a7r\u00a7r\u00a7r"))
                    break;
                by += 10;
            }

            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            //Gui.drawRect(bx, by, bx + width, by + height, 0x55000000);

            int j = 0;
            //Look through all the ItemStacks and draw each one in the specified X/Y position
            for (Map.Entry<ItemStack, Integer> entry : list) {
                int hasAmt = InventoryManipulation.countItem(entry.getKey(), Minecraft.getMinecraft().player, cache);
                int x = bx + (j % STACKS_PER_LINE) * 18;
                int y = by + (j / STACKS_PER_LINE) * 20;
                totalMissing = totalMissing + renderRequiredBlocks(entry.getKey(), x, y, hasAmt, entry.getValue());
                j++;
            }
            if (totalMissing > 0) {
                ItemStack pasteItemStack = new ItemStack(BuildingObjects.constructionPaste);
                int hasAmt = InventoryManipulation.countPaste(Minecraft.getInstance().player);
                int x = bx + (j % STACKS_PER_LINE) * 18;
                int y = by + (j / STACKS_PER_LINE) * 20;
                renderRequiredBlocks(pasteItemStack, x, y, hasAmt, totalMissing);
                j++;
            }
        }
    }

    private static int renderRequiredBlocks(ItemStack itemStack, int x, int y, int count, int req) {
        Minecraft mc = Minecraft.getInstance();
        GlStateManager.disableDepthTest();
        ItemRenderer render = mc.getItemRenderer();

        net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
        render.renderItemIntoGUI(itemStack, x, y);

        //String s1 = count == Integer.MAX_VALUE ? "\u221E" : TextFormatting.BOLD + Integer.toString((int) ((float) req));
        String s1 = count == Integer.MAX_VALUE ? "\u221E" : Integer.toString(req);
        int w1 = mc.fontRenderer.getStringWidth(s1);
        int color = 0xFFFFFF;

        boolean hasReq = req > 0;

        GlStateManager.pushMatrix();
        GlStateManager.translatef(x + 8 - w1 / 4, y + (hasReq ? 12 : 14), 0);
        GlStateManager.scalef(0.5F, 0.5F, 0.5F);
        mc.fontRenderer.drawStringWithShadow(s1, 0, 0, color);
        GlStateManager.popMatrix();

        int missingCount = 0;

        if (hasReq) {
            if (count < req) {
                String fs = Integer.toString(req - count);
                String s2 = "(" + fs + ")";
                int w2 = mc.fontRenderer.getStringWidth(s2);

                GlStateManager.pushMatrix();
                GlStateManager.translatef(x + 8 - w2 / 4, y + 17, 0);
                GlStateManager.scalef(0.5F, 0.5F, 0.5F);
                mc.fontRenderer.drawStringWithShadow(s2, 0, 0, 0xFF0000);
                GlStateManager.popMatrix();
                missingCount = (req - count);
            }
        }
        GlStateManager.enableDepthTest();
        return missingCount;
    }
}
