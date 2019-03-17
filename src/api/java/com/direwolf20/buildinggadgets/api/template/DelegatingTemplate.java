package com.direwolf20.buildinggadgets.api.template;

import com.direwolf20.buildinggadgets.api.abstraction.IUniqueItem;
import com.direwolf20.buildinggadgets.api.template.building.IBuildContext;
import com.direwolf20.buildinggadgets.api.template.building.ITemplateView;
import com.direwolf20.buildinggadgets.api.template.serialisation.ITemplateSerializer;
import com.direwolf20.buildinggadgets.api.template.transaction.ITemplateTransaction;
import com.google.common.collect.Multiset;

import javax.annotation.Nullable;

public class DelegatingTemplate implements ITemplate {
    public static ITemplate unwrap(ITemplate template) {
        if (template instanceof DelegatingTemplate)
            return unwrap(((DelegatingTemplate) template).getDelegate());
        return template;
    }

    public static DelegatingTemplate createUnwrapped(ITemplate template) {
        return new DelegatingTemplate(unwrap(template));
    }

    private ITemplate delegate;

    public DelegatingTemplate(ITemplate delegate) {
        this.delegate = delegate;
    }

    public DelegatingTemplate() {
        this(ImmutableTemplate.create());
    }

    @Override
    @Nullable
    public ITemplateTransaction startTransaction() {
        return delegate.startTransaction();
    }

    @Override
    public Multiset<IUniqueItem> estimateRequiredItems() {
        return delegate.estimateRequiredItems();
    }

    @Override
    public ITemplateSerializer getSerializer() {
        return delegate.getSerializer();
    }

    @Override
    public ITemplateView createViewInContext(IBuildContext buildContext) {
        return delegate.createViewInContext(buildContext);
    }

    @Override
    public int estimateSize() {
        return delegate.estimateSize();
    }

    public ITemplate getDelegate() {
        return delegate;
    }

    protected void setDelegate(ITemplate delegate) {
        this.delegate = delegate;
    }
}
