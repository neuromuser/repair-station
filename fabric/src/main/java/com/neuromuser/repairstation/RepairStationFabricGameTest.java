package com.neuromuser.repairstation;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class RepairStationFabricGameTest {

    @GameTest(maxTicks = 100)
    public void repairStationIsInFunctionalBlocksTab(GameTestHelper helper) {
        RepairStationGameTests.repairStationIsInFunctionalBlocksTab(helper);
    }

    @GameTest(maxTicks = 100)
    public void repairStationRecipeCraftsTheBlock(GameTestHelper helper) {
        RepairStationGameTests.repairStationRecipeCraftsTheBlock(helper);
    }
}