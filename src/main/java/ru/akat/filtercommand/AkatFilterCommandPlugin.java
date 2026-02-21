package ru.akat.filtercommand;

import org.bukkit.plugin.java.JavaPlugin;

public final class AkatFilterCommandPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        var resolver = new CommandResolver(this);
        getServer().getPluginManager().registerEvents(new CommandFilterListener(resolver), this);
        getLogger().info("AkatFilterCommand enabled: filtering command roots in tab completion.");
    }
}
