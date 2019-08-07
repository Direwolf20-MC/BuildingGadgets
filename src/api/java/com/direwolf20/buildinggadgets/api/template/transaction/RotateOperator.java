package com.direwolf20.buildinggadgets.api.template.transaction;

import java.util.EnumSet;

final class RotateOperator extends AbsSingleRunTransactionOperator {
    RotateOperator() {
        super(EnumSet.of(TransactionOperation.TRANSFORM_TARGET));
    }
}
