package com.direwolf20.buildinggadgets.client.screens;

import com.direwolf20.buildinggadgets.common.gadgets.AbstractGadget;
import com.direwolf20.buildinggadgets.common.gadgets.BuildingGadget;
import com.direwolf20.buildinggadgets.common.network.PacketHandler;
import com.direwolf20.buildinggadgets.common.network.PacketToggleBlockPlacement;
import com.direwolf20.buildinggadgets.common.network.PacketToggleConnectedArea;
import com.direwolf20.buildinggadgets.common.network.PacketToggleFuzzy;
import net.minecraft.item.ItemStack;

public class BuildingRadial extends AbstractRadialMenu {
    private static final ModeIcon[] icons = new ModeIcon[]{
        new ModeIcon("textures/gui/mode/build_to_me.png", "build_to_me"),
        new ModeIcon("textures/gui/mode/vertical_column.png", "vertical_column"),
        new ModeIcon("textures/gui/mode/horizontal_column.png", "horizontal_column"),
        new ModeIcon("textures/gui/mode/vertical_wall.png", "vertical_wall"),
        new ModeIcon("textures/gui/mode/horizontal_wall.png", "horizontal_wall"),
        new ModeIcon("textures/gui/mode/stairs.png", "stairs"),
        new ModeIcon("textures/gui/mode/grid.png", "grid"),
        new ModeIcon("textures/gui/mode/surface.png", "surface")
    };

    public BuildingRadial(ItemStack gadget) {
        super(icons, gadget);

        this.modeIndex = BuildingGadget.getToolMode(gadget).ordinal();
    }

    @Override
    public void initGui() {
        super.initGui();

        this.rightSlots.add(new ZeroButton("Place on top", "building_place_atop", send -> {
            if (send) PacketHandler.INSTANCE.sendToServer(new PacketToggleBlockPlacement());
            return BuildingGadget.shouldPlaceAtop(this.getGadget());
        }));

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
