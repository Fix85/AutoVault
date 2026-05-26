package dev.fix85;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

public class Config {
    public boolean enabled = true;

    public boolean useFilter = true;

    public Set<String> filter = new LinkedHashSet<>();

    public boolean requireWindBurstOnBook = true;

    public boolean openOminous = true;

    public boolean openNormal = true;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Config INSTANCE;

    public static Config get() {
        if (INSTANCE == null) INSTANCE = new Config();
        return INSTANCE;
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("autovault.json");
    }

    public static void load() {
        Path path = file();
        try {
            if (Files.exists(path)) {
                String json = Files.readString(path);
                Config loaded = GSON.fromJson(json, Config.class);
                if (loaded != null) INSTANCE = loaded;
            } else {
                INSTANCE = defaults();
                save();
            }
        } catch (IOException e) {
            INSTANCE = defaults();
        }
        if (INSTANCE.filter == null) INSTANCE.filter = new LinkedHashSet<>();
    }

    public static void save() {
        try {
            Files.createDirectories(file().getParent());
            Files.writeString(file(), GSON.toJson(get()));
        } catch (IOException ignored) {}
    }

    public static void resetToDefaults() {
        INSTANCE = defaults();
        save();
    }

    private static Config defaults() {
        Config c = new Config();
        c.filter.add("minecraft:trident");
        c.filter.add("minecraft:mace");
        c.filter.add("minecraft:heavy_core");
        c.filter.add("minecraft:enchanted_book");
        return c;
    }
}
