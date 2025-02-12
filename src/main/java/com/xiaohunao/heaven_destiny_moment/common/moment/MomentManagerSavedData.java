package com.xiaohunao.heaven_destiny_moment.common.moment;

import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.common.mixed.MomentManagerMixed;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MomentManagerSavedData extends SavedData{
    private static final String NAME = HeavenDestinyMoment.MODID + "_moment_manager_data";
    private final ServerLevel level;
    public static void init(ServerLevel level) {
        Factory<MomentManagerSavedData> factory = new Factory<>(
                () -> new MomentManagerSavedData(level),
                (tag, prov) -> new MomentManagerSavedData(level, tag));
        level.getDataStorage().computeIfAbsent(factory, NAME);
    }

    public MomentManagerSavedData(ServerLevel level) {
        this.level = level;
    }

    public MomentManagerSavedData(ServerLevel level, CompoundTag compoundTag) {
        this.level = level;
        ((MomentManagerMixed) level).heaven_destiny_moment$getMomentManager().deserializeNBT(compoundTag);
    }

    @Override
    @NotNull
    public CompoundTag save(@NotNull CompoundTag compoundTag, @NotNull HolderLookup.Provider provider) {
        return Objects.requireNonNullElseGet(((MomentManagerMixed) level).heaven_destiny_moment$getMomentManager().serializeNBT(), CompoundTag::new);
    }

    @Override
    public boolean isDirty() {
        return true;
    }
}
