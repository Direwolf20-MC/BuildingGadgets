package com.direwolf20.buildinggadgets.common.items.gadgets;

import com.direwolf20.buildinggadgets.common.network.packets.PacketRotateMirror.Operation;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public abstract class ModeGadget extends AbstractGadget {
    public ModeGadget(Properties builder) {
        super(builder);
    }

    @Override
    public boolean performRotate(ItemStack stack, PlayerEntity player) {
        GadgetUtils.rotateOrMirrorToolBlock(stack, player, Operation.ROTATE);
        return true;
    }

    @Override
    public boolean performMirror(ItemStack stack, PlayerEntity player) {
        GadgetUtils.rotateOrMirrorToolBlock(stack, player, Operation.MIRROR);
        return true;
    }
}
