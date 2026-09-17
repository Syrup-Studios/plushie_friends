package net.syrupstudios.plushiefriends.client;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
//? if fabric {
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
//? if <26.2
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
//? if <26.2
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
//? if >=26.2
/*import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.renderer.special.SpecialModelRenderers;*/
//?} else if neoforge {
/*import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
*///?}
//? if neoforge && >=26.2
/*import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;*/
import net.minecraft.client.Minecraft;
//? if <26.2
import net.minecraft.client.renderer.MultiBufferSource;
//? if <26.2
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.syrupstudios.plushiefriends.PlushieFriends;
import net.syrupstudios.plushiefriends.client.renderer.DynamicPlushieBlockEntityRenderer;
import net.syrupstudios.plushiefriends.client.renderer.PlushieModel;
import net.syrupstudios.plushiefriends.item.PlushieItemData;
import net.syrupstudios.plushiefriends.util.PlushieNbtHelper;
import net.syrupstudios.plushiefriends.util.PlushieProfileManager;

//? if neoforge && <26.2
/*@EventBusSubscriber(modid = PlushieFriends.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)*/
//? if neoforge && >=26.2
/*@EventBusSubscriber(modid = PlushieFriends.MOD_ID, value = Dist.CLIENT)*/
public final class PlushieFriendsClient
        //? if fabric
        implements ClientModInitializer
{
    private static PlushieModel itemModel;

    //? if fabric {
    @Override
    public void onInitializeClient() {
        //? if >=26.2
        /*ModelLayerRegistry.registerModelLayer(
                DynamicPlushieBlockEntityRenderer.LAYER_LOCATION,
                PlushieModel::createLayer
        );
        SpecialModelRenderers.ID_MAPPER.put(
                PlushieFriends.id("plushie"), PlushieSpecialRenderer.Unbaked.MAP_CODEC
        );*/
        //? if <26.2
        EntityModelLayerRegistry.registerModelLayer(DynamicPlushieBlockEntityRenderer.LAYER_LOCATION, PlushieModel::createLayer);
        BlockEntityRendererRegistry.register(
                PlushieFriends.PLUSHIE_BLOCK_ENTITY,
                DynamicPlushieBlockEntityRenderer::new
        );
        //? if <26.2
        BuiltinItemRendererRegistry.INSTANCE.register(PlushieFriends.PLUSHIE_ITEM, PlushieFriendsClient::renderItem);
    }
    //?} else if neoforge {
    /*@SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(DynamicPlushieBlockEntityRenderer.LAYER_LOCATION, PlushieModel::createLayer);
    }

    @SubscribeEvent
    public static void registerRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                PlushieFriends.PLUSHIE_BLOCK_ENTITY,
                DynamicPlushieBlockEntityRenderer::new
        );
    }
    *///?}

    //? if neoforge && >=26.2
    /*@SubscribeEvent
    public static void registerSpecialModelRenderer(RegisterSpecialModelRendererEvent event) {
        event.register(PlushieFriends.id("plushie"), PlushieSpecialRenderer.Unbaked.MAP_CODEC);
    }*/

    //? if <26.2 {
    public static void renderItem(
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay
    ) {
        if (itemModel == null) {
            itemModel = new PlushieModel(
                    Minecraft.getInstance().getEntityModels()
                            .bakeLayer(DynamicPlushieBlockEntityRenderer.LAYER_LOCATION)
            );
        }

        CompoundTag tag = PlushieItemData.read(stack);
        GameProfile owner = PlushieNbtHelper.getOwnerFromRoot(tag);
        if (owner != null && !owner.getProperties().containsKey("textures")) {
            GameProfile cached = PlushieProfileManager.getCachedProfile(owner.getName());
            if (cached != null && cached.getProperties().containsKey("textures")) {
                owner = cached;
            }
        }

        PlushieProfileCache.Skin skin = PlushieProfileCache.getSkin(owner);
        boolean leftHandView = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(leftHandView ? -112.5F : 112.5F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.scale(0.5F, 0.5F, 0.5F);

        VertexConsumer vertices = buffers.getBuffer(RenderType.entityCutoutNoCull(skin.textureLocation()));
        itemModel.render(poseStack, vertices, light, overlay, skin.slim());
        poseStack.popPose();
    }
    //?}
}
