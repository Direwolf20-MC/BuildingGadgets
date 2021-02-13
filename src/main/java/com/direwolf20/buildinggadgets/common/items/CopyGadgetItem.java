package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.api.modes.IMode;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetAbilities;

import java.util.HashSet;
import java.util.Set;

public class CopyGadgetItem extends AbstractGadget {
    private static final GadgetAbilities ABILITIES = new GadgetAbilities(true, false, false, false, true, false);

    public CopyGadgetItem() {
        super(Config.GADGETS.GADGET_COPY_PASTE);
    }

    @Override
    public Set<IMode> getModes() {
        return new HashSet<>();
    }

    @Override
    public GadgetAbilities getAbilities() {
        return ABILITIES;
    }
}
