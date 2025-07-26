package com.xiaohunao.heaven_destiny_moment.common.context;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobCategory;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class SpawnCategoryMultiplierInstance {
    private final MobCategory category;

    private final Map<SpawnCategoryMultiplierModifier.Operation, Map<ResourceLocation, SpawnCategoryMultiplierModifier>> modifiersByOperation = Maps.newEnumMap(SpawnCategoryMultiplierModifier.Operation.class);
    private final Map<ResourceLocation, SpawnCategoryMultiplierModifier> modifierById = new Object2ObjectArrayMap<>();
    private double cachedValue;
    private boolean dirty = true;

    public SpawnCategoryMultiplierInstance(MobCategory category) {
        this.category = category;
    }

    private double calculateValue() {
        double baseValue = 1.0;

        for(SpawnCategoryMultiplierModifier modifier : this.getModifiersOrEmpty(SpawnCategoryMultiplierModifier.Operation.ADD_VALUE)) {
            baseValue += modifier.amount();
        }

        double newValue = baseValue;

        for(SpawnCategoryMultiplierModifier modifier : this.getModifiersOrEmpty(SpawnCategoryMultiplierModifier.Operation.ADD_MULTIPLIED_BASE)) {
            newValue += baseValue * modifier.amount();
        }

        for(SpawnCategoryMultiplierModifier modifier : this.getModifiersOrEmpty(SpawnCategoryMultiplierModifier.Operation.ADD_MULTIPLIED_TOTAL)) {
            newValue *= (double)1.0F + modifier.amount();
        }

        return newValue;
    }

    private Collection<SpawnCategoryMultiplierModifier> getModifiersOrEmpty(SpawnCategoryMultiplierModifier.Operation operation) {
        return this.modifiersByOperation.getOrDefault(operation, Map.of()).values();
    }

    public void addModifier(SpawnCategoryMultiplierModifier modifier) {
        SpawnCategoryMultiplierModifier multiplierModifier = this.modifierById.putIfAbsent(modifier.id(), modifier);
        if (multiplierModifier == null) {
            this.getModifiers(modifier.operation()).put(modifier.id(), modifier);
            this.setDirty();
        }
    }

    public void removeModifiers() {
        for(SpawnCategoryMultiplierModifier multiplierModifier : this.getModifiers()) {
            this.removeModifier(multiplierModifier);
            this.setDirty();
        }

    }

    public void removeModifier(SpawnCategoryMultiplierModifier modifier) {
        this.removeModifier(modifier.id());
    }

    public boolean removeModifier(ResourceLocation id) {
        SpawnCategoryMultiplierModifier multiplierModifier = this.modifierById.remove(id);
        if (multiplierModifier == null) {
            return false;
        } else {
            this.getModifiers(multiplierModifier.operation()).remove(id);
            this.modifierById.remove(id);
            this.setDirty();
            return true;
        }
    }

    public double getValue() {
        if (this.dirty) {
            this.cachedValue = this.calculateValue();
            this.dirty = false;
        }

        return this.cachedValue;
    }

    protected void setDirty() {
        this.dirty = true;
    }

    Map<ResourceLocation, SpawnCategoryMultiplierModifier> getModifiers(SpawnCategoryMultiplierModifier.Operation operation) {
        return this.modifiersByOperation.computeIfAbsent(operation, (p_332604_) -> new Object2ObjectOpenHashMap<>());
    }

    public Set<SpawnCategoryMultiplierModifier> getModifiers() {
        return ImmutableSet.copyOf(this.modifierById.values());
    }
}
