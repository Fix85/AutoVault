package dev.fix85.gui;

import dev.fix85.Config;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
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

    private static final int ROW1 = 55;
    private static final int ROW2 = 79;
    private static final int ROW3 = 103;
    private static final int ROW4 = 127;
    private static final int CUSTOM_START_Y = 103;
    private static final int CUSTOM_CLEAR_Y = 170;
    private static final int BOTTOM_Y = 210;

    public AutoVaultConfigScreen(Screen parent) {
        super(Text.translatable("autovault.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;

        int leftX = cx - 180;
        int colW = 110;
        int midX = cx - 55;
        int midW = 110;
        int itemW = 53;
        int rightX = cx + 70;
        int rightW = 115;

        enabledBtn = ButtonWidget.builder(buildOnOff("autovault.gui.auto_vault", Config.get().enabled),
                b -> { Config.get().enabled = !Config.get().enabled; refresh(); })
                .dimensions(leftX, ROW1, colW, 20)
                .tooltip(Tooltip.of(Text.translatable("autovault.gui.tooltip.enabled")))
                .build();
        addDrawableChild(enabledBtn);

        normalBtn = ButtonWidget.builder(buildOnOff("autovault.gui.normal_vaults", Config.get().openNormal),
                b -> { Config.get().openNormal = !Config.get().openNormal; refresh(); })
                .dimensions(leftX, ROW2, colW, 20)
                .tooltip(Tooltip.of(Text.translatable("autovault.gui.tooltip.normal")))
                .build();
        addDrawableChild(normalBtn);

        ominousBtn = ButtonWidget.builder(buildOnOff("autovault.gui.ominous_vaults", Config.get().openOminous),
                b -> { Config.get().openOminous = !Config.get().openOminous; refresh(); })
                .dimensions(leftX, ROW3, colW, 20)
                .tooltip(Tooltip.of(Text.translatable("autovault.gui.tooltip.ominous")))
                .build();
        addDrawableChild(ominousBtn);

        filterBtn = ButtonWidget.builder(buildOnOff("autovault.gui.use_filter", Config.get().useFilter),
                b -> { Config.get().useFilter = !Config.get().useFilter; refresh(); })
                .dimensions(midX, ROW1, midW, 20)
                .tooltip(Tooltip.of(Text.translatable("autovault.gui.tooltip.use_filter")))
                .build();
        addDrawableChild(filterBtn);

        tridentBtn = ButtonWidget.builder(buildItemLabel("autovault.gui.trident", "minecraft:trident"),
                b -> { toggleItem("minecraft:trident"); refresh(); })
                .dimensions(midX, ROW2, itemW, 20)
                .tooltip(Tooltip.of(Text.translatable("autovault.gui.tooltip.trident")))
                .build();
        addDrawableChild(tridentBtn);

        maceBtn = ButtonWidget.builder(buildItemLabel("autovault.gui.mace", "minecraft:mace"),
                b -> { toggleItem("minecraft:mace"); refresh(); })
                .dimensions(midX + itemW + 4, ROW2, itemW, 20)
                .tooltip(Tooltip.of(Text.translatable("autovault.gui.tooltip.mace")))
                .build();
        addDrawableChild(maceBtn);

        heavyCoreBtn = ButtonWidget.builder(buildItemLabel("autovault.gui.core", "minecraft:heavy_core"),
                b -> { toggleItem("minecraft:heavy_core"); refresh(); })
                .dimensions(midX, ROW3, itemW, 20)
                .tooltip(Tooltip.of(Text.translatable("autovault.gui.tooltip.core")))
                .build();
        addDrawableChild(heavyCoreBtn);

        bookBtn = ButtonWidget.builder(buildItemLabel("autovault.gui.book", "minecraft:enchanted_book"),
                b -> { toggleItem("minecraft:enchanted_book"); refresh(); })
                .dimensions(midX + itemW + 4, ROW3, itemW, 20)
                .tooltip(Tooltip.of(Text.translatable("autovault.gui.tooltip.book")))
                .build();
        addDrawableChild(bookBtn);

        boolean hasBook = Config.get().filter.contains("minecraft:enchanted_book");
        windBurstBtn = ButtonWidget.builder(buildOnOff("autovault.gui.wind_burst_only", Config.get().requireWindBurstOnBook),
                b -> { Config.get().requireWindBurstOnBook = !Config.get().requireWindBurstOnBook; refresh(); })
                .dimensions(midX, ROW4, midW, 20)
                .tooltip(Tooltip.of(Text.translatable("autovault.gui.tooltip.wind_burst")))
                .build();
        windBurstBtn.active = hasBook && Config.get().useFilter;
        addDrawableChild(windBurstBtn);

        customItemField = new TextFieldWidget(this.textRenderer, rightX, ROW1, rightW, 20,
                Text.literal("custom item id"));
        customItemField.setPlaceholder(Text.literal("minecraft:diamond"));
        customItemField.setMaxLength(64);
        addDrawableChild(customItemField);

        addDrawableChild(ButtonWidget.builder(Text.translatable("autovault.gui.add_remove"), b -> {
            String idStr = customItemField.getText().trim().toLowerCase();
            if (idStr.isEmpty()) return;
            Identifier identifier = Identifier.tryParse(idStr);
            if (identifier != null) {
                String id = identifier.toString();
                if (!Config.get().filter.add(id)) Config.get().filter.remove(id);
                Config.save();
            }
            customItemField.setText("");
            refresh();
        }).dimensions(rightX, ROW2, rightW, 20)
                .tooltip(Tooltip.of(Text.translatable("autovault.gui.tooltip.add_remove_btn")))
                .build());

        customItemsToDraw.clear();
        extraCustomCount = 0;
        int customY = CUSTOM_START_Y;
        int displayedCount = 0;

        for (String id : Config.get().filter) {
            if (id.equals("minecraft:trident") || id.equals("minecraft:mace") ||
                id.equals("minecraft:heavy_core") || id.equals("minecraft:enchanted_book")) continue;

            if (displayedCount < 3) {
                customItemsToDraw.add(id);
                String shortName = id.replace("minecraft:", "");
                if (shortName.length() > 11) shortName = shortName.substring(0, 9) + "..";
                final String itemId = id;
                addDrawableChild(ButtonWidget.builder(Text.literal("§c✖ §7" + shortName), btn -> {
                    Config.get().filter.remove(itemId);
                    Config.save();
                    refresh();
                }).dimensions(rightX, customY, rightW, 18).build());
                customY += 20;
                displayedCount++;
            } else {
                extraCustomCount++;
            }
        }

        addDrawableChild(ButtonWidget.builder(Text.translatable("autovault.gui.clear_list"), b -> {
            Config.get().filter.removeIf(id ->
                !id.equals("minecraft:trident") && !id.equals("minecraft:mace") &&
                !id.equals("minecraft:heavy_core") && !id.equals("minecraft:enchanted_book")
            );
            Config.save();
            refresh();
        }).dimensions(rightX, CUSTOM_CLEAR_Y, rightW, 20)
                .tooltip(Tooltip.of(Text.translatable("autovault.gui.tooltip.clear_list")))
                .build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("autovault.gui.reset"), b -> {
            Config.resetToDefaults();
            refresh();
        }).dimensions(cx - 110, BOTTOM_Y, 105, 20)
                .tooltip(Tooltip.of(Text.translatable("autovault.gui.tooltip.reset")))
                .build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("autovault.gui.done"), b -> close())
                .dimensions(cx + 5, BOTTOM_Y, 105, 20)
                .tooltip(Tooltip.of(Text.translatable("autovault.gui.tooltip.done")))
                .build());
    }

    private void toggleItem(String id) {
        if (!Config.get().filter.add(id)) Config.get().filter.remove(id);
        Config.save();
    }

    private Text buildOnOff(String key, boolean value) {
        String state = value ? "§aON" : "§cOFF";
        return Text.translatable(key).append(": " + state);
    }

    private Text buildItemLabel(String key, String id) {
        boolean has = Config.get().filter.contains(id);
        return Text.literal(has ? "§a✔§r " : "§c✖§r ").append(Text.translatable(key));
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

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, cx, 12, 0xFFFFFF);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("autovault.gui.general"), cx - 125, 42, 0xAAAAAA);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("autovault.gui.presets"), cx, 42, 0xAAAAAA);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.translatable("autovault.gui.custom_list"), cx + 127, 42, 0xAAAAAA);

        if (extraCustomCount > 0) {
            int moreY = CUSTOM_START_Y + (3 * 20) + 4;
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.translatable("autovault.gui.more_items", String.valueOf(extraCustomCount)),
                    cx + 127, moreY, 0x888888);
        }

        String filterText = Config.get().filter.isEmpty()
                ? Text.translatable("autovault.gui.filter_empty").getString()
                : String.join(", ", Config.get().filter);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("§7").append(Text.translatable("autovault.gui.active_filter", filterText)),
                cx, this.height - 14, 0xAAAAAA);
    }
}
