package com.direwolf20.buildinggadgets.common.concurrent;

import com.direwolf20.buildinggadgets.api.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.api.exceptions.TransactionExecutionException;
import com.direwolf20.buildinggadgets.api.template.ITemplate;
import com.direwolf20.buildinggadgets.api.template.transaction.ITemplateTransaction;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;

import javax.annotation.Nullable;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public enum TransactionPoolExecutor {
    INSTANCE;

    public enum CompletionResult {
        SUBMIT_FAILED,
        EXECUTE_FAILED,
        SUCCESS
    }

    private final ThreadPoolExecutor executor;

    TransactionPoolExecutor() {
        executor = new ThreadPoolExecutor(2, 16, 1, TimeUnit.MINUTES, new SynchronousQueue<>());
        executor.allowCoreThreadTimeOut(true);
    }

    public boolean submitTask(Runnable task) {
        return submitTask(task, () -> {});
    }

    public boolean submitTask(Runnable task, Runnable completionListener) {
        try {
            executor.execute(() -> {
                task.run();
                completionListener.run();
            });
            return true;
        } catch (RejectedExecutionException e) {
            return false;
        }
    }

    public void tryExecuteTransaction(ITemplate template, Consumer<ITemplateTransaction> setupFunction, int ttl) {
        tryExecuteTransaction(template, setupFunction, ttl, null);
    }

    public void tryExecuteTransaction(ITemplate template, Consumer<ITemplateTransaction> setupFunction, int ttl, @Nullable IBuildContext context) {
        tryExecuteTransaction(template, setupFunction, (cr, tr) -> {}, ttl, context);
    }

    public void tryExecuteTransaction(ITemplate template, Consumer<ITemplateTransaction> setupFunction,
                                      BiConsumer<CompletionResult, ITemplateTransaction> completionListener, int ttl) {
        tryExecuteTransaction(template, setupFunction, completionListener, ttl, null);
    }

    public void tryExecuteTransaction(ITemplate template, Consumer<ITemplateTransaction> setupFunction, BiConsumer<CompletionResult, ITemplateTransaction> completionListener,
                                      int ttl, @Nullable IBuildContext context) {
        ServerTickingScheduler.runTicked(new TimeOutSupplier(ttl) {
            private CompletionResult res = CompletionResult.SUBMIT_FAILED;

            @Override
            protected boolean run() {
                ITemplateTransaction transaction = template.startTransaction();
                return transaction == null || submitTask(
                        () -> {
                            setupFunction.accept(transaction);
                            try {
                                transaction.execute(context);
                                res = CompletionResult.SUCCESS;
                                BuildingGadgets.LOG.trace("Successfully completed async Transaction.");
                            } catch (TransactionExecutionException e) {
                                BuildingGadgets.LOG.warn("Failed to execute Transaction! Execution is deemed impossible!", e);
                                res = CompletionResult.EXECUTE_FAILED;
                            }
                        }, () -> {
                            completionListener.accept(res, transaction);
                        }
                );
            }

            @Override
            protected void onTimeout() {
                BuildingGadgets.LOG.debug("Template seems to be busy. Aborting execution attempt.");
                completionListener.accept(res, null);
            }
        });
    }
}
