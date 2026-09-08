package com.neuromuser.repairstation.compat;

import com.neuromuser.repairstation.config.RepairStationConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return this::createConfigScreen;
    }

    private Screen createConfigScreen(Screen parent) {
        return RepairStationConfigScreen.create(parent,
                FabricLoader.getInstance().getConfigDir().resolve("repair-station.json"));
    }
}