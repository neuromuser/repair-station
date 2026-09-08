package com.neuromuser.repairstation.config;

import java.util.List;

public class FuelConfig {
    public String itemOrTag;
    public boolean isTag;
    public int durationSeconds;
    public int durabilityPerCycle;

    public FuelConfig() {
        this.itemOrTag = "minecraft:flint";
        this.isTag = false;
        this.durationSeconds = 80;
        this.durabilityPerCycle = 5;
    }

    public FuelConfig(String itemOrTag, boolean isTag, int durationSeconds, int durabilityPerCycle) {
        this.itemOrTag = itemOrTag;
        this.isTag = isTag;
        this.durationSeconds = durationSeconds;
        this.durabilityPerCycle = durabilityPerCycle;
    }

    public int getDurationTicks() {
        return durationSeconds * 20;
    }

    @Override
    public String toString() {
        return String.format("%s%s - %ds (%d durability per 5s)",
                isTag ? "#" : "",
                itemOrTag,
                durationSeconds,
                durabilityPerCycle);
    }
}