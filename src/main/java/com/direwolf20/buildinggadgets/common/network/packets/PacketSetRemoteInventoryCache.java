package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.client.events.EventTooltip;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.tools.InventoryManipulation;
import com.direwolf20.buildinggadgets.common.tools.ToolRenders;
import com.direwolf20.buildinggadgets.common.tools.UniqueItem;
import com.direwolf20.buildinggadgets.common.utils.GadgetUtils;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class PacketSetRemoteInventoryCache {

    private boolean isCopyPaste;
    private Multiset<UniqueItem> cache;
    private Pair<Integer, BlockPos> loc;

    public PacketSetRemoteInventoryCache(Multiset<UniqueItem> cache, boolean isCopyPaste) {
        this.cache = cache;
        this.isCopyPaste = isCopyPaste;
    }

    public PacketSetRemoteInventoryCache(Pair<Integer, BlockPos> loc, boolean isCopyPaste) {
        this.loc = loc;
        this.isCopyPaste = isCopyPaste;
    }

    public static PacketSetRemoteInventoryCache decode(PacketBuffer buf) {
        boolean isCopyPaste = buf.readBoolean();
        if (buf.readBoolean()) {
            Pair<Integer, BlockPos> loc = new ImmutablePair<>(buf.readInt(), BlockPos.fromLong(buf.readLong()));
            return new PacketSetRemoteInventoryCache(loc, isCopyPaste);
        }
        int len = buf.readInt();
        ImmutableMultiset.Builder<UniqueItem> builder = ImmutableMultiset.builder();
        for (int i = 0; i < len; i++)
            builder.addCopies(new UniqueItem(Item.getItemById(buf.readInt())), buf.readInt());

        Multiset<UniqueItem> cache = builder.build();
        return new PacketSetRemoteInventoryCache(cache, isCopyPaste);
    }

    public static void encode(PacketSetRemoteInventoryCache msg, PacketBuffer buf) {
        buf.writeBoolean(msg.isCopyPaste());
        boolean isRequest = msg.getCache() == null;
        buf.writeBoolean(isRequest);
        if (isRequest) {
            buf.writeInt(msg.getLoc().getLeft());
            buf.writeLong(msg.getLoc().getRight().toLong());
            return;
        }
        Set<Entry<UniqueItem>> items = msg.getCache().entrySet();
        buf.writeInt(items.size());
        for (Entry<UniqueItem> entry : items) {
            UniqueItem uniqueItem = entry.getElement();
            buf.writeInt(Item.getIdFromItem(uniqueItem.getItem()));
            buf.writeInt(entry.getCount());
        }
    }

    public boolean isCopyPaste() {
        return isCopyPaste;
    }

    public Multiset<UniqueItem> getCache() {
        return cache;
    }

    public Pair<Integer, BlockPos> getLoc() {
        return loc;
    }

    public static class Handler {
        public static void handle(final PacketSetRemoteInventoryCache msg, Supplier<Context> ctx) {
            ctx.get().enqueueWork(() -> {
                EntityPlayerMP player = ctx.get().getSender();
                if (player != null) {
                    Set<UniqueItem> itemTypes = new HashSet<>();
                    ImmutableMultiset.Builder<UniqueItem> builder = ImmutableMultiset.builder();
                    IItemHandler remoteInventory = GadgetUtils.getRemoteInventory(msg.loc.getRight(), DimensionType.getById(msg.loc.getLeft()), player.world);
                    if (remoteInventory != null) {
                        for (int i = 0; i < remoteInventory.getSlots(); i++) {
                            ItemStack stack = remoteInventory.getStackInSlot(i);
                            if (!stack.isEmpty()) {
                                Item item = stack.getItem();
                                UniqueItem uniqueItem = new UniqueItem(item);
                                if (!itemTypes.contains(uniqueItem)) {
                                    itemTypes.add(uniqueItem);
                                    builder.addCopies(uniqueItem, InventoryManipulation.countInContainer(remoteInventory, item));
                                }
                            }
                        }
                    }
                    PacketHandler.sendTo(new PacketSetRemoteInventoryCache(builder.build(), msg.isCopyPaste()), player);
                    return;
                }
                if (msg.isCopyPaste())
                    EventTooltip.setCache(msg.getCache());
                else
                    ToolRenders.setInventoryCache(msg.getCache());
            });
        }
    }
}