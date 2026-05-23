package net.syrupstudios.plushiefriends.loot;

import com.mojang.authlib.GameProfile;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.syrupstudios.plushiefriends.PlushieFriends;
import net.syrupstudios.plushiefriends.data.PlushieDataManager;

public class SetPlushieFunction extends LootItemConditionalFunction {
    private final ResourceLocation plushieId;

    protected SetPlushieFunction(LootItemCondition[] predicates, ResourceLocation plushieId) {
        super(predicates);
        this.plushieId = plushieId;
    }

    @Override
    public LootItemFunctionType getType() {
        return PlushieFriends.SET_PLUSHIE_FUNCTION;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        PlushieDataManager.PlushieDefinition def = PlushieDataManager.get(this.plushieId);
        if (def != null) {
            CompoundTag blockEntityTag = stack.getOrCreateTagElement("BlockEntityTag");

            if (!def.ownerName().isEmpty()) {
                GameProfile profile = PlushieDataManager.getResolvedProfile(def.ownerName(), context.getLevel().getServer());
                if (profile != null) {
                    CompoundTag profileTag = new CompoundTag();
                    NbtUtils.writeGameProfile(profileTag, profile);
                    blockEntityTag.put("PlushieOwner", profileTag);
                } else {
                    blockEntityTag.putString("PlushieOwner", def.ownerName());
                }
            }

            if (!def.lore().isEmpty()) {
                ListTag loreList = new ListTag();
                for (String line : def.lore()) {
                    loreList.add(StringTag.valueOf(line));
                }
                blockEntityTag.put("PlushieLore", loreList);
            }
        }
        return stack;
    }

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
}