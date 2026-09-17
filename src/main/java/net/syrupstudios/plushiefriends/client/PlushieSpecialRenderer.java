package net.syrupstudios.plushiefriends.client;

//? if >=26.2 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemStack;
import net.syrupstudios.plushiefriends.PlushieFriends;
import net.syrupstudios.plushiefriends.client.renderer.DynamicPlushieBlockEntityRenderer;
import net.syrupstudios.plushiefriends.client.renderer.PlushieModel;
import net.syrupstudios.plushiefriends.item.PlushieItemData;
import net.syrupstudios.plushiefriends.util.PlushieNbtHelper;
import net.syrupstudios.plushiefriends.util.PlushieProfileManager;
import com.mojang.authlib.GameProfile;
import java.util.function.Consumer;
import org.joml.Vector3fc;

public final class PlushieSpecialRenderer implements SpecialModelRenderer<PlushieSpecialRenderer.RenderData> {
    private final PlushieModel model;
    private final boolean leftHand;

    private PlushieSpecialRenderer(PlushieModel model, boolean leftHand) {
        this.model = model;
        this.leftHand = leftHand;
    }

    @Override
    public RenderData extractArgument(ItemStack stack) {
        GameProfile owner = PlushieProfileCache.resolveCachedProfile(
                PlushieNbtHelper.getOwnerFromRoot(PlushieItemData.read(stack)));
        return new RenderData(PlushieProfileCache.getSkin(owner));
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        applyTransform(poseStack);
        this.model.root().getExtentsForGui(poseStack, output);
    }

    @Override
    public void submit(RenderData data, PoseStack poseStack, SubmitNodeCollector collector,
                       int light, int overlay, boolean foil, int outlineColor) {
        PlushieProfileCache.Skin skin = data.skin();
        poseStack.pushPose();
        applyTransform(poseStack);
        //? if >=26.3
        collector.submitModel(this.model, new PlushieModel.State(skin.slim()), poseStack, RenderTypes.entityCutout(skin.textureLocation()), light, overlay, outlineColor);
        //? if <26.3
        collector.submitModel(this.model, new PlushieModel.State(skin.slim()), poseStack, RenderTypes.entityCutout(skin.textureLocation()), light, overlay, outlineColor, null);
        poseStack.popPose();
    }

    private void applyTransform(PoseStack poseStack) {
        poseStack.translate(0.5D, 0.0D, 0.5D);
        //? if >=26.3
        poseStack.rotateDegrees(com.mojang.math.Axis.YP, leftHand ? -112.5F : 112.5F);
        //? if >=26.3
        poseStack.rotateDegrees(com.mojang.math.Axis.ZP, 180.0F);
        //? if <26.3
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(leftHand ? -112.5F : 112.5F));
        //? if <26.3
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(180.0F));
        poseStack.scale(0.5F, 0.5F, 0.5F);
    }

    public record RenderData(PlushieProfileCache.Skin skin) {}

    public record Unbaked(boolean leftHand) implements SpecialModelRenderer.Unbaked<RenderData> {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(Codec.BOOL.optionalFieldOf("left_hand", false).forGetter(Unbaked::leftHand))
                        .apply(instance, Unbaked::new));

        @Override
        public MapCodec<Unbaked> type() { return MAP_CODEC; }

        @Override
        public PlushieSpecialRenderer bake(BakingContext context) {
            return new PlushieSpecialRenderer(new PlushieModel(
                    context.entityModelSet().bakeLayer(DynamicPlushieBlockEntityRenderer.LAYER_LOCATION)), leftHand);
        }
    }
}
*///?}
