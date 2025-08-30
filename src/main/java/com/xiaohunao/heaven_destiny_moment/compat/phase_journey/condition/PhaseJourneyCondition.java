package com.xiaohunao.heaven_destiny_moment.compat.phase_journey.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.compat.phase_journey.init.HDMConditions;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import org.confluence.phase_journey.common.init.PJAttachments;
import org.confluence.phase_journey.common.util.PhaseUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public record PhaseJourneyCondition(Type type, ResourceLocation phase) implements ICondition {
    public static final MapCodec<PhaseJourneyCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Type.CODEC.fieldOf("type").forGetter(PhaseJourneyCondition::type),
            ResourceLocation.CODEC.fieldOf("phase").forGetter(PhaseJourneyCondition::phase)
    ).apply(instance, PhaseJourneyCondition::new));

    public static PhaseJourneyCondition of(Type type, ResourceLocation phase) {
        return new PhaseJourneyCondition(type, phase);
    }

    @Override
    public boolean matches(MomentInstance instance, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer) {
        return switch (type) {
            case MOMENT -> instance.getData(PJAttachments.PHASE).getPhases().contains(phase);
            case PLAYER -> serverPlayer != null && PhaseUtils.hadPlayerReachedPhase(phase, serverPlayer);
            case LEVEL -> PhaseUtils.hadLevelFinishedPhase(phase, instance.getLevel());
            case null -> false;
        };
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }

    public enum Type implements StringRepresentable {
        MOMENT,
        PLAYER,
        LEVEL;

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

        @Override
        @NotNull
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
