package me.wiefferink.playerwarps.commands;

import me.wiefferink.interactivemessenger.processing.Message;
import me.wiefferink.playerwarps.FileManager;
import me.wiefferink.playerwarps.PlayerWarps;
import me.wiefferink.playerwarps.Warp;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
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
					if(offlinePlayer != null && offlinePlayer.getName() != null) {
						sortedNames.add(offlinePlayer.getName());
					}
				}
				for(String warpPlayer : sortedNames) {
					OfflinePlayer player = Bukkit.getOfflinePlayer(warpPlayer);
					Map<String, Warp> playerWarps = fileManager.getPlayerWarps(player.getUniqueId());
					if(playerWarps == null) {
						plugin.getFileManager().removePlayerWarps(player.getUniqueId());
						continue;
					}

					TreeSet<String> sorted = new TreeSet<>(playerWarps.keySet());

					// Add all warps to a string
					if (!(fileManager.getCurrentTotalWarps(player.getUniqueId()) == 0)) {
						List<String> warps = new ArrayList<>();
						for (String warpName : sorted) {
							Warp warp = playerWarps.get(warpName);
							UUID senderUUID = null;
							if(sender instanceof Player) {
								senderUUID = ((Player)sender).getUniqueId();
							}
							if(warp.isPublished()) {
								warps.add(warp.getName());
							} else if(warp.getTrustedPlayers().contains(senderUUID)) {
								warps.add(ChatColor.GREEN + warp.getName() + ChatColor.RESET);
							}
						}

						if (!warps.isEmpty()) {
							plugin.messageNoPrefix(sender, "public-entry", player.getName(), Message.fromString(StringUtils.join(warps, ", ")));
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
					Map<String, Warp> playerWarps = fileManager.getPlayerWarps(player.getUniqueId());
					TreeSet<String> sorted = new TreeSet<>(playerWarps.keySet());
					List<String> warps = new ArrayList<>();

					for(String warpName : sorted) {
						Warp warp = playerWarps.get(warpName);
						UUID senderUUID = null;
						if(sender instanceof Player) {
							senderUUID = ((Player)sender).getUniqueId();
						}
						if(warp.isPublished()) {
							warps.add(warp.getName());
						} else if(warp.getTrustedPlayers().contains(senderUUID)) {
							warps.add(ChatColor.GREEN + warp.getName() + ChatColor.RESET);
						}
					}
					if (!warps.isEmpty()) {
						plugin.message(sender, "public-player", player.getName(), Message.fromString(StringUtils.join(warps, ", ")));
					} else {
						plugin.message(sender, "public-noWarpsPlayer", args[1]);
					}
				}
			}
		}
	}

}










