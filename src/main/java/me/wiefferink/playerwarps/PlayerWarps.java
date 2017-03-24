package me.wiefferink.playerwarps;


import me.wiefferink.interactivemessenger.processing.Message;
import me.wiefferink.interactivemessenger.source.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class PlayerWarps extends JavaPlugin {
	// General variables
	private FileManager fileManager = null;
	private LanguageManager languageManager = null;
	private CommandManager commandManager = null;
	private static boolean debug = false;
	private static PlayerWarps instance;

	/**
	 * Called on start or reload of the server
	 */
	public void onEnable() {
		instance = this;
		boolean error;
		// Save a copy of the default config.yml if one is not present
		this.saveDefaultConfig();
		debug = this.getConfig().getString("debug").equalsIgnoreCase("true");

		// Create a LanguageMananager
		languageManager = new LanguageManager(this, "lang", getConfig().getString("language"), "EN", Utils.listOrSingle(getConfig(), "chatPrefix"));

		// Load all data from files
		fileManager = new FileManager(this);
		error = !fileManager.loadWarps();

		if (error) {
			this.getLogger().info("The plugin has not started, fix the errors listed above");
		} else {
			commandManager = new CommandManager(this);
			// Save warps timer
			new BukkitRunnable() {
				@Override
				public void run() {
					fileManager.saveFilesNow();
				}
			}.runTaskTimer(this, 18000L, 18000L);
		}
	}

	/**
	 * Called on shutdown or reload of the server
	 */
	public void onDisable() {
		getFileManager().saveFilesNow();
		Bukkit.getScheduler().cancelTasks(this);
		HandlerList.unregisterAll(this);
		fileManager = null;
		languageManager = null;
		debug = false;
	}


	/**
	 * Function to get the LanguageManager
	 * @return The LanguageManager
	 */
	public LanguageManager getLanguageManager() {
		return languageManager;
	}

	/**
	 * Method to get the FileManager
	 * @return The fileManager
	 */
	public FileManager getFileManager() {
		return fileManager;
	}

	/**
	 * Method to get the CommandManager
	 * @return The commandManager
	 */
	public CommandManager getCommandManager() {
		return commandManager;
	}

	/**
	 * Send a message to a target without a prefix
	 * @param target       The target to send the message to
	 * @param key          The key of the language string
	 * @param replacements The replacements to insert in the message
	 */
	public void messageNoPrefix(Object target, String key, Object... replacements) {
		Message.fromKey(key).replacements(replacements).send(target);
	}

	/**
	 * Send a message to a target, prefixed by the default chat prefix
	 * @param target       The target to send the message to
	 * @param key          The key of the language string
	 * @param replacements The replacements to insert in the message
	 */
	public void message(Object target, String key, Object... replacements) {
		Message.fromKey(key).prefix().replacements(replacements).send(target);
	}

	/**
	 * Sends an debug message to the console
	 * @param message The message that should be printed to the console
	 */
	public static void debug(String message) {
		if (debug) {
			instance.getLogger().info("Debug: " + message);
		}
	}

	/**
	 * Reload the config of the plugin
	 */
	public void reload() {
		onDisable();
		onEnable();
	}

}




