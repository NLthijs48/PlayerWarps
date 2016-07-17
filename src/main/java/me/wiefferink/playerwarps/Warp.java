package me.wiefferink.playerwarps;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Warp {
	private UUID player;
	private ConfigurationSection details;

	/* CONFIG LAYOUT
		<name_lower>:
		  name: <name>
		  location:
			x: <double>
			y: <double>
			z: <double>
			world: <string>
			pitch: <double>
			yaw: <double>
		  trusted: <list>
		  published: <boolean>
	 */

	/**
	 * Constructor
	 * @param player  Name of the owning player
	 */
	public Warp(UUID player) {
		this(player, null);
	}

	/**
	 * Constructor
	 * @param player Name of the owning player
	 * @param details The data of the warp
	 */
	public Warp(UUID player, ConfigurationSection details) {
		this.player = player;
		this.details = details;
		if(this.details == null) {
			this.details = new YamlConfiguration();
		}
	}

	/**
	 * Get the stored data for this warp
	 * @return The stored data
	 */
	public ConfigurationSection getDetails() {
		return details;
	}

	/**
	 * Get the location from the warp
	 * @return Location from the warp
	 */
	public Location getLocation() {
		return Utils.configToLocation(details.getConfigurationSection("location"));
	}

	/**
	 * Set the location of the warp
	 * @param location The location of the warp
	 */
	public void setLocation(Location location) {
		details.set("location", Utils.locationToConfig(location, true));
	}

	/**
	 * Check if the warp has been published
	 * @return true if published, false otherwise
	 */
	public boolean isPublished() {
		return details.getBoolean("published");
	}

	/**
	 * Set the published state of the warp
	 * @param published true to set to public, false to set to private
	 */
	public void setPublished(boolean published) {
		details.set("published", published);
	}

	/**
	 * Add a trusted player
	 * @param player Player to add
	 */
	public void addTrustedPlayer(UUID player) {
		List<String> trusted = details.getStringList("trusted");
		trusted.add(player.toString());
		details.set("trusted", trusted);
	}

	/**
	 * Remove a trusted player
	 * @param player Player to remove
	 */
	public void removeTrustedPlayer(UUID player) {
		List<String> trusted = details.getStringList("trusted");
		trusted.remove(player.toString());
		details.set("trusted", trusted);
	}

	/**
	 * Get the trusted players, sorted by name
	 * @return Set with the trusted players
	 */
	public Set<UUID> getTrustedPlayers() {
		List<String> trustedPlayers = details.getStringList("trusted");
		Set<UUID> result = new HashSet<>();
		for(String player : trustedPlayers) {
			try {
				UUID id = UUID.fromString(player);
				result.add(id);
			} catch(IllegalArgumentException ignored) {
			}
		}
		return result;
	}

	/**
	 * Check if a player is trusted for this warp
	 * @param player Player to check
	 * @return true if the player is trusted, otherwise false
	 */
	public boolean isTrusted(UUID player) {
		return getTrustedPlayers().contains(player);
	}

	/**
	 * Get the the player who owns this warp
	 * @return The player
	 */
	public OfflinePlayer getPlayer() {
		return Bukkit.getOfflinePlayer(player);
	}

	/**
	 * Get the name of the warp in the correct casing
	 * @return The name of the warp
	 */
	public String getName() {
		return details.getString("name");
	}

	/**
	 * Set the name of the warp
	 * @param name The name of the warp
	 */
	public void setName(String name) {
		details.set("name", name);
	}

	@Override
	public String toString() {
		String result = "warp(";
		result += "name="+getName();
		result += ", player="+getPlayer().getName();
		result += ", published="+isPublished();
		result += ", location="+getLocation().toString();
		result += ", trusted="+getTrustedPlayers().toString();
		result += ")";
		return result;
	}

}




















