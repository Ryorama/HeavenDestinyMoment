package com.xiaohunao.heaven_destiny_moment.common.moment;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum MomentState implements StringRepresentable {
    UNINITIALIZED("uninitialized"),
    READY("ready"),
    START("start"),
    ONGOING("ongoing"),
    VICTORY("victory"),
    LOSE("lose"),
    END("end");
    public static final Codec<MomentState> CODEC = StringRepresentable.fromEnum(MomentState::values);
    private final String name;


    private static final Map<String, MomentState> BY_NAME = Arrays.stream(values())
            .collect(Collectors.toMap(MomentState::getSerializedName, Function.identity()));

    MomentState(String name) {
        this.name = name;
    }

    @Nullable
    public static MomentState wrap(Object object) {
        if (object == null) {
            return null;
        }

        if (object instanceof MomentState state) {
            return state;
        }

        String str = object.toString().toLowerCase(Locale.ROOT);
        MomentState result = BY_NAME.get(str);

        if (result != null) {
            return result;
        }

        try {
            return valueOf(object.toString().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Nullable
    public static MomentState fromString(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        return BY_NAME.get(str.toLowerCase(Locale.ROOT));
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