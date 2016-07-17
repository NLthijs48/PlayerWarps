package me.wiefferink.playerwarps.commands;

import me.wiefferink.playerwarps.FileManager;
import me.wiefferink.playerwarps.PlayerWarps;
import me.wiefferink.playerwarps.Warp;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class TrustCommand extends CommandPlayerWarps {

	public TrustCommand(PlayerWarps plugin) {
		super(plugin);
	}

	@Override
	public String getCommandStart() {
		return "warp trust";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("playerwarps.trust")) {
			return "help-trust";
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		FileManager fileManager = plugin.getFileManager();
		// Check permission
		if(!sender.hasPermission("playerwarps.trust")) {
			plugin.message(sender, "trust-noPermission");
			return;
		}

		// Check the name
		if(args.length < 3) {
			if(sender.hasPermission("playerwarps.trustother")) {
				plugin.message(sender, "trust-wrongArgsOther");
			} else {
				plugin.message(sender, "trust-wrongArgs");
			}
		} else if(args.length == 3) {
			// Check if it is a player
			if(!(sender instanceof Player)) {
				plugin.message(sender, "cmd-onlyByPlayer");
				return;
			}
			Player player = (Player)sender;
			String playerName = player.getName();

			Map<String, Warp> playerWarps = fileManager.getPlayerWarps(player.getUniqueId());
			if(playerWarps == null) {
				plugin.message(sender, "trust-noWarps", args[2]);
				return;
			}
			Warp warp = playerWarps.get(args[2].toLowerCase());
			if(warp == null) {
				plugin.message(sender, "trust-noWarps", args[2]);
				return;
			}
			OfflinePlayer addPlayer = Bukkit.getOfflinePlayer(args[1]);
			if(addPlayer == null) {
				plugin.message(sender, "cmd-noPlayer", args[1]);
				return;
			}
			if(warp.isTrusted(addPlayer.getUniqueId())) {
				plugin.message(player, "trust-alreadyTrusted", addPlayer.getName(), warp.getName());
			} else {
				warp.addTrustedPlayer(addPlayer.getUniqueId());
				plugin.message(player, "trust-success", addPlayer.getName(), warp.getName());
				plugin.getFileManager().saveFiles();
			}
		} else {
			// Check permission
			if(!sender.hasPermission("playerwarps.trustother")) {
				plugin.message(sender, "trust-noPermissionOther");
				return;
			}

			OfflinePlayer forPlayer = Bukkit.getOfflinePlayer(args[3]);
			if(forPlayer == null) {
				plugin.message(sender, "cmd-noPlayer", args[1]);
				return;
			}
			Map<String, Warp> playerWarps = fileManager.getPlayerWarps(forPlayer.getUniqueId());
			if(playerWarps == null) {
				plugin.message(sender, "trust-noWarpsOther", forPlayer.getName(), args[2]);
				return;
			}

			Warp warp = playerWarps.get(args[2].toLowerCase());
			if(warp == null) {
				plugin.message(sender, "trust-noWarpsOther", forPlayer.getName(), args[2]);
				return;
			}
			OfflinePlayer addPlayer = Bukkit.getOfflinePlayer(args[1]);
			if(addPlayer == null) {
				plugin.message(sender, "cmd-noPlayer", args[1]);
				return;
			}
			if(warp.isTrusted(addPlayer.getUniqueId())) {
				plugin.message(sender, "trust-alreadyTrustedOther", addPlayer.getName(), warp.getName(), warp.getPlayer().getName());
			} else {
				warp.addTrustedPlayer(addPlayer.getUniqueId());
				plugin.message(sender, "trust-successOther", addPlayer.getName(), warp.getName(), warp.getPlayer().getName());
				plugin.getFileManager().saveFiles();
			}
		}
	}

}










