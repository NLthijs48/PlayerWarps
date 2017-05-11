package me.wiefferink.playerwarps.commands;

import me.wiefferink.playerwarps.PlayerWarps;
import org.bukkit.command.CommandSender;

/**
 * Created by ewoud on 11/05/2017.
 */
public class ReloadCommand extends CommandPlayerWarps {

    public ReloadCommand(PlayerWarps plugin) {
        super(plugin);
    }

    @Override
    public String getCommandStart() {
        return "warp reload";
    }

    @Override
    public String getHelp(CommandSender target) {
        if(target.hasPermission("playerwarps.reload")) {
            return "help-reload";
        }
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
       plugin.onDisable();
       plugin.onEnable();
       plugin.message(sender, "reload-success");
    }

}