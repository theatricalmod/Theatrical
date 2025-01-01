package dev.imabad.theatrical.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.imabad.theatrical.networks.AVNetworkMode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class DMXNetworkModeArgument implements ArgumentType<AVNetworkMode> {
    private static final DynamicCommandExceptionType ERROR_INVALID = new DynamicCommandExceptionType(
            object -> Component.translatable("commands.network.invalid", object)
    );
    private static final AVNetworkMode[] VALUES = AVNetworkMode.values();

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
        return commandContext.getSource() instanceof SharedSuggestionProvider
                ? SharedSuggestionProvider.suggest(Arrays.stream(VALUES).map(AVNetworkMode::getName), suggestionsBuilder)
                : Suggestions.empty();
    }

    @Override
    public AVNetworkMode parse(StringReader reader) throws CommandSyntaxException {
        String string = reader.readUnquotedString();
        AVNetworkMode networkMode = AVNetworkMode.byName(string);
        if (networkMode == null) {
            throw ERROR_INVALID.createWithContext(reader, string);
        } else {
            return networkMode;
        }
    }

    public static DMXNetworkModeArgument networkMode() {
        return new DMXNetworkModeArgument();
    }
    public static AVNetworkMode getMode(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, AVNetworkMode.class);
    }
}
