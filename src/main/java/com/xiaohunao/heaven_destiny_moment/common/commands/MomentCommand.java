package com.xiaohunao.heaven_destiny_moment.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.util.UUID;

public class MomentCommand {
    // 命令权限等级
    static final int PERMISSION_LIST = 2;    // 查看列表的权限
    static final int PERMISSION_QUERY = 2;   // 查询的权限
    static final int PERMISSION_REMOVE = 4;  // 移除的权限

    // 错误类型
    static final SimpleCommandExceptionType ERROR_INVALID_UUID = new SimpleCommandExceptionType(
            Component.translatable("commands.moment.error.invalid_uuid"));
    static final SimpleCommandExceptionType ERROR_MOMENT_NOT_FOUND = new SimpleCommandExceptionType(
            Component.translatable("commands.moment.error.not_found"));

    // UUID建议提供者
    static final SuggestionProvider<CommandSourceStack> SUGGEST_MOMENT_UUID = (context, builder) -> {
        var source = context.getSource();
        var manager = MomentInstanceManager.of(source.getLevel());
        var registry = source.getLevel().registryAccess().registryOrThrow(HDMRegistries.Keys.MOMENT);
        
        manager.getMomentInstances().forEach(momentInstance -> {
            String uuid = momentInstance.getID().toString();
            String name = momentInstance.getMomentResource().toLanguageKey();
            builder.suggest(uuid, Component.translatable(name));
        });
        
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("moment")
                .then(ListCommand.register()
                        .requires(ctx -> ctx.hasPermission(PERMISSION_LIST)))
                .then(QueryCommand.register()
                        .requires(ctx -> ctx.hasPermission(PERMISSION_QUERY)))
                .then(RemoveCommand.register()
                        .requires(ctx -> ctx.hasPermission(PERMISSION_REMOVE)))
                .then(ClearClientCommand.register());

        dispatcher.register(builder);
    }

    static MomentInstance getMomentInstance(CommandContext<CommandSourceStack> ctx, String uuidStr) throws CommandSyntaxException {
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            throw ERROR_INVALID_UUID.create();
        }

        var source = ctx.getSource();
        var manager = MomentInstanceManager.of(source.getLevel());
        
        MomentInstance instance = manager.getMomentInstance(uuid);
        if (instance == null) {
            throw ERROR_MOMENT_NOT_FOUND.create();
        }
        return instance;
    }

    static Component formatMomentInfo(MomentInstance instance, ServerLevel level) {
        String momentName = instance.getMomentResource().toLanguageKey();
        
        return Component.literal("- ")
                .append(Component.translatable(momentName))
                .append(" (")
                .append(Component.literal(instance.getID().toString()).withStyle(ChatFormatting.GRAY))
                .append(")");
    }
}
