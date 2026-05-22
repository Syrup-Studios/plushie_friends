package net.syrupstudios.plushiefriends.client;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.syrupstudios.plushiefriends.PlushieFriends;
import net.syrupstudios.plushiefriends.block.entity.DynamicPlushieBlockEntity;
import net.syrupstudios.plushiefriends.client.renderer.DynamicPlushieBlockEntityRenderer;
import net.syrupstudios.plushiefriends.client.renderer.PlushieModel;

public class PlushieFriendsClient implements ClientModInitializer {
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

            PlushieProfileCache.Skin skin = PlushieProfileCache.getSkin(owner);

            poseStack.pushPose();

            poseStack.translate(0.5D, 0.0D, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));

            float globalScale = 0.5F;
            poseStack.scale(globalScale, globalScale, globalScale);

            VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(skin.textureLocation()));
            this.itemModel.render(poseStack, vertexConsumer, combinedLight, combinedOverlay, skin.slim());

            poseStack.popPose();
        });
    }
}