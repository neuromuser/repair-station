package com.neuromuser.repairstation.config;

import com.neuromuser.repairstation.RepairStation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.Supplier;

public class ForgeConfigNetworking {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(RepairStation.MOD_ID, "config"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public static void init() {
        CHANNEL.registerMessage(0, ConfigSyncPacket.class,
                ConfigSyncPacket::encode,
                ConfigSyncPacket::decode,
                ConfigSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void sendToPlayer(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new ConfigSyncPacket(ConfigManager.toJson(), player.getServer().isDedicatedServer()));
    }

    public static class ConfigSyncPacket {
        private final String json;
        private final boolean dedicatedServer;

        public ConfigSyncPacket() {
            this.json = "";
            this.dedicatedServer = false;
        }

        public ConfigSyncPacket(String json, boolean dedicatedServer) {
            this.json = json;
            this.dedicatedServer = dedicatedServer;
        }

        public static void encode(ConfigSyncPacket packet, FriendlyByteBuf buf) {
            buf.writeUtf(packet.json);
            buf.writeBoolean(packet.dedicatedServer);
        }

        public static ConfigSyncPacket decode(FriendlyByteBuf buf) {
            return new ConfigSyncPacket(buf.readUtf(), buf.readBoolean());
        }

        public static void handle(ConfigSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                if (context.getDirection().getReceptionSide().isClient()) {
                    ConfigManager.receiveServerConfig(packet.json, packet.dedicatedServer);
                }
            });
            context.setPacketHandled(true);
        }
    }
}