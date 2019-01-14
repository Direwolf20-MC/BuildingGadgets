package com.direwolf20.buildinggadgets.common.blocks.templatemanager;

import com.direwolf20.buildinggadgets.api.*;
import com.direwolf20.buildinggadgets.common.items.ModItems;
import com.direwolf20.buildinggadgets.common.items.Template;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.network.PacketBlockMap;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import com.direwolf20.buildinggadgets.common.tools.PasteToolBufferBuilder;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.minecraft.client.gui.GuiScreen.setClipboardString;

public class TemplateManagerCommands {
    private static final Set<Item> allowedItemsRight = Stream.of(Items.PAPER, ModItems.template).collect(Collectors.toSet());

    public static void loadTemplate(TemplateManagerContainer container, EntityPlayer player) {
        ItemStack itemStack0 = container.getSlot(0).getStack();
        ItemStack itemStack1 = container.getSlot(1).getStack();
        if (!(itemStack0.getItem() instanceof ITemplateOld) || !(allowedItemsRight.contains(itemStack1.getItem()))) {
            return;
        }
        ITemplateOld template = (ITemplateOld) itemStack0.getItem();
        if (itemStack1.getItem().equals(Items.PAPER)) return;
        World world = player.world;

        BlockPos startPos = template.getStartPos(itemStack1);
        BlockPos endPos = template.getEndPos(itemStack1);
        Multiset<UniqueItem> tagMap = template.getItemCountMap(itemStack1);
        UUID uuidTemplate = ModItems.template.getUUID(itemStack1);
        if (uuidTemplate == null) return;

        WorldSave worldSave = WorldSave.getWorldSave(world);
        WorldSave templateWorldSave = WorldSave.getTemplateWorldSave(world);
        NBTTagCompound tagCompound;

        template.setStartPos(itemStack0, startPos);
        template.setEndPos(itemStack0, endPos);
        template.setItemCountMap(itemStack0, tagMap);
        UUID uuid = template.getUUID(itemStack0);

        if (uuid == null) return;

        NBTTagCompound templateTagCompound = templateWorldSave.getCompoundFromUUID(uuidTemplate);
        tagCompound = templateTagCompound.copy();
        template.incrementCopyCounter(itemStack0);
        tagCompound.setInteger("copycounter", template.getCopyCounter(itemStack0));
        tagCompound.setUniqueId("UUID", template.getUUID(itemStack0));
        tagCompound.setString("owner", player.getName());
        if (template.equals(ModItems.gadgetCopyPaste)) {
            worldSave.addToMap(uuid, tagCompound);
        } else {
            templateWorldSave.addToMap(uuid, tagCompound);
            Template.setName(itemStack0, Template.getName(itemStack1));
        }
        container.putStackInSlot(0, itemStack0);
        PacketHandler.INSTANCE.sendTo(new PacketBlockMap(tagCompound), (EntityPlayerMP) player);
    }

    public static void saveTemplate(TemplateManagerContainer container, EntityPlayer player, String templateName) {
        ItemStack itemStack0 = container.getSlot(0).getStack();
        ItemStack itemStack1 = container.getSlot(1).getStack();

        if (itemStack0.isEmpty() && itemStack1.getItem() instanceof Template && !templateName.isEmpty()) {
            Template.setName(itemStack1, templateName);
            container.putStackInSlot(1, itemStack1);
            return;
        }

        if (!(itemStack0.getItem() instanceof ITemplateOld) || !(allowedItemsRight.contains(itemStack1.getItem()))) {
            return;
        }
        ITemplateOld template = (ITemplateOld) itemStack0.getItem();
        World world = player.world;
        ItemStack templateStack;
        if (itemStack1.getItem().equals(Items.PAPER)) {
            templateStack = new ItemStack(ModItems.template, 1);
            container.putStackInSlot(1, templateStack);
        }
        if (!(container.getSlot(1).getStack().getItem().equals(ModItems.template))) return;
        templateStack = container.getSlot(1).getStack();
        WorldSave worldSave = WorldSave.getWorldSave(world);
        WorldSave templateWorldSave = WorldSave.getTemplateWorldSave(world);
        NBTTagCompound templateTagCompound;

        UUID uuid = template.getUUID(itemStack0);
        UUID uuidTemplate = ModItems.template.getUUID(templateStack);
        if (uuid == null || uuidTemplate == null) return;

        boolean isTool = itemStack0.getItem().equals(ModItems.gadgetCopyPaste);
        NBTTagCompound tagCompound = isTool ? worldSave.getCompoundFromUUID(uuid) : templateWorldSave.getCompoundFromUUID(uuid);
        templateTagCompound = tagCompound.copy();
        template.incrementCopyCounter(templateStack);
        templateTagCompound.setInteger("copycounter", template.getCopyCounter(templateStack));
        ITemplateOld.setUUID(ModItems.template.getUUID(templateStack),templateTagCompound);

        templateWorldSave.addToMap(uuidTemplate, templateTagCompound);
        BlockPos startPos = template.getStartPos(itemStack0);
        BlockPos endPos = template.getEndPos(itemStack0);
        Multiset<UniqueItem> tagMap = template.getItemCountMap(itemStack0);
        template.setStartPos(templateStack, startPos);
        template.setEndPos(templateStack, endPos);
        template.setItemCountMap(templateStack, tagMap);
        if (isTool) {
            Template.setName(templateStack, templateName);
        } else {
            if (templateName.equals("")) {
                Template.setName(templateStack, Template.getName(itemStack0));
            } else {
                Template.setName(templateStack, templateName);
            }
        }
        container.putStackInSlot(1, templateStack);
        PacketHandler.INSTANCE.sendTo(new PacketBlockMap(templateTagCompound), (EntityPlayerMP) player);
    }

    public static void pasteTemplate(TemplateManagerContainer container, EntityPlayer player, NBTTagCompound sentTagCompound, String templateName) {
        ItemStack itemStack1 = container.getSlot(1).getStack();

        if (!(allowedItemsRight.contains(itemStack1.getItem()))) {
            return;
        }

        World world = player.world;
        ItemStack templateStack;
        if (itemStack1.getItem().equals(Items.PAPER)) {
            templateStack = new ItemStack(ModItems.template, 1);
            container.putStackInSlot(1, templateStack);
        }
        if (!(container.getSlot(1).getStack().getItem().equals(ModItems.template))) return;
        templateStack = container.getSlot(1).getStack();

        WorldSave templateWorldSave = WorldSave.getTemplateWorldSave(world);
        Template template = ModItems.template;
        UUID uuidTemplate = template.getUUID(templateStack);
        if (uuidTemplate == null) return;

        NBTTagCompound templateTagCompound;

        templateTagCompound = sentTagCompound.copy();
        BlockPos startPos = GadgetUtils.getPOSFromNBT(templateTagCompound, "startPos");
        BlockPos endPos = GadgetUtils.getPOSFromNBT(templateTagCompound, "endPos");
        template.incrementCopyCounter(templateStack);
        templateTagCompound.setInteger("copycounter", template.getCopyCounter(templateStack));
        ITemplateOld.setUUID(template.getUUID(templateStack),templateTagCompound);
        //GadgetUtils.writePOSToNBT(templateTagCompound, startPos, "startPos", 0);
        //GadgetUtils.writePOSToNBT(templateTagCompound, endPos, "startPos", 0);
        //Map<UniqueItem, Integer> tagMap = GadgetUtils.nbtToItemCount((NBTTagList) templateTagCompound.getTag("itemcountmap"));
        //templateTagCompound.removeTag("itemcountmap");
        BlockState2ItemMap mapIntState = BlockState2ItemMap.readFromNBT(templateTagCompound);
        mapIntState.initStateItemMap(player);
        mapIntState.writeToNBT(templateTagCompound);
        templateTagCompound.setString("owner", player.getName());

        Multiset<UniqueItem> itemCountMap = HashMultiset.create();
        List<BlockMap> blockMapList = GadgetCopyPaste.getBlockMapList(templateTagCompound);
        for (BlockMap blockMap : blockMapList) {
            UniqueItem uniqueItem = mapIntState.getItemForState(blockMap.getState());
            if (!(uniqueItem == null)) {
                NonNullList<ItemStack> drops = NonNullList.create();
                blockMap.getState().getBlock().getDrops(drops, world, new BlockPos(0, 0, 0), blockMap.getState(), 0);
                int neededItems = 0;
                for (ItemStack drop : drops) {
                    if (drop.getItem().equals(uniqueItem.getItem())) {
                        neededItems++;
                    }
                }
                if (neededItems == 0) {
                    neededItems = 1;
                }
                if (uniqueItem.getItem() != Items.AIR) {
                    itemCountMap.add(uniqueItem, neededItems);
                }
            }
        }

        templateWorldSave.addToMap(uuidTemplate, templateTagCompound);


        template.setStartPos(templateStack, startPos);
        template.setEndPos(templateStack, endPos);
        //template.setItemCountMap(templateStack, tagMap);
        template.setItemCountMap(templateStack, itemCountMap);
        Template.setName(templateStack, templateName);
        container.putStackInSlot(1, templateStack);
        PacketHandler.INSTANCE.sendTo(new PacketBlockMap(templateTagCompound), (EntityPlayerMP) player);
    }

    public static void copyTemplate(TemplateManagerContainer container) {
        ItemStack itemStack0 = container.getSlot(0).getStack();
        if (itemStack0.getItem() instanceof GadgetCopyPaste) {
            NBTTagCompound tagCompound = PasteToolBufferBuilder.getTagFromUUID(ModItems.gadgetCopyPaste.getUUID(itemStack0));
            if (tagCompound == null) {
                Minecraft.getMinecraft().player.sendStatusMessage(new TextComponentString(TextFormatting.RED + new TextComponentTranslation("message.gadget.copyfailed").getUnformattedComponentText()), false);
                return;
            }
            NBTTagCompound newCompound = new NBTTagCompound();
            newCompound.setIntArray("stateIntArray", tagCompound.getIntArray("stateIntArray"));
            newCompound.setIntArray("posIntArray", tagCompound.getIntArray("posIntArray"));
            newCompound.setTag("mapIntState", tagCompound.getTag("mapIntState"));
            GadgetUtils.writePOSToNBT(newCompound, GadgetUtils.getPOSFromNBT(tagCompound, "startPos"), "startPos", 0);
            GadgetUtils.writePOSToNBT(newCompound, GadgetUtils.getPOSFromNBT(tagCompound, "endPos"), "endPos", 0);
            //Map<UniqueItem, Integer> tagMap = GadgetCopyPaste.getItemCountMap(itemStack0);
            //NBTTagList tagList = GadgetUtils.itemCountToNBT(tagMap);
            //newCompound.setTag("itemcountmap", tagList);
            String jsonTag = newCompound.toString();
            setClipboardString(jsonTag);
            Minecraft.getMinecraft().player.sendStatusMessage(new TextComponentString(TextFormatting.AQUA + new TextComponentTranslation("message.gadget.copysuccess").getUnformattedComponentText()), false);
        }
    }
}
