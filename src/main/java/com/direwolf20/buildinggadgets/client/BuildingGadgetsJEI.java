//package com.direwolf20.buildinggadgets.client;
//
//import com.direwolf20.buildinggadgets.client.screen.TemplateManagerGUI;
//import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
//import com.direwolf20.buildinggadgets.common.items.OurItems;
//import com.direwolf20.buildinggadgets.common.util.ref.Reference;
//import mezz.jei.api.IModPlugin;
//import mezz.jei.api.JeiPlugin;
//import mezz.jei.api.gui.handlers.IGuiContainerHandler;
//import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
//import mezz.jei.api.registration.IGuiHandlerRegistration;
//import mezz.jei.api.registration.ISubtypeRegistration;
//import net.minecraft.client.renderer.Rect2i;
//import net.minecraft.world.item.Item;
//import net.minecraft.resources.ResourceLocation;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//@JeiPlugin
//public class BuildingGadgetsJEI implements IModPlugin {
//    @Override
//    public ResourceLocation getPluginUid() {
//        return new ResourceLocation(Reference.MODID, "jei_plugin");
//    }
//
//    @Override
//    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
//        registration.addGuiContainerHandler(TemplateManagerGUI.class, new GuiContainerHandler());
//    }
//
//    @Override
//    public void registerItemSubtypes(ISubtypeRegistration registration) {
//        List<Item> gadgets = new ArrayList<Item>(){{
//            add(OurItems.BUILDING_GADGET_ITEM.get());
//            add(OurItems.EXCHANGING_GADGET_ITEM.get());
//            add(OurItems.DESTRUCTION_GADGET_ITEM.get());
//            add(OurItems.COPY_PASTE_GADGET_ITEM.get());
//        }};
//
//        for(Item gadget : gadgets) {
//            registration.registerSubtypeInterpreter(gadget, itemStack -> {
//                if (!(itemStack.getItem() instanceof AbstractGadget))
//                    return ISubtypeInterpreter.NONE;
//
//                double energy = itemStack.getOrCreateTag().getDouble("energy");
//                if (energy == 0)
//                    return "empty";
//                else if (energy == ((AbstractGadget) itemStack.getItem()).getEnergyMax())
//                    return "charged";
//
//                return ISubtypeInterpreter.NONE;
//            });
//        }
//    }
//
//    private static class GuiContainerHandler implements IGuiContainerHandler<TemplateManagerGUI> {
//        @Override
//        public List<Rect2i> getGuiExtraAreas(TemplateManagerGUI containerScreen) {
//            return new ArrayList<>(Collections.singleton(new Rect2i((containerScreen.width / 2) + 80, (containerScreen.height / 2) - 80, 60, 120)));
//        }
//    }
//}
