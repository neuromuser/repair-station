package com.neuromuser.repairstation;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class RepairStation {
    public static final String MOD_ID = "repairstation";

    public static final ResourceKey<Block> REPAIR_STATION_BLOCK_ID =
            ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, "repair_station"));

    public static final Block REPAIR_STATION_BLOCK = new RepairStationBlock(
        BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE).noOcclusion()
            .setId(REPAIR_STATION_BLOCK_ID)
    );

    public static final MenuType<RepairStationScreenHandler> REPAIR_STATION_SCREEN_HANDLER =
        new MenuType<>(RepairStationScreenHandler::new, FeatureFlags.VANILLA_SET);

    public static BlockEntityType<RepairStationBlockEntity> REPAIR_STATION_BLOCK_ENTITY;
}