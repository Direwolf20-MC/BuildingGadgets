package com.direwolf20.buildinggadgets.common.tainted.save;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.util.ref.Reference.SaveReference;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public enum SaveManager {
    INSTANCE;
    private SaveTemplateProvider templateProvider;
    private TemplateSave templateSave;
    private List<UndoSaveContainer> undoSaves;

    SaveManager() {
        this.templateProvider = new SaveTemplateProvider(this::getTemplateSave);
        this.undoSaves = new LinkedList<>();
    }

    public Supplier<UndoWorldSave> registerUndoSave(Function<ServerWorld, UndoWorldSave> ctrFun) {
        UndoSaveContainer container = new UndoSaveContainer(Objects.requireNonNull(ctrFun));
        this.undoSaves.add(container);
        return container::getCurrentSave;
    }

    public void onServerStarted(FMLServerStartedEvent event) {
        BuildingGadgets.LOG.debug("Loading World Saves.");
        ServerWorld world = event.getServer().getLevel(World.OVERWORLD);
        for (UndoSaveContainer c : undoSaves) {
            c.acquire(world);
        }
        templateSave = getTemplateSave(world, SaveReference.TEMPLATE_SAVE_TEMPLATES);
        BuildingGadgets.LOG.debug("Finished Loading saves");
    }

    public void onServerStopped(FMLServerStoppedEvent event) {
        BuildingGadgets.LOG.debug("Clearing save caches");
        for (UndoSaveContainer c : undoSaves) {
            c.release();
        }
        templateSave = null;
        BuildingGadgets.LOG.debug("Finished clearing save caches");
    }

    public static UndoWorldSave getUndoSave(ServerWorld world, IntSupplier maxLengthSupplier, String name) {
        return get(world, () -> new UndoWorldSave(name, maxLengthSupplier), name);
    }

    private static TemplateSave getTemplateSave(ServerWorld world, String name) {
        return get(world, () -> new TemplateSave(name), name);
    }

    private static <T extends WorldSavedData> T get(ServerWorld world, Supplier<T> supplier, String name) {
        return world.getDataStorage().computeIfAbsent(supplier, name);
    }

    public SaveTemplateProvider getTemplateProvider() {
        return templateProvider;
    }

    public TemplateSave getTemplateSave() {
        return templateSave;
    }

    private static final class UndoSaveContainer {
        private final Function<ServerWorld, UndoWorldSave> constructor;
        @Nullable
        private UndoWorldSave currentSave;

        private UndoSaveContainer(Function<ServerWorld, UndoWorldSave> constructor) {
            this.constructor = constructor;
            this.currentSave = null;
        }

        private void acquire(ServerWorld world) {
            this.currentSave = constructor.apply(world);
        }

        @Nullable
        private UndoWorldSave getCurrentSave() {
            return currentSave;
        }

        private void release() {
            currentSave = null;
        }
    }
}
