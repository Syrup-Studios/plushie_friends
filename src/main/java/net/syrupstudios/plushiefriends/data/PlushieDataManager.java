package net.syrupstudios.plushiefriends.data;

import com.mojang.authlib.GameProfile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.syrupstudios.plushiefriends.PlushieFriends;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PlushieDataManager extends SimpleJsonResourceReloadListener implements IdentifiableResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Map<ResourceLocation, PlushieDefinition> PLUSHIES = new HashMap<>();
    private static final Map<String, GameProfile> RESOLVED_PROFILES = new ConcurrentHashMap<>();

    public PlushieDataManager() {
        super(GSON, "plushies");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
        PLUSHIES.clear();
        RESOLVED_PROFILES.clear();
        prepared.forEach((id, jsonElement) -> {
            try {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                String ownerName = jsonObject.has("owner_name") ? jsonObject.get("owner_name").getAsString() : "";

                List<String> lore = new ArrayList<>();
                if (jsonObject.has("lore")) {
                    jsonObject.getAsJsonArray("lore").forEach(element -> lore.add(element.getAsString()));
                }

                PLUSHIES.put(id, new PlushieDefinition(ownerName, lore));
                preloadOwner(ownerName);
            } catch (Exception e) {
                PlushieFriends.LOGGER.error("Failed to parse plushie data pack entry for {}", id, e);
            }
        });
    }

    @Override
    public ResourceLocation getFabricId() {
        return new ResourceLocation(PlushieFriends.MOD_ID, "plushies");
    }

    public static PlushieDefinition get(ResourceLocation id) {
        return PLUSHIES.get(id);
    }

    public static GameProfile getResolvedProfile(String ownerName, MinecraftServer server) {
        GameProfile profile = RESOLVED_PROFILES.get(ownerName);
        if (profile != null || server == null) {
            return profile;
        }

        profile = server.getProfileCache().get(ownerName)
                .map(cachedProfile -> server.getSessionService().fillProfileProperties(cachedProfile, true))
                .filter(resolvedProfile -> resolvedProfile.getProperties().containsKey("textures"))
                .orElse(null);
        if (profile != null) {
            RESOLVED_PROFILES.put(ownerName, profile);
        }
        return profile;
    }

    private static void preloadOwner(String ownerName) {
        if (!ownerName.isEmpty() && !RESOLVED_PROFILES.containsKey(ownerName)) {
            SkullBlockEntity.updateGameprofile(new GameProfile(null, ownerName), profile -> {
                if (profile.getProperties().containsKey("textures")) {
                    RESOLVED_PROFILES.put(ownerName, profile);
                }
            });
        }
    }

    public record PlushieDefinition(String ownerName, List<String> lore) {}
}