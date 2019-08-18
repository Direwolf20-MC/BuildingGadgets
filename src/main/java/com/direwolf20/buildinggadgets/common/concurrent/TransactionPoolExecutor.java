package com.direwolf20.buildinggadgets.common.concurrent;

import com.direwolf20.buildinggadgets.api.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.api.exceptions.TransactionExecutionException;
import com.direwolf20.buildinggadgets.api.template.ITemplate;
import com.direwolf20.buildinggadgets.api.template.transaction.ITemplateTransaction;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import jdk.internal.jline.internal.Nullable;

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

    private final ListeningExecutorService executor;

    TransactionPoolExecutor() {
        ThreadPoolExecutor core = new ThreadPoolExecutor(2, 16, 1, TimeUnit.MINUTES, new SynchronousQueue<>());
        core.allowCoreThreadTimeOut(true);
        executor = MoreExecutors.listeningDecorator(core);
    }

    public boolean submitTask(Runnable task, Runnable completionListener) {
        try {
            executor.submit(task).addListener(completionListener, ServerTickingExecutor.INSTANCE);
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
        new ServerTickingScheduler(new TimeOutSupplier(ttl) {
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
                BuildingGadgets.LOG.error("Template seems to be busy. Aborting execution attempt.");
                completionListener.accept(res, null);
            }
        });
    }
}
