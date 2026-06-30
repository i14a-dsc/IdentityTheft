package dev.melncat.identitytheft;

import dev.melncat.identitytheft.command.ExitCommand;
import dev.melncat.identitytheft.command.IdentityTheftCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.util.Arrays;

public final class IdentityTheft extends JavaPlugin {
    private IdentityTheftConfig config;

    public IdentityTheftConfig getITConfig() {
        return config;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = new IdentityTheftConfig(getConfig());

        IdentityTheftCommand command = new IdentityTheftCommand(this);
        registerCommand("identitytheft", command, command, Arrays.asList("it"), "The main command.", "identitytheft.command.identitytheft");

        ExitCommand exitCommand = new ExitCommand(this);
        registerCommand("exit", exitCommand, null, null, "Resets your identity without permission checks.", null);

        Bukkit.getPluginManager().registerEvents(new IdentityTheftListener(this), this);
    }

    private void registerCommand(String name, org.bukkit.command.CommandExecutor executor,
                                  org.bukkit.command.TabCompleter tabCompleter,
                                  java.util.List<String> aliases, String description, String permission) {
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, org.bukkit.plugin.Plugin.class);
            constructor.setAccessible(true);
            PluginCommand cmd = constructor.newInstance(name, this);
            cmd.setExecutor(executor);
            if (tabCompleter != null) cmd.setTabCompleter(tabCompleter);
            if (aliases != null) cmd.setAliases(aliases);
            if (description != null) cmd.setDescription(description);
            if (permission != null) cmd.setPermission(permission);

            CommandMap commandMap = getServer().getCommandMap();
            commandMap.register(getPluginMeta().getName(), cmd);
        } catch (Exception e) {
            getLogger().severe("Failed to register command: " + name);
            e.printStackTrace();
        }
    }
    

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
