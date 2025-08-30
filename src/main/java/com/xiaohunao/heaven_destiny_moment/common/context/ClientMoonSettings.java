package com.xiaohunao.heaven_destiny_moment.common.context;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record ClientMoonSettings(Optional<Integer> moonColor, Optional<Float> moonSize, Optional<ResourceLocation> moonTexture) {
    public static final ClientMoonSettings EMPTY = new ClientMoonSettings(Optional.empty(),Optional.empty(),Optional.empty());
    public static final Codec<ClientMoonSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("moon_color").forGetter(ClientMoonSettings::moonColor),
            Codec.FLOAT.optionalFieldOf("moon_size").forGetter(ClientMoonSettings::moonSize),
            ResourceLocation.CODEC.optionalFieldOf("moon_texture").forGetter(ClientMoonSettings::moonTexture)
    ).apply(instance, ClientMoonSettings::new));

    public boolean isEmpty() {
        return moonColor.isPresent() || moonSize.isPresent() || moonTexture.isPresent();
    }


    public static class Builder implements IBuilderConverter<ClientMoonSettings> {
        private Integer moonColor;
        private Float moonSize;
        private ResourceLocation moonTexture;

        public ClientMoonSettings build() {
            return new ClientMoonSettings(Optional.ofNullable(moonColor), Optional.ofNullable(moonSize), Optional.ofNullable(moonTexture));
        }

        public Builder moonColor(int moonColor) {
            this.moonColor = moonColor;
            return this;
        }
        public Builder moonSize(float moonSize) {
            this.moonSize = moonSize;
            return this;
        }
        public Builder moonTexture(ResourceLocation moonTexture) {
            this.moonTexture = moonTexture;
            return this;
        }

        @Override
        public Builder converter(ClientMoonSettings clientMoonSettings) {
            Builder builder = new Builder();
            clientMoonSettings.moonColor.ifPresent(color -> builder.moonColor = color);
            clientMoonSettings.moonSize.ifPresent(size -> builder.moonSize = size);
            clientMoonSettings.moonTexture.ifPresent(texture -> builder.moonTexture = texture);
            return builder;
        }
    }
}
