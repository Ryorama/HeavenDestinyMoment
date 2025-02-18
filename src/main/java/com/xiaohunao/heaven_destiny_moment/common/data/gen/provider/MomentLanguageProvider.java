package com.xiaohunao.heaven_destiny_moment.common.data.gen.provider;

import com.google.gson.JsonObject;
import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import com.xiaohunao.heaven_destiny_moment.common.context.TipSettings;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentState;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.data.LanguageProvider;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public abstract class MomentLanguageProvider extends LanguageProvider {
    private final Map<String, String> translationData;
    private final PackOutput output;
    private final String locale;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;
    private final String modid;
    
    protected MomentLanguageProvider(PackOutput output,
                                     CompletableFuture<HolderLookup.Provider> lookupProvider,
                                     String modid,
                                     String locale) {
        super(output, modid, locale);
        this.output = output;
        this.locale = locale;
        this.lookupProvider = lookupProvider;
        this.modid = modid;
        this.translationData = new TreeMap<>();
    }

    protected HolderLookup.RegistryLookup<Moment<?>> getMomentRegistry() {
        try {
            return lookupProvider.get().lookupOrThrow(HDMRegistries.Keys.MOMENT);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to get moment registry", e);
        }
    }


    public void addMomentDefaultBarName(ResourceKey<Moment<?>> key, String en, String zh) {
        String translationKey = HeavenDestinyMoment.asDescriptionId("bar." + key.location().toLanguageKey());
        addTranslation(translationKey, en, zh);
    }


    public void addMomentTooltip(ResourceKey<Moment<?>> key, Map<MomentState, String> en, Map<MomentState, String> zh) {
        try {
            Moment<?> moment = getMomentRegistry()
                    .getOrThrow(key)
                    .value();

            moment.tipSettings()
                    .flatMap(TipSettings::texts)
                    .ifPresentOrElse(
                            texts -> processTooltipTexts(texts, en, zh),
                            () -> System.out.println("No tip settings found for moment: " + key.location())
                    );
        } catch (Exception e) {
            throw new RuntimeException("Failed to process tooltip for moment: " + key.location(), e);
        }
    }

    private void processTooltipTexts(Map<MomentState, ?> texts, Map<MomentState, String> en, Map<MomentState, String> zh) {
        texts.forEach((state, component) -> {
            if (component instanceof MutableComponent mutable && mutable.getContents() instanceof TranslatableContents translatable) {
                addTranslation(translatable.getKey(),
                        en.getOrDefault(state, null),
                        zh.getOrDefault(state, null));
            }
        });
    }

    protected void addTranslation(String key, String en, String zh) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Translation key cannot be null or empty");
        }

        String translation = locale.equals("en_us") ? en : zh;
        if (translation != null && !translationData.containsKey(key)) {
            translationData.put(key, translation);
        }
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        addTranslations();

        if (translationData.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        Path langPath = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
                .resolve(modid)
                .resolve("lang")
                .resolve(locale + ".json");

        return saveTranslations(cache, langPath);
    }

    private CompletableFuture<?> saveTranslations(CachedOutput cache, Path target) {
        JsonObject json = new JsonObject();
        translationData.forEach(json::addProperty);
        return DataProvider.saveStable(cache, json, target);
    }
}
