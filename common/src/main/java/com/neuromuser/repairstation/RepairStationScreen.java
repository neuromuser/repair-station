package com.neuromuser.repairstation;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class RepairStationScreen extends AbstractContainerScreen<RepairStationScreenHandler> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(RepairStation.MOD_ID, "textures/gui/repair_station.png");

    public RepairStationScreen(RepairStationScreenHandler menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageHeight = 166;
        this.imageWidth = 176;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);

        if (menu.isRepairing()) {
            int fuelProgress = menu.getFuelProgress();
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 56, y + 36 + 12 - fuelProgress,
                    176.0F, 12.0F - fuelProgress, 14, fuelProgress + 1, 256, 256);
        }

        int repairProgress = menu.getRepairProgress();
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x + 79, y + 34,
                176.0F, 14.0F, repairProgress + 1, 16, 256, 256);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
