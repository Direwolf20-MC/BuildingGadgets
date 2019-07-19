package com.direwolf20.buildinggadgets.api.template.transaction;

import com.direwolf20.buildinggadgets.api.building.IPlacementSequence;
import com.direwolf20.buildinggadgets.api.building.Region;

/**
 * Represents some context information during execution of an {@link ITemplateTransaction}
 */
public interface ITransactionExecutionContext {
    // Todo add getter for Template Boundingbox

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
}
