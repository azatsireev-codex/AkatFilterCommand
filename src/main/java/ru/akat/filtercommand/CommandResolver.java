package ru.akat.filtercommand;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;

public final class CommandResolver {
    private final Plugin plugin;

    public CommandResolver(Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean canUse(Player player, String rootCommand) {
        return resolve(rootCommand)
                .map(command -> command.testPermissionSilent(player))
                .orElse(false);
    }

    private Optional<Command> resolve(String rawRootCommand) {
        String normalized = normalize(rawRootCommand);
        if (normalized.isEmpty()) {
            return Optional.empty();
        }

        try {
            CommandMap commandMap = getCommandMap(Bukkit.getServer());

            Command directMatch = commandMap.getCommand(normalized);
            if (directMatch != null) {
                return Optional.of(directMatch);
            }

            int namespaceSeparator = normalized.indexOf(':');
            if (namespaceSeparator > 0 && namespaceSeparator + 1 < normalized.length()) {
                Command namespacedFallback = commandMap.getCommand(normalized.substring(namespaceSeparator + 1));
                if (namespacedFallback != null) {
                    return Optional.of(namespacedFallback);
                }
            }
        } catch (ReflectiveOperationException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to access command map; tab filtering will hide unresolved commands.", exception);
        }

        return Optional.empty();
    }

    private String normalize(String rawRootCommand) {
        String trimmed = rawRootCommand == null ? "" : rawRootCommand.trim();
        if (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1);
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    private CommandMap getCommandMap(Server server) throws ReflectiveOperationException {
        Method getCommandMapMethod = server.getClass().getMethod("getCommandMap");
        return (CommandMap) getCommandMapMethod.invoke(server);
    }
}
