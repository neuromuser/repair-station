package com.neuromuser.repairstation;

import com.neuromuser.repairstation.config.ConfigManager;
import com.neuromuser.repairstation.config.NeoForgeConfigNetworking;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Set;

@Mod(RepairStation.MOD_ID)
public class RepairStationNeoForge {
    public static final DeferredRegister<net.minecraft.world.level.block.Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, RepairStation.MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, RepairStation.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, RepairStation.MOD_ID);
    public static final DeferredRegister<net.minecraft.world.inventory.MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, RepairStation.MOD_ID);

    public RepairStationNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENUS.register(modEventBus);
        modEventBus.addListener(this::buildContents);

        BLOCKS.register("repair_station", () -> RepairStation.REPAIR_STATION_BLOCK);
        ITEMS.register("repair_station", () -> new BlockItem(RepairStation.REPAIR_STATION_BLOCK, new Item.Properties()
                .useBlockDescriptionPrefix()
                .setId(ResourceKey.create(Registries.ITEM,
                        Identifier.fromNamespaceAndPath(RepairStation.MOD_ID, "repair_station")))));
        BLOCK_ENTITIES.register("repair_station", () -> {
            BlockEntityType<RepairStationBlockEntity> type = new BlockEntityType<>(
                    RepairStationBlockEntity::new, Set.of(RepairStation.REPAIR_STATION_BLOCK));
            RepairStationBlockEntity.BLOCK_ENTITY_TYPE = type;
            RepairStation.REPAIR_STATION_BLOCK_ENTITY = type;
            return type;
        });
        MENUS.register("repair_station", () -> RepairStation.REPAIR_STATION_SCREEN_HANDLER);

        ConfigManager.load(FMLPaths.CONFIGDIR.get().resolve("repair-station.json"));
        NeoForgeConfigNetworking.init(modEventBus);

        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            NeoForgeConfigNetworking.sendToPlayer(player);
        }
    }

    private void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(RepairStation.REPAIR_STATION_BLOCK);
        }
    }
}
