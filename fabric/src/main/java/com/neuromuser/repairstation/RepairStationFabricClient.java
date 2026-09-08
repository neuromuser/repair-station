package com.neuromuser.repairstation;

import com.neuromuser.repairstation.config.ConfigManager;
import com.neuromuser.repairstation.config.FabricConfigNetworkingClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.gui.screens.MenuScreens;

public class RepairStationFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricConfigNetworkingClient.init();
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ConfigManager.clearServerState());
        MenuScreens.register(RepairStation.REPAIR_STATION_SCREEN_HANDLER, RepairStationScreen::new);
    }
}