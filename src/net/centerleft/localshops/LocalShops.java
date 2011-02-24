package net.centerleft.localshops;

import java.io.File;
import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import cuboidLocale.QuadTree;

/**
 * Local Shops Plugin
 * 
 * @author Jonbas
 */
public class LocalShops extends JavaPlugin {
	private final ShopsPlayerListener playerListener = new ShopsPlayerListener(this);
	
	static String pluginName;
	static String pluginVersion;
	
	static QuadTree cuboidTree = new QuadTree();
	static String folderPath = "plugins/LocalShops/";
	static File folderDir;
	static String shopsPath = "shops/";
	static File shopsDir;
	
	public void onEnable() {

		// Register our events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
		
		// setup the file IO
		folderDir = new File(folderPath);
		folderDir.mkdir();
		shopsDir = new File(folderPath + shopsPath); 
		shopsDir.mkdir();
		
		// read the shops into memory
		shopData.LoadShops( shopsDir );

		//update the console that we've started
		PluginDescriptionFile pdfFile = this.getDescription();
		pluginName = pdfFile.getName();
		pluginVersion = pdfFile.getVersion();
		System.out.println( pluginName + ": Loaded " + shopData.shops.size() + " shops.");
		System.out.println( pluginName + " version " + pluginVersion + " is enabled!");
	}

	public void onDisable() {
		//update the console that we've stopped
		System.out.println( pluginName + " version "
				+ pluginVersion + " is disabled!");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {
		String[] trimmedArgs = args;
		String commandName = command.getName().toLowerCase();

		if (commandName.equals("shop")) {
			return true;
		}
		return false;
	}
}
