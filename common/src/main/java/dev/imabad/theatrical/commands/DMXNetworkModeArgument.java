package dev.imabad.theatrical.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.imabad.theatrical.dmx.DMXNetworkMode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class DMXNetworkModeArgument implements ArgumentType<DMXNetworkMode> {
    private static final DynamicCommandExceptionType ERROR_INVALID = new DynamicCommandExceptionType(
            object -> Component.translatable("commands.network.invalid", object)
    );
    private static final DMXNetworkMode[] VALUES = DMXNetworkMode.values();

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
        return commandContext.getSource() instanceof SharedSuggestionProvider
                ? SharedSuggestionProvider.suggest(Arrays.stream(VALUES).map(DMXNetworkMode::getName), suggestionsBuilder)
                : Suggestions.empty();
    }

    @Override
    public DMXNetworkMode parse(StringReader reader) throws CommandSyntaxException {
        String string = reader.readUnquotedString();
        DMXNetworkMode networkMode = DMXNetworkMode.byName(string);
        if (networkMode == null) {
            throw ERROR_INVALID.createWithContext(reader, string);
        } else {
            return networkMode;
        }
    }

    public static DMXNetworkModeArgument networkMode() {
        return new DMXNetworkModeArgument();
    }
    public static DMXNetworkMode getMode(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, DMXNetworkMode.class);
    }
}
