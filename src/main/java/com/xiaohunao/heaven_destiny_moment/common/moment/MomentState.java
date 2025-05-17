package com.xiaohunao.heaven_destiny_moment.common.moment;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum MomentState implements StringRepresentable {
    CREATE("create", 0),
    READY("ready", 1),
    START("start", 2),
    ONGOING("ongoing", 3),
    VICTORY("victory", 4),
    LOSE("lose", 5),
    END("end", 6);
    public static final Codec<MomentState> CODEC = StringRepresentable.fromEnum(MomentState::values);
    private final String name;
    private final int index;

    MomentState(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public static MomentState wrap(Object object) {
        if (object == null) {
            return null;
        }

        if (object instanceof MomentState state) {
            return state;
        }

        try {
            return valueOf(object.toString().toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    @NotNull
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}