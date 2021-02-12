package com.direwolf20.buildinggadgets.common.network.packets;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.capability.OurCapabilities;
import com.direwolf20.buildinggadgets.common.items.OurItems;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateKey;
import com.direwolf20.buildinggadgets.common.util.exceptions.CapabilityNotPresentException;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.UUID;
import java.util.function.Supplier;

public final class PacketTemplateManagerTemplateCreated extends UUIDPacket {
    private final BlockPos pos;

    public PacketTemplateManagerTemplateCreated(PacketBuffer buffer) {
        super(buffer);
        this.pos = buffer.readBlockPos();
    }

    public PacketTemplateManagerTemplateCreated(UUID id, BlockPos pos) {
        super(id);
        this.pos = pos;
    }

    @Override
    public void encode(PacketBuffer buffer) {
        super.encode(buffer);
        buffer.writeBlockPos(pos);
    }

    public void handle(Supplier<Context> contextSupplier) {
        Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            if (contextSupplier.get().getDirection().getReceptionSide() == LogicalSide.SERVER) {
                World world = contextSupplier.get().getSender().world;
                if (world.isBlockLoaded(pos)) {
                    TileEntity tileEntity = world.getTileEntity(pos);
                    if (tileEntity != null) {
                        tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(handler -> {
                            ItemStack stack = new ItemStack(OurItems.TEMPLATE_ITEM.get());
                            ITemplateKey key = stack.getCapability(OurCapabilities.TEMPLATE_KEY_CAPABILITY).orElseThrow(CapabilityNotPresentException::new);
                            UUID id = key.getTemplateId(this::getId);
                            if (! id.equals(getId()))
                                BuildingGadgets.LOG.error("Failed to apply Template id on server!");
                            else {
                                ((IItemHandlerModifiable) handler).setStackInSlot(1, stack);
                                world.getCapability(OurCapabilities.TEMPLATE_PROVIDER_CAPABILITY).ifPresent(provider ->
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
