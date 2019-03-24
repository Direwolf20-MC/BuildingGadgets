package com.direwolf20.buildinggadgets.common.network;

import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetBuilding;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetCopyPaste;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetExchanger;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetGeneric;
import com.direwolf20.buildinggadgets.common.tools.GadgetUtils;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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

    public static enum Operation {
        ROTATE, MIRROR;
    }

    public static class Handler implements IMessageHandler<PacketRotateMirror, IMessage> {
        @Override
        public IMessage onMessage(PacketRotateMirror message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                EntityPlayer player = ctx.getServerHandler().player;
                ItemStack stack = GadgetGeneric.getGadget(player);
                Operation operation = message.operation != null ? message.operation : (player.isSneaking() ? Operation.MIRROR : Operation.ROTATE);
                if (stack.getItem() instanceof GadgetBuilding || stack.getItem() instanceof GadgetExchanger)
                    GadgetUtils.rotateOrMirrorToolBlock(stack, player, operation);
                else if (stack.getItem() instanceof GadgetCopyPaste)
                    GadgetCopyPaste.rotateOrMirrorBlocks(stack, player, operation);
            });
            return null;
        }
    }
}