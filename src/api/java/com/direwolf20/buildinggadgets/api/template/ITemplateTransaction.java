package com.direwolf20.buildinggadgets.api.template;

import com.direwolf20.buildinggadgets.api.exceptions.TransactionExecutionException;
import net.minecraft.util.math.BlockPos;

/**
 * This class represents a Transaction used for modifying {@link ITemplate} in more advanced ways than just translating the {@link ITemplate}
 * to some other location. It has therefore the following responsibilities:
 * <ul>
 *     <li>Provide a possibility for registering {@link ITransactionOperator} which specify the actual work to be performed by this Transaction.</li>
 *     <li>Provide a possibility to execute the {@link ITransactionOperator}'s as efficiently as possible for the backing {@link ITemplate}.</li>
 * </ul><br>
 * For more detailed Information about how the {@code ITemplateTransaction} will be executed see {@link #execute()}.
 */
public interface ITemplateTransaction {
    /**
     * Registers the given {@link ITransactionOperator} with this {@code ITemplateTransaction}.
     * The passed in {@link ITransactionOperator} may be a duplicate.<br>
     * <b>This Method does not execute the {@link ITransactionOperator}, see {@link #execute()} for more Information.</b>
     * @param operator The {@link ITransactionOperator} to register with this {@code ITemplateTransaction}
     * @throws NullPointerException if faced by an null value
     */
    public void operate(ITransactionOperator operator);

    /**
     * This Method executes all via {@link #operate(ITransactionOperator)} specified {@link ITransactionOperator}'s in the order in which they were specified.
     * It is recommended that the implementation would iterates over the Contents of the backing {@link ITemplate} only once and executes the
     * {@link ITransactionOperator} on each position as specified. Further optimization might be possible by taking {@link ITransactionOperator#characteristics()}
     * into account which describe, what kind of Operations a given {@link ITransactionOperator} performs. The required execution order for the specified
     * Operations is as follows:
     * <ol>
     *     <li>Call {@link ITransactionOperator#createPos()} until it returns null. The resulting positions must exist in the resulting {@link ITemplate}.</li>
     *     <li>Call {@link ITransactionOperator#createDataForPos(BlockPos)} for each Position previously returned by {@link ITransactionOperator#createPos()}</li>
     *     <li>Call {@link ITransactionOperator#transformData(BlockData)} <b>for each BlockData in the {@link ITemplate}</b></li>
     *     <li>Call {@link ITransactionOperator#transformPos(BlockPos, BlockData)} <b>for each Position in the {@link ITemplate}</b>.
     *         If the Method returns null then this position may not show up in the resulting Template, even if it was previously created via {@link ITransactionOperator#createPos()}.</li>
     * </ol>
     * @return The transformed Template, as specified by the passed in {@link ITransactionOperator}'s. May be the original or a new instance.
     * @throws com.direwolf20.buildinggadgets.api.exceptions.ConcurrentTransactionExecutionException If this Method is attempted to be used concurrently, but
     *         concurrent execution is not supported by this {@code ITemplateTransaction}
     * @throws com.direwolf20.buildinggadgets.api.exceptions.OperatorExecutionFailedException When an {@link ITransactionOperator} throws an {@link Exception}
     * @throws TransactionExecutionException On any encountered Exception
     * @throws UnsupportedOperationException if {@code execute} has already been called on this {@code ITemplateTransaction}
     */
    public ITemplate execute() throws TransactionExecutionException;
}
