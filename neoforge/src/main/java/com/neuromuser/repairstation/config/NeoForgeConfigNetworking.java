package com.neuromuser.repairstation.config;

import com.neuromuser.repairstation.RepairStation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NeoForgeConfigNetworking {
    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(NeoForgeConfigNetworking::registerPayloadHandler);
    }

    public static void registerPayloadHandler(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(RepairStation.MOD_ID)
                .versioned("1");
        registrar.playToClient(
                ConfigSyncPayload.TYPE,
                ConfigSyncPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    ConfigManager.receiveServerConfig(payload.json(), payload.dedicatedServer());
                })
        );
    }

    public static void sendToPlayer(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player,
                new ConfigSyncPayload(ConfigManager.toJson(), player.level().getServer().isDedicatedServer()));
    }

    public record ConfigSyncPayload(String json, boolean dedicatedServer) implements CustomPacketPayload {
        public static final Type<ConfigSyncPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(RepairStation.MOD_ID, "config_sync"));
        public static final StreamCodec<FriendlyByteBuf, ConfigSyncPayload> STREAM_CODEC = CustomPacketPayload.codec(
                ConfigSyncPayload::encode,
                ConfigSyncPayload::decode
        );

        private static void encode(ConfigSyncPayload payload, FriendlyByteBuf buf) {
            buf.writeUtf(payload.json);
            buf.writeBoolean(payload.dedicatedServer);
        }

        private static ConfigSyncPayload decode(FriendlyByteBuf buf) {
            return new ConfigSyncPayload(buf.readUtf(), buf.readBoolean());
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
