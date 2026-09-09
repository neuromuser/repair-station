package com.neuromuser.repairstation;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
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
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class RepairStationGameTests {

    public static void registerTestFunctions() {
        registerTestFunction("repair_station_is_in_functional_blocks_tab",
                RepairStationGameTests::repairStationIsInFunctionalBlocksTab);
        registerTestFunction("repair_station_recipe_crafts_the_block",
                RepairStationGameTests::repairStationRecipeCraftsTheBlock);
    }

    private static void registerTestFunction(String name, Consumer<GameTestHelper> function) {
        Registry.register(BuiltInRegistries.TEST_FUNCTION,
                ResourceKey.create(Registries.TEST_FUNCTION,
                        Identifier.fromNamespaceAndPath(RepairStation.MOD_ID, name)),
                function);
    }

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

    public static void repairStationRecipeCraftsTheBlock(GameTestHelper helper) {
        Item station = RepairStation.REPAIR_STATION_BLOCK.asItem();
        if (station == Items.AIR) {
            helper.fail("repair_station block has no registered item");
            return;
        }

        Identifier recipeId = Identifier.fromNamespaceAndPath(RepairStation.MOD_ID, "repair_station");
        ResourceKey<Recipe<?>> recipeKey = ResourceKey.create(Registries.RECIPE, recipeId);
        RecipeManager recipes = helper.getLevel().getServer().getRecipeManager();
        Optional<RecipeHolder<?>> found = recipes.byKey(recipeKey);
        if (found.isEmpty() || !(found.get().value() instanceof CraftingRecipe recipe)) {
            helper.fail("recipe repairstation:repair_station is not registered");
            return;
        }

        TransientCraftingContainer container = new TransientCraftingContainer(dummyMenu(), 3, 3);
        PlacementInfo placementInfo = recipe.placementInfo();
        List<Ingredient> ingredients = placementInfo.ingredients();
        for (int i = 0; i < container.getContainerSize(); i++) {
            int ingredientIndex = placementInfo.slotsToIngredientIndex().size() > i
                    ? placementInfo.slotsToIngredientIndex().getInt(i)
                    : -1;
            if (ingredientIndex < 0) {
                container.setItem(i, ItemStack.EMPTY);
                continue;
            }
            ItemStack variant = ingredients.get(ingredientIndex).items()
                    .findFirst()
                    .map(Holder::value)
                    .map(Item::getDefaultInstance)
                    .orElse(ItemStack.EMPTY);
            container.setItem(i, variant);
        }

        CraftingInput input = CraftingInput.of(3, 3, container.getItems());

        Optional<RecipeHolder<CraftingRecipe>> match = recipes.getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
        if (match.isEmpty() || !match.get().id().equals(recipeKey)) {
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
