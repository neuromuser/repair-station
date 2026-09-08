package com.neuromuser.repairstation;

import net.minecraft.client.gui.screens.MenuScreens;

public class RepairStationClient {
    public static void init() {
        MenuScreens.register(RepairStation.REPAIR_STATION_SCREEN_HANDLER, RepairStationScreen::new);
    }
}
