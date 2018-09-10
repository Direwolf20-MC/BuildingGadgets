package com.direwolf20.buildinggadgets.blocks.templatemanager;

import com.direwolf20.buildinggadgets.ModItems;
import com.direwolf20.buildinggadgets.items.CopyPasteTool;
import com.direwolf20.buildinggadgets.items.Template;
import com.direwolf20.buildinggadgets.network.PacketBlockMap;
import com.direwolf20.buildinggadgets.network.PacketHandler;
import com.direwolf20.buildinggadgets.tools.BlockMapWorldSave;
import com.direwolf20.buildinggadgets.tools.TemplateWorldSave;
import com.direwolf20.buildinggadgets.tools.UniqueItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TemplateManagerCommands {
    private static final Set<Item> allowedItemsLeft = Stream.of(ModItems.copyPasteTool, ModItems.template).collect(Collectors.toSet());
    private static final Set<Item> allowedItemsRight = Stream.of(Items.PAPER, ModItems.template).collect(Collectors.toSet());

    public static void loadTemplate(TemplateManagerContainer container, EntityPlayer player) {
        ItemStack itemStack0 = container.getSlot(0).getStack();
        ItemStack itemStack1 = container.getSlot(1).getStack();
        if (!(allowedItemsLeft.contains(itemStack0.getItem())) || !(allowedItemsRight.contains(itemStack1.getItem()))) {
            return;
        }
        if (itemStack1.getItem().equals(Items.PAPER)) return;
        World world = player.world;

        BlockPos startPos = Template.getStartPos(itemStack1);
        BlockPos endPos = Template.getEndPos(itemStack1);
        Map<UniqueItem, Integer> tagMap = Template.getItemCountMap(itemStack1);
        String UUIDTemplate = Template.getUUID(itemStack1);
        if (UUIDTemplate == null || UUIDTemplate.equals("")) return;

        BlockMapWorldSave worldSave = BlockMapWorldSave.get(world);
        TemplateWorldSave templateWorldSave = TemplateWorldSave.get(world);
        NBTTagCompound tagCompound;

        if (itemStack0.getItem().equals(ModItems.copyPasteTool)) {
            CopyPasteTool.setStartPos(itemStack0, startPos);
            CopyPasteTool.setEndPos(itemStack0, endPos);
            CopyPasteTool.setItemCountMap(itemStack0, tagMap);
            String UUID = CopyPasteTool.getUUID(itemStack0);

            if (UUID == null || UUID.equals("")) return;

            NBTTagCompound templateTagCompound = templateWorldSave.getCompoundFromUUID(UUIDTemplate);
            tagCompound = templateTagCompound.copy();

            tagCompound.setInteger("copycounter", CopyPasteTool.getCopyCounter(itemStack0));
            tagCompound.setString("UUID", CopyPasteTool.getUUID(itemStack0));
            tagCompound.setString("owner", player.getName());
            worldSave.addToMap(UUID, tagCompound);
            CopyPasteTool.incrementCopyCounter(itemStack0);

            container.putStackInSlot(0, itemStack0);
            PacketHandler.INSTANCE.sendTo(new PacketBlockMap(tagCompound), (EntityPlayerMP) player);
        }
    }

    public static void saveTemplate(TemplateManagerContainer container, EntityPlayer player, String templateName) {
        ItemStack itemStack0 = container.getSlot(0).getStack();
        ItemStack itemStack1 = container.getSlot(1).getStack();

        if (itemStack0.isEmpty() && itemStack1.getItem() instanceof Template && !templateName.isEmpty()) {
            Template.setName(itemStack1, templateName);
            container.putStackInSlot(1, itemStack1);
            return;
        }


        if (!(allowedItemsLeft.contains(itemStack0.getItem())) || !(allowedItemsRight.contains(itemStack1.getItem()))) {
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
        BlockMapWorldSave worldSave = BlockMapWorldSave.get(world);
        TemplateWorldSave templateWorldSave = TemplateWorldSave.get(world);
        NBTTagCompound templateTagCompound;

        if (itemStack0.getItem().equals(ModItems.copyPasteTool)) {
            String UUID = CopyPasteTool.getUUID(itemStack0);
            String UUIDTemplate = Template.getUUID(templateStack);
            if (UUID == null || UUID.equals("")) return;
            if (UUIDTemplate == null || UUIDTemplate.equals("")) return;

            NBTTagCompound tagCompound = worldSave.getCompoundFromUUID(UUID);
            templateTagCompound = tagCompound.copy();

            templateWorldSave.addToMap(UUIDTemplate, templateTagCompound);
            BlockPos startPos = CopyPasteTool.getStartPos(itemStack0);
            BlockPos endPos = CopyPasteTool.getEndPos(itemStack0);
            Map<UniqueItem, Integer> tagMap = CopyPasteTool.getItemCountMap(itemStack0);
            Template.setStartPos(templateStack, startPos);
            Template.setEndPos(templateStack, endPos);
            Template.setItemCountMap(templateStack, tagMap);
            Template.setName(templateStack, templateName);
            container.putStackInSlot(1, templateStack);
        } else {
            String UUID = Template.getUUID(itemStack0);
            String UUIDTemplate = Template.getUUID(templateStack);
            if (UUID == null || UUID.equals("")) return;
            if (UUIDTemplate == null || UUIDTemplate.equals("")) return;

            NBTTagCompound tagCompound = templateWorldSave.getCompoundFromUUID(UUID);
            templateTagCompound = tagCompound.copy();

            templateWorldSave.addToMap(UUIDTemplate, templateTagCompound);
            BlockPos startPos = Template.getStartPos(itemStack0);
            BlockPos endPos = Template.getEndPos(itemStack0);
            Map<UniqueItem, Integer> tagMap = Template.getItemCountMap(itemStack0);
            Template.setStartPos(templateStack, startPos);
            Template.setEndPos(templateStack, endPos);
            Template.setItemCountMap(templateStack, tagMap);
            if (templateName.equals("")) {
                Template.setName(templateStack, Template.getName(itemStack0));
            } else {
                Template.setName(templateStack, templateName);
            }
            container.putStackInSlot(1, templateStack);
        }
    }
}
