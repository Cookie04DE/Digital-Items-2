package com.example.digital_items_2;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class DigitizerScreen extends AbstractContainerScreen<DigitizerMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(DigitalItems2.MODID, "textures/gui/digitizer_container.png");

    private final ContainerData data;

    public DigitizerScreen(DigitizerMenu menu, Inventory inv, Component component) {
        super(menu, inv, component);
        this.data = menu.data;
    }

    public int getCurrentEnergy() {
        return (data.get(0) << 16) + (data.get(1) << 12) + (data.get(2) << 8) + data.get(3);
    }

    public int getMaxEnergy() {
        return (data.get(4) << 16) + (data.get(5) << 12) + (data.get(6) << 8) + data.get(7);
    }

    private Rect2i getEnergyArea(int x, int y) {
        return new Rect2i(x+152, y+9, 16, 69);
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        blit(poseStack, x, y, 0, 0, imageWidth, imageHeight);


        Rect2i energyArea = getEnergyArea(x, y);

        float entireHeight = (float) energyArea.getHeight();
        float filled = ((float)getCurrentEnergy()) / ((float)getMaxEnergy());
        int missingHeight = (int) (entireHeight * (1.0F - filled));

        fill(poseStack, energyArea.getX(), energyArea.getY(), energyArea.getX() + energyArea.getWidth(), energyArea.getY() + missingHeight, 0xffff0000);
        fill(poseStack, energyArea.getX(), energyArea.getY() + missingHeight, energyArea.getX() + energyArea.getWidth(), energyArea.getY() + energyArea.getHeight(), 0xff00ff00);

        renderTooltip(poseStack,mouseX, mouseY);
    }

    @Override
    protected void renderLabels(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        Rect2i energyArea = getEnergyArea(x, y);
        if(energyArea.contains(mouseX, mouseY)) {
            renderTooltip(poseStack, List.of(Component.literal("%d/%d FE".formatted(getCurrentEnergy(), getMaxEnergy()))), Optional.empty(), mouseX - x, mouseY - y);
        }
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int mouseX, int mouseY, float delta) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, delta);
        renderTooltip(poseStack, mouseX, mouseY);
    }
}
