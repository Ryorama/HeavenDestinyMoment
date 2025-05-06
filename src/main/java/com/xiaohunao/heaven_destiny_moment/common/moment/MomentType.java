package com.xiaohunao.heaven_destiny_moment.common.moment;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

public class MomentType<T extends MomentInstance> {
    private final MomentSupplier<T> factory;

    public MomentType(MomentSupplier<T> factory) {
        this.factory = factory;
    }

    @Nullable
    public T create(UUID uuid, Level level, Moment moment) {
        return factory.create(uuid, level, moment);
    }


    public static final class Builder<T extends MomentInstance> {
        private final MomentSupplier<T> factory;
        public Builder(MomentSupplier<T> factory) {
            this.factory = factory;
        }

        public MomentType<T> build() {
            return new MomentType<>(factory);
        }
    }

    @FunctionalInterface
    public interface MomentSupplier<T> {
        T create(UUID uuid, Level level, Moment moment);
    }
}
