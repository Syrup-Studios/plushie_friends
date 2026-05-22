package net.syrupstudios.plushiefriends.client;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public final class PlushieProfileCache {
    private static final Map<String, ResourceLocation> SKIN_LOCATION_CACHE = new HashMap<>();

    private PlushieProfileCache() {
    }

    public static Skin getSkin(GameProfile profile) {
        if (profile != null && profile.getProperties().containsKey("textures")) {
            return new Skin(getSkinLocation(profile), isSlimSkin(profile));
        }
        return new Skin(DefaultPlayerSkin.getDefaultSkin(), false);
    }

    private static ResourceLocation getSkinLocation(GameProfile profile) {
        String key = getTextureKey(profile);

        ResourceLocation textureLocation = SKIN_LOCATION_CACHE.get(key);
        if (textureLocation == null) {
            textureLocation = Minecraft.getInstance().getSkinManager().getInsecureSkinLocation(profile);
            SKIN_LOCATION_CACHE.put(key, textureLocation);
        }
        return textureLocation;
    }

    private static String getTextureKey(GameProfile profile) {
        for (Property property : profile.getProperties().get("textures")) {
            return property.getValue();
        }
        return profile.getName() != null ? profile.getName() : profile.toString();
    }

    private static boolean isSlimSkin(GameProfile profile) {
        try {
            for (Property property : profile.getProperties().get("textures")) {
                String jsonStr = new String(Base64.getDecoder().decode(property.getValue()), StandardCharsets.UTF_8);
                JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();
                if (json.has("textures")) {
                    JsonObject textures = json.getAsJsonObject("textures");
                    if (textures.has("SKIN")) {
                        JsonObject skin = textures.getAsJsonObject("SKIN");
                        if (skin.has("metadata")) {
                            JsonObject metadata = skin.getAsJsonObject("metadata");
                            if (metadata.has("model")) {
                                return "slim".equals(metadata.get("model").getAsString());
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    public record Skin(ResourceLocation textureLocation, boolean slim) {
    }
}