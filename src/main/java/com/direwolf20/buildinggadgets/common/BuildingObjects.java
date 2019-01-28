package com.direwolf20.buildinggadgets.common;

import com.direwolf20.buildinggadgets.common.items.ConstructionPaste;
import com.direwolf20.buildinggadgets.common.items.ConstructionPasteContainer;
import com.direwolf20.buildinggadgets.common.items.Template;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetExchanger;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class BuildingObjects {

    private static int[] containerAmounts = new int[]{512, 2048, 8192};

    private static final String modId = BuildingGadgets.MODID;

    private static ItemGroup creativeTab = new ItemGroup(BuildingGadgets.MODID){
        @Override
        public ItemStack createIcon() {
            return new ItemStack(gadgetBuilding);
        }
    };

    // Gadgets
    public static final Item gadgetBuilding     = new GadgetBuilding(itemBuilder().maxStackSize(1)).setRegistryName(modId, "buildingtool");
    public static final Item gadgetCopyPaste    = new GadgetCopyPaste(itemBuilder().maxStackSize(1)).setRegistryName(modId, "copypastetool");
    public static final Item gadgetDestruction  = new GadgetDestruction(itemBuilder().maxStackSize(1)).setRegistryName(modId, "destructiontool");
    public static final Item gadgetExchanger    = new GadgetExchanger(itemBuilder().maxStackSize(1)).setRegistryName(modId, "exchangertool");

    // Building Items
    public static final Item constructionPaste  = new ConstructionPaste(itemBuilder()).setRegistryName(modId, "constructionpaste");
    public static final Item template           = new Template(itemBuilder().maxStackSize(1)).setRegistryName(modId, "template");

    // Construction Paste Containers
    public static final Item ConstructionPasteContainer = new ConstructionPasteContainer(itemBuilder(), containerAmounts[0]).setRegistryName("constructionpastecontainer");
    public static final Item ConstructionPasteContainer2 = new ConstructionPasteContainer(itemBuilder(), containerAmounts[1]).setRegistryName("constructionpastecontainer2");
    public static final Item ConstructionPasteContainer3 = new ConstructionPasteContainer(itemBuilder(), containerAmounts[2]).setRegistryName("constructionpastecontainer3");

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().registerAll(
                // Gadgets
                gadgetBuilding,
                gadgetCopyPaste,
                gadgetDestruction,
                gadgetExchanger,

                // Building Items
                constructionPaste,
                ConstructionPasteContainer,
                ConstructionPasteContainer2,
                ConstructionPasteContainer3,

                template
        );
    }

    private static Item.Builder itemBuilder() {
        return new Item.Builder().group(creativeTab);
    }
}
