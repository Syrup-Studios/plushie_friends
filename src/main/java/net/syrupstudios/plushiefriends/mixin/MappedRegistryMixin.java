package net.syrupstudios.plushiefriends.mixin;

import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MappedRegistry.class)
public class MappedRegistryMixin {
    private static ResourceLocation remapId(ResourceLocation id) {
        if (id != null && "plushie-friends".equals(id.getNamespace())) {
            return net.syrupstudios.plushiefriends.PlushieFriends.id(id.getPath());
        }
        return id;
    }

    //? if >=26 {
    /*private static ResourceKey<?> remapKey(ResourceKey<?> key) {
        if (key != null && "plushie-friends".equals(key.identifier().getNamespace())) {
            return ResourceKey.create(ResourceKey.createRegistryKey(key.registry()),
                    net.syrupstudios.plushiefriends.PlushieFriends.id(key.identifier().getPath()));
        }
        return key;
    }

    @ModifyVariable(method = "getValue(Lnet/minecraft/resources/ResourceLocation;)Ljava/lang/Object;", at = @At("HEAD"), argsOnly = true)
    private ResourceLocation remapGetValueId(ResourceLocation id) { return remapId(id); }

    @ModifyVariable(method = "getValue(Lnet/minecraft/resources/ResourceKey;)Ljava/lang/Object;", at = @At("HEAD"), argsOnly = true)
    private ResourceKey<?> remapGetValueKey(ResourceKey<?> key) { return remapKey(key); }

    @ModifyVariable(method = "get(Lnet/minecraft/resources/ResourceLocation;)Ljava/util/Optional;", at = @At("HEAD"), argsOnly = true)
    private ResourceLocation remapGetId(ResourceLocation id) { return remapId(id); }

    @ModifyVariable(method = "get(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;", at = @At("HEAD"), argsOnly = true)
    private ResourceKey<?> remapGetKey(ResourceKey<?> key) { return remapKey(key); }

    *///?} else {
    private static ResourceKey<?> remapKey(ResourceKey<?> key) {
        if (key != null && "plushie-friends".equals(key.location().getNamespace())) {
            return ResourceKey.create(ResourceKey.createRegistryKey(key.registry()),
                    net.syrupstudios.plushiefriends.PlushieFriends.id(key.location().getPath()));
        }
        return key;
    }

    @ModifyVariable(method = "get(Lnet/minecraft/resources/ResourceLocation;)Ljava/lang/Object;", at = @At("HEAD"), argsOnly = true)
    private ResourceLocation remapGetId(ResourceLocation id) { return remapId(id); }

    @ModifyVariable(method = "get(Lnet/minecraft/resources/ResourceKey;)Ljava/lang/Object;", at = @At("HEAD"), argsOnly = true)
    private ResourceKey<?> remapGetKey(ResourceKey<?> key) { return remapKey(key); }

    @ModifyVariable(method = "getHolder(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;", at = @At("HEAD"), argsOnly = true)
    private ResourceKey<?> remapGetHolderKey(ResourceKey<?> key) { return remapKey(key); }

    //?}

    @ModifyVariable(method = "containsKey(Lnet/minecraft/resources/ResourceLocation;)Z", at = @At("HEAD"), argsOnly = true)
    private ResourceLocation remapContainsKeyId(ResourceLocation id) { return remapId(id); }

    @ModifyVariable(method = "containsKey(Lnet/minecraft/resources/ResourceKey;)Z", at = @At("HEAD"), argsOnly = true)
    private ResourceKey<?> remapContainsKeyKey(ResourceKey<?> key) { return remapKey(key); }
}
