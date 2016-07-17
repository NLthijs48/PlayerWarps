package me.wiefferink.playerwarps.commands;

import me.wiefferink.playerwarps.FileManager;
import me.wiefferink.playerwarps.PlayerWarps;
import me.wiefferink.playerwarps.Warp;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DelCommand extends CommandPlayerWarps {

	public DelCommand(PlayerWarps plugin) {
		super(plugin);
	}

	@Override
	public String getCommandStart() {
		return "warp del";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("playerwarps.del")) {
			return "help-del";
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		FileManager fileManager = plugin.getFileManager();
		// Check permission
		if(!sender.hasPermission("playerwarps.del")) {
			plugin.message(sender, "del-noPermission");
			return;
		}

		// Check the name
		if(args.length < 2) {
			if(sender.hasPermission("playerwarps.delother")) {
				plugin.message(sender, "del-wrongArgsOther");
			} else {
				plugin.message(sender, "del-wrongArgs");
			}
		} else if(args.length == 2) {
			// Check if it is a player
			if(!(sender instanceof Player)) {
				plugin.message(sender, "cmd-onlyByPlayer");
				return;
			}
			Player player = (Player)sender;
			String playerName = player.getName();

			if(fileManager.getPlayerWarps(player.getUniqueId()) == null) {
				plugin.message(sender, "del-wrongWarp", args[1]);
			} else {
				Warp warp = fileManager.getWarp(player.getUniqueId(), args[1]);
				if(warp != null) {
					fileManager.removePlayerWarp(player.getUniqueId(), args[1]);
					plugin.message(sender, "del-deleted", warp.getName());
					fileManager.saveFiles();
				} else {
					plugin.message(sender, "del-wrongWarp", args[1]);
				}
			}
		} else {
			// Check permission
			if(!sender.hasPermission("playerwarps.delother")) {
				plugin.message(sender, "del-noPermission");
				return;
			}

			OfflinePlayer otherPlayer = Bukkit.getOfflinePlayer(args[2]);
			if(otherPlayer == null) {
				plugin.message(sender, "cmd-noPlayer", args[2]);
				return;
			}
			if(fileManager.getPlayerWarps(otherPlayer.getUniqueId()) == null) {
				plugin.message(sender, "del-wrongWarp", args[1]);
			} else {
				Warp warp = fileManager.getWarp(otherPlayer.getUniqueId(), args[1]);
				if(warp != null) {
					fileManager.removePlayerWarp(otherPlayer.getUniqueId(), args[1]);
					plugin.message(sender, "del-deletedOther", warp.getName(), args[2]);
					fileManager.saveFiles();
				} else {
					plugin.message(sender, "del-wrongWarpOther", args[2], args[1]);
				}
			}
		}
	}

	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		List<String> result = new ArrayList<>();
		if(toComplete == 2) {
			if(sender instanceof Player) {
				Player player = (Player)sender;
				Map<String, Warp> playerWarps = plugin.getFileManager().getPlayerWarps(player.getUniqueId());
				if(playerWarps != null) {
					for(Warp warp : playerWarps.values()) {
						result.add(warp.getName());
					}
				}
			}
		}
		return result;
	}

}










