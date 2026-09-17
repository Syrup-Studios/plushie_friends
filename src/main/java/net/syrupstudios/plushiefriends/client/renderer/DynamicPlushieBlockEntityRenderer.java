package net.syrupstudios.plushiefriends.client.renderer;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
//? if <26.2
import net.minecraft.client.renderer.MultiBufferSource;
//? if >=26.2 {
/*import net.minecraft.client.renderer.rendertype.RenderTypes;
*///?} else
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
//? if >=26.2 {
/*import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
*///?}
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.syrupstudios.plushiefriends.PlushieFriends;
import net.syrupstudios.plushiefriends.block.DynamicPlushieBlock;
import net.syrupstudios.plushiefriends.block.entity.DynamicPlushieBlockEntity;
import net.syrupstudios.plushiefriends.client.PlushieProfileCache;

public class DynamicPlushieBlockEntityRenderer
//? if >=26.2 {
/* implements BlockEntityRenderer<DynamicPlushieBlockEntity, DynamicPlushieBlockEntityRenderer.RenderState> */
//?} else {
 implements BlockEntityRenderer<DynamicPlushieBlockEntity>
//?}
{
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            PlushieFriends.id("plushie"), "main"
    );

    private final PlushieModel model;

    public DynamicPlushieBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new PlushieModel(context.bakeLayer(LAYER_LOCATION));
    }

    //? if >=26.2 {
    /*public static final class RenderState extends BlockEntityRenderState {
        private GameProfile owner;
        private PlushieProfileCache.Skin skin;
        private int rotation;
        private boolean render;
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(
            DynamicPlushieBlockEntity blockEntity,
            RenderState state,
            float partialTick,
            net.minecraft.world.phys.Vec3 cameraPosition,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumblingOverlay);
        GameProfile owner = blockEntity.getOwner();
        state.render = PlushieProfileCache.hasTextures(owner)
                || blockEntity.isSafeToForceRender();
        state.owner = owner;
        state.skin = PlushieProfileCache.getSkin(owner);
        state.rotation = DynamicPlushieBlock.getRotation(blockEntity.getBlockState());
    }

    @Override
    public void submit(RenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.render) return;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        //? if >=26.3
        poseStack.rotateDegrees(Axis.YP, -RotationSegment.convertToDegrees(state.rotation));
        //? if >=26.3
        poseStack.rotateDegrees(Axis.ZP, 180.0F);
        //? if <26.3
        poseStack.mulPose(Axis.YP.rotationDegrees(-RotationSegment.convertToDegrees(state.rotation)));
        //? if <26.3
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.scale(0.5F, 0.5F, 0.5F);

        PlushieProfileCache.Skin skin = state.skin;
        //? if >=26.3
        collector.submitModel(this.model, new PlushieModel.State(skin.slim()), poseStack, RenderTypes.entityCutout(skin.textureLocation()), state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        //? if <26.3
        collector.submitModel(this.model, new PlushieModel.State(skin.slim()), poseStack, RenderTypes.entityCutout(skin.textureLocation()), state.lightCoords, OverlayTexture.NO_OVERLAY, 0, state.breakProgress);
        poseStack.popPose();
    }
    *///?} else {
    @Override
    public void render(DynamicPlushieBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        GameProfile owner = blockEntity.getOwner();
        if ((owner == null || !owner.getProperties().containsKey("textures")) && !blockEntity.isSafeToForceRender()) return;

        BlockState state = blockEntity.getBlockState();
        int rotation = DynamicPlushieBlock.getRotation(state);

        poseStack.pushPose();

        poseStack.translate(0.5D, 0.0D, 0.5D);

        float rotationAngle = -RotationSegment.convertToDegrees(rotation);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationAngle));

        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));

        float globalScale = 0.5F;
        poseStack.scale(globalScale, globalScale, globalScale);

        PlushieProfileCache.Skin skin = PlushieProfileCache.getSkin(blockEntity.getOwner());

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(skin.textureLocation()));
        this.model.render(poseStack, vertexConsumer, combinedLight, combinedOverlay, skin.slim());

        poseStack.popPose();
    }
    //?}
}
