package net.centerleft.localshops;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

import com.nijiko.permissions.PermissionHandler;

import cuboidLocale.BookmarkedResult;
import cuboidLocale.QuadTree;

/**
 * Local Shops Plugin
 * 
 * @author Jonbas
 */
public class LocalShops extends JavaPlugin {
	private final ShopsPlayerListener playerListener = new ShopsPlayerListener(this);
	private final ShopsPluginListener pluginListener = new ShopsPluginListener(this);
	
	static String pluginName;
	static String pluginVersion;
	
	static QuadTree cuboidTree = new QuadTree();
	static String folderPath = "plugins/LocalShops/";
	static File folderDir;
	static String shopsPath = "shops/";
	static File shopsDir;
	static List<World> foundWorlds;
	
	
	static ItemData itemList = new ItemData();
	
	public Map<String, BookmarkedResult> playerResult;  //synchronized result buffer hash
	
	public void onEnable() {
		
		playerResult = Collections.synchronizedMap(new HashMap<String, BookmarkedResult>());
		
		for( Player player : this.getServer().getOnlinePlayers() ) {
			if( !this.playerResult.containsKey(player.getName())) {
				this.playerResult.put(player.getName(), new BookmarkedResult());
			}
			if( !PlayerData.playerShopList.containsKey(player.getName())) {
				PlayerData.playerShopList.put(player.getName(), Collections.synchronizedList(new ArrayList<String>()));	
			}
		}

		// Register our events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLUGIN_ENABLE, pluginListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLUGIN_DISABLE, pluginListener, Priority.Monitor, this);
		
		Plugin p = pm.getPlugin("GroupManager");
		if (p != null) {
            if (!p.isEnabled()) {
                pm.enablePlugin(p);
            }
            GroupManager gm = (GroupManager) p;
            ShopsPluginListener.groupManager = gm;
            ShopsPluginListener.gmPermissionCheck = gm.getPermissionHandler();
            System.out.println("HelpPages: GroupManager found.");
            ShopsPluginListener.useGroupManager = true;
          
        } else {
        	System.out.println("HelpPages: GroupManager not found.");
        	ShopsPluginListener.useGroupManager = false;
        }
		
		

		
		// setup the file IO
		folderDir = new File(folderPath);
		folderDir.mkdir();
		shopsDir = new File(folderPath + shopsPath); 
		shopsDir.mkdir();
		
		//build data table for item names and values
		itemList.loadData(new File(folderPath + "items.txt"));
		
		foundWorlds = getServer().getWorlds();
		// read the shops into memory
		ShopData.LoadShops( shopsDir );

		//update the console that we've started
		PluginDescriptionFile pdfFile = this.getDescription();
		pluginName = pdfFile.getName();
		pluginVersion = pdfFile.getVersion();
		System.out.println( pluginName + ": Loaded " + ShopData.shops.size() + " shop(s).");
		System.out.println( pluginName + ": version " + pluginVersion + " is enabled!");
		
		
	}

	public void onDisable() {
		//update the console that we've stopped
		System.out.println( pluginName + " version "
				+ pluginVersion + ": is disabled!");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {
		String[] trimmedArgs = args;
		String commandName = command.getName().toLowerCase();

		if (commandName.equalsIgnoreCase("shop")) {
			if(args.length >= 1) {
				if(args[0].equalsIgnoreCase("create")) {
					Commands.createShop(sender, trimmedArgs);
				} else if(args[0].equalsIgnoreCase("list")) {
					Commands.listShop(sender, trimmedArgs);
				} else if(args[0].equalsIgnoreCase("reload")) {
					if(Commands.canUseCommand(sender, trimmedArgs)) {
						PluginManager pm = sender.getServer().getPluginManager();
						Plugin ourPlugin = pm.getPlugin(pluginName);
						pm.disablePlugin(ourPlugin);
						pm.enablePlugin(ourPlugin);
					}
				} else if(args[0].equalsIgnoreCase("sell")) {
					Commands.sellItemShop(sender, trimmedArgs);
				} else if(args[0].equalsIgnoreCase("add")) {
					Commands.addItemShop(sender, trimmedArgs);
				}
				
			} else {
				Commands.printHelp(sender, args);
			}
			
			return true;
		}
		return false;
	}
}
