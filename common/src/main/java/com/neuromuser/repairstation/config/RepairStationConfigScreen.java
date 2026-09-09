package com.neuromuser.repairstation.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RepairStationConfigScreen extends Screen {
    private static final int ROW_HEIGHT = 58;
    private static final int COL_ITEM_X = 14;
    private static final int COL_DUR_X = 172;
    private static final int COL_PROD_X = 240;

    private final Screen parent;
    private final Path configPath;
    private final boolean serverControlled;
    private final List<FuelRow> rows = new ArrayList<>();
    private EditBox newFuelField;
    private Button addButton;
    private int scrollOffset = 0;
    private int maxScroll = 0;

    public static Screen create(Screen parent, Path configPath) {
        return new RepairStationConfigScreen(parent, configPath);
    }

    private RepairStationConfigScreen(Screen parent, Path configPath) {
        super(Component.literal("Repair Station Config"));
        this.parent = parent;
        this.configPath = configPath;
        this.serverControlled = ConfigManager.isServerControlled();
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();
        this.rows.clear();

        if (serverControlled) {
            this.addRenderableWidget(Button.builder(Component.literal("OK"),
                            button -> this.minecraft.setScreen(parent))
                    .bounds(this.width / 2 - 50, this.height / 2, 100, 20).build());
            return;
        }

        Config config = ConfigManager.get();
        for (FuelConfig fuel : config.fuels) {
            this.rows.add(new FuelRow(fuel));
        }
        this.newFuelField = new EditBox(this.font, COL_ITEM_X, 0, 150, 18, Component.literal(""));
        this.newFuelField.setMaxLength(64);
        this.addRenderableWidget(newFuelField);

        layoutRows();

        this.addButton = Button.builder(Component.literal("Add"),
                        button -> addNewFuel())
                .bounds(COL_ITEM_X + 160, 0, 70, 18).build();
        this.addRenderableWidget(addButton);
        layoutRows();
        this.addRenderableWidget(Button.builder(Component.literal("Save"), button -> saveAndClose())
                .bounds(this.width - 192, 12, 92, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> this.minecraft.setScreen(parent))
                .bounds(this.width - 96, 12, 92, 20).build());
    }

    private void layoutRows() {
        int viewTop = 48;
        int viewBottom = this.height - 56;
        int viewHeight = Math.max(0, viewBottom - viewTop);
        int contentHeight = rows.size() * ROW_HEIGHT + 30;

        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).layoutAt(viewTop - scrollOffset + i * ROW_HEIGHT);
        }

        if (newFuelField != null) {
            newFuelField.setX(COL_ITEM_X);
            newFuelField.setY(this.height - 40);
        }
        if (addButton != null) {
            addButton.setX(COL_ITEM_X + 160);
            addButton.setY(this.height - 41);
        }

        maxScroll = Math.max(0, contentHeight - viewHeight);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - (long) (scrollY * 12)));
        layoutRows();
        return true;
    }

    private void addNewFuel() {
        String add = newFuelField.getValue();
        if (add == null || add.trim().isEmpty()) {
            return;
        }
        if (add.startsWith("#")) {
            ConfigManager.get().fuels.add(new FuelConfig(add.substring(1).trim(), true, 80, 5));
        } else {
            ConfigManager.get().fuels.add(new FuelConfig(add.trim(), false, 80, 5));
        }
        newFuelField.setValue("");
        init();
    }

    private void handleRemove(FuelRow row) {
        ConfigManager.get().fuels.remove(row.fuel);
        rows.remove(row);
        removeWidget(row.itemField);
        removeWidget(row.durationField);
        removeWidget(row.productField);
        removeWidget(row.removeButton);
        layoutRows();
    }

    private void saveAndClose() {
        Config config = ConfigManager.get();
        for (FuelRow row : rows) {
            FuelConfig fuel = row.fuel;
            String item = row.itemField.getValue();
            if (item.startsWith("#")) {
                fuel.itemOrTag = item.substring(1).trim();
                fuel.isTag = true;
            } else {
                fuel.itemOrTag = item.trim();
                fuel.isTag = false;
            }
            fuel.durationSeconds = parseInt(row.durationField.getValue(), fuel.durationSeconds, 1, 3600);
            fuel.durabilityPerCycle = parseInt(row.productField.getValue(), fuel.durabilityPerCycle, 1, 100);
        }

        String add = newFuelField.getValue();
        if (add != null && !add.trim().isEmpty()) {
            if (add.startsWith("#")) {
                config.fuels.add(new FuelConfig(add.substring(1).trim(), true, 80, 5));
            } else {
                config.fuels.add(new FuelConfig(add.trim(), false, 80, 5));
            }
        }

        ConfigManager.save(configPath);
        this.minecraft.setScreen(parent);
    }

    private static int parseInt(String value, int fallback, int min, int max) {
        try {
            return Math.max(min, Math.min(Integer.parseInt(value.trim()), max));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawString(this.font, Component.literal("Repair Station Config"), 14, 16, 0xFFFFFFFF);

        if (serverControlled) {
            guiGraphics.drawCenteredString(this.font,
                    Component.literal("This server controls the fuel configuration."),
                    this.width / 2, this.height / 2 - 20, 0xFFAAAAAA);
            guiGraphics.drawCenteredString(this.font,
                    Component.literal("Edit config/repair-station.json on the server."),
                    this.width / 2, this.height / 2 - 8, 0xFFAAAAAA);
            return;
        }

        enableRowScissor(guiGraphics);
        for (int i = 0; i < rows.size(); i++) {
            FuelRow row = rows.get(i);
            int y = 48 - scrollOffset + i * ROW_HEIGHT;
            row.renderLabels(guiGraphics, y);
        }
        guiGraphics.disableScissor();

        guiGraphics.drawString(this.font, Component.literal("Add New Fuel"), COL_ITEM_X, this.height - 52, 0xFFAAAAAA);
        if (newFuelField != null) {
            guiGraphics.drawString(this.font, Component.literal("Save writes to config/repair-station.json"),
                    COL_ITEM_X, this.height - 16, 0xFF888888);
        }
    }

    private void enableRowScissor(GuiGraphics guiGraphics) {
        guiGraphics.enableScissor(0, 48, this.width, this.height - 56);
    }

    private boolean isRowClick(double mouseY) {
        return mouseY >= 48 && mouseY <= this.height - 56;
    }

    private class ViewportEditBox extends EditBox {
        ViewportEditBox(int x, int y, int width, int height, Component message) {
            super(RepairStationConfigScreen.this.font, x, y, width, height, message);
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            enableRowScissor(guiGraphics);
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
            guiGraphics.disableScissor();
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return isRowClick(mouseY) && super.isMouseOver(mouseX, mouseY);
        }
    }

    private class ViewportButton extends Button {
        ViewportButton(int x, int y, int width, int height, Component message, OnPress onPress) {
            super(x, y, width, height, message, onPress, Button.DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            enableRowScissor(guiGraphics);
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
            guiGraphics.disableScissor();
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return isRowClick(mouseY) && super.isMouseOver(mouseX, mouseY);
        }
    }

    private class FuelRow {
        final FuelConfig fuel;
        final EditBox itemField;
        final EditBox durationField;
        final EditBox productField;
        final Button removeButton;

        FuelRow(FuelConfig fuel) {
            this.fuel = fuel;
            this.itemField = new ViewportEditBox(0, 0, 150, 18, Component.literal(""));
            this.durationField = new ViewportEditBox(0, 0, 60, 18, Component.literal(""));
            this.productField = new ViewportEditBox(0, 0, 60, 18, Component.literal(""));
            this.itemField.setMaxLength(64);
            this.durationField.setFilter(value -> value.matches("[0-9]*"));
            this.productField.setFilter(value -> value.matches("[0-9]*"));
            this.itemField.setValue((fuel.isTag ? "#" : "") + fuel.itemOrTag);
            this.durationField.setValue(String.valueOf(fuel.durationSeconds));
            this.productField.setValue(String.valueOf(fuel.durabilityPerCycle));
            this.removeButton = new ViewportButton(0, 0, 20, 20, Component.literal("X"), button -> handleRemove(FuelRow.this));

            RepairStationConfigScreen.this.addRenderableWidget(itemField);
            RepairStationConfigScreen.this.addRenderableWidget(durationField);
            RepairStationConfigScreen.this.addRenderableWidget(productField);
            RepairStationConfigScreen.this.addRenderableWidget(removeButton);
        }

        void layoutAt(int y) {
            itemField.setX(COL_ITEM_X);
            itemField.setY(y + 10);
            durationField.setX(COL_DUR_X);
            durationField.setY(y + 10);
            productField.setX(COL_PROD_X);
            productField.setY(y + 10);
            removeButton.setX(RepairStationConfigScreen.this.width - 24);
            removeButton.setY(y + 6);
        }

        void renderLabels(GuiGraphics guiGraphics, int y) {
            guiGraphics.drawString(RepairStationConfigScreen.this.font, Component.literal("Item/Tag ID"), COL_ITEM_X, y, 0xFFAAAAAA);
            guiGraphics.drawString(RepairStationConfigScreen.this.font, Component.literal("Duration (s)"), COL_DUR_X, y, 0xFFAAAAAA);
            guiGraphics.drawString(RepairStationConfigScreen.this.font, Component.literal("Dura/5s"), COL_PROD_X, y, 0xFFAAAAAA);
        }
    }
}