package com.xiaohunao.heaven_destiny_moment.common.context.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.context.amount.IAmount;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMConditions;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.moment.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record MomentHistoryCondition(IAmount time, MomentType<?> momentType) implements ICondition {
    public static final MapCodec<MomentHistoryCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            IAmount.CODEC.fieldOf("time").forGetter(MomentHistoryCondition::time),
            HDMRegistries.MOMENT_TYPE.byNameCodec().fieldOf("moment_type").forGetter(MomentHistoryCondition::momentType)
    ).apply(instance, MomentHistoryCondition::new));

    @Override
    public boolean matches(MomentInstance instance, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer) {
        int amount = time.getAmount();
        long gameTime = instance.getLevel().getGameTime();

        MomentInstanceManager momentInstanceManager = MomentInstanceManager.of(instance.getLevel());
        MomentHistoryManager momentHistoryManager = momentInstanceManager.getMomentHistoryManager();
        List<MomentRunningRecord> history = momentHistoryManager.getHistory(momentType);

        return history.stream().allMatch(momentRunningRecord
                -> gameTime - momentRunningRecord.getEndTime() > amount
        );
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return HDMConditions.MOMENT_HISTORY.get();
    }
}
