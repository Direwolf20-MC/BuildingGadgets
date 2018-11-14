package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetDestruction;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetExchanger;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import net.minecraftforge.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(BuildingGadgets.MODID)
public class ModItems {
    @GameRegistry.ObjectHolder("buildingtool")
    public static GadgetBuilding gadgetBuilding;

    @GameRegistry.ObjectHolder("exchangertool")
    public static GadgetExchanger gadgetExchanger;

    @GameRegistry.ObjectHolder("constructionpaste")
    public static ConstructionPaste constructionPaste;

    @GameRegistry.ObjectHolder("constructionpastecontainer")
    public static ConstructionPasteContainer constructionPasteContainer;

    @GameRegistry.ObjectHolder("constructionpastecontainert2")
    public static ConstructionPasteContainerT2 constructionPasteContainert2;

    @GameRegistry.ObjectHolder("constructionpastecontainert3")
    public static ConstructionPasteContainerT3 constructionPasteContainert3;

    @GameRegistry.ObjectHolder("copypastetool")
    public static GadgetCopyPaste gadgetCopyPaste;

    @GameRegistry.ObjectHolder("template")
    public static Template template;

    @GameRegistry.ObjectHolder("destructiontool")
    public static GadgetDestruction gadgetDestruction;
}
