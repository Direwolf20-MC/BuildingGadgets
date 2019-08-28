package com.direwolf20.buildinggadgets.common.save;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.util.ref.Reference.SaveReference;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

public enum SaveManager {
    INSTANCE;
    private TemplateSave copyPasteSave;
    private TemplateSave templateSave;
    private UndoWorldSave copyPasteUndo;
    private UndoWorldSave destructionUndo;
    private UndoWorldSave buildingUndo;
    private UndoWorldSave exchangingUndo;

    SaveManager() {
    }

    public void onServerStarted(FMLServerStartedEvent event) {
        ServerWorld world = event.getServer().getWorld(DimensionType.OVERWORLD);
        BuildingGadgets.LOG.debug("Loading World Saves.");
        copyPasteUndo = getUndoSave(world, Config.GADGETS.GADGET_COPY_PASTE.undoSize::get, SaveReference.UNDO_COPY_PASTE);
        destructionUndo = getUndoSave(world, Config.GADGETS.GADGET_DESTRUCTION.undoSize::get, SaveReference.UNDO_DESTRUCTION);
        buildingUndo = getUndoSave(world, Config.GADGETS.GADGET_BUILDING.undoSize::get, SaveReference.UNDO_BUILDING);
        exchangingUndo = getUndoSave(world, Config.GADGETS.GADGET_EXCHANGER.undoSize::get, SaveReference.UNDO_EXCHANGING);
        templateSave = getTemplateSave(world, SaveReference.TEMPLATE_SAVE_TEMPLATES);
        copyPasteSave = getTemplateSave(world, SaveReference.TEMPLATE_SAVE_COPY_PASTE);
        BuildingGadgets.LOG.info("Finished Loading saves");
    }

    public void onServerStopped(FMLServerStoppedEvent event) {
        BuildingGadgets.LOG.debug("Clearing save caches");
        copyPasteSave = null;
        templateSave = null;
        copyPasteUndo = null;
        destructionUndo = null;
        buildingUndo = null;
        exchangingUndo = null;
        BuildingGadgets.LOG.debug("Finished clearing save caches");
    }

    private static UndoWorldSave getUndoSave(ServerWorld world, IntSupplier maxLengthSupplier, String name) {
        return get(world, () -> new UndoWorldSave(name, maxLengthSupplier), name);
    }

    private static TemplateSave getTemplateSave(ServerWorld world, String name) {
        return get(world, () -> new TemplateSave(name), name);
    }

    private static <T extends WorldSavedData> T get(ServerWorld world, Supplier<T> supplier, String name) {
        return world.getSavedData().getOrCreate(supplier, name);
    }

    public TemplateSave getCopyPasteSave() {
        return copyPasteSave;
    }

    public TemplateSave getTemplateSave() {
        return templateSave;
    }

    public UndoWorldSave getCopyPasteUndo() {
        return copyPasteUndo;
    }

    public UndoWorldSave getDestructionUndo() {
        return destructionUndo;
    }

    public UndoWorldSave getBuildingUndo() {
        return buildingUndo;
    }

    public UndoWorldSave getExchangingUndo() {
        return exchangingUndo;
    }
}
