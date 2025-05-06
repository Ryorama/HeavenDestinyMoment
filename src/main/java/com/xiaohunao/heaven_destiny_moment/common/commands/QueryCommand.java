package com.xiaohunao.heaven_destiny_moment.common.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.xiaohunao.heaven_destiny_moment.common.init.HDMRegistries;
import com.xiaohunao.heaven_destiny_moment.common.moment.MomentInstance;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;

import java.util.UUID;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Collection;

public class QueryCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("query")
                .requires(ctx -> ctx.hasPermission(2))
                .then(Commands.argument("uuid", StringArgumentType.string())
                        .suggests(MomentCommand.SUGGEST_MOMENT_UUID)
                        .executes(QueryCommand::execute)
                        .then(Commands.literal("field")
                                .then(Commands.argument("fieldName", StringArgumentType.word())
                                        .suggests(SUGGEST_FIELDS)
                                        .executes(QueryCommand::queryField)))
                        .then(Commands.literal("enemiesManager")
                                .then(Commands.literal("list")
                                        .executes(QueryCommand::listEnemies))
                                .then(Commands.literal("kill")
                                        .then(Commands.literal("all")
                                                .executes(QueryCommand::killAllEnemies))
                                        .then(Commands.argument("enemyUUID", StringArgumentType.string())
                                                .suggests(SUGGEST_ENEMIES)
                                                .executes(QueryCommand::killEnemy)))));
    }

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_FIELDS = (context, builder) -> {
        String[] suggestions = {
                "persistentData",
                "players",
                "state",
                "uuid",
                "tick",
                "bar"
        };
        for (String suggestion : suggestions) {
            builder.suggest(suggestion);
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ENEMIES = (context, builder) -> {
        try {
            String uuidStr = StringArgumentType.getString(context, "uuid");
            MomentInstance instance = MomentCommand.getMomentInstance(context, uuidStr);
            ServerLevel level = context.getSource().getLevel();
            
            instance.getEnemies().forEach(uuid -> {
                Entity entity = level.getEntity(uuid);
                if (entity != null) {
                    builder.suggest(uuid.toString(), 
                        Component.literal(entity.getName().getString())
                            .withStyle(ChatFormatting.GRAY));
                }
            });
        } catch (CommandSyntaxException e) {
            // 忽略异常
        }
        return builder.buildFuture();
    };

    private static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var source = ctx.getSource();
        String uuidStr = StringArgumentType.getString(ctx, "uuid");
        MomentInstance instance = MomentCommand.getMomentInstance(ctx, uuidStr);
        String momentName = instance.getMomentResource().toLanguageKey();
        source.sendSuccess(() -> Component.translatable("commands.moment.query.info", 
                Component.translatable(momentName),
                Component.literal(instance.getID().toString()).withStyle(ChatFormatting.GRAY)), 
                false);
        
        return 1;
    }

    private static int queryField(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var source = ctx.getSource();
        String uuidStr = StringArgumentType.getString(ctx, "uuid");
        MomentInstance instance = MomentCommand.getMomentInstance(ctx, uuidStr);
        String fieldName = StringArgumentType.getString(ctx, "fieldName");
        
        try {
            Field field = MomentInstance.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(instance);
            
            Component displayValue;
            if (value == null) {
                displayValue = Component.literal("null").withStyle(ChatFormatting.RED);
            } else if (value instanceof Optional<?> optional) {
                displayValue = optional.map(obj -> formatValue(obj))
                        .orElse(Component.literal("empty").withStyle(ChatFormatting.RED));
            } else if (value instanceof CompoundTag tag) {
                if (tag.isEmpty()) {
                    source.sendSuccess(() -> Component.translatable("commands.moment.query.persistent_data.empty"), false);
                    return 0;
                }
                source.sendSuccess(() -> Component.translatable("commands.moment.query.persistent_data.header"), false);
                source.sendSuccess(() -> NbtUtils.toPrettyComponent(tag).copy()
                        .withStyle(ChatFormatting.GRAY), false);
                return 1;
            } else if (value instanceof Collection<?> collection) {
                if (collection.isEmpty()) {
                    displayValue = Component.literal("[]").withStyle(ChatFormatting.GRAY);
                } else {
                    source.sendSuccess(() -> Component.translatable("commands.moment.query.collection.header", 
                            Component.literal(fieldName).withStyle(ChatFormatting.AQUA), 
                            collection.size()), false);
                    collection.forEach(item -> {
                        source.sendSuccess(() -> Component.literal("- ")
                                .append(formatCollectionItem(item)), false);
                    });
                    return collection.size();
                }
            } else {
                displayValue = formatValue(value);
            }
            
            source.sendSuccess(() -> Component.translatable("commands.moment.query.field", 
                    Component.literal(fieldName).withStyle(ChatFormatting.AQUA),
                    displayValue), false);
            
            return 1;
        } catch (NoSuchFieldException e) {
            throw new SimpleCommandExceptionType(
                    Component.translatable("commands.moment.query.field.not_found", fieldName))
                    .create();
        } catch (IllegalAccessException e) {
            throw new SimpleCommandExceptionType(
                    Component.translatable("commands.moment.query.field.access_denied", fieldName))
                    .create();
        }
    }

    private static Component formatValue(Object value) {
        String valueStr = value.toString();
        return Component.literal(valueStr)
                .withStyle(style -> style
                        .withColor(ChatFormatting.GREEN)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, valueStr))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
                                Component.translatable("chat.copy.click"))));
    }

    private static Component formatCollectionItem(Object item) {
        if (item instanceof Player player) {
            return player.getDisplayName();
        } else {
            return formatValue(item);
        }
    }

    private static int listEnemies(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var source = ctx.getSource();
        String uuidStr = StringArgumentType.getString(ctx, "uuid");
        MomentInstance instance = MomentCommand.getMomentInstance(ctx, uuidStr);
        ServerLevel level = source.getLevel();

        if (instance.getEnemies().isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.moment.query.enemies.empty")
                    .withStyle(ChatFormatting.YELLOW), false);
            return 0;
        }

        source.sendSuccess(() -> Component.translatable("commands.moment.query.enemies.header", 
                instance.getEnemyCount())
                .withStyle(ChatFormatting.GREEN), false);

        instance.getEnemies().forEach(uuid -> {
            Entity entity = level.getEntity(uuid);
            Component message;
            if (entity != null) {
                message = Component.literal("- ")
                        .append(entity.getName())
                        .append(" (")
                        .append(Component.literal(uuid.toString()).withStyle(ChatFormatting.GRAY))
                        .append(")");
            } else {
                message = Component.literal("- ")
                        .append(Component.literal("Unknown Entity").withStyle(ChatFormatting.RED))
                        .append(" (")
                        .append(Component.literal(uuid.toString()).withStyle(ChatFormatting.GRAY))
                        .append(")");
            }
            source.sendSuccess(() -> message, false);
        });

        return instance.getEnemyCount();
    }

    private static int killAllEnemies(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var source = ctx.getSource();
        String uuidStr = StringArgumentType.getString(ctx, "uuid");
        MomentInstance instance = MomentCommand.getMomentInstance(ctx, uuidStr);
        ServerLevel level = source.getLevel();

        if (instance.getEnemies().isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.moment.query.enemies.empty")
                    .withStyle(ChatFormatting.YELLOW), false);
            return 0;
        }

        int count = instance.getEnemyCount();
        instance.killAllEnemies(level);

        source.sendSuccess(() -> Component.translatable("commands.moment.query.enemies.kill.all", count)
                .withStyle(ChatFormatting.GREEN), true);
        return count;
    }

    private static int killEnemy(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var source = ctx.getSource();
        String uuidStr = StringArgumentType.getString(ctx, "uuid");
        MomentInstance instance = MomentCommand.getMomentInstance(ctx, uuidStr);
        ServerLevel level = source.getLevel();

        String enemyUuidStr = StringArgumentType.getString(ctx, "enemyUUID");
        UUID uuid;
        try {
            uuid = UUID.fromString(enemyUuidStr);
        } catch (IllegalArgumentException e) {
            throw MomentCommand.ERROR_INVALID_UUID.create();
        }

        if (!instance.hasEnemy(uuid)) {
            throw new SimpleCommandExceptionType(
                    Component.translatable("commands.moment.query.enemies.not_found", enemyUuidStr))
                    .create();
        }

        Entity entity = level.getEntity(uuid);
        if (entity != null) {
            entity.kill();
            instance.removeEnemy(uuid);
            source.sendSuccess(() -> Component.translatable("commands.moment.query.enemies.kill.single", 
                    entity.getName()), true);
            return 1;
        } else {
            throw new SimpleCommandExceptionType(
                    Component.translatable("commands.moment.query.enemies.entity_not_found", enemyUuidStr))
                    .create();
        }
    }
} 