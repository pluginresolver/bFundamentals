package uk.codingbadgers.bplugincontrol.commands;

import java.io.File;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;

import uk.codingbadgers.bFundamentals.bFundamentals;
import uk.codingbadgers.bFundamentals.commands.ModuleCommand;
import uk.codingbadgers.bFundamentals.module.Module;

public class CommandPlugin extends ModuleCommand {

	/**
	 * Command constructor.
	 */
	public CommandPlugin() {
		super("plugin", "plugin reload <plugin> | plugin disable <plugin> | plugin enable <plugin> | plugin info <plugin> | plugin load <plugin>");
	}
	
	/**
	 * Called when the ''plugin' command is executed.
	 */
	public boolean onCommand(CommandSender sender, String label, String[] args) {
		
		if (!(sender instanceof Player)) {
			System.out.println("Plugin commands must be used in game");
			return true;
		}
		
		Player player = (Player)sender;
		
		if (!Module.hasPermission(player, this.getPermission())) {
			Module.sendMessage("bPluginControl", player, "You do not have permission to access these commands.");
			return true;
		}
		
		if (args.length != 2) {
			// invalid usage
			Module.sendMessage("bPluginControl", player, "The following plugin command uses exist:");
			Module.sendMessage("bPluginControl", player, " - plugin reload <plugin>");
			Module.sendMessage("bPluginControl", player, " - plugin disable <plugin>");
			Module.sendMessage("bPluginControl", player, " - plugin enable <plugin>");
			Module.sendMessage("bPluginControl", player, " - plugin load <plugin>");
			Module.sendMessage("bPluginControl", player, " - plugin info <plugin>");
			return true;
		}
		
		final String command = args[0];
		final String pluginName = args[1];

		if (command.equalsIgnoreCase("load")) {
			return loadPlugin(player, pluginName);
		}
				
		if (command.equalsIgnoreCase("reload")) {
			return reloadPlugin(player, pluginName);		
		}
		
		if (command.equalsIgnoreCase("disable")) {
			return disablePlugin(player, pluginName);	
		}
		
		if (command.equalsIgnoreCase("enable")) {
			return enablePlugin(player, pluginName);
		}
		
		if (command.equalsIgnoreCase("info")) {
			return pluginInformation(player, pluginName);
		}
				
		return true;
	}
	
	/**
	 * Output information about a given plugin.
	 */
	private boolean pluginInformation(Player player, String pluginName) {
		PluginManager pluginManager = Bukkit.getServer().getPluginManager();
		Plugin plugin = pluginManager.getPlugin(pluginName);
		if (plugin == null) {
			Module.sendMessage("bPluginControl", player, "A plugin with the name '" + pluginName + "' could not be found.");
			return true;
		}
		
		PluginDescriptionFile discription = plugin.getDescription();
		
		String authors = "";
		List<String> authorsList = discription.getAuthors();
		for (int authorIndex = 0; authorIndex < authorsList.size() - 1; ++authorIndex) {
			String author = authorsList.get(authorIndex);
			authors += author + ", ";
		}
		authors += authorsList.get(authorsList.size() - 1);
		
		Module.sendMessage("bPluginControl", player, "||================================================||");
		Module.sendMessage("bPluginControl", player, "Name: " + plugin.getName());
		Module.sendMessage("bPluginControl", player, "Version: " + discription.getVersion());
		Module.sendMessage("bPluginControl", player, "Authors: " + authors);
		Module.sendMessage("bPluginControl", player, "Website: " + discription.getWebsite());
		Module.sendMessage("bPluginControl", player, "Enabled: " + (plugin.isEnabled() ? "Enable" : "Disabled"));
		Module.sendMessage("bPluginControl", player, "||================================================||");
		
		return true;
	}

	/**
	 * Disable a given plugin.
	 */
	private boolean disablePlugin(Player player, String pluginName) {
		PluginManager pluginManager = Bukkit.getServer().getPluginManager();
		Plugin plugin = pluginManager.getPlugin(pluginName);
		if (plugin == null) {
			Module.sendMessage("bPluginControl", player, "A plugin with the name '" + pluginName + "' could not be found.");
			return true;
		}
		
		pluginManager.disablePlugin(plugin);	
		
		Module.sendMessage("bPluginControl", player, "The plugin '" + pluginName + "' was successfully disabled.");
		return true;
	}
	
	/**
	 * Enable a given plugin.
	 */
	private boolean enablePlugin(Player player, String pluginName) {
		PluginManager pluginManager = Bukkit.getServer().getPluginManager();
		Plugin plugin = pluginManager.getPlugin(pluginName);
		if (plugin == null) {
			Module.sendMessage("bPluginControl", player, "A plugin with the name '" + pluginName + "' could not be found.");
			return true;
		}
		
		pluginManager.enablePlugin(plugin);	
		
		Module.sendMessage("bPluginControl", player, "The plugin '" + pluginName + "' was successfully enabled.");
		return true;
	}
	
	/**
	 * Reload a given plugin.
	 */
	private boolean reloadPlugin(Player player, String pluginName) {
		PluginManager pluginManager = Bukkit.getServer().getPluginManager();
		Plugin plugin = pluginManager.getPlugin(pluginName);
		if (plugin == null) {
			Module.sendMessage("bPluginControl", player, "A plugin with the name '" + pluginName + "' could not be found.");
			return true;
		}
		
		pluginManager.disablePlugin(plugin);
		pluginManager.enablePlugin(plugin);	
		
		Module.sendMessage("bPluginControl", player, "The plugin '" + pluginName + "' was successfully reloaded.");
		return true;
	}

	/**
	 * Load a given plugin.
	 */
	private boolean loadPlugin(Player player, String pluginName) {	
		PluginManager pluginManager = Bukkit.getServer().getPluginManager();
		
		// load and enable the given plugin	
		File pluginFolder = bFundamentals.getInstance().getDataFolder().getParentFile();
		File pluginFile = new File(pluginFolder + File.separator + pluginName);
		if (!pluginFile.exists()) {
			// plugin does not exist
			Module.sendMessage("bPluginControl", player, "A plugin with the name '" + pluginName + "' could not be found at location:");
			Module.sendMessage("bPluginControl", player, pluginFile.getAbsolutePath());
			return true;
		}
		
		// Try and load the plugin
		Plugin plugin = null;
		try {
			plugin = pluginManager.loadPlugin(pluginFile);
		} catch (Exception e) {
			// Something went wrong so set the plugin to null
			e.printStackTrace();
			plugin = null;
		}	
		
		if (plugin == null) {
			// The plugin failed to load correctly
			Module.sendMessage("bPluginControl", player, "The plugin '" + pluginName + "' failed to load correctly.");
			return true;
		}
		
		// plugin loaded and enabled successfully
		pluginManager.enablePlugin(plugin);
		Module.sendMessage("bPluginControl", player, "The plugin '" + pluginName + "' has been succesfully loaded and enabled.");
		
		return true;
	}
	
}