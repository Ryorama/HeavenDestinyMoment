package com.xiaohunao.heaven_destiny_moment.common.context.condition.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.automation.AutomationContext;
import com.xiaohunao.heaven_destiny_moment.common.context.IBuilderConverter;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.predicate.AttributePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.PlayerPredicate;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

public record PlayerCondition(Type type,
                              Optional<PlayerPredicate> playerPredicate,
                              Optional<EntityPredicate> entityPredicate,
                              Optional<AttributePredicate> attributePredicate,
                              Optional<List<ICondition>> subConditions
) implements ICondition {

    public static final MapCodec<PlayerCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Type.CODEC.fieldOf("player_type").forGetter(PlayerCondition::type),
            PlayerPredicate.CODEC.codec().optionalFieldOf("player").forGetter(PlayerCondition::playerPredicate),
            EntityPredicate.CODEC.optionalFieldOf("entity").forGetter(PlayerCondition::entityPredicate),
            AttributePredicate.CODEC.codec().optionalFieldOf("attribute").forGetter(PlayerCondition::attributePredicate),
            ICondition.CODEC.listOf().optionalFieldOf("sub_conditions").forGetter(PlayerCondition::subConditions)
    ).apply(instance, PlayerCondition::new));

    public static Builder builder(Type type) {
        return new Builder(type);
    }

    @Override
    public boolean matches(AutomationContext context) {
        if (playerPredicate.isEmpty() && entityPredicate.isEmpty() && attributePredicate.isEmpty() && subConditions.isEmpty()) {
            return false;
        }

        return type.matches(context, player -> {
            boolean playerResult = playerPredicate.map(pred
                    -> pred.matches(player, (ServerLevel) player.level(), player.getEyePosition())).orElse(true);

            boolean entityResult = entityPredicate.map(pred
                    -> pred.matches((ServerLevel) player.level(), player.position(), player)).orElse(true);

            boolean attributeResult = attributePredicate.map(attrPred
                    -> attrPred.matches(player, (ServerLevel) player.level(), null)).orElse(true);

            boolean subConditionsResult = subConditions.map(conditions
                    -> conditions.stream().allMatch(cond -> cond.matches(context))).orElse(true);

            return playerResult && entityResult && attributeResult && subConditionsResult;
        });
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }

    public enum Type implements StringRepresentable {
        SINGLE {
            @Override
            public boolean matches(AutomationContext context, Function<ServerPlayer,Boolean> function) {
                if (context.getPlayer().isEmpty() || !(context.getPlayer().get() instanceof ServerPlayer serverPlayer)){
                    return false;
                }
                return function.apply(serverPlayer);
            }
        },
        ALL {
            @Override
            public boolean matches(AutomationContext context, Function<ServerPlayer,Boolean> function) {
                if (context.getMomentInstance().isEmpty()) {
                    return false;
                }
                MomentInstance instance = context.getMomentInstance().get();

                return !instance.getLevel().isClientSide
                        && instance.getPlayers().stream()
                        .filter(player -> player instanceof ServerPlayer)
                        .map(player -> (ServerPlayer) player)
                        .allMatch(function::apply);
            }
        },
        ANY {
            @Override
            public boolean matches(AutomationContext context, Function<ServerPlayer,Boolean> function) {
                if (context.getMomentInstance().isEmpty()) {
                    return false;
                }
                MomentInstance momentInstance = context.getMomentInstance().get();
                return !momentInstance.getLevel().isClientSide
                        && momentInstance.getPlayers().stream()
                        .filter(player -> player instanceof ServerPlayer)
                        .map(player -> (ServerPlayer) player)
                        .anyMatch(function::apply);
            }
        };

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

        @Override
        @NotNull
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public abstract boolean matches(AutomationContext context,
                Function<ServerPlayer,Boolean> function);
    }

    public static class Builder implements IBuilderConverter<PlayerCondition> {

        private final Type type;
        private PlayerPredicate playerPredicate;
        private EntityPredicate entityPredicate;
        private AttributePredicate attributePredicate;
        private List<ICondition> subConditions;

        public Builder(Type type) {
            this.type = type;
        }

        public Builder playerPredicate(Function<PlayerPredicate.Builder, PlayerPredicate.Builder> playerPredicate) {
            PlayerPredicate.Builder builder = new PlayerPredicate.Builder();
            if (this.playerPredicate != null) {
                // 这里需要PlayerPredicate.Builder也实现IBuilderConverter接口
                // 但由于这是Minecraft原版类，我们无法修改，所以保持原样
            }
            this.playerPredicate = playerPredicate.apply(builder).build();
            return this;
        }

        public Builder entityPredicate(Function<EntityPredicate.Builder, EntityPredicate.Builder> entityPredicate) {
            EntityPredicate.Builder builder = new EntityPredicate.Builder();
            if (this.entityPredicate != null) {
                // 这里需要EntityPredicate.Builder也实现IBuilderConverter接口
                // 但由于这是Minecraft原版类，我们无法修改，所以保持原样
            }
            this.entityPredicate = entityPredicate.apply(builder).build();
            return this;
        }

        public Builder attributePredicate(Holder<Attribute> attribute, AttributePredicate.ValueType type, MinMaxBounds.Doubles value) {
            this.attributePredicate = new AttributePredicate(attribute, type, value);
            return this;
        }

        public Builder subConditions(ICondition... conditions) {
            if (this.subConditions == null) {
                this.subConditions = List.of(conditions);
            }else {
                this.subConditions = this.subConditions.stream().toList();
            }
            return this;
        }

        public PlayerCondition build() {
            return new PlayerCondition(type,
                    Optional.ofNullable(playerPredicate),
                    Optional.ofNullable(entityPredicate),
                    Optional.ofNullable(attributePredicate),
                    Optional.ofNullable(subConditions)
            );
        }

        @Override
        public Builder converter(PlayerCondition playerCondition) {
            Builder builder = new Builder(playerCondition.type());
            playerCondition.playerPredicate().ifPresent(predicate -> builder.playerPredicate = predicate);
            playerCondition.entityPredicate().ifPresent(predicate -> builder.entityPredicate = predicate);
            playerCondition.attributePredicate().ifPresent(predicate -> builder.attributePredicate = predicate);
            playerCondition.subConditions().ifPresent(conditions -> builder.subConditions = conditions);
            return builder;
        }
    }
}
