package com.neuromuser.repairstation;

import com.neuromuser.repairstation.config.ConfigManager;
import com.neuromuser.repairstation.config.RepairStationConfigScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLPaths;

public class RepairStationForgeClient {
    @Mod.EventBusSubscriber(modid = RepairStation.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                ModLoadingContext.get().registerExtensionPoint(
                        ConfigScreenHandler.ConfigScreenFactory.class,
                        () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) ->
                                RepairStationConfigScreen.create(parent,
                                        FMLPaths.CONFIGDIR.get().resolve("repair-station.json"))));
                MinecraftForge.EVENT_BUS.register(ClientForgeEvents.class);
                RepairStationClient.init();
            });
        }
    }

    public static class ClientForgeEvents {
        @SubscribeEvent
        public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            ConfigManager.clearServerState();
        }
    }
}