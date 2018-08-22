package com.direwolf20.buildinggadgets.eventhandlers;

/**
 * This class was adapted from code written by Vazkii
 * Thanks Vazkii!!
 */

import com.direwolf20.buildinggadgets.items.CopyPasteTool;
import com.direwolf20.buildinggadgets.tools.BlockMap;
import com.direwolf20.buildinggadgets.tools.InventoryManipulation;
import com.direwolf20.buildinggadgets.tools.PasteToolBufferBuilder;
import com.direwolf20.buildinggadgets.tools.UniqueItem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class TooltipRender {

    private static final int STACKS_PER_LINE = 8;

    @SideOnly(Side.CLIENT)
    public static void tooltipIfShift(List<String> tooltip, Runnable r) {
        if (GuiScreen.isShiftKeyDown())
            r.run();
        //else addToTooltip(tooltip, "arl.misc.shiftForInfo");
    }

    @SubscribeEvent
    public static void onMakeTooltip(ItemTooltipEvent event) {
        //This method extends the tooltip box size to fit the item's we will render in onDrawTooltip
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack stack = event.getItemStack();
        String UUID = CopyPasteTool.getUUID(stack);
        List<String> tooltip = event.getToolTip();
        if (stack.getItem() instanceof CopyPasteTool) {
            Map<UniqueItem, Integer> itemCountMap = makeRequiredList(UUID);
            int count = itemCountMap.size();
            //boolean creative = ((IReagentHolder) stack.getItem()).isCreativeReagentHolder(stack);

            if (count > 0)
                tooltipIfShift(tooltip, () -> {
                    int lines = (((count - 1) / STACKS_PER_LINE) + 1) * 2;
                    int width = Math.min(STACKS_PER_LINE, count) * 18;
                    String spaces = "\u00a7r\u00a7r\u00a7r\u00a7r\u00a7r";
                    while (mc.fontRenderer.getStringWidth(spaces) < width)
                        spaces += " ";

                    for (int j = 0; j < lines; j++)
                        tooltip.add(spaces);
                });
        }
    }

    @SubscribeEvent
    public static void onDrawTooltip(RenderTooltipEvent.PostText event) {
        //This method will draw items on the tooltip
        ItemStack stack = event.getStack();
        String UUID = CopyPasteTool.getUUID(stack);
        if (stack.getItem() instanceof CopyPasteTool && GuiScreen.isShiftKeyDown()) {
            Map<UniqueItem, Integer> itemCountMap = makeRequiredList(UUID); //Create a new UniqueItem->Count map

            //Create an ItemStack -> Integer Map
            Map<ItemStack, Integer> itemStackCount = new HashMap<ItemStack, Integer>();
            for (Map.Entry<UniqueItem, Integer> entry : itemCountMap.entrySet()) {
                ItemStack itemStack = new ItemStack(entry.getKey().item, 1, entry.getKey().meta);
                itemStackCount.put(itemStack, entry.getValue());
            }
            // Sort the ItemStack -> Integer map, first by Required Items, then ItemID, then Meta
            List<Map.Entry<ItemStack, Integer>> list = new ArrayList<>(itemStackCount.entrySet());
            Comparator<Map.Entry<ItemStack, Integer>> comparator = Comparator.comparing(entry -> entry.getValue());
            comparator = comparator.reversed();
            comparator = comparator.thenComparing(Comparator.comparing(entry -> Item.getIdFromItem(entry.getKey().getItem())));
            comparator = comparator.thenComparing(Comparator.comparing(entry -> entry.getKey().getMetadata()));
            list.sort(comparator);

            int count = itemStackCount.size();

            int bx = event.getX();
            int by = event.getY();

            List<String> tooltip = event.getLines();
            int lines = (((count - 1) / STACKS_PER_LINE) + 1);
            int width = Math.min(STACKS_PER_LINE, count) * 18;
            int height = lines * 20 + 1;

            for (String s : tooltip) {
                if (s.trim().equals("\u00a77\u00a7r\u00a7r\u00a7r\u00a7r\u00a7r"))
                    break;
                else by += 10;
            }

            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            Gui.drawRect(bx, by, bx + width, by + height, 0x55000000);

            int j = 0;
            //Look through all the ItemStacks and draw each one in the specified X/Y position
            for (Map.Entry<ItemStack, Integer> entry : list) {
                int hasAmt = InventoryManipulation.countItem(entry.getKey(), Minecraft.getMinecraft().player);
                int x = bx + (j % STACKS_PER_LINE) * 18;
                int y = by + (j / STACKS_PER_LINE) * 20;
                renderRequiredBlocks(entry.getKey(), x, y, hasAmt, entry.getValue());
                j++;
            }
        }
    }

    private static void renderRequiredBlocks(ItemStack itemStack, int x, int y, int count, int req) {
        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.disableDepth();
        RenderItem render = mc.getRenderItem();

        net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
        render.renderItemIntoGUI(itemStack, x, y);

        //String s1 = count == Integer.MAX_VALUE ? "\u221E" : TextFormatting.BOLD + Integer.toString((int) ((float) req));
        String s1 = count == Integer.MAX_VALUE ? "\u221E" : Integer.toString((int) ((float) req));
        int w1 = mc.fontRenderer.getStringWidth(s1);
        int color = 0xFFFFFF;

        boolean hasReq = req > 0;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 8 - w1 / 4, y + (hasReq ? 12 : 14), 0);
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        mc.fontRenderer.drawStringWithShadow(s1, 0, 0, color);
        GlStateManager.popMatrix();

        if (hasReq) {
            //The commented out code will draw a red box around any items that you don't have enough of
            //I personally didn't like it.
            /*if (count < req) {
                GlStateManager.enableDepth();
                Gui.drawRect(x - 1, y - 1, x + 17, y + 17, 0x44FF0000);
                GlStateManager.disableDepth();
            }*/
            if (count < req) {
                String fs = Integer.toString(req - count);
                //String s2 = TextFormatting.BOLD + "(" + fs + ")";
                String s2 = "(" + fs + ")";
                int w2 = mc.fontRenderer.getStringWidth(s2);

                GlStateManager.pushMatrix();
                GlStateManager.translate(x + 8 - w2 / 4, y + 17, 0);
                GlStateManager.scale(0.5F, 0.5F, 0.5F);
                mc.fontRenderer.drawStringWithShadow(s2, 0, 0, 0xFF0000);
                GlStateManager.popMatrix();
            }
        }
        GlStateManager.enableDepth();
    }

    public static Map<UniqueItem, Integer> makeRequiredList(String UUID) {
        Map<UniqueItem, Integer> itemCountMap = new HashMap<UniqueItem, Integer>();
        Map<IBlockState, UniqueItem> IntStackMap = CopyPasteTool.getBlockMapIntState(PasteToolBufferBuilder.getTagFromUUID(UUID)).getIntStackMap();
        ArrayList<BlockMap> blockMapList = CopyPasteTool.getBlockMapList(PasteToolBufferBuilder.getTagFromUUID(UUID));
        for (BlockMap blockMap : blockMapList) {
            UniqueItem uniqueItem = IntStackMap.get(blockMap.state);
            if (uniqueItem.item != Items.AIR) {
                boolean found = false;
                for (Map.Entry<UniqueItem, Integer> entry : itemCountMap.entrySet()) {
                    if (entry.getKey().equals(uniqueItem)) {
                        itemCountMap.put(entry.getKey(), itemCountMap.get(entry.getKey()) + 1);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    itemCountMap.put(uniqueItem, 1);
                }
            }
        }
        return itemCountMap;
    }

}
