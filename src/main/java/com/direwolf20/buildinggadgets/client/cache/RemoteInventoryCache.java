package com.direwolf20.buildinggadgets.client.cache;

import com.direwolf20.buildinggadgets.common.inventory.materials.objects.IUniqueObject;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.packets.PacketSetRemoteInventoryCache;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.ref.NBTKeys;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Multiset;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class RemoteInventoryCache {
    private boolean isCopyPaste, forceUpdate;
    private Pair<ResourceLocation, BlockPos> locCached;
    private Multiset<IUniqueObject<?>> cache;
    private Stopwatch timer;

    public RemoteInventoryCache(boolean isCopyPaste) {
        this.isCopyPaste = isCopyPaste;
    }

    public void setCache(Multiset<IUniqueObject<?>> cache) {
        this.cache = cache;
    }

    public void forceUpdate() {
        forceUpdate = true;
    }

    public Multiset<IUniqueObject<?>> getCache() {
        return cache;
    }

    public boolean maintainCache(ItemStack tool) {
        Pair<ResourceLocation, BlockPos> loc = getInventoryLocation(tool);
        if (isCacheOld(loc))
            updateCache(loc);

        return loc != null;
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