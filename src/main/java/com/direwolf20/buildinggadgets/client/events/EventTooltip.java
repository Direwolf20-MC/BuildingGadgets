package com.direwolf20.buildinggadgets.client.events;

import com.direwolf20.buildinggadgets.client.cache.RemoteInventoryCache;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.capability.CapabilityTemplate;
import com.direwolf20.buildinggadgets.common.tainted.inventory.IItemIndex;
import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.tainted.inventory.MatchResult;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.IUniqueObject;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.UniqueItem;
import com.direwolf20.buildinggadgets.common.items.OurItems;
import com.direwolf20.buildinggadgets.common.tainted.template.Template;
import com.direwolf20.buildinggadgets.common.tainted.template.TemplateHeader;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

import java.util.Comparator;
import java.util.List;

/**
 * This class was adapted from code written by Vazkii
 * Thanks Vazkii!!
 */
@Mod.EventBusSubscriber(modid = Reference.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class EventTooltip {
    private static final String PLACE_HOLDER = "\u00a77\u00a7r\u00a7r\u00a7r\u00a7r\u00a7r";
    private static final Comparator<Multiset.Entry<IUniqueObject<?>>> ENTRY_COMPARATOR = Comparator
            .<Multiset.Entry<IUniqueObject<?>>, Integer>comparing(Entry::getCount)
            .reversed()
            .thenComparing(e -> e.getElement().getObjectRegistryName());

    private static final int STACKS_PER_LINE = 8;
    private static RemoteInventoryCache cache = new RemoteInventoryCache(true);

    public static void setCache(Multiset<UniqueItem> cache) {
        EventTooltip.cache.setCache(cache);
    }

    public static void addTemplatePadding(ItemStack stack, List<ITextComponent> tooltip) {
        //This method extends the tooltip box size to fit the item's we will render in onDrawTooltip
        Minecraft mc = Minecraft.getInstance();
        if (mc.world == null || mc.player == null) //populateSearchTreeManager...
            return;

        mc.world.getCapability(CapabilityTemplate.TEMPLATE_PROVIDER_CAPABILITY).ifPresent(provider -> {
            stack.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).ifPresent(templateKey -> {
                Template template = provider.getTemplateForKey(templateKey);
                IItemIndex index = InventoryHelper.index(stack, mc.player);

                BuildContext buildContext = BuildContext.builder()
                        .stack(stack)
                        .player(mc.player)
                        .build(mc.world);

                TemplateHeader header = template.getHeaderAndForceMaterials(buildContext);
                MaterialList list = header.getRequiredItems();
                if (list == null)
                    list = MaterialList.empty();

                MatchResult match = index.tryMatch(list);
                int count = match.isSuccess() ? match.getChosenOption().entrySet().size() : match.getChosenOption().entrySet().size() + 1;
                if (count > 0 && Screen.hasShiftDown()) {
                    int lines = (((count - 1) / STACKS_PER_LINE) + 1) * 2;
                    int width = Math.min(STACKS_PER_LINE, count) * 18;
                    String spaces = PLACE_HOLDER;
                    while (mc.fontRenderer.getStringWidth(spaces) < width)
                        spaces += " ";

                    for (int j = 0; j < lines; j++)
                        tooltip.add(new StringTextComponent(spaces));
                }
            });
        });
    }

    @SubscribeEvent
    public static void onDrawTooltip(RenderTooltipEvent.PostText event) {
        if (!Screen.hasShiftDown())
            return;

        //This method will draw old_items on the tooltip
        ItemStack stack = event.getStack();
        Minecraft mc = Minecraft.getInstance();
        MatrixStack matrices = event.getMatrixStack();

        if( mc.world == null || mc.player == null )
            return;

        mc.world.getCapability(CapabilityTemplate.TEMPLATE_PROVIDER_CAPABILITY).ifPresent(provider -> {
            stack.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).ifPresent(templateKey -> {
                Template template = provider.getTemplateForKey(templateKey);
                IItemIndex index = InventoryHelper.index(stack, mc.player);
                BuildContext buildContext = BuildContext.builder()
                        .stack(stack)
                        .player(mc.player)
                        .build(mc.world);
                TemplateHeader header = template.getHeaderAndForceMaterials(buildContext);
                MaterialList list = header.getRequiredItems();
                if (list == null)
                    list = MaterialList.empty();

                MatchResult match = index.tryMatch(list);
                Multiset<IUniqueObject<?>> existing = match.getFoundItems();
                List<Multiset.Entry<IUniqueObject<?>>> sortedEntries = ImmutableList.sortedCopyOf(ENTRY_COMPARATOR, match.getChosenOption().entrySet());

                int bx = event.getX();
                int by = event.getY();
                int j = 0;
                int totalMissing = 0;
                List<? extends ITextProperties> tooltip = event.getLines();
                FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
                for (ITextProperties s : tooltip) {
                    if (s.getString().trim().equals(PLACE_HOLDER))
                        break;
                    by += fontRenderer.FONT_HEIGHT;
                }
                //add missing offset because the Stack is 16 by 16 as a render, not 9 by 9
                //needs to be 8 instead of 7, so that there is a one pixel padding to the text, just as there is between stacks
                by += 8;
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                for (Multiset.Entry<IUniqueObject<?>> entry : sortedEntries) {
                    int x = bx + (j % STACKS_PER_LINE) * 18;
                    int y = by + (j / STACKS_PER_LINE) * 20;
                    totalMissing += renderRequiredBlocks(matrices, entry.getElement().createStack(), x, y, existing.count(entry.getElement()), entry.getCount());
                    j++;
                }
                if (!match.isSuccess()) {
                    IUniqueObject<?> pasteItem = new UniqueItem(OurItems.CONSTRUCTION_PASTE_ITEM.get());
                    Multiset<IUniqueObject<?>> pasteSet = ImmutableMultiset.<IUniqueObject<?>>builder()
                            .addCopies(pasteItem, totalMissing)
                            .build();
                    int hasAmt = index.tryMatch(pasteSet).getFoundItems().count(pasteItem);
                    int x = bx + (j % STACKS_PER_LINE) * 18;
                    int y = by + (j / STACKS_PER_LINE) * 20;

                    int required = Integer.MAX_VALUE;
                    try {
                        required = Math.toIntExact(totalMissing);
                    } catch (ArithmeticException ignored) {}

                    renderRequiredBlocks(matrices, pasteItem.createStack(), x, y, hasAmt, required);
                }
            });
        });
    }

    private static int renderRequiredBlocks(MatrixStack matrices, ItemStack itemStack, int x, int y, int count, int req) {
        Minecraft mc = Minecraft.getInstance();
        ItemRenderer render = mc.getItemRenderer();

        String s1 = req == Integer.MAX_VALUE ? "\u221E" : Integer.toString(req);
        int w1 = mc.fontRenderer.getStringWidth(s1);

        boolean hasReq = req > 0;

        render.zLevel += 500f;
        render.renderItemAndEffectIntoGUI(mc.player, itemStack, x, y);
        render.renderItemOverlays(mc.fontRenderer, itemStack, x, y);
        render.zLevel -= 500f;

        matrices.push();
        matrices.translate(x + 8 - w1 / 4f, y + (hasReq ? 12 : 14), render.zLevel + 800.0F);
        matrices.scale(.5f, .5f, 0);
        IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
        mc.fontRenderer.renderString(s1, 0, 0, 0xFFFFFF, true, matrices.getLast().getMatrix(), irendertypebuffer$impl, false, 0, 15728880);
        matrices.pop();

        int missingCount = 0;
        if (hasReq) {
            if (count < req) {
                String fs = Integer.toString(req - count);
                String s2 = "(" + fs + ")";
                int w2 = mc.fontRenderer.getStringWidth(s2);

                matrices.push();
                matrices.translate(x + 8 - w2 / 4f, y + 17, render.zLevel + 800.0F);
                matrices.scale(.5f, .5f, 0);
                mc.fontRenderer.renderString(s2, 0, 0, 0xFF0000, true, matrices.getLast().getMatrix(), irendertypebuffer$impl, false, 0, 15728880);
                matrices.pop();

                missingCount = (req - count);
            }
        }

        irendertypebuffer$impl.finish();
        return missingCount;
    }
}
