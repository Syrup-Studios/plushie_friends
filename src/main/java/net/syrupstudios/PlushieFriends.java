package net.syrupstudios;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.syrupstudios.block.DynamicPlushieBlock;
import net.syrupstudios.block.entity.DynamicPlushieBlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlushieFriends implements ModInitializer {
	public static final String MOD_ID = "plushie-friends";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final DynamicPlushieBlock DYNAMIC_PLUSHIE_BLOCK = new DynamicPlushieBlock(
			FabricBlockSettings.copyOf(Blocks.WHITE_WOOL).noOcclusion()
	);

	public static final BlockItem DYNAMIC_PLUSHIE_ITEM = new BlockItem(
			DYNAMIC_PLUSHIE_BLOCK,
			new FabricItemSettings()
	);

	@Override
	public void onInitialize() {
		Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(MOD_ID, "dynamic_plushie"), DYNAMIC_PLUSHIE_BLOCK);
		Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(MOD_ID, "dynamic_plushie"), DYNAMIC_PLUSHIE_ITEM);

		DynamicPlushieBlockEntity.TYPE = Registry.register(
				BuiltInRegistries.BLOCK_ENTITY_TYPE,
				new ResourceLocation(MOD_ID, "dynamic_plushie_be"),
				FabricBlockEntityTypeBuilder.create(DynamicPlushieBlockEntity::new, DYNAMIC_PLUSHIE_BLOCK).build()
		);

		LOGGER.info("Plushie Friends initialized successfully!");
	}
}