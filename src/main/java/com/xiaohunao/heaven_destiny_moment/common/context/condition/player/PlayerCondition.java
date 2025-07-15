package com.xiaohunao.heaven_destiny_moment.common.context.condition.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.context.condition.ICondition;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMConditions;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.PlayerPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;


public record PlayerCondition(Type type, Optional<PlayerPredicate> playerPredicate, Optional<EntityPredicate> entityPredicate) implements ICondition {
    public static final MapCodec<PlayerCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Type.CODEC.fieldOf("player_type").forGetter(PlayerCondition::type),
            PlayerPredicate.CODEC.codec().optionalFieldOf("player").forGetter(PlayerCondition::playerPredicate),
            EntityPredicate.CODEC.optionalFieldOf("entity").forGetter(PlayerCondition::entityPredicate)
    ).apply(instance, PlayerCondition::new));



    public static Builder builder(Type type) {
        return new Builder(type);
    }


    @Override
    public boolean matches(MomentInstance instance, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer) {
        if (playerPredicate.isEmpty() && entityPredicate.isEmpty()) {
            return false;
        }
        
        return type.matches(instance, pos, serverPlayer, (inst, p, player) -> {
            ServerLevel level = (ServerLevel) inst.getLevel();
            boolean playerResult = playerPredicate.map(pred -> 
                    pred.matches(player, level, player.getEyePosition())).orElse(true);
            
            boolean entityResult = entityPredicate.map(pred -> 
                    pred.matches(level, player.position(), player)).orElse(true);
                    
            return playerResult && entityResult;
        });
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return HDMConditions.PLAYER_CONDITION.get();
    }



    public enum Type implements StringRepresentable {
        SINGLE {
            @Override
            public boolean matches(MomentInstance instance, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer,
                                  TriFunction<MomentInstance, BlockPos, ServerPlayer, Boolean> function) {
                return function.apply(instance, pos, serverPlayer);
            }
        },

        GLOBAL {
            @Override
            public boolean matches(MomentInstance instance, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer,
                                  TriFunction<MomentInstance, BlockPos, ServerPlayer, Boolean> function) {
                return !instance.getLevel().isClientSide && 
                        instance.getPlayers().stream().allMatch(player -> 
                                function.apply(instance, pos, (ServerPlayer) player));
            }
        },

        ANY {
            @Override
            public boolean matches(MomentInstance instance, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer,
                                  TriFunction<MomentInstance, BlockPos, ServerPlayer, Boolean> function) {
                return !instance.getLevel().isClientSide && 
                        instance.getPlayers().stream().anyMatch(player -> 
                                function.apply(instance, pos, (ServerPlayer) player));
            }
        };


        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

        @Override
        @NotNull
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public abstract boolean matches(MomentInstance instance, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer,
                                  TriFunction<MomentInstance, BlockPos, ServerPlayer, Boolean> function);
    }


    public static class Builder {
        private final Type type;
        private PlayerPredicate playerPredicate = null;
        private EntityPredicate entityPredicate = null;

        public Builder(Type type) {
            this.type = type;
        }


        public Builder playerPredicate(Function<PlayerPredicate.Builder,PlayerPredicate.Builder> playerPredicate) {
            this.playerPredicate = playerPredicate.apply(new PlayerPredicate.Builder()).build();
            return this;
        }

        public Builder entityPredicate(Function<EntityPredicate.Builder,EntityPredicate.Builder> entityPredicate) {
            this.entityPredicate = entityPredicate.apply(new EntityPredicate.Builder()).build();
            return this;
        }

        public PlayerCondition build() {
            return new PlayerCondition(type, Optional.ofNullable(playerPredicate), Optional.ofNullable(entityPredicate));
        }
    }
}
