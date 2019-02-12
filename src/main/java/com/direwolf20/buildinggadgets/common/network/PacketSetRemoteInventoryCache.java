package com.direwolf20.buildinggadgets.common.network;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.direwolf20.buildinggadgets.client.events.EventTooltip;
import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;
import com.direwolf20.buildinggadgets.common.tools.InventoryManipulation;
import com.direwolf20.buildinggadgets.common.tools.ToolRenders;
import com.direwolf20.buildinggadgets.common.tools.UniqueItem;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.IItemHandler;

public class PacketSetRemoteInventoryCache implements IMessage {

    private boolean isCopyPaste;
    private Multiset<UniqueItem> cache;
    private Pair<Integer, BlockPos> loc;

    public PacketSetRemoteInventoryCache() {}

    public PacketSetRemoteInventoryCache(Multiset<UniqueItem> cache, boolean isCopyPaste) {
        this.cache = cache;
        this.isCopyPaste = isCopyPaste;
    }

    public PacketSetRemoteInventoryCache(Pair<Integer, BlockPos> loc, boolean isCopyPaste) {
        this.loc = loc;
        this.isCopyPaste = isCopyPaste;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        isCopyPaste = buf.readBoolean();
        if (buf.readBoolean()) {
            loc = new ImmutablePair<>(buf.readInt(), BlockPos.fromLong(buf.readLong()));
            return;
        }
        int len = buf.readInt();
        ImmutableMultiset.Builder<UniqueItem> builder = ImmutableMultiset.builder();
        for (int i = 0; i < len; i++)
            builder.addCopies(new UniqueItem(Item.getItemById(buf.readInt()), buf.readInt()), buf.readInt());

        cache = builder.build();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(isCopyPaste);
        boolean isRequest = cache == null;
        buf.writeBoolean(isRequest);
        if (isRequest) {
            buf.writeInt(loc.getLeft());
            buf.writeLong(loc.getRight().toLong());
            return;
        }
        Set<Entry<UniqueItem>> items = cache.entrySet();
        buf.writeInt(items.size());
        for (Entry<UniqueItem> entry : items) {
            UniqueItem uniqueItem = entry.getElement();
            buf.writeInt(Item.getIdFromItem(uniqueItem.item));
            buf.writeInt(uniqueItem.meta);
            buf.writeInt(entry.getCount());
        }
    }

    public static class Handler implements IMessageHandler<PacketSetRemoteInventoryCache, IMessage> {
        @Override
        public IMessage onMessage(PacketSetRemoteInventoryCache message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                if (ctx.side == Side.SERVER) {
                    EntityPlayerMP player = ctx.getServerHandler().player;
                    Set<UniqueItem> itemTypes = new HashSet<>();
                    ImmutableMultiset.Builder<UniqueItem> builder = ImmutableMultiset.builder();
                    IItemHandler remoteInventory = GadgetUtils.getRemoteInventory(message.loc.getRight(), message.loc.getLeft(), player.world);
                    if (remoteInventory != null) {
                        for (int i = 0; i < remoteInventory.getSlots(); i++) {
                            ItemStack stack = remoteInventory.getStackInSlot(i);
                            if (!stack.isEmpty()) {
                                Item item = stack.getItem();
                                int meta = stack.getItemDamage();
                                UniqueItem uniqueItem = new UniqueItem(item, meta);
                                if (!itemTypes.contains(uniqueItem)) {
                                    itemTypes.add(uniqueItem);
                                    builder.addCopies(uniqueItem, InventoryManipulation.countInContainer(remoteInventory,item, meta));
                                }
                            }
                        }
                    }
                    PacketHandler.INSTANCE.sendTo(new PacketSetRemoteInventoryCache(builder.build(), message.isCopyPaste), player);
                    return;
                }
                if (message.isCopyPaste)
                    EventTooltip.setCache(message.cache);
                else
                    ToolRenders.setInventoryCache(message.cache);
            });
            return null;
        }
    }
}