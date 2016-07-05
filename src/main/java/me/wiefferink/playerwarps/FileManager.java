package me.wiefferink.playerwarps;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class FileManager {
	private PlayerWarps plugin;
	private ObjectInputStream input = null;
	private ObjectOutputStream output = null;
	private HashMap<String, HashMap<String, Warp>> warps = null;
	private String warpsPath = null;

	/**
	 * Constructor
	 * @param plugin The PlayerWarps plugin
	 */
	public FileManager(PlayerWarps plugin) {
		this.plugin = plugin;		
		
		/* Initialize files */
		warps = new HashMap<>();
		
		/* Initialize paths */
		warpsPath = plugin.getDataFolder().getPath() + File.separator + "warps";
	}

	/**
	 * Get the total amount of warps a player has
	 * @param playerName The name of the player
	 * @return The total number of warps the player has
	 */
	public int getCurrentTotalWarps(String playerName) {
		playerName = playerName.toLowerCase();
		int result = 0;
		HashMap<String, Warp> playerWarps = warps.get(playerName);
		if (playerWarps != null) {
			result = playerWarps.keySet().size();
		}
		return result;
	}

	/**
	 * Get the amount of private warps a player has
	 * @param playerName The name of the player
	 * @return The number of private warps the player has
	 */
	public int getCurrentPrivateWarps(String playerName) {
		playerName = playerName.toLowerCase();
		int result = 0;
		HashMap<String, Warp> playerWarps = warps.get(playerName);
		if (playerWarps != null) {
			for (String warp : playerWarps.keySet()) {
				if (playerWarps.get(warp) != null && !playerWarps.get(warp).isPublished()) {
					result++;
				}
			}
		}
		return result;
	}

	/**
	 * Get the amount of public warps a player has
	 * @param playerName The name of the player
	 * @return The number of public warps the player has
	 */
	public int getCurrentPublicWarps(String playerName) {
		playerName = playerName.toLowerCase();
		int result = 0;
		HashMap<String, Warp> playerWarps = warps.get(playerName);
		if (playerWarps != null) {
			for (String warp : playerWarps.keySet()) {
				if (playerWarps.get(warp) != null && playerWarps.get(warp).isPublished()) {
					result++;
				}
			}
		}
		return result;
	}

	/**
	 * Get the total number of warps the player can still set
	 * @param playerName Name of the player
	 * @return Integer.MAX_VALUE if unlimited, otherwise a number
	 */
	public int getPossibleTotalWarps(String playerName) {
		playerName = playerName.toLowerCase();
		int result = 0;

		Player player = Bukkit.getPlayer(playerName);
		if (player == null) {
			return 0;
		}

		Set<String> groups = plugin.config().getConfigurationSection("warpLimitGroups").getKeys(false);
		ArrayList<String> list = new ArrayList<>(groups);
		groups.remove("unlimited");
		for (String group : groups) {
			if (player.hasPermission("playerwarps.limits." + group)) {
				int w = plugin.config().getInt("warpLimitGroups." + group + ".total");
				if (w > result) {
					result = w;
				}
			}
		}
		if (player.hasPermission("playerwarps.limits.unlimited")) {
			result = Integer.MAX_VALUE;
		}

		return result;
	}

	/**
	 * Get the number of private warps the player can still set
	 * @param playerName Name of the player
	 * @return Integer.MAX_VALUE if unlimited, otherwise a number
	 */
	public int getPossiblePrivateWarps(String playerName) {
		playerName = playerName.toLowerCase();
		int result = 0;

		Player player = Bukkit.getPlayer(playerName);
		if (player == null) {
			return 0;
		}

		Set<String> groups = plugin.config().getConfigurationSection("warpLimitGroups").getKeys(false);
		groups.remove("unlimited");
		for (String group : groups) {
			if (player.hasPermission("playerwarps.limits." + group)) {
				int w = plugin.config().getInt("warpLimitGroups." + group + ".private");
				if (w > result) {
					result = w;
				}
			}
		}
		if (player.hasPermission("playerwarps.limits.unlimited")) {
			result = Integer.MAX_VALUE;
		}

		return result;
	}

	/**
	 * Get the number of public warps the player can still set
	 * @param playerName Name of the player
	 * @return Integer.MAX_VALUE if unlimited, otherwise a number
	 */
	public int getPossiblePublicWarps(String playerName) {
		playerName = playerName.toLowerCase();
		int result = 0;

		Player player = Bukkit.getPlayer(playerName);
		if (player == null) {
			return 0;
		}

		Set<String> groups = plugin.config().getConfigurationSection("warpLimitGroups").getKeys(false);
		groups.remove("unlimited");
		for (String group : groups) {
			if (player.hasPermission("playerwarps.limits." + group)) {
				int w = plugin.config().getInt("warpLimitGroups." + group + ".public");
				if (w > result) {
					result = w;
				}
			}
		}
		if (player.hasPermission("playerwarps.limits.unlimited")) {
			result = Integer.MAX_VALUE;
		}
		return result;
	}


	/**
	 * Load all data from the files
	 * @return true if it succeeded, otherwise false
	 */
	@SuppressWarnings("unchecked")
	public boolean loadFiles() {
		boolean error = false;
		warps.clear();
		File file = new File(warpsPath);
		if (file.exists()) {
			/* Load all rents from file */
			try {
				input = new ObjectInputStream(new FileInputStream(warpsPath));
				warps = (HashMap<String, HashMap<String, Warp>>) input.readObject();
				input.close();
			} catch (IOException | ClassNotFoundException e) {
				plugin.getLogger().info("Error: Something went wrong reading file: " + warpsPath);
				e.printStackTrace();
				error = true;
			}

			if (!error) {
				Set<String> groups = plugin.config().getConfigurationSection("warpLimitGroups").getKeys(false);
				for (String group : groups) {
					if (!"unlimited".equals(group) && !"default".equals(group)) {
						Permission perm = new Permission("playerwarps.limits." + group);
						Bukkit.getPluginManager().addPermission(perm);
					}
				}
				Bukkit.getPluginManager().recalculatePermissionDefaults(Bukkit.getPluginManager().getPermission("playerwarps.limits"));
				
				/* Output info to console */
				if (warps.keySet().size() == 1) {
					plugin.debug("Warps of " + warps.keySet().size() + " player loaded");
				} else {
					plugin.debug("Warps of " + warps.keySet().size() + " players loaded");
				}
			}
		} else {
			this.saveFiles();
			plugin.getLogger().info("New file for the warps created, should only happen when starting for the first time");
		}
		return !error;
	}

	/**
	 * Save all files to disk
	 */
	public void saveFiles() {
		try {
			output = new ObjectOutputStream(new FileOutputStream(warpsPath));
			output.writeObject(warps);
			output.close();
		} catch (IOException e) {
			plugin.getLogger().info("File could not be saved: " + warpsPath);
		}

	}

	/**
	 * Get player warps from the list
	 * @param playerName Name of the player you want to get the warps from
	 * @return The Map with all the values from the player
	 */
	public HashMap<String, Warp> getPlayerWarps(String playerName) {
		playerName = playerName.toLowerCase();
		return warps.get(playerName);
	}

	/**
	 * Get all info about playerwarps
	 * @return The Map with all the values from the players
	 */
	public HashMap<String, HashMap<String, Warp>> getAllPlayerWarps() {
		return warps;
	}

	/**
	 * Add player warps to the map
	 * @param playerName Name of the player to be added
	 * @param playerWarps Map containing all the warps for a player
	 */
	public void addPlayerWarps(String playerName, HashMap<String, Warp> playerWarps) {
		playerName = playerName.toLowerCase();
		warps.put(playerName, playerWarps);
		this.saveFiles();
	}

	/**
	 * Add player warp to the map
	 * @param playerName Name of the player to be added
	 * @param warp The warp that should be added
	 * @return true if a warp has been overridden, false otherwise
	 */
	public boolean addPlayerWarp(String playerName, Warp warp) {
		playerName = playerName.toLowerCase();

		boolean result;
		HashMap<String, Warp> playerWarps = this.getPlayerWarps(playerName);
		if (playerWarps == null) {
			playerWarps = new HashMap<>();
		}
		result = playerWarps.get(warp.getName().toLowerCase()) != null;
		playerWarps.put(warp.getName().toLowerCase(), warp);
		this.addPlayerWarps(playerName, playerWarps);
		this.saveFiles();
		return result;
	}


	/**
	 * Remove a player from the list
	 * @param playerName Name of the player
	 */
	public boolean removePlayerWarps(String playerName) {
		playerName = playerName.toLowerCase();

		boolean result = warps.remove(playerName) != null;
		this.saveFiles();
		return result;
	}

	/**
	 * Remove a warp from a player from the list
	 * @param playerName Name of the player
	 */
	public boolean removePlayerWarp(String playerName, String warpName) {
		playerName = playerName.toLowerCase();
		warpName = warpName.toLowerCase();

		boolean result = false;
		HashMap<String, Warp> playerWarps = warps.get(playerName);
		if (playerWarps != null) {
			result = playerWarps.remove(warpName) != null;
			if (playerWarps.size() == 0) {
				this.removePlayerWarps(playerName);
			}
		}
		if (result) {
			this.saveFiles();
		}
		return result;
	}

}






















































