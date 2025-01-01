package dev.imabad.theatrical.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.imabad.theatrical.networks.AVNetworkMemberRole;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class MemberRoleArgument implements ArgumentType<AVNetworkMemberRole> {
    private static final DynamicCommandExceptionType ERROR_INVALID = new DynamicCommandExceptionType(
            object -> Component.translatable("commands.network.role.invalid", object)
    );
    private static final AVNetworkMemberRole[] VALUES = AVNetworkMemberRole.values();

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
        return commandContext.getSource() instanceof SharedSuggestionProvider
                ? SharedSuggestionProvider.suggest(Arrays.stream(VALUES).map(AVNetworkMemberRole::getName), suggestionsBuilder)
                : Suggestions.empty();
    }

    @Override
    public AVNetworkMemberRole parse(StringReader reader) throws CommandSyntaxException {
        String string = reader.readUnquotedString();
        AVNetworkMemberRole networkMode = AVNetworkMemberRole.byName(string);
        if (networkMode == null) {
            throw ERROR_INVALID.createWithContext(reader, string);
        } else {
            return networkMode;
        }
    }

    public static MemberRoleArgument memberRole() {
        return new MemberRoleArgument();
    }
    public static AVNetworkMemberRole getMode(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, AVNetworkMemberRole.class);
    }
}
