package com.neuromuser.repairstation;

import com.neuromuser.repairstation.config.ConfigManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = RepairStation.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class RepairStationNeoForgeClient {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            NeoForge.EVENT_BUS.register(ClientForgeEvents.class);
            RepairStationClient.init();
        });
    }

    public static class ClientForgeEvents {
        @SubscribeEvent
        public static void onLoggingOut(PlayerEvent.PlayerLoggedOutEvent event) {
            ConfigManager.clearServerState();
        }
    }
}