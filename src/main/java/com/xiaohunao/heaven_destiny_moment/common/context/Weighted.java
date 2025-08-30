package com.xiaohunao.heaven_destiny_moment.common.context;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.random.WeightedEntry;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public record Weighted<T>(RandomType type,int totalWeight, List<WeightedEntry.Wrapper<T>> list) {
    private static final Random random = new Random();

    public enum RandomType implements StringRepresentable{
        SINGLE,
        SUBSET,
        ALL;
        public static final Codec<RandomType> CODEC = StringRepresentable.fromEnum(RandomType::values);

        @Override @NotNull
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public List<T> getRandomWeighted() {
        if (this.totalWeight == 0) {
            return Lists.newArrayList();
        }

        return switch (type) {
            case SINGLE -> getRandomValue().map(Lists::newArrayList).orElse(Lists.newArrayList());
            case SUBSET -> getRandomSubset();
            case ALL -> getAll();
        };
    }

    private List<T> getAll() {
        return this.list.stream()
                .map(WeightedEntry.Wrapper::data)
                .collect(Collectors.toList());
    }

    private List<T> getRandomSubset() {
        ArrayList<T> subSet = Lists.newArrayList();
        if (this.totalWeight == 0) {
            return subSet;
        } else {
            for (WeightedEntry.Wrapper<T> entry : this.list) {
                if (random.nextInt(this.totalWeight) < entry.getWeight().asInt()) {
                    subSet.add(entry.data());
                }
            }
        }
        return subSet;
    }

    private static int calculateTotalWeight(List<? extends WeightedEntry> entries) {
        long total = 0;
        for (WeightedEntry entry : entries) {
            total += entry.getWeight().asInt();
        }
        if (total > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Sum of weights must be <= 2147483647");
        }
        return (int) total;
    }

    private Optional<WeightedEntry.Wrapper<T>> getRandom() {
        if (this.totalWeight == 0) {
            return Optional.empty();
        } else {
            int index = random.nextInt(this.totalWeight);
            return getWeightedItem(this.list, index);
        }
    }

    private Optional<T> getRandomValue() {
        return getRandom().map(WeightedEntry.Wrapper::data);
    }

    private static <T extends WeightedEntry> Optional<T> getWeightedItem(List<T> entries, int weight) {
        for (T entry : entries) {
            weight -= entry.getWeight().asInt();
            if (weight < 0) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    public static <T> Codec<Weighted<T>> codec(Codec<T> codec) {
        return Codec.pair(
                RandomType.CODEC.fieldOf("type").codec(),
                WeightedEntry.Wrapper.codec(codec).listOf().fieldOf("entries").codec()
        ).xmap(
                pair -> new Weighted<>(
                        pair.getFirst(),
                        calculateTotalWeight(pair.getSecond()),
                        pair.getSecond()
                ),
                weighted -> Pair.of(weighted.type(), weighted.list())
        );
    }

    public static class Builder<T> implements IBuilderConverter<Weighted<T>> {
        private final List<WeightedEntry.Wrapper<T>> list = Lists.newArrayList();
        private RandomType type = RandomType.ALL;

        public Builder<T> randomType(RandomType type) {
            this.type = type;
            return this;
        }

        public Builder<T> add(T t, int weight) {
            list.add(WeightedEntry.wrap(t, weight));
            return this;
        }

        public Weighted<T> build(){
            return new Weighted<>(type,calculateTotalWeight(list), list);
        }

        @Override
        public Builder<T> converter(Weighted<T> weighted) {
            Builder<T> builder = new Builder<>();
            builder.type = weighted.type();
            builder.list.addAll(weighted.list());
            return builder;
        }
    }
}