package com.neuromuser.repairstation.config;

import com.neuromuser.repairstation.RepairStation;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class FabricConfigNetworking {
    private static final ResourceLocation SYNC_ID = new ResourceLocation(RepairStation.MOD_ID, "config");

    public static void init() {
    }

    public static void sendToClient(ServerPlayer player, boolean dedicatedServer) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUtf(ConfigManager.toJson());
        buf.writeBoolean(dedicatedServer);
        ServerPlayNetworking.send(player, SYNC_ID, buf);
    }
}