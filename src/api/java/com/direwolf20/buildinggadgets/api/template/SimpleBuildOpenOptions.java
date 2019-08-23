package com.direwolf20.buildinggadgets.api.template;

import com.direwolf20.buildinggadgets.api.building.view.IBuildContext;

import java.util.Objects;

public final class SimpleBuildOpenOptions implements IBuildOpenOptions {
    public static Builder builder() {
        return new Builder();
    }

    public static Builder builderCopyOf(IBuildOpenOptions openOptions) {
        return builder()
                .context(openOptions.getContext())
                .openType(openOptions.getOpenType());
    }

    public static SimpleBuildOpenOptions copyOf(IBuildOpenOptions openOptions) {
        return SimpleBuildOpenOptions.builderCopyOf(openOptions).build();
    }

    public static SimpleBuildOpenOptions withContext(IBuildContext context) {
        return builder().context(context).build();
    }

    private final IBuildContext buildContext;
    private final OpenType openType;

    private SimpleBuildOpenOptions(IBuildContext buildContext, OpenType openType) {
        this.buildContext = buildContext;
        this.openType = openType;
    }

    @Override
    public IBuildContext getContext() {
        return buildContext;
    }

    @Override
    public OpenType getOpenType() {
        return openType;
    }

    public static final class Builder {
        private IBuildContext buildContext;
        private OpenType openType;

        private Builder() {
            buildContext = null;
            openType = null;
        }

        public Builder context(IBuildContext context) {
            this.buildContext = context;
            return this;
        }

        public Builder openType(OpenType openType) {
            this.openType = openType;
            return this;
        }

        public SimpleBuildOpenOptions build() {
            return new SimpleBuildOpenOptions(
                    Objects.requireNonNull(buildContext, "Cannot have a null IBuildContext!"),
                    openType != null ? openType : OpenType.DEFAULT);
        }
    }
}
