package com.neuromuser.repairstation;

import com.neuromuser.repairstation.config.FuelConfig;
import com.neuromuser.repairstation.config.FuelMatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class RepairStationBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    public static BlockEntityType<RepairStationBlockEntity> BLOCK_ENTITY_TYPE;

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(3, ItemStack.EMPTY);
    private int repairTime = 0;
    private int fuelTime = 0;
    private int maxFuelTime = 1600;
    private int currentDurabilityPerCycle = 5;
    private static final int REPAIR_INTERVAL = 100;
    private static final int ACTIVE_BUFFER_TICKS = 3;

    private boolean wasActive = false;
    private int activeBufferTimer = 0;

    public RepairStationBlockEntity(BlockPos pos, BlockState state) {
        super(BLOCK_ENTITY_TYPE, pos, state);
    }

    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> RepairStationBlockEntity.this.fuelTime;
                case 1 -> RepairStationBlockEntity.this.maxFuelTime;
                case 2 -> RepairStationBlockEntity.this.repairTime;
                case 3 -> REPAIR_INTERVAL;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0: RepairStationBlockEntity.this.fuelTime = value; break;
                case 1: RepairStationBlockEntity.this.maxFuelTime = value; break;
                case 2: RepairStationBlockEntity.this.repairTime = value; break;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.repairstation.repair_station");
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new RepairStationScreenHandler(syncId, playerInventory, this, this.containerData);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RepairStationBlockEntity blockEntity) {
        if (!level.isClientSide()) {
            ItemStack inputStack = blockEntity.inventory.get(0);
            ItemStack fuelStack = blockEntity.inventory.get(1);
            ItemStack outputStack = blockEntity.inventory.get(2);

            boolean shouldBeActive = blockEntity.shouldBeActive();
            if (shouldBeActive) {
                blockEntity.activeBufferTimer = ACTIVE_BUFFER_TICKS;
            } else if (blockEntity.activeBufferTimer > 0) {
                blockEntity.activeBufferTimer--;
                shouldBeActive = true;
            }

            boolean isActive = shouldBeActive;

            if (isActive != blockEntity.wasActive) {
                RepairStationBlock.setActive(level, pos, isActive);
                blockEntity.wasActive = isActive;
            }

            if (inputStack.isEmpty() || !inputStack.isDamageableItem() || inputStack.getDamageValue() == 0) {
                blockEntity.repairTime = 0;
                blockEntity.setChanged();
                return;
            }

            if (blockEntity.fuelTime > 0) {
                blockEntity.fuelTime--;
                blockEntity.repairTime++;

                if (blockEntity.repairTime >= REPAIR_INTERVAL) {
                    blockEntity.repairTime = 0;
                    int repairAmount = blockEntity.currentDurabilityPerCycle;
                    inputStack.setDamageValue(Math.max(0, inputStack.getDamageValue() - repairAmount));

                    level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS,
                            0.3f, 0.8f + level.random.nextFloat() * 0.4f);

                    if (inputStack.getDamageValue() == 0) {
                        if (outputStack.isEmpty()) {
                            blockEntity.inventory.set(2, inputStack.copy());
                            blockEntity.inventory.set(0, ItemStack.EMPTY);
                        } else if (outputStack.getItem() == inputStack.getItem() && outputStack.getCount() < outputStack.getMaxStackSize()) {
                            outputStack.grow(1);
                            blockEntity.inventory.set(0, ItemStack.EMPTY);
                        }
                    }
                }
                blockEntity.setChanged();
            } else if (!fuelStack.isEmpty()) {
                FuelConfig fuelConfig = FuelMatcher.matchFuel(fuelStack);
                if (fuelConfig != null) {
                    fuelStack.shrink(1);
                    blockEntity.fuelTime = fuelConfig.getDurationTicks();
                    blockEntity.maxFuelTime = fuelConfig.getDurationTicks();
                    blockEntity.currentDurabilityPerCycle = fuelConfig.durabilityPerCycle;
                    blockEntity.setChanged();
                }
            } else {
                blockEntity.repairTime = 0;
                blockEntity.setChanged();
            }
        } else {
            if (state.getValue(RepairStationBlock.ACTIVE)) {
                spawnActiveParticles(level, pos);
            }
        }
    }

    private static void spawnActiveParticles(Level level, BlockPos pos) {
        if (level.random.nextInt(12) == 0) {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.9;
            double z = pos.getZ() + 0.5;

            level.addParticle(ParticleTypes.SMOKE,
                    x, y, z,
                    (level.random.nextDouble() - 0.5) * 0.01,
                    0.01,
                    (level.random.nextDouble() - 0.5) * 0.01);
        }

        if (level.random.nextInt(25) == 0) {
            double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.3;
            double y = pos.getY() + 0.7 + level.random.nextDouble() * 0.2;
            double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.3;

            level.addParticle(ParticleTypes.ENCHANT,
                    x, y, z,
                    0.0, 0.005, 0.0);
        }

        if (level.random.nextInt(45) == 0) {
            double x = pos.getX() + 0.4 + level.random.nextDouble() * 0.2;
            double y = pos.getY() + 0.5;
            double z = pos.getZ() + 0.4 + level.random.nextDouble() * 0.2;

            level.addParticle(ParticleTypes.HAPPY_VILLAGER,
                    x, y, z,
                    0.0, 0.03, 0.0);
        }
    }

    private boolean shouldBeActive() {
        if (fuelTime <= 0) return false;

        ItemStack inputStack = inventory.get(0);
        return !inputStack.isEmpty() &&
                inputStack.isDamageableItem() &&
                inputStack.getDamageValue() > 0;
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, this.inventory);
        this.repairTime = input.getIntOr("RepairTime", 0);
        this.fuelTime = input.getIntOr("FuelTime", 0);
        this.maxFuelTime = input.getIntOr("MaxFuelTime", 1600);
        this.currentDurabilityPerCycle = input.getIntOr("CurrentDurabilityPerCycle", 5);
        this.wasActive = false;
        this.activeBufferTimer = 0;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.inventory);
        output.putInt("RepairTime", this.repairTime);
        output.putInt("FuelTime", this.fuelTime);
        output.putInt("MaxFuelTime", this.maxFuelTime);
        output.putInt("CurrentDurabilityPerCycle", this.currentDurabilityPerCycle);
    }

    @Override
    public int getContainerSize() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        return inventory.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(inventory, slot, amount);
        if (!result.isEmpty()) {
            this.setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(inventory, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        this.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        inventory.clear();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN) {
            return new int[]{2};
        } else if (side == Direction.UP) {
            return new int[]{0};
        } else {
            return new int[]{1};
        }
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
        if (slot == 0) {
            return stack.isDamageableItem() && stack.getDamageValue() > 0;
        } else if (slot == 1) {
            return FuelMatcher.isFuel(stack);
        }
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
        return slot == 2;
    }
}
