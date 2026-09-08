package com.neuromuser.repairstation.config;

import com.neuromuser.repairstation.RepairStation;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public class FabricConfigNetworkingClient {
    private static final ResourceLocation SYNC_ID = new ResourceLocation(RepairStation.MOD_ID, "config");

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(SYNC_ID, (client, handler, buf, responseSender) -> {
            String json = buf.readUtf();
            boolean dedicatedServer = buf.readBoolean();
            client.execute(() -> ConfigManager.receiveServerConfig(json, dedicatedServer));
        });
    }
}