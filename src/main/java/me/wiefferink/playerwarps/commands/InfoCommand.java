package me.wiefferink.playerwarps.commands;

import me.wiefferink.playerwarps.FileManager;
import me.wiefferink.playerwarps.PlayerWarps;
import me.wiefferink.playerwarps.Warp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class InfoCommand extends CommandPlayerWarps {

	public InfoCommand(PlayerWarps plugin) {
		super(plugin);
	}

	@Override
	public String getCommandStart() {
		return "warp info";
	}

	@Override
	public String getHelp(CommandSender target) {
		if(target.hasPermission("playerwarps.info")) {
			return "help-info";
		}
		return null;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		FileManager fileManager = plugin.getFileManager();
		// check permission
		if(!sender.hasPermission("playerwarps.info")) {
			plugin.message(sender, "info-noPermission");
			return;
		}

		// check arguments
		if(args.length == 1) {
			plugin.message(sender, "info-wrongArgs");
			return;
		} else if(args.length == 2 && !(sender instanceof Player)) {
			plugin.message(sender, "info-specifyPlayer");
			return;
		}


		boolean isSelf = true;
		OfflinePlayer player = null;
		if(sender instanceof Player) {
			player = (Player)sender;
		}
		if(args.length > 2) {
			player = Bukkit.getOfflinePlayer(args[2]);
			if(player == null) {
				plugin.message(sender, "cmd-noPlayer", args[2]);
				return;
			}
			isSelf = (sender instanceof Player && ((Player)sender).getUniqueId().equals(player.getUniqueId()));
		}
		if(player == null) {
			plugin.message(sender, "info-wrongArgs");
			return;
		}

		Map<String, Warp> playerWarps = fileManager.getPlayerWarps(player.getUniqueId());
		if(playerWarps == null) {
			plugin.message(sender, "info-wrongWarp", player.getName(), args[1]);
			return;
		}

		Warp warp = playerWarps.get(args[1].toLowerCase());
		if(warp == null) {
			plugin.message(sender, "info-wrongWarp", player.getName(), args[1]);
			return;
		}

		if(!isSelf && !sender.hasPermission("playerwarps.infopublic") && warp.isPublished()) {
			plugin.message(sender, "info-noPermissionPublic");
			return;
		}
		if(!isSelf && !sender.hasPermission("playerwarps.infoprivate") && !warp.isPublished()) {
			plugin.message(sender, "info-noPermissionPrivate");
			return;
		}

		plugin.message(sender, "info-header", warp.getName(), warp.getPlayer().getName());
		Location l = warp.getLocation();
		plugin.messageNoPrefix(sender, "info-location", l.getBlockX(), l.getBlockY(), l.getBlockZ());
		plugin.messageNoPrefix(sender, "info-rotation", l.getWorld().getName(), Math.round(l.getPitch()), Math.round(l.getYaw()));
		if(warp.isPublished()) {
			plugin.messageNoPrefix(sender, "info-accessPublic");
		} else {
			plugin.messageNoPrefix(sender, "info-accessPrivate");
		}
		if(warp.getTrustedPlayers().size() == 0) {
			plugin.messageNoPrefix(sender, "info-noTrusted");
		} else {
			String result = "";
			boolean first = true;
			for(UUID trusted : warp.getTrustedPlayers()) {
				OfflinePlayer p = Bukkit.getOfflinePlayer(trusted);
				if(p == null) {
					continue;
				}
				if(first) {
					result += p.getName();
					first = false;
				} else {
					result += ", "+p.getName();
				}
			}
			plugin.messageNoPrefix(sender, "info-trusted", result);
		}
	}

}










