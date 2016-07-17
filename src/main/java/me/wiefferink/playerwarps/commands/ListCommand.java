package me.wiefferink.playerwarps.commands;

import me.wiefferink.playerwarps.FileManager;
import me.wiefferink.playerwarps.PlayerWarps;
import me.wiefferink.playerwarps.Warp;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;

public class ListCommand extends CommandPlayerWarps {

	public ListCommand(PlayerWarps plugin) {
		super(plugin);
	}

	@Override
	public String getCommandStart() {
		return "warp list";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("playerwarps.list")) {
			return "help-list";
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		FileManager fileManager = plugin.getFileManager();
		UUID id;
		String name;
		if(args.length == 1) {
			// Check if it is a player
			if(!(sender instanceof Player)) {
				plugin.message(sender, "cmd-onlyByPlayer");
				return;
			}
			Player player = (Player)sender;
			id = player.getUniqueId();
			name = player.getName();

			// check permission
			if(!player.hasPermission("playerwarps.list")) {
				plugin.message(player, "list-noPermission");
				return;
			}
		} else {
			// check permission
			if(!sender.hasPermission("playerwarps.listOther")) {
				plugin.message(sender, "list-noPermissionOther");
				return;
			}
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
			if(offlinePlayer == null) {
				plugin.message(sender, "cmd-noPlayer", args[1]);
				return;
			}
			id = offlinePlayer.getUniqueId();
			name = offlinePlayer.getName();
		}

		Map<String, Warp> playerWarps = fileManager.getPlayerWarps(id);
		if(playerWarps == null) {
			plugin.message(sender, "list-hasNoWarps", name);
		} else {
			plugin.message(sender, "list-header", name);
			TreeSet<String> sorted = new TreeSet<>(playerWarps.keySet());
			// Public part
			if(fileManager.getCurrentPublicWarps(id) == 0) {
				plugin.messageNoPrefix(sender, "list-noPublic");
			} else {
				String msg = "";
				boolean first = true;
				for(String warpName : sorted) {
					if(playerWarps.get(warpName).isPublished()) {
						if(first) {
							msg += playerWarps.get(warpName).getName();
							first = false;
						} else {
							msg += ", "+playerWarps.get(warpName).getName();
						}
					}
				}
				plugin.messageNoPrefix(sender, "list-public", msg);
			}
			// Private part
			if(fileManager.getCurrentPrivateWarps(id) == 0) {
				plugin.messageNoPrefix(sender, "list-noPrivate");
			} else {
				String msg = "";
				boolean first = true;
				for(String warpName : sorted) {
					if(!playerWarps.get(warpName).isPublished()) {
						if(first) {
							msg += playerWarps.get(warpName).getName();
							first = false;
						} else {
							msg += ", "+playerWarps.get(warpName).getName();
						}
					}
				}
				plugin.messageNoPrefix(sender, "list-private", msg);
			}
		}
	}

}










