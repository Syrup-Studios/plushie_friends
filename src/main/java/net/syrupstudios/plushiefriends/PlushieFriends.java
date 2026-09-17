package net.syrupstudios.plushiefriends;

//? if >=26 {
/*import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
*///?} else if >=1.21 {
/*import net.minecraft.resources.ResourceLocation;
*///?} else {
import net.minecraft.resources.ResourceLocation;
//?}
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
//? if >=26 {
/*import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
*///?} else {
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
//?}
import net.syrupstudios.plushiefriends.block.DynamicPlushieBlock;
import net.syrupstudios.plushiefriends.block.entity.DynamicPlushieBlockEntity;
import net.syrupstudios.plushiefriends.item.PlushieBlockItem;
import net.syrupstudios.plushiefriends.loot.SetPlushieFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;

public final class PlushieFriends {
    public static final String MOD_ID = "plushie_friends";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static DynamicPlushieBlock PLUSHIE_BLOCK;
    public static BlockItem PLUSHIE_ITEM;
    public static BlockEntityType<DynamicPlushieBlockEntity> PLUSHIE_BLOCK_ENTITY;
    //? if >=26 {
    /*public static MapCodec<? extends LootItemFunction> SET_PLUSHIE_FUNCTION;
    *///?} else
    public static LootItemFunctionType SET_PLUSHIE_FUNCTION;

    private PlushieFriends() { }

    public static DynamicPlushieBlock createPlushieBlock() {
        if (PLUSHIE_BLOCK == null) {
            //? if >=26 {
            /*PLUSHIE_BLOCK = new DynamicPlushieBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WOOL.white())
                    .noOcclusion().setId(net.minecraft.resources.ResourceKey.create(Registries.BLOCK, id("plushie"))));
            *///?} else if >=1.21 {
            /*PLUSHIE_BLOCK = new DynamicPlushieBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL).noOcclusion());
            *///?} else {
            PLUSHIE_BLOCK = new DynamicPlushieBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL).noOcclusion());
            //?}
        }
        return PLUSHIE_BLOCK;
    }

    public static BlockItem createPlushieItem() {
        if (PLUSHIE_ITEM == null) {
            //? if >=26 {
            /*PLUSHIE_ITEM = new PlushieBlockItem(createPlushieBlock(),
                    new net.minecraft.world.item.Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)
                            .setId(net.minecraft.resources.ResourceKey.create(Registries.ITEM, id("plushie"))));
            *///?} else {
            PLUSHIE_ITEM = new PlushieBlockItem(createPlushieBlock(),
                    new net.minecraft.world.item.Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
            //?}
        }
        return PLUSHIE_ITEM;
    }

    public static BlockEntityType<DynamicPlushieBlockEntity> createPlushieBlockEntity() {
        if (PLUSHIE_BLOCK_ENTITY == null) {
            //? if >=26 {
            /*PLUSHIE_BLOCK_ENTITY = new BlockEntityType<>(DynamicPlushieBlockEntity::new, Set.of(createPlushieBlock()));
            *///?} else {
            PLUSHIE_BLOCK_ENTITY = new BlockEntityType<>(DynamicPlushieBlockEntity::new, Set.of(createPlushieBlock()), null);
            //?}
        }
        return PLUSHIE_BLOCK_ENTITY;
    }

    //? if >=26 {
    /*public static MapCodec<? extends LootItemFunction> createSetPlushieFunction() {
    *///?} else
    public static LootItemFunctionType createSetPlushieFunction() {
        if (SET_PLUSHIE_FUNCTION == null) {
            //? if >=26 {
            /*SET_PLUSHIE_FUNCTION = SetPlushieFunction.CODEC;
            *///?} else if >=1.21 {
            /*SET_PLUSHIE_FUNCTION = new LootItemFunctionType(SetPlushieFunction.CODEC);
            *///?} else {
            SET_PLUSHIE_FUNCTION = new LootItemFunctionType(new SetPlushieFunction.Serializer());
            //?}
        }
        return SET_PLUSHIE_FUNCTION;
    }

    public static void createContent() {
        createPlushieBlock();
        createPlushieItem();
        createPlushieBlockEntity();
        createSetPlushieFunction();
    }

    //? if >=26 {
    /*public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
    *///?} else if >=1.21 {
    /*public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
    *///?} else {
    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
    //?}

    public static void initialized(String loader) {
        LOGGER.info("Plushie Friends initialized on {}.", loader);
    }
}
