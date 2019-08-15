package com.direwolf20.buildinggadgets.api.template;

import com.direwolf20.buildinggadgets.api.building.view.IBuildContext;

public interface IBuildOpenOptions {
    enum OpenType {
        IF_NON_BLOCKING,
        IF_NO_TRANSACTION_OPEN,
        DEFAULT;
    }

    IBuildContext getContext();

    OpenType getOpenType();
}
