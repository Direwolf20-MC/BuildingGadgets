package com.direwolf20.buildinggadgets.common.util.tools.building;

import com.direwolf20.buildinggadgets.api.building.PlacementTarget;
import com.direwolf20.buildinggadgets.api.building.Region;
import com.direwolf20.buildinggadgets.api.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.api.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.api.building.view.IBuildView;
import com.direwolf20.buildinggadgets.api.exceptions.TemplateException;
import com.direwolf20.buildinggadgets.api.materials.MaterialList;
import com.direwolf20.buildinggadgets.api.util.CommonUtils;
import com.direwolf20.buildinggadgets.api.util.DelegatingSpliterator;
import com.direwolf20.buildinggadgets.common.util.exceptions.CapabilityNotPresentException;
import com.direwolf20.buildinggadgets.common.util.inventory.IItemIndex;
import com.direwolf20.buildinggadgets.common.util.inventory.MatchResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;

import java.util.Spliterator;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

public class PlacementEvaluator implements IBuildView {
    private final IBuildView view;
    private final LazyOptional<IEnergyStorage> energyCap;
    private final ToIntFunction<PlacementTarget> energyFun;
    private final IItemIndex index;
    private final boolean firePlaceEvents;
    private final boolean giveBackItems;
    private final BiPredicate<IBuildContext, PlacementTarget> placeCheck;

    public PlacementEvaluator(
            IBuildView view,
            LazyOptional<IEnergyStorage> energyCap,
            ToIntFunction<PlacementTarget> energyFun,
            IItemIndex index,
            BiPredicate<IBuildContext, PlacementTarget> placeCheck,
            boolean firePlaceEvents,
            boolean giveBackItems) {
        this.view = view;
        this.energyCap = energyCap;
        this.energyFun = energyFun;
        this.index = index;
        this.firePlaceEvents = firePlaceEvents;
        this.giveBackItems = giveBackItems;
        this.placeCheck = placeCheck;
    }

    @Override
    public Spliterator<PlacementTarget> spliterator() {
        return new EvaluatingSpliterator(view.spliterator());
    }

    @Override
    public PlacementEvaluator translateTo(BlockPos pos) {
        view.translateTo(pos);
        return this;
    }

    @Override
    public int estimateSize() {
        return view.estimateSize();
    }

    @Override
    public void close() throws TemplateException {
        view.close();
    }

    @Override
    public IBuildView copy() {
        return new PlacementEvaluator(view.copy(), energyCap, energyFun, index, placeCheck, firePlaceEvents, giveBackItems);
    }

    @Override
    public IBuildContext getContext() {
        return view.getContext();
    }

    @Override
    public Region getBoundingBox() {
        return view.getBoundingBox();
    }

    @Override
    public boolean mayContain(int x, int y, int z) {
        return view.mayContain(x, y, z);
    }

    public IBuildView getView() {
        return view;
    }

    public LazyOptional<IEnergyStorage> getEnergyCap() {
        return energyCap;
    }

    public ToIntFunction<PlacementTarget> getEnergyFun() {
        return energyFun;
    }

    public IItemIndex getIndex() {
        return index;
    }

    public boolean firesPlaceEvents() {
        return firePlaceEvents;
    }

    public boolean givesBackItems() {
        return giveBackItems;
    }

    public BiPredicate<IBuildContext, PlacementTarget> getPlaceCheck() {
        return placeCheck;
    }

    class EvaluatingSpliterator extends DelegatingSpliterator<PlacementTarget, PlacementTarget> {
        public EvaluatingSpliterator(Spliterator<PlacementTarget> other) {
            super(other);
        }

        @Override
        protected boolean advance(PlacementTarget object, Consumer<? super PlacementTarget> action) {
            if (! placeCheck.test(getContext(), object))
                return false;
            boolean useEnergy = getContext().getBuildingPlayer() == null || ! getContext().getBuildingPlayer().isCreative();
            int energy = energyFun.applyAsInt(object);
            IEnergyStorage storage = energyCap.orElseThrow(CapabilityNotPresentException::new);
            if (useEnergy && storage.extractEnergy(energy, true) != energy)
                return false;
            RayTraceResult targetRayTrace = null;
            if (getContext().getBuildingPlayer() != null) {
                PlayerEntity player = getContext().getBuildingPlayer();
                targetRayTrace = CommonUtils.fakeRayTrace(player.posX, player.posY, player.posZ, object.getPos());
            }
            MaterialList materials = object.getRequiredMaterials(getContext(), targetRayTrace);
            MatchResult match = index.tryMatch(materials);
            if (! match.isSuccess())
                return false;
            BlockSnapshot blockSnapshot = BlockSnapshot.getBlockSnapshot(getContext().getWorld(), object.getPos());
            boolean isAir = blockSnapshot.getCurrentBlock().isAir(getContext().getWorld(), object.getPos());
            if (firePlaceEvents && ForgeEventFactory.onBlockPlace(getContext().getBuildingPlayer(), blockSnapshot, Direction.UP))
                return false;
            if (! isAir) {
                if (firePlaceEvents) {
                    BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(getContext().getWorld().getWorld(),
                            object.getPos(), blockSnapshot.getCurrentBlock(),
                            getContext().getBuildingPlayer());
                    if (MinecraftForge.EVENT_BUS.post(e))
                        return false;
                }
                if (giveBackItems)
                    index.insert(TileSupport.createTileData(getContext().getWorld().getTileEntity(object.getPos()))
                            .getRequiredItems(getContext(), blockSnapshot.getCurrentBlock(), null, object.getPos()).iterator().next());
            }
            if ((! useEnergy || storage.extractEnergy(energy, false) == energy) && index.applyMatch(match)) {
                action.accept(object);
                return true;
            }
            return false;
        }

        @Override
        public Spliterator<PlacementTarget> trySplit() {
            return null;
        }
    }
}
