package com.xiaohunao.heaven_destiny_moment.common.data.gen.provider;

import com.xiaohunao.heaven_destiny_moment.HeavenDestinyMoment;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import java.util.concurrent.CompletableFuture;

public class HDMLanguageProvider extends MomentLanguageProvider{
    public HDMLanguageProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String locale) {
        super(output, lookupProvider, HeavenDestinyMoment.MODID, locale);
    }

    @Override
    protected void addTranslations() {
        addTranslation("moment.unknown",
                "Unknown Moment",
                "未知时刻"
        );

        addTranslation("commands.moment.query.enemiesManager.enemy_count",
                "The number of enemies: %d",
                "敌人数量: %d"
        );

        addTranslation("commands.moment.list.empty",
                "There are no running moments currently",
                "当前没有运行中的时刻"
        );
        addTranslation("commands.moment.list.header",
                "There are %d moments running:",
                "共有 %d 个运行中的时刻："
        );
        addTranslation("commands.moment.list.player_header",
                "%s is currently participating in %d running moments:",
                "%s 正在参加的 %d 个运行中的时刻："
        );

        addTranslation("commands.moment.error.invalid_uuid",
                "Invalid UUID format",
                "无效的UUID格式"
        );
        addTranslation("commands.moment.error.not_found",
                "No moment found with the specified UUID",
                "找不到指定UUID的时刻"
        );

        addTranslation("commands.moment.remove.success",
                "Successfully removed moment with UUID %s",
                "成功删除UUID为 %s 的时刻"
        );

        addTranslation("commands.moment.query.info",
                "Moment information: %s (UUID: %s)",
                "时刻信息：%s (UUID: %s)"
        );

        addTranslation("commands.moment.query.field",
                "The value of field %s: %s",
                "字段 %s 的值：%s"
        );
        addTranslation("commands.moment.query.field.not_found",
                "Field not found: %s",
                "找不到字段：%s"
        );
        addTranslation("commands.moment.query.field.access_denied",
                "Cannot access field: %s",
                "无法访问字段：%s"
        );
        addTranslation("commands.moment.query.field.tick",
                "Runtime: %d ticks",
                "已运行时间：%d tick"
        );
        addTranslation("commands.moment.query.field.bar",
                "Progress bar: %s",
                "进度条：%s"
        );
        addTranslation("commands.moment.query.field.uuid",
                "UUID: %s",
                "UUID：%s"
        );
        addTranslation("commands.moment.query.field.state",
                "State: %s",
                "状态：%s"
        );

        addTranslation("commands.moment.query.enemies.empty",
                "This moment has no hostile entities",
                "该时刻没有敌对实体"
        );
        addTranslation("commands.moment.query.enemies.header",
                "Hostile entities list (total: %d):",
                "敌对实体列表（共 %d 个）："
        );
        addTranslation("commands.moment.query.enemies.not_found",
                "Hostile entity with specified UUID not found: %s",
                "找不到指定UUID的敌对实体：%s"
        );
        addTranslation("commands.moment.query.enemies.entity_not_found",
                "Hostile entity not found (may have been removed): %s",
                "找不到该敌对实体（可能已被移除）：%s"
        );
        addTranslation("commands.moment.query.enemies.kill.all",
                "Successfully killed all hostile entities (total: %d)",
                "成功击杀所有敌对实体（共 %d 个）"
        );
        addTranslation("commands.moment.query.enemies.kill.single",
                "Successfully killed hostile entity: %s",
                "成功击杀敌对实体：%s"
        );

        addTranslation("commands.moment.query.players.empty",
                "No players are currently participating in this moment",
                "当前没有玩家参与此时刻"
        );
        addTranslation("commands.moment.query.players.header",
                "Total %d players participating in this moment:",
                "共有 %d 个玩家参与此时刻："
        );

        addTranslation("commands.moment.query.persistent_data",
                "Persistent data: %s",
                "持久化数据：%s"
        );
        addTranslation("commands.moment.query.persistent_data.empty",
                "This moment has no persistent data",
                "该时刻没有持久化数据"
        );
        addTranslation("commands.moment.query.persistent_data.header",
                "Persistent data:",
                "持久化数据："
        );

        addTranslation("commands.moment.query.state",
                "Current state: %s",
                "当前状态：%s"
        );

        addTranslation("commands.moment.query.collection.header",
                "%s list (total: %d):",
                "%s 列表（共 %d 个）："
        );

        addTranslation("commands.moment.clear_only_client.success",
                "Successfully cleared the only client moment instance",
                "已成功清除唯一客户端时刻实例"
        );
        addTranslation("commands.moment.clear_only_client.sent_to_all",
                "Sent clear only client moment request to all players",
                "已向所有玩家发送清除唯一客户端时刻的请求"
        );
    }
}
