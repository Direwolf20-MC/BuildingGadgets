package com.direwolf20.buildinggadgets.common.integration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashSet;
import java.util.Set;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.discovery.ASMDataTable.ASMData;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class IntegrationHandler {
    private static final Set<IIntegratedMod> MODS = new HashSet<>();

    public static void preInit(FMLPreInitializationEvent event) {
        for (ASMData asmData : event.getAsmData().getAll(IntegratedMod.class.getName())) {
            String name = asmData.getClassName();
            try {
                if (Loader.isModLoaded((String) asmData.getAnnotationInfo().get("value"))) {
                    IIntegratedMod mod = Class.forName(name).asSubclass(IIntegratedMod.class).newInstance();
                    mod.preInit();
                    MODS.add(mod);
                }
            } catch (Exception e) {
                BuildingGadgets.logger.error(String.format("Integration with %s failed", name), e);
            }
        }
    }

    public static void init() {
        MODS.forEach(mod -> mod.init());
    }

    public static void postInit() {
        MODS.forEach(mod -> mod.postInit());
    }

    @Retention(RetentionPolicy.RUNTIME)
    public static @interface IntegratedMod {
        String value();
    }

    public static interface IIntegratedMod {

        default void preInit() {}

        default void init() {}
 
        default void postInit() {}
    }
}