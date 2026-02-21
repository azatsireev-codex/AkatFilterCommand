package ru.akat.filtercommand;

import com.mojang.brigadier.tree.RootCommandNode;
import io.papermc.paper.event.brigadier.AsyncPlayerSendCommandsEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;

import java.util.ArrayList;
import java.util.HashSet;

public final class CommandFilterListener implements Listener {
    private final CommandResolver commandResolver;

    public CommandFilterListener(CommandResolver commandResolver) {
        this.commandResolver = commandResolver;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommandSend(PlayerCommandSendEvent event) {
        Player player = event.getPlayer();

        for (String root : new HashSet<>(event.getCommands())) {
            if (!commandResolver.canUse(player, root)) {
                event.getCommands().remove(root);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncCommandTreeSend(AsyncPlayerSendCommandsEvent<?> event) {
        Player player = event.getPlayer();
        RootCommandNode<?> rootNode = event.getCommandNode();

        for (String root : new ArrayList<>(rootNode.getChildren().stream().map(node -> node.getName()).toList())) {
            if (!commandResolver.canUse(player, root)) {
                rootNode.removeCommand(root);
            }
        }
    }
}
