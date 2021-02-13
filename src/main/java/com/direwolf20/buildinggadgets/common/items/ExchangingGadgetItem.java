package com.direwolf20.buildinggadgets.common.items;

import com.direwolf20.buildinggadgets.api.modes.IMode;
import com.direwolf20.buildinggadgets.common.building.Modes;
import com.direwolf20.buildinggadgets.common.config.Config;
import com.direwolf20.buildinggadgets.common.items.gadgets.GadgetAbilities;

import java.util.Set;

public class ExchangingGadgetItem extends AbstractGadget {
    private static final GadgetAbilities ABILITIES = new GadgetAbilities(true, true, true, false, false, true);

    public ExchangingGadgetItem() {
        super(Config.GADGETS.GADGET_EXCHANGER);
    }

    @Override
    public Set<IMode> getModes() {
        return Modes.getExchangingModes();
    }

    @Override
    public GadgetAbilities getAbilities() {
        return ABILITIES;
    }
}
