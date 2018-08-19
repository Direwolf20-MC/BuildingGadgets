package com.direwolf20.buildinggadgets.eventhandlers;

import com.direwolf20.buildinggadgets.items.CopyPasteTool;
import com.direwolf20.buildinggadgets.tools.BlockMap;
import com.direwolf20.buildinggadgets.tools.InventoryManipulation;
import com.direwolf20.buildinggadgets.tools.UniqueItem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TooltipRender {

    private static final int STACKS_PER_LINE = 8;

    private static Map<UniqueItem, Integer> itemCountMapCache = new HashMap<UniqueItem, Integer>();
    private static Map<ItemStack, Integer> itemStackCountCache = new HashMap<ItemStack, Integer>();

    @SideOnly(Side.CLIENT)
    public static void tooltipIfShift(List<String> tooltip, Runnable r) {
        if (GuiScreen.isShiftKeyDown())
            r.run();
        //else addToTooltip(tooltip, "arl.misc.shiftForInfo");
    }

    @SubscribeEvent
    public static void onMakeTooltip(ItemTooltipEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack stack = event.getItemStack();
        List<String> tooltip = event.getToolTip();
        if (stack.getItem() instanceof CopyPasteTool) {
            //ReagentList list = ReagentHandler.getReagents(stack);
            /*Map<UniqueItem, Integer> itemCountMap = makeRequiredList(stack);
            if (!itemCountMap.equals(itemCountMapCache)) {
                itemCountMapCache = itemCountMap;
                itemStackCountCache.clear();
                for (Map.Entry<UniqueItem, Integer> entry : itemCountMapCache.entrySet()) {
                    ItemStack itemStack = new ItemStack(entry.getKey().item, 1, entry.getKey().meta);
                    itemStackCountCache.put(itemStack, entry.getValue());
                }
            }*/
            if (itemStackCountCache.isEmpty()) {
                makeRequiredList(stack);
            }
            int count = itemStackCountCache.size();
            //boolean creative = ((IReagentHolder) stack.getItem()).isCreativeReagentHolder(stack);

            if (count > 0)
                tooltipIfShift(tooltip, () -> {
                    int lines = (((count - 1) / STACKS_PER_LINE) + 1) * 2;
                    int width = Math.min(STACKS_PER_LINE, count) * 18;
                    String spaces = "\u00a7r\u00a7r\u00a7r\u00a7r\u00a7r";
                    while (mc.fontRenderer.getStringWidth(spaces) < width)
                        spaces += " ";

                    for (int i = 0; i < lines; i++)
                        tooltip.add(spaces);
                });
        }
    }

    @SubscribeEvent
    public static void onDrawTooltip(RenderTooltipEvent.PostText event) {
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack stack = event.getStack();
        if (stack.getItem() instanceof CopyPasteTool && GuiScreen.isShiftKeyDown()) {
            /*Map<UniqueItem, Integer> itemCountMap = makeRequiredList(stack);
            if (!itemCountMap.equals(itemCountMapCache)) {
                itemCountMapCache = itemCountMap;
                itemStackCountCache.clear();
                for (Map.Entry<UniqueItem, Integer> entry : itemCountMapCache.entrySet()) {
                    ItemStack itemStack = new ItemStack(entry.getKey().item, 1, entry.getKey().meta);
                    itemStackCountCache.put(itemStack, entry.getValue());
                }
            }*/
            if (itemStackCountCache.isEmpty()) {
                makeRequiredList(stack);
            }
            int count = itemStackCountCache.size();

            //List<ReagentStack> stacks = new ArrayList(list.stacks);
            //Collections.sort(stacks);

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

            //for(int i = 0; i < stacks.size(); i++) {
            int i = 0;
            for (Map.Entry<ItemStack, Integer> entry : itemStackCountCache.entrySet()) {
                i++;
                int hasAmt = InventoryManipulation.countItem(entry.getKey(), Minecraft.getMinecraft().player);
                //ReagentStack rstack = stacks.get(i);
                int x = bx + (i % STACKS_PER_LINE) * 18;
                int y = by + (i / STACKS_PER_LINE) * 20;
                renderRequiredBlocks(entry.getKey(), x, y, hasAmt, entry.getValue());
            }
        }
    }

    //private static void renderRequiredBlocks(ReagentStack rstack, int x, int y, int count, int req) {
    private static void renderRequiredBlocks(ItemStack itemStack, int x, int y, int count, int req) {
        Minecraft mc = Minecraft.getMinecraft();
        GlStateManager.disableDepth();
        RenderItem render = mc.getRenderItem();

        net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
        render.renderItemIntoGUI(itemStack, x, y);

        //if(count == -1)
        //    count = rstack.trueCount;

        String s1 = count == Integer.MAX_VALUE ? "\u221E" : TextFormatting.BOLD + Integer.toString((int) ((float) count));
        int w1 = mc.fontRenderer.getStringWidth(s1);
        int color = 0xFFFFFF;
        if (count < req)
            color = 0xFF0000;

        boolean hasReq = req > 0;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 8 - w1 / 4, y + (hasReq ? 12 : 14), 0);
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        mc.fontRenderer.drawStringWithShadow(s1, 0, 0, color);
        GlStateManager.popMatrix();

        if (hasReq) {
            if (count < req) {
                GlStateManager.enableDepth();
                Gui.drawRect(x - 1, y - 1, x + 17, y + 17, 0x44FF0000);
                GlStateManager.disableDepth();
            }

            //float f = (float) req / ReagentList.DEFAULT_MULTIPLICATION_FACTOR;
            float f = (float) req;
            String fs = (f - (int) f) == 0 ? Integer.toString((int) f) : Float.toString(f);
            String s2 = TextFormatting.BOLD + "(" + fs + ")";
            int w2 = mc.fontRenderer.getStringWidth(s2);

            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 8 - w2 / 4, y + 17, 0);
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            mc.fontRenderer.drawStringWithShadow(s2, 0, 0, 0x999999);
            GlStateManager.popMatrix();
        }
        GlStateManager.enableDepth();
    }

    public static void makeRequiredList(ItemStack stack) {
        //Map<UniqueItem, Integer> itemCountMap = new HashMap<UniqueItem, Integer>();
        Map<IBlockState, UniqueItem> IntStackMap = CopyPasteTool.getBlockMapIntState(stack).getIntStackMap();
        ArrayList<BlockMap> blockMapList = CopyPasteTool.getBlockMapList(stack, CopyPasteTool.getStartPos(stack));
        Map<UniqueItem, String> stackNames = new HashMap<UniqueItem, String>();
        //Map<String, Integer> stringCount = new HashMap<String, Integer>();
        for (BlockMap blockMap : blockMapList) {
            UniqueItem uniqueItem = IntStackMap.get(blockMap.state);
            ItemStack itemStack = new ItemStack(uniqueItem.item, 1, uniqueItem.meta);
            Item item = uniqueItem.item;
            String name = "";
            String domain = item.getRegistryName().getResourceDomain();
            if (domain.equals("chisel")) {
                List<String> extendedName = itemStack.getTooltip(Minecraft.getMinecraft().player, ITooltipFlag.TooltipFlags.NORMAL);
                if (extendedName.size() > 1) {
                    name = extendedName.get(1);
                } else {
                    name = itemStack.getDisplayName();
                }
            } else {
                name = itemStack.getDisplayName();
            }
            if (name != "Air") {
                boolean found = false;
                for (Map.Entry<UniqueItem, Integer> entry : itemCountMapCache.entrySet()) {
                    //if (entry.getKey().item == uniqueItem.item && entry.getKey().meta == uniqueItem.meta) {
                    if (entry.getKey().equals(uniqueItem)) {
                        itemCountMapCache.put(entry.getKey(), itemCountMapCache.get(entry.getKey()) + 1);
                        //stringCount.put(name, stringCount.get(name) + 1);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    itemCountMapCache.put(uniqueItem, 1);
                    stackNames.put(uniqueItem, name);
                    //stringCount.put(name, 1);
                }
            }
        }
        for (Map.Entry<UniqueItem, Integer> entry : itemCountMapCache.entrySet()) {
            ItemStack itemStack = new ItemStack(entry.getKey().item, 1, entry.getKey().meta);
            itemStackCountCache.put(itemStack, entry.getValue());
        }
        //return itemCountMapCache;
        /*Map<String, Integer> hasMap = new HashMap<String, Integer>();
        for (Map.Entry<UniqueItem, Integer> entry : itemCountMap.entrySet()) {
            ItemStack itemStack = new ItemStack(entry.getKey().item, 1, entry.getKey().meta);
            int itemCount = InventoryManipulation.countItem(itemStack, Minecraft.getMinecraft().player);
            hasMap.put(stackNames.get(entry.getKey()), itemCount);
        }
        int totalMissing = 0;
        for (Map.Entry<String, Integer> entry : stringCount.entrySet()) {
            int itemCount = hasMap.get(entry.getKey());
            if (itemCount >= entry.getValue()) {
                //list.add(TextFormatting.GREEN + net.minecraft.client.resources.I18n.format(entry.getKey() + ": " + entry.getValue()));
            } else {
                //list.add(TextFormatting.RED + net.minecraft.client.resources.I18n.format(entry.getKey() + ": " + entry.getValue() + " (Missing: " + (entry.getValue() - itemCount) + ")"));
                totalMissing = totalMissing + (entry.getValue() - itemCount);
            }
        }
        if (totalMissing > 0) {
            //list.add(TextFormatting.AQUA + net.minecraft.client.resources.I18n.format("message.gadget.pasterequired" + ": " + totalMissing));
        }*/
    }

}
