package com.xiaohunao.heaven_destiny_moment.common.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.function.FailableToDoubleFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public record AttributePredicate(Holder<Attribute> attribute, ValueType type, MinMaxBounds.Doubles value) implements EntitySubPredicate {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttributePredicate.class);
    public static MapCodec<AttributePredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Attribute.CODEC.fieldOf("attribute").forGetter(AttributePredicate::attribute),
            ValueType.CODEC.fieldOf("value_type").forGetter(AttributePredicate::type),
            MinMaxBounds.Doubles.CODEC.fieldOf("value").forGetter(AttributePredicate::value)
    ).apply(instance, AttributePredicate::new));

    @Override
    public MapCodec<AttributePredicate> codec() { return CODEC; }

    @Override
    public boolean matches(Entity entity, ServerLevel level, @Nullable Vec3 position) {
        if (entity instanceof LivingEntity living) {
            var instance = living.getAttribute(attribute);
            if (instance != null) {
                try {
                    double value = type.getValue(instance);
                    return this.value.matches(value);
                } catch (Exception e) {
                    LOGGER.warn("An error occurred while matching attribute '{}' of entity '{}', returning false",
                                attribute.getRegisteredName(), entity.getStringUUID(), e);
                    return false;
                }
            }
        }
        return false;
    }

    public enum ValueType implements StringRepresentable{

        DEFAULT("default", (instance) -> instance.getAttribute().value().getDefaultValue()),
        BASE("base", AttributeInstance::getBaseValue),
        CURRENT("current", AttributeInstance::getValue),
        MAX("max", (instance) -> castToRangedAttribute(instance).getMaxValue()),
        MIN("min", (instance) -> castToRangedAttribute(instance).getMinValue());

        private final String name;
        private final FailableToDoubleFunction<AttributeInstance, IllegalArgumentException> getter;

        ValueType(String name, FailableToDoubleFunction<AttributeInstance, IllegalArgumentException> getter) {
            this.name = name;
            this.getter = getter;
        }

        public double getValue(AttributeInstance instance) { return this.getter.applyAsDouble(instance); }

        private static RangedAttribute castToRangedAttribute(AttributeInstance instance) {
            var attr = instance.getAttribute();
            if (attr.value() instanceof RangedAttribute ranged) {
                return ranged;
            }
            throw new IllegalArgumentException("Attribute '" + attr.getRegisteredName() + "' is not a RangedAttribute");
        }

        public static final Codec<ValueType> CODEC = StringRepresentable.fromEnum(ValueType::values);

        @Override
        @NotNull
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

}
