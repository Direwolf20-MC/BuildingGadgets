package com.direwolf20.buildinggadgets.api.template.transaction;

import com.direwolf20.buildinggadgets.api.building.BlockData;
import com.direwolf20.buildinggadgets.api.building.PlacementTarget;
import com.direwolf20.buildinggadgets.api.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.api.exceptions.OperatorExecutionFailedException;
import com.direwolf20.buildinggadgets.api.exceptions.TransactionExecutionException;
import com.direwolf20.buildinggadgets.api.serialisation.TemplateHeader;
import com.direwolf20.buildinggadgets.api.template.ITemplate;
import com.direwolf20.buildinggadgets.api.template.transaction.ITransactionOperator.TransactionOperation;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.*;

public abstract class AbsTemplateTransaction implements ITemplateTransaction {
    private List<ITransactionOperator> operators;
    private boolean valid;

    public AbsTemplateTransaction() {
        operators = new ArrayList<>();
        this.valid = true;
    }

    @Override
    public ITemplateTransaction operate(ITransactionOperator operator) {
        operators.add(operator);
        return this;
    }

    @Override
    public ITemplate execute(@Nullable IBuildContext context) throws TransactionExecutionException {
        if (! isValid())
            throw new UnsupportedOperationException("Cannot execute TemplateTransaction twice!");
        OperatorOrdering ordering = createOrdering(operators);
        ITransactionExecutionContext exContext = createContext();
        boolean changed = false;
        Queue<OperatorOrdering> orderingHistory = new LinkedList<>();
        while (ordering.hasActingTransformers()) {
            transformData(exContext, ordering);
            exContext = createContext();
            transformPositions(exContext, ordering);
            exContext = createContext();
            transformTargets(exContext, ordering);
            exContext = createContext();
            transformHeader(exContext, ordering);
            orderingHistory.add(ordering);
            exContext = createContext();
            ordering = createOrdering(operators);
            changed = true;
        }
        return createTemplate(exContext, orderingHistory, context, changed);
    }

    protected void invalidate() {
        this.valid = false;
    }

    protected boolean isValid() {
        return valid;
    }

    protected OperatorOrdering createOrdering(Collection<ITransactionOperator> operators) throws TransactionExecutionException {
        return new OperatorOrdering(operators);
    }

    protected abstract ITransactionExecutionContext createContext() throws TransactionExecutionException;

    protected void failOperatorExecution(ITransactionOperator operator, @Nullable Throwable cause) throws TransactionExecutionException {
        throw new OperatorExecutionFailedException("Failed to execute Transaction Operator of type " + operator.getClass().getName(), cause, operator);
    }

    protected Map<BlockPos, BlockData> createData(ITransactionExecutionContext exContext, OperatorOrdering ordering) throws TransactionExecutionException {
        Map<BlockPos, BlockData> created = new HashMap<>();
        for (ITransactionOperator operator : ordering.getDataCreators()) {
            List<BlockPos> positions = new LinkedList<>();
            BlockPos current = operator.createPos(exContext);
            while (current != null) {
                positions.add(current);
                try {
                    current = operator.createPos(exContext);
                } catch (Exception e) {
                    failOperatorExecution(operator, e);
                }
            }
            for (BlockPos pos : positions) {
                try {
                    created.put(pos, Objects.requireNonNull(operator.createDataForPos(exContext, pos),
                            "Operator " + operator + " may not return null for position which he created himself!"));
                } catch (Exception e) {
                    failOperatorExecution(operator, e);
                }
            }
        }
        return created;
    }

    protected abstract void mergeCreated(ITransactionExecutionContext exContext, OperatorOrdering ordering, Map<BlockPos, BlockData> created);

    protected abstract void transformAllData(ITransactionExecutionContext exContext, OperatorOrdering ordering, DataTransformer dataTransformer) throws TransactionExecutionException;

    protected abstract void transformAllPositions(ITransactionExecutionContext exContext, OperatorOrdering ordering, PositionTransformer positionTransformer) throws TransactionExecutionException;

    protected abstract void transformAllTargets(ITransactionExecutionContext exContext, OperatorOrdering ordering, TargetTransformer targetTransformer) throws TransactionExecutionException;

    protected abstract void updateHeader(ITransactionExecutionContext exContext, OperatorOrdering ordering, HeaderTransformer transformer) throws TransactionExecutionException;

    protected abstract ITemplate createTemplate(ITransactionExecutionContext exContext, Queue<OperatorOrdering> ordering, @Nullable IBuildContext context, boolean changed) throws TransactionExecutionException;

    protected void performCreateData(ITransactionExecutionContext exContext, OperatorOrdering ordering) throws TransactionExecutionException {
        Map<BlockPos, BlockData> created = createData(exContext, ordering);
        if (! created.isEmpty())
            mergeCreated(exContext, ordering, created);
    }

    protected void transformData(ITransactionExecutionContext exContext, OperatorOrdering ordering) throws TransactionExecutionException {
        if (ordering.getDataTransformers().isEmpty())
            return;
        transformAllData(exContext, ordering, d -> {
            for (ITransactionOperator operator : ordering.getDataTransformers()) {
                try {
                    d = operator.transformData(exContext, d);
                } catch (Exception e) {
                    failOperatorExecution(operator, e);
                }
                if (d == null)
                    break;
            }
            return d;
        });
    }

    protected void transformPositions(ITransactionExecutionContext exContext, OperatorOrdering ordering) throws TransactionExecutionException {
        if (ordering.getPositionTransformers().isEmpty())
            return;
        transformAllPositions(exContext, ordering, (p, d) -> {
            for (ITransactionOperator operator : ordering.getPositionTransformers()) {
                try {
                    p = operator.transformPos(exContext, p, d);
                } catch (Exception e) {
                    failOperatorExecution(operator, e);
                }
                if (p == null)
                    break;
            }
            return p;
        });
    }

    protected void transformTargets(ITransactionExecutionContext exContext, OperatorOrdering ordering) throws TransactionExecutionException {
        if (ordering.getTargetTransformers().isEmpty())
            return;
        transformAllTargets(exContext, ordering, target -> {
            for (ITransactionOperator operator : ordering.getTargetTransformers()) {
                try {
                    target = operator.transformTarget(exContext, target);
                } catch (Exception e) {
                    failOperatorExecution(operator, e);
                }
                if (target == null)
                    break;
            }
            return target;
        });
    }

    protected void transformHeader(ITransactionExecutionContext exContext, OperatorOrdering ordering) throws TransactionExecutionException {
        updateHeader(exContext, ordering, header -> {
            for (ITransactionOperator operator : ordering.getHeaderTransformers()) {
                try {
                    header = Objects.requireNonNull(operator.transformHeader(exContext, header), "Operator " + operator + " may not return a null TemplateHeader!");
                } catch (Exception e) {
                    failOperatorExecution(operator, e);
                }
            }
            return header;
        });
    }

    @FunctionalInterface
    protected interface DataTransformer {
        @Nullable
        BlockData transformData(BlockData data) throws TransactionExecutionException;
    }

    @FunctionalInterface
    protected interface PositionTransformer {
        @Nullable
        BlockPos transformPos(BlockPos pos, BlockData data) throws TransactionExecutionException;
    }

    @FunctionalInterface
    protected interface TargetTransformer {
        @Nullable
        PlacementTarget transformTarget(PlacementTarget target) throws TransactionExecutionException;
    }

    @FunctionalInterface
    protected interface HeaderTransformer {
        public TemplateHeader transformHeader(TemplateHeader header) throws TransactionExecutionException;
    }

    public static class OperatorOrdering {
        private final ImmutableList<ITransactionOperator> headerTransformers;
        private final ImmutableList<ITransactionOperator> positionTransformers;
        private final ImmutableList<ITransactionOperator> dataTransformers;
        private final ImmutableList<ITransactionOperator> targetTransformers;
        private final ImmutableList<ITransactionOperator> dataCreators;
        private final boolean hasActingTransformers;

        protected OperatorOrdering(Collection<ITransactionOperator> operators) {
            headerTransformers = operators.stream()
                    .filter(op -> op.remainingOperations().contains(TransactionOperation.TRANSFORM_HEADER))
                    .collect(ImmutableList.toImmutableList());
            positionTransformers = operators.stream()
                    .filter(op -> op.remainingOperations().contains(TransactionOperation.TRANSFORM_POSITION))
                    .collect(ImmutableList.toImmutableList());
            dataTransformers = operators.stream()
                    .filter(op -> op.remainingOperations().contains(TransactionOperation.TRANSFORM_DATA))
                    .collect(ImmutableList.toImmutableList());
            targetTransformers = operators.stream()
                    .filter(op -> op.remainingOperations().contains(TransactionOperation.TRANSFORM_TARGET))
                    .collect(ImmutableList.toImmutableList());
            dataCreators = operators.stream()
                    .filter(op -> op.remainingOperations().contains(TransactionOperation.CREATE_DATA))
                    .collect(ImmutableList.toImmutableList());
            hasActingTransformers = ! (headerTransformers.isEmpty() && positionTransformers.isEmpty() && dataTransformers.isEmpty() && targetTransformers.isEmpty() && dataCreators.isEmpty());
        }

        public ImmutableList<ITransactionOperator> getHeaderTransformers() {
            return headerTransformers;
        }

        public ImmutableList<ITransactionOperator> getPositionTransformers() {
            return positionTransformers;
        }

        public ImmutableList<ITransactionOperator> getDataTransformers() {
            return dataTransformers;
        }

        public ImmutableList<ITransactionOperator> getDataCreators() {
            return dataCreators;
        }

        public ImmutableList<ITransactionOperator> getTargetTransformers() {
            return targetTransformers;
        }

        public boolean hasActingTransformers() {
            return hasActingTransformers;
        }
    }
}
