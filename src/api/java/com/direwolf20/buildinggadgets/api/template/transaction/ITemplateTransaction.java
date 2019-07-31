package com.direwolf20.buildinggadgets.api.template.transaction;

import com.direwolf20.buildinggadgets.api.building.BlockData;
import com.direwolf20.buildinggadgets.api.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.api.building.view.IBuildView;
import com.direwolf20.buildinggadgets.api.exceptions.TransactionExecutionException;
import com.direwolf20.buildinggadgets.api.serialisation.TemplateHeader;
import com.direwolf20.buildinggadgets.api.template.ITemplate;
import jdk.internal.jline.internal.Nullable;
import net.minecraft.util.math.BlockPos;

/**
 * This class represents a transaction used for modifying {@link ITemplate} in more advanced ways than just translating the {@link IBuildView}
 * to some other location. It has therefore the following responsibilities:
 * <ul>
 * <li>Provide a possibility for registering {@link ITransactionOperator} which specify the actual work to be performed by this Transaction.
 * <li>Provide a possibility to execute the {@link ITransactionOperator}'s as efficiently as possible for the backing {@link ITemplate}.
 * </ul><br>
 * For more detailed Information about how the {@code ITemplateTransaction} will be executed see {@link #execute()}.
 */
public interface ITemplateTransaction {
    /**
     * Registers the given {@link ITransactionOperator} with this {@code ITemplateTransaction}.
     * The passed in {@link ITransactionOperator} may be a duplicate.<br>
     * <b>This Method does not execute the {@link ITransactionOperator}, see {@link #execute()} for more Information.</b>
     * @param operator The {@link ITransactionOperator} to register with this {@code ITemplateTransaction}
     * @return itself to allow for Method chaining
     * @throws NullPointerException if faced with an null value
     */
    ITemplateTransaction operate(ITransactionOperator operator);

    /**
     * This Method executes all via {@link #operate(ITransactionOperator)} specified {@link ITransactionOperator}'s in the order in which they were specified.
     * It is recommended that the implementation iterates over the Contents of the backing {@link ITemplate} only once and executes the
     * {@link ITransactionOperator} on each position as specified. Further optimization might be possible by taking {@link ITransactionOperator#characteristics()}
     * into account which describe, what kind of Operations a given {@link ITransactionOperator} performs.
     * <p>
     * The required execution order for the specified Operations is as follows:
     * <ol>
     * <li>Call {@link ITransactionOperator#createPos(ITransactionExecutionContext)} until it returns null. The resulting positions must exist in the resulting {@link ITemplate}.
     * <li>Call {@link ITransactionOperator#createDataForPos(ITransactionExecutionContext, BlockPos)} for each Position previously returned by {@link ITransactionOperator#createPos(ITransactionExecutionContext)}
     * <li>Call {@link ITransactionOperator#transformData(ITransactionExecutionContext, BlockData)} <b>for each BlockData in the {@link ITemplate}.</b>
     * <li>Call {@link ITransactionOperator#transformPos(ITransactionExecutionContext, BlockPos, BlockData)} <b>for each Position in the {@link ITemplate}.</b>
     * <li>Call {@link ITransactionOperator#transformHeader(ITransactionExecutionContext, TemplateHeader)} for all {@link ITransactionOperator ITransactionOperators} with the
     *     {@link ITransactionOperator.Characteristic#TRANSFORM_HEADER} characteristic
     * </ol>
     * <p>
     * It is upon this method to create the {@link ITransactionExecutionContext} required to execute a given {@link ITransactionOperator}.
     * @param context {@link IBuildContext} intended to allow for efficient pre-evaluation of for example required Items. May be null.
     * @return The transformed Template, as specified by the passed in {@link ITransactionOperator}'s. May be the original or a new instance.
     * @throws com.direwolf20.buildinggadgets.api.exceptions.ConcurrentTransactionExecutionException If this method is attempted to be used concurrently, but
     *         concurrent execution is not supported by this {@code ITemplateTransaction}
     * @throws com.direwolf20.buildinggadgets.api.exceptions.OperatorExecutionFailedException If an {@link ITransactionOperator} throws an {@link Exception}
     * @throws com.direwolf20.buildinggadgets.api.exceptions.TransactionInvalidException If the {@code ITemplateTransaction} has already been executed.
     * @throws TransactionExecutionException If any other {@link Exception} is encountered or the implementation specifies additional subclasses
     * @implSpec Notice that an {@code ITemplateTransaction} is considered invalid as soon as {@code execute()} has been called.
     *         Any further calls should throw {@link UnsupportedOperationException}.
     * @implNote Note that {@link ITransactionOperator#createPos(ITransactionExecutionContext)} may produce Positions already contained in the backing {@link ITemplate}.
     *         This indicates that current Data should be replaced by whatever {@link ITransactionOperator#createDataForPos(ITransactionExecutionContext, BlockPos)} returns.
     * @implNote <br> Note that {@link ITransactionOperator#createDataForPos(ITransactionExecutionContext, BlockPos)} may also return null in cases where data is already present in the backing
     *         {@link ITemplate}. Returning null from this method, when no data is present for the given {@link BlockPos} is considered an error and
     *         should therefore throw an {@link TransactionExecutionException}.
     * @implNote <br> Note that {@link ITransactionOperator#transformData(ITransactionExecutionContext, BlockData)} and {@link ITransactionOperator#transformPos(ITransactionExecutionContext, BlockPos, BlockData)} may
     *         return null to indicate that a certain position or <b>all positions referenced by a specific {@link BlockData}</b> should be removed from
     *         the backing {@link ITemplate}.
     */
    ITemplate execute(@Nullable IBuildContext context) throws TransactionExecutionException;

    /**
     * Equivalent to calling {@code transaction.execute(null)}. Prefer {@link #execute(IBuildContext)} wherever it is possible to construct even a minimalistic
     * {@link IBuildContext} in order to allow the resulting {@link ITemplate} to perform caching of the required Items.
     *
     * @see #execute(IBuildContext)
     */
    default ITemplate execute() throws TransactionExecutionException {
        return execute(null);
    }
}
