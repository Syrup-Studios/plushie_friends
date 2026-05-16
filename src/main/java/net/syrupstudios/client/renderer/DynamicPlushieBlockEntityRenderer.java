package net.syrupstudios.client.renderer;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.syrupstudios.PlushieFriends;
import net.syrupstudios.block.DynamicPlushieBlock;
import net.syrupstudios.block.entity.DynamicPlushieBlockEntity;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class DynamicPlushieBlockEntityRenderer implements BlockEntityRenderer<DynamicPlushieBlockEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation(PlushieFriends.MOD_ID, "plushie"), "main"
    );

    private final PlushieModel model;

    public DynamicPlushieBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new PlushieModel(context.bakeLayer(LAYER_LOCATION));
    }

    @Override
    public void render(DynamicPlushieBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        BlockState state = blockEntity.getBlockState();
        Direction facing = state.hasProperty(DynamicPlushieBlock.FACING) ? state.getValue(DynamicPlushieBlock.FACING) : Direction.NORTH;

        poseStack.pushPose();

        poseStack.translate(0.5D, 0.0D, 0.5D);

        float rotationAngle = -facing.toYRot() + 180.0F;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationAngle));

        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));

        float globalScale = 0.5F;
        poseStack.scale(globalScale, globalScale, globalScale);

        GameProfile owner = blockEntity.getOwner();
        ResourceLocation textureLocation;
        boolean isSlim = false;

        if (owner != null) {
            textureLocation = Minecraft.getInstance().getSkinManager().getInsecureSkinLocation(owner);
            isSlim = isSlimSkin(owner);
        } else {
            textureLocation = DefaultPlayerSkin.getDefaultSkin();
        }

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(textureLocation));
        this.model.render(poseStack, vertexConsumer, combinedLight, combinedOverlay, isSlim);

        poseStack.popPose();
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