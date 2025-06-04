package com.xiaohunao.heaven_destiny_moment.common.moment;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum MomentState implements StringRepresentable {
    READY("ready"),
    START("start"),
    ONGOING("ongoing"),
    VICTORY("victory"),
    LOSE("lose"),
    END("end");
    public static final Codec<MomentState> CODEC = StringRepresentable.fromEnum(MomentState::values);
    private final String name;


    MomentState(String name) {
        this.name = name;
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

    public String getName() {
        return name;
    }
}