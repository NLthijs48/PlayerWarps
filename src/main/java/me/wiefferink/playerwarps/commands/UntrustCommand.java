package me.wiefferink.playerwarps.commands;

import me.wiefferink.playerwarps.FileManager;
import me.wiefferink.playerwarps.PlayerWarps;
import me.wiefferink.playerwarps.Warp;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class UntrustCommand extends CommandPlayerWarps {

	public UntrustCommand(PlayerWarps plugin) {
		super(plugin);
	}

	@Override
	public String getCommandStart() {
		return "warp untrust";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("playerwarps.untrust")) {
			return "help-untrust";
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		FileManager fileManager = plugin.getFileManager();
		// Check permission
		if(!sender.hasPermission("playerwarps.untrust")) {
			plugin.message(sender, "untrust-noPermission");
			return;
		}

		// Check the name
		if(args.length < 3) {
			if(sender.hasPermission("playerwarps.untrustother")) {
				plugin.message(sender, "untrust-wrongArgsOther");
			} else {
				plugin.message(sender, "untrust-wrongArgs");
			}
		} else if(args.length == 3) {
			// Check if it is a player
			if(!(sender instanceof Player)) {
				plugin.message(sender, "cmd-onlyByPlayer");
				return;
			}
			Player player = (Player)sender;
			Map<String, Warp> playerWarps = fileManager.getPlayerWarps(player.getUniqueId());
			if(playerWarps == null) {
				plugin.message(sender, "untrust-noWarps", args[2]);
				return;
			}
			Warp warp = playerWarps.get(args[2].toLowerCase());
			if(warp == null) {
				plugin.message(sender, "untrust-noWarps", args[2]);
				return;
			}
			OfflinePlayer removePlayer = Bukkit.getOfflinePlayer(args[1]);
			if(removePlayer == null) {
				plugin.message(sender, "cmd-noPlayer", args[1]);
				return;
			}
			if(!warp.isTrusted(removePlayer.getUniqueId())) {
				plugin.message(player, "untrust-notTrusted", removePlayer.getName(), warp.getName());
			} else {
				warp.removeTrustedPlayer(removePlayer.getUniqueId());
				plugin.message(player, "untrust-success", removePlayer.getName(), warp.getName());
				plugin.getFileManager().saveFiles();
			}
		} else {
			// Check permission
			if(!sender.hasPermission("playerwarps.untrustother")) {
				plugin.message(sender, "untrust-noPermissionOther");
				return;
			}

			OfflinePlayer forPlayer = Bukkit.getOfflinePlayer(args[3]);
			if(forPlayer == null) {
				plugin.message(sender, "cmd-noPlayer", args[1]);
				return;
			}
			Map<String, Warp> playerWarps = fileManager.getPlayerWarps(forPlayer.getUniqueId());
			if(playerWarps == null) {
				plugin.message(sender, "untrust-noWarpsOther", forPlayer.getName(), args[2]);
				return;
			}

			Warp warp = playerWarps.get(args[2].toLowerCase());
			if(warp == null) {
				plugin.message(sender, "untrust-noWarpsOther", forPlayer.getName(), args[2]);
				return;
			}
			OfflinePlayer removePlayer = Bukkit.getOfflinePlayer(args[1]);
			if(removePlayer == null) {
				plugin.message(sender, "cmd-noPlayer", args[1]);
				return;
			}
			if(!warp.isTrusted(removePlayer.getUniqueId())) {
				plugin.message(sender, "untrust-notTrustedOther", removePlayer.getName(), warp.getName(), warp.getPlayer().getName());
			} else {
				warp.removeTrustedPlayer(removePlayer.getUniqueId());
				plugin.message(sender, "untrust-successOther", removePlayer.getUniqueId(), warp.getName(), warp.getPlayer().getName());
				plugin.getFileManager().saveFiles();
			}
		}
	}

}










