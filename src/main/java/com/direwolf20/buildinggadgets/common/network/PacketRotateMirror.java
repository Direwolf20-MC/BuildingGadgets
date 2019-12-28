package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.common.gadgets.*;
import com.direwolf20.buildinggadgets.common.gadgets.BuildingGadget;
import com.direwolf20.buildinggadgets.common.gadgets.AbstractGadget;
import com.direwolf20.buildinggadgets.common.utils.GadgetUtils;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketRotateMirror implements IMessage {
    private Operation operation;

    public PacketRotateMirror() {}

    public PacketRotateMirror(Operation operation) {
        this.operation = operation;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        if (buf.readBoolean())
            operation = Operation.values()[buf.readInt()];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        boolean hasOperation = operation != null;
        buf.writeBoolean(hasOperation);
        if (hasOperation)
            buf.writeInt(operation.ordinal());
    }

    public enum Operation {
        ROTATE, MIRROR
    }

    public static class Handler implements IMessageHandler<PacketRotateMirror, IMessage> {
        @Override
        public IMessage onMessage(PacketRotateMirror message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                EntityPlayer player = ctx.getServerHandler().player;

                AbstractGadget.getGadget(player).ifPresent(gadget -> {
                    Operation operation = message.operation != null ? message.operation : (player.isSneaking() ? Operation.MIRROR : Operation.ROTATE);
                    if (gadget.getItem() instanceof BuildingGadget || gadget.getItem() instanceof ExchangingGadget)
                        GadgetUtils.rotateOrMirrorToolBlock(gadget, player, operation);
                    else if (gadget.getItem() instanceof CopyGadget)
                        CopyGadget.rotateOrMirrorBlocks(gadget, player, operation);
                });
            });
            return null;
        }
    }
}