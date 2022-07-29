package com.direwolf20.buildinggadgets.client;

import com.direwolf20.buildinggadgets.common.items.AbstractGadget;
import com.direwolf20.buildinggadgets.common.items.TemplateItem;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyBindings {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyBindings.class);

    private static final KeyConflictContextGadget CONFLICT_CONTEXT_GADGET = new KeyConflictContextGadget();

    private static final List<KeyMapping> keyMappings = new ArrayList<>();

    public static KeyMapping menuSettings = createBinding("settings_menu",GLFW.GLFW_KEY_G);
    public static KeyMapping range = createBinding("range", GLFW.GLFW_KEY_R);
    public static KeyMapping undo = createBinding("undo", GLFW.GLFW_KEY_U);
    public static KeyMapping anchor = createBinding("anchor", GLFW.GLFW_KEY_H);
    public static KeyMapping fuzzy = createBinding("fuzzy", GLFW.GLFW_KEY_UNKNOWN);
    public static KeyMapping connectedArea = createBinding("connected_area", GLFW.GLFW_KEY_UNKNOWN);
    public static KeyMapping rotateMirror = createBinding("rotate_mirror", GLFW.GLFW_KEY_UNKNOWN);
    public static KeyMapping materialList = createBinding("material_list", GLFW.GLFW_KEY_M);

    public static void init() {}

    private static KeyMapping createBinding(String name, int key) {
        KeyMapping keyBinding = new KeyMapping(getKey(name), CONFLICT_CONTEXT_GADGET, InputConstants.Type.KEYSYM.getOrCreate(key), getKey("category"));
        keyMappings.add(keyBinding);
        return keyBinding;
    }

    private static String getKey(String name) {
        return String.join(".", "key", Reference.MODID, name);
    }

    @SubscribeEvent
    static void register(RegisterKeyMappingsEvent event) {
        LOGGER.debug("Registering {} keybinding for {}", keyMappings.size(), Reference.MODID);
        keyMappings.forEach(event::register);
    }

    public static class KeyConflictContextGadget implements IKeyConflictContext
    {
        @Override
        public boolean isActive() {
            Player player = Minecraft.getInstance().player;
            return !KeyConflictContext.GUI.isActive() && player != null
                    && (!AbstractGadget.getGadget(player).isEmpty()
                        || (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof TemplateItem || player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof TemplateItem));
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return other == this || other == KeyConflictContext.IN_GAME;
        }
    }
}
