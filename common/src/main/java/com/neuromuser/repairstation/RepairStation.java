package com.neuromuser.repairstation;

import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class RepairStation {
    public static final String MOD_ID = "repairstation";

    public static final Block REPAIR_STATION_BLOCK = new RepairStationBlock(
        BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE).noOcclusion()
    );

    public static final MenuType<RepairStationScreenHandler> REPAIR_STATION_SCREEN_HANDLER =
        new MenuType<>(RepairStationScreenHandler::new, FeatureFlags.VANILLA_SET);

    public static BlockEntityType<RepairStationBlockEntity> REPAIR_STATION_BLOCK_ENTITY;
}