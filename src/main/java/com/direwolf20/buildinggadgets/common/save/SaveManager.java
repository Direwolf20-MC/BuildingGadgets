package com.direwolf20.buildinggadgets.common.save;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;

import java.nio.file.Path;

public enum SaveManager {
    INSTANCE;
    private TemplateSave copyPasteSave;
    private TemplateSave templateSave;
    private RegionSnapshotWorldSave copyPasteUndo;
    private RegionSnapshotWorldSave destructionUndo;
    private RegionSnapshotWorldSave buildingUndo;
    private RegionSnapshotWorldSave exchangingUndo;

    SaveManager() {
    }

    public void onServerStarted(FMLServerStartedEvent event) {
        Path bgSaveFolder = event.getServer().getDataDirectory().toPath();
        bgSaveFolder = (event.getServer() instanceof DedicatedServer ? bgSaveFolder.resolve("saves") : bgSaveFolder)
                .resolve(event.getServer().getFolderName())
                .resolve(Reference.MODID)
                .resolve(Reference.DIRECTORY_SAVE);
        BuildingGadgets.LOG.info("Loading saves from {}.", bgSaveFolder);
        ServerWorld world = event.getServer().getWorld(DimensionType.OVERWORLD);
        copyPasteSave = new TemplateSave(bgSaveFolder.resolve(Reference.DIRECTORY_COPY_PASTE));
        templateSave = new TemplateSave(bgSaveFolder.resolve(Reference.DIRECTORY_TEMPLATE));
        BuildingGadgets.LOG.debug("Loading World Saves.");
        copyPasteUndo = get(world, Reference.UNDO_COPY_PASTE);
        destructionUndo = get(world, Reference.UNDO_DESTRUCTION);
        buildingUndo = get(world, Reference.UNDO_BUILDING);
        exchangingUndo = get(world, Reference.UNDO_EXCHANGING);
        BuildingGadgets.LOG.debug("Loading Copy-Paste Save.");
        copyPasteSave.loadAll();
        BuildingGadgets.LOG.debug("Loading Template Save.");
        templateSave.loadAll();
        BuildingGadgets.LOG.info("Finished Loading saves");
    }

    public void onServerStopped(FMLServerStoppedEvent event) {
        BuildingGadgets.LOG.info("Writing saves.");
        BuildingGadgets.LOG.debug("Writing Copy-Paste Save.");
        copyPasteSave.saveAll();
        BuildingGadgets.LOG.debug("Writing Template Save.");
        templateSave.saveAll();
        copyPasteSave = null;
        templateSave = null;
        copyPasteUndo = null;
        destructionUndo = null;
        buildingUndo = null;
        exchangingUndo = null;
        BuildingGadgets.LOG.info("Finished writing saves.");
    }

    private static RegionSnapshotWorldSave get(ServerWorld world, String name) {
        return world.getSavedData().getOrCreate(() -> new RegionSnapshotWorldSave(name), name);
    }

    public TemplateSave getCopyPasteSave() {
        return copyPasteSave;
    }

    public TemplateSave getTemplateSave() {
        return templateSave;
    }

    public RegionSnapshotWorldSave getCopyPasteUndo() {
        return copyPasteUndo;
    }

    public RegionSnapshotWorldSave getDestructionUndo() {
        return destructionUndo;
    }

    public RegionSnapshotWorldSave getBuildingUndo() {
        return buildingUndo;
    }

    public RegionSnapshotWorldSave getExchangingUndo() {
        return exchangingUndo;
    }
}
