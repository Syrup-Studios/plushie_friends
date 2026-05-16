package net.syrupstudios.client;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.syrupstudios.PlushieFriends;
import net.syrupstudios.block.entity.DynamicPlushieBlockEntity;
import net.syrupstudios.client.renderer.DynamicPlushieBlockEntityRenderer;
import net.syrupstudios.client.renderer.PlushieModel;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlushieFriendsClient implements ClientModInitializer {
    private static final Map<String, GameProfile> ITEM_PROFILE_CACHE = new HashMap<>();
    private static final Set<String> ITEM_RESOLVING_NAMES = new HashSet<>();
    private PlushieModel itemModel;

    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(DynamicPlushieBlockEntityRenderer.LAYER_LOCATION, PlushieModel::createLayer);
        BlockEntityRendererRegistry.register(DynamicPlushieBlockEntity.TYPE, DynamicPlushieBlockEntityRenderer::new);

        BuiltinItemRendererRegistry.INSTANCE.register(PlushieFriends.PLUSHIE_ITEM, (stack, displayContext, poseStack, bufferSource, combinedLight, combinedOverlay) -> {
            if (this.itemModel == null) {
                this.itemModel = new PlushieModel(Minecraft.getInstance().getEntityModels().bakeLayer(DynamicPlushieBlockEntityRenderer.LAYER_LOCATION));
            }

            GameProfile owner = null;
            if (stack.hasTag()) {
                CompoundTag tag = stack.getTag();
                if (tag != null && tag.contains("BlockEntityTag", 10)) {
                    CompoundTag blockEntityTag = tag.getCompound("BlockEntityTag");
                    if (blockEntityTag.contains("PlushieOwner", 10)) {
                        owner = NbtUtils.readGameProfile(blockEntityTag.getCompound("PlushieOwner"));
                    } else if (blockEntityTag.contains("PlushieOwner", 8)) {
                        String name = blockEntityTag.getString("PlushieOwner");
                        if (!name.isEmpty()) {
                            owner = new GameProfile(null, name);
                        }
                    }
                }
            }

            if (owner != null && !owner.getProperties().containsKey("textures")) {
                String name = owner.getName();
                if (name != null && !name.isEmpty()) {
                    if (ITEM_PROFILE_CACHE.containsKey(name)) {
                        owner = ITEM_PROFILE_CACHE.get(name);
                    } else if (!ITEM_RESOLVING_NAMES.contains(name)) {
                        ITEM_RESOLVING_NAMES.add(name);
                        GameProfile finalOwner = owner;
                        SkullBlockEntity.updateGameprofile(finalOwner, (resolvedProfile) -> {
                            Minecraft.getInstance().execute(() -> {
                                ITEM_PROFILE_CACHE.put(name, resolvedProfile);
                                ITEM_RESOLVING_NAMES.remove(name);
                            });
                        });
                    }
                }
            }

            poseStack.pushPose();

            poseStack.translate(0.5D, 0.0D, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));

            float globalScale = 0.5F;
            poseStack.scale(globalScale, globalScale, globalScale);

            ResourceLocation textureLocation;
            boolean isSlim = false;

            if (owner != null && owner.getProperties().containsKey("textures")) {
                textureLocation = Minecraft.getInstance().getSkinManager().getInsecureSkinLocation(owner);
                isSlim = isSlimSkin(owner);
            } else {
                textureLocation = DefaultPlayerSkin.getDefaultSkin();
            }

            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(textureLocation));
            this.itemModel.render(poseStack, vertexConsumer, combinedLight, combinedOverlay, isSlim);

            poseStack.popPose();
        });
    }

    private boolean isSlimSkin(GameProfile profile) {
        if (profile == null) return false;
        if (!profile.getProperties().containsKey("textures")) {
            return profile.getId() != null && (profile.getId().hashCode() & 1) == 1;
        }
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
        } catch (Exception e) {
        }
        return false;
    }
}