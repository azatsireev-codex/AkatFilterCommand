package ru.akat.filtercommand;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
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
                .orElse(true);
    }

    private Optional<Command> resolve(String rawRootCommand) {
        String normalized = rawRootCommand.toLowerCase(Locale.ROOT);
        Map<String, Command> knownCommands = knownCommands();

        Command directMatch = knownCommands.get(normalized);
        if (directMatch != null) {
            return Optional.of(directMatch);
        }

        int namespaceSeparator = normalized.indexOf(':');
        if (namespaceSeparator > 0 && namespaceSeparator + 1 < normalized.length()) {
            Command namespacedFallback = knownCommands.get(normalized.substring(namespaceSeparator + 1));
            if (namespacedFallback != null) {
                return Optional.of(namespacedFallback);
            }
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Command> knownCommands() {
        try {
            CommandMap commandMap = getCommandMap(Bukkit.getServer());
            if (!(commandMap instanceof SimpleCommandMap simpleCommandMap)) {
                throw new IllegalStateException("Unsupported CommandMap implementation: " + commandMap.getClass().getName());
            }

            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            return (Map<String, Command>) knownCommandsField.get(simpleCommandMap);
        } catch (ReflectiveOperationException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to access command registry; tab filtering will be permissive.", exception);
            return Map.of();
        }
    }

    private CommandMap getCommandMap(Server server) throws ReflectiveOperationException {
        Method getCommandMapMethod = server.getClass().getMethod("getCommandMap");
        return (CommandMap) getCommandMapMethod.invoke(server);
    }
}
