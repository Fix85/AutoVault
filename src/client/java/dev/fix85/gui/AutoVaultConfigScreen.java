package dev.fix85.gui;

import dev.fix85.Config;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class AutoVaultConfigScreen extends Screen {
    private final Screen parent;

    private EditBox customItemField;

    private Button enabledBtn;
    private Button filterBtn;
    private Button windBurstBtn;
    private Button ominousBtn;
    private Button normalBtn;
    private Button tridentBtn;
    private Button maceBtn;
    private Button heavyCoreBtn;
    private Button bookBtn;

    private final List<String> customItemsToDraw = new ArrayList<>();
    private int extraCustomCount = 0;

    public AutoVaultConfigScreen(Screen parent) {
        super(Component.translatable("autovault.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;

        int leftX = cx - 180;
        int colW = 110;

        enabledBtn = Button.builder(buildOnOff("autovault.gui.auto_vault", Config.get().enabled),
                b -> {
                    Config.get().enabled = !Config.get().enabled;
                    refresh();
                })
                .bounds(leftX, 55, colW, 20)
                .tooltip(Tooltip.create(Component.translatable("autovault.gui.tooltip.enabled")))
                .build();
        addRenderableWidget(enabledBtn);

        normalBtn = Button.builder(buildOnOff("autovault.gui.normal_vaults", Config.get().openNormal),
                b -> { Config.get().openNormal = !Config.get().openNormal; refresh(); })
                .bounds(leftX, 80, colW, 20)
                .tooltip(Tooltip.create(Component.translatable("autovault.gui.tooltip.normal")))
                .build();
        addRenderableWidget(normalBtn);

        ominousBtn = Button.builder(buildOnOff("autovault.gui.ominous_vaults", Config.get().openOminous),
                b -> { Config.get().openOminous = !Config.get().openOminous; refresh(); })
                .bounds(leftX, 105, colW, 20)
                .tooltip(Tooltip.create(Component.translatable("autovault.gui.tooltip.ominous")))
                .build();
        addRenderableWidget(ominousBtn);

        int midX = cx - 55;
        int midW = 110;

        filterBtn = Button.builder(buildOnOff("autovault.gui.use_filter", Config.get().useFilter),
                b -> { Config.get().useFilter = !Config.get().useFilter; refresh(); })
                .bounds(midX, 55, midW, 20)
                .tooltip(Tooltip.create(Component.translatable("autovault.gui.tooltip.use_filter")))
                .build();
        addRenderableWidget(filterBtn);

        int itemW = 53;
        tridentBtn = Button.builder(buildItemLabel("autovault.gui.trident", "minecraft:trident"),
                b -> { toggleItem("minecraft:trident"); refresh(); })
                .bounds(midX, 80, itemW, 20)
                .tooltip(Tooltip.create(Component.translatable("autovault.gui.tooltip.trident")))
                .build();
        addRenderableWidget(tridentBtn);

        maceBtn = Button.builder(buildItemLabel("autovault.gui.mace", "minecraft:mace"),
                b -> { toggleItem("minecraft:mace"); refresh(); })
                .bounds(midX + 57, 80, itemW, 20)
                .tooltip(Tooltip.create(Component.translatable("autovault.gui.tooltip.mace")))
                .build();
        addRenderableWidget(maceBtn);

        heavyCoreBtn = Button.builder(buildItemLabel("autovault.gui.core", "minecraft:heavy_core"),
                b -> { toggleItem("minecraft:heavy_core"); refresh(); })
                .bounds(midX, 105, itemW, 20)
                .tooltip(Tooltip.create(Component.translatable("autovault.gui.tooltip.core")))
                .build();
        addRenderableWidget(heavyCoreBtn);

        bookBtn = Button.builder(buildItemLabel("autovault.gui.book", "minecraft:enchanted_book"),
                b -> { toggleItem("minecraft:enchanted_book"); refresh(); })
                .bounds(midX + 57, 105, itemW, 20)
                .tooltip(Tooltip.create(Component.translatable("autovault.gui.tooltip.book")))
                .build();
        addRenderableWidget(bookBtn);

        boolean hasBook = Config.get().filter.contains("minecraft:enchanted_book");
        windBurstBtn = Button.builder(buildOnOff("autovault.gui.wind_burst_only", Config.get().requireWindBurstOnBook),
                b -> { Config.get().requireWindBurstOnBook = !Config.get().requireWindBurstOnBook; refresh(); })
                .bounds(midX, 130, midW, 20)
                .tooltip(Tooltip.create(Component.translatable("autovault.gui.tooltip.wind_burst")))
                .build();
        windBurstBtn.active = hasBook && Config.get().useFilter;
        addRenderableWidget(windBurstBtn);

        int rightX = cx + 70;
        int rightW = 115;

        customItemField = new EditBox(this.font, rightX, 55, rightW, 20,
                Component.literal("custom item id"));
        customItemField.setHint(Component.literal("minecraft:diamond"));
        customItemField.setMaxLength(64);
        addRenderableWidget(customItemField);

        addRenderableWidget(Button.builder(Component.translatable("autovault.gui.add_remove"), b -> {
            String idStr = customItemField.getValue().trim().toLowerCase();
            if (idStr.isEmpty()) return;
            Identifier identifier = Identifier.tryParse(idStr);
            if (identifier != null) {
                String id = identifier.toString();
                if (!Config.get().filter.add(id)) {
                    Config.get().filter.remove(id);
                }
                Config.save();
            }
            customItemField.setValue("");
            refresh();
        }).bounds(rightX, 80, rightW, 20)
                .tooltip(Tooltip.create(Component.translatable("autovault.gui.tooltip.add_remove_btn")))
                .build());

        customItemsToDraw.clear();
        extraCustomCount = 0;
        int customY = 105;
        int displayedCount = 0;

        for (String id : Config.get().filter) {
            if (id.equals("minecraft:trident") || id.equals("minecraft:mace") ||
                id.equals("minecraft:heavy_core") || id.equals("minecraft:enchanted_book")) {
                continue;
            }

            if (displayedCount < 3) {
                customItemsToDraw.add(id);
                String shortName = id.replace("minecraft:", "");
                if (shortName.length() > 11) {
                    shortName = shortName.substring(0, 9) + "..";
                }

                final String itemId = id;
                Button removeBtn = Button.builder(Component.literal("§c✖ §7" + shortName), btn -> {
                    Config.get().filter.remove(itemId);
                    Config.save();
                    refresh();
                }).bounds(rightX, customY, rightW, 18).build();
                addRenderableWidget(removeBtn);

                customY += 20;
                displayedCount++;
            } else {
                extraCustomCount++;
            }
        }

        addRenderableWidget(Button.builder(Component.translatable("autovault.gui.clear_list"), b -> {
            Config.get().filter.removeIf(id ->
                !id.equals("minecraft:trident") && !id.equals("minecraft:mace") &&
                !id.equals("minecraft:heavy_core") && !id.equals("minecraft:enchanted_book")
            );
            Config.save();
            refresh();
        }).bounds(rightX, 170, rightW, 20)
                .tooltip(Tooltip.create(Component.translatable("autovault.gui.tooltip.clear_list")))
                .build());

        addRenderableWidget(Button.builder(Component.translatable("autovault.gui.reset"), b -> {
            Config.resetToDefaults();
            refresh();
        }).bounds(cx - 110, 205, 105, 20)
                .tooltip(Tooltip.create(Component.translatable("autovault.gui.tooltip.reset")))
                .build());

        addRenderableWidget(Button.builder(Component.translatable("autovault.gui.done"), b -> onClose())
                .bounds(cx + 5, 205, 105, 20)
                .tooltip(Tooltip.create(Component.translatable("autovault.gui.tooltip.done")))
                .build());
    }

    private void toggleItem(String id) {
        if (!Config.get().filter.add(id)) {
            Config.get().filter.remove(id);
        }
        Config.save();
    }

    private Component buildOnOff(String key, boolean value) {
        String state = value ? "§aON" : "§cOFF";
        return Component.translatable(key).copy().append(": " + state);
    }

    private Component buildItemLabel(String key, String id) {
        boolean has = Config.get().filter.contains(id);
        Component label = Component.translatable(key);
        return Component.literal(has ? "§a✔§r " : "§c✖§r ").append(label);
    }

    private void refresh() {
        Config.save();
        rebuildWidgets();
    }

    @Override
    public void onClose() {
        Config.save();
        if (this.minecraft != null) this.minecraft.setScreen(parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float delta) {
        super.extractRenderState(extractor, mouseX, mouseY, delta);
        int cx = this.width / 2;

        extractor.centeredText(this.font, this.title, cx, 12, 0xFFFFFF);

        extractor.centeredText(this.font, Component.translatable("autovault.gui.general"), cx - 125, 42, 0xAAAAAA);
        extractor.centeredText(this.font, Component.translatable("autovault.gui.presets"), cx, 42, 0xAAAAAA);
        extractor.centeredText(this.font, Component.translatable("autovault.gui.custom_list"), cx + 127, 42, 0xAAAAAA);

        if (extraCustomCount > 0) {
            extractor.centeredText(this.font,
                    Component.translatable("autovault.gui.more_items", String.valueOf(extraCustomCount)),
                    cx + 127, 160, 0x888888);
        }

        String filterText = Config.get().filter.isEmpty()
                ? Component.translatable("autovault.gui.filter_empty").getString()
                : String.join(", ", Config.get().filter);
        Component hintText = Component.translatable("autovault.gui.active_filter", filterText);

        extractor.centeredText(this.font,
                Component.literal("§7").append(hintText),
                cx, this.height - 18, 0xAAAAAA);
    }
}
