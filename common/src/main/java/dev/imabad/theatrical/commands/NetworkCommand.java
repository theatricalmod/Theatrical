package dev.imabad.theatrical.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.imabad.theatrical.dmx.*;
import dev.imabad.theatrical.networks.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
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
                                                            Set<AVNetworkMember> members =
                                                                    getDMXNetwork(commandContext).members();
                                                            GameProfileCache profileCache = commandContext.getSource()
                                                                    .getServer().getProfileCache();
                                                            return SharedSuggestionProvider.suggest(
                                                                    members.stream()
                                                                            .map(serverPlayer -> profileCache
                                                                                    .get(serverPlayer.playerId())
                                                                                    .orElse(new GameProfile(
                                                                                            serverPlayer.playerId(),
                                                                                            serverPlayer.playerId()
                                                                                                    .toString()
                                                                                    )).getName()),
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
                                                                            .map(serverPlayer -> serverPlayer.getGameProfile().getName()),
                                                                    suggestionsBuilder
                                                            );
                                                        }
                                                )
                                                .executes(NetworkCommand::addPlayer)))
                                .then(Commands.literal("remove")
                                        .then(Commands.argument("targets", GameProfileArgument.gameProfile())
                                                .suggests(
                                                        (commandContext, suggestionsBuilder) -> {
                                                            Set<AVNetworkMember> members =
                                                                    getDMXNetwork(commandContext).members();
                                                            GameProfileCache profileCache = commandContext.getSource()
                                                                    .getServer().getProfileCache();
                                                            return SharedSuggestionProvider.suggest(
                                                                    members.stream()
                                                                            .map(serverPlayer -> profileCache
                                                                                    .get(serverPlayer.playerId())
                                                                                    .orElse(new GameProfile(
                                                                                            serverPlayer.playerId(),
                                                                                            serverPlayer.playerId()
                                                                                                    .toString()
                                                                                    )).getName()),
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
        AVNetwork AVNetwork = getDMXNetwork(context);
        if (!isSourceOperator(context)) {
            if (context.getSource().isPlayer() && !AVNetwork.isAdmin(context.getSource().getPlayer().getUUID())) {
                throw ERROR_NETWORK_DOES_NOT_EXIST.create();
            }
        }
        Collection<GameProfile> players = GameProfileArgument.getGameProfiles(context, "target");
        AVNetworkMemberRole role = MemberRoleArgument.getMode(context, "role");
        for (GameProfile player : players) {
            AVNetwork.setMemberRole(player.getId(), role);
        }
        context.getSource().sendSuccess(() -> Component.translatable("commands.network.updated"), false);
        return 0;
    }

    private static int changeNetworkMode(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        AVNetwork AVNetwork = getDMXNetwork(context);
        if (!isSourceOperator(context)) {
            if (context.getSource().isPlayer() && !AVNetwork.isAdmin(context.getSource().getPlayer().getUUID())) {
                throw ERROR_NETWORK_DOES_NOT_EXIST.create();
            }
        }
        AVNetworkMode mode = DMXNetworkModeArgument.getMode(context, "mode");
        AVNetworkMode oldMode = AVNetwork.mode();
        AVNetwork.setMode(mode);
        List<UUID> members = new ArrayList<>();
        for (AVNetworkMember member : AVNetwork.members()) {
            ServerPlayer player = context.getSource().getServer().getPlayerList().getPlayer(member.playerId());
            if (player != null) {
                AVNetworkData.getInstance(context.getSource().getLevel()).notifyNetworks(player);
                members.add(player.getUUID());
            }
        }
        if (AVNetwork.mode() == AVNetworkMode.PUBLIC || oldMode == AVNetworkMode.PUBLIC) {
            for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
                if (!members.contains(player.getUUID())) {
                    AVNetworkData.getInstance(context.getSource().getServer().overworld()).notifyNetworks(player);
                }
            }
        }
        context.getSource().sendSuccess(() -> Component.translatable("commands.network.updated"), false);
        return 1;
    }

    private static int createNetwork(CommandContext<CommandSourceStack> context) {
        String newName = StringArgumentType.getString(context, "name");
        AVNetworkMode mode = DMXNetworkModeArgument.getMode(context, "mode");
        AVNetwork network = AVNetworkData.getInstance(context.getSource().getServer().overworld()).createNetwork(newName, mode);
        if (context.getSource().isPlayer()) {
            network.addMember(context.getSource().getPlayer().getUUID(), AVNetworkMemberRole.ADMIN);
        }
        if (network.mode() == AVNetworkMode.PUBLIC) {
            for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
                AVNetworkData.getInstance(context.getSource().getServer().overworld()).notifyNetworks(player);
            }
        }
        context.getSource().sendSuccess(() -> Component.translatable("commands.network.created"), false);

        return 1;
    }

    private static int listNetworkMembers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        AVNetwork AVNetwork = getDMXNetwork(context);
        GameProfileCache profileCache = context.getSource().getServer().getProfileCache();
        List<String> list = AVNetwork.members()
                .stream().map(AVNetworkMember::playerId)
                .map((playerUUID) -> new Tuple<>(playerUUID, profileCache.get(playerUUID)))
                .map(uuidOptionalTuple -> uuidOptionalTuple.getB().isPresent() ? uuidOptionalTuple.getB().get().getName() : uuidOptionalTuple.getA().toString())
                .toList();
        context.getSource().sendSuccess(() -> Component.translatable("commands.network.members", list.size(), String.join(", ", list)), false);
        return 1;
    }

    private static int getNetwork(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        AVNetwork AVNetwork = getDMXNetwork(context);
        if (!isSourceOperator(context)) {
            if (context.getSource().isPlayer() && !AVNetwork.isAdmin(context.getSource().getPlayer().getUUID())) {
                throw ERROR_NETWORK_DOES_NOT_EXIST.create();
            }
        }
        int i = 0;
        context.getSource().sendSuccess(() -> Component.translatable("commands.network", Component.literal(AVNetwork.name()), AVNetwork.id().toString(), AVNetwork.members().size()), false);
        return i;
    }

    private static int addPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        AVNetwork AVNetwork = getDMXNetwork(context);
        if (!isSourceOperator(context)) {
            if (context.getSource().isPlayer() && !AVNetwork.isAdmin(context.getSource().getPlayer().getUUID())) {
                throw ERROR_NETWORK_DOES_NOT_EXIST.create();
            }
        }
        int i = 0;
        Collection<GameProfile> players = GameProfileArgument.getGameProfiles(context, "targets");
        for (GameProfile player : players) {
            if (!AVNetwork.isMember(player.getId())) {
                AVNetwork.addMember(player.getId(), AVNetworkMemberRole.NONE);
                context.getSource().sendSuccess(() -> Component.translatable("commands.network.members.add.success", Component.literal(player.getName())), false);
                ServerPlayer serverPlayer = context.getSource().getServer().getPlayerList().getPlayer(player.getId());
                if (serverPlayer != null) {
                    AVNetworkData.getInstance(context.getSource().getLevel()).notifyNetworks(serverPlayer);
                }
                i++;
            }
        }
        return i;
    }

    private static int renameNetwork(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        AVNetwork AVNetwork = getDMXNetwork(context);
        if (!isSourceOperator(context)) {
            if (context.getSource().isPlayer() && !AVNetwork.isAdmin(context.getSource().getPlayer().getUUID())) {
                throw ERROR_NETWORK_DOES_NOT_EXIST.create();
            }
        }
        String newName = StringArgumentType.getString(context, "name");
        AVNetwork.setName(newName);
        List<UUID> members = new ArrayList<>();
        for (AVNetworkMember member : AVNetwork.members()) {
            ServerPlayer player = context.getSource().getServer().getPlayerList().getPlayer(member.playerId());
            if (player != null) {
                AVNetworkData.getInstance(context.getSource().getLevel()).notifyNetworks(player);
                members.add(player.getUUID());
            }
        }
        if (AVNetwork.mode() == AVNetworkMode.PUBLIC) {
            for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
                if (!members.contains(player.getUUID())) {
                    AVNetworkData.getInstance(context.getSource().getServer().overworld()).notifyNetworks(player);
                }
            }
        }
        return 1;
    }

    private static int deleteNetwork(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        AVNetwork AVNetwork = getDMXNetwork(context);
        if (!isSourceOperator(context)) {
            if (context.getSource().isPlayer() && !AVNetwork.isAdmin(context.getSource().getPlayer().getUUID())) {
                throw ERROR_NETWORK_DOES_NOT_EXIST.create();
            }
        }

        AVNetworkData.getInstance(context.getSource().getLevel()).deleteNetwork(AVNetwork);
        List<UUID> members = new ArrayList<>();
        for (AVNetworkMember member : AVNetwork.members()) {
            ServerPlayer player = context.getSource().getServer().getPlayerList().getPlayer(member.playerId());
            if (player != null) {
                AVNetworkData.getInstance(context.getSource().getLevel()).notifyNetworks(player);
                members.add(player.getUUID());
            }
        }
        if (AVNetwork.mode() == AVNetworkMode.PUBLIC) {
            for (ServerPlayer player : context.getSource().getServer().getPlayerList().getPlayers()) {
                if (!members.contains(player.getUUID())) {
                    AVNetworkData.getInstance(context.getSource().getServer().overworld()).notifyNetworks(player);
                }
            }
        }
        context.getSource().sendSuccess(() -> Component.translatable("commands.network.deleted"), false);
        return 1;
    }

    private static int removePlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        AVNetwork AVNetwork = getDMXNetwork(context);
        if (!isSourceOperator(context)) {
            if (context.getSource().isPlayer() && !AVNetwork.isAdmin(context.getSource().getPlayer().getUUID())) {
                throw ERROR_NETWORK_DOES_NOT_EXIST.create();
            }
        }
        Collection<GameProfile> players = GameProfileArgument.getGameProfiles(context, "targets");
        for (GameProfile player : players) {
            if (AVNetwork.isMember(player.getId())) {
                AVNetwork.removeMember(player.getId());
                ServerPlayer serverPlayer = context.getSource().getServer().getPlayerList().getPlayer(player.getId());
                if (serverPlayer != null) {
                    AVNetworkData.getInstance(context.getSource().getLevel()).notifyNetworks(serverPlayer);
                }
                context.getSource().sendSuccess(() -> Component.translatable("commands.network.members.remove.success", Component.literal(player.getName())), false);
            }
        }
        return 0;
    }

    private static Collection<AVNetwork> getNetworksForPlayer(CommandContext<CommandSourceStack> context) {
        AVNetworkData instance = AVNetworkData.
                getInstance(context.getSource().getServer().overworld());
        if (isSourceOperator(context)) {
            return instance.getAllNetworks();
        } else {
            return instance.getNetworksForPlayer(context.getSource().getPlayer().getUUID());
        }
    }

    private static int listNetworks(CommandContext<CommandSourceStack> context) {
        AVNetworkData instance = AVNetworkData.
                getInstance(context.getSource().getServer().overworld());
        List<String> networks;
        if (isSourceOperator(context)) {
            networks = instance.getAllNetworks().stream()
                    .map(AVNetwork::name).collect(Collectors.toList());
        } else {
            networks = instance.getNetworksForPlayer(context.getSource().getPlayer().getUUID()).stream()
                    .map(AVNetwork::name).collect(Collectors.toList());
        }
        context.getSource().sendSuccess(() -> Component.translatable("commands.networks",
                networks.size(), String.join(", ", networks)), false);
        return 1;
    }

    private static boolean isSourceOperator(CommandContext<CommandSourceStack> context) {
        return context.getSource().hasPermission(context.getSource().getServer().getOperatorUserPermissionLevel());
    }

    private static AVNetwork getDMXNetwork(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        AVNetworkData instance = AVNetworkData.
                getInstance(context.getSource().getServer().overworld());
        try {
            String id = context.getArgument("id", String.class);
            UUID uuid = UUID.fromString(id);
            AVNetwork network = instance.getNetwork(uuid);
            if (network != null) {
                return network;
            }
        } catch (Exception ignored) {
        }
        throw ERROR_NETWORK_DOES_NOT_EXIST.create();
    }
}
