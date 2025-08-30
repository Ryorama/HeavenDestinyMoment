package com.xiaohunao.heaven_destiny_moment.common.context.condition.common;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.common.automation.AutomationContext;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMMapCodecRegisters;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record OrCondition(ICondition or, List<ICondition> trueCondition, List<ICondition> falseCondition) implements ICondition {
    public static final MapCodec<OrCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ICondition.CODEC.fieldOf("or").forGetter(OrCondition::or),
            ICondition.CODEC.listOf().fieldOf("true").forGetter(OrCondition::trueCondition),
            ICondition.CODEC.listOf().fieldOf("false").forGetter(OrCondition::falseCondition)
    ).apply(instance, OrCondition::new));

    @Override
    public boolean matches(AutomationContext context) {
        if (or.matches(context)) {
            return trueCondition.stream().allMatch(condition -> condition.matches(context));
        } else {
            return falseCondition.stream().allMatch(condition -> condition.matches(context));
        }
    }

    public static OrCondition of(ICondition or, List<ICondition> trueCondition, List<ICondition> falseCondition) {
        return new OrCondition(or, trueCondition, falseCondition);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }




}
