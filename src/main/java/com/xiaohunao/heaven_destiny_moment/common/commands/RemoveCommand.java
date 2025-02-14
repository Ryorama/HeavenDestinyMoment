package com.xiaohunao.heaven_destiny_moment.common.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Registry;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.moment.Moment;

import java.util.UUID;

public class RemoveCommand {
    private static final SimpleCommandExceptionType ERROR_INVALID_UUID = new SimpleCommandExceptionType(
            Component.translatable("commands.moment.remove.invalid_uuid"));
    private static final SimpleCommandExceptionType ERROR_MOMENT_NOT_FOUND = new SimpleCommandExceptionType(
            Component.translatable("commands.moment.remove.not_found"));

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_UUID = (context, builder) -> {
        var source = context.getSource();
        var manager = MomentManager.of(source.getLevel());
        var registry = source.getLevel().registryAccess().registryOrThrow(HDMRegistries.Keys.MOMENT);
        
        manager.getMomentInstances().forEach(moment -> {
            String uuid = moment.getID().toString();
            String name = registry.getKey(moment.moment().get()).toLanguageKey();
            builder.suggest(uuid, Component.translatable(name));
        });
        
        return builder.buildFuture();
    };

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("remove")
                .requires(ctx -> ctx.hasPermission(4))
                .then(Commands.argument("uuid", StringArgumentType.string())
                        .suggests(SUGGEST_UUID)
                        .executes(RemoveCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String uuidStr = StringArgumentType.getString(ctx, "uuid");
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            throw ERROR_INVALID_UUID.create();
        }

        var source = ctx.getSource();
        var manager = MomentManager.of(source.getLevel());
        
        MomentInstance<?> instance = manager.getMomentInstance(uuid);
        if (instance == null) {
            throw ERROR_MOMENT_NOT_FOUND.create();
        }

        manager.removeMomentInstance(instance, true);
        source.sendSuccess(() -> Component.translatable("commands.moment.remove.success", 
                Component.literal(uuid.toString())), true);
        
        return 1;
    }
} 