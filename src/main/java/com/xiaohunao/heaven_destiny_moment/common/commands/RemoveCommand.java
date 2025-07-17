package com.xiaohunao.heaven_destiny_moment.common.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstanceManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;


public class RemoveCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("remove")
                .requires(ctx -> ctx.hasPermission(4))
                .then(Commands.argument("uuid", StringArgumentType.string())
                        .suggests(MomentCommand.SUGGEST_MOMENT_UUID)
                        .executes(RemoveCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String uuidStr = StringArgumentType.getString(ctx, "uuid");
        var source = ctx.getSource();
        var manager = MomentInstanceManager.of(source.getLevel());
        
        MomentInstance instance = MomentCommand.getMomentInstance(ctx, uuidStr);
        manager.removeMomentInstance(instance);
        
        // 创建一个带悬停提示的UUID文本组件，悬停时显示时刻类型
        String momentTypeName = instance.getMomentResource().toLanguageKey();
        Component uuidComponent = Component.literal(uuidStr)
                .setStyle(Style.EMPTY.withHoverEvent(
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                    Component.translatable(momentTypeName))
                ));
        
        source.sendSuccess(() -> Component.translatable("commands.moment.remove.success", 
                uuidComponent), true);
        
        return 1;
    }
} 