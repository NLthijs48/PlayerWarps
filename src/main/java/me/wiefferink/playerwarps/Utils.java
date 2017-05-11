package me.wiefferink.playerwarps;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Utils {

    private static ArrayList<Material> canSpawnIn = new ArrayList<>(Arrays.asList(Material.WOOD_DOOR, Material.WOODEN_DOOR, Material.SIGN_POST, Material.WALL_SIGN, Material.STONE_PLATE, Material.IRON_DOOR_BLOCK, Material.WOOD_PLATE, Material.TRAP_DOOR, Material.REDSTONE_LAMP_OFF, Material.REDSTONE_LAMP_ON, Material.DRAGON_EGG, Material.GOLD_PLATE, Material.IRON_PLATE));
    private static ArrayList<Material> cannotSpawnOn = new ArrayList<>(Arrays.asList(Material.PISTON_EXTENSION, Material.PISTON_MOVING_PIECE, Material.SIGN_POST, Material.WALL_SIGN, Material.STONE_PLATE, Material.IRON_DOOR_BLOCK, Material.WOOD_PLATE, Material.TRAP_DOOR, Material.REDSTONE_LAMP_OFF, Material.REDSTONE_LAMP_ON, Material.CACTUS, Material.IRON_FENCE, Material.FENCE_GATE, Material.THIN_GLASS, Material.NETHER_FENCE, Material.DRAGON_EGG, Material.GOLD_PLATE, Material.IRON_PLATE, Material.STAINED_GLASS_PANE, Material.FIRE));
    private static ArrayList<Material> cannotSpawnBeside = new ArrayList<>(Arrays.asList(Material.LAVA, Material.STATIONARY_LAVA, Material.CACTUS, Material.FIRE));

	/**
	 * Create a map from a location, to save it in the config
	 * @param location    The location to transform
	 * @param setPitchYaw true to save the pitch and yaw, otherwise false
	 * @return The map with the location values
	 */
	public static ConfigurationSection locationToConfig(Location location, boolean setPitchYaw) {
		if(location == null) {
			return null;
		}
		ConfigurationSection result = new YamlConfiguration();
		result.set("world", location.getWorld().getName());
		result.set("x", location.getX());
		result.set("y", location.getY());
		result.set("z", location.getZ());
		if(setPitchYaw) {
			result.set("yaw", Float.toString(location.getYaw()));
			result.set("pitch", Float.toString(location.getPitch()));
		}
		return result;
	}

	/**
	 * Create a map from a location, to save it in the config (without pitch and yaw)
	 * @param location The location to transform
	 * @return The map with the location values
	 */
	public static ConfigurationSection locationToConfig(Location location) {
		return locationToConfig(location, false);
	}

	/**
	 * Create a location from a map, reconstruction from the config values
	 * @param config The config section to reconstruct from
	 * @return The location
	 */
	public static Location configToLocation(ConfigurationSection config) {
		if(config == null
				|| !config.isString("world")
				|| !config.isDouble("x")
				|| !config.isDouble("y")
				|| !config.isDouble("z")
				|| Bukkit.getWorld(config.getString("world")) == null) {
			return null;
		}
		Location result = new Location(
				Bukkit.getWorld(config.getString("world")),
				config.getDouble("x"),
				config.getDouble("y"),
				config.getDouble("z"));
		if(config.isString("yaw") && config.isString("pitch")) {
			result.setPitch(Float.parseFloat(config.getString("pitch")));
			result.setYaw(Float.parseFloat(config.getString("yaw")));
		}
		return result;
	}

	/**
	 * Get a string list from the config, with fallback to single string as list
	 * @param section The section to look in
	 * @param key     The key in the section to get
	 * @return A list
	 */
	public static List<String> listOrSingle(ConfigurationSection section, String key) {
		if (section.isList(key)) {
			return section.getStringList(key);
		} else if (section.isSet(key)) {
			return new ArrayList<>(Collections.singletonList(section.getString(key)));
		}
		return null;
	}

    /**
     * Teleport a player to a location, or somewhere close to it where it is safe
     *
     * @param player   Player that should be teleported
     * @param location The location to teleport to
     * @return true if the teleport succeeded, otherwise false
     */
    public static boolean teleportToLocation(Player player, Location location, int maximumAttempts) {
        int checked = 1;

        // Setup startlocation at the center of the block (simplifies safe location check)
        Location startLocation = location.clone();
        startLocation.setX(startLocation.getBlockX() + 0.5);
        startLocation.setZ(startLocation.getBlockZ() + 0.5);

        // Check locations starting from startLocation and then a cube that increases
        // radius around that (until no block in the region is found at all cube sides)
        Location safeLocation = startLocation;
        int radius = 1;
        boolean done = isSafe(safeLocation);
        while (!done) {
            // North side
            for (int x = -radius + 1; x <= radius && !done; x++) {
                for (int y = -radius + 1; y < radius && !done; y++) {
                    safeLocation = startLocation.clone().add(x, y, -radius);
                    if (safeLocation.getBlockY() > 256 || safeLocation.getBlockY() < 0) {
                        continue;
                    }
                    checked++;
                    done = isSafe(safeLocation) || checked > maximumAttempts;
                }
            }

            // East side
            for (int z = -radius + 1; z <= radius && !done; z++) {
                for (int y = -radius + 1; y < radius && !done; y++) {
                    safeLocation = startLocation.clone().add(radius, y, z);
                    if (safeLocation.getBlockY() > 256 || safeLocation.getBlockY() < 0) {
                        continue;
                    }
                    checked++;
                    done = isSafe(safeLocation) || checked > maximumAttempts;
                }
            }

            // South side
            for (int x = radius - 1; x >= -radius && !done; x--) {
                for (int y = -radius + 1; y < radius && !done; y++) {
                    safeLocation = startLocation.clone().add(x, y, radius);
                    if (safeLocation.getBlockY() > 256 || safeLocation.getBlockY() < 0) {
                        continue;
                    }
                    checked++;
                    done = isSafe(safeLocation) || checked > maximumAttempts;
                }
            }

            // West side
            for (int z = radius - 1; z >= -radius && !done; z--) {
                for (int y = -radius + 1; y < radius && !done; y++) {
                    safeLocation = startLocation.clone().add(-radius, y, z);
                    if (safeLocation.getBlockY() > 256 || safeLocation.getBlockY() < 0) {
                        continue;
                    }
                    checked++;
                    done = isSafe(safeLocation) || checked > maximumAttempts;
                }
            }

            // Top side
            if (startLocation.getBlockY() + radius < 256 && !done) {
                // Middle block of the top
                safeLocation = startLocation.clone().add(0, radius, 0);
                checked++;
                done = isSafe(safeLocation) || checked > maximumAttempts;
                // Blocks around it
                for (int r = 1; r <= radius && !done; r++) {
                    // North
                    for (int x = -r + 1; x <= r && !done; x++) {
                        safeLocation = startLocation.clone().add(x, radius, -r);
                        checked++;
                        done = isSafe(safeLocation) || checked > maximumAttempts;
                    }
                    // East
                    for (int z = -r + 1; z <= r && !done; z++) {
                        safeLocation = startLocation.clone().add(r, radius, z);
                        checked++;
                        done = isSafe(safeLocation) || checked > maximumAttempts;
                    }
                    // South side
                    for (int x = r - 1; x >= -r && !done; x--) {
                        safeLocation = startLocation.clone().add(x, radius, r);
                        checked++;
                        done = isSafe(safeLocation) || checked > maximumAttempts;
                    }
                    // West side
                    for (int z = r - 1; z >= -r && !done; z--) {
                        safeLocation = startLocation.clone().add(-r, radius, z);
                        checked++;
                        done = isSafe(safeLocation) || checked > maximumAttempts;
                    }
                }
            }

            // Bottom side
            if (startLocation.getBlockY() - radius >= 0 && !done) {
                // Middle block of the bottom
                safeLocation = startLocation.clone().add(0, -radius, 0);
                checked++;
                done = isSafe(safeLocation) || checked > maximumAttempts;
                // Blocks around it
                for (int r = 1; r <= radius && !done; r++) {
                    // North
                    for (int x = -r + 1; x <= r && !done; x++) {
                        safeLocation = startLocation.clone().add(x, -radius, -r);
                        checked++;
                        done = isSafe(safeLocation) || checked > maximumAttempts;
                    }
                    // East
                    for (int z = -r + 1; z <= r && !done; z++) {
                        safeLocation = startLocation.clone().add(r, -radius, z);
                        checked++;
                        done = isSafe(safeLocation) || checked > maximumAttempts;
                    }
                    // South side
                    for (int x = r - 1; x >= -r && !done; x--) {
                        safeLocation = startLocation.clone().add(x, -radius, r);
                        checked++;
                        done = isSafe(safeLocation) || checked > maximumAttempts;
                    }
                    // West side
                    for (int z = r - 1; z >= -r && !done; z--) {
                        safeLocation = startLocation.clone().add(-r, -radius, z);
                        checked++;
                        done = isSafe(safeLocation) || checked > maximumAttempts;
                    }
                }
            }

            // Increase cube radius
            radius++;
        }

        // Either found safe location or ran out of attempts
        if (isSafe(safeLocation)) {
            player.teleport(safeLocation);
            //Log.debug("Found location: "+safeLocation.toString()+" Tries: "+(checked-1));
            return true;
        } else {
            PlayerWarps.debug("No location found, checked " + (checked - 1) + " spots of max " + maximumAttempts);
            return false;
        }
    }

    /**
     * Checks if a certain location is safe to teleport to
     *
     * @param location The location to check
     * @return true if it is safe, otherwise false
     */
    public static boolean isSafe(Location location) {
        Block feet = location.getBlock();
        Block head = feet.getRelative(BlockFace.UP);
        Block below = feet.getRelative(BlockFace.DOWN);
        Block above = head.getRelative(BlockFace.UP);
        // Check the block at the feet of the player
        if ((feet.getType().isSolid() && !canSpawnIn.contains(feet.getType())) || feet.isLiquid()) {
            return false;
        } else if ((head.getType().isSolid() && !canSpawnIn.contains(head.getType())) || head.isLiquid()) {
            return false;
        } else if (!below.getType().isSolid() || cannotSpawnOn.contains(below.getType()) || below.isLiquid()) {
            return false;
        } else if (above.isLiquid() || cannotSpawnBeside.contains(above.getType())) {
            return false;
        }
        // Check all blocks around
        ArrayList<Material> around = new ArrayList<>(Arrays.asList(
                feet.getRelative(BlockFace.NORTH).getType(),
                feet.getRelative(BlockFace.NORTH_EAST).getType(),
                feet.getRelative(BlockFace.EAST).getType(),
                feet.getRelative(BlockFace.SOUTH_EAST).getType(),
                feet.getRelative(BlockFace.SOUTH).getType(),
                feet.getRelative(BlockFace.SOUTH_WEST).getType(),
                feet.getRelative(BlockFace.WEST).getType(),
                feet.getRelative(BlockFace.NORTH_WEST).getType(),
                below.getRelative(BlockFace.NORTH).getType(),
                below.getRelative(BlockFace.NORTH_EAST).getType(),
                below.getRelative(BlockFace.EAST).getType(),
                below.getRelative(BlockFace.SOUTH_EAST).getType(),
                below.getRelative(BlockFace.SOUTH).getType(),
                below.getRelative(BlockFace.SOUTH_WEST).getType(),
                below.getRelative(BlockFace.WEST).getType(),
                below.getRelative(BlockFace.NORTH_WEST).getType(),
                head.getRelative(BlockFace.NORTH).getType(),
                head.getRelative(BlockFace.NORTH_EAST).getType(),
                head.getRelative(BlockFace.EAST).getType(),
                head.getRelative(BlockFace.SOUTH_EAST).getType(),
                head.getRelative(BlockFace.SOUTH).getType(),
                head.getRelative(BlockFace.SOUTH_WEST).getType(),
                head.getRelative(BlockFace.WEST).getType(),
                head.getRelative(BlockFace.NORTH_WEST).getType(),
                above.getRelative(BlockFace.NORTH).getType(),
                above.getRelative(BlockFace.NORTH_EAST).getType(),
                above.getRelative(BlockFace.EAST).getType(),
                above.getRelative(BlockFace.SOUTH_EAST).getType(),
                above.getRelative(BlockFace.SOUTH).getType(),
                above.getRelative(BlockFace.SOUTH_WEST).getType(),
                above.getRelative(BlockFace.WEST).getType(),
                above.getRelative(BlockFace.NORTH_WEST).getType()
        ));
        for (Material material : around) {
            if (cannotSpawnBeside.contains(material)) {
                return false;
            }
        }
        return true;
    }

}
