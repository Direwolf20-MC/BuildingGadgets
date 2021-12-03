package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.capability.CapabilityTemplate;
import com.direwolf20.buildinggadgets.common.items.OurItems;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateKey;
import com.direwolf20.buildinggadgets.common.util.exceptions.CapabilityNotPresentException;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

public final class PacketTemplateManagerTemplateCreated extends UUIDPacket {
    private final BlockPos pos;

    public PacketTemplateManagerTemplateCreated(FriendlyByteBuf buffer) {
        super(buffer);
        this.pos = buffer.readBlockPos();
    }

    public PacketTemplateManagerTemplateCreated(UUID id, BlockPos pos) {
        super(id);
        this.pos = pos;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        super.encode(buffer);
        buffer.writeBlockPos(pos);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            if (contextSupplier.get().getDirection().getReceptionSide() == LogicalSide.SERVER) {
                Level world = contextSupplier.get().getSender().level;
                if (world.hasChunkAt(pos)) {
                    BlockEntity tileEntity = world.getBlockEntity(pos);
                    if (tileEntity != null) {
                        tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(handler -> {
                            ItemStack stack = new ItemStack(OurItems.TEMPLATE_ITEM.get());
                            ITemplateKey key = stack.getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).orElseThrow(CapabilityNotPresentException::new);
                            UUID id = key.getTemplateId(this::getId);
                            if (! id.equals(getId()))
                                BuildingGadgets.LOG.error("Failed to apply Template id on server!");
                            else {
                                ((IItemHandlerModifiable) handler).setStackInSlot(1, stack);
                                world.getCapability(CapabilityTemplate.TEMPLATE_PROVIDER_CAPABILITY).ifPresent(provider ->
                                        provider.requestUpdate(key, PacketDistributor.PLAYER.with(() -> contextSupplier.get().getSender())));
                            }
                        });
                    }
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
