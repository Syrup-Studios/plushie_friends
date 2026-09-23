package net.syrupstudios.plushiefriends.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.syrupstudios.plushiefriends.util.PlushieNbtHelper;
import net.syrupstudios.syruplibrary.item.ItemStackData;

import java.util.function.Consumer;

//? if >=1.21 {
/*import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ResolvableProfile;
*///?}

/** Adapts Plushie Friends' legacy fields to the library's custom item data. */
public final class PlushieItemData {
    private PlushieItemData() {}

    public static CompoundTag read(ItemStack stack) {
        CompoundTag result = ItemStackData.read(stack);
        mergeLegacy(stack, result);
        return result;
    }

    public static void update(ItemStack stack, Consumer<CompoundTag> updater) {
        ItemStackData.update(stack, data -> {
            mergeLegacy(stack, data);
            updater.accept(data);
            PlushieNbtHelper.migrateLegacyItemData(data);
        });
    }

    private static void mergeLegacy(ItemStack stack, CompoundTag result) {
        //? if >=26.2 {
        /*var blockData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        CompoundTag legacy = blockData == null ? new CompoundTag() : blockData.copyTagWithoutId();
        copyMissingField(result, legacy, PlushieNbtHelper.PLUSHIE_OWNER);
        copyMissingField(result, legacy, PlushieNbtHelper.PLUSHIE_LORE);
        ResolvableProfile profile = stack.get(DataComponents.PROFILE);
        if (!result.contains(PlushieNbtHelper.PLUSHIE_OWNER) && profile != null) {
            PlushieNbtHelper.writeOwnerToBlockEntityTag(result, profile.partialProfile());
        }
        *///?} else if >=1.21 {
        /*CompoundTag legacy = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        copyMissingField(result, legacy, PlushieNbtHelper.PLUSHIE_OWNER);
        copyMissingField(result, legacy, PlushieNbtHelper.PLUSHIE_LORE);
        ResolvableProfile profile = stack.get(DataComponents.PROFILE);
        if (!result.contains(PlushieNbtHelper.PLUSHIE_OWNER) && profile != null) {
            PlushieNbtHelper.writeOwnerToBlockEntityTag(result, profile.gameProfile());
        }
        *///?}
    }

    private static void copyMissingField(CompoundTag target, CompoundTag source, String field) {
        if (!target.contains(field) && source.contains(field) && source.get(field) != null) {
            target.put(field, source.get(field).copy());
        }
    }
}
