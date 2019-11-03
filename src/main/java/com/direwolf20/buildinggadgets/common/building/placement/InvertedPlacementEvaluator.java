package com.direwolf20.buildinggadgets.common.building.placement;

import com.direwolf20.buildinggadgets.common.building.PlacementTarget;
import com.direwolf20.buildinggadgets.common.building.view.BuildContext;
import com.google.common.collect.AbstractIterator;

import java.util.Iterator;

public class InvertedPlacementEvaluator implements Iterable<PlacementTarget> {
    private final Iterable<PlacementTarget> iterable;
    private final PlacementChecker checker;
    private final BuildContext context;

    public InvertedPlacementEvaluator(Iterable<PlacementTarget> iterable, PlacementChecker checker, BuildContext context) {
        this.iterable = iterable;
        this.checker = checker;
        this.context = context;
    }

    @Override
    public Iterator<PlacementTarget> iterator() {
        return new AbstractIterator<PlacementTarget>() {
            private final Iterator<PlacementTarget> other = iterable.iterator();

            @Override
            protected PlacementTarget computeNext() {
                PlacementTarget next = null;
                while (other.hasNext() && next == null) {
                    next = other.next();
                    if (checker.checkPosition(context, next, false)) //if it is valid: skip
                        next = null;
                }
                if (! other.hasNext() && next == null)
                    return endOfData();
                return next;
            }
        };
    }
}
