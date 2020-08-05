package com.direwolf20.buildinggadgets.common.schema.template;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.Vec3i;

import java.util.Iterator;

public final class TranslationWrapper implements Iterable<TemplateData> {
    private final Vec3i relTranslation;
    private final Iterable<TemplateData> delegate;

    public TranslationWrapper(Vec3i relTranslation, Iterable<TemplateData> delegate) {
        this.relTranslation = relTranslation;
        this.delegate = delegate;
    }

    @Override
    public Iterator<TemplateData> iterator() {
        return new Iterator<TemplateData>() {
            private final Iterator<TemplateData> it = delegate.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public TemplateData next() {
                TemplateData other = it.next();
                BlockPos pos = other.getPos();
                if (pos instanceof Mutable)
                    ((Mutable) pos).setPos(pos.getX() + relTranslation.getX(), pos.getY() + relTranslation.getY(), pos.getZ() + relTranslation.getZ());
                else
                    other.setInformation(pos.add(relTranslation), other.getState(), other.getTileNbt());
                return other;
            }
        };
    }
}
