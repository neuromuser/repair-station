package com.neuromuser.repairstation.config;

import com.neuromuser.repairstation.RepairStation;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class FabricConfigNetworking {
    public static void init() {
        PayloadTypeRegistry.playS2C().register(ConfigSyncPayload.TYPE, ConfigSyncPayload.STREAM_CODEC);
    }

    public static void sendToClient(ServerPlayer player, boolean dedicatedServer) {
        ServerPlayNetworking.send(player, new ConfigSyncPayload(ConfigManager.toJson(), dedicatedServer));
    }

    public record ConfigSyncPayload(String json, boolean dedicatedServer) implements CustomPacketPayload {
        public static final Type<ConfigSyncPayload> TYPE =
                new Type<>(Identifier.fromNamespaceAndPath(RepairStation.MOD_ID, "config_sync"));
        public static final StreamCodec<FriendlyByteBuf, ConfigSyncPayload> STREAM_CODEC = StreamCodec.composite(
                net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8, ConfigSyncPayload::json,
                net.minecraft.network.codec.ByteBufCodecs.BOOL, ConfigSyncPayload::dedicatedServer,
                ConfigSyncPayload::new
        );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
