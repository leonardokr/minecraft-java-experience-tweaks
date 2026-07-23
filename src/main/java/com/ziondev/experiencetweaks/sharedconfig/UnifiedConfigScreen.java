package com.ziondev.experiencetweaks.sharedconfig;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import java.util.Map;
import java.util.function.Function;

/**
 * UnifiedConfigScreen is a GUI screen presenting a selection of registered
 * Metalion's mods.
 */
public class UnifiedConfigScreen extends Screen {
    private final Screen lastScreen;

    /**
     * Constructs a new unified config screen.
     *
     * @param lastScreen The parent screen to return to when closing (Screen)
     */
    public UnifiedConfigScreen(Screen lastScreen) {
        super(Component.translatable("key.metalions.open_config"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        super.init();
        Map<String, Function<Screen, Screen>> screens = ModConfigRegistry.getConfigScreens();
        int yStart = this.height / 2 - (screens.size() * 24 / 2);
        int index = 0;
        for (Map.Entry<String, Function<Screen, Screen>> entry : screens.entrySet()) {
            String displayName = entry.getKey();
            Function<Screen, Screen> factory = entry.getValue();

            this.addRenderableWidget(Button.builder(Component.literal(displayName), button -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(factory.apply(this));
                }
            }).bounds(this.width / 2 - 100, yStart + (index * 24), 200, 20).build());
            index++;
        }

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 40, 200, 20).build());
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.text(this.font, this.title, (this.width - this.font.width(this.title)) / 2, 20, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.lastScreen);
        }
    }
}
