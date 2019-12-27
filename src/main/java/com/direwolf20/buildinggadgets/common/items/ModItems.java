package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.gadgets.*;
import com.direwolf20.buildinggadgets.common.gadgets.DestructionGadget;
import com.direwolf20.buildinggadgets.common.gadgets.ExchangerGadget;
import com.direwolf20.buildinggadgets.common.items.pastes.ConstructionChunkDense;
import com.direwolf20.buildinggadgets.common.items.pastes.ConstructionPaste;
import com.direwolf20.buildinggadgets.common.items.pastes.ConstructionPasteContainer;
import com.direwolf20.buildinggadgets.common.items.pastes.ConstructionPasteContainerCreative;

import net.minecraftforge.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(BuildingGadgets.MODID)
public class ModItems {
    @GameRegistry.ObjectHolder("buildingtool")
    public static BuildingGadget buildingGadget;

    @GameRegistry.ObjectHolder("exchangertool")
    public static ExchangerGadget exchangerGadget;

    @GameRegistry.ObjectHolder("constructionpaste")
    public static ConstructionPaste constructionPaste;

    @GameRegistry.ObjectHolder("construction_chunk_dense")
    public static ConstructionChunkDense constructionChunkDense;

    @GameRegistry.ObjectHolder("constructionpastecontainer")
    public static ConstructionPasteContainer constructionPasteContainer;

    @GameRegistry.ObjectHolder("constructionpastecontainert2")
    public static ConstructionPasteContainer constructionPasteContainert2;

    @GameRegistry.ObjectHolder("constructionpastecontainert3")
    public static ConstructionPasteContainer constructionPasteContainert3;

    @GameRegistry.ObjectHolder("constructionpastecontainercreative")
    public static ConstructionPasteContainerCreative constructionPasteContainerCreative;

    @GameRegistry.ObjectHolder("copypastetool")
    public static CopyGadget copyGadget;

    @GameRegistry.ObjectHolder("template")
    public static Template template;

    @GameRegistry.ObjectHolder("destructiontool")
    public static DestructionGadget destructionGadget;
}
