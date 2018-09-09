package com.direwolf20.buildinggadgets.blocks.templatemanager;

import com.direwolf20.buildinggadgets.ModItems;
import com.direwolf20.buildinggadgets.items.CopyPasteTool;
import com.direwolf20.buildinggadgets.items.Template;
import com.direwolf20.buildinggadgets.tools.BlockMapWorldSave;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TemplateManagerCommands {
    private static final Set<Item> allowedItems = Stream.of(Items.PAPER, ModItems.copyPasteTool, ModItems.template).collect(Collectors.toSet());

    public static void loadTemplate(TemplateManagerContainer container, EntityPlayer player) {
        if (!(allowedItems.contains(container.getSlot(0).getStack().getItem())) || !(allowedItems.contains(container.getSlot(1).getStack().getItem()))) {
            return;
        }
    }

    public static void saveTemplate(TemplateManagerContainer container, EntityPlayer player) {
        Set<Item> allowedItems = Stream.of(Items.PAPER, ModItems.copyPasteTool, ModItems.template).collect(Collectors.toSet());
        if (!(allowedItems.contains(container.getSlot(0).getStack().getItem())) || !(allowedItems.contains(container.getSlot(1).getStack().getItem()))) {
            return;
        }
        ItemStack itemStack0 = container.getSlot(0).getStack();
        ItemStack itemStack1 = container.getSlot(1).getStack();
        if (itemStack0.getItem().equals(Items.PAPER)) return;
        World world = player.world;
        if (itemStack0.getItem().equals(ModItems.copyPasteTool)) {
            String UUID = CopyPasteTool.getUUID(itemStack0);
            if (itemStack1.getItem().equals(Items.PAPER)) {
                ItemStack templateStack = new ItemStack(ModItems.template, 1);
                container.putStackInSlot(1, templateStack);
                //container.detectAndSendChanges();
            }
            String UUIDTemplate = Template.getUUID(itemStack1);
            if (UUID == null || UUID.equals("")) return;
            BlockMapWorldSave worldSave = BlockMapWorldSave.get(world);
            NBTTagCompound tagCompound = worldSave.getCompoundFromUUID(UUID);

            System.out.println("Good");
        }
    }
}
