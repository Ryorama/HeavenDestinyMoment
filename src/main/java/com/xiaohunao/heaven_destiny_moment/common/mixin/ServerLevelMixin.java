package com.xiaohunao.heaven_destiny_moment.common.mixin;

import com.google.common.collect.Maps;
import com.xiaohunao.heaven_destiny_moment.common.context.SpawnCategoryMultiplierInstance;
import com.xiaohunao.heaven_destiny_moment.common.mixed.EntityManagerAccessor;
import com.xiaohunao.heaven_destiny_moment.common.mixed.SpawnCategoryMultiplierInstanceMixed;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentManagerSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ServerLevel.class)
public class ServerLevelMixin implements SpawnCategoryMultiplierInstanceMixed, EntityManagerAccessor {
    @Unique
    private final Map<MobCategory, SpawnCategoryMultiplierInstance> heaven_destiny_moment$mobCategoryMultiplierInstance = Maps.newHashMap();

    @Shadow
    @Final private PersistentEntitySectionManager<Entity> entityManager;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void clinit(CallbackInfo ci){
        for (MobCategory value : MobCategory.values()) {
            heaven_destiny_moment$mobCategoryMultiplierInstance.put(value,new SpawnCategoryMultiplierInstance(value));
        }

        MomentManagerSavedData.init((ServerLevel)(Object)this);
    }

    @Override
    public SpawnCategoryMultiplierInstance getMobCategoryMultiplierInstance(MobCategory mobCategory){
        return heaven_destiny_moment$mobCategoryMultiplierInstance.get(mobCategory);
    }

    @Override
    public PersistentEntitySectionManager<Entity> getEntityManager() {
        return entityManager;
    }
}
