package com.direwolf20.buildinggadgets.common.registry.container;

import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.ScreenManager.IScreenFactory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.network.IContainerFactory;

import java.util.Objects;

public final class ScreenContainerBuilder<T extends Container, U extends Screen & IHasContainer<T>> {
    private final IContainerFactory<T> containerFactory;
    private final IScreenFactory<T, U> screenFactory;
    private ContainerType<T> type;

    public ScreenContainerBuilder(IContainerFactory<T> containerFactory, IScreenFactory<T, U> screenFactory) {
        this.containerFactory = Objects.requireNonNull(containerFactory);
        this.screenFactory = Objects.requireNonNull(screenFactory);
        this.type = null;
    }

    ContainerType<T> getOrCreate() {
        if (type != null)
            return type;
        type = IForgeContainerType.create(containerFactory);
        return type;
    }

    void registerScreen() {
        ScreenManager.registerFactory(type, screenFactory);
    }
}
