package net.syrupstudios.plushiefriends;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.syrupstudios.plushiefriends.block.DynamicPlushieBlock;
import net.syrupstudios.plushiefriends.block.entity.DynamicPlushieBlockEntity;
import net.syrupstudios.plushiefriends.data.PlushieDataManager;
import net.syrupstudios.plushiefriends.loot.SetPlushieFunction;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlushieFriends implements ModInitializer {
	public static final String MOD_ID = "plushie-friends";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final Map<String, GameProfile> RESOLVED_PROFILE_CACHE = new ConcurrentHashMap<>();

	public static LootItemFunctionType SET_PLUSHIE_FUNCTION;

	public static final DynamicPlushieBlock PLUSHIE_BLOCK = new DynamicPlushieBlock(
			FabricBlockSettings.copyOf(Blocks.WHITE_WOOL).noOcclusion()
	);

	public static final BlockItem PLUSHIE_ITEM = new BlockItem(PLUSHIE_BLOCK, new FabricItemSettings()) {
		@Override
		public void verifyTagAfterLoad(CompoundTag tag) {
			super.verifyTagAfterLoad(tag);
			resolvePlushieOwner(tag);
		}

		@Override
		protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack stack, BlockState state) {
			boolean updated = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
			applyCachedOwner(level, pos, state);
			return updated;
		}

		@Override
		public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
			super.inventoryTick(stack, level, entity, slotId, isSelected);
			applyCachedOwner(stack);
		}

		@Override
		public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
			super.appendHoverText(stack, level, tooltip, flag);

			if (stack.hasTag()) {
				CompoundTag tag = stack.getTag();
				if (tag != null && tag.contains("BlockEntityTag", 10)) {
					CompoundTag blockEntityTag = tag.getCompound("BlockEntityTag");

					if (blockEntityTag.contains("PlushieOwner")) {
						String ownerName = "";

						if (blockEntityTag.contains("PlushieOwner", 10)) {
							CompoundTag ownerTag = blockEntityTag.getCompound("PlushieOwner");
							if (ownerTag.contains("Name", 8)) {
								ownerName = ownerTag.getString("Name");
							}
						} else if (blockEntityTag.contains("PlushieOwner", 8)) {
							ownerName = blockEntityTag.getString("PlushieOwner");
						}

						if (!ownerName.isEmpty()) {
							tooltip.add(Component.literal(ownerName).withStyle(ChatFormatting.AQUA));
						}
					}

					if (blockEntityTag.contains("PlushieLore", 9)) {
						ListTag loreList = blockEntityTag.getList("PlushieLore", 8);
						for (int i = 0; i < loreList.size(); i++) {
							tooltip.add(Component.literal(loreList.getString(i)).withStyle(ChatFormatting.GRAY));
						}
					}
				}
			}
		}
	};

	private static void resolvePlushieOwner(CompoundTag tag) {
		if (!tag.contains("BlockEntityTag", 10)) {
			return;
		}

		CompoundTag blockEntityTag = tag.getCompound("BlockEntityTag");
		if (!blockEntityTag.contains("PlushieOwner", 8)) {
			return;
		}

		String ownerName = blockEntityTag.getString("PlushieOwner");
		if (ownerName.isEmpty()) {
			return;
		}

		GameProfile cached = RESOLVED_PROFILE_CACHE.get(ownerName);
		if (cached != null) {
			writeProfileToTag(blockEntityTag, cached);
			return;
		}

		SkullBlockEntity.updateGameprofile(new GameProfile(null, ownerName), (profile) -> {
			if (profile.getProperties().containsKey("textures")) {
				RESOLVED_PROFILE_CACHE.put(ownerName, profile);
			}
		});
	}

	private static void applyCachedOwner(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		if (tag == null || !tag.contains("BlockEntityTag", 10)) {
			return;
		}

		CompoundTag blockEntityTag = tag.getCompound("BlockEntityTag");
		if (!blockEntityTag.contains("PlushieOwner", 8)) {
			return;
		}

		GameProfile cachedProfile = RESOLVED_PROFILE_CACHE.get(blockEntityTag.getString("PlushieOwner"));
		if (cachedProfile == null || !cachedProfile.getProperties().containsKey("textures")) {
			return;
		}

		writeProfileToTag(blockEntityTag, cachedProfile);
	}

	private static void applyCachedOwner(Level level, BlockPos pos, BlockState state) {
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (!(blockEntity instanceof DynamicPlushieBlockEntity plushieBlockEntity)) {
			return;
		}

		GameProfile owner = plushieBlockEntity.getOwner();
		if (owner == null || owner.getProperties().containsKey("textures")) {
			return;
		}

		GameProfile cachedProfile = RESOLVED_PROFILE_CACHE.get(owner.getName());
		if (cachedProfile == null || !cachedProfile.getProperties().containsKey("textures")) {
			return;
		}

		plushieBlockEntity.setOwner(cachedProfile);
	}

	private static void writeProfileToTag(CompoundTag blockEntityTag, GameProfile profile) {
		CompoundTag profileTag = new CompoundTag();
		NbtUtils.writeGameProfile(profileTag, profile);
		blockEntityTag.put("PlushieOwner", profileTag);
	}

	@Override
	public void onInitialize() {
		Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(MOD_ID, "plushie"), PLUSHIE_BLOCK);
		Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(MOD_ID, "plushie"), PLUSHIE_ITEM);

		DynamicPlushieBlockEntity.TYPE = Registry.register(
				BuiltInRegistries.BLOCK_ENTITY_TYPE,
				new ResourceLocation(MOD_ID, "plushie_be"),
				FabricBlockEntityTypeBuilder.create(DynamicPlushieBlockEntity::new, PLUSHIE_BLOCK).build()
		);

		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new PlushieDataManager());

		SET_PLUSHIE_FUNCTION = Registry.register(
				BuiltInRegistries.LOOT_FUNCTION_TYPE,
				new ResourceLocation(MOD_ID, "set_plushie"),
				new LootItemFunctionType(new SetPlushieFunction.Serializer())
		);

		LOGGER.info("Plushie Friends initialized successfully!");
	}
}