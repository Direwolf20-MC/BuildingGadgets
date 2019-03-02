package com.direwolf20.buildinggadgets.common.integration.mods;

import mrriegel.storagenetwork.api.IStorageNetworkHelpers;
import mrriegel.storagenetwork.api.IStorageNetworkPlugin;
import mrriegel.storagenetwork.api.StorageNetworkPlugin;
import mrriegel.storagenetwork.api.data.IItemStackMatcher;
import net.minecraft.item.ItemStack;

@StorageNetworkPlugin
public class SimpleStorageNetworkAPI implements IStorageNetworkPlugin {
    private static IStorageNetworkHelpers helpers;

    @Override
    public void helpersReady(IStorageNetworkHelpers helpers) {
        this.helpers = helpers;
    }

    public static IItemStackMatcher createItemStackMatcher(ItemStack stack) {
        return helpers.createItemStackMatcher(stack, false, true, true);
    }
}