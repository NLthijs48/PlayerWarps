package me.wiefferink.playerwarps;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public final class PlayerWarps extends JavaPlugin {
	/* General variables */
	private FileManager fileManager = null;
	private LanguageManager languageManager = null;
	private boolean configOk = false;
	private boolean debug = false;
	private String chatprefix = null;

	/* Folder where the language files will be stored */
	public final String languageFolder = "lang";


	/**
	 * Called on start or reload of the server
	 */
	public void onEnable() {
		boolean error;
		// Save a copy of the default config.yml if one is not present
		this.saveDefaultConfig();

		// Check the config, loads default if errors
		configOk = this.checkConfig();

		// Create a LanguageMananager
		languageManager = new LanguageManager(this);

		// Save the chatPrefix
		chatprefix = this.config().getString("chatPrefix");

		// Load all data from files
		fileManager = new FileManager(this);
		error = !fileManager.loadFiles();

		if (error) {
			this.getLogger().info("The plugin has not started, fix the errors listed above");
		} else {
			// Setup CommandManager
			CommandManager commandManager = new CommandManager(this);
			List<String> commands = Arrays.asList("warp", "sethome", "delhome", "home");
			for (String command : commands) {
				PluginCommand pluginCommand = getCommand(command);
				if (pluginCommand != null) {
					pluginCommand.setExecutor(commandManager);
				} else {
					getLogger().warning("Command " + command + " is not properly registered in plugin.yml!");
				}
			}
		}
	}

	/**
	 * Called on shutdown or reload of the server
	 */
	public void onDisable() {
		fileManager = null;
		languageManager = null;
		configOk = false;
		debug = false;
	}


	/**
	 * Function to get the WorldGuard plugin
	 * @return WorldGuardPlugin
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
	 * Method to send a message to a CommandSender, using chatprefix if it is a player
	 * @param target The CommandSender you wan't to send the message to (e.g. a player)
	 * @param key The key to get the translation
	 * @param params The parameters to inject into the message string
	 */
	public void message(Object target, String key, Object... params) {
		String langString = this.fixColors(languageManager.getLang(key, params));
		if (langString == null) {
			this.getLogger().info("Something is wrong with the language file, could not find key: " + key);
		} else {
			if (target instanceof Player) {
				((Player) target).sendMessage(this.fixColors(chatprefix) + langString);
			} else if (target instanceof CommandSender) {
				((CommandSender) target).sendMessage(langString);
			} else if (target instanceof Logger) {
				((Logger) target).info(langString);
			} else {
				this.getLogger().info("Could not send message, target is wrong: " + langString);
			}
		}
	}

	/**
	 * Convert color and formatting codes to bukkit values
	 * @param input Start string with color and formatting codes in it
	 * @return String with the color and formatting codes in the bukkit format
	 */
	public String fixColors(String input) {
		String result = null;
		if (input != null) {
			result = input.replaceAll("(&([a-f0-9]))", "\u00A7$2");
			result = result.replaceAll("&k", ChatColor.MAGIC.toString());
			result = result.replaceAll("&l", ChatColor.BOLD.toString());
			result = result.replaceAll("&m", ChatColor.STRIKETHROUGH.toString());
			result = result.replaceAll("&n", ChatColor.UNDERLINE.toString());
			result = result.replaceAll("&o", ChatColor.ITALIC.toString());
			result = result.replaceAll("&r", ChatColor.RESET.toString());
			result = result.replaceAll("�", "\u20AC");
		}
		return result;
	}


	/**
	 * Function for quitting the plugin, NOT USED ATM
	 */
	public void quit() {
		this.getLogger().info("Plugin will be stopped");
		Bukkit.getPluginManager().disablePlugin(this);
	}


	/**
	 * Return the config configured by the user or the default
	 */
	public Configuration config() {
		if (configOk) {
			return this.getConfig();
		} else {
			return this.getConfig().getDefaults();
		}
	}

	/**
	 * Shows the help page for the player
	 * @param target The player to show the help to
	 */
	public void showHelp(CommandSender target) {
		/* Set up the list of messages to be sent */
		ArrayList<String> messages = new ArrayList<>();
		messages.add(this.config().getString("chatPrefix") + languageManager.getLang("help-header"));
		messages.add(this.config().getString("chatPrefix") + languageManager.getLang("help-alias"));
		if (target.hasPermission("playerwarps.list")) {
			messages.add(languageManager.getLang("help-list"));
		}
		if (target.hasPermission("playerwarps.public")) {
			messages.add(languageManager.getLang("help-public"));
		}
		if (target.hasPermission("playerwarps.add")) {
			messages.add(languageManager.getLang("help-add"));
		}
		if (target.hasPermission("playerwarps.to")) {
			messages.add(languageManager.getLang("help-to"));
		}
		if (target.hasPermission("playerwarps.del")) {
			messages.add(languageManager.getLang("help-del"));
		}
		if (target.hasPermission("playerwarps.trust")) {
			messages.add(languageManager.getLang("help-trust"));
		}
		if (target.hasPermission("playerwarps.untrust")) {
			messages.add(languageManager.getLang("help-untrust"));
		}
		if (target.hasPermission("playerwarps.info")) {
			messages.add(languageManager.getLang("help-info"));
		}

		for (String message : messages) {
			target.sendMessage(this.fixColors(message));
		}
	}


	/**
	 * Checks the config for errors, loads default config if they occur
	 */
	public boolean checkConfig() {
		int error = 0;
		debug = this.getConfig().getString("debug").equalsIgnoreCase("true");
		
		/* GENERAL */
		String chatPrefix = this.getConfig().getString("chatPrefix");
		if (chatPrefix.length() == 0) {
			this.getLogger().info("Config-Error: chatPrefix has length zero");
			error++;
		}		
		
		/* Load default config if errors have occurred */
		if (error > 0) {
			this.getLogger().info("The plugin has " + error + " error(s) in the config, default config will be used");
		}
		
		/* return true if no errors, false if there are errors */
		return (error == 0);
	}

	/**
	 * Sends an debug message to the console
	 * @param message The message that should be printed to the console
	 */
	public void debug(String message) {
		if (this.debug) {
			this.getLogger().info("Debug: " + message);
		}
	}

	/**
	 * Reload the config of the plugin
	 */
	public void reload() {
		this.saveDefaultConfig();
		this.reloadConfig();
		configOk = this.checkConfig();
		chatprefix = this.config().getString("chatPrefix");
		languageManager = new LanguageManager(this);
	}

}




