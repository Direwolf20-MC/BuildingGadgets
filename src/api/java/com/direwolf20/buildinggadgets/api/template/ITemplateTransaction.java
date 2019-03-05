package com.direwolf20.buildinggadgets.api.template;

public interface ITemplateTransaction {
    public void operate(ITransactionOperator operator);

    public void execute();
}
