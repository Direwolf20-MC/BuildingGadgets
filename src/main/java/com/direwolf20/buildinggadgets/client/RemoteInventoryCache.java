package com.direwolf20.buildinggadgets.client;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.PacketSetRemoteInventoryCache;
import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import com.direwolf20.buildinggadgets.common.tools.InventoryManipulation.IRemoteInventoryProvider;
import com.direwolf20.buildinggadgets.common.tools.UniqueItem;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Multiset;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RemoteInventoryCache implements IRemoteInventoryProvider {
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

    @Override
    public int countItem(ItemStack tool, ItemStack stack) {
        Pair<Integer, BlockPos> loc = getInventoryLocation(tool);
        if (isCacheOld(loc))
            updateCache(loc);

        return cache == null ? 0 : cache.count(new UniqueItem(stack.getItem(), stack.getMetadata()));
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
        if (nbt == null)
            return null;

        Integer dim = GadgetUtils.getDIMFromNBT(stack, "boundTE");
        BlockPos pos = GadgetUtils.getPOSFromNBT(stack, "boundTE");
        return dim == null || pos == null ? null : new ImmutablePair<>(dim, pos);
    }
}