package com.neuromuser.repairstation;

import com.neuromuser.repairstation.config.ConfigManager;
import com.neuromuser.repairstation.config.FabricConfigNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public class RepairStationFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ConfigManager.load(FabricLoader.getInstance().getConfigDir().resolve("repair-station.json"));
        FabricConfigNetworking.init();
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                FabricConfigNetworking.sendToClient(handler.getPlayer(), server.isDedicatedServer()));

        Registry.register(Registry.BLOCK, new ResourceLocation(RepairStation.MOD_ID, "repair_station"),
                RepairStation.REPAIR_STATION_BLOCK);
        Registry.register(Registry.ITEM, new ResourceLocation(RepairStation.MOD_ID, "repair_station"),
                new BlockItem(RepairStation.REPAIR_STATION_BLOCK,
                        new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));
        RepairStation.REPAIR_STATION_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE,
                new ResourceLocation(RepairStation.MOD_ID, "repair_station"),
                FabricBlockEntityTypeBuilder.create(RepairStationBlockEntity::new,
                        RepairStation.REPAIR_STATION_BLOCK).build());
        RepairStationBlockEntity.BLOCK_ENTITY_TYPE = RepairStation.REPAIR_STATION_BLOCK_ENTITY;
        Registry.register(Registry.MENU, new ResourceLocation(RepairStation.MOD_ID, "repair_station"),
                RepairStation.REPAIR_STATION_SCREEN_HANDLER);
    }
}