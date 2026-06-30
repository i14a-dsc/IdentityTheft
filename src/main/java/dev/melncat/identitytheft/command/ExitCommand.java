package dev.melncat.identitytheft.command;

import dev.melncat.identitytheft.IdentityManager;
import dev.melncat.identitytheft.IdentityTheft;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;

import java.util.UUID;

@SuppressWarnings("unused")
public class ExitCommand implements CommandExecutor {
	private final IdentityTheft plugin;

	public ExitCommand(IdentityTheft plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You must be a player to use this command.");
			return true;
		}

		Player player = (Player) sender;
		UUID realUUID = getRealPlayer(player.getUniqueId());

		if (IdentityManager.getInstance().hasChangedIdentity(realUUID)) {
			IdentityManager.getInstance().removeChangedIdentity(realUUID);
			player.kick(Component.text("Please rejoin for changes to apply."));
		} else {
			sender.sendMessage("§eYour identity is not altered.");
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
}
