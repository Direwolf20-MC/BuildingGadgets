package com.direwolf20.buildinggadgets.client.cache;

import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketSetRemoteInventoryCache;
import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryLinker;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.UniqueItem;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Multiset;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class RemoteInventoryCache {
    private boolean isCopyPaste, forceUpdate;
    private Pair<BlockPos, ResourceKey<Level>> locCached;
    private Multiset<UniqueItem> cache;
    private Stopwatch timer;

    public RemoteInventoryCache(boolean isCopyPaste) {
        this.isCopyPaste = isCopyPaste;
    }

    public void setCache(Multiset<UniqueItem> cache) {
        this.cache = cache;
    }

    public void forceUpdate() {
        forceUpdate = true;
    }

    public boolean maintainCache(ItemStack tool) {
        Pair<BlockPos, ResourceKey<Level>> loc = InventoryLinker.getDataFromStack(tool);
        if (isCacheOld(loc))
            updateCache(loc);

        return loc != null;
    }

    public Multiset<UniqueItem> getCache() {
        return cache;
    }

    private void updateCache(Pair<BlockPos, ResourceKey<Level>> loc) {
        locCached = loc;
        if (loc == null)
            cache = null;
        else {
            PacketHandler.sendToServer(new PacketSetRemoteInventoryCache(loc, isCopyPaste));
        }
    }

    private boolean isCacheOld(@Nullable Pair<BlockPos, ResourceKey<Level>> loc) {
        if (!Objects.equals(locCached, loc)) {
            timer = loc == null ? null : Stopwatch.createStarted();
            return true;
        }
        if (timer != null) {
            boolean overtime = forceUpdate || timer.elapsed(TimeUnit.MILLISECONDS) >= 5000;
            if (overtime) {
                timer.reset();
                timer.start();
                forceUpdate = false;
            }
            return overtime;
        }
        return false;
    }
}