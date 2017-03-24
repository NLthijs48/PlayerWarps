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
import java.util.UUID;

public class AddCommand extends CommandPlayerWarps {

	public AddCommand(PlayerWarps plugin) {
		super(plugin);
	}

	@Override
	public String getCommandStart() {
		return "warp add";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("playerwarps.add")) {
			return "help-add";
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		FileManager fileManager = plugin.getFileManager();
		// Check if it is a player
		if(!(sender instanceof Player)) {
			plugin.message(sender, "cmd-onlyByPlayer");
			return;
		}
		Player player = (Player)sender;
		OfflinePlayer forPlayer = player;

		// Check permission
		if(!player.hasPermission("playerwarps.add")) {
			plugin.message(player, "add-noPermission");
			return;
		}

		// Check the name
		if(args.length < 2) {
			if(player.hasPermission("playerwarps.addother")) {
				plugin.message(player, "add-wrongArgsOther");
			} else {
				plugin.message(player, "add-wrongArgs");
			}
			return;
		}

		// Published or not?
		boolean published = false;
		String pub = plugin.getConfig().getString("defaultAccess");
		if(args.length >= 3) {
			pub = args[2];
			if("public".equalsIgnoreCase(pub)) {
				published = true;
			} else if("private".equalsIgnoreCase(pub)) {
				published = false;
			} else {
				plugin.message(player, "add-wrongAccess");
				return;
			}
		} else {
			if("public".equalsIgnoreCase(pub)) {
				published = true;
			}
		}

		// Set the playerName to the given player if
		if(args.length >= 4) {
			if(!player.hasPermission("playerwarps.addother")) {
				plugin.message(player, "add-wrongArgs");
				return;
			}
			forPlayer = Bukkit.getOfflinePlayer(args[3]);
			if(forPlayer == null) {
				plugin.message(player, "cmd-noPlayer");
				return;
			}
		}

		// Check max amount
		UUID id = forPlayer.getUniqueId();
		if (!(args.length >= 4) || !plugin.getConfig().getBoolean("addwarpOtherLimitBypas")) {
			int currentTotal = fileManager.getCurrentTotalWarps(id);
			int possibleTotal = fileManager.getPossibleTotalWarps(id);
			int currentPrivate = fileManager.getCurrentPrivateWarps(id);
			int possiblePrivate = fileManager.getPossiblePrivateWarps(id);
			int currentPublic = fileManager.getCurrentPublicWarps(id);
			int possiblePublic = fileManager.getPossiblePublicWarps(id);
			if((possibleTotal-currentTotal) <= 0) {
				plugin.message(player, "add-totalMax", currentTotal, possibleTotal);
			} else if(!published && (possiblePrivate-currentPrivate) <= 0) {
				plugin.message(player, "add-privateMax", currentPrivate, possiblePrivate);
			} else if(published && (possiblePublic-currentPublic) <= 0) {
				plugin.message(player, "add-publicMax", currentPublic, possiblePublic);
			} else {
				Warp warp = plugin.getFileManager().getWarp(id, args[1]);
				boolean overridden = warp != null;
				if(warp == null) {
					warp = new Warp(id);
				}
				warp.setLocation(player.getLocation());
				warp.setName(args[1]);
				warp.setPublished(published);
				plugin.getFileManager().addPlayerWarp(id, warp);
				if(overridden) {
					plugin.message(player, "add-overridden", args[1]);
				} else {
					plugin.message(player, "add-added", args[1]);
				}
			}
		} else {
			Warp warp = plugin.getFileManager().getWarp(id, args[1]);
			boolean overridden = warp != null;
			if(warp == null) {
				warp = new Warp(id);
			}
			warp.setLocation(player.getLocation());
			warp.setName(args[1]);
			warp.setPublished(published);
			plugin.getFileManager().addPlayerWarp(id, warp);
			if(overridden) {
				plugin.message(player, "add-overriddenOther", args[1], forPlayer.getName());
			} else {
				plugin.message(player, "add-addedOther", args[1], forPlayer.getName());
			}
		}
		plugin.getFileManager().saveFiles();
	}

	@Override
	public List<String> getTabCompleteList(int toComplete, String[] start, CommandSender sender) {
		List<String> result = new ArrayList<>();
		if(toComplete == 3) {
			result.add("private");
			result.add("public");
		}
		return result;
	}

}










