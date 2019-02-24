package com.direwolf20.buildinggadgets.common.utils;

import com.direwolf20.buildinggadgets.common.utils.exceptions.CapabilityNotPresentException;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import java.util.function.*;

public final class CapabilityUtil {
    private CapabilityUtil() {}

    public static <C> double returnDoubleIfPresent(ICapabilityProvider provider, Capability<C> capability, ToDoubleFunction<C> function, final double def) {
        return returnDoubleIfPresent(provider, capability, function, () -> def);
    }

    public static <C> double returnDoubleIfPresent(ICapabilityProvider provider, Capability<C> capability, ToDoubleFunction<C> function, DoubleSupplier def) {
        LazyOptional<C> cap = provider.getCapability(capability);
        if (cap.isPresent())
            return function.applyAsDouble(cap.orElseThrow(CapabilityNotPresentException::new));
        return def.getAsDouble();
    }

    public static <C> int returnIntIfPresent(ICapabilityProvider provider, Capability<C> capability, ToIntFunction<C> function, final int def) {
        return returnIntIfPresent(provider, capability, function, () -> def);
    }

    public static <C> int returnIntIfPresent(ICapabilityProvider provider, Capability<C> capability, ToIntFunction<C> function, IntSupplier def) {
        LazyOptional<C> cap = provider.getCapability(capability);
        if (cap.isPresent())
            return function.applyAsInt(cap.orElseThrow(CapabilityNotPresentException::new));
        return def.getAsInt();
    }

    public static <C> boolean returnBooleanIfPresent(ICapabilityProvider provider, Capability<C> capability, Predicate<C> function, final boolean def) {
        return returnBooleanIfPresent(provider, capability, function, () -> def);
    }

    public static <C> boolean returnBooleanIfPresent(ICapabilityProvider provider, Capability<C> capability, Predicate<C> function, BooleanSupplier def) {
        LazyOptional<C> cap = provider.getCapability(capability);
        if (cap.isPresent())
            return function.test(cap.orElseThrow(CapabilityNotPresentException::new));
        return def.getAsBoolean();
    }

    public static <C, R> R returnIfPresent(ICapabilityProvider provider, Capability<C> capability, Function<C, R> function, final R def) {
        return returnIfPresent(provider, capability, function, (Supplier<R>) (() -> def)); //Cast needed, so that compiler doesn't complain... No idea why it is needed her, but not with the IntSupplier
    }

    public static <C, R> R returnIfPresent(ICapabilityProvider provider, Capability<C> capability, Function<C, R> function, Supplier<R> def) {
        LazyOptional<C> cap = provider.getCapability(capability);
        if (cap.isPresent())
            return function.apply(cap.orElseThrow(CapabilityNotPresentException::new));
        return def.get();
    }

    public static final class Energy {
        private Energy() {}

        public static double returnDoubleIfPresent(ICapabilityProvider provider, ToDoubleFunction<IEnergyStorage> function, final double def) {
            return CapabilityUtil.returnDoubleIfPresent(provider, CapabilityEnergy.ENERGY, function, () -> def);
        }

        public static double returnDoubleIfPresent(ICapabilityProvider provider, ToDoubleFunction<IEnergyStorage> function, DoubleSupplier def) {
            return CapabilityUtil.returnDoubleIfPresent(provider, CapabilityEnergy.ENERGY, function, def);
        }

        public static int returnIntIfPresent(ICapabilityProvider provider, ToIntFunction<IEnergyStorage> function, int def) {
            return CapabilityUtil.returnIntIfPresent(provider, CapabilityEnergy.ENERGY, function, def);
        }

        public static int returnIntIfPresent(ICapabilityProvider provider, ToIntFunction<IEnergyStorage> function, IntSupplier def) {
            return CapabilityUtil.returnIntIfPresent(provider, CapabilityEnergy.ENERGY, function, def);
        }

        public static boolean returnBooleanIfPresent(ICapabilityProvider provider, Predicate<IEnergyStorage> function, boolean def) {
            return CapabilityUtil.returnBooleanIfPresent(provider, CapabilityEnergy.ENERGY, function, def);
        }

        public static boolean returnBooleanIfPresent(ICapabilityProvider provider, Predicate<IEnergyStorage> function, BooleanSupplier def) {
            return CapabilityUtil.returnBooleanIfPresent(provider, CapabilityEnergy.ENERGY, function, def);
        }

        public static <R> R returnIfPresent(ICapabilityProvider provider, Function<IEnergyStorage, R> function, R def) {
            return CapabilityUtil.returnIfPresent(provider, CapabilityEnergy.ENERGY, function, def);
        }

        public static <R> R returnIfPresent(ICapabilityProvider provider, Function<IEnergyStorage, R> function, Supplier<R> def) {
            return CapabilityUtil.returnIfPresent(provider, CapabilityEnergy.ENERGY, function, def);
        }

        @Nonnull
        public static LazyOptional<IEnergyStorage> getCap(ICapabilityProvider stack) {
            return stack.getCapability(CapabilityEnergy.ENERGY);
        }

        public static boolean hasCap(ICapabilityProvider stack) {
            return stack.getCapability(CapabilityEnergy.ENERGY).isPresent();
        }
    }
}
