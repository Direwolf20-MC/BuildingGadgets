package com.direwolf20.buildinggadgets.api.template.transaction;

/**
 * Represents some context information during execution of an {@link ITemplateTransaction}
 */
public interface ITransactionExecutionContext {
    //Todo add getter for Template Boundingbox
    int getEstimatedTemplateSize();
}
