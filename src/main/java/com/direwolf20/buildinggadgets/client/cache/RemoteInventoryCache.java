package com.direwolf20.buildinggadgets.client.cache;

import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketSetRemoteInventoryCache;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.helpers.InventoryHelper.IRemoteInventoryProvider;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.direwolf20.buildinggadgets.common.util.tools.UniqueItem;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Multiset;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

public class RemoteInventoryCache implements IRemoteInventoryProvider {
    private boolean isCopyPaste, forceUpdate;
    private Pair<ResourceLocation, BlockPos> locCached;
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

    @Override
    public int countItem(ItemStack tool, ItemStack stack) {
        Pair<ResourceLocation, BlockPos> loc = getInventoryLocation(tool);
        if (isCacheOld(loc))
            updateCache(loc);

        return cache == null ? 0 : cache.count(new UniqueItem(stack.getItem()));
    }

    private void updateCache(Pair<ResourceLocation, BlockPos> loc) {
        locCached = loc;
        if (loc == null)
            cache = null;
        else {
            PacketHandler.sendToServer(new PacketSetRemoteInventoryCache(loc, isCopyPaste));
        }
    }

    private boolean isCacheOld(@Nullable Pair<ResourceLocation, BlockPos> loc) {
        if (locCached == null ? loc != null : !locCached.equals(loc)) {
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

    @Nullable
    private Pair<ResourceLocation, BlockPos> getInventoryLocation(ItemStack stack) {
        CompoundNBT nbt = stack.getTag();
        if (nbt == null)
            return null;

        ResourceLocation dim = GadgetUtils.getDIMFromNBT(stack, NBTKeys.REMOTE_INVENTORY_POS);
        BlockPos pos = GadgetUtils.getPOSFromNBT(stack, NBTKeys.REMOTE_INVENTORY_POS);
        return dim == null || pos == null ? null : new ImmutablePair<>(dim, pos);
    }
}