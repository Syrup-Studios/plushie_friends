package net.syrupstudios.plushiefriends.util;

import com.mojang.authlib.properties.Property;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PlushieProfileManager {
    private static final Map<String, Boolean> SLIM_MODEL_CACHE = new ConcurrentHashMap<>();

    private PlushieProfileManager() {}

    public static boolean getOrCacheIsSlim(String textureValue) {
        return SLIM_MODEL_CACHE.computeIfAbsent(textureValue, PlushieProfileManager::decodeSlimModel);
    }

    private static boolean decodeSlimModel(String textureValue) {
        try {
            String jsonString = new String(Base64.getDecoder().decode(textureValue), StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(jsonString).getAsJsonObject();
            JsonObject textures = root.getAsJsonObject("textures");
            if (textures == null) return false;

            JsonObject skin = textures.getAsJsonObject("SKIN");
            if (skin == null) return false;

            JsonObject metadata = skin.getAsJsonObject("metadata");
            return metadata != null && metadata.has("model")
                    && "slim".equals(metadata.get("model").getAsString());
        } catch (RuntimeException error) {
            return false;
        }
    }

    public static String propertyValue(Property property) {
        //? if >=1.21 {
        /*return property.value();
        *///?} else
        return property.getValue();
    }

    public static void clearCache() {
        SLIM_MODEL_CACHE.clear();
    }
}
