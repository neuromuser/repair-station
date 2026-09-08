package com.neuromuser.repairstation;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class RepairStationFabricGameTest implements FabricGameTest {

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public static void repairStationIsInFunctionalBlocksTab(GameTestHelper helper) {
        RepairStationGameTests.repairStationIsInFunctionalBlocksTab(helper);
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 100)
    public static void repairStationRecipeCraftsTheBlock(GameTestHelper helper) {
        RepairStationGameTests.repairStationRecipeCraftsTheBlock(helper);
    }
}