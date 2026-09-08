package com.neuromuser.repairstation;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;
import java.util.Optional;

public class RepairStationGameTests {

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void repairStationIsInFunctionalBlocksTab(GameTestHelper helper) {
        Item station = RepairStation.REPAIR_STATION_BLOCK.asItem();
        if (station == Items.AIR) {
            helper.fail("repair_station block has no registered item");
            return;
        }

        CreativeModeTabs.tryRebuildTabContents(FeatureFlags.VANILLA_SET, false,
                helper.getLevel().registryAccess());

        CreativeModeTab functional = null;
        for (CreativeModeTab tab : CreativeModeTabs.allTabs()) {
            if (tab.getDisplayName().getString().equals("Functional Blocks")) {
                functional = tab;
                break;
            }
        }
        if (functional == null) {
            helper.fail("the Functional Blocks creative tab was not found");
            return;
        }

        boolean found = functional.getDisplayItems().stream()
                .anyMatch(stack -> stack.is(station));
        if (!found) {
            helper.fail("repair_station item is missing from the Functional Blocks creative tab");
            return;
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void repairStationRecipeCraftsTheBlock(GameTestHelper helper) {
        Item station = RepairStation.REPAIR_STATION_BLOCK.asItem();
        if (station == Items.AIR) {
            helper.fail("repair_station block has no registered item");
            return;
        }

        ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(RepairStation.MOD_ID, "repair_station");
        RecipeManager recipes = helper.getLevel().getServer().getRecipeManager();
        Optional<RecipeHolder<CraftingRecipe>> found = recipes.getAllRecipesFor(RecipeType.CRAFTING).stream()
                .filter(e -> e.id().equals(recipeId))
                .findFirst();
        if (found.isEmpty()) {
            helper.fail("recipe repairstation:repair_station is not registered");
            return;
        }

        RecipeHolder<CraftingRecipe> holder = found.get();
        CraftingRecipe recipe = holder.value();
        ItemStack anticipated = recipe.getResultItem(helper.getLevel().registryAccess());
        if (!anticipated.is(station)) {
            helper.fail("recipe repairstation:repair_station resolves to " + anticipated + " instead of the repair station");
            return;
        }

        TransientCraftingContainer container = new TransientCraftingContainer(dummyMenu(), 3, 3);
        List<Ingredient> ingredients = recipe.getIngredients();
        for (int i = 0; i < container.getContainerSize() && i < ingredients.size(); i++) {
            ItemStack[] variants = ingredients.get(i).getItems();
            container.setItem(i, variants.length == 0 ? ItemStack.EMPTY : variants[0].copy());
        }

        CraftingInput input = CraftingInput.of(3, 3, container.getItems());

        Optional<RecipeHolder<CraftingRecipe>> match = recipes.getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
        if (match.isEmpty()) {
            helper.fail("the reconstructed pattern does not match recipe repairstation:repair_station");
            return;
        }

        ItemStack crafted = match.get().value().assemble(input, helper.getLevel().registryAccess());
        if (!crafted.is(station)) {
            helper.fail("crafting produced " + crafted + " instead of the repair station");
            return;
        }
        helper.succeed();
    }

    private static AbstractContainerMenu dummyMenu() {
        return new AbstractContainerMenu(null, 0) {
            @Override
            public ItemStack quickMoveStack(Player player, int index) {
                return ItemStack.EMPTY;
            }

            @Override
            public boolean stillValid(Player player) {
                return true;
            }
        };
    }
}
