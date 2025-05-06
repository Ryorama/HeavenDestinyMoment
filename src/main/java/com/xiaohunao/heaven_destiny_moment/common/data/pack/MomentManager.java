package com.xiaohunao.heaven_destiny_moment.common.data.pack;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class MomentManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();

    private static BiMap<ResourceLocation, Moment> allMoments = HashBiMap.create();


    public MomentManager() {
        super(GSON, "moment");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> data, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {
        data.forEach((resourceLocation, jsonElement) -> {

        });
    }
}
