package net.syrupstudios.plushiefriends.client;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
//? if >=26.2 {
/*import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.component.ResolvableProfile;
*///?}
import net.syrupstudios.plushiefriends.util.PlushieProfileManager;
import net.syrupstudios.syruplibrary.profile.SyrupProfiles;

import java.util.HashMap;
import java.util.Map;

public final class PlushieProfileCache {
    private static final Map<String, Skin> SKIN_CACHE = new HashMap<>();

    private PlushieProfileCache() {}

    public static Skin getSkin(GameProfile profile) {
        if (hasTextures(profile)) {
            //? if >=26.2 {
            /*PlayerSkinRenderCache.RenderInfo info = Minecraft.getInstance().playerSkinRenderCache()
                    .getOrDefault(ResolvableProfile.createResolved(profile));
            net.minecraft.world.entity.player.PlayerSkin skin = info.playerSkin();
            return new Skin(skin.body().texturePath(), skin.model() == PlayerModelType.SLIM);
            *///?} else if >=1.21 {
            /*net.minecraft.client.resources.PlayerSkin playerSkin = Minecraft.getInstance().getSkinManager().getInsecureSkin(profile);
            return new Skin(playerSkin.texture(), playerSkin.model() == net.minecraft.client.resources.PlayerSkin.Model.SLIM);
            *///?} else {
            String key = getTextureKey(profile);
            Skin cached = SKIN_CACHE.get(key);
            if (cached == null) {
                ResourceLocation texture = Minecraft.getInstance().getSkinManager().getInsecureSkinLocation(profile);
                cached = new Skin(texture, isSlimSkin(profile));
                SKIN_CACHE.put(key, cached);
            }
            return cached;
            //?}
        }

        //? if >=26.2 {
        /*net.minecraft.world.entity.player.PlayerSkin skin = profile != null
                ? DefaultPlayerSkin.get(profile) : DefaultPlayerSkin.getDefaultSkin();
        return new Skin(skin.body().texturePath(), skin.model() == PlayerModelType.SLIM);
        *///?} else if >=1.21 {
        /*if (profile != null) {
            net.minecraft.client.resources.PlayerSkin skin = DefaultPlayerSkin.get(profile);
            return new Skin(skin.texture(), skin.model() == net.minecraft.client.resources.PlayerSkin.Model.SLIM);
        }
        return new Skin(DefaultPlayerSkin.getDefaultTexture(), false);
        *///?} else
        return new Skin(DefaultPlayerSkin.getDefaultSkin(), false);
    }

    //? if <26.2 {
    private static String getTextureKey(GameProfile profile) {
        for (Property property : profile.getProperties().get("textures")) {
            return PlushieProfileManager.propertyValue(property);
        }
        return profile.getName() != null ? profile.getName() : profile.toString();
    }
    private static boolean isSlimSkin(GameProfile profile) {
        for (Property property : profile.getProperties().get("textures")) {
            return PlushieProfileManager.getOrCacheIsSlim(PlushieProfileManager.propertyValue(property));
        }
        return false;
    }
    //?}

    public static boolean hasTextures(GameProfile profile) {
        //? if >=26.2 {
        /*return profile != null && profile.properties().containsKey("textures")
                && !profile.properties().get("textures").isEmpty();
        *///?} else
        return profile != null && profile.getProperties().containsKey("textures");
    }

    static GameProfile resolveCachedProfile(GameProfile profile) {
        if (profile == null || hasTextures(profile)) return profile;
        //? if >=26.2 {
        /*String name = profile.name();
        *///?} else
        String name = profile.getName();
        GameProfile cached = name == null ? null : SyrupProfiles.getCachedProfile(name);
        return cached != null && hasTextures(cached) ? cached : profile;
    }

    public record Skin(
            ResourceLocation
            textureLocation,
            boolean slim
    ) {}
}
