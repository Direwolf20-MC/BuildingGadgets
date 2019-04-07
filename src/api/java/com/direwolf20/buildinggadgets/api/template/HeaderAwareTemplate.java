package com.direwolf20.buildinggadgets.api.template;

import com.direwolf20.buildinggadgets.api.abstraction.UniqueItem;
import com.direwolf20.buildinggadgets.api.template.serialisation.TemplateHeader;
import com.google.common.collect.Multiset;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * {@link ITemplate} Decorator exposing the {@link ITemplate Template's} {@link TemplateHeader} directly as instance Methods.
 */
public class HeaderAwareTemplate extends DelegatingTemplate {
    private TemplateHeader header;

    public HeaderAwareTemplate(ITemplate delegate) {
        super(delegate);
        header = getDelegate().getSerializer().createHeaderFor(getDelegate());
    }

    /**
     * @see TemplateHeader#getName()
     */
    @Nullable
    public String getName() {
        return header.getName();
    }

    /**
     * @see TemplateHeader#getAuthor()
     */
    @Nullable
    public String getAuthor() {
        return header.getAuthor();
    }

    /**
     * @see TemplateHeader#getRequiredItems()
     */
    @Nonnull
    public Multiset<UniqueItem> getRequiredItems() {
        return header.getRequiredItems();
    }

    /**
     * @see TemplateHeader#getBoundingBox()
     */
    @Nonnull
    public BlockPos getBoundingBox() {
        return header.getBoundingBox();
    }
}
