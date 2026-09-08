package com.neuromuser.repairstation;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class RepairStationScreen extends AbstractContainerScreen<RepairStationScreenHandler> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(RepairStation.MOD_ID, "textures/gui/repair_station.png");

    public RepairStationScreen(RepairStationScreenHandler menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageHeight = 166;
        this.imageWidth = 176;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        RenderSystem.setShaderTexture(0, TEXTURE);
        GuiComponent.blit(poseStack, x, y, 0, 0.0f, 0.0f, imageWidth, imageHeight, 256, 256);

        if (menu.isRepairing()) {
            int fuelProgress = menu.getFuelProgress();
            GuiComponent.blit(poseStack, x + 56, y + 36 + 12 - fuelProgress, 0, 176.0f, 12.0f - fuelProgress, 14, fuelProgress + 1, 256, 256);
        }

        int repairProgress = menu.getRepairProgress();
        GuiComponent.blit(poseStack, x + 79, y + 34, 0, 176.0f, 14.0f, repairProgress + 1, 16, 256, 256);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        if (this.hoveredSlot != null) {
            this.renderTooltip(poseStack, this.hoveredSlot.getItem(), mouseX, mouseY);
        }
    }
}