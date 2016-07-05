package me.wiefferink.playerwarps;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.Serializable;
import java.util.TreeSet;

public class Warp implements Serializable {
	/* Class version number */
	private static final long serialVersionUID = 1L;
	/* Location */
	private double x, y, z;
	private float pitch, yaw;
	private String world;
	/* Other */
	private boolean published;
	private String player;
	private String name;
	private TreeSet<String> trusted;

	/**
	 * Constructor
	 * @param player Name of the owning player
	 * @param published Other people can use it or not
	 * @param world Name of the world
	 * @param x X
	 * @param y Y
	 * @param z Z
	 * @param pitch Pitch
	 * @param yaw Yaw
	 */
	public Warp(String name, String player, boolean published, String world, double x, double y, double z, float pitch, float yaw) {
		this.player = player;
		this.published = published;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.pitch = pitch;
		this.yaw = yaw;
		this.name = name;
		trusted = new TreeSet<>();
	}

	/**
	 * Alternative constructor
	 * @param name Name of the warp
	 * @param player Name of the owning player
	 * @param published Other people can use it or not
	 * @param location Location
	 */
	public Warp(String name, String player, boolean published, Location location) {
		this(name, player, published, location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw());
	}

	/**
	 * Get the location from the warp
	 * @return Location from the warp
	 */
	public Location getLocation() {
		World realWorld = Bukkit.getWorld(world);
		return new Location(realWorld, x, y, z, yaw, pitch);
	}

	/**
	 * Getters for the location variables
	 */
	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	public float getYaw() {
		return yaw;
	}

	public float getPitch() {
		return pitch;
	}

	public String getWorld() {
		return world;
	}

	/**
	 * Check if the warp has been published
	 * @return true if published, false otherwise
	 */
	public boolean isPublished() {
		return published;
	}

	/**
	 * Add a trusted player
	 * @param name Player to add
	 */
	public void addTrustedPlayer(String name) {
		trusted.add(name.toLowerCase());
	}

	/**
	 * Remove a trusted player
	 * @param name Player to remove
	 */
	public void removeTrustedPlayer(String name) {
		trusted.remove(name.toLowerCase());
	}

	/**
	 * Get the trusted players, sorted by name
	 * @return Set with the trusted players
	 */
	public TreeSet<String> getTrustedPlayers() {
		return trusted;
	}

	/**
	 * Check if a player is trusted for this warp
	 * @param name Player to check
	 * @return true if the player is trusted, otherwise false
	 */
	public boolean isTrusted(String name) {
		return trusted.contains(name.toLowerCase());
	}

	/**
	 * Get the name of the player who owns this warp
	 * @return The players name
	 */
	public String getPlayerName() {
		return player;
	}

	/**
	 * Get the name of the warp in the correct casing
	 * @return The name of the warp
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		String result = "warp(";
		result += "name=" + name;
		result += ", player=" + player;
		result += ", published=" + published;
		result += ", world=" + world;
		result += ", x=" + x;
		result += ", y=" + y;
		result += ", z=" + z;
		result += ", pitch=" + pitch;
		result += ", yaw=" + yaw;
		result += ", trusted=" + trusted.toString();
		result += ")";
		return result;
	}


}




















