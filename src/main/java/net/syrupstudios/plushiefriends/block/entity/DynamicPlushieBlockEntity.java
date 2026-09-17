package net.syrupstudios.plushiefriends.block.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
//? if >=1.21
/*import net.minecraft.core.HolderLookup;*/
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.syrupstudios.plushiefriends.util.PlushieNbtHelper;
import net.syrupstudios.plushiefriends.util.PlushieProfileManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DynamicPlushieBlockEntity extends BlockEntity {
    private GameProfile owner = null;
    private ListTag lore = new ListTag();
    private boolean isResolving = false;
    private final long creationTime = System.currentTimeMillis();

    public boolean isSafeToForceRender() {
        return (System.currentTimeMillis() - this.creationTime) > 250;
    }

    public DynamicPlushieBlockEntity(BlockPos pos, BlockState state) {
        super(net.syrupstudios.plushiefriends.PlushieFriends.PLUSHIE_BLOCK_ENTITY, pos, state);
    }

    public void setOwner(@Nullable GameProfile profile) {
        this.owner = profile;
        this.setChanged();
    }

    @Nullable
    public GameProfile getOwner() {
        return this.owner;
    }

    public boolean applyItemData(CompoundTag itemTag) {
        boolean updated = false;
        GameProfile itemOwner = PlushieNbtHelper.getOwnerFromRoot(itemTag);
        if (itemOwner != null) {
            this.owner = itemOwner;
            updated = true;
        }

        if (PlushieNbtHelper.hasLoreInRoot(itemTag)) {
            List<String> itemLore = PlushieNbtHelper.getLoreFromRoot(itemTag);
            this.lore = new ListTag();
            for (String line : itemLore) {
                this.lore.add(StringTag.valueOf(line));
            }
            updated = true;
        }
        if (updated) {
            this.setChanged();
        }
        return updated;
    }

    //? if >=26.2 {
    /*@Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        CompoundTag tag = new CompoundTag();
        input.read(PlushieNbtHelper.PLUSHIE_OWNER, com.mojang.serialization.Codec.PASSTHROUGH)
                .ifPresent(value -> tag.put(PlushieNbtHelper.PLUSHIE_OWNER, value.convert(net.minecraft.nbt.NbtOps.INSTANCE).getValue()));
        this.owner = PlushieNbtHelper.getOwnerFromBlockEntityTag(tag);
        this.lore = new ListTag();
        input.read(PlushieNbtHelper.PLUSHIE_LORE, com.mojang.serialization.Codec.STRING.listOf())
                .ifPresent(lines -> lines.forEach(line -> this.lore.add(StringTag.valueOf(line))));
    }
    *///?} else if >=1.21 {
    /*@Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loadPlushieData(tag);
    }
    *///?} else {
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadPlushieData(tag);
    }
    //?}

    //? if <26.2 {
    private void loadPlushieData(CompoundTag tag) {
        this.owner = PlushieNbtHelper.getOwnerFromBlockEntityTag(tag);

        if (tag.contains(PlushieNbtHelper.PLUSHIE_LORE, PlushieNbtHelper.TAG_LIST)) {
            this.lore = tag.getList(PlushieNbtHelper.PLUSHIE_LORE, PlushieNbtHelper.TAG_STRING);
        } else {
            this.lore = new ListTag();
        }
    }

    //?}
    //? if >=26.2 {
    /*@Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        CompoundTag tag = new CompoundTag();
        savePlushieData(tag);
        if (tag.get(PlushieNbtHelper.PLUSHIE_OWNER) != null) {
            output.store(PlushieNbtHelper.PLUSHIE_OWNER, com.mojang.serialization.Codec.PASSTHROUGH,
                    new com.mojang.serialization.Dynamic<>(net.minecraft.nbt.NbtOps.INSTANCE, tag.get(PlushieNbtHelper.PLUSHIE_OWNER)));
        }
        output.store(PlushieNbtHelper.PLUSHIE_LORE, com.mojang.serialization.Codec.STRING.listOf(),
                PlushieNbtHelper.getLoreFromBlockEntityTag(tag));
    }
    *///?} else if >=1.21 {
    /*@Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        savePlushieData(tag);
    }
    *///?} else {
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        savePlushieData(tag);
    }
    //?}

    private void savePlushieData(CompoundTag tag) {
        if (this.owner != null) {
            PlushieNbtHelper.writeOwnerToBlockEntityTag(tag, this.owner);
        }
        if (this.lore != null && !this.lore.isEmpty()) {
            tag.put(PlushieNbtHelper.PLUSHIE_LORE, this.lore);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    //? if >=1.21 {
    /*@Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }
    *///?} else {
    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }
    //?}

    //? if >=26.2 {
    /*public static void serverTick(Level level, BlockPos pos, BlockState state, DynamicPlushieBlockEntity blockEntity) {
        if (blockEntity.owner == null || blockEntity.owner.properties().containsKey("textures")) {
            return;
        }

        GameProfile cachedProfile = PlushieProfileManager.getCachedProfile(blockEntity.owner.name());
        if (cachedProfile != null && cachedProfile.properties().containsKey("textures")) {
            blockEntity.owner = cachedProfile;
            blockEntity.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            return;
        }

        if (!blockEntity.isResolving && PlushieProfileManager.shouldAttemptResolution(blockEntity.owner.name())) {
            blockEntity.isResolving = true;

            PlushieProfileManager.resolveProfileAsync(blockEntity.owner.name(), profile -> {
                Runnable applyResult = () -> {
                    if (profile != null) {
                        blockEntity.owner = profile;
                        blockEntity.setChanged();
                        level.sendBlockUpdated(pos, state, state, 3);
                    }
                    blockEntity.isResolving = false;
                };
                if (level.getServer() != null) {
                    level.getServer().execute(applyResult);
                } else {
                    applyResult.run();
                }
            });
        }
    }
    *///?} else {
    public static void serverTick(Level level, BlockPos pos, BlockState state, DynamicPlushieBlockEntity blockEntity) {
        if (blockEntity.owner == null || blockEntity.owner.getProperties().containsKey("textures")) {
            return;
        }

        GameProfile cachedProfile = PlushieProfileManager.getCachedProfile(blockEntity.owner.getName());
        if (cachedProfile != null && cachedProfile.getProperties().containsKey("textures")) {
            blockEntity.owner = cachedProfile;
            blockEntity.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            return;
        }

        if (!blockEntity.isResolving && PlushieProfileManager.shouldAttemptResolution(blockEntity.owner.getName())) {
            blockEntity.isResolving = true;

            PlushieProfileManager.resolveProfileAsync(blockEntity.owner.getName(), profile -> {
                Runnable applyResult = () -> {
                    if (profile != null) {
                        blockEntity.owner = profile;
                        blockEntity.setChanged();
                        level.sendBlockUpdated(pos, state, state, 3);
                    }
                    blockEntity.isResolving = false;
                };
                if (level.getServer() != null) {
                    level.getServer().execute(applyResult);
                } else {
                    applyResult.run();
                }
            });
        }
    }
    //?}
}
