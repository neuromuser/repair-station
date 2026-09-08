package com.neuromuser.repairstation.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigManagerTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void resetState() {
        ConfigManager.resetForTesting();
    }

    @Test
    void loadCreatesDefaultFileWhenMissing() {
        Path path = tempDir.resolve("config.json");
        ConfigManager.load(path);

        assertTrue(Files.exists(path), "missing config should be written to disk");
        Config config = ConfigManager.get();
        assertNotNull(config.fuels);
        assertEquals(1, config.fuels.size());
        assertEquals("minecraft:flint", config.fuels.get(0).itemOrTag);
        assertEquals(80, config.fuels.get(0).durationSeconds);
    }

    @Test
    void loadReadsExistingConfig() throws IOException {
        Path path = tempDir.resolve("config.json");
        Files.writeString(path, """
                {
                  "fuels": [
                    {"itemOrTag": "minecraft:coal", "isTag": false, "durationSeconds": 30, "durabilityPerCycle": 2}
                  ]
                }
                """);

        ConfigManager.load(path);

        Config config = ConfigManager.get();
        assertEquals(1, config.fuels.size());
        assertEquals("minecraft:coal", config.fuels.get(0).itemOrTag);
        assertEquals(30, config.fuels.get(0).durationSeconds);
    }

    @Test
    void emptyFuelListFallsBackToDefaultFuel() throws IOException {
        Path path = tempDir.resolve("config.json");
        Files.writeString(path, "{\"fuels\": []}");

        ConfigManager.load(path);

        assertEquals(1, ConfigManager.get().fuels.size());
        assertEquals("minecraft:flint", ConfigManager.get().fuels.get(0).itemOrTag);
    }

    @Test
    void saveRoundTripsConfig() {
        Path first = tempDir.resolve("first.json");
        Path second = tempDir.resolve("second.json");
        ConfigManager.load(first);

        ConfigManager.addFuel(new FuelConfig("#minecraft:coals", true, 45, 3));
        ConfigManager.save(second);
        ConfigManager.load(second);

        Config reloaded = ConfigManager.get();
        assertEquals(2, reloaded.fuels.size());
        assertEquals("#minecraft:coals", reloaded.fuels.get(1).itemOrTag);
        assertTrue(reloaded.fuels.get(1).isTag);
    }

    @Test
    void receiveServerConfigFromDedicatedServerShadowsLocal() {
        Path path = tempDir.resolve("config.json");
        ConfigManager.load(path);

        String serverJson = """
                {"fuels": [{"itemOrTag": "minecraft:charcoal", "isTag": false, "durationSeconds": 15, "durabilityPerCycle": 1}]}
                """;
        ConfigManager.receiveServerConfig(serverJson, true);

        assertTrue(ConfigManager.isServerControlled());
        assertTrue(ConfigManager.shouldRunServerLogic());
        assertEquals("minecraft:charcoal", ConfigManager.get().fuels.get(0).itemOrTag);
    }

    @Test
    void receiveServerConfigFromSingleplayerIsIgnored() {
        Path path = tempDir.resolve("config.json");
        ConfigManager.load(path);

        String serverJson = """
                {"fuels": [{"itemOrTag": "minecraft:charcoal", "isTag": false, "durationSeconds": 15, "durabilityPerCycle": 1}]}
                """;
        ConfigManager.receiveServerConfig(serverJson, false);

        assertFalse(ConfigManager.isServerControlled());
        assertFalse(ConfigManager.shouldRunServerLogic());
        assertEquals("minecraft:flint", ConfigManager.get().fuels.get(0).itemOrTag);
    }

    @Test
    void clearServerStateRestoresLocalConfig() {
        Path path = tempDir.resolve("config.json");
        ConfigManager.load(path);
        ConfigManager.receiveServerConfig(
                "{\"fuels\": [{\"itemOrTag\": \"minecraft:charcoal\", \"isTag\": false, \"durationSeconds\": 15, \"durabilityPerCycle\": 1}]}",
                true);
        ConfigManager.clearServerState();

        assertFalse(ConfigManager.isServerControlled());
        assertFalse(ConfigManager.shouldRunServerLogic());
        assertEquals("minecraft:flint", ConfigManager.get().fuels.get(0).itemOrTag);
    }

    @Test
    void removeFuelByIndexDoesNothingOutOfBounds() {
        Path path = tempDir.resolve("config.json");
        ConfigManager.load(path);

        ConfigManager.removeFuel(5);
        assertEquals(1, ConfigManager.get().fuels.size());
    }

    @Test
    void toJsonRoundTripsThroughServerConfig() {
        Path path = tempDir.resolve("config.json");
        ConfigManager.load(path);

        ConfigManager.addFuel(new FuelConfig("minecraft:blaze_rod", false, 100, 7));
        String json = ConfigManager.toJson();

        ConfigManager.receiveServerConfig(json, true);
        Config config = ConfigManager.get();
        assertEquals(2, config.fuels.size());
        assertEquals("minecraft:blaze_rod", config.fuels.get(1).itemOrTag);
        ConfigManager.clearServerState();
    }
}