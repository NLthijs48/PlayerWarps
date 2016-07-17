package me.wiefferink.playerwarps.commands;

import me.wiefferink.playerwarps.FileManager;
import me.wiefferink.playerwarps.PlayerWarps;
import me.wiefferink.playerwarps.Warp;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;

public class PublicCommand extends CommandPlayerWarps {

	public PublicCommand(PlayerWarps plugin) {
		super(plugin);
	}

	@Override
	public String getCommandStart() {
		return "warp public";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("playerwarps.public")) {
			return "help-public";
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		FileManager fileManager = plugin.getFileManager();
		// check permission
		if(!sender.hasPermission("playerwarps.public")) {
			plugin.message(sender, "public-noPermission");
			return;
		}

		if(fileManager.getAllPlayerWarps().size() == 0) {
			plugin.message(sender, "public-noWarps");
		} else {
			if(args.length == 1) {
				plugin.message(sender, "public-header");
				TreeSet<String> sortedNames = new TreeSet<>();
				for(UUID uPlayer : fileManager.getAllPlayerWarps().keySet()) {
					OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uPlayer);
					if(offlinePlayer != null) {
						sortedNames.add(offlinePlayer.getName());
					}
				}
				for(String warpPlayer : sortedNames) {
					UUID warpPlayerId = Bukkit.getOfflinePlayer(warpPlayer).getUniqueId();
					Map<String, Warp> playerWarps = fileManager.getPlayerWarps(warpPlayerId);

					TreeSet<String> sorted2 = new TreeSet<>(playerWarps.keySet());
					// Add all warps to a string
					if(!(fileManager.getCurrentTotalWarps(warpPlayerId) == 0)) {
						String warps = "";
						String realName = "";
						boolean first = true;
						for(String warpName : sorted2) {
							Warp warp = playerWarps.get(warpName);
							UUID senderUUID = null;
							if(sender instanceof Player) {
								senderUUID = ((Player)sender).getUniqueId();
							}
							if(warp.isPublished()) {
								if(first) {
									warps += warp.getName();
									realName = warp.getPlayer().getName();
									first = false;
								} else {
									warps += ", "+warp.getName();
								}
							} else if(warp.getTrustedPlayers().contains(senderUUID)) {
								if(first) {
									warps += ChatColor.GREEN+warp.getName()+ChatColor.RESET;
									realName = warp.getPlayer().getName();
									first = false;
								} else {
									warps += ", "+warp.getName();
								}
							}
						}
						if(warps.length() != 0) {
							plugin.messageNoPrefix(sender, "public-entry", realName, warps);
						}
					}
				}
			} else {
				// List public warps from another player
				OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
				if(player == null) {
					plugin.message(sender, "cmd-noPlayer", args[1]);
					return;
				}
				if(fileManager.getCurrentTotalWarps(player.getUniqueId()) == 0) {
					plugin.message(sender, "public-noWarpsPlayer", args[1]);
				} else {
					String warps = "";
					Map<String, Warp> playerWarps = fileManager.getPlayerWarps(player.getUniqueId());
					TreeSet<String> sorted = new TreeSet<>(playerWarps.keySet());
					String realName = "";

					boolean first = true;
					for(String warpName : sorted) {
						Warp warp = playerWarps.get(warpName);
						UUID senderUUID = null;
						if(sender instanceof Player) {
							senderUUID = ((Player)sender).getUniqueId();
						}
						if(warp.isPublished()) {
							if(first) {
								warps += warp.getName();
								realName = warp.getPlayer().getName();
								first = false;
							} else {
								warps += ", "+warp.getName();
							}
						} else if(warp.getTrustedPlayers().contains(senderUUID)) {
							if(first) {
								warps += ChatColor.GREEN+warp.getName()+ChatColor.RESET;
								realName = warp.getPlayer().getName();
								first = false;
							} else {
								warps += ", "+warp.getName();
							}
						}
					}
					if(warps.length() != 0) {
						plugin.message(sender, "public-player", realName, warps);
					} else {
						plugin.message(sender, "public-noWarpsPlayer", args[1]);
					}
				}
			}
		}
	}

}










