package com.neuromuser.repairstation;

import com.neuromuser.repairstation.config.ConfigManager;
import com.neuromuser.repairstation.config.FabricConfigNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class RepairStationFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ConfigManager.load(FabricLoader.getInstance().getConfigDir().resolve("repair-station.json"));
        FabricConfigNetworking.init();
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                FabricConfigNetworking.sendToClient(handler.getPlayer(), server.isDedicatedServer()));

        Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(RepairStation.MOD_ID, "repair_station"),
                RepairStation.REPAIR_STATION_BLOCK);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(RepairStation.MOD_ID, "repair_station"),
                new BlockItem(RepairStation.REPAIR_STATION_BLOCK, new Item.Properties()));
        RepairStation.REPAIR_STATION_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                new ResourceLocation(RepairStation.MOD_ID, "repair_station"),
                BlockEntityType.Builder.of(
                        RepairStationBlockEntity::new, RepairStation.REPAIR_STATION_BLOCK).build(null));
        RepairStationBlockEntity.BLOCK_ENTITY_TYPE = RepairStation.REPAIR_STATION_BLOCK_ENTITY;
        Registry.register(BuiltInRegistries.MENU, new ResourceLocation(RepairStation.MOD_ID, "repair_station"),
                RepairStation.REPAIR_STATION_SCREEN_HANDLER);

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(content ->
                content.accept(RepairStation.REPAIR_STATION_BLOCK));
    }
}