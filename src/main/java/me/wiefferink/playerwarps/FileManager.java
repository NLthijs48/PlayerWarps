package me.wiefferink.playerwarps;

import com.google.common.base.Charsets;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FileManager {
	private PlayerWarps plugin;
	private Map<UUID, Map<String, Warp>> warps = null;
	private YamlConfiguration warpsFile;
	private File warpsPath;

	private boolean dirty = false;

	/**
	 * Constructor
	 * @param plugin The PlayerWarps plugin
	 */
	public FileManager(PlayerWarps plugin) {
		this.plugin = plugin;		
		warps = new HashMap<>();
		warpsPath = new File(plugin.getDataFolder()+File.separator+"warps.yml");
	}

	/**
	 * Get the total amount of warps a player has
	 * @param player The player
	 * @return The total number of warps the player has
	 */
	public int getCurrentTotalWarps(UUID player) {
		int result = 0;
		Map<String, Warp> playerWarps = warps.get(player);
		if (playerWarps != null) {
			result = playerWarps.keySet().size();
		}
		return result;
	}

	/**
	 * Get the amount of private warps a player has
	 * @param player The player
	 * @return The number of private warps the player has
	 */
	public int getCurrentPrivateWarps(UUID player) {
		int result = 0;
		Map<String, Warp> playerWarps = warps.get(player);
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
	 * @param player The player
	 * @return The number of public warps the player has
	 */
	public int getCurrentPublicWarps(UUID player) {
		int result = 0;
		Map<String, Warp> playerWarps = warps.get(player);
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
	 * @param uPlayer The player
	 * @return Integer.MAX_VALUE if unlimited, otherwise a number
	 */
	public int getPossibleTotalWarps(UUID uPlayer) {
		int result = 0;

		Player player = Bukkit.getPlayer(uPlayer);
		if (player == null) {
			return 0;
		}

		Set<String> groups = plugin.getConfig().getConfigurationSection("warpLimitGroups").getKeys(false);
		ArrayList<String> list = new ArrayList<>(groups);
		groups.remove("unlimited");
		for (String group : groups) {
			if (player.hasPermission("playerwarps.limits." + group)) {
				int w = plugin.getConfig().getInt("warpLimitGroups." + group + ".total");
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
	 * @param uPlayer The player
	 * @return Integer.MAX_VALUE if unlimited, otherwise a number
	 */
	public int getPossiblePrivateWarps(UUID uPlayer) {
		int result = 0;

		Player player = Bukkit.getPlayer(uPlayer);
		if (player == null) {
			return 0;
		}

		Set<String> groups = plugin.getConfig().getConfigurationSection("warpLimitGroups").getKeys(false);
		groups.remove("unlimited");
		for (String group : groups) {
			if (player.hasPermission("playerwarps.limits." + group)) {
				int w = plugin.getConfig().getInt("warpLimitGroups." + group + ".private");
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
	 * @param uPlayer The player
	 * @return Integer.MAX_VALUE if unlimited, otherwise a number
	 */
	public int getPossiblePublicWarps(UUID uPlayer) {
		int result = 0;
		Player player = Bukkit.getPlayer(uPlayer);
		if (player == null) {
			return 0;
		}

		Set<String> groups = plugin.getConfig().getConfigurationSection("warpLimitGroups").getKeys(false);
		groups.remove("unlimited");
		for (String group : groups) {
			if (player.hasPermission("playerwarps.limits." + group)) {
				int w = plugin.getConfig().getInt("warpLimitGroups." + group + ".public");
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
	 * Load the warps.yml file from disk
	 * @return true if succeeded, otherwise false
	 */
	public boolean loadWarps() {
		if(warpsPath.exists() && warpsPath.isFile()) {
			try(
					InputStreamReader reader = new InputStreamReader(new FileInputStream(warpsPath), Charsets.UTF_8)
			) {
				warpsFile = YamlConfiguration.loadConfiguration(reader);
			} catch(IOException e) {
				plugin.getLogger().warning("Could not load warps.yml file: "+warpsPath.getAbsolutePath());
			}
		}
		if(warpsFile == null) {
			warpsFile = new YamlConfiguration();
		}
		// Build map with warps
		int warpCount = 0, playerCount = 0;
		for(String player : warpsFile.getKeys(false)) {
			ConfigurationSection playerSection = warpsFile.getConfigurationSection(player);
			if(playerSection.getKeys(false).size() == 0) {
				warpsFile.set(player, null);
				saveFiles();
				continue;
			}

			UUID uPlayer;
			try {
				uPlayer = UUID.fromString(player);
			} catch(IllegalArgumentException e) {
				continue;
			}
			Map<String, Warp> playerWarps = warps.get(uPlayer);
			if(playerWarps == null) {
				playerWarps = new HashMap<>();
				warps.put(uPlayer, playerWarps);
			}

			for(String warpName : playerSection.getKeys(false)) {
				Warp warp = new Warp(uPlayer, playerSection.getConfigurationSection(warpName));
				playerWarps.put(warpName, warp);
				warpCount++;
			}
			playerCount++;
		}
		PlayerWarps.debug(playerCount + " players with " + warpCount + " warps have been loaded");
		return true;
	}


	/**
	 * Save all files to disk
	 */
	public void saveFiles() {
		dirty = true;
	}

	public void saveFilesNow() {
		if(dirty) {
			dirty = false;
			for(UUID player : warps.keySet()) {
				Map<String, Warp> playerWarps = warps.get(player);
				if(playerWarps != null && player != null) {
					for(String warpName : playerWarps.keySet()) {
						Warp warp = playerWarps.get(warpName);
						if(warp != null && warpName != null) {
							warpsFile.set(player.toString()+"."+warpName, warp.getDetails());
						}
					}
				}
			}
			try {
				warpsFile.save(warpsPath);
			} catch(IOException e) {
				plugin.getLogger().warning("Could not save warps.yml file: "+warpsPath.getAbsolutePath());
				e.printStackTrace();
			}
		}
	}

	/**
	 * Get player warps from the list
	 * @param player Name of the player you want to get the warps from
	 * @return The Map with all the values from the player
	 */
	public Map<String, Warp> getPlayerWarps(UUID player) {
		return warps.get(player);
	}

	/**
	 * Get a warp from a player
	 * @param player The player to get the warp from
	 * @param name   The name of the warp to get
	 * @return The warp of the player, or null if there is none
	 */
	public Warp getWarp(UUID player, String name) {
		Warp result = null;
		Map<String, Warp> playerWarps = getPlayerWarps(player);
		if(playerWarps != null) {
			result = playerWarps.get(name.toLowerCase());
		}
		return result;
	}

	/**
	 * Get all info about playerwarps
	 * @return The Map with all the values from the players
	 */
	public Map<UUID, Map<String, Warp>> getAllPlayerWarps() {
		return warps;
	}

	/**
	 * Add player warp to the map
	 * @param player The player to be added
	 * @param warp The warp that should be added
	 */
	public void addPlayerWarp(UUID player, Warp warp) {
		Map<String, Warp> playerWarps = this.getPlayerWarps(player);
		if (playerWarps == null) {
			playerWarps = new HashMap<>();
			warps.put(player, playerWarps);
		}
		playerWarps.put(warp.getName().toLowerCase(), warp);
		this.saveFiles();
	}

	/**
	 * Remove a player from the list
	 * @param player The player
	 */
	public boolean removePlayerWarps(UUID player) {
		boolean result = warps.remove(player) != null;
		warpsFile.set(player.toString(), null);
		this.saveFiles();
		return result;
	}

	/**
	 * Remove a warp from a player from the list
	 * @param player The player
	 */
	public void removePlayerWarp(UUID player, String warpName) {
		warpName = warpName.toLowerCase();
		Map<String, Warp> playerWarps = warps.get(player);
		if (playerWarps != null) {
			playerWarps.remove(warpName);
			warpsFile.set(player.toString()+"."+warpName, null);
			if (playerWarps.size() == 0) {
				this.removePlayerWarps(player);
			}
		}
		this.saveFiles();
	}

}






















































