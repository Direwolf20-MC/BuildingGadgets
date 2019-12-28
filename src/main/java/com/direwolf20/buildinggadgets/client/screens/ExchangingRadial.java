package com.direwolf20.buildinggadgets.client.screens;

import com.direwolf20.buildinggadgets.common.gadgets.AbstractGadget;
import com.direwolf20.buildinggadgets.common.gadgets.ExchangingGadget;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.PacketToggleConnectedArea;
import com.direwolf20.buildinggadgets.common.network.PacketToggleFuzzy;
import net.minecraft.item.ItemStack;

public class ExchangingRadial extends AbstractRadialMenu {
    private static final ModeIcon[] icons = new ModeIcon[]{
        new ModeIcon("textures/gui/mode/surface.png", "surface"),
        new ModeIcon("textures/gui/mode/vertical_column.png", "vertical_column"),
        new ModeIcon("textures/gui/mode/horizontal_column.png", "horizontal_column"),
        new ModeIcon("textures/gui/mode/grid.png", "grid")
    };

    public ExchangingRadial(ItemStack gadget) {
        super(icons, gadget);

        this.modeIndex = ExchangingGadget.getToolMode(gadget).ordinal();
    }

    @Override
    public void initGui() {
        super.initGui();

        this.rightSlots.add(new ZeroButton("Connected area", "connected_area", send -> {
            if (send) PacketHandler.INSTANCE.sendToServer(new PacketToggleConnectedArea());
            return AbstractGadget.getConnectedArea(this.getGadget());
        }));

        this.rightSlots.add(new ZeroButton("Fuzzy", "fuzzy", send -> {
            if (send) PacketHandler.INSTANCE.sendToServer(new PacketToggleFuzzy());
            return AbstractGadget.getFuzzy(this.getGadget());
        }));

        this.sortButtons();
    }
}
