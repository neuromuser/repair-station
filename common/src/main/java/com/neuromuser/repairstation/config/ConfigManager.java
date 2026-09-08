package com.neuromuser.repairstation.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Config config = new Config();
    private static Config serverConfig = null;
    private static boolean hasServerMod = false;

    public static Config get() {
        return isServerControlled() ? serverConfig : config;
    }

    public static boolean isServerControlled() {
        return hasServerMod && serverConfig != null;
    }

    public static boolean shouldRunServerLogic() {
        return hasServerMod;
    }

    public static void load(Path path) {
        try {
            if (Files.exists(path)) {
                String json = Files.readString(path);
                config = GSON.fromJson(json, Config.class);
                if (config.fuels == null || config.fuels.isEmpty()) {
                    config.fuels.add(new FuelConfig("minecraft:flint", false, 80, 5));
                }
            } else {
                save(path);
            }
        } catch (IOException e) {
            System.err.println("Failed to load config: " + e.getMessage());
        }
    }

    public static void save(Path path) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(config));
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }

    public static String toJson() {
        return GSON.toJson(config);
    }

    public static void receiveServerConfig(String json, boolean dedicatedServer) {
        if (!dedicatedServer) {
            return;
        }
        serverConfig = GSON.fromJson(json, Config.class);
        hasServerMod = true;
    }

    public static void clearServerState() {
        serverConfig = null;
        hasServerMod = false;
    }

    static void resetForTesting() {
        config = new Config();
        serverConfig = null;
        hasServerMod = false;
    }

    public static void addFuel(FuelConfig fuel) {
        config.fuels.add(fuel);
    }

    public static void removeFuel(int index) {
        if (index >= 0 && index < config.fuels.size()) {
            config.fuels.remove(index);
        }
    }

    public static void removeFuel(FuelConfig fuel) {
        config.fuels.remove(fuel);
    }
}