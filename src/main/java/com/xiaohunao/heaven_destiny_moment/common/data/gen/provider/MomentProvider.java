package com.xiaohunao.heaven_destiny_moment.common.data.gen.provider;

import com.google.common.collect.Maps;
import com.mojang.serialization.JsonOps;
import com.xiaohunao.heaven_destiny_moment.common.moment.IMoment;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.xhn_lib.api.register.holder.FlexibleHolder;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class MomentProvider implements DataProvider {
    private final Map<FlexibleHolder<IMoment,?>, IMoment> moments = Maps.newHashMap();
    private final PackOutput packOutput;
    private final String modId;
    
    protected MomentProvider(PackOutput packOutput, String modId) {
        this.packOutput = packOutput;
        this.modId = modId;
    }
    
    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cachedOutput) {
        addMoments();
        
        // Create a CompletableFuture list to track all save operations
        Map<ResourceLocation, CompletableFuture<?>> futures = Maps.newHashMap();
        
        // For each moment, encode it and save it to the appropriate path
        moments.forEach((holder, moment) -> {
            ResourceLocation id = holder.getKey().location();
            Path path = getPath(id);
            
            // Encode the moment to JSON using the IMoment.CODEC
            CompletableFuture<?> future = IMoment.CODEC.encodeStart(JsonOps.INSTANCE, moment)
                .resultOrPartial(error -> {
                    throw new RuntimeException("Failed to encode moment " + id + ": " + error);
                })
                .map(jsonElement -> DataProvider.saveStable(cachedOutput, jsonElement, path))
                .orElseThrow(() -> new RuntimeException("Failed to encode moment " + id));
            
            futures.put(id, future);
        });
        
        // Combine all futures into a single CompletableFuture that completes when all save operations are done
        return CompletableFuture.allOf(futures.values().toArray(new CompletableFuture[0]));
    }

    protected void addMoment(FlexibleHolder<IMoment,?> holder, IMoment moment) {
        if (moments.containsKey(holder)) {
            throw new IllegalStateException("Duplicate moment registration: " + holder.getKey().location());
        }
        moments.put(holder, moment);
    }
    
    /**
     * Gets the path where the moment JSON file should be saved.
     *
     * @param id The ResourceLocation of the moment
     * @return The Path where the moment should be saved
     */
    private Path getPath(ResourceLocation id) {
        return packOutput.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve(id.getNamespace())
                .resolve("heaven_destiny_moment/moment")
                .resolve(id.getPath() + ".json");
    }

    protected abstract void addMoments();

    @Override
    public @NotNull String getName() {
        return "Moment Provider";
    }
}
