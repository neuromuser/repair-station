package com.neuromuser.repairstation.config;

import com.neuromuser.repairstation.config.FabricConfigNetworking.ConfigSyncPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class FabricConfigNetworkingClient {
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> ConfigManager.receiveServerConfig(payload.json(), payload.dedicatedServer()));
        });
    }
}
