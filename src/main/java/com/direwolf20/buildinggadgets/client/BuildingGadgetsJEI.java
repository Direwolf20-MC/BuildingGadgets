package com.direwolf20.buildinggadgets.client;

import com.direwolf20.buildinggadgets.client.screen.TemplateManagerGUI;
import com.direwolf20.buildinggadgets.common.items.gadgets.AbstractGadget;
import com.direwolf20.buildinggadgets.common.registry.OurItems;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@JeiPlugin
public class BuildingGadgetsJEI implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(Reference.MODID, "jei_plugin");
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(TemplateManagerGUI.class, new GuiContainerHandler());
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        List<AbstractGadget> gadgets = new ArrayList<AbstractGadget>(){{
            add(OurItems.gadgetBuilding);
            add(OurItems.gadgetExchanger);
            add(OurItems.gadgetDestruction);
            add(OurItems.gadgetCopyPaste);
        }};

        for(AbstractGadget gadget : gadgets) {
            registration.registerSubtypeInterpreter(gadget, itemStack -> {
                if (!(itemStack.getItem() instanceof AbstractGadget))
                    return ISubtypeInterpreter.NONE;

                double energy = itemStack.getOrCreateTag().getDouble("energy");
                if (energy == 0)
                    return "empty";
                else if (energy == ((AbstractGadget) itemStack.getItem()).getEnergyMax())
                    return "charged";

                return ISubtypeInterpreter.NONE;
            });
        }
    }

    private static class GuiContainerHandler implements IGuiContainerHandler<TemplateManagerGUI> {
        @Override
        public List<Rectangle2d> getGuiExtraAreas(TemplateManagerGUI containerScreen) {
            return new ArrayList<>(Collections.singleton(new Rectangle2d((containerScreen.width / 2) + 80, (containerScreen.height / 2) - 80, 60, 120)));
        }
    }
}
