package com.direwolf20.buildinggadgets.api.template.transaction;

import com.direwolf20.buildinggadgets.api.building.IPlacementSequence;
import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.serialisation.TemplateHeader;
import com.direwolf20.buildinggadgets.api.template.ITemplate;

import javax.annotation.Nullable;

/**
 * Represents some context information during execution of an {@link ITemplateTransaction}.
 * Notice that this Information is required to be updated between different TransactionSteps, but does not require
 * to be updated between different {@link ITransactionOperator ITransactionOperators}. This implies that the Context
 * should always represent the state of the resulting {@link ITemplate} before the current TransactionStep.
 * An {@link ITransactionOperator} may for example still see positions which are outside of {@link #getBoundingBox()} because
 * a previous {@link ITransactionOperator} moved them there and the {@link #getBoundingBox()} was not updated yet.
 */
public interface ITransactionExecutionContext {

    /**
     * @return The estimated amount of {@link com.direwolf20.buildinggadgets.api.building.PlacementTarget}'s produced by the
     * {@link com.direwolf20.buildinggadgets.api.template.ITemplate} which owns this {@code ITransactionExecutionContext}.
     */
    int getEstimatedTemplateSize();

    /**
     * @return A {@link Region} enclosing the {@link com.direwolf20.buildinggadgets.api.template.ITemplate} owning this {@code ITransactionExecutionContext}.
     * @see IPlacementSequence#getBoundingBox()
     */
    Region getBoundingBox();

    /**
     * @return Optionally a {@link TemplateHeader} for the Template under construction.
     * @see com.direwolf20.buildinggadgets.api.serialisation.ITemplateSerializer#createHeaderFor(ITemplate)
     */
    @Nullable
    TemplateHeader getHeader();
}
