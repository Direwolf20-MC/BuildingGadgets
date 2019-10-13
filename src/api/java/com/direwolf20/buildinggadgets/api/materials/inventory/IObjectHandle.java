package com.direwolf20.buildinggadgets.api.materials.inventory;

public interface IObjectHandle<T> {
    Class<T> getIndexClass();

    T getIndexObject();

    int match(IUniqueObject<?> item, int count, boolean simulate);

    int insert(IUniqueObject<?> item, int count, boolean simulate);

    boolean shouldCleanup();
}
