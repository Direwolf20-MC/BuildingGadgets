package com.direwolf20.buildinggadgets.common.util.tools;

import com.direwolf20.buildinggadgets.common.capability.IPrivateEnergy;
import com.direwolf20.buildinggadgets.common.tainted.Tainted;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * An {@link IEnergyStorage} which delegates through to another implementation, but only ever performs simulate actions on the backing storage.
 * To provide similar behaviour to regular extractions/insertions to the backing implementation this uses an accumulated buffer and always simulates
 * insertion/extraction of the accumulated buffer.
 * <p>
 * Notice that reported behaviour will be different from the behaviour of the underlying storage (if no extractions are performed), if and only if there
 * are insert/extract limits on the backing storage. In that case this implementation will only work as long as the accumulated buffer is smaller then
 * the defined limits.
 */

@Tainted(reason = "So stupid. The cap supports simulating by default. This is pointless overhead")
public final class SimulateEnergyStorage implements IPrivateEnergy {
    private final IEnergyStorage other;
    private int energyChanged;

    public SimulateEnergyStorage(IEnergyStorage other) {
        this.other = other;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = other.receiveEnergy(maxReceive + energyChanged, true);
        int dif = Math.max(received - energyChanged, 0);
        if (! simulate)
            energyChanged += dif;
        return dif;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int extractPower(int maxExtract, boolean simulate) {
        int extracted = other.extractEnergy(maxExtract - energyChanged, true);
        int dif = Math.max(extracted + energyChanged, 0);
        if (! simulate)
            energyChanged -= dif;
        return dif;
    }

    @Override
    public int getEnergyStored() {
        return Math.max(other.getEnergyStored() + energyChanged, 0);
    }

    @Override
    public int getMaxEnergyStored() {
        return other.getMaxEnergyStored();
    }

    @Override
    public boolean canExtract() {
        return other.canExtract();
    }

    @Override
    public boolean canReceive() {
        return other.canReceive();
    }
}
