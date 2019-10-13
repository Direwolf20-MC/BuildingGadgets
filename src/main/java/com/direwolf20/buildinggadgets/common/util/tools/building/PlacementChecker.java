package com.direwolf20.buildinggadgets.common.util.tools.building;

import com.direwolf20.buildinggadgets.api.building.PlacementTarget;
import com.direwolf20.buildinggadgets.api.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.api.building.view.IBuildContext;
import com.direwolf20.buildinggadgets.api.materials.MaterialList;
import com.direwolf20.buildinggadgets.api.materials.inventory.IUniqueObject;
import com.direwolf20.buildinggadgets.api.util.CommonUtils;
import com.direwolf20.buildinggadgets.common.util.exceptions.CapabilityNotPresentException;
import com.direwolf20.buildinggadgets.common.util.inventory.IItemIndex;
import com.direwolf20.buildinggadgets.common.util.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.util.inventory.MatchResult;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;

import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;

public final class PlacementChecker {
    private final LazyOptional<IEnergyStorage> energyCap;
    private final ToIntFunction<PlacementTarget> energyFun;
    private final IItemIndex index;
    private final boolean firePlaceEvents;
    private final BiPredicate<IBuildContext, PlacementTarget> placeCheck;

    public PlacementChecker(LazyOptional<IEnergyStorage> energyCap, ToIntFunction<PlacementTarget> energyFun, IItemIndex index, BiPredicate<IBuildContext, PlacementTarget> placeCheck, boolean firePlaceEvents) {
        this.energyCap = energyCap;
        this.energyFun = energyFun;
        this.index = index;
        this.firePlaceEvents = firePlaceEvents;
        this.placeCheck = placeCheck;
    }

    public CheckResult checkPositionWithResult(IBuildContext context, PlacementTarget target, boolean giveBackItems) {
        if (! placeCheck.test(context, target))
            return new CheckResult(MatchResult.failure(), ImmutableMultiset.of(), - 1, false, false);
        int energy = energyFun.applyAsInt(target);
        Multiset<IUniqueObject<?>> insertedItems = ImmutableMultiset.of();
        boolean isCreative = context.getBuildingPlayer() != null && context.getBuildingPlayer().isCreative();
        IEnergyStorage storage = energyCap.orElseThrow(CapabilityNotPresentException::new);
        if (! isCreative && storage.extractEnergy(energy, true) != energy)
            return new CheckResult(MatchResult.failure(), insertedItems, energy, false, false);
        RayTraceResult targetRayTrace = null;
        if (context.getBuildingPlayer() != null) {
            PlayerEntity player = context.getBuildingPlayer();
            targetRayTrace = CommonUtils.fakeRayTrace(player.posX, player.posY, player.posZ, target.getPos());
        }
        MaterialList materials = target.getRequiredMaterials(context, targetRayTrace);
        MatchResult match = index.tryMatch(materials);
        boolean usePaste = false;
        if (! match.isSuccess()) {
            match = index.tryMatch(InventoryHelper.PASTE_LIST);
            if (! match.isSuccess())
                return new CheckResult(match, insertedItems, energy, false, false);
            usePaste = true;
        }
        BlockSnapshot blockSnapshot = BlockSnapshot.getBlockSnapshot(context.getWorld(), target.getPos());
        boolean isAir = blockSnapshot.getCurrentBlock().isAir(context.getWorld(), target.getPos());
        if (firePlaceEvents && ForgeEventFactory.onBlockPlace(context.getBuildingPlayer(), blockSnapshot, Direction.UP))
            return new CheckResult(match, insertedItems, energy, false, usePaste);
        if (! isAir) {
            if (firePlaceEvents) {
                BlockEvent.BreakEvent e = new BlockEvent.BreakEvent(context.getWorld().getWorld(),
                        target.getPos(), blockSnapshot.getCurrentBlock(),
                        context.getBuildingPlayer());
                if (MinecraftForge.EVENT_BUS.post(e))
                    return new CheckResult(match, insertedItems, energy, false, usePaste);
            }
            if (giveBackItems) {
                insertedItems = TileSupport.createTileData(context.getWorld().getTileEntity(target.getPos()))
                        .getRequiredItems(context, blockSnapshot.getCurrentBlock(), null, target.getPos()).iterator().next();
                index.insert(insertedItems);
            }
        }
        boolean success = true;
        if (! isCreative)
            success = storage.extractEnergy(energy, false) == energy;
        success = success && index.applyMatch(match);
        return new CheckResult(match, insertedItems, energy, success, usePaste);
    }

    public boolean checkPosition(IBuildContext context, PlacementTarget target, boolean giveBackItems) {
        return checkPositionWithResult(context, target, giveBackItems).isSuccess();
    }

    public static final class CheckResult {
        private final MatchResult match;
        private final Multiset<IUniqueObject<?>> insertedItems;
        private final int usedEnergy;
        private final boolean success;
        private final boolean usingPaste;

        private CheckResult(MatchResult match, Multiset<IUniqueObject<?>> insertedItems, int usedEnergy, boolean success, boolean usingPaste) {
            this.match = match;
            this.insertedItems = insertedItems;
            this.usedEnergy = usedEnergy;
            this.success = success;
            this.usingPaste = usingPaste;
        }

        public Multiset<IUniqueObject<?>> getInsertedItems() {
            return insertedItems;
        }

        public MatchResult getMatch() {
            return match;
        }

        public int getUsedEnergy() {
            return usedEnergy;
        }

        public boolean isSuccess() {
            return success;
        }

        public boolean isUsingPaste() {
            return usingPaste;
        }
    }
}
