package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.common.modes.Mode;

import java.util.Collections;
import java.util.List;

public class DestructionGadget extends Gadget {

    public DestructionGadget() {
        super();
    }

    @Override
    public void action() {

    }

    @Override
    public void undo() {

    }

    @Override
    protected List<Mode> getModes() {
        return Collections.emptyList();
    }
}
