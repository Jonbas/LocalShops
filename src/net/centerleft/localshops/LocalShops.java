package net.centerleft.localshops;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
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
import com.nijiko.coelho.iConomy.iConomy;
import com.nijikokun.bukkit.Permissions.Permissions;

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
	
	static String pluginName = "LocalShops";
	static String pluginVersion;
	
	static QuadTree cuboidTree = new QuadTree();
	static String folderPath = "plugins/LocalShops/";
	static File folderDir;
	static String shopsPath = "shops/";
	static File shopsDir;
	static List<World> foundWorlds;
	
	static PropertyHandler properties;
	
	
	static ItemData itemList = new ItemData();
	static Map<String, PlayerData> playerData; //synchronized player hash
	
	public Map<String, BookmarkedResult> playerResult;  //synchronized result buffer hash
	
	public void onEnable() {
		
		QuadTree cuboidTree = new QuadTree();
		playerResult = Collections.synchronizedMap(new HashMap<String, BookmarkedResult>());
		playerData = Collections.synchronizedMap(new HashMap<String, PlayerData>());
		
		// add all the online users to the data trees
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
		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLUGIN_ENABLE, pluginListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLUGIN_DISABLE, pluginListener, Priority.Monitor, this);
		
		
		//check hook for permissions
		Plugin p = pm.getPlugin("Permissions");
		if (p != null) {
            if (p.isEnabled()) {
                Permissions gm = (Permissions) p;
                ShopsPluginListener.permissions = gm;
                ShopsPluginListener.gmPermissionCheck = gm.getHandler();
                System.out.println("LocalShops: Permissions found.");
                ShopsPluginListener.usePermissions = true;
            } else {
            	ShopsPluginListener.usePermissions = false;
            }
        } else {
        	System.out.println("LocalShops: Permissions not found.");
        	ShopsPluginListener.usePermissions = false;
        }
		
		//check hook for iConomy
		Plugin ic = pm.getPlugin("iConomy");
		if (ic != null) {
            if (ic.isEnabled()) {
                iConomy icon = (iConomy) ic;
                ShopsPluginListener.iConomy = icon;
                System.out.println("LocalShops: iConomy found.");
                ShopsPluginListener.useiConomy = true;
            } else {
            	ShopsPluginListener.useiConomy = false;
            	System.out.println("LocalShops: waiting for iConomy to start.");
            }
        } else {
        	System.out.println("LocalShops: iConomy not found.");
        	ShopsPluginListener.useiConomy = false;
        }
		
		

		
		// setup the file IO
		folderDir = new File(folderPath);
		folderDir.mkdir();
		shopsDir = new File(folderPath + shopsPath); 
		shopsDir.mkdir();
		
		//build data table for item names and values
		itemList.loadData(new File(folderPath + "items.txt"));
		
		properties = new PropertyHandler(folderPath + "localshops.properties");
		properties.load();
		PluginProperties.loadProperties(properties);
		properties.save();
		
		foundWorlds = getServer().getWorlds();
		// read the shops into memory
		ShopData.LoadShops( shopsDir );

		//update the console that we've started
		PluginDescriptionFile pdfFile = this.getDescription();
		pluginName = pdfFile.getName();
		pluginVersion = pdfFile.getVersion();
		System.out.println( pluginName + ": Loaded " + ShopData.shops.size() + " shop(s).");
		System.out.println( pluginName + ": version " + pluginVersion + " is enabled!");
		
		// check which shops players are inside
		for( Player player : this.getServer().getOnlinePlayers() ) {
			ShopsPlayerListener.checkPlayerPosition(this, player);
		}
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
					for(Player player: this.getServer().getOnlinePlayers()) {
						ShopsPlayerListener.checkPlayerPosition(this, player);
					}
				} else if(args[0].equalsIgnoreCase("destroy")) {
					Commands.destroyShop(sender, trimmedArgs);
					for(Player player: this.getServer().getOnlinePlayers()) {
						ShopsPlayerListener.checkPlayerPosition(this, player);
					}
				} else if(args[0].equalsIgnoreCase("move")) {
					Commands.moveShop(sender, trimmedArgs);
					for(Player player: this.getServer().getOnlinePlayers()) {
						ShopsPlayerListener.checkPlayerPosition(this, player);
					}
				} else if(args[0].equalsIgnoreCase("list")) {
					Commands.listShop(sender, trimmedArgs);
				} else if(args[0].equalsIgnoreCase("reload")) {
					if(Commands.canUseCommand(sender, trimmedArgs)) {
						
						//TODO fix this null pointer exception from ourPlugin
						PluginManager pm = sender.getServer().getPluginManager();
						Plugin ourPlugin = pm.getPlugin(pluginName);
						pm.disablePlugin(ourPlugin);
						pm.enablePlugin(ourPlugin);
						
						sender.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "The plugin has been reloaded." );
						
						
					}
				} else if(args[0].equalsIgnoreCase("sell")) {
					Commands.sellItemShop(sender, trimmedArgs);
				} else if(args[0].equalsIgnoreCase("add")) {
					Commands.addItemShop(sender, trimmedArgs);
				} else if(args[0].equalsIgnoreCase("remove")) {
					Commands.removeItemShop(sender, trimmedArgs);
				}else if(args[0].equalsIgnoreCase("buy")) {
					Commands.buyItemShop(sender, trimmedArgs);
				} else if(args[0].equalsIgnoreCase("set")) {
					Commands.setItemShop(sender, trimmedArgs);
				} else if(args[0].equalsIgnoreCase("select")) {
					if(Commands.canUseCommand(sender, args)) {
						if(!(sender instanceof Player)) return false;
						String playerName = ((Player)sender).getName();
						if(!playerData.containsKey(playerName)) {
							playerData.put(playerName, new PlayerData());
						}
						playerData.get(playerName).isSelecting = !playerData.get(playerName).isSelecting;
						
						if(playerData.get(playerName).isSelecting) {
							sender.sendMessage(ChatColor.AQUA + "Left click to select the first corner for a shop.");
							sender.sendMessage(ChatColor.AQUA + "Right click to select the second corner for the shop.");
						} else {
							sender.sendMessage(ChatColor.AQUA + "Selection disabled");
							playerData.put(playerName, new PlayerData());
						}
					}
				} else {
					Commands.printHelp(sender, args);
				}
				
			} else {
				Commands.printHelp(sender, args);
			}
			
			return true;
		}
		return false;
	}
}
