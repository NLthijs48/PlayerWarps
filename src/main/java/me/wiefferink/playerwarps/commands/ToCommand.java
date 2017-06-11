package me.wiefferink.playerwarps.commands;

import me.wiefferink.playerwarps.PlayerWarps;
import me.wiefferink.playerwarps.Utils;
import me.wiefferink.playerwarps.Warp;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ToCommand extends CommandPlayerWarps {

	public ToCommand(PlayerWarps plugin) {
		super(plugin);
	}

	@Override
	public String getCommandStart() {
		return "warp to";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("playerwarps.to")) {
			return "help-to";
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		// Check if it is a player
		if(!(sender instanceof Player)) {
			plugin.message(sender, "cmd-onlyByPlayer");
			return;
		}
		Player player = (Player)sender;
		String playerName = player.getName();

		// check permission
		if(!player.hasPermission("playerwarps.to")) {
			plugin.message(player, "to-noPermission");
			return;
		}

		if(args.length == 1) {
			if(sender.hasPermission("playerwarps.topublic") || sender.hasPermission("playerwarps.toprivate")) {
				plugin.message(sender, "to-helpOther");
			} else {
				plugin.message(sender, "to-help");
			}
		} else if(args.length == 2) {
			Map<String, Warp> playerWarps = plugin.getFileManager().getPlayerWarps(player.getUniqueId());
			if(playerWarps == null) {
				plugin.message(player, "to-hasNoWarps");
				return;
			}

			Warp warp = playerWarps.get(args[1].toLowerCase());
			if(warp == null) {
				plugin.message(player, "to-wrongWarp", args[1]);
				return;
			}

			boolean warpResult = Utils.teleportToLocation(player, warp.getLocation(), plugin.getConfig().getInt("maximumTries"));
			if(!warpResult) {
				plugin.message(player, "to-failed");
				return;
			}
			plugin.message(player, "to-warped", warp.getName());
		} else { // Go to warp of other person
			OfflinePlayer otherPlayer = Bukkit.getOfflinePlayer(args[2]);
			if(otherPlayer == null) {
				plugin.message(player, "cmd-noPlayer", args[2]);
				return;
			}
			Map<String, Warp> playerWarps = plugin.getFileManager().getPlayerWarps(otherPlayer.getUniqueId());
			if(playerWarps == null) {
				plugin.message(player, "to-noWarpsOther", args[2]);
				return;
			}

			Warp warp = playerWarps.get(args[1].toLowerCase());
			if(warp == null) {
				plugin.message(player, "to-wrongWarpOther", args[2], args[1]);
				return;
			}

			// Permisssions
			if(warp.isPublished() && !player.hasPermission("playerwarps.topublic") && !warp.isTrusted(player.getUniqueId())) {
				plugin.message(player, "to-noPermissionPublic");
				return;
			}
			if(!warp.isPublished() && !player.hasPermission("playerwarps.toprivate") && !warp.isTrusted(player.getUniqueId())) {
				plugin.message(player, "to-noPermissionPrivate");
				return;
			}

			boolean warpResult = Utils.teleportToLocation(player, warp.getLocation(), plugin.getConfig().getInt("maximumTries"));
			if(!warpResult) {
				plugin.message(player, "to-failed");
				return;
			}
			plugin.message(player, "to-warpedOther", warp.getName(), warp.getPlayer().getName());

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










