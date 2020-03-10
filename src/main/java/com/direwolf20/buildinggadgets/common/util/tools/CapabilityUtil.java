package com.direwolf20.buildinggadgets.common.util.tools;

import com.direwolf20.buildinggadgets.common.util.exceptions.CapabilityNotPresentException;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.function.*;

public final class CapabilityUtil {
    private CapabilityUtil() {}

    public static <C> double returnDoubleIfPresent(ICapabilityProvider provider, Capability<C> capability, ToDoubleFunction<C> function, DoubleSupplier def) {
        LazyOptional<C> cap = provider.getCapability(capability);
        if (cap.isPresent())
            return function.applyAsDouble(cap.orElseThrow(CapabilityNotPresentException::new));
        return def.getAsDouble();
    }

    public static <C> int returnIntIfPresent(ICapabilityProvider provider, Capability<C> capability, ToIntFunction<C> function, IntSupplier def) {
        LazyOptional<C> cap = provider.getCapability(capability);
        if (cap.isPresent())
            return function.applyAsInt(cap.orElseThrow(CapabilityNotPresentException::new));
        return def.getAsInt();
    }

    public static <C> boolean returnBooleanIfPresent(ICapabilityProvider provider, Capability<C> capability, Predicate<C> function, BooleanSupplier def) {
        LazyOptional<C> cap = provider.getCapability(capability);
        if (cap.isPresent())
            return function.test(cap.orElseThrow(CapabilityNotPresentException::new));
        return def.getAsBoolean();
    }


    public static final class EnergyUtil {
        private EnergyUtil() {}

        public static double returnDoubleIfPresent(ICapabilityProvider provider, ToDoubleFunction<IEnergyStorage> function, DoubleSupplier def) {
            return CapabilityUtil.returnDoubleIfPresent(provider, CapabilityEnergy.ENERGY, function, def);
        }

        public static int returnIntIfPresent(ICapabilityProvider provider, ToIntFunction<IEnergyStorage> function, IntSupplier def) {
            return CapabilityUtil.returnIntIfPresent(provider, CapabilityEnergy.ENERGY, function, def);
        }

        public static boolean returnBooleanIfPresent(ICapabilityProvider provider, Predicate<IEnergyStorage> function, BooleanSupplier def) {
            return CapabilityUtil.returnBooleanIfPresent(provider, CapabilityEnergy.ENERGY, function, def);
        }
    }
}
