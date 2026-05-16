package net.syrupstudios.block.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DynamicPlushieBlockEntity extends BlockEntity {
    public static BlockEntityType<DynamicPlushieBlockEntity> TYPE;

    private GameProfile owner = null;
    private boolean isResolving = false;

    public DynamicPlushieBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    public void setOwner(@Nullable GameProfile profile) {
        this.owner = profile;
        this.setChanged();
    }

    @Nullable
    public GameProfile getOwner() {
        return this.owner;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("PlushieOwner", 10)) {
            this.owner = NbtUtils.readGameProfile(tag.getCompound("PlushieOwner"));
        } else if (tag.contains("PlushieOwner", 8)) {
            String name = tag.getString("PlushieOwner");
            if (!name.isEmpty()) {
                this.owner = new GameProfile(null, name);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.owner != null) {
            CompoundTag profileTag = new CompoundTag();
            NbtUtils.writeGameProfile(profileTag, this.owner);
            tag.put("PlushieOwner", profileTag);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DynamicPlushieBlockEntity blockEntity) {
        if (blockEntity.owner != null && !blockEntity.owner.getProperties().containsKey("textures") && !blockEntity.isResolving) {
            blockEntity.isResolving = true;
            SkullBlockEntity.updateGameprofile(blockEntity.owner, (profile) -> {
                blockEntity.owner = profile;
                blockEntity.isResolving = false;
                blockEntity.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            });
        }
    }
}