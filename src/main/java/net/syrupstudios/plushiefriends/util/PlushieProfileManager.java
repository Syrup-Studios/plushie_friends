package net.syrupstudios.plushiefriends.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class PlushieProfileManager {
    private static final Map<String, GameProfile> PROFILE_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> SLIM_MODEL_CACHE = new ConcurrentHashMap<>();

    private PlushieProfileManager() {}

    public static void resolveProfileAsync(String ownerName, Consumer<GameProfile> callback) {
        if (ownerName == null || ownerName.isEmpty()) {
            callback.accept(null);
            return;
        }

        String cacheKey = ownerName.toLowerCase(Locale.ROOT);
        GameProfile cached = PROFILE_CACHE.get(cacheKey);
        if (cached != null) {
            callback.accept(cached);
            return;
        }

        SkullBlockEntity.updateGameprofile(new GameProfile(null, ownerName), profile -> {
            if (profile != null && profile.getProperties().containsKey("textures")) {
                PROFILE_CACHE.put(cacheKey, profile);
                PROFILE_CACHE.put(profile.getName().toLowerCase(Locale.ROOT), profile);
                cacheModelTypeAsync(profile);
            }
            callback.accept(profile);
        });
    }

    public static GameProfile getCachedProfile(String ownerName) {
        return ownerName != null ? PROFILE_CACHE.get(ownerName.toLowerCase(Locale.ROOT)) : null;
    }

    public static GameProfile getOrResolveServerProfile(String ownerName, MinecraftServer server) {
        if (ownerName == null || ownerName.isEmpty()) return null;

        String cacheKey = ownerName.toLowerCase(Locale.ROOT);
        GameProfile profile = PROFILE_CACHE.get(cacheKey);
        if (profile != null || server == null) {
            return profile;
        }

        profile = server.getProfileCache().get(ownerName)
                .map(cachedProfile -> server.getSessionService().fillProfileProperties(cachedProfile, true))
                .filter(resolvedProfile -> resolvedProfile.getProperties().containsKey("textures"))
                .orElse(null);

        if (profile != null) {
            PROFILE_CACHE.put(cacheKey, profile);
            PROFILE_CACHE.put(profile.getName().toLowerCase(Locale.ROOT), profile);
            cacheModelTypeAsync(profile);
        }
        return profile;
    }

    public static void preloadOwner(String ownerName) {
        if (ownerName != null && !ownerName.isEmpty()) {
            resolveProfileAsync(ownerName, profile -> {});
        }
    }

    public static Boolean getIsSlimCached(String textureKey) {
        return SLIM_MODEL_CACHE.get(textureKey);
    }

    private static void cacheModelTypeAsync(GameProfile profile) {
        for (Property property : profile.getProperties().get("textures")) {
            String value = property.getValue();
            try {
                String jsonStr = new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
                JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();
                if (json.has("textures")) {
                    JsonObject textures = json.getAsJsonObject("textures");
                    if (textures.has("SKIN")) {
                        JsonObject skin = textures.getAsJsonObject("SKIN");
                        if (skin.has("metadata")) {
                            JsonObject metadata = skin.getAsJsonObject("metadata");
                            if (metadata.has("model")) {
                                boolean isSlim = "slim".equals(metadata.get("model").getAsString());
                                SLIM_MODEL_CACHE.put(value, isSlim);
                                return;
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
            SLIM_MODEL_CACHE.put(value, false);
        }
    }

    public static void clearCache() {
        PROFILE_CACHE.clear();
        SLIM_MODEL_CACHE.clear();
    }
}