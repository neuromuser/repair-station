package com.neuromuser.repairstation.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class FuelMatcher {

    public static FuelConfig matchFuel(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        Config config = ConfigManager.get();

        for (FuelConfig fuel : config.fuels) {
            if (fuel.isTag) {
                try {
                    ResourceLocation tagId = new ResourceLocation(fuel.itemOrTag);
                    TagKey<Item> tag = TagKey.create(Registries.ITEM, tagId);
                    if (stack.is(tag)) {
                        return fuel;
                    }
                } catch (Exception e) {
                    System.err.println("Invalid tag identifier: " + fuel.itemOrTag);
                }
            } else {
                try {
                    ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                    if (itemId.toString().equals(fuel.itemOrTag)) {
                        return fuel;
                    }
                } catch (Exception e) {
                    System.err.println("Invalid item identifier: " + fuel.itemOrTag);
                }
            }
        }

        return null;
    }

    public static boolean isFuel(ItemStack stack) {
        return matchFuel(stack) != null;
    }
}