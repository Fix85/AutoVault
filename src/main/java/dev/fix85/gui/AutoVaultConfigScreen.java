package dev.fix85.gui;

import dev.fix85.Config;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class AutoVaultConfigScreen extends Screen {
    private final Screen parent;

    private TextFieldWidget customItemField;

    private ButtonWidget enabledBtn;
    private ButtonWidget filterBtn;
    private ButtonWidget windBurstBtn;
    private ButtonWidget ominousBtn;
    private ButtonWidget normalBtn;
    private ButtonWidget tridentBtn;
    private ButtonWidget maceBtn;
    private ButtonWidget heavyCoreBtn;
    private ButtonWidget bookBtn;

    private final List<String> customItemsToDraw = new ArrayList<>();
    private int extraCustomCount = 0;

    public AutoVaultConfigScreen(Screen parent) {
        super(Text.literal("Auto Vault"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;

        // --- ЛЕВАЯ КОЛОНКА: ОБЩИЕ НАСТРОЙКИ ---
        int leftX = cx - 180;
        int colW = 110;

        enabledBtn = ButtonWidget.builder(buildOnOff("Auto Vault", Config.get().enabled),
                b -> {
                    Config.get().enabled = !Config.get().enabled;
                    refresh();
                })
                .dimensions(leftX, 55, colW, 20)
                .build();
        addDrawableChild(enabledBtn);

        normalBtn = ButtonWidget.builder(buildOnOff("Normal Vaults", Config.get().openNormal),
                b -> { Config.get().openNormal = !Config.get().openNormal; refresh(); })
                .dimensions(leftX, 80, colW, 20).build();
        addDrawableChild(normalBtn);

        ominousBtn = ButtonWidget.builder(buildOnOff("Ominous Vaults", Config.get().openOminous),
                b -> { Config.get().openOminous = !Config.get().openOminous; refresh(); })
                .dimensions(leftX, 105, colW, 20).build();
        addDrawableChild(ominousBtn);


        // --- СРЕДНЯЯ КОЛОНКА: ПРЕСЕТЫ ФИЛЬТРА ---
        int midX = cx - 55;
        int midW = 110;

        filterBtn = ButtonWidget.builder(buildOnOff("Use Filter", Config.get().useFilter),
                b -> { Config.get().useFilter = !Config.get().useFilter; refresh(); })
                .dimensions(midX, 55, midW, 20).build();
        addDrawableChild(filterBtn);

        int itemW = 53;
        tridentBtn = ButtonWidget.builder(buildItemLabel("Trident", "minecraft:trident"),
                b -> { toggleItem("minecraft:trident"); refresh(); })
                .dimensions(midX, 80, itemW, 20).build();
        addDrawableChild(tridentBtn);

        maceBtn = ButtonWidget.builder(buildItemLabel("Mace", "minecraft:mace"),
                b -> { toggleItem("minecraft:mace"); refresh(); })
                .dimensions(midX + 57, 80, itemW, 20).build();
        addDrawableChild(maceBtn);

        heavyCoreBtn = ButtonWidget.builder(buildItemLabel("Core", "minecraft:heavy_core"),
                b -> { toggleItem("minecraft:heavy_core"); refresh(); })
                .dimensions(midX, 105, itemW, 20).build();
        addDrawableChild(heavyCoreBtn);

        bookBtn = ButtonWidget.builder(buildItemLabel("Book", "minecraft:enchanted_book"),
                b -> { toggleItem("minecraft:enchanted_book"); refresh(); })
                .dimensions(midX + 57, 105, itemW, 20).build();
        addDrawableChild(bookBtn);

        boolean hasBook = Config.get().filter.contains("minecraft:enchanted_book");
        windBurstBtn = ButtonWidget.builder(buildOnOff("Wind Burst Only", Config.get().requireWindBurstOnBook),
                b -> { Config.get().requireWindBurstOnBook = !Config.get().requireWindBurstOnBook; refresh(); })
                .dimensions(midX, 130, midW, 20).build();
        windBurstBtn.active = hasBook && Config.get().useFilter;
        addDrawableChild(windBurstBtn);


        // --- ПРАВАЯ КОЛОНКА: КАСТОМНЫЕ ПРЕДМЕТЫ ---
        int rightX = cx + 70;
        int rightW = 115;

        customItemField = new TextFieldWidget(this.textRenderer, rightX, 55, rightW, 20,
                Text.literal("custom item id"));
        customItemField.setPlaceholder(Text.literal("minecraft:diamond"));
        customItemField.setMaxLength(64);
        addDrawableChild(customItemField);

        addDrawableChild(ButtonWidget.builder(Text.literal("Add / Remove"), b -> {
            String idStr = customItemField.getText().trim().toLowerCase();
            if (idStr.isEmpty()) return;
            Identifier identifier = Identifier.tryParse(idStr);
            if (identifier != null) {
                String id = identifier.toString();
                if (!Config.get().filter.add(id)) {
                    Config.get().filter.remove(id);
                }
                Config.save();
            }
            customItemField.setText("");
            refresh();
        }).dimensions(rightX, 80, rightW, 20).build());

        // Динамический список добавленных предметов с кнопками быстрого удаления
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
                ButtonWidget removeBtn = ButtonWidget.builder(Text.literal("§c✖ §7" + shortName), btn -> {
                    Config.get().filter.remove(itemId);
                    Config.save();
                    refresh();
                }).dimensions(rightX, customY, rightW, 18).build();
                addDrawableChild(removeBtn);
                
                customY += 20;
                displayedCount++;
            } else {
                extraCustomCount++;
            }
        }

        // Кнопка полной очистки
        addDrawableChild(ButtonWidget.builder(Text.literal("Clear List"), b -> {
            // Очищаем только кастомные элементы, либо все
            // Очистим все кастомные элементы
            Config.get().filter.removeIf(id -> 
                !id.equals("minecraft:trident") && !id.equals("minecraft:mace") &&
                !id.equals("minecraft:heavy_core") && !id.equals("minecraft:enchanted_book")
            );
            Config.save();
            refresh();
        }).dimensions(rightX, 170, rightW, 20).build());


        // --- КНОПКИ ДЕЙСТВИЙ (САМЫЙ НИЗ) ---
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset to Defaults"), b -> {
            Config.resetToDefaults();
            refresh();
        }).dimensions(cx - 110, 205, 105, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(cx + 5, 205, 105, 20)
                .build());
    }

    private void toggleItem(String id) {
        if (!Config.get().filter.add(id)) {
            Config.get().filter.remove(id);
        }
        Config.save();
    }

    private Text buildOnOff(String label, boolean value) {
        return Text.literal(label + ": " + (value ? "§aON" : "§cOFF"));
    }

    private Text buildItemLabel(String label, String id) {
        boolean has = Config.get().filter.contains(id);
        return Text.literal((has ? "§a✔§r " : "§c✖§r ") + label);
    }

    private void refresh() {
        Config.save();
        clearAndInit();
    }

    @Override
    public void close() {
        Config.save();
        if (this.client != null) this.client.setScreen(parent);
    }

    @Override
    public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        int cx = this.width / 2;

        // Заголовки над группами
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, cx, 12, 0xFFFFFF);
        
        context.drawCenteredTextWithShadow(this.textRenderer, "§eGeneral", cx - 125, 42, 0xAAAAAA);
        context.drawCenteredTextWithShadow(this.textRenderer, "§ePresets", cx, 42, 0xAAAAAA);
        context.drawCenteredTextWithShadow(this.textRenderer, "§eCustom List", cx + 127, 42, 0xAAAAAA);

        if (extraCustomCount > 0) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§7+ " + extraCustomCount + " more items"),
                    cx + 127, 160, 0x888888);
        }

        // Текст текущего списка фильтров
        String hint = "Active Filter: " + (Config.get().filter.isEmpty()
                ? "(none — opens nothing)"
                : String.join(", ", Config.get().filter));
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§7" + hint),
                cx, this.height - 18, 0xAAAAAA);
    }
}
