package net.centerleft.localshops;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wool;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.nijiko.permissions.PermissionHandler;

import cuboidLocale.BookmarkedResult;
import cuboidLocale.PrimitiveCuboid;

public class Commands {
	
	static boolean createShop( CommandSender sender, String[] args ) {
		//TODO Change this so that non players can create shops as long as they send x, y, z coords
		if(canUseCommand(sender, args) && args.length == 2 && (sender instanceof Player)) {
			//command format /shop create ShopName
			Player player = (Player)sender;
			Location location = player.getLocation();
			
			//check to see if that shop name is already used
			Iterator<String> itr = ShopData.shops.keySet().iterator();
			while( itr.hasNext() ) {
				String name = itr.next();
				if( name.equalsIgnoreCase(args[1]) ) {
					player.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "Could not create shop.  " 
							+ ChatColor.WHITE + name + ChatColor.AQUA + " already exists.");
					return false;
				}
			}
			
			long x = (long)location.getX();
			long y = (long)location.getY();
			long z = (long)location.getZ();
			
			String shopName = args[1];
			
			Shop thisShop = new Shop();
			
			
			thisShop.setShopCreator(player.getName());
			thisShop.setShopOwner(player.getName());
			thisShop.setShopName(shopName);
			thisShop.setWorldName(player.getWorld().getName());
			
			//setup the cuboid for the tree
			long[] xyzA = new long[3];
			long[] xyzB = new long[3];
			
			if(LocalShops.playerData.containsKey(player.getName()) 
					&& LocalShops.playerData.get(player.getName()).isSelecting ) {
				if (!LocalShops.playerData.get(player.getName()).sizeOkay) {
					if (!canUseCommand(player, "admin".split(""))) {
						String size = "" + ShopData.maxWidth + "x" + ShopData.maxHeight + "x" + ShopData.maxWidth;
						player.sendMessage(ChatColor.AQUA + "Problem with selection. Max size is "
								+ ChatColor.WHITE + size);
						return false;
					}
				}
				//if a custom size had been set, use that
				PlayerData data = LocalShops.playerData.get(player.getName());
				xyzA = data.getPositionA();
				xyzB = data.getPositionB();
				
				if(xyzA == null || xyzB == null) {
					player.sendMessage(ChatColor.AQUA + "Problem with selection.");
					return false;
				}
			} else {
				//otherwise calculate the shop from the player's location
				if( ShopData.shopSize % 2 == 1) {
					xyzA[0] = x - (ShopData.shopSize / 2);
					xyzB[0] = x + (ShopData.shopSize / 2);
					xyzA[2] = z - (ShopData.shopSize / 2);
					xyzB[2] = z + (ShopData.shopSize / 2);
				} else {
					xyzA[0] = x - (ShopData.shopSize / 2) + 1;
					xyzB[0] = x + (ShopData.shopSize / 2);
					xyzA[2] = z - (ShopData.shopSize / 2) + 1;
					xyzB[2] = z + (ShopData.shopSize / 2);
				}
				
				xyzA[1] = y - 1;
				xyzB[1] = y + ShopData.shopHeight - 1;
				
			}
			
			thisShop.setLocation( xyzA, xyzB );
			
			//need to check to see if the shop overlaps another shop
			if( shopPositionOk( player,  xyzA, xyzB )) {
				
				PrimitiveCuboid tempShopCuboid = new PrimitiveCuboid( xyzA, xyzB );
				tempShopCuboid.name = shopName;
				tempShopCuboid.world = player.getWorld().getName();

				
				if(ShopData.chargeForShop) {
					String[] freeShop = {"freeshop"};
					if(!canUseCommand(sender, freeShop)) {
						if(!PlayerData.chargePlayer(player.getName(), ShopData.shopCost)) {
							player.sendMessage( PlayerData.chatPrefix + ChatColor.AQUA + "You need " + ShopData.shopCost + " " + ShopData.currencyName + " to create a shop.");
							return false;
						}
					}
				}
				
				//insert the shop into the world
				LocalShops.cuboidTree.insert(tempShopCuboid);
				ShopData.shops.put(shopName, thisShop );

				LocalShops.playerData.put(player.getName(), new PlayerData());
				
				//write the file
				if( ShopData.saveShop(thisShop) ) { 
					player.sendMessage( PlayerData.chatPrefix + ChatColor.WHITE + shopName + ChatColor.AQUA + " was created successfully.");
					return true;
				} else {
					player.sendMessage( PlayerData.chatPrefix + ChatColor.AQUA + "There was an error, could not create shop.");
					return false;
				}
				
			} 
		}
		if(args.length != 2) {
			sender.sendMessage( PlayerData.chatPrefix + ChatColor.AQUA + "The command format is " + ChatColor.WHITE + "/shop create [ShopName]");
		}
		if(!canUseCommand(sender, args)) {
			sender.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "You don't have permission to use this command");
		}
		return false;
	}
	
	static boolean moveShop( CommandSender sender, String[] args ) {
		//TODO Change this so that non players can create shops as long as they send x, y, z coords
		if(canUseCommand(sender, args) && args.length == 2 && (sender instanceof Player)) {
			//command format /shop move ShopName
			Player player = (Player)sender;
			Location location = player.getLocation();
			Shop thisShop = null;
			
			long[] xyzAold = new long[3];
			long[] xyzBold = new long[3];
			
			//check to see if that shop name exists and has access
			boolean foundShop = false;
			Iterator<String> itr = ShopData.shops.keySet().iterator();
			while( itr.hasNext() ) {
				String name = itr.next();
				if( name.equalsIgnoreCase(args[1]) ) {
					thisShop = ShopData.shops.get(name);
					foundShop = true;
				}
			}
			
			if(!foundShop) {
				player.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "Could not find shop: " + ChatColor.WHITE 
						+ args[1]);
				return false;
			}
			
			if(!thisShop.getShopOwner().equalsIgnoreCase(player.getName()) 
					&& !canUseCommand(player, "admin".split("")) ) {
				player.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "You must be the shop owner to move this shop.");
				return false;
			}
			
			//store shop info
			String shopName = thisShop.getShopName();
			xyzAold = thisShop.getLocation1();
			xyzBold = thisShop.getLocation2();
			
			long x = (long)location.getX();
			long y = (long)location.getY();
			long z = (long)location.getZ();
			
			
			//setup the cuboid for the tree
			long[] xyzA = new long[3];
			long[] xyzB = new long[3];
			
			if(LocalShops.playerData.containsKey(player.getName()) 
					&& LocalShops.playerData.get(player.getName()).isSelecting ) {
				if (!LocalShops.playerData.get(player.getName()).sizeOkay) {
					if (!canUseCommand(player, "admin".split(""))) {
						String size = "" + ShopData.maxWidth + "x"
								+ ShopData.maxHeight + "x" + ShopData.maxWidth;
						player.sendMessage(ChatColor.AQUA
								+ "Problem with selection. Max size is "
								+ ChatColor.WHITE + size);
						return false;
					}
				}
				//if a custom size had been set, use that
				PlayerData data = LocalShops.playerData.get(player.getName());
				xyzA = data.getPositionA().clone();
				xyzB = data.getPositionB().clone();
				
				if(xyzA == null || xyzB == null) {
					player.sendMessage(ChatColor.AQUA + "Problem with selection.");
					return false;
				}
			} else {
				//otherwise calculate the shop from the player's location
				if( ShopData.shopSize % 2 == 1) {
					xyzA[0] = x - (ShopData.shopSize / 2);
					xyzB[0] = x + (ShopData.shopSize / 2);
					xyzA[2] = z - (ShopData.shopSize / 2);
					xyzB[2] = z + (ShopData.shopSize / 2);
				} else {
					xyzA[0] = x - (ShopData.shopSize / 2) + 1;
					xyzB[0] = x + (ShopData.shopSize / 2);
					xyzA[2] = z - (ShopData.shopSize / 2) + 1;
					xyzB[2] = z + (ShopData.shopSize / 2);
				}
				
				xyzA[1] = y - 1;
				xyzB[1] = y + ShopData.shopHeight - 1;
				
			}
			
			
			//remove the old shop from the cuboid
			long[] xyz = thisShop.getLocation();
			BookmarkedResult res = new BookmarkedResult();

			res = LocalShops.cuboidTree.relatedSearch(res.bookmark, xyz[0],
					xyz[1], xyz[2]);

			// get the shop's tree node and delete it
			for (PrimitiveCuboid shopLocation : res.results) {

				// for each shop that you find, check to see if we're already in it
				// this should only find one shop node
				if (shopLocation.name == null)
					continue;
				if (!shopLocation.world.equalsIgnoreCase(thisShop.getWorldName()))
					continue;

				LocalShops.cuboidTree.delete(shopLocation);
			}
			
			//need to check to see if the shop overlaps another shop
			if( shopPositionOk( player,  xyzA, xyzB )) {
				
				PrimitiveCuboid tempShopCuboid = new PrimitiveCuboid( xyzA, xyzB );
				tempShopCuboid.name = shopName;
				tempShopCuboid.world = player.getWorld().getName();

				
				if(ShopData.chargeForMove) {
					String[] freemove = {"freemove"};
					if(!canUseCommand(sender, freemove)) {
						if(!PlayerData.chargePlayer(player.getName(), ShopData.shopCost)) {
							//insert the old cuboid back into the world
							tempShopCuboid = new PrimitiveCuboid( xyzAold, xyzBold );
							tempShopCuboid.name = shopName;
							tempShopCuboid.world = thisShop.getWorldName();
							LocalShops.cuboidTree.insert(tempShopCuboid);
							
							player.sendMessage( PlayerData.chatPrefix + ChatColor.AQUA + "You need " + ShopData.moveCost + " " + ShopData.currencyName + " to move a shop.");
							return false;
						}
					}
				}
				
				//insert the shop into the world
				LocalShops.cuboidTree.insert(tempShopCuboid);
				thisShop.setWorldName(player.getWorld().getName());
				thisShop.setLocation( xyzA, xyzB );
				ShopData.shops.put(shopName, thisShop );

				LocalShops.playerData.put(player.getName(), new PlayerData());
				
				//write the file
				if( ShopData.saveShop(thisShop) ) { 
					player.sendMessage( PlayerData.chatPrefix + ChatColor.WHITE + shopName + ChatColor.AQUA + " was moved successfully.");
					return true;
				} else {
					player.sendMessage( PlayerData.chatPrefix + ChatColor.AQUA + "There was an error, could not move shop.");
					return false;
				}
			} else {
				//insert the old cuboid back into the world
				PrimitiveCuboid tempShopCuboid = new PrimitiveCuboid( xyzAold, xyzBold );
				tempShopCuboid.name = shopName;
				tempShopCuboid.world = thisShop.getWorldName();
				LocalShops.cuboidTree.insert(tempShopCuboid);
			}
		}
		if(args.length != 2) {
			sender.sendMessage( PlayerData.chatPrefix + ChatColor.AQUA + "The command format is " + ChatColor.WHITE + "/shop move [ShopName]");
		}
		if(!canUseCommand(sender, args)) {
			sender.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "You don't have permission to use this command");
		}
		return false;
	}
	
	
	static boolean canUseCommand( CommandSender sender, String[] args ) {
		boolean useManager = ShopsPluginListener.usePermissions;
		PermissionHandler pm = ShopsPluginListener.gmPermissionCheck;
		
		if(!(sender instanceof Player)) return false;
		
		Player player = (Player)sender;
		
		if(useManager ) {
			if(pm.has(player,"localshops.admin")) return true;
		} else if ( sender.isOp() ) {
			return true;
		}
		
		if(args.length >= 1) {
			
			if(args[0].equalsIgnoreCase("create") || (args[0].equalsIgnoreCase("select"))) {
				if(useManager) {
					return pm.has(player, "localshops.create");
				} 
			} if(args[0].equalsIgnoreCase("move")) {
				if(useManager) {
					return pm.has(player, "localshops.move");
				} 
			} if(args[0].equalsIgnoreCase("freemove")) {
				if(useManager) {
					return pm.has(player, "localshops.move.free");
				} 
			} else if(args[0].equalsIgnoreCase("freeshop")) {
				if(useManager) {
					return pm.has(player, "localshops.create.free");
				} 
			} else if(args[0].equalsIgnoreCase("destroy")) {
				if(useManager) {
					return pm.has(player, "localshops.destroy");
				} 
				
			} else if(args[0].equalsIgnoreCase("reload")) {
				if(useManager) {
					return pm.has(player, "localshops.reload");
				} 
				
			} else if(args[0].equalsIgnoreCase("sell") || args[0].equalsIgnoreCase("buy") 
					|| args[0].equalsIgnoreCase("list")) {
				if(useManager) {
					return pm.has(player, "localshops.buysell");
				} else {
					return true;
				}
			} else if (args[0].equalsIgnoreCase("set")){
				if(args.length > 1 && args[1].equalsIgnoreCase("owner")) {
					if(useManager) {
						return pm.has(player, "localshops.manage.owner");
					} else {
						return true;
					}
				} else {
					if(useManager) {
						return pm.has(player, "localshops.manage");
					} else {
						return true;
					}
				}
			} else if(args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove") ) {
				if(useManager) {
					return pm.has(player, "localshops.manage");
				} else {
					return true;
				}
			} 
	
		}
		return false;
	}

	public static void printHelp(CommandSender sender, String[] args) {
		sender.sendMessage( PlayerData.chatPrefix + ChatColor.AQUA + "Here are the available commands [required] <optional>" );

		String[] sell = { "sell" };
		if(canUseCommand(sender, sell)) {
			String partial = "";
			if(canUseCommand(sender, "admins".split(""))) {
				partial = "|info";
			}
			sender.sendMessage( ChatColor.WHITE + "   /shop list <buy|sell" + partial + "> " + ChatColor.AQUA + "- List the shop's inventory." );
			sender.sendMessage( ChatColor.WHITE + "   /shop buy [itemname] [number] " + ChatColor.AQUA + "- Buy this item." );
			sender.sendMessage( ChatColor.WHITE + "   /shop sell <#|all>" + ChatColor.AQUA + " - Sell the item in your hand." );
			sender.sendMessage( ChatColor.WHITE + "   /shop sell [itemname] [number]" );
		}
		
		String[] set = { "set" };
		if(canUseCommand(sender, set)) {
			sender.sendMessage( ChatColor.WHITE + "   /shop add" + ChatColor.AQUA + " - Add the item that you are holding to the shop.");
			sender.sendMessage( ChatColor.WHITE + "   /shop remove [itemname]" + ChatColor.AQUA + " - Stop selling item in shop." );
			sender.sendMessage( ChatColor.WHITE + "   /shop set" + ChatColor.AQUA + " - Display list of set commands" );
			
		}
		
		String[] create = { "create" };
		if(canUseCommand(sender, create)) {
			sender.sendMessage( ChatColor.WHITE + "   /shop create [ShopName]" + ChatColor.AQUA + " - Create a shop at your location.");
			sender.sendMessage( ChatColor.WHITE + "   /shop select" + ChatColor.AQUA + " - Select two corners for custom shop size.");
			
		}
		
		String[] move = { "move" };
		if(canUseCommand(sender, move)) {
			sender.sendMessage( ChatColor.WHITE + "   /shop move [ShopName]" + ChatColor.AQUA + " - Move a shop to your location.");
			
		}
		
		String[] destroy = { "destroy" };
		if(canUseCommand(sender, destroy)) {
			sender.sendMessage( ChatColor.WHITE + "   /shop destroy" + ChatColor.AQUA + " - Destroy the shop you're in.");
		}
		
		String[] reload = { "reload" };
		if(canUseCommand(sender, reload)) {
			sender.sendMessage( ChatColor.WHITE + "   /shop reload" + ChatColor.AQUA + " - Reload the plugin and shop files.");
		}
	}
	
	private static boolean shopPositionOk( Player player, long[] xyzA, long[] xyzB ) {
		BookmarkedResult res = new BookmarkedResult();
		
		//make sure coords are in right order
		for( int i = 0; i < 3; i++) {
			if( xyzA[i] > xyzB[i]) {
				long temp = xyzA[i];
				xyzA[i] = xyzB[i];
				xyzB[i] = temp;
			}
		}
		
		//Need to test every position to account for variable shop sizes
		
		for( long x = xyzA[0]; x <= xyzB[0]; x++) {
			for( long z = xyzA[2]; z <= xyzB[2]; z++) {
				for( long y = xyzA[1]; y <= xyzB[1]; y++) {
					res = LocalShops.cuboidTree.relatedSearch(res.bookmark, x, y, z );
					if( shopOverlaps(player, res) ) return false;
				}
			}
		}
		return true;
	}
	
	private static boolean shopOverlaps( Player player, BookmarkedResult res ) {
		if( res.results.size() != 0 ) {
			for( PrimitiveCuboid shop : res.results) {
				if(shop.name != null) {
					if(shop.world.equalsIgnoreCase(player.getWorld().getName())) {
					player.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "Could not create shop, it overlaps with " + ChatColor.WHITE 
							+ shop.name );
					return true;
					}
				}
			}
		}
		return false;
	}

	public static void listShop(CommandSender sender, String[] args) {
		if(canUseCommand(sender, args) && (sender instanceof Player)) {
			Player player = (Player)sender;
			String playerName = player.getName();
			String inShopName;
			
			int pageNumber = 1;
			
			if(args.length == 2) {
				try {
					pageNumber = Integer.parseInt(args[1]);
				} catch (NumberFormatException ex) {
					
				}
			}
			
			if(args.length == 3 ) {
				try {
					pageNumber = Integer.parseInt(args[2]);
				} catch (NumberFormatException ex2) {
					
				}
			}
		
			//get the shop the player is currently in

			if( PlayerData.playerShopsList(playerName).size() == 1 ) {
				inShopName = PlayerData.playerShopList.get(playerName).get(0);
				Shop shop = ShopData.shops.get(inShopName);
				
				if( args.length > 1 ) {
					if( args[1].equalsIgnoreCase("buy") || args[1].equalsIgnoreCase("sell")) {
						printInventory( shop, player, args[1], pageNumber );
						
					} else if (args[1].equalsIgnoreCase("info")){ 
						player.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "Info for shop " + ChatColor.WHITE + shop.getShopName());
						String location = "" + shop.getLocation1()[0];
						location +=  " " + shop.getLocation1()[1];
						location += " " + shop.getLocation1()[2];
						player.sendMessage( ChatColor.AQUA + "  Shop Location 1 " + ChatColor.WHITE + location);
						location = "" + shop.getLocation2()[0];
						location +=  " " + shop.getLocation2()[1];
						location += " " + shop.getLocation2()[2];
						player.sendMessage( ChatColor.AQUA + "  Shop Location 2 " + ChatColor.WHITE + location);
						player.sendMessage( ChatColor.AQUA + "  Shop Owner " + ChatColor.WHITE + shop.getShopOwner());
						String message = "";
						if(shop.getShopManagers() != null) {
							for( String manager: shop.getShopManagers()) {
								message += " " + manager;
							}
						} else {
							message = "none";
						}
						player.sendMessage( ChatColor.AQUA + "  Shop managers " + ChatColor.WHITE + message);
						player.sendMessage( ChatColor.AQUA + "  Shop Creator " + ChatColor.WHITE + shop.getShopCreator());
						player.sendMessage( ChatColor.AQUA + "  Shop has unlimited Stock " + ChatColor.WHITE + shop.getValueofUnlimitedStock());
						player.sendMessage( ChatColor.AQUA + "  Shop has unlimited Money " + ChatColor.WHITE + shop.getValueofUnlimitedMoney());
					} else {
						printInventory( shop, player, "list", pageNumber );
					}
				} else {
					printInventory( shop, player, "list", pageNumber );
				}
			} else {
				player.sendMessage(ChatColor.AQUA + "You must be inside a shop to use /shop list");
			}
		} else {
			sender.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "You don't have permission to use this command");
		}
	}
	
	/**
	 * Prints shop inventory with default page # = 1
	 * 
	 * @param shop
	 * @param player
	 * @param buySellorList
	 */
	public static void printInventory( Shop shop, Player player, String buySellorList) {
		printInventory( shop, player, buySellorList, 1 );
	}
	
	/**
	 * Prints shop inventory list.  Takes buy, sell, or list as arguments for which format to print.
	 * 
	 * @param shop
	 * @param player
	 * @param buySellorList
	 * @param pageNumber
	 */
	public static void printInventory( Shop shop, Player player, String buySellorList, int pageNumber) {
		String inShopName = shop.getShopName();
		ArrayList<String> shopItems = shop.getItems();
		
		boolean buy = buySellorList.equalsIgnoreCase("buy");
		boolean sell = buySellorList.equalsIgnoreCase("sell");
		boolean list = buySellorList.equalsIgnoreCase("list");
		
		ArrayList<String> inventoryMessage = new ArrayList<String>();
		for(String item: shopItems ) {
			
			String subMessage = "   " + item;
			int maxStock = 0;
			if(!list) {
				int price = 0;
				if(buy) {
				//get buy price
					price = shop.getItemBuyPrice(item);
				}
				if(sell) {
					price = shop.getItemSellPrice(item);
				}
	 			if(price == 0) continue;
				subMessage += ChatColor.AQUA + " [" + ChatColor.WHITE + price + " " + ShopData.currencyName 
					+ ChatColor.AQUA + "]";
				//get stack size
				int stack = shop.itemBuyAmount(item);
				if(buy) {
					stack = shop.itemBuyAmount(item);
				}
				if(sell) {
					stack = shop.itemSellAmount(item);
					int stock = shop.getItemStock(item);
					maxStock = shop.itemMaxStock(item);
					
					if(stock >= maxStock && !(maxStock == 0)) continue;
				}
				if( stack > 1 ) {
					subMessage += ChatColor.AQUA + " [" + ChatColor.WHITE + "Bundle: " + stack + ChatColor.AQUA + "]";
				}
			}
			//get stock
			int stock = shop.getItemStock(item);
			if(buy) {
				if(stock == 0 && !shop.isUnlimitedStock()) continue;
			}
			if(!shop.isUnlimitedStock()) {
				subMessage += ChatColor.AQUA + " [" + ChatColor.WHITE + "Stock: " + stock + ChatColor.AQUA + "]";
	
				maxStock = shop.itemMaxStock(item);
				if(maxStock > 0) {
					subMessage += ChatColor.AQUA + " [" + ChatColor.WHITE + "Max Stock: " + maxStock + ChatColor.AQUA + "]";
				}
			}

			inventoryMessage.add(subMessage);
		}
		
		String message = ChatColor.AQUA + "The shop " + ChatColor.WHITE + inShopName + ChatColor.AQUA;
		
		if( buy ) {
			message += " is selling:";
		} else if ( sell ) {
			message += " is buying:";
		} else {
			message += " trades in: ";
		}
		
		message += " (Page " + pageNumber + " of " 
			+ (int) Math.ceil((double) inventoryMessage.size() / (double) 7) + ")";
		
		player.sendMessage(message);
		
		int amount = (pageNumber > 0 ? (pageNumber - 1)*7 : 0);
		for (int i = amount; i < amount + 7; i++) {
            if (inventoryMessage.size() > i) {
            	player.sendMessage(inventoryMessage.get(i));
            }
        }
		
		if(!list) {
			String buySell = ( buy ? "buy" : "sell" );
			message = ChatColor.AQUA + "To " + buySell + " an item on the list type: " +
				ChatColor.WHITE + "/shop " + buySell + " ItemName [amount]";
			player.sendMessage(message);
		} else {
			player.sendMessage(ChatColor.AQUA + "Type " + ChatColor.WHITE + "/shop list buy" 
					+ ChatColor.AQUA + " or " + ChatColor.WHITE + "/shop list sell");
			player.sendMessage(ChatColor.AQUA + "to see details about price and quantity.");
		}
	}
	
	/**
	 * Processes sell command.
	 *  
	 * @param sender
	 * @param args
	 * @return
	 *   true - if command succeeds
	 *   false otherwise
	 */
	public static boolean sellItemShop(CommandSender sender, String[] args) {
		if(!(sender instanceof Player) || !canUseCommand(sender, args)) {
			sender.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "You don't have permission to use this command");
			return false;
		}
		if(!ShopsPluginListener.useiConomy) {
			sender.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "Can not complete. Can not find iConomy.");
			return false;
		}
	
		/* Available formats:
		 *  /shop sell
		 *  /shop sell #
		 *  /shop sell all
		 *  /shop sell item #
		 *  /shop sell item all
		 */
		String shopName;
		Shop shop;
		
		Player player = (Player)sender;
		String playerName = player.getName();

		ItemStack item = player.getItemInHand();
		String itemName = null;
		int amount = item.getAmount();

		
		//get the shop the player is currently in
		if( PlayerData.playerShopsList(playerName).size() == 1 ) {
			shopName = PlayerData.playerShopsList(playerName).get(0);
			shop = ShopData.shops.get(shopName);
		} else {
			player.sendMessage(ChatColor.AQUA + "You must be inside a shop to use /shop " + args[0]);
			return false;
		}
		
		if(args.length == 1) {
			// /shop sell
		} else if(args.length == 2 ){
			/*	/shop sell #
			 *  /shop sell all
			 *  /shop sell item 
			 */
			if(!args[1].equalsIgnoreCase("all")) {
				try {
					amount = Integer.parseInt(args[1]);
				} catch( NumberFormatException ex1 ) {
					item = LocalShops.itemList.getShopItem(player, shop, args[1]);
					itemName = null;
				}
			}
		} else if(args.length == 3 ) {
			/*	/shop sell item #
			 *  /shop sell item all
			 */
			item = LocalShops.itemList.getShopItem(player, shop, args[1]);
			itemName = null;
			if(!args[2].equalsIgnoreCase("all")) {
				try {
					amount = Integer.parseInt(args[2]);
				} catch( NumberFormatException ex1 ) {
					itemName = null;
				}
			}
		} else {
			item = null;
			itemName = null;
		}
		
		if(item == null && itemName == null) {
			player.sendMessage(ChatColor.AQUA + "Input problem. The format is " + ChatColor.WHITE + "/shop sell <itemName> <# to sell>");
			return false;
		}
		
		if(item == null && itemName != null) {
			item = LocalShops.itemList.getItem(player, args[1]);
			if(item == null) {
				player.sendMessage(ChatColor.AQUA + "Could not add the item to shop.");
				return false;
			} 
		} else if (item != null && itemName == null) {
			itemName = LocalShops.itemList.getItemName(item.getType().getId(), (int)item.getDurability());
			if(itemName == null) {
				sender.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "Item " + ChatColor.WHITE + item.getType().toString() + ChatColor.AQUA + " can not be added to the shop.");
				System.out.println("LocalShops: " + player.getName() + " tried to add " + item.getType().toString() + " but it's not in the item list." );
				return false;
			}
		}
		
		//check if the shop is buying that item
		if(!shop.getItems().contains(itemName) || shop.getItemSellPrice(itemName) == 0) {
			player.sendMessage(ChatColor.AQUA + "Sorry, " + ChatColor.WHITE + shopName 
					+ ChatColor.AQUA + " is not buying " + ChatColor.WHITE + itemName 
					+ ChatColor.AQUA + " right now." );
			return false;
		}
		
		// check how many items the player has
		int playerInventory = countItemsinInventory(player.getInventory(), item);
		if( amount < 0 ) amount = 0;
		if( args.length == 2 ) {
			if(args[1].equalsIgnoreCase("all")) {
				amount = playerInventory;
			}
		} else if ( args.length == 3 ) {
			if(args[2].equalsIgnoreCase("all")) {
				amount = playerInventory;
			}
		}
		
		// check if the amount to add is okay
		if(amount > playerInventory) {
			player.sendMessage(ChatColor.AQUA + "You only have " + ChatColor.WHITE +  playerInventory 
					+ ChatColor.AQUA + " in your inventory that can be added.");
			amount = playerInventory;
		}
		
		//check if the shop has a max stock level set
		if(shop.itemMaxStock(itemName) != 0 && !shop.isUnlimitedStock()) {
			if(shop.getItemStock(itemName) >= shop.itemMaxStock(itemName)) {
				player.sendMessage(ChatColor.AQUA + "Sorry, " + ChatColor.WHITE + shopName 
						+ ChatColor.AQUA + " is not buying any more " + ChatColor.WHITE + itemName 
						+ ChatColor.AQUA + " right now." );
				return false;
			}

			if( amount > (shop.itemMaxStock(itemName) - shop.getItemStock(itemName))) {
				amount = shop.itemMaxStock(itemName) - shop.getItemStock(itemName);
			}
		}
		
		//calculate cost
		int bundles = amount / shop.itemSellAmount(itemName);
		
		if(bundles == 0 && amount > 0) {
			player.sendMessage(ChatColor.AQUA + "The minimum number to sell is  " + ChatColor.WHITE 
					+ shop.itemSellAmount(itemName));
			return false;
		}
		
		int itemPrice = shop.getItemSellPrice(itemName);
		//recalculate # of items since may not fit cleanly into bundles
		//notify player if there is a change
		if(amount%shop.itemSellAmount(itemName) != 0) {
			player.sendMessage(ChatColor.AQUA + "The bundle size is  " + ChatColor.WHITE 
					+ shop.itemSellAmount(itemName) + ChatColor.AQUA + " order reduced to " 
					+ ChatColor.WHITE + bundles * shop.itemSellAmount(itemName));
		}
		amount = bundles * shop.itemSellAmount(itemName);
		int totalCost = bundles * itemPrice;

		//try to pay the player for order
		if(shop.isUnlimitedMoney()) {
			PlayerData.payPlayer(playerName, totalCost);
		} else {
			if( !isShopController(player, shop )) {
				if(!PlayerData.payPlayer(shop.getShopOwner(), playerName, totalCost)) {
					//shop owner doesn't have enough money
					//get shop owner's balance and calculate how many it can buy
					long shopBalance = PlayerData.getBalance(shop.getShopOwner());
					int bundlesCanAford = (int)shopBalance / itemPrice;
					totalCost = bundlesCanAford * itemPrice;
					amount = bundlesCanAford * shop.itemSellAmount(itemName);
					player.sendMessage(ChatColor.AQUA + "The shop could only afford " + ChatColor.WHITE + amount);
					if(!PlayerData.payPlayer(shop.getShopOwner(), playerName, totalCost)) {
						player.sendMessage(ChatColor.AQUA + "Unexpected money problem: could not complete sale.");
						return false;
					}
				}
			}
		}
		
		if(!shop.isUnlimitedStock()) {
			shop.addStock(itemName, amount);
		}

		if(isShopController(player, shop )) {
			player.sendMessage(ChatColor.AQUA + "You added " + ChatColor.WHITE +  amount + " " 
					+ itemName + ChatColor.AQUA +" to the shop"); 				
		} else {
			player.sendMessage(ChatColor.AQUA + "You sold " + ChatColor.WHITE +  amount + " " 
					+ itemName + ChatColor.AQUA + " and gained " + ChatColor.WHITE + totalCost 
					+ " " + ShopData.currencyName); 
		}
		
		//log the transaction
		int itemInv = shop.getItemStock(itemName);
		int startInv = itemInv - amount;
		if( startInv < 0 ) startInv = 0;
		ShopData.logItems(playerName, shopName, "sell-item", itemName, amount, startInv, itemInv );
		
		removeItemsFromInventory(player.getInventory(), item, amount);		
		ShopData.saveShop(shop);
		
		
		return true;
	}
	
	/**
	 * Add an item to the shop.  Checks if shop manager or owner and takes item from inventory of player.
	 * If item is not sold in the shop yet, adds the item with buy and sell price of 0 and default bundle
	 * sizes of 1.
	 * 
	 * @param sender
	 * @param args
	 * @return true if the commands succeeds, otherwise false
	 */
	public static boolean addItemShop(CommandSender sender, String[] args) {
		if(!(sender instanceof Player) || !canUseCommand(sender, args)) {
			sender.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "You don't have permission to use this command");
			return false;
		}
//TODO		
		/* Available formats:
		 *  /shop add
		 *  /shop add #
		 *  /shop add all
		 *  /shop add item
		 *  /shop add item #
		 *  /shop add item all
		 */
		
		String shopName;
		Shop shop;
		
		Player player = (Player)sender;
		String playerName = player.getName();

		ItemStack item = player.getItemInHand();
		String itemName = null;
		int amount = item.getAmount();

		
		//get the shop the player is currently in
		if( PlayerData.playerShopsList(playerName).size() == 1 ) {
			shopName = PlayerData.playerShopsList(playerName).get(0);
			shop = ShopData.shops.get(shopName);
			
			if(!isShopController(player, shop) && !canUseCommand(player, "admin".split("")))  {
				player.sendMessage(ChatColor.AQUA + "You must be the shop owner or a manager to add items.");
				player.sendMessage(ChatColor.AQUA + "The current shop owner is " + ChatColor.WHITE + shop.getShopOwner());
				return false;
			}
			
		} else {
			player.sendMessage(ChatColor.AQUA + "You must be inside a shop to use /shop " + args[0]);
			return false;
		}
		
		if(args.length == 1) {
			// /shop add
		} else if(args.length == 2 ){
			/*	/shop add #
			 *  /shop add all
			 *  /shop add item 
			 */
			if(!args[1].equalsIgnoreCase("all")) {
				try {
					amount = Integer.parseInt(args[1]);
				} catch( NumberFormatException ex1 ) {
					item = LocalShops.itemList.getItem(player, args[1]);
					itemName = null;
				}
			}
		} else if(args.length == 3 ) {
			/*	/shop add item #
			 *  /shop add item all
			 */
			item = LocalShops.itemList.getItem(player, args[1]);
			itemName = null;
			if(!args[2].equalsIgnoreCase("all")) {
				try {
					amount = Integer.parseInt(args[2]);
				} catch( NumberFormatException ex1 ) {
					itemName = null;
				}
			}
		} else {
			item = null;
			itemName = null;
		}
		
		if(item == null && itemName == null) {
			player.sendMessage(ChatColor.AQUA + "Input problem. The format is " + ChatColor.WHITE + "/shop add <itemName> <# to sell>");
			return false;
		}
		
		if(item == null && itemName != null) {
			item = LocalShops.itemList.getItem(player, args[1]);
			if(item == null) {
				player.sendMessage(ChatColor.AQUA + "Could not add the item to shop.");
				return false;
			} 
		} else if (item != null && itemName == null) {
			itemName = LocalShops.itemList.getItemName(item.getType().getId(), (int)item.getDurability());
			if(itemName == null) {
				sender.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "Item " + ChatColor.WHITE + item.getType().toString() + ChatColor.AQUA + " can not be added to the shop.");
				System.out.println("LocalShops: " + player.getName() + " tried to add " + item.getType().toString() + " but it's not in the item list." );
				return false;
			}
		}
		
		// check how many items the player has
		int playerInventory = countItemsinInventory(player.getInventory(), item);
		if( amount < 0 ) amount = 0;
		if( args.length == 2 ) {
			if(args[1].equalsIgnoreCase("all")) {
				amount = playerInventory;
			}
		} else if ( args.length == 3 ) {
			if(args[2].equalsIgnoreCase("all")) {
				amount = playerInventory;
			}
		}
		
		// check if the amount to add is okay
		if(amount > playerInventory) {
			player.sendMessage(ChatColor.AQUA + "You only have " + ChatColor.WHITE +  playerInventory 
					+ ChatColor.AQUA + " in your inventory that can be added.");
			amount = playerInventory;
		}
		
		
		// check if the shop is buying that item
		if(!shop.getItems().contains(itemName)) {
			int itemInfo[] = LocalShops.itemList.getItemInfo(player, itemName);
			if( itemInfo == null ) {
				player.sendMessage(ChatColor.AQUA + "Could not add the item to shop.");
				return false;
			}
			shop.addItem(itemInfo[0], itemInfo[1], 0, 1, 0, 1, 0, 0);
		}
		
		if(!shop.isUnlimitedStock()) {
			shop.addStock(itemName, amount);
			player.sendMessage(ChatColor.AQUA + "Succesfully added " + ChatColor.WHITE + itemName 
					+ ChatColor.AQUA + " to the shop. Stock is now " + ChatColor.WHITE + shop.getItemStock(itemName));
		} else {
			player.sendMessage(ChatColor.AQUA + "Succesfully added " + ChatColor.WHITE + itemName 
					+ ChatColor.AQUA + " to the shop.");
		}
		
		//log the transaction
		int itemInv = shop.getItemStock(itemName);
		int startInv = itemInv - amount;
		if( startInv < 0 ) startInv = 0;
		ShopData.logItems(playerName, shopName, "add-item", itemName, amount, startInv, itemInv );
		
		//take items from player
		removeItemsFromInventory(player.getInventory(), item, amount);		
		ShopData.saveShop(shop);
		
		
		return true;
	}

	/**
	 * Returns true if the player is in the shop manager list or is the shop owner
	 * 
	 * @param player
	 * @param shop
	 * @return
	 */
	private static boolean isShopController(Player player, Shop shop) {
		if(shop.getShopOwner().equalsIgnoreCase(player.getName())) return true;
		if(shop.getShopManagers() != null) {
			for(String manager: shop.getShopManagers()) {
				if( player.getName().equalsIgnoreCase(manager)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Processes buy command.
	 *  
	 * @param sender
	 * @param args
	 * @return
	 *   true - if command succeeds
	 *   false otherwise
	 */
	public static boolean buyItemShop(CommandSender sender, String[] args) {
		if(!(sender instanceof Player) || !canUseCommand(sender, args)) {
			sender.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "You don't have permission to use this command");
			return false;
		}
		if(!ShopsPluginListener.useiConomy) {
			sender.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "Can not complete. Can not find iConomy.");
			return false;
		}
		
		/* Available formats:
		 *  /shop buy item #
		 */
		
		Player player = (Player)sender;
		String playerName = player.getName();

		//get the shop the player is currently in
		if( PlayerData.playerShopsList(playerName).size() == 1 ) {
			String shopName = PlayerData.playerShopsList(playerName).get(0);
			Shop shop = ShopData.shops.get(shopName);
			
			ItemStack item = null;
			String itemName = null;
			int amount = 0;
			
			if(args.length == 3) {
				item = LocalShops.itemList.getShopItem(player, shop, args[1]);
				if(item == null) {
					player.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "Could not complete the purchase.");
					return false;
				} else {
					int itemData = item.getDurability();
					itemName = LocalShops.itemList.getItemName(item.getTypeId(), itemData);
				}
				
				//check if the shop is selling that item
				if(!shop.getItems().contains(itemName)) {
					player.sendMessage(ChatColor.AQUA + "Sorry, " + ChatColor.WHITE + shopName 
							+ ChatColor.AQUA + " is not selling " + ChatColor.WHITE + itemName 
							+ ChatColor.AQUA + " right now." );
					return false;
				}
				
				//check if the item has a price, or if this is a shop owner
				if(shop.getItemBuyPrice(itemName) == 0 && !isShopController(player, shop)) {
					player.sendMessage(ChatColor.AQUA + "Sorry, " + ChatColor.WHITE + shopName 
							+ ChatColor.AQUA + " is not selling " + ChatColor.WHITE + itemName 
							+ ChatColor.AQUA + " right now." );
					return false;
				}
				
				int totalAmount;
				totalAmount = shop.getItemStock(itemName);
				
				if(totalAmount == 0 && !shop.isUnlimitedStock()) {
					player.sendMessage(ChatColor.AQUA + "The shop has " + ChatColor.WHITE + totalAmount + " " + itemName);
					return true;
				}
					
				try {
					int numberToRemove = Integer.parseInt(args[2]);
					if(numberToRemove < 0) numberToRemove = 0;
					if(shop.isUnlimitedStock()) {
						totalAmount = numberToRemove;
					}
					if( numberToRemove > totalAmount) {
						amount = totalAmount - (totalAmount%shop.itemBuyAmount(itemName));
						if(!shop.isUnlimitedStock()) {
							player.sendMessage(ChatColor.AQUA + "The shop has " + ChatColor.WHITE + totalAmount + " " + itemName);
						}
					} else {
						amount = numberToRemove - (numberToRemove%shop.itemBuyAmount(itemName));
					}
					
					if(amount%shop.itemBuyAmount(itemName) != 0) {
						player.sendMessage(ChatColor.AQUA + "The bundle size is  " + ChatColor.WHITE 
								+ shop.itemBuyAmount(itemName) + ChatColor.AQUA + " order reduced to " 
								+ ChatColor.WHITE + (int)(amount / shop.itemBuyAmount(itemName)));
					}
				} catch (NumberFormatException ex2 ) {
					if( args[2].equalsIgnoreCase("all")) {
						amount = totalAmount;
					} else {
						player.sendMessage(ChatColor.AQUA + "Input problem. The format is " + ChatColor.WHITE + "/shop buy <itemName> <# to buy>");
						return false;
					}
				}
				
			} else {
				player.sendMessage(ChatColor.AQUA + "Input problem. The format is " + ChatColor.WHITE + "/shop buy <itemName> <# to buy>");
				return false;
			}
			
			//check how many items the user has room for
			int freeSpots = 0;
			for(ItemStack thisSlot: player.getInventory().getContents()) {
				if(thisSlot == null || thisSlot.getType() == Material.AIR) {
					freeSpots += 64;
					continue;
				}
				if(thisSlot.getTypeId() == item.getTypeId() && thisSlot.getDurability() == item.getDurability()) {
					freeSpots += 64 - thisSlot.getAmount();
				}
			}
			
			if(amount > freeSpots) {
				player.sendMessage(ChatColor.AQUA + "You only have room for " + ChatColor.WHITE + freeSpots);
				amount = freeSpots;
			}
			
			//calculate cost
			int bundles = amount / shop.itemBuyAmount(itemName);
			int itemPrice = shop.getItemBuyPrice(itemName);
			//recalculate # of items since may not fit cleanly into bundles
			amount = bundles * shop.itemBuyAmount(itemName);
			int totalCost = bundles * itemPrice;
			
			//try to pay the shop owner
			if( !isShopController(player, shop )) {
				if(!PlayerData.payPlayer( playerName, shop.getShopOwner(), totalCost)) {
					//player doesn't have enough money
					//get player's balance and calculate how many it can buy
					long playerBalance = PlayerData.getBalance(playerName);
					int bundlesCanAford = (int)Math.floor(playerBalance / itemPrice);
					totalCost = bundlesCanAford * itemPrice;
					amount = bundlesCanAford * shop.itemSellAmount(itemName);
					player.sendMessage(ChatColor.AQUA + "You could only afford " + ChatColor.WHITE + amount);
					
					if(!PlayerData.payPlayer( playerName, shop.getShopOwner(), totalCost)) {
						player.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "Unexpected money problem: could not complete sale.");
						return false;
					}
				}
			}
			
			if(!shop.isUnlimitedStock()) {
				shop.removeStock(itemName, amount);
			}
			if(isShopController(player, shop)) {
				player.sendMessage(ChatColor.AQUA + "You removed " + ChatColor.WHITE +  amount + " " 
						+ itemName + ChatColor.AQUA + " from the shop"); 
			} else {
				player.sendMessage(ChatColor.AQUA + "You purchased " + ChatColor.WHITE +  amount + " " 
						+ itemName + ChatColor.AQUA + " for " + ChatColor.WHITE + totalCost + " " + ShopData.currencyName); 
			}
			
			//log the transaction
			int itemInv = shop.getItemStock(itemName);
			int startInv = itemInv + amount;
			if( shop.isUnlimitedStock() ) startInv = 0;
			ShopData.logItems(playerName, shopName, "buy-item", itemName, amount, startInv, itemInv );
			
			givePlayerItem(player, item, amount);
			ShopData.saveShop(shop);

		} else {
			player.sendMessage(ChatColor.AQUA + "You must be inside a shop to use /shop " + args[0]);
		}
			
		return true;
	}
	
	/**
	 * Processes set command.
	 *  
	 * @param sender
	 * @param args
	 * @return
	 *   true - if command succeeds
	 *   false otherwise
	 */
	public static boolean setItemShop(CommandSender sender, String[] args) {
		if(!(sender instanceof Player) || !canUseCommand(sender, args)) {
			sender.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "You don't have permission to use this command");
			return false;
		}
		
		/* Available formats:
		 *  /shop set buy itemName price stackSize
		 *  /shop set sell itemName price stackSize
		 *  /shop set max itemName amount
		 *  /shop set manager +managerName +managerName -managerName
		 *  /shop set owner ownerName
		 */
		
		Player player = (Player)sender;
		String playerName = player.getName();

		//get the shop the player is currently in
		if( PlayerData.playerShopsList(playerName).size() == 1 ) {
			String shopName = PlayerData.playerShopsList(playerName).get(0);
			Shop shop = ShopData.shops.get(shopName);
			
			if(!isShopController(player, shop) && !canUseCommand(player, "admin".split("")))  {
				player.sendMessage(ChatColor.AQUA + "You must be the shop owner or a manager to set this.");
				player.sendMessage(ChatColor.AQUA + "The current shop owner is " + ChatColor.WHITE + shop.getShopOwner());
				return false;
			}
				
			if( args.length == 1) {
				String[] temp = args.clone();
				args = new String[2];
				args[0] = temp[0];
				args[1] = "empty";
			}
			
			
			if( args[1].equalsIgnoreCase("buy")) {
				//  /shop set buy ItemName Price <bundle size>
				
				ItemStack item = null;
				String itemName = null;
				
				if(args.length == 4 || args.length == 5) {
					int price = 0;
					int bundle = 1;
					
					item = LocalShops.itemList.getShopItem(player, shop, args[2]);
					if(item == null) {
						player.sendMessage(ChatColor.AQUA + "Could not complete command.");
						return false;
					} else {
						int itemData = item.getDurability();
						itemName = LocalShops.itemList.getItemName(item.getTypeId(), itemData);
					}
				
					if(!shop.getItems().contains(itemName)) {
						player.sendMessage(ChatColor.AQUA + "Shop is not yet selling " + ChatColor.WHITE + itemName );
						player.sendMessage(ChatColor.AQUA + "To add the item use " + ChatColor.WHITE + "/shop add");
						return false;
					}
					
					try {
						if(args.length == 4 ) {
							price = Integer.parseInt(args[3]);
							bundle = shop.itemBuyAmount(itemName);
						} else {
							price = Integer.parseInt(args[3]);
							bundle = Integer.parseInt(args[4]);
						}
					} catch(NumberFormatException ex1) {
						player.sendMessage(ChatColor.AQUA + "The price and bundle size must be a number." );
						player.sendMessage(ChatColor.AQUA + "The command format is " + ChatColor.WHITE + "/shop set buy [item name] [price] <bundle size>");
						return false;
					}
					
					shop.setItemBuyPrice(itemName, price);
					shop.setItemBuyAmount(itemName, bundle);
					
					player.sendMessage(ChatColor.AQUA + "The buy information for " + ChatColor.WHITE + itemName + ChatColor.AQUA + " has been updated.");
					player.sendMessage("   " + ChatColor.WHITE + itemName + ChatColor.AQUA + " [" + ChatColor.WHITE 
							+ price + " " + ShopData.currencyName + ChatColor.AQUA + "] [" + ChatColor.WHITE + "Bundle: " 
							+ bundle + ChatColor.AQUA + "]");
				
					ShopData.saveShop(shop);
					
				} else {
					player.sendMessage(ChatColor.AQUA + "The command format is " + ChatColor.WHITE + "/shop set buy [item name] [price] <bundle size>");
					return true;
				}
				
			} else if ( args[1].equalsIgnoreCase("sell")) {
				
				//  /shop set sell ItemName Price <bundle size>
				
				ItemStack item = null;
				String itemName = null;
				
				if(args.length == 4 || args.length == 5) {
					int price = 0;
					int bundle = 1;
					
					item = LocalShops.itemList.getShopItem(player, shop, args[2]);
					if(item == null) {
						player.sendMessage(ChatColor.AQUA + "Could not complete command.");
						return false;
					} else {
						int itemData = item.getDurability();
						itemName = LocalShops.itemList.getItemName(item.getTypeId(), itemData);
					}
				
					if(!shop.getItems().contains(itemName)) {
						player.sendMessage(ChatColor.AQUA + "Shop is not yet buying " + ChatColor.WHITE + itemName );
						player.sendMessage(ChatColor.AQUA + "To add the item use " + ChatColor.WHITE + "/shop add");
						return false;
					}
					
					try {
						if(args.length == 4 ) {
							price = Integer.parseInt(args[3]);
							bundle = shop.itemSellAmount(itemName);
						} else {
							price = Integer.parseInt(args[3]);
							bundle = Integer.parseInt(args[4]);
						}
					} catch(NumberFormatException ex1) {
						player.sendMessage(ChatColor.AQUA + "The price and bundle size must be a number." );
						player.sendMessage(ChatColor.AQUA + "The command format is " + ChatColor.WHITE + "/shop set sell [item name] [price] <bundle size>");
						return false;
					}
					
					
					shop.setItemSellPrice(itemName, price);
					shop.setItemSellAmount(itemName, bundle);
					
					ShopData.saveShop(shop);
					
					player.sendMessage(ChatColor.AQUA + "The sell information for " + ChatColor.WHITE + itemName + ChatColor.AQUA + " has been updated.");
					player.sendMessage("   " + ChatColor.WHITE + itemName + ChatColor.AQUA + " [" + ChatColor.WHITE 
							+ price + " " + ShopData.currencyName + ChatColor.AQUA + "] [" + ChatColor.WHITE + "Bundle: " 
							+ bundle + ChatColor.AQUA + "]");
					
				} else {
					player.sendMessage(ChatColor.AQUA + "The command format is " + ChatColor.WHITE + "/shop set sell [item name] [price] <bundle size>");
					return true;
				}

			} else if ( args[1].equalsIgnoreCase("max")) {
				
				//  /shop set max ItemName amount
				
				ItemStack item = null;
				String itemName = null;
				
				int maxStock = 0;
				
				if(args.length == 4) {
					
					item = LocalShops.itemList.getShopItem(player, shop, args[2]);
					if(item == null) {
						player.sendMessage(ChatColor.AQUA + "Could not complete command.");
						return false;
					} else {
						int itemData = item.getDurability();
						itemName = LocalShops.itemList.getItemName(item.getTypeId(), itemData);
					}
				
					if(!shop.getItems().contains(itemName)) {
						player.sendMessage(ChatColor.AQUA + "Shop is not yet buying " + ChatColor.WHITE + itemName );
						player.sendMessage(ChatColor.AQUA + "To add the item use " + ChatColor.WHITE + "/shop add");
						return false;
					}
					
					try {
						maxStock = Integer.parseInt(args[3]);
					} catch(NumberFormatException ex1) {
						player.sendMessage(ChatColor.AQUA + "The price and bundle size must be a number." );
						player.sendMessage(ChatColor.AQUA + "The command format is " + ChatColor.WHITE + "/shop set sell [item name] [price] <bundle size>");
						return false;
					}
					
					shop.setItemMaxStock(itemName, maxStock);
					
					ShopData.saveShop(shop);
					
					player.sendMessage(ChatColor.AQUA + "The max stock level for " + ChatColor.WHITE + itemName 
							+ ChatColor.AQUA + " has been changed to " + maxStock + ".");
					
					
				} else {
					player.sendMessage(ChatColor.AQUA + "The command format is " + ChatColor.WHITE + "/shop set max [item name] [amount]");
					return true;
				}
				
			} else if ( args[1].equalsIgnoreCase("manager")) {
				String[] managers = shop.getShopManagers();
				if(!shop.getShopOwner().equalsIgnoreCase(player.getName()) && !canUseCommand(player, "admin".split(""))) {
					player.sendMessage(ChatColor.AQUA + "You must be the shop owner to set this.");
					player.sendMessage(ChatColor.AQUA + "The current shop owner is " + ChatColor.WHITE + shop.getShopOwner());
					return false;
				}
				for(String newName: args ) {
					if(newName.equalsIgnoreCase("set") || newName.equalsIgnoreCase("manager")) {
						continue;
					}
					
					String partial = "";
					String[] part = newName.split("\\+");
					if(part == null) continue;
					if(part.length == 2) {
						if(managers != null) {
							for(String name: managers) {
								partial += name + ",";
							}
						}
						partial += part[1];
						managers = partial.split(",");
						
					}
					
					partial = "";
					part = newName.split("\\-");
					if(part.length == 2) {
						if(managers != null) {
							for(String name: managers) {
								if(name.equalsIgnoreCase(part[1])) continue;
								partial += name + ",";
							}
							managers = partial.split(",");
						}
					}
					
				}
				shop.setShopManagers(managers);
				
				
				String msg = "";
				if(shop.getShopManagers() != null) {
					for(String name: shop.getShopManagers()) {
						msg += " " + name;
					}
				}
				
				player.sendMessage(ChatColor.AQUA + "The shop managers have been updated. The current managers are:");
				player.sendMessage("   " + msg.trim());
				
				
			} else if ( args[1].equalsIgnoreCase("owner")) {
				if(args.length == 3) {
					if(!shop.getShopOwner().equalsIgnoreCase(player.getName()) && !canUseCommand(player, "admin".split(""))) {
						player.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "You must be the shop owner to set this.");
						player.sendMessage(ChatColor.AQUA + "  The current shop owner is " + ChatColor.WHITE + shop.getShopOwner());
						return false;
					} else if( !canUseCommand(player, args)) {
						player.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "You do not have permission to do this.");
						return false;
					} else {
						shop.setShopOwner(args[2]);
						player.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "Shop owner changed to " + ChatColor.WHITE + args[2]);
						return true;
					}
				}
			} else if ( args[1].equalsIgnoreCase("unlimited")) {
				if( !canUseCommand(player, "admin".split(""))) {
					player.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "You must be a shop admin to do this.");
					return false;
				} else {
					if( args.length == 3) {
						if(args[2].equalsIgnoreCase("money")) {
							boolean current = shop.isUnlimitedMoney();
							shop.setUnlimitedMoney(!current);
							player.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "Unlimited money was set to " + ChatColor.WHITE + shop.getValueofUnlimitedMoney());
							ShopData.saveShop(shop);
							return true;
						} else if( args[2].equalsIgnoreCase("stock")) {
							boolean current = shop.isUnlimitedStock();
							shop.setUnlimitedStock(!current);
							player.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "Unlimited stock was set to " + ChatColor.WHITE + shop.getValueofUnlimitedStock());
							ShopData.saveShop(shop);
							return true;
						}
					} 
					player.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "The following set commands are available: ");
					player.sendMessage("   " + "/shop set unlimited money");
					player.sendMessage("   " + "/shop set unlimited stock");
					return true;
				}
			} else {
				player.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "The following set commands are available: ");
				player.sendMessage("   " + "/shop set buy [item name] [price] <bundle size>");
				player.sendMessage("   " + "/shop set sell [item name] [price] <bundle size>");
				player.sendMessage("   " + "/shop set max [item name] [max number]");
				player.sendMessage("   " + "/shop set manager +[playername] -[playername2]");
				player.sendMessage("   " + "/shop set owner [player name]");
				if(canUseCommand(player, "admin".split(""))) {
					player.sendMessage("   " + "/shop set unlimited money");
					player.sendMessage("   " + "/shop set unlimited stock");
				}
			}

			ShopData.saveShop(shop);

		} else {
			player.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "You must be inside a shop to use /shop " + args[0]);
			return false;
		}
			
		return true;
	}
	
	
	/**
	 * Processes remove command.  Removes item from shop and returns stock to player.
	 *  
	 * @param sender
	 * @param args
	 * @return
	 *   true - if command succeeds
	 *   false otherwise
	 */
	public static boolean removeItemShop(CommandSender sender, String[] args) {
		if(!(sender instanceof Player) || !canUseCommand(sender, args)) {
			sender.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "You don't have permission to use this command");
			return false;
		}
		
		/* Available formats:
		 *  /shop remove itemName
		 */
		
		Player player = (Player)sender;
		String playerName = player.getName();

		//get the shop the player is currently in
		if( PlayerData.playerShopsList(playerName).size() == 1 ) {
			String shopName = PlayerData.playerShopsList(playerName).get(0);
			Shop shop = ShopData.shops.get(shopName);
			
			if(!isShopController(player, shop) && !canUseCommand(player, "admin".split("")))  {
				player.sendMessage(ChatColor.AQUA + "You must be the shop owner or a manager to remove an item.");
				player.sendMessage(ChatColor.AQUA + "The current shop owner is " + ChatColor.WHITE + shop.getShopOwner());
				return false;
			}

			if( args.length != 2) {
				player.sendMessage(ChatColor.AQUA + "The command format is " + ChatColor.WHITE + "/shop remove [item name]");
				return false;
			}
			
			ItemStack item = LocalShops.itemList.getShopItem(player, shop, args[1]);
			String itemName;
			
			if(item == null) {
				player.sendMessage(ChatColor.AQUA + "Could not complete command.");
				return false;
			} else {
				int itemData = item.getDurability();
				itemName = LocalShops.itemList.getItemName(item.getTypeId(), itemData);
			}
			
			if(!shop.getItems().contains(itemName)) {
				player.sendMessage(ChatColor.AQUA + "The shop is not selling " + ChatColor.WHITE + itemName);
				return true;
			}		

			
			player.sendMessage(ChatColor.WHITE + itemName + ChatColor.AQUA + " removed from the shop. " );
			if(!shop.isUnlimitedStock()) {
				int amount = shop.getItemStock(itemName);
				
				//log the transaction
				ShopData.logItems(playerName, shopName, "remove-item", itemName, amount, amount, 0 );
				
				givePlayerItem(player, item, amount);
				player.sendMessage("" + ChatColor.WHITE + amount + ChatColor.AQUA + " have been returned to your inventory"); 
			}

			shop.removeItem(itemName);
			ShopData.saveShop(shop);

		} else {
			player.sendMessage(ChatColor.AQUA + "You must be inside a shop to use /shop " + args[0]);
		}
			
		return true;
	}
	
	/**
	 * Destroys current shop.  Deleting file and removing from tree.
	 *  
	 * @param sender
	 * @param args
	 * @return
	 *   true - if command succeeds
	 *   false otherwise
	 */
	public static boolean destroyShop(CommandSender sender, String[] args) {
		if(!(sender instanceof Player) || !canUseCommand(sender, args)) {
			sender.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "You don't have permission to use this command");
			return false;
		}
		
		/* Available formats:
		 *  /shop destroy
		 */
		
		Player player = (Player)sender;
		String playerName = player.getName();

		//get the shop the player is currently in
		if( PlayerData.playerShopsList(playerName).size() == 1 ) {
			String shopName = PlayerData.playerShopsList(playerName).get(0);
			Shop shop = ShopData.shops.get(shopName);
			
			if(!shop.getShopOwner().equalsIgnoreCase(player.getName()) && !canUseCommand(player, "admin".split(""))) {
				player.sendMessage(ChatColor.AQUA + "You must be the shop owner to destroy it.");
				return false;
			}
			
			sender.sendMessage(PlayerData.chatPrefix + ChatColor.WHITE 
					+ shop.getShopName() + ChatColor.AQUA + " has been destroyed");
			ShopData.deleteShop(shop);
			
			

		} else {
			player.sendMessage(ChatColor.AQUA + "You must be inside a shop to use /shop " + args[0]);
		}
			
		return true;
	}
	
	public static int countItemsinInventory( PlayerInventory inventory, ItemStack item) {
		int totalAmount = 0;
		boolean isDurable = LocalShops.itemList.isDurable(item);
		
		for(Integer i : inventory.all(item.getType()).keySet()) {
			ItemStack thisStack = inventory.getItem(i);
			if(isDurable) {
				int damage = calcDurabilityPercentage(thisStack);
				if(damage > ShopData.maxDamage && ShopData.maxDamage != 0) continue;
			} else {
				if( thisStack.getDurability() != item.getDurability()) continue;
			}
			totalAmount += thisStack.getAmount();
		}

		return totalAmount;
	}
	
	private static int removeItemsFromInventory(PlayerInventory inventory,
			ItemStack item, int amount) {
		
		boolean isDurable = LocalShops.itemList.isDurable(item);
		
		//remove number of items from player adding stock
		for(int i: inventory.all(item.getType()).keySet()) {
			if( amount == 0 ) continue;
			ItemStack thisStack = inventory.getItem(i);
			if(isDurable) {
				int damage = calcDurabilityPercentage(thisStack);
				if(damage > ShopData.maxDamage && ShopData.maxDamage != 0) continue;
			} else {
				if( thisStack.getDurability() != item.getDurability()) continue;
			}
			
			int foundAmount = thisStack.getAmount();
			if( amount >= foundAmount ) {
				amount -= foundAmount;
				inventory.setItem(i, null);
			} else {
				thisStack.setAmount(foundAmount - amount);
				inventory.setItem(i, thisStack);
				amount = 0;
			}
		}
		
		return amount;
		
	}
	
	private static int calcDurabilityPercentage(ItemStack item) {
		
		//calc durability prcnt
		short damage;
		if(item.getType() == Material.IRON_SWORD) {
			damage = (short)((double)item.getDurability()/250*100);
		} else {
			damage = (short)((double)item.getDurability()/(double)item.getType().getMaxDurability()*100);
		}
		
		return damage;
	}
	
	private static void givePlayerItem(Player player, ItemStack item, int amount) {
		int maxStackSize = 64;
				
		//fill all the existing stacks first
		for(int i: player.getInventory().all(item.getType()).keySet()) {
			if( amount == 0 ) continue;
			ItemStack thisStack = player.getInventory().getItem(i);
			if( thisStack.getType().equals(item.getType()) && thisStack.getDurability() == item.getDurability()) {
				if( thisStack.getAmount() < maxStackSize ) {
					int remainder = maxStackSize - thisStack.getAmount();
					if(remainder <= amount) {
						amount -= remainder;
						thisStack.setAmount(maxStackSize); 
					} else {
						thisStack.setAmount(maxStackSize - remainder + amount);
						amount = 0;
					}
				} 
			}
			
		}
		
		for(int i = 0; i < 36; i++ ) {
			ItemStack thisSlot = player.getInventory().getItem(i);
			if(thisSlot == null || thisSlot.getType() == Material.AIR) {
				if (amount == 0) continue;
				if(amount >= maxStackSize) {
					item.setAmount(maxStackSize);
					player.getInventory().setItem(i, item);
					amount -= maxStackSize; 
				} else {
					item.setAmount(amount);
					player.getInventory().setItem(i, item);
					amount = 0;
				}
			}
		}
		
		
		
		while( amount > 0 ) {
			if( amount >= maxStackSize ) {
				item.setAmount(maxStackSize);
				amount -= maxStackSize;
			} else {
				item.setAmount(amount - maxStackSize);
				amount = 0;
			}
			player.getWorld().dropItemNaturally(player.getLocation(), item);
		}
		
	}
}
