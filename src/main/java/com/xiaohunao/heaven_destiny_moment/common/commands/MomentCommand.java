package com.xiaohunao.heaven_destiny_moment.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class MomentCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("moment")
                .requires(ctx -> ctx.hasPermission(2))
                .then(ListCommand.register())
                .then(RemoveCommand.register())
                .then(QueryCommand.register());

        dispatcher.register(builder);
    }
}
