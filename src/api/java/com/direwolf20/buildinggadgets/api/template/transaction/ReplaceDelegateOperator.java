package com.direwolf20.buildinggadgets.api.template.transaction;


import com.direwolf20.buildinggadgets.api.template.ITemplate;

import java.util.Objects;

public class ReplaceDelegateOperator implements ITransactionOperator {
    private final ITemplate newDelegate;

    ReplaceDelegateOperator(ITemplate newDelegate) {
        this.newDelegate = Objects.requireNonNull(newDelegate, "Cannot have a null Delegate Template!");
    }

    public ITemplate getNewDelegate() {
        return newDelegate;
    }
}
