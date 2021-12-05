package com.direwolf20.buildinggadgets.client.events;

import com.direwolf20.buildinggadgets.client.cache.RemoteInventoryCache;
import com.direwolf20.buildinggadgets.common.capability.CapabilityTemplate;
import com.direwolf20.buildinggadgets.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.OurItems;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.tainted.inventory.IItemIndex;
import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.tainted.inventory.MatchResult;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.IUniqueObject;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.UniqueItem;
import com.direwolf20.buildinggadgets.common.tainted.template.Template;
import com.direwolf20.buildinggadgets.common.tainted.template.TemplateHeader;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

import java.util.Comparator;
import java.util.List;

@Mod.EventBusSubscriber(modid = Reference.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class EventTooltip {
    private static final Comparator<Multiset.Entry<IUniqueObject<?>>> ENTRY_COMPARATOR = Comparator
            .<Multiset.Entry<IUniqueObject<?>>, Integer>comparing(Entry::getCount)
            .reversed()
            .thenComparing(e -> e.getElement().getObjectRegistryName());

    private static final int STACKS_PER_LINE = 16;
    private static RemoteInventoryCache cache = new RemoteInventoryCache(true);

    public static void setCache(Multiset<UniqueItem> cache) {
        EventTooltip.cache.setCache(cache);
    }

    public static class CopyPasteTooltipComponent implements ClientTooltipComponent {
        Data tooltipData;

        public CopyPasteTooltipComponent(Data tooltipComponent) {
            tooltipData = tooltipComponent;
        }

        @Override
        public int getHeight() {
            return Screen.hasShiftDown() && tooltipData.data != null ? 20 + (tooltipData.data.sortedEntries.size() / STACKS_PER_LINE * 20) : 0;
        }

        @Override
        public int getWidth(Font font) {
            return Screen.hasShiftDown() && tooltipData.data != null ? (tooltipData.data.sortedEntries.size() <= STACKS_PER_LINE ? tooltipData.data.sortedEntries.size() * 18 : STACKS_PER_LINE * 18) : 0;
        }

        @Override
        public void renderImage(Font font, int x, int y, PoseStack poseStack, ItemRenderer itemRenderer, int p_194053_) {
            if (this.tooltipData.stack == null || !(this.tooltipData.stack.getItem() instanceof GadgetCopyPaste))
                return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null || !Screen.hasShiftDown() || tooltipData.data == null)
                return;


            int bx = x;
            int by = y;
            int j = 0;
            int totalMissing = 0;

            //add missing offset because the Stack is 16 by 16 as a render, not 9 by 9
            //needs to be 8 instead of 7, so that there is a one pixel padding to the text, just as there is between stacks
//            by += 8;
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            for (Multiset.Entry<IUniqueObject<?>> entry : tooltipData.data.sortedEntries) {
                int xx = bx + (j % STACKS_PER_LINE) * 18;
                int yy = by + (j / STACKS_PER_LINE) * 20;
                totalMissing += renderRequiredBlocks(poseStack, itemRenderer, entry.getElement().createStack(), xx, yy, tooltipData.data.existing.count(entry.getElement()), entry.getCount());
                j++;
            }

            if (!tooltipData.data.match.isSuccess()) {
                IUniqueObject<?> pasteItem = new UniqueItem(OurItems.CONSTRUCTION_PASTE_ITEM.get());
                Multiset<IUniqueObject<?>> pasteSet = ImmutableMultiset.<IUniqueObject<?>>builder()
                        .addCopies(pasteItem, totalMissing)
                        .build();

                int hasAmt = tooltipData.data.index.tryMatch(pasteSet).getFoundItems().count(pasteItem);
                int xx = bx + (j % STACKS_PER_LINE) * 18;
                int yy = by + (j / STACKS_PER_LINE) * 20;

                int required = Integer.MAX_VALUE;
                try {
                    required = Math.toIntExact(totalMissing);
                } catch (ArithmeticException ignored) {
                }

                renderRequiredBlocks(poseStack, itemRenderer, pasteItem.createStack(), xx, yy, hasAmt, required);
            }
        }

        public static class Data implements TooltipComponent {
            public ItemStack stack;
            public TemplateData data;

            public Data(ItemStack stack) {
                this.stack = stack;

                Minecraft mc = Minecraft.getInstance();
                mc.level.getCapability(CapabilityTemplate.TEMPLATE_PROVIDER_CAPABILITY).ifPresent(provider -> stack.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).ifPresent(templateKey -> {
                    Template template = provider.getTemplateForKey(templateKey);

                    IItemIndex index = InventoryHelper.index(stack, mc.player);
                    BuildContext buildContext = BuildContext.builder()
                            .stack(stack)
                            .player(mc.player)
                            .build(mc.level);

                    TemplateHeader header = template.getHeaderAndForceMaterials(buildContext);
                    MaterialList list = header.getRequiredItems();
                    if (list == null)
                        list = MaterialList.empty();

                    MatchResult match = index.tryMatch(list);
                    Multiset<IUniqueObject<?>> existing = match.getFoundItems();
                    List<Entry<IUniqueObject<?>>> sortedEntries = ImmutableList.sortedCopyOf(ENTRY_COMPARATOR, match.getChosenOption().entrySet());

                    this.data = new TemplateData(existing, sortedEntries, index, match);
                }));
            }
        }

        public record TemplateData(Multiset<IUniqueObject<?>> existing,
                                   List<Entry<IUniqueObject<?>>> sortedEntries,
                                   IItemIndex index,
                                   MatchResult match) {
        }
    }

    private static int renderRequiredBlocks(PoseStack matrices, ItemRenderer itemRenderer, ItemStack itemStack, int x, int y, int count, int req) {
        Minecraft mc = Minecraft.getInstance();

        String s1 = req == Integer.MAX_VALUE ? "\u221E" : Integer.toString(req);
        int w1 = mc.font.width(s1);

        boolean hasReq = req > 0;

        itemRenderer.renderAndDecorateItem(itemStack, x, y);
        itemRenderer.renderGuiItemDecorations(mc.font, itemStack, x, y);

        MultiBufferSource.BufferSource irendertypebuffer$impl = Minecraft.getInstance().renderBuffers().bufferSource();

        matrices.pushPose();
        matrices.translate(x + 8 - w1 / 4f, y + (hasReq ? 12 : 14), itemRenderer.blitOffset + 250);
        matrices.scale(.5f, .5f, 0);
        mc.font.draw(matrices, s1, 0, 0, 0xFFFFFF);
        matrices.popPose();

        int missingCount = 0;
        if (hasReq) {

            if (count < req) {
                String fs = Integer.toString(req - count);
                String s2 = "(" + fs + ")";
                int w2 = mc.font.width(s2);

                matrices.pushPose();
                matrices.translate(x + 8 - w2 / 4f, y + 17, itemRenderer.blitOffset + 250);
                matrices.scale(.5f, .5f, 0);
                mc.font.drawInBatch(s2, 0, 0, 0xFF0000, true, matrices.last().pose(), irendertypebuffer$impl, false, 0, 15728880);
                matrices.popPose();

                missingCount = (req - count);
            }
        }

        irendertypebuffer$impl.endBatch();

        return missingCount;
    }
}
