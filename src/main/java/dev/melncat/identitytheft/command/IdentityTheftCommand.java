package dev.melncat.identitytheft.command;

import dev.melncat.identitytheft.IdentityManager;
import dev.melncat.identitytheft.IdentityTheft;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class IdentityTheftCommand implements CommandExecutor, TabCompleter {
    private final IdentityTheft plugin;
    
    public IdentityTheftCommand(IdentityTheft plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text()
                .append(Component.text(plugin.getName(), NamedTextColor.YELLOW))
                .append(Component.text(" v", NamedTextColor.GRAY))
                .append(Component.text(plugin.getPluginMeta().getVersion(), NamedTextColor.GREEN))
                .append(Component.text("\nMade by ", NamedTextColor.GRAY))
                .append(Component.text(plugin.getPluginMeta().getAuthors().get(0), NamedTextColor.GREEN))
                .build());
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("identitytheft.command.identitytheft.reload")) {
                sendMissingPermission(sender, "identitytheft.command.identitytheft.reload");
                return true;
            }
            plugin.reloadConfig();
            plugin.getITConfig().setConfig(plugin.getConfig());
            sender.sendMessage(Component.text("IdentityTheft configuration successfully reloaded.", NamedTextColor.YELLOW));
        } else if (args[0].equalsIgnoreCase("become")) {
            if (!sender.hasPermission("identitytheft.command.identitytheft.become")) {
                sendMissingPermission(sender, "identitytheft.command.identitytheft.become");
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage(Component.text("You must be a player to use this command.", NamedTextColor.RED));
                return true;
            }
            if (args.length < 2) {
                sender.sendMessage(Component.text()
                    .append(Component.text("Insufficient arguments provided.\n", NamedTextColor.RED))
                    .append(Component.text("Syntax: /it become <player>", NamedTextColor.RED))
                    .build());
                return true;
            }
            if (((Player) sender).getPlayerProfile().hasProperty("it_real")) {
                sender.sendMessage(Component.text()
                    .append(Component.text("You cannot use this while your identity is changed.\n", NamedTextColor.RED))
                    .append(Component.text("Reset it first with /it reset.", NamedTextColor.RED))
                    .build());
                return true;
            }
            UUID target = playerFromString(args[1]);
            if (target == null) {
                sender.sendMessage(Component.text(args[1] + " is not a valid player.", NamedTextColor.RED));
                return true;
            }
            if (plugin.getITConfig().opProtection()
                && !sender.isOp()
                && Bukkit.getOperators().stream().anyMatch(x -> x.getUniqueId().equals(target))
            ) {
                sender.sendMessage(Component.text("You cannot change your identity into an operator.", NamedTextColor.RED));
                return true;
            }
            IdentityManager.getInstance().setChangedIdentity(((Player) sender).getUniqueId(), target);
            ((Player) sender).kick(Component.text("Please rejoin for changes to apply."));
        } else if (args[0].equalsIgnoreCase("set")) {
            if (!sender.hasPermission("identitytheft.command.identitytheft.set")) {
                sendMissingPermission(sender, "identitytheft.command.identitytheft.set");
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(Component.text()
                    .append(Component.text("Insufficient arguments provided.\n", NamedTextColor.RED))
                    .append(Component.text("Example: /it set <from> <to>", NamedTextColor.RED))
                    .build());
                return true;
            }
            UUID from = playerFromString(args[1]);
            if (from == null) {
                sender.sendMessage(Component.text(args[1] + " is not a valid player.", NamedTextColor.RED));
                return true;
            }
            UUID to = playerFromString(args[2]);
            if (to == null) {
                sender.sendMessage(Component.text(args[2] + " is not a valid player.", NamedTextColor.RED));
                return true;
            }
            if (plugin.getITConfig().opProtection()
                && !sender.isOp()
                && Bukkit.getOperators().stream().anyMatch(x -> x.getUniqueId().equals(from) || x.getUniqueId().equals(to))
            ) {
                sender.sendMessage(Component.text("You cannot change the identities of operators.", NamedTextColor.RED));
                return true;
            }
            IdentityManager.getInstance().setChangedIdentity(from, to);
            sender.sendMessage(Component.text()
                .append(Component.text("The player ", NamedTextColor.YELLOW))
                .append(Component.text(args[1], NamedTextColor.WHITE))
                .append(Component.text(" has successfully been changed to ", NamedTextColor.YELLOW))
                .append(Component.text(args[2], NamedTextColor.WHITE))
                .append(Component.text(".", NamedTextColor.YELLOW))
                .build());
            if (Bukkit.getPlayer(from) != null) Bukkit.getPlayer(from).kick(Component.text("Disconnected"));
        } else if (args[0].equalsIgnoreCase("reset")) {
            if (!sender.hasPermission("identitytheft.command.identitytheft.reset")) {
                sendMissingPermission(sender, "identitytheft.command.identitytheft.reset");
                return true;
            }
            if (args.length < 2) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Component.text("You must be a player to use /it reset without arguments.", NamedTextColor.RED));
                    return true;
                }
                if (IdentityManager.getInstance().hasChangedIdentity(getRealPlayer(((Player) sender).getUniqueId()))) {
                    IdentityManager.getInstance().removeChangedIdentity(getRealPlayer(((Player) sender).getUniqueId()));
                    ((Player) sender).kick(Component.text("Please rejoin for changes to apply."));
                } else sender.sendMessage(Component.text("Your identity is not altered.", NamedTextColor.RED));
                return true;
            }
            if (!sender.hasPermission("identitytheft.command.identitytheft.reset.others")) {
                sendMissingPermission(sender, "identitytheft.command.identitytheft.reset.others");
                return true;
            }
            UUID target = playerFromString(args[1]);
            if (target == null) {
                sender.sendMessage(Component.text(args[1] + " is not a valid player.", NamedTextColor.RED));
                return true;
            }
            if (IdentityManager.getInstance().hasChangedIdentity(target)) {
                IdentityManager.getInstance().removeChangedIdentity(target);
                sender.sendMessage(Component.text("The identity of " + args[1] + " has been reset.", NamedTextColor.YELLOW));
            } else sender.sendMessage(Component.text("The identity of " + args[1] + "is not altered.", NamedTextColor.RED));
        }
        return true;
    }
    private UUID getRealPlayer(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return uuid;
        if (player.getPlayerProfile().hasProperty("it_real"))
            return UUID.fromString(player.getPlayerProfile().getProperties().stream().filter(x -> x.getName().equals("it_real")).findFirst().get().getValue());
        return uuid;
    }
    
    private void sendMissingPermission(CommandSender sender, String permission) {
        sender.sendMessage(Component.text()
            .append(Component.text("You do not have permission to perform this command.\n", NamedTextColor.RED))
            .append(Component.text("Missing permission ", NamedTextColor.RED))
            .append(Component.text(permission, NamedTextColor.DARK_RED))
            .build());
    }
    
    private UUID playerFromString(String str) {
        try {
            return UUID.fromString(str);
        } catch (IllegalArgumentException e) {
            return Bukkit.getPlayerUniqueId(str);
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String name, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            if (sender.hasPermission("identitytheft.command.identitytheft.reload")) suggestions.add("reload");
            if (sender.hasPermission("identitytheft.command.identitytheft.become")) suggestions.add("become");
            if (sender.hasPermission("identitytheft.command.identitytheft.set")) suggestions.add("set");
            if (sender.hasPermission("identitytheft.command.identitytheft.reset")) suggestions.add("reset");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("become") || args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("reset"))
                suggestions.addAll(listPlayerNames());
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("set")) suggestions.addAll(listPlayerNames());
        }
        return suggestions.stream().filter(x -> x.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).collect(Collectors.toList());
    }
    @SuppressWarnings("null")
    private List<String> listPlayerNames() {
        return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
    }
}