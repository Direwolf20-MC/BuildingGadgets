package com.direwolf20.buildinggadgets.common.integration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.discovery.ASMDataTable.ASMData;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class IntegrationHandler {

    public static void preInit(FMLPreInitializationEvent event) {
        for (ASMData asmData : event.getAsmData().getAll(IntegratedMod.class.getName())) {
            String name = asmData.getClassName();
            try {
                if (Loader.isModLoaded((String) asmData.getAnnotationInfo().get("value")))
                    Class.forName(name).asSubclass(IIntegratedMod.class).newInstance().initialize();
            } catch (Exception e) {
                BuildingGadgets.logger.error(String.format("Integration with %s failed", name), e);
            }
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    public static @interface IntegratedMod {
        String value();
    }

    public static interface IIntegratedMod {

        void initialize();
    }
}