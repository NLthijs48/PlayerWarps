package me.wiefferink.playerwarps;

import me.wiefferink.playerwarps.commands.AddCommand;
import me.wiefferink.playerwarps.commands.CommandPlayerWarps;
import me.wiefferink.playerwarps.commands.DelCommand;
import me.wiefferink.playerwarps.commands.InfoCommand;
import me.wiefferink.playerwarps.commands.ListCommand;
import me.wiefferink.playerwarps.commands.PublicCommand;
import me.wiefferink.playerwarps.commands.ToCommand;
import me.wiefferink.playerwarps.commands.TrustCommand;
import me.wiefferink.playerwarps.commands.UntrustCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class CommandManager implements CommandExecutor, TabCompleter {
	private PlayerWarps plugin;
	private List<CommandPlayerWarps> commands;

	/**
	 * Constructor
	 * @param plugin The PlayerWarps plugin
	 */
	public CommandManager(PlayerWarps plugin) {
		this.plugin = plugin;
		commands = new ArrayList<>();
		commands.add(new AddCommand(plugin));
		commands.add(new ToCommand(plugin));
		commands.add(new DelCommand(plugin));
		commands.add(new ListCommand(plugin));
		commands.add(new PublicCommand(plugin));
		commands.add(new TrustCommand(plugin));
		commands.add(new UntrustCommand(plugin));
		commands.add(new InfoCommand(plugin));

		// Register commands in bukkit
		List<String> commands = Arrays.asList("warp", "sethome", "delhome", "home");
		for(String command : commands) {
			PluginCommand pluginCommand = plugin.getCommand(command);
			if(pluginCommand != null) {
				pluginCommand.setExecutor(this);
				pluginCommand.setTabCompleter(this);
			} else {
				plugin.getLogger().warning("Command "+command+" is not properly registered in plugin.yml!");
			}
		}
	}

	/**
	 * Get the list with AreaShop commands
	 * @return The list with AreaShop commands
	 */
	public List<CommandPlayerWarps> getCommands() {
		return commands;
	}

	/**
	 * Shows the help page for the CommandSender
	 * @param target The CommandSender to show the help to
	 */
	public void showHelp(CommandSender target) {
		// Add all messages to a list
		ArrayList<String> messages = new ArrayList<>();
		plugin.message(target, "help-header");
		plugin.message(target, "help-alias");
		for(CommandPlayerWarps command : commands) {
			String help = command.getHelp(target);
			if(help != null && help.length() != 0) {
				messages.add(help);
			}
		}
		// Send the messages to the target
		for(String message : messages) {
			plugin.messageNoPrefix(target, message);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		args = translateArgs(command, args);

		// Execute command
		PlayerWarps.debug("command: " + command.getName() + ", args: " + Arrays.toString(args));
		boolean executed = false;
		for(int i = 0; i < commands.size() && !executed; i++) {
			if(commands.get(i).canExecute("warp", args)) {
				commands.get(i).execute(sender, args);
				executed = true;
			}
		}
		if(!executed) {
			this.showHelp(sender);
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		args = translateArgs(command, args);

		List<String> result = new ArrayList<>();
		if(!sender.hasPermission("playerwarps.tabcomplete")) {
			return result;
		}
		int toCompleteNumber = args.length;
		String toCompletePrefix = args[args.length-1].toLowerCase();
		if(toCompleteNumber == 1) {
			for(CommandPlayerWarps c : commands) {
				String begin = c.getCommandStart();
				result.add(begin.substring(begin.indexOf(' ')+1));
			}
		} else {
			String[] start = new String[args.length];
			start[0] = "warp";
			System.arraycopy(args, 1, start, 1, args.length-1);
			for(CommandPlayerWarps c : commands) {
				if(c.canExecute("warp", args)) {
					result = c.getTabCompleteList(toCompleteNumber, start, sender);
				}
			}
		}
		// Filter and sort the results
		if(result.size() > 0) {
			SortedSet<String> set = new TreeSet<>();
			for(String suggestion : result) {
				if(suggestion.toLowerCase().startsWith(toCompletePrefix)) {
					set.add(suggestion);
				}
			}
			result.clear();
			result.addAll(set);
		}
		return result;
	}


	// Mapping for old style commands
	public String[] translateArgs(Command command, String[] args) {
		List<String> newArgs = new ArrayList<>();
		// Alias '/home [name]' to '/warp to [name]'
		if(command.getName().equalsIgnoreCase("home")) {
			newArgs.add("to");
		}
		// Alias  '/sethome [name]' to '/warp add [name]'
		else if(command.getName().equalsIgnoreCase("sethome")) {
			newArgs.add("add");
		}
		// Alias  '/sethome [name]' to '/warp add [name]'
		else if(command.getName().equalsIgnoreCase("delhome")) {
			newArgs.add("del");
		}
		if(!newArgs.isEmpty()) {
			if(args.length > 0) { // Provided a name
				newArgs.add(args[0]);
			} else { // Fallback to 'home'
				newArgs.add("home");
			}
			args = newArgs.toArray(new String[newArgs.size()]);
		}
		return args;
	}
}

















