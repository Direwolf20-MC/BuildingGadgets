package com.direwolf20.buildinggadgets.client.screens;

import com.direwolf20.buildinggadgets.common.gadgets.DestructionGadget;
import com.direwolf20.buildinggadgets.common.network.PacketChangeRange;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import net.minecraft.item.ItemStack;

public class DestructionRadial extends AbstractRadialMenu {
    public DestructionRadial(ItemStack gadget) {
        super(new ModeIcon[]{}, gadget);
    }

    @Override
    public void initGui() {
        super.initGui();

        this.topSlots.add(new ZeroButton("Show Overlay", "destroy_overlay", send -> {
            if (send) PacketHandler.INSTANCE.sendToServer(new PacketChangeRange());
            return DestructionGadget.getOverlay(this.getGadget());
        }));

        this.sortButtons();
    }
}
