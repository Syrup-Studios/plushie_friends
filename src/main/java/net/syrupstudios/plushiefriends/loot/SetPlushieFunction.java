package net.syrupstudios.plushiefriends.loot;

import com.mojang.authlib.GameProfile;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
//? if >=1.21 {
/*import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
*///?}
//? if >=26.2 {
/*import net.minecraft.resources.Identifier;
*///?} else
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
//? if >=26.2
/*import net.minecraft.world.level.storage.loot.functions.LootItemFunction;*/
//? if <26.2
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
//? if >=26.3
/*import net.minecraft.core.Holder;*/
import net.minecraft.nbt.CompoundTag;
import net.syrupstudios.plushiefriends.PlushieFriends;
import net.syrupstudios.plushiefriends.data.PlushieDataManager;
import net.syrupstudios.plushiefriends.util.PlushieNbtHelper;
import net.syrupstudios.plushiefriends.item.PlushieItemData;

import java.util.List;

public class SetPlushieFunction extends LootItemConditionalFunction {
    //? if >=26.3 {
    /*public static final MapCodec<SetPlushieFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
            commonFields(instance)
                    .and(Identifier.CODEC.fieldOf("id").forGetter(function -> function.plushieId))
                    .apply(instance, SetPlushieFunction::new)
    );

    *///?} else if >=26.2 {
    /*public static final MapCodec<SetPlushieFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
            commonFields(instance)
                    .and(Identifier.CODEC.fieldOf("id").forGetter(function -> function.plushieId))
                    .apply(instance, SetPlushieFunction::new)
    );

    *///?} else if >=1.21 {
    /*public static final MapCodec<SetPlushieFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
            commonFields(instance)
                    .and(ResourceLocation.CODEC.fieldOf("id").forGetter(function -> function.plushieId))
                    .apply(instance, SetPlushieFunction::new)
    );

    *///?}
    //? if >=26.2 {
    /*private final Identifier plushieId;
    *///?} else
    private final ResourceLocation plushieId;

    //? if >=26.3 {
    /*protected SetPlushieFunction(java.util.Optional<Holder<LootItemCondition>> predicates, Identifier plushieId) {
        super(predicates);
        this.plushieId = plushieId;
    }
    *///?} else if >=26.2 {
    /*protected SetPlushieFunction(List<LootItemCondition> predicates, Identifier plushieId) {
        super(predicates);
        this.plushieId = plushieId;
    }
    *///?} else if >=1.21 {
    /*protected SetPlushieFunction(List<LootItemCondition> predicates, ResourceLocation plushieId) {
        super(predicates);
        this.plushieId = plushieId;
    }
    *///?} else {
    protected SetPlushieFunction(LootItemCondition[] predicates, ResourceLocation plushieId) {
        super(predicates);
        this.plushieId = plushieId;
    }
    //?}

    //? if >=26.2 {
    /*@Override
    public MapCodec<? extends LootItemConditionalFunction> codec() {
        return CODEC;
    }
    *///?} else {
    @Override
    public LootItemFunctionType getType() {
        return PlushieFriends.SET_PLUSHIE_FUNCTION;
    }
    //?}

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        PlushieDataManager.PlushieDefinition def = PlushieDataManager.get(this.plushieId);
        if (def != null) {
            PlushieItemData.update(stack, itemTag -> {
                if (!def.ownerName().isEmpty()) {
                    GameProfile profile = PlushieDataManager.getResolvedProfile(def.ownerName(), context.getLevel().getServer());
                    if (profile != null) {
                        PlushieNbtHelper.writeOwnerToBlockEntityTag(itemTag, profile);
                    } else {
                        PlushieNbtHelper.writeOwnerStringToBlockEntityTag(itemTag, def.ownerName());
                    }
                }

                if (!def.lore().isEmpty()) {
                    PlushieNbtHelper.writeLoreToBlockEntityTag(itemTag, def.lore());
                }
            });
        }
        return stack;
    }

    //? if <1.21 {
    public static class Serializer extends LootItemConditionalFunction.Serializer<SetPlushieFunction> {
        @Override
        public SetPlushieFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            ResourceLocation id = new ResourceLocation(json.get("id").getAsString());
            return new SetPlushieFunction(conditions, id);
        }

        @Override
        public void serialize(JsonObject json, SetPlushieFunction value, JsonSerializationContext context) {
            super.serialize(json, value, context);
            json.addProperty("id", value.plushieId.toString());
        }
    }
    //?}
}
