package com.direwolf20.buildinggadgets.client;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.PacketSetRemoteInventoryCache;
import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import com.direwolf20.buildinggadgets.common.tools.UniqueItem;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Multiset;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RemoteInventoryCache {
    private boolean isCopyPaste;
    private Pair<Integer, BlockPos> locCached;
    private Multiset<UniqueItem> cache;
    private Stopwatch timer;

    public RemoteInventoryCache(boolean isCopyPaste) {
        this.isCopyPaste = isCopyPaste;
    }

    public void setCache(@Nullable Multiset<UniqueItem> cache) {
         this.cache = cache;
    }

    @Nullable
    public Multiset<UniqueItem> getCache(ItemStack stack) {
        Pair<Integer, BlockPos> loc = getInventoryLocation(stack);
        if (isCacheOld(loc))
            updateCache(loc);

        return cache;
    }

    private void updateCache(Pair<Integer, BlockPos> loc) {
        locCached = loc;
        if (loc == null)
            cache = null;
        else
            PacketHandler.INSTANCE.sendToServer(new PacketSetRemoteInventoryCache(loc, isCopyPaste));
    }

    private boolean isCacheOld(@Nullable Pair<Integer, BlockPos> loc) {
        if (locCached == null ? loc != null : !locCached.equals(loc)) {
            timer = loc == null ? null : Stopwatch.createStarted();
            return true;
        }
        if (timer != null) {
            boolean overtime = timer.elapsed(TimeUnit.MILLISECONDS) >= 5000;
            if (overtime) {
                timer.reset();
                timer.start();
            }
            return overtime;
        }
        return false;
    }

    @Nullable
    private Pair<Integer, BlockPos> getInventoryLocation(ItemStack stack) {
        NBTTagCompound nbt = stack.getTagCompound();
        return nbt == null || !nbt.hasKey("boundTE") ? null : new ImmutablePair<>(nbt.getInteger("dimension"), GadgetUtils.getPOSFromNBT(stack, "boundTE"));//TODO mirror GadgetUtils#getBoundTE's inclusion of dimension along with pos
    }
}