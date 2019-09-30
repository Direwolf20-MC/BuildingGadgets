package com.direwolf20.buildinggadgets.common.util.tools.building;

import com.direwolf20.buildinggadgets.api.building.PlacementTarget;
import com.direwolf20.buildinggadgets.api.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.api.building.view.IBuildView;
import com.direwolf20.buildinggadgets.common.util.inventory.IItemIndex;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.Spliterator;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

public class InvertedPlacementEvaluator extends PlacementEvaluator {
    public InvertedPlacementEvaluator(
            IBuildView view,
            LazyOptional<IEnergyStorage> energyCap,
            ToIntFunction<PlacementTarget> energyFun,
            IItemIndex index, BiPredicate<IBuildContext, PlacementTarget> placeCheck,
            boolean firePlaceEvents,
            boolean giveBackItems) {
        super(view, energyCap, energyFun, index, placeCheck, firePlaceEvents, giveBackItems);
    }

    @Override
    public Spliterator<PlacementTarget> spliterator() {
        return new InvertedEvaluatingSpliterator(getView().spliterator());
    }

    @Override
    public IBuildView copy() {
        return new InvertedPlacementEvaluator(getView(), getEnergyCap(), getEnergyFun(), getIndex(), getPlaceCheck(), firesPlaceEvents(), givesBackItems());
    }

    private final class InvertedEvaluatingSpliterator extends EvaluatingSpliterator {
        public InvertedEvaluatingSpliterator(Spliterator<PlacementTarget> other) {
            super(other);
        }

        @Override
        protected boolean advance(PlacementTarget object, Consumer<? super PlacementTarget> action) {
            if (! super.advance(object, t -> {})) {
                action.accept(object);
                return true;
            }
            return false;
        }
    }
}
