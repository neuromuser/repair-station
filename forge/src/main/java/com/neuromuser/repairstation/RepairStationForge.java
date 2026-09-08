package com.neuromuser.repairstation;

import com.neuromuser.repairstation.config.ConfigManager;
import com.neuromuser.repairstation.config.ForgeConfigNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(RepairStation.MOD_ID)
public class RepairStationForge {
    public static final DeferredRegister<net.minecraft.world.level.block.Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, RepairStation.MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, RepairStation.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, RepairStation.MOD_ID);
    public static final DeferredRegister<net.minecraft.world.inventory.MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, RepairStation.MOD_ID);

    public RepairStationForge() {
        IEventBus modEventBus = net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus();

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENUS.register(modEventBus);
        modEventBus.addListener(this::registerGameTests);

        BLOCKS.register("repair_station", () -> RepairStation.REPAIR_STATION_BLOCK);
        ITEMS.register("repair_station", () -> new BlockItem(RepairStation.REPAIR_STATION_BLOCK,
                new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));
        BLOCK_ENTITIES.register("repair_station", () -> {
            BlockEntityType<RepairStationBlockEntity> type = BlockEntityType.Builder.of(
                    RepairStationBlockEntity::new, RepairStation.REPAIR_STATION_BLOCK).build(null);
            RepairStationBlockEntity.BLOCK_ENTITY_TYPE = type;
            RepairStation.REPAIR_STATION_BLOCK_ENTITY = type;
            return type;
        });
        MENUS.register("repair_station", () -> RepairStation.REPAIR_STATION_SCREEN_HANDLER);

        ConfigManager.load(FMLPaths.CONFIGDIR.get().resolve("repair-station.json"));
        ForgeConfigNetworking.init();

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ForgeConfigNetworking.sendToPlayer(player);
        }
    }

    private void registerGameTests(RegisterGameTestsEvent event) {
        event.register(RepairStationGameTests.class);
    }
}