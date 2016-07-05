package me.wiefferink.playerwarps;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public class CommandManager implements CommandExecutor {
	PlayerWarps plugin;

	public CommandManager(PlayerWarps plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		List<String> newArgs = new ArrayList<>();

		// Alias '/home [name]' to '/warp to [name]'
		if (command.getName().equalsIgnoreCase("home")) {
			newArgs.add("to");
		}
		// Alias  '/sethome [name]' to '/warp add [name]'
		else if (command.getName().equalsIgnoreCase("sethome")) {
			newArgs.add("add");
		}
		// Alias  '/sethome [name]' to '/warp add [name]'
		else if (command.getName().equalsIgnoreCase("delhome")) {
			newArgs.add("del");
		}

		if (!newArgs.isEmpty()) {
			if (args.length > 0) { // Provided a name
				newArgs.add(args[0]);
			} else { // Fallback to 'home'
				newArgs.add("home");
			}
			args = newArgs.toArray(new String[newArgs.size()]);
		}


		FileManager fileManager = plugin.getFileManager();


		// DISPLAY HELP
		if (args.length == 0) {
			plugin.showHelp(sender);
			return true;
		}


		// GO TO A WARP
		if ("to".equalsIgnoreCase(args[0])) {
			// Check if it is a player
			if (!(sender instanceof Player)) {
				plugin.message(sender, "cmd-onlyByPlayer");
				return true;
			}
			Player player = (Player) sender;
			String playerName = player.getName();

			// check permission
			if (!player.hasPermission("playerwarps.to")) {
				plugin.message(player, "to-noPermission");
				return true;
			}

			if (args.length == 1) {
				if (sender.hasPermission("playerwarps.topublic") || sender.hasPermission("playerwarps.toprivate")) {
					plugin.message(sender, "to-helpOther");
				} else {
					plugin.message(sender, "to-help");
				}
			} else if (args.length == 2) {
				HashMap<String, Warp> playerWarps = fileManager.getPlayerWarps(playerName);
				if (playerWarps == null) {
					plugin.message(player, "to-hasNoWarps");
					return true;
				}

				Warp warp = playerWarps.get(args[1].toLowerCase());
				if (warp == null) {
					plugin.message(player, "to-wrongWarp", args[1]);
					return true;
				}

				boolean warpResult = player.teleport(warp.getLocation());
				if (!warpResult) {
					plugin.message(player, "to-failed");
					return true;
				}
				plugin.message(player, "to-warped", warp.getName());
			} else {
				HashMap<String, Warp> playerWarps = fileManager.getPlayerWarps(args[2]);
				if (playerWarps == null) {
					plugin.message(player, "to-noWarpsOther", args[2]);
					return true;
				}

				Warp warp = playerWarps.get(args[1].toLowerCase());
				if (warp == null) {
					plugin.message(player, "to-wrongWarpOther", args[2], args[1]);
					return true;
				}

				// Permisssions
				if (warp.isPublished() && !player.hasPermission("playerwarps.topublic") && !warp.isTrusted(player.getName())) {
					plugin.message(player, "to-noPermissionPublic");
					return true;
				}
				if (!warp.isPublished() && !player.hasPermission("playerwarps.toprivate") && !warp.isTrusted(player.getName())) {
					plugin.message(player, "to-noPermissionPrivate");
					return true;
				}

				boolean warpResult = player.teleport(warp.getLocation());
				if (!warpResult) {
					plugin.message(player, "to-failed");
					return false;
				}
				plugin.message(player, "to-warpedOther", warp.getName(), warp.getPlayerName());

			}
		}


		// ADDING A WARP
		else if ("add".equalsIgnoreCase(args[0])) {
			// Check if it is a player
			if (!(sender instanceof Player)) {
				plugin.message(sender, "cmd-onlyByPlayer");
				return true;
			}
			Player player = (Player) sender;
			String playerName = player.getName();

			// Check permission
			if (!player.hasPermission("playerwarps.add")) {
				plugin.message(player, "add-noPermission");
				return true;
			}

			// Check the name
			if (args.length < 2) {
				if (player.hasPermission("playerwarps.addother")) {
					plugin.message(player, "add-wrongArgsOther");
				} else {
					plugin.message(player, "add-wrongArgs");
				}
				return true;
			}

			// Published or not?
			boolean published = false;
			String pub = plugin.config().getString("defaultAccess");
			if (args.length >= 3) {
				pub = args[2];
				if ("public".equalsIgnoreCase(pub)) {
					published = true;
				} else if ("private".equalsIgnoreCase(pub)) {
					published = false;
				} else {
					plugin.message(player, "add-wrongAccess");
					return true;
				}
			} else {
				if ("public".equalsIgnoreCase(pub)) {
					published = true;
				}
			}

			// Set the playerName to the given player if
			if (args.length >= 4) {
				if (!player.hasPermission("playerwarps.addother")) {
					plugin.message(player, "add-wrongArgs");
					return true;
				}
				playerName = args[3];
			}

			// Check max amount
			if (!(args.length >= 4) || !plugin.config().getBoolean("addwarpOtherLimitBypas")) {
				int currentTotal = fileManager.getCurrentTotalWarps(playerName);
				int possibleTotal = fileManager.getPossibleTotalWarps(playerName);
				int currentPrivate = fileManager.getCurrentPrivateWarps(playerName);
				int possiblePrivate = fileManager.getPossiblePrivateWarps(playerName);
				int currentPublic = fileManager.getCurrentPublicWarps(playerName);
				int possiblePublic = fileManager.getPossiblePublicWarps(playerName);
				if ((possibleTotal - currentTotal) <= 0) {
					plugin.message(player, "add-totalMax", currentTotal, possibleTotal);
				} else if (!published && (possiblePrivate - currentPrivate) <= 0) {
					plugin.message(player, "add-privateMax", currentPrivate, possiblePrivate);
				} else if (published && (possiblePublic - currentPublic) <= 0) {
					plugin.message(player, "add-publicMax", currentPublic, possiblePublic);
				} else {
					Warp warp = new Warp(args[1], playerName, published, player.getLocation());
					boolean overridden = fileManager.addPlayerWarp(playerName, warp);
					if (overridden) {
						plugin.message(player, "add-overridden", args[1]);
					} else {
						plugin.message(player, "add-added", args[1]);
					}
				}
			} else {
				Warp warp = new Warp(args[1], playerName, published, player.getLocation());
				boolean overridden = fileManager.addPlayerWarp(playerName, warp);
				if (overridden) {
					plugin.message(player, "add-overriddenOther", args[1], playerName);
				} else {
					plugin.message(player, "add-addedOther", args[1], playerName);
				}
			}

		}


		// DELETING A WARP
		else if ("del".equalsIgnoreCase(args[0])) {
			// Check permission
			if (!sender.hasPermission("playerwarps.del")) {
				plugin.message(sender, "del-noPermission");
				return true;
			}

			// Check the name
			if (args.length < 2) {
				if (sender.hasPermission("playerwarps.delother")) {
					plugin.message(sender, "del-wrongArgsOther");
				} else {
					plugin.message(sender, "del-wrongArgs");
				}
				return true;
			} else if (args.length == 2) {
				// Check if it is a player
				if (!(sender instanceof Player)) {
					plugin.message(sender, "cmd-onlyByPlayer");
					return true;
				}
				Player player = (Player) sender;
				String playerName = player.getName();

				if (fileManager.getPlayerWarps(playerName) == null) {
					plugin.message(sender, "del-wrongWarp", args[1]);
				} else {
					Warp warp = fileManager.getPlayerWarps(playerName).get(args[1]);
					if (fileManager.removePlayerWarp(playerName, args[1])) {
						plugin.message(sender, "del-deleted", warp.getName());
					} else {
						plugin.message(sender, "del-wrongWarp", args[1]);
					}
				}
			} else {
				// Check permission
				if (!sender.hasPermission("playerwarps.delother")) {
					plugin.message(sender, "del-noPermission");
					return true;
				}

				if (fileManager.getPlayerWarps(args[2]) == null) {
					plugin.message(sender, "del-wrongWarp", args[1]);
				} else {
					Warp warp = fileManager.getPlayerWarps(args[2]).get(args[1]);
					if (fileManager.removePlayerWarp(args[2], args[1])) {
						plugin.message(sender, "del-deletedOther", warp.getName(), args[2]);
					} else {
						plugin.message(sender, "del-wrongWarpOther", args[2], args[1]);
					}
				}
			}
		}


		// LIST PLAYER WARPS
		else if ("list".equalsIgnoreCase(args[0])) {
			String playerName;
			if (args.length == 1) {
				// Check if it is a player
				if (!(sender instanceof Player)) {
					plugin.message(sender, "cmd-onlyByPlayer");
					return true;
				}
				Player player = (Player) sender;
				playerName = player.getName();

				// check permission
				if (!player.hasPermission("playerwarps.list")) {
					plugin.message(player, "list-noPermission");
					return true;
				}
			} else {
				// check permission
				if (!sender.hasPermission("playerwarps.listOther")) {
					plugin.message(sender, "list-noPermissionOther");
					return true;
				}
				playerName = args[1];
			}

			HashMap<String, Warp> playerWarps = fileManager.getPlayerWarps(playerName);
			if (playerWarps == null) {
				plugin.message(sender, "list-hasNoWarps");
			} else {
				plugin.message(sender, "list-header", playerName);
				TreeSet<String> sorted = new TreeSet<>(playerWarps.keySet());
				// Public part
				if (fileManager.getCurrentPublicWarps(playerName) == 0) {
					sender.sendMessage(plugin.fixColors(plugin.getLanguageManager().getLang("list-noPublic")));
				} else {
					String msg = "";
					boolean first = true;
					for (String warpName : sorted) {
						if (playerWarps.get(warpName).isPublished()) {
							if (first) {
								msg += playerWarps.get(warpName).getName();
								first = false;
							} else {
								msg += ", " + playerWarps.get(warpName).getName();
							}
						}
					}
					sender.sendMessage(plugin.fixColors(plugin.getLanguageManager().getLang("list-public", msg)));
				}
				// Private part
				if (fileManager.getCurrentPrivateWarps(playerName) == 0) {
					sender.sendMessage(plugin.fixColors(plugin.getLanguageManager().getLang("list-noPrivate")));
				} else {
					String msg = "";
					boolean first = true;
					for (String warpName : sorted) {
						if (!playerWarps.get(warpName).isPublished()) {
							if (first) {
								msg += playerWarps.get(warpName).getName();
								first = false;
							} else {
								msg += ", " + playerWarps.get(warpName).getName();
							}
						}
					}
					sender.sendMessage(plugin.fixColors(plugin.getLanguageManager().getLang("list-private", msg)));
				}
			}
		}


		// LIST PUBLIC WARPS
		else if ("public".equalsIgnoreCase(args[0])) {
			// check permission
			if (!sender.hasPermission("playerwarps.public")) {
				plugin.message(sender, "public-noPermission");
				return true;
			}

			if (fileManager.getAllPlayerWarps().size() == 0) {
				plugin.message(sender, "public-noWarps");
			} else {
				if (args.length == 1) {
					plugin.message(sender, "public-header");
					TreeSet<String> sorted = new TreeSet<>(fileManager.getAllPlayerWarps().keySet());
					for (String warpPlayer : sorted) {
						HashMap<String, Warp> playerWarps = fileManager.getPlayerWarps(warpPlayer);

						TreeSet<String> sorted2 = new TreeSet<>(playerWarps.keySet());
						// Add all warps to a string
						if (!(fileManager.getCurrentTotalWarps(warpPlayer) == 0)) {
							String warps = "";
							String realName = "";
							boolean first = true;
							for (String warpName : sorted2) {
								Warp warp = playerWarps.get(warpName);
								if (warp.isPublished()) {
									if (first) {
										warps += warp.getName();
										realName = warp.getPlayerName();
										first = false;
									} else {
										warps += ", " + warp.getName();
									}
								} else if (warp.getTrustedPlayers().contains(sender.getName().toLowerCase())) {
									if (first) {
										warps += ChatColor.GREEN + warp.getName() + ChatColor.RESET;
										realName = warp.getPlayerName();
										first = false;
									} else {
										warps += ", " + warp.getName();
									}
								}
							}
							if (warps.length() != 0) {
								sender.sendMessage(plugin.fixColors(plugin.getLanguageManager().getLang("public-entry", realName, warps)));
							}
						}
					}
				} else {
					// List public warps from another player
					if (fileManager.getCurrentTotalWarps(args[1]) == 0) {
						plugin.message(sender, "public-noWarpsPlayer", args[1]);
					} else {
						String warps = "";
						HashMap<String, Warp> playerWarps = fileManager.getPlayerWarps(args[1]);
						TreeSet<String> sorted = new TreeSet<>(playerWarps.keySet());
						String realName = "";

						boolean first = true;
						for (String warpName : sorted) {
							Warp warp = playerWarps.get(warpName);
							if (warp.isPublished()) {
								if (first) {
									warps += warp.getName();
									realName = warp.getPlayerName();
									first = false;
								} else {
									warps += ", " + warp.getName();
								}
							} else if (warp.getTrustedPlayers().contains(sender.getName().toLowerCase())) {
								if (first) {
									warps += ChatColor.GREEN + warp.getName() + ChatColor.RESET;
									realName = warp.getPlayerName();
									first = false;
								} else {
									warps += ", " + warp.getName();
								}
							}
						}
						if (warps.length() != 0) {
							plugin.message(sender, "public-player", realName, warps);
						} else {
							plugin.message(sender, "public-noWarpsPlayer", args[1]);
						}
					}
				}
			}
		}


		// TRUST A PLAYER FOR A WARP
		else if ("trust".equalsIgnoreCase(args[0])) {
			// Check permission
			if (!sender.hasPermission("playerwarps.trust")) {
				plugin.message(sender, "trust-noPermission");
				return true;
			}

			// Check the name
			if (args.length < 3) {
				if (sender.hasPermission("playerwarps.trustother")) {
					plugin.message(sender, "trust-wrongArgsOther");
				} else {
					plugin.message(sender, "trust-wrongArgs");
				}
				return true;
			} else if (args.length == 3) {
				// Check if it is a player
				if (!(sender instanceof Player)) {
					plugin.message(sender, "cmd-onlyByPlayer");
					return true;
				}
				Player player = (Player) sender;
				String playerName = player.getName();

				HashMap<String, Warp> playerWarps = fileManager.getPlayerWarps(playerName);
				if (playerWarps == null) {
					plugin.message(sender, "trust-noWarps", args[2]);
					return true;
				}
				Warp warp = playerWarps.get(args[2].toLowerCase());
				if (warp == null) {
					plugin.message(sender, "trust-noWarps", args[2]);
					return true;
				}
				if (warp.isTrusted(args[1])) {
					plugin.message(player, "trust-alreadyTrusted", args[1], warp.getName());
				} else {
					warp.addTrustedPlayer(args[1]);
					plugin.message(player, "trust-success", args[1], warp.getName());
					plugin.getFileManager().saveFiles();
				}
			} else {
				// Check permission
				if (!sender.hasPermission("playerwarps.trustother")) {
					plugin.message(sender, "trust-noPermissionOther");
					return true;
				}

				HashMap<String, Warp> playerWarps = fileManager.getPlayerWarps(args[3]);
				if (playerWarps == null) {
					plugin.message(sender, "trust-noWarpsOther", args[3], args[2]);
					return true;
				}

				Warp warp = playerWarps.get(args[2].toLowerCase());
				if (warp == null) {
					plugin.message(sender, "trust-noWarpsOther", args[3], args[2]);
					return true;
				}
				if (warp.isTrusted(args[1])) {
					plugin.message(sender, "trust-alreadyTrustedOther", args[1], warp.getName(), warp.getPlayerName());
				} else {
					warp.addTrustedPlayer(args[1]);
					plugin.message(sender, "trust-successOther", args[1], warp.getName(), warp.getPlayerName());
					plugin.getFileManager().saveFiles();
				}
			}
		}


		// UNTRUST A PLAYER FOR A WARP
		else if ("untrust".equalsIgnoreCase(args[0])) {
			// Check permission
			if (!sender.hasPermission("playerwarps.untrust")) {
				plugin.message(sender, "untrust-noPermission");
				return true;
			}

			// Check the name
			if (args.length < 3) {
				if (sender.hasPermission("playerwarps.untrustother")) {
					plugin.message(sender, "untrust-wrongArgsOther");
				} else {
					plugin.message(sender, "untrust-wrongArgs");
				}
				return true;
			} else if (args.length == 3) {
				// Check if it is a player
				if (!(sender instanceof Player)) {
					plugin.message(sender, "cmd-onlyByPlayer");
					return true;
				}
				Player player = (Player) sender;
				String playerName = player.getName();

				HashMap<String, Warp> playerWarps = fileManager.getPlayerWarps(playerName);
				if (playerWarps == null) {
					plugin.message(sender, "untrust-noWarps", args[2]);
					return true;
				}
				Warp warp = playerWarps.get(args[2].toLowerCase());
				if (warp == null) {
					plugin.message(sender, "untrust-noWarps", args[2]);
					return true;
				}
				if (!warp.isTrusted(args[1])) {
					plugin.message(player, "untrust-notTrusted", args[1], warp.getName());
				} else {
					warp.removeTrustedPlayer(args[1]);
					plugin.message(player, "untrust-success", args[1], warp.getName());
					plugin.getFileManager().saveFiles();
				}
			} else {
				// Check permission
				if (!sender.hasPermission("playerwarps.untrustother")) {
					plugin.message(sender, "untrust-noPermissionOther");
					return true;
				}

				HashMap<String, Warp> playerWarps = fileManager.getPlayerWarps(args[3]);
				if (playerWarps == null) {
					plugin.message(sender, "untrust-noWarpsOther", args[3], args[2]);
					return true;
				}

				Warp warp = playerWarps.get(args[2].toLowerCase());
				if (warp == null) {
					plugin.message(sender, "untrust-noWarpsOther", args[3], args[2]);
					return true;
				}
				if (!warp.isTrusted(args[1])) {
					plugin.message(sender, "untrust-notTrustedOther", args[1], warp.getName(), warp.getPlayerName());
				} else {
					warp.removeTrustedPlayer(args[1]);
					plugin.message(sender, "untrust-successOther", args[1], warp.getName(), warp.getPlayerName());
					plugin.getFileManager().saveFiles();
				}
			}
		}


		// LIST WARP INFO
		else if ("info".equalsIgnoreCase(args[0])) {
			// check permission
			if (!sender.hasPermission("playerwarps.info")) {
				plugin.message(sender, "info-noPermission");
				return true;
			}

			// check arguments
			if (args.length == 1) {
				plugin.message(sender, "info-wrongArgs");
				return true;
			} else if (args.length == 2 && !(sender instanceof Player)) {
				plugin.message(sender, "info-specifyPlayer");
				return true;
			}

			String playerName = sender.getName();
			if (args.length > 2) {
				playerName = args[2];
			}

			HashMap<String, Warp> playerWarps = fileManager.getPlayerWarps(playerName);
			if (playerWarps == null) {
				plugin.message(sender, "info-wrongWarp", playerName, args[1]);
				return true;
			}

			Warp warp = playerWarps.get(args[1].toLowerCase());
			if (warp == null) {
				plugin.message(sender, "info-wrongWarp", playerName, args[1]);
				return true;
			}

			if (!playerName.equalsIgnoreCase(sender.getName()) && !sender.hasPermission("playerwarps.infopublic") && warp.isPublished()) {
				plugin.message(sender, "info-noPermissionPublic");
				return true;
			}
			if (!playerName.equalsIgnoreCase(sender.getName()) && !sender.hasPermission("playerwarps.infoprivate") && !warp.isPublished()) {
				plugin.message(sender, "info-noPermissionPrivate");
				return true;
			}

			plugin.message(sender, "info-header", warp.getName(), warp.getPlayerName());
			sender.sendMessage(plugin.fixColors(plugin.getLanguageManager().getLang("info-location", warp.getX(), warp.getY(), warp.getZ())));
			sender.sendMessage(plugin.fixColors(plugin.getLanguageManager().getLang("info-rotation", warp.getWorld(), warp.getPitch(), warp.getYaw())));
			if (warp.isPublished()) {
				sender.sendMessage(plugin.fixColors(plugin.getLanguageManager().getLang("info-accessPublic")));
			} else {
				sender.sendMessage(plugin.fixColors(plugin.getLanguageManager().getLang("info-accessPrivate")));
			}
			if (warp.getTrustedPlayers().size() == 0) {
				sender.sendMessage(plugin.fixColors(plugin.getLanguageManager().getLang("info-noTrusted")));
			} else {
				String result = "";
				boolean first = true;
				for (String trustName : warp.getTrustedPlayers()) {
					if (first) {
						result += trustName;
						first = false;
					} else {
						result += ", " + trustName;
					}
				}
				sender.sendMessage(plugin.fixColors(plugin.getLanguageManager().getLang("info-trusted", result)));
			}
		}


		// NOT A VALID COMMAND
		else {
			plugin.showHelp(sender);
		}

		return true;
	}

}




































