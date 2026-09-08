package com.neuromuser.repairstation.config;

import java.util.ArrayList;
import java.util.List;

public class Config {
    public List<FuelConfig> fuels = new ArrayList<>();

    public Config() {
        fuels.add(new FuelConfig("minecraft:flint", false, 80, 5));
    }
}

