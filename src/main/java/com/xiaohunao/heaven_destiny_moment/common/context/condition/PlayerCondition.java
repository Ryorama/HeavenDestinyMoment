package com.xiaohunao.heaven_destiny_moment.common.context.condition;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMContextRegister;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.PlayerPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.GameType;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public record PlayerCondition(Type type, Optional<MinMaxBounds.Ints> level, Optional<List<GameType>> gameType, Optional<List<PlayerPredicate.StatMatcher<?>>> statMatchers,
                              Optional<Object2BooleanMap<ResourceLocation>> recipes, Optional<Map<ResourceLocation, PlayerPredicate.AdvancementPredicate>> advancements
) implements ICondition {
    public static final MapCodec<PlayerCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Type.CODEC.fieldOf("player_type").forGetter(PlayerCondition::type),
            MinMaxBounds.Ints.CODEC.optionalFieldOf("level").forGetter(PlayerCondition::level),
            GameType.CODEC.listOf().optionalFieldOf("gameType").forGetter(PlayerCondition::gameType),
            PlayerPredicate.StatMatcher.CODEC.listOf().optionalFieldOf("stats").forGetter(PlayerCondition::statMatchers),
            ExtraCodecs.object2BooleanMap(ResourceLocation.CODEC).optionalFieldOf("recipes").forGetter(PlayerCondition::recipes),
            Codec.unboundedMap(ResourceLocation.CODEC, PlayerPredicate.AdvancementPredicate.CODEC).optionalFieldOf("advancements").forGetter(PlayerCondition::advancements)
    ).apply(instance, PlayerCondition::new));

    @Override
    public boolean matches(MomentInstance<?> instance, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer) {
        return type.matches(instance, pos, serverPlayer, (momentInstance, pos1, serverPlayer1) -> checkPlayer(serverPlayer1));
    }

    private boolean checkPlayer(ServerPlayer serverPlayer) {
        return serverPlayer != null &&
                matchesLevel(serverPlayer) &&
                matchesGameType(serverPlayer) &&
                matchesStats(serverPlayer) &&
                matchesRecipe(serverPlayer) &&
                matchesAdvancements(serverPlayer);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return HDMContextRegister.PLAYER.get();
    }

    private boolean matchesLevel(ServerPlayer serverplayer) {
        return this.level.map(level -> {
            if (level().isEmpty() || level().get().isAny()) {
                return true;
            }
            return level.matches(serverplayer.experienceLevel);
        }).orElse(true);
    }

    private boolean matchesGameType(ServerPlayer serverplayer) {
        return this.gameType.map(gameMode -> {
            if (gameType().isEmpty() || gameType().get().isEmpty()) {
                return true;
            }
            return gameMode.contains(serverplayer.gameMode.getGameModeForPlayer());
        }).orElse(true);
    }

    private boolean matchesStats(ServerPlayer serverplayer) {
        StatsCounter statscounter = serverplayer.getStats();
        return this.statMatchers.map(statMatchers -> {
            for (PlayerPredicate.StatMatcher<?> statmatcher : statMatchers) {
                if (!statmatcher.matches(statscounter)) {
                    return false;
                }
            }
            return true;
        }).orElse(true);
    }

    private boolean matchesRecipe(ServerPlayer serverPlayer) {
        RecipeBook recipebook = serverPlayer.getRecipeBook();

        return this.recipes.map(recipe -> {
            for (Object2BooleanMap.Entry<ResourceLocation> entry : recipe.object2BooleanEntrySet()) {
                if (recipebook.contains(entry.getKey()) != entry.getBooleanValue()) {
                    return false;
                }
            }
            return true;
        }).orElse(true);
    }

    private boolean matchesAdvancements(ServerPlayer serverPlayer) {
        return this.advancements.map(advancements -> {
            if (!advancements.isEmpty()) {
                PlayerAdvancements playeradvancements = serverPlayer.getAdvancements();
                ServerAdvancementManager serveradvancementmanager = serverPlayer.getServer().getAdvancements();

                for (java.util.Map.Entry<ResourceLocation, PlayerPredicate.AdvancementPredicate> entry1 : advancements.entrySet()) {
                    AdvancementHolder advancementholder = serveradvancementmanager.get(entry1.getKey());
                    if (advancementholder == null || !entry1.getValue().test(playeradvancements.getOrStartProgress(advancementholder))) {
                        return false;
                    }
                }
            }
            return true;
        }).orElse(true);
    }

//    private boolean matchesLookingAt(ServerPlayer serverPlayer){
//        return this.lookingAt.map(lookingAt -> {
//            Vec3 vec3 = serverPlayer.getEyePosition();
//            Vec3 vec31 = serverPlayer.getViewVector(1.0F);
//            Vec3 vec32 = vec3.add(vec31.x * 100.0, vec31.y * 100.0, vec31.z * 100.0);
//            EntityHitResult entityhitresult = ProjectileUtil.getEntityHitResult(
//                    serverPlayer.level(), serverPlayer, vec3, vec32, new AABB(vec3, vec32).inflate(1.0), p_156765_ -> !p_156765_.isSpectator(), 0.0F
//            );
//
//            if (entityhitresult == null || entityhitresult.getType() != HitResult.Type.ENTITY) {
//                return false;
//            }
//
//            Entity entity = entityhitresult.getEntity();
//            return this.lookingAt.get().matches(serverPlayer, entity) && serverPlayer.hasLineOfSight(entity);
//        }).orElse(true);
//    }

    public static class Builder {
        private final Type type;
        private MinMaxBounds.Ints level = MinMaxBounds.Ints.ANY;
        private List<GameType> gameType = Lists.newArrayList();
        private ImmutableList.Builder<PlayerPredicate.StatMatcher<?>> stats;
        private Object2BooleanMap<ResourceLocation> recipes;
        private Map<ResourceLocation, PlayerPredicate.AdvancementPredicate> advancements = Maps.newHashMap();

        public Builder(Type type) {
            this.type = type;
        }

        public Builder level(MinMaxBounds.Ints level) {
            this.level = level;
            return this;
        }

        public Builder gameType(GameType... gameType) {
            this.gameType = List.of(gameType);
            return this;
        }


        public <T> Builder stat(StatType<T> type, Holder.Reference<T> value, MinMaxBounds.Ints range) {
            if (this.stats == null) this.stats = ImmutableList.builder();

            this.stats.add(new PlayerPredicate.StatMatcher<>(type, value, range));
            return this;
        }

        public Builder recipe(ResourceLocation recipe, boolean unlocked) {
            if (this.recipes == null) this.recipes = new Object2BooleanOpenHashMap<>();

            this.recipes.put(recipe, unlocked);
            return this;
        }

        public Builder checkAdvancementDone(ResourceLocation advancement, boolean done) {
            if (this.advancements == null) this.advancements = Maps.newHashMap();

            this.advancements.put(advancement, new PlayerPredicate.AdvancementDonePredicate(done));
            return this;
        }

        public Builder checkAdvancementCriterions(ResourceLocation advancement, Map<String, Boolean> criterions) {
            if (this.advancements == null) this.advancements = Maps.newHashMap();

            this.advancements.put(advancement, new PlayerPredicate.AdvancementCriterionsPredicate(new Object2BooleanOpenHashMap<>(criterions)));
            return this;
        }

        public PlayerCondition build() {
            return new PlayerCondition(this.type, Optional.ofNullable(this.level), Optional.ofNullable(this.gameType),
                    Optional.ofNullable(this.stats == null ? null : this.stats.build()), Optional.ofNullable(this.recipes),
                    Optional.ofNullable(this.advancements)
            );
        }
    }


    public enum Type implements StringRepresentable {
        SINGLE {
            @Override
            public boolean matches(MomentInstance<?> instance, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer, TriFunction<MomentInstance<?>, @Nullable BlockPos, @Nullable ServerPlayer, Boolean> function) {
                return function.apply(instance, pos, serverPlayer);
            }
        },
        GLOBAL {
            @Override
            public boolean matches(MomentInstance<?> instance, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer, TriFunction<MomentInstance<?>, @Nullable BlockPos, @Nullable ServerPlayer, Boolean> function) {
                return !instance.getLevel().isClientSide && instance.getPlayers().stream()
                        .allMatch(player -> function.apply(instance, pos, (ServerPlayer) player));
            }
        },
        ANY {
            @Override
            public boolean matches(MomentInstance<?> instance, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer, TriFunction<MomentInstance<?>, @Nullable BlockPos, @Nullable ServerPlayer, Boolean> function) {
                return !instance.getLevel().isClientSide && instance.getPlayers().stream()
                        .anyMatch(player -> function.apply(instance, pos, (ServerPlayer) player));
            }
        };

        public static final Codec<Type> CODEC = Codec.STRING.xmap(type -> valueOf(type.toUpperCase(Locale.ROOT)), type -> type.name().toLowerCase(Locale.ROOT));

        @Override
        @NotNull
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public abstract boolean matches(MomentInstance<?> instance, @Nullable BlockPos pos, @Nullable ServerPlayer serverPlayer, TriFunction<MomentInstance<?>, @Nullable BlockPos, @Nullable ServerPlayer, Boolean> function);
    }
}
