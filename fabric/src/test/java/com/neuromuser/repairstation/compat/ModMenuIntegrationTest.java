package com.neuromuser.repairstation.compat;

import com.terraformersmc.modmenu.api.ModMenuApi;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModMenuIntegrationTest {

    @Test
    void entrypointImplementsModMenuApi() throws Exception {
        ClassLoader loader = ModMenuIntegrationTest.class.getClassLoader();
        Class<?> entrypoint = Class.forName(
                "com.neuromuser.repairstation.compat.ModMenuIntegration", false, loader);
        assertTrue(ModMenuApi.class.isAssignableFrom(entrypoint),
                "ModMenuIntegration must implement ModMenuApi");
    }

    @Test
    void modJsonRegistersModMenuEntrypoint() throws IOException {
        String modJson;
        try (InputStream in = ModMenuIntegrationTest.class.getResourceAsStream("/fabric.mod.json")) {
            assertNotNull(in, "fabric.mod.json must be on the test classpath");
            modJson = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        assertTrue(modJson.contains("\"modmenu\""),
                "fabric.mod.json must declare the modmenu entrypoint");
        assertTrue(modJson.contains("com.neuromuser.repairstation.compat.ModMenuIntegration"),
                "fabric.mod.json must point modmenu at ModMenuIntegration");
    }
}