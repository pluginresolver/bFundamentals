package uk.codingbadgers.bFundamentals.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

/**
 * Listener to handle commands for each module in bFundamentals
 *
 * @see ModuleCommand
 */
public class CommandListener implements Listener {

	/**
	 * Event listener for Player command.
	 *
	 * @param event the player command preprocess event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent event) {
			
		CommandSender sender = event.getPlayer();
		String command = event.getMessage().substring(1, event.getMessage().indexOf(' ') != -1 ? event.getMessage().indexOf(' ') : event.getMessage().length());
		String[] args = event.getMessage().indexOf(' ') != -1 ? event.getMessage().substring(event.getMessage().indexOf(' ') + 1).split(" ") : new String[0];
		
		try {
			if (ModuleCommandHandler.handleCommad(sender, command, args)) {
				event.setCancelled(true);
			}
		} catch (Exception ex) {
			sender.sendMessage(ChatColor.RED + "Error executing command");
			sender.sendMessage(ex.getMessage());
			ex.printStackTrace();
			event.setCancelled(true);
		}
	}
	
	/**
	 * Event listener for Server commands.
	 *
	 * @param event the server command event
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void onServerCommand(ServerCommandEvent event) {
		if (event.getCommand() == null || event.getCommand().length() <= 0)
			return;
		
		CommandSender sender = event.getSender();
		String command = event.getCommand().substring(0, event.getCommand().indexOf(' ') != -1 ? event.getCommand().indexOf(' ') : event.getCommand().length());
		String[] args = event.getCommand().indexOf(' ') != -1 ? event.getCommand().substring(event.getCommand().indexOf(' ') + 1).split(" ") : new String[0];
		

		try {
			if (ModuleCommandHandler.handleCommad(sender, command, args)) {
				return;
			}
		} catch (Exception ex) {
			sender.sendMessage(ChatColor.RED + "Error executing command");
			sender.sendMessage(ex.getMessage());
			ex.printStackTrace();
			return;
		}
	}
}
