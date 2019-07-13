package com.direwolf20.buildinggadgets.common.capability;


import java.util.function.IntSupplier;

public abstract class CappedEnergyStorage extends ConfigEnergyStorage {
    private int curReceiveCap;
    private int curExtractCap;

    public CappedEnergyStorage(IntSupplier capacitySupplier, IntSupplier extractSupplier, IntSupplier receiveSupplier) {
        super(capacitySupplier, extractSupplier, receiveSupplier);
        resetExtractCap();
        resetReceiveCap();
    }

    public void resetExtractCap() {
        this.curExtractCap = getExtractSupplier().getAsInt();
    }

    public void resetReceiveCap() {
        this.curReceiveCap = getReceiveSupplier().getAsInt();
    }

    @Override
    protected int evaluateEnergyExtracted(int maxExtract, boolean simulate) {
        int extract = Math.min(super.evaluateEnergyExtracted(maxExtract, simulate), curExtractCap);
        if (! simulate)
            curExtractCap -= extract;
        return extract;
    }

    @Override
    protected int evaluateEnergyReceived(int maxReceive, boolean simulate) {
        int receive = Math.min(super.evaluateEnergyReceived(maxReceive, simulate), curReceiveCap);
        if (! simulate)
            curReceiveCap -= receive;
        return receive;
    }
}
