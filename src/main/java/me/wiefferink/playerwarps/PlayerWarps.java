package me.wiefferink.playerwarps;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

public final class PlayerWarps extends JavaPlugin {
	// General variables
	private FileManager fileManager = null;
	private LanguageManager languageManager = null;
	private CommandManager commandManager = null;
	private boolean configOk = false;
	private boolean debug = false;
	private String chatprefix = null;

	// Folder where the language files will be stored
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
	 * Method to get the CommandManager
	 * @return The commandManager
	 */
	public CommandManager getCommandManager() {
		return commandManager;
	}

	public void configurableMessage(String prefix, Object target, String key, Object... params) {
		String langString = Utils.applyColors(this.languageManager.getLang(key, params));
		if(langString == null) {
			getLogger().info("Something is wrong with the language file, could not find key: "+key);
		} else if((target instanceof Player)) {
			String message = langString;
			if(prefix != null) {
				message = prefix+message;
			}
			message = Utils.applyColors(message);
			((Player)target).sendMessage(message);
		} else if((target instanceof CommandSender)) {
			((CommandSender)target).sendMessage(langString);
		} else if((target instanceof Logger)) {
			((Logger)target).info(ChatColor.stripColor(langString));
		} else if(target instanceof BufferedWriter) {
			try {
				if(prefix != null) {
					langString = prefix+langString;
				}
				((BufferedWriter)target).append(ChatColor.stripColor(langString));
				((BufferedWriter)target).newLine();
			} catch(IOException e) {
				getLogger().warning("Error while printing message to BufferedWriter: "+e.getMessage());
				e.printStackTrace();
			}
		} else {
			getLogger().info("Could not send message, target is wrong: "+langString);
		}
	}

	public void message(Object target, String key, Object... params) {
		configurableMessage(this.chatprefix, target, key, params);
	}

	public void messageNoPrefix(Object target, String key, Object... params) {
		configurableMessage(null, target, key, params);
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
			target.sendMessage(Utils.applyColors(message));
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
		onDisable();
		onEnable();
	}

}




