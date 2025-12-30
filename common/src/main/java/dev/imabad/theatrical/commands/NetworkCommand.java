package dev.imabad.theatrical.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.imabad.theatrical.networks.*;
import dev.imabad.theatrical.networks.members.TheatricalNetworkMember;
import dev.imabad.theatrical.networks.members.TheatricalNetworkMemberRole;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.Tuple;

import java.util.*;
import java.util.stream.Collectors;

public class NetworkCommand {

    private static final SimpleCommandExceptionType ERROR_NETWORK_DOES_NOT_EXIST = new SimpleCommandExceptionType(
            Component.translatable("commands.network.notfound")
    );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("theatrical")
                .then(Commands.literal("networks")
                        .executes(NetworkCommand::listNetworks)
                )
                .then(Commands.literal("network")
                        .then(Commands.literal("new")
                                .then(Commands.argument("mode", DMXNetworkModeArgument.networkMode())
                                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                                .executes(NetworkCommand::createNetwork))))
                        .then(Commands.argument("id", StringArgumentType.string())
                                .suggests((commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(
                                        getNetworksForPlayer(commandContext)
                                                .stream()
                                                .map(serverPlayer -> serverPlayer.id().toString()),
                                        suggestionsBuilder
                                ))
                                .executes(NetworkCommand::getNetwork)
                                .then(Commands.literal("members")
                                        .executes(NetworkCommand::listNetworkMembers)
                                ).then(Commands.literal("rename")
                                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                                .executes(NetworkCommand::renameNetwork)
                                        )
                                ).then(Commands.literal("delete")
                                        .executes(NetworkCommand::deleteNetwork)
                                ).then(Commands.literal("mode")
                                        .then(Commands.argument("mode", DMXNetworkModeArgument.networkMode())
                                                .executes(NetworkCommand::changeNetworkMode))
                                ).then(Commands.literal("setrole")
                                        .then(Commands.argument("target", GameProfileArgument.gameProfile())
                                                .suggests(
                                                        (commandContext, suggestionsBuilder) -> {
                                                            Set<TheatricalNetworkMember> members =
                                                                    getDMXNetwork(commandContext).members().members();
                                                            PlayerList playerList = commandContext.getSource()
                                                                    .getServer().getPlayerList();
                                                            //TODO: Clean this up.
                                                            return SharedSuggestionProvider.suggest(
                                                                    members.stream()
                                                                            .map(serverPlayer -> playerList
                                                                                    .getPlayer(serverPlayer.playerId()).getPlainTextName()),
                                                                    suggestionsBuilder
                                                            );
                                                        }
                                                ).then(Commands.argument("role", MemberRoleArgument.memberRole())
                                                        .executes(NetworkCommand::changeMemberRole)))
                                )
                                .then(Commands.literal("add")
                                        .then(Commands.argument("targets", GameProfileArgument.gameProfile())
                                                .suggests(
                                                        (commandContext, suggestionsBuilder) -> {
                                                            PlayerList playerList = commandContext.getSource().getServer().getPlayerList();
                                                            return SharedSuggestionProvider.suggest(
                                                                    playerList.getPlayers()
                                                                            .stream()
                                                                            .map(serverPlayer -> serverPlayer.getGameProfile().name()),
                                                                    suggestionsBuilder
                                                            );
                                                        }
                                                )
                                                .executes(NetworkCommand::addPlayer)))
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("targets", GameProfileArgument.gameProfile())
                                                .suggests(
                                                        (commandContext, suggestionsBuilder) -> {
                                                            Set<TheatricalNetworkMember> members =
                                                                    getDMXNetwork(commandContext).members().members();
                                                            PlayerList playerList = commandContext.getSource()
                                                                    .getServer().getPlayerList();
                                                            //TODO: Clean this up.
                                                            return SharedSuggestionProvider.suggest(
                                                                    members.stream()
                                                                            .map(serverPlayer -> playerList
                                                                                    .getPlayer(serverPlayer.playerId()).getPlainTextName()),
                                                                    suggestionsBuilder
                                                            );
                                                        }
                                                )
                                                .executes(NetworkCommand::removePlayer)))
                        )
                )
        );
    }

    private static int changeMemberRole(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        TheatricalNetwork theatricalNetwork = getDMXNetwork(context);
        if (!isSourceOperator(context)) {
            if (context.getSource().isPlayer() && !theatricalNetwork.members().isAdmin(context.getSource().getPlayer().getUUID())) {
                throw ERROR_NETWORK_DOES_NOT_EXIST.create();
            }
        }
        Collection<NameAndId> players = GameProfileArgument.getGameProfiles(context, "target");
        TheatricalNetworkMemberRole role = MemberRoleArgument.getMode(context, "role");
        for (NameAndId player : players) {
            theatricalNetwork.members().setMemberRole(player.id(), role);
        }
        context.getSource().sendSuccess(() -> Component.translatable("commands.network.updated"), false);
        return 0;
    }

    private static int changeNetworkMode(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        TheatricalNetwork theatricalNetwork = getDMXNetwork(context);
        if (!isSourceOperator(context)) {
            if (context.getSource().isPlayer() && !theatricalNetwork.members().isAdmin(context.getSource().getPlayer().getUUID())) {
                throw ERROR_NETWORK_DOES_NOT_EXIST.create();
            }
        }
        TheatricalNetworkMode mode = DMXNetworkModeArgument.getMode(context, "mode");
        TheatricalNetworkMode oldMode = theatricalNetwork.mode();
        theatricalNetwork.setMode(mode);
        List<UUID> members = new ArrayList<>();
        for (TheatricalNetworkMember member : theatricalNetwork.members().members()) {
            ServerPlayer player = context.getSource().getServer().getPlayerList().getPlayer(member.playerId());
            if (player != null) {
                TheatricalNetworkData.getInstance(context.getSource().getLevel()).notifyNetworks(player);
                members.add(player.getUUID());
            }
        }
        if (theatricalNetwork.mode() == TheatricalNetworkMode.PUBLIC || oldMode == TheatricalNetworkMode.PUBLIC) {
            for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
                if (!members.contains(player.getUUID())) {
                    TheatricalNetworkData.getInstance(context.getSource().getServer().overworld()).notifyNetworks(player);
                }
            }
        }
        context.getSource().sendSuccess(() -> Component.translatable("commands.network.updated"), false);
        return 1;
    }

    private static int createNetwork(CommandContext<CommandSourceStack> context) {
        String newName = StringArgumentType.getString(context, "name");
        TheatricalNetworkMode mode = DMXNetworkModeArgument.getMode(context, "mode");
        TheatricalNetwork network = TheatricalNetworkData.getInstance(context.getSource().getServer().overworld()).createNetwork(newName, mode);
        if (context.getSource().isPlayer()) {
            network.members().addMember(context.getSource().getPlayer().getUUID(), TheatricalNetworkMemberRole.ADMIN);
        }
        if (network.mode() == TheatricalNetworkMode.PUBLIC) {
            for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
                TheatricalNetworkData.getInstance(context.getSource().getServer().overworld()).notifyNetworks(player);
            }
        }
        context.getSource().sendSuccess(() -> Component.translatable("commands.network.created"), false);

        return 1;
    }

    private static int listNetworkMembers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        TheatricalNetwork theatricalNetwork = getDMXNetwork(context);
        PlayerList playerList = context.getSource().getServer().getPlayerList();
        List<String> list = theatricalNetwork.members().members()
                .stream().map(TheatricalNetworkMember::playerId)
                .map((playerUUID) -> new Tuple<>(playerUUID, Optional.ofNullable(playerList.getPlayer(playerUUID))))
                .map(uuidOptionalTuple -> uuidOptionalTuple.getB().isPresent() ? uuidOptionalTuple.getB().get().getGameProfile().name() : uuidOptionalTuple.getA().toString())
                .toList();
        context.getSource().sendSuccess(() -> Component.translatable("commands.network.members", list.size(), String.join(", ", list)), false);
        return 1;
    }

    private static int getNetwork(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        TheatricalNetwork theatricalNetwork = getDMXNetwork(context);
        if (!isSourceOperator(context)) {
            if (context.getSource().isPlayer() && !theatricalNetwork.members().isAdmin(context.getSource().getPlayer().getUUID())) {
                throw ERROR_NETWORK_DOES_NOT_EXIST.create();
            }
        }
        int i = 0;
        context.getSource().sendSuccess(() -> Component.translatable("commands.network", Component.literal(theatricalNetwork.name()), theatricalNetwork.id().toString(), theatricalNetwork.members().members().size()), false);
        return i;
    }

    private static int addPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        TheatricalNetwork theatricalNetwork = getDMXNetwork(context);
        if (!isSourceOperator(context)) {
            if (context.getSource().isPlayer() && !theatricalNetwork.members().isAdmin(context.getSource().getPlayer().getUUID())) {
                throw ERROR_NETWORK_DOES_NOT_EXIST.create();
            }
        }
        int i = 0;
        Collection<NameAndId> players = GameProfileArgument.getGameProfiles(context, "targets");
        for (NameAndId player : players) {
            if (!theatricalNetwork.members().isMember(player.id())) {
                theatricalNetwork.members().addMember(player.id(), TheatricalNetworkMemberRole.NONE);
                context.getSource().sendSuccess(() -> Component.translatable("commands.network.members.add.success", Component.literal(player.name())), false);
                ServerPlayer serverPlayer = context.getSource().getServer().getPlayerList().getPlayer(player.id());
                if (serverPlayer != null) {
                    TheatricalNetworkData.getInstance(context.getSource().getLevel()).notifyNetworks(serverPlayer);
                }
                i++;
            }
        }
        return i;
    }

    private static int renameNetwork(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        TheatricalNetwork theatricalNetwork = getDMXNetwork(context);
        if (!isSourceOperator(context)) {
            if (context.getSource().isPlayer() && !theatricalNetwork.members().isAdmin(context.getSource().getPlayer().getUUID())) {
                throw ERROR_NETWORK_DOES_NOT_EXIST.create();
            }
        }
        String newName = StringArgumentType.getString(context, "name");
        theatricalNetwork.setName(newName);
        List<UUID> members = new ArrayList<>();
        for (TheatricalNetworkMember member : theatricalNetwork.members().members()) {
            ServerPlayer player = context.getSource().getServer().getPlayerList().getPlayer(member.playerId());
            if (player != null) {
                TheatricalNetworkData.getInstance(context.getSource().getLevel()).notifyNetworks(player);
                members.add(player.getUUID());
            }
        }
        if (theatricalNetwork.mode() == TheatricalNetworkMode.PUBLIC) {
            for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
                if (!members.contains(player.getUUID())) {
                    TheatricalNetworkData.getInstance(context.getSource().getServer().overworld()).notifyNetworks(player);
                }
            }
        }
        return 1;
    }

    private static int deleteNetwork(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        TheatricalNetwork theatricalNetwork = getDMXNetwork(context);
        if (!isSourceOperator(context)) {
            if (context.getSource().isPlayer() && !theatricalNetwork.members().isAdmin(context.getSource().getPlayer().getUUID())) {
                throw ERROR_NETWORK_DOES_NOT_EXIST.create();
            }
        }

        TheatricalNetworkData.getInstance(context.getSource().getLevel()).deleteNetwork(theatricalNetwork);
        List<UUID> members = new ArrayList<>();
        for (TheatricalNetworkMember member : theatricalNetwork.members().members()) {
            ServerPlayer player = context.getSource().getServer().getPlayerList().getPlayer(member.playerId());
            if (player != null) {
                TheatricalNetworkData.getInstance(context.getSource().getLevel()).notifyNetworks(player);
                members.add(player.getUUID());
            }
        }
        if (theatricalNetwork.mode() == TheatricalNetworkMode.PUBLIC) {
            for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
                if (!members.contains(player.getUUID())) {
                    TheatricalNetworkData.getInstance(context.getSource().getServer().overworld()).notifyNetworks(player);
                }
            }
        }
        context.getSource().sendSuccess(() -> Component.translatable("commands.network.deleted"), false);
        return 1;
    }

    private static int removePlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        TheatricalNetwork theatricalNetwork = getDMXNetwork(context);
        if (!isSourceOperator(context)) {
            if (context.getSource().isPlayer() && !theatricalNetwork.members().isAdmin(context.getSource().getPlayer().getUUID())) {
                throw ERROR_NETWORK_DOES_NOT_EXIST.create();
            }
        }
        Collection<NameAndId> players = GameProfileArgument.getGameProfiles(context, "targets");
        for (NameAndId player : players) {
            if (theatricalNetwork.members().isMember(player.id())) {
                theatricalNetwork.members().removeMember(player.id());
                ServerPlayer serverPlayer = context.getSource().getServer().getPlayerList().getPlayer(player.id());
                if (serverPlayer != null) {
                    TheatricalNetworkData.getInstance(context.getSource().getLevel()).notifyNetworks(serverPlayer);
                }
                context.getSource().sendSuccess(() -> Component.translatable("commands.network.members.remove.success", Component.literal(player.name())), false);
            }
        }
        return 0;
    }

    private static Collection<TheatricalNetwork> getNetworksForPlayer(CommandContext<CommandSourceStack> context) {
        TheatricalNetworkData instance = TheatricalNetworkData.
                getInstance(context.getSource().getServer().overworld());
        if (isSourceOperator(context)) {
            return instance.getAllNetworks();
        } else {
            return instance.getNetworksForPlayer(context.getSource().getPlayer().getUUID());
        }
    }

    private static int listNetworks(CommandContext<CommandSourceStack> context) {
        TheatricalNetworkData instance = TheatricalNetworkData.
                getInstance(context.getSource().getServer().overworld());
        List<String> networks;
        if (isSourceOperator(context)) {
            networks = instance.getAllNetworks().stream()
                    .map(TheatricalNetwork::name).collect(Collectors.toList());
        } else {
            networks = instance.getNetworksForPlayer(context.getSource().getPlayer().getUUID()).stream()
                    .map(TheatricalNetwork::name).collect(Collectors.toList());
        }
        context.getSource().sendSuccess(() -> Component.translatable("commands.networks",
                networks.size(), String.join(", ", networks)), false);
        return 1;
    }

    private static boolean isSourceOperator(CommandContext<CommandSourceStack> context) {
        return Commands.hasPermission(Commands.LEVEL_ADMINS).test(context.getSource());
    }

    private static TheatricalNetwork getDMXNetwork(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        TheatricalNetworkData instance = TheatricalNetworkData.
                getInstance(context.getSource().getServer().overworld());
        try {
            String id = context.getArgument("id", String.class);
            UUID uuid = UUID.fromString(id);
            TheatricalNetwork network = instance.getNetwork(uuid);
            if (network != null) {
                return network;
            }
        } catch (Exception ignored) {
        }
        throw ERROR_NETWORK_DOES_NOT_EXIST.create();
    }
}
