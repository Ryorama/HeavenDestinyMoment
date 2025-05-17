package com.xiaohunao.heaven_destiny_moment.common.context;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.common.AutoProbabilityCondition;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public record ConditionGroup(
        Optional<Pair<Boolean, List<ICondition>>> create,
        Optional<List<ICondition>> victory,
        Optional<List<ICondition>> lose,
        Optional<List<ICondition>> end) {

    public static final Codec<Pair<Boolean, List<ICondition>>> CREATE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.lenientOptionalFieldOf("auto", false).forGetter(Pair::getFirst),
            Codec.list(ICondition.CODEC).fieldOf("conditions").orElseGet(List::of).forGetter(Pair::getSecond)
    ).apply(instance, Pair::new));
    public static final Codec<ConditionGroup> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CREATE_CODEC.optionalFieldOf("create").forGetter(ConditionGroup::create),
            Codec.list(ICondition.CODEC).optionalFieldOf("victory").forGetter(ConditionGroup::victory),
            Codec.list(ICondition.CODEC).optionalFieldOf("lose").forGetter(ConditionGroup::lose),
            Codec.list(ICondition.CODEC).optionalFieldOf("end").forGetter(ConditionGroup::end)
    ).apply(instance, ConditionGroup::new));
    private static final Logger LOGGER = LoggerFactory.getLogger(ConditionGroup.class);

    // 添加事件调试的GameRule
    public static final GameRules.Key<GameRules.BooleanValue> RULE_MOMENT_DEBUG =
            GameRules.register("momentDebug", GameRules.Category.MISC, GameRules.BooleanValue.create(false));

    public static class Builder {
        private Pair<Boolean, List<ICondition>> create;
        private List<ICondition> victory;
        private List<ICondition> lose;
        private List<ICondition> end;

        public ConditionGroup build() {
            return new ConditionGroup(Optional.ofNullable(create), Optional.ofNullable(victory), Optional.ofNullable(lose), Optional.ofNullable(end));
        }

        public Builder create(boolean auto, ICondition... conditions) {
            if (this.create == null) {
                this.create = Pair.of(auto, List.of(conditions));
            }
            return this;
        }

        public Builder victory(ICondition... conditions) {
            this.victory = add(this.victory, conditions);
            return this;
        }

        public Builder lose(ICondition... conditions) {
            this.lose = add(this.lose, conditions);
            return this;
        }

        public Builder end(ICondition... conditions) {
            this.end = add(this.end, conditions);
            return this;
        }

        private List<ICondition> add(List<ICondition> conditions, ICondition... condition) {
            if (conditions == null) {
                conditions = Lists.newArrayList();
            }
            conditions.addAll(List.of(condition));
            return conditions;
        }
    }
}
