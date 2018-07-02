package com.direwolf20.buildinggadgets;

import com.direwolf20.buildinggadgets.Entities.BlockBuildEntity;
import com.direwolf20.buildinggadgets.Entities.BlockBuildEntityRender;
import com.direwolf20.buildinggadgets.Items.BuildingTool;
import com.direwolf20.buildinggadgets.Items.ExchangerTool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

import static com.direwolf20.buildinggadgets.ModItems.buildingTool;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = BuildingGadgets.MODID)
public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent e) {
        // register texture stitcher
        //MinecraftForge.EVENT_BUS.register(new TextureStitcher());
        ModEntities.initModels();
        super.preInit(e);
    }

    @Override
    public void init(FMLInitializationEvent e) {
        super.init(e);
        MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
        KeyBindings.init();
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModBlocks.effectBlock.initModel();
        //ModBlocks.turretLaser.initModel();
        buildingTool.initModel();
        ModItems.exchangerTool.initModel();
    }

    public void registerEntityRenderers() {
        RenderingRegistry.registerEntityRenderingHandler(BlockBuildEntity.class, new BlockBuildEntityRender.Factory());
        //RenderingRegistry.registerEntityRenderingHandler(LaserBlastEntity.class, new LaserBlastEntityRender.Factory());
    }

    @SubscribeEvent
    public static void renderWorldLastEvent(RenderWorldLastEvent evt) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer p = mc.player;
        ItemStack heldItem = p.getHeldItemMainhand();
        if (heldItem.getItem() instanceof BuildingTool) {
            BuildingTool buildingTool = (BuildingTool) heldItem.getItem();
            buildingTool.renderOverlay(evt, p, heldItem);
        }
        else if (heldItem.getItem() instanceof ExchangerTool) {
            ExchangerTool exchangerTool = (ExchangerTool) heldItem.getItem();
            exchangerTool.renderOverlay(evt, p, heldItem);
        }

    }
}