package com.direwolf20.buildinggadgets.api.template.transaction;

import com.direwolf20.buildinggadgets.api.building.BlockData;
import com.direwolf20.buildinggadgets.api.building.PlacementTarget;
import com.direwolf20.buildinggadgets.api.serialisation.TemplateHeader;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public abstract class AbsSingleRunTransactionOperator implements ITransactionOperator {
    private final EnumSet<TransactionOperation> remainingOperations;

    public AbsSingleRunTransactionOperator(EnumSet<TransactionOperation> remainingOperations) {
        this.remainingOperations = remainingOperations;
    }

    @Nullable
    @Override
    public BlockPos createPos(ITransactionExecutionContext context) {
        remainingOperations.remove(TransactionOperation.CREATE_DATA);
        return ITransactionOperator.super.createPos(context);
    }

    @Override
    public TemplateHeader transformHeader(ITransactionExecutionContext context, TemplateHeader header) {
        remainingOperations.remove(TransactionOperation.TRANSFORM_HEADER);
        return ITransactionOperator.super.transformHeader(context, header);
    }

    @Nullable
    @Override
    public BlockData transformData(ITransactionExecutionContext context, BlockData data) {
        remainingOperations.remove(TransactionOperation.TRANSFORM_HEADER);
        return null;
    }

    @Nullable
    @Override
    public BlockPos transformPos(ITransactionExecutionContext context, BlockPos pos, BlockData data) {
        remainingOperations.remove(TransactionOperation.TRANSFORM_HEADER);
        return ITransactionOperator.super.transformPos(context, pos, data);
    }

    @Nullable
    @Override
    public PlacementTarget transformTarget(ITransactionExecutionContext context, PlacementTarget target) {
        remainingOperations.remove(TransactionOperation.TRANSFORM_HEADER);
        return ITransactionOperator.super.transformTarget(context, target);
    }

    @Override
    public Set<TransactionOperation> remainingOperations() {
        return Collections.unmodifiableSet(remainingOperations);
    }
}
