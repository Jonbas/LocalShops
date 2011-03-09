package net.centerleft.localshops;

import java.util.ArrayList;
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
			
			long x = (long)location.getX();
			long y = (long)location.getY();
			long z = (long)location.getZ();
			
			String shopName = args[1];
			
			Shop thisShop = new Shop();
			
			thisShop.setLocation(x, y, z);
			thisShop.setShopCreator(player.getName());
			thisShop.setShopOwner(player.getName());
			thisShop.setShopName(shopName);
			thisShop.setWorldName(player.getWorld().getName());
			
			//setup the cuboid for the tree
			long[] xyzA = new long[3];
			long[] xyzB = new long[3];
			
			xyzA[0] = x - (ShopData.shopSize / 2);
			xyzB[0] = x + (ShopData.shopSize / 2);
			xyzA[2] = z - (ShopData.shopSize / 2);
			xyzB[2] = z + (ShopData.shopSize / 2);
			
			xyzA[1] = y - 1;
			xyzB[1] = y + ShopData.shopHeight - 1;
			
			//need to check to see if the shop overlaps another shop
			if( shopPositionOk(  player, xyzA, xyzB )) {
				
				PrimitiveCuboid tempShopCuboid = new PrimitiveCuboid( xyzA, xyzB );
				tempShopCuboid.name = shopName;
				//insert the shop into the world
				LocalShops.cuboidTree.insert(tempShopCuboid);
				ShopData.shops.put(shopName, thisShop );
				
				if(ShopData.chargeForShop) {
					String[] freeShop = {"freeshop"};
					if(!canUseCommand(sender, freeShop)) {
						if(!PlayerData.chargePlayer(player.getName(), ShopData.shopCost)) {
							player.sendMessage( PlayerData.chatPrefix + ChatColor.AQUA + "You need " + ShopData.shopCost + " " + ShopData.currencyName + " to create a shop.");
							return false;
						}
					}
				}
				
				//write the file
				if( ShopData.saveShop(thisShop) ) { 

					player.sendMessage( PlayerData.chatPrefix + ChatColor.WHITE + shopName + ChatColor.AQUA + " was created succesfully.");
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
	
	static boolean canUseCommand( CommandSender sender, String[] args ) {
		boolean useManager = ShopsPluginListener.usePermissions;
		PermissionHandler pm = ShopsPluginListener.gmPermissionCheck;
		
		if(!(sender instanceof Player)) return false;
		
		Player player = (Player)sender;
		
		if(args.length >= 1) {
			if(args[0].equalsIgnoreCase("create")) {
				if(useManager) {
					return pm.has(player, "localshops.create");
				} else if ( sender.isOp() ) {
					return true;
				}
			} else if(args[0].equalsIgnoreCase("freeshop")) {
				if(useManager) {
					return pm.has(player, "localshops.create.free");
				} else if ( sender.isOp() ) {
					return true;
				}
			} else if(args[0].equalsIgnoreCase("destroy")) {
				if(useManager) {
					return pm.has(player, "localshops.destroy");
				} else if ( sender.isOp() ) {
					return true;
				}
				
			} else if(args[0].equalsIgnoreCase("reload")) {
				if(useManager) {
					return pm.has(player, "localshops.reload");
				} else if ( sender.isOp() ) {
					return true;
				}
				
			} else if(args[0].equalsIgnoreCase("sell") || args[0].equalsIgnoreCase("buy") 
					|| args[0].equalsIgnoreCase("list")) {
				if(useManager) {
					return pm.has(player, "localshops.buysell");
				} else {
					return true;
				}
			} else if(args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove") 
					|| args[0].equalsIgnoreCase("set")) {
				if(useManager) {
					return pm.has(player, "localshops.manage");
				} else {
					return true;
				}
			}
				
		}
		return true;
	}

	public static void printHelp(CommandSender sender, String[] args) {
		sender.sendMessage( PlayerData.chatPrefix + ChatColor.AQUA + "Here are the available commands [required] <optional>" );

		String[] sell = { "sell" };
		if(canUseCommand(sender, sell)) {
			sender.sendMessage( ChatColor.WHITE + "   /shop list <buy|sell> " + ChatColor.AQUA + "- List the shop's inventory." );
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
		
		res = LocalShops.cuboidTree.relatedSearch(res.bookmark, xyzA[0], xyzA[1], xyzA[2] );
		if( shopOverlaps(player, res) ) return false;
		
		res = LocalShops.cuboidTree.relatedSearch(res.bookmark, xyzA[0], xyzA[1], xyzB[2] );
		if( shopOverlaps(player, res) ) return false;
		res = LocalShops.cuboidTree.relatedSearch(res.bookmark, xyzA[0], xyzB[1], xyzA[2] );
		if( shopOverlaps(player, res) ) return false;
		res = LocalShops.cuboidTree.relatedSearch(res.bookmark, xyzA[0], xyzB[1], xyzB[2] );
		if( shopOverlaps(player, res) ) return false;
		res = LocalShops.cuboidTree.relatedSearch(res.bookmark, xyzB[0], xyzA[1], xyzA[2] );
		if( shopOverlaps(player, res) ) return false;
		res = LocalShops.cuboidTree.relatedSearch(res.bookmark, xyzB[0], xyzA[1], xyzB[2] );
		if( shopOverlaps(player, res) ) return false;
		res = LocalShops.cuboidTree.relatedSearch(res.bookmark, xyzB[0], xyzB[1], xyzA[2] );
		if( shopOverlaps(player, res) ) return false;
		res = LocalShops.cuboidTree.relatedSearch(res.bookmark, xyzB[0], xyzB[1], xyzB[2] );
		if( shopOverlaps(player, res) ) return false;
		return true;
	}
	
	private static boolean shopOverlaps( Player player, BookmarkedResult res ) {
		if( res.results.size() != 0 ) {
			for( PrimitiveCuboid shop : res.results) {
				if(shop.name != null) {
					player.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "Could not create shop, it overlaps with " + ChatColor.WHITE 
							+ shop.name );
					return true;
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
				}
				if( stack > 1 ) {
					subMessage += ChatColor.AQUA + " [" + ChatColor.WHITE + "Bundle: " + stack + ChatColor.AQUA + "]";
				}
			}
			//get stock
			int stock = shop.getItemStock(item);
			if(buy) {
				if(stock == 0) continue;
			}
			subMessage += ChatColor.AQUA + " [" + ChatColor.WHITE + "Stock: " + stock + ChatColor.AQUA + "]";
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
				ChatColor.WHITE + "/shop " + buySell + " ItemName [ammount]";
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
		
		Player player = (Player)sender;
		String playerName = player.getName();

		//get the shop the player is currently in
		if( PlayerData.playerShopsList(playerName).size() == 1 ) {
			String shopName = PlayerData.playerShopsList(playerName).get(0);
			Shop shop = ShopData.shops.get(shopName);
			
			ItemStack item = null;
			String itemName = null;
			int amount = 0;
			
			if(args.length == 1 || args.length == 2) {
			
				item = player.getInventory().getItemInHand();
				if( item == null || item.getType().getId() == Material.AIR.getId()) {
					return true;
				}
	

				if( item.getData() != null) {
					itemName = LocalShops.itemList.getItemName(item.getType().getId(), (int)item.getDurability());
				} else {
					itemName = LocalShops.itemList.getItemName(item.getType().getId()).get(0);
				}
				
				amount = item.getAmount() - (item.getAmount()%shop.itemSellAmount(itemName));
				if(args.length == 2) {
					int totalAmount = 0;
					for(Integer i : player.getInventory().all(item.getType()).keySet()) {
						if(player.getInventory().getItem(i).getDurability() != item.getDurability()) continue;
						totalAmount += player.getInventory().getItem(i).getAmount();
					}
					try {
						int numberToRemove = Integer.parseInt(args[1]);
						if( numberToRemove > totalAmount) {
							amount = totalAmount - (totalAmount%shop.itemSellAmount(itemName));
						} else {
							amount = numberToRemove - (numberToRemove%shop.itemSellAmount(itemName));
						}
					} catch ( NumberFormatException ex1 ) {
						if(args[1].equalsIgnoreCase("all")) {
							amount = totalAmount - (totalAmount%shop.itemSellAmount(itemName));
						} else {
							player.sendMessage(ChatColor.AQUA + "Input problem. The format is " + ChatColor.WHITE + "/shop sell <# to sell>");
							return false;							
						}
					}
				}
				
			}
			
			if(args.length == 3) {
				item = LocalShops.itemList.getItem(player, args[1]);
				if(item == null) {
					player.sendMessage(ChatColor.AQUA + "Could not complete the sale.");
					return false;
				}
				
				int totalAmount = 0;
				for(Integer i : player.getInventory().all(item.getType()).keySet()) {
					if(player.getInventory().getItem(i).getDurability() != item.getDurability()) continue;
					totalAmount += player.getInventory().getItem(i).getAmount();
				}
				
				if(item.getData() != null) {
					itemName = LocalShops.itemList.getItemName(item.getType().getId(), (int)item.getDurability());
				} else {
					itemName = LocalShops.itemList.getItemName(item.getType().getId()).get(0);
				}
				
				try {
					int numberToRemove = Integer.parseInt(args[2]);
					if( numberToRemove > totalAmount) {
						amount = totalAmount - (totalAmount%shop.itemSellAmount(itemName));
					} else {
						amount = numberToRemove - (numberToRemove%shop.itemSellAmount(itemName));
					}
				} catch (NumberFormatException ex2 ) {
					if( args[2].equalsIgnoreCase("all")) {
						amount = totalAmount - (totalAmount%shop.itemSellAmount(itemName));
					} else {
						player.sendMessage(ChatColor.AQUA + "Input problem. The format is " + ChatColor.WHITE + "/shop sell <itemName> <# to sell>");
						return false;
					}
				}
				
			}
			
			
			//check if the shop is buying that item
			if(!shop.getItems().contains(itemName) || shop.getItemSellPrice(itemName) == 0) {
				player.sendMessage(ChatColor.AQUA + "Sorry, " + ChatColor.WHITE + shopName 
						+ ChatColor.AQUA + " is not buying " + ChatColor.WHITE + itemName 
						+ ChatColor.AQUA + " right now." );
				return false;
			}
			
			//calculate cost
			int bundles = amount / shop.itemSellAmount(itemName);
			int itemPrice = shop.getItemSellPrice(itemName);
			//recalculate # of items since may not fit cleanly into bundles
			amount = bundles * shop.itemSellAmount(itemName);
			int totalCost = bundles * itemPrice;
			
			if(shop.isUnlimited()) {
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
						if(!PlayerData.payPlayer(shop.getShopOwner(), playerName, totalCost)) {
							player.sendMessage(ChatColor.AQUA + "Unexpected money problem: could not complete sale.");
							return false;
						}
					}
				}
			}
			
			shop.addStock(itemName, amount);
			
			if(isShopController(player, shop )) {
				player.sendMessage(ChatColor.AQUA + "You added " + ChatColor.WHITE +  amount + " " 
						+ itemName + ChatColor.AQUA +" to the shop"); 				
			} else {
				player.sendMessage(ChatColor.AQUA + "You sold " + ChatColor.WHITE +  amount + " " 
						+ itemName + ChatColor.AQUA + " and gained " + ChatColor.WHITE + totalCost 
						+ " " + ShopData.currencyName); 
			}
			
			//remove number of items from seller
			for(int i: player.getInventory().all(item.getType()).keySet()) {
				if( amount == 0 ) continue;
				ItemStack thisStack = player.getInventory().getItem(i);
				int foundAmount = thisStack.getAmount();
				if( amount >= foundAmount ) {
					amount -= foundAmount;
					player.getInventory().setItem(i, null);
				} else {
					int amountInStack = player.getInventory().getItem(i).getAmount();
					thisStack.setAmount(amountInStack - amount);
					player.getInventory().setItem(i, thisStack);
					amount = 0;
				}
				
			}
			

			ShopData.saveShop(shop);

		} else {
			player.sendMessage(ChatColor.AQUA + "You must be inside a shop to use /shop " + args[0]);
		}
			
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
		
		/* Available formats:
		 *  /shop add
		 *  /shop add #
		 *  /shop add all
		 *  /shop add item #
		 *  /shop add item all
		 */
		
		Player player = (Player)sender;
		String playerName = player.getName();

		//get the shop the player is currently in
		if( PlayerData.playerShopsList(playerName).size() == 1 ) {
			String shopName = PlayerData.playerShopsList(playerName).get(0);
			Shop shop = ShopData.shops.get(shopName);
			
			if(!isShopController(player, shop))  {
				player.sendMessage(ChatColor.AQUA + "You must be the shop owner or a manager to add items.");
				player.sendMessage(ChatColor.AQUA + "The current shop owner is " + ChatColor.WHITE + shop.getShopOwner());
				return false;
			}
			
			ItemStack item = null;
			String itemName = null;
			int amount = 0;
			
			if(args.length == 1 || args.length == 2) {
			
				item = player.getInventory().getItemInHand();
				if( item == null || item.getType().getId() == Material.AIR.getId()) {
					sender.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "To add an item to the shop hold it in your hand.");
					return true;
				}
	

				if( item.getData() != null) {
//TODO this is a workaround for a bukkit bug.  Check to make sure this still works with new versions.
					itemName = LocalShops.itemList.getItemName(item.getType().getId(), (int)item.getDurability());
				} else {
					itemName = LocalShops.itemList.getItemName(item.getType().getId()).get(0);
				}
				
				amount = item.getAmount();
				if(args.length == 2) {
					int totalAmount = 0;
					for(Integer i : player.getInventory().all(item.getType()).keySet()) {
						if(player.getInventory().getItem(i).getDurability() != item.getDurability()) continue;
						totalAmount += player.getInventory().getItem(i).getAmount();
					}
					try {
						int numberToRemove = Integer.parseInt(args[1]);
						if( numberToRemove > totalAmount) {
							amount = totalAmount;
						} else {
							amount = numberToRemove;
						}
					} catch ( NumberFormatException ex1 ) {
						if(args[1].equalsIgnoreCase("all")) {
							amount = totalAmount;
						} else {
							player.sendMessage(ChatColor.AQUA + "Input problem. The format is " + ChatColor.WHITE + "/shop sell <# to sell>");
							return false;							
						}
					}
				}
				
			}
			
			if(args.length == 3) {
				item = LocalShops.itemList.getItem(player, args[1]);
				if(item == null) {
					player.sendMessage(ChatColor.AQUA + "Could not add the item to shop.");
					return false;
				}
				
				int totalAmount = 0;
				for(Integer i : player.getInventory().all(item.getType()).keySet()) {
					if(player.getInventory().getItem(i).getDurability() != item.getDurability()) continue;
					totalAmount += player.getInventory().getItem(i).getAmount();
				}
				
				if(item.getData() != null) {
					
//TODO bukkit glitch work arround for data.  check if this still works later
					itemName = LocalShops.itemList.getItemName(item.getType().getId(), (int)item.getDurability());
				} else {
					itemName = LocalShops.itemList.getItemName(item.getType().getId()).get(0);
				}
				
				try {
					int numberToRemove = Integer.parseInt(args[2]);
					if( numberToRemove > totalAmount) {
						amount = totalAmount;
					} else {
						amount = numberToRemove;
					}
				} catch (NumberFormatException ex2 ) {
					if( args[2].equalsIgnoreCase("all")) {
						amount = totalAmount;
					} else {
						player.sendMessage(ChatColor.AQUA + "Input problem. The format is " + ChatColor.WHITE + "/shop add <itemName> <# to sell>");
						return false;
					}
				}
				
			}
			
			
			//check if the shop is buying that item
			if(!shop.getItems().contains(itemName)) {
				int itemInfo[] = LocalShops.itemList.getItemInfo(player, itemName);
				if( itemInfo == null ) {
					player.sendMessage(ChatColor.AQUA + "Could not add the item to shop.");
					return false;
				}
				shop.addItem(itemInfo[0], itemInfo[1], 0, 1, 0, 1, 0);
			}
			
			shop.addStock(itemName, amount);
			player.sendMessage(ChatColor.AQUA + "Succesfully added " + ChatColor.WHITE + itemName 
					+ ChatColor.AQUA + " to the shop. Stock is now " + ChatColor.WHITE + shop.getItemStock(itemName));
			
			//remove number of items from player adding stock
			for(int i: player.getInventory().all(item.getType()).keySet()) {
				if( amount == 0 ) continue;
				ItemStack thisStack = player.getInventory().getItem(i);
				if( thisStack.getDurability() != item.getDurability()) continue;
				int foundAmount = thisStack.getAmount();
				if( amount >= foundAmount ) {
					amount -= foundAmount;
					player.getInventory().setItem(i, null);
				} else {
					int amountInStack = player.getInventory().getItem(i).getAmount();
					thisStack.setAmount(amountInStack - amount);
					player.getInventory().setItem(i, thisStack);
					amount = 0;
				}
			}
			

			ShopData.saveShop(shop);

		} else {
			player.sendMessage(ChatColor.AQUA + "You must be inside a shop to use /shop " + args[0]);
		}
		
		
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
				item = LocalShops.itemList.getItem(player, args[1]);
				if(item == null) {
					player.sendMessage(ChatColor.AQUA + "Could not complete the purchase.");
					return false;
				} else {
					int itemData = 0;
//TODO bukkit data glitch fix.  Check if it still works
					if( item.getData() != null) itemData = item.getDurability();
					itemName = LocalShops.itemList.getItemName(item.getTypeId(), itemData);
				}
				
				//check if the shop is selling that item
				if(!shop.getItems().contains(itemName) || shop.getItemBuyPrice(itemName) == 0) {
					player.sendMessage(ChatColor.AQUA + "Sorry, " + ChatColor.WHITE + shopName 
							+ ChatColor.AQUA + " is not selling " + ChatColor.WHITE + itemName 
							+ ChatColor.AQUA + " right now." );
					return false;
				}
				
				int totalAmount = shop.getItemStock(itemName);

				try {
					int numberToRemove = Integer.parseInt(args[2]);
					if( numberToRemove > totalAmount) {
						amount = totalAmount - (totalAmount%shop.itemBuyAmount(itemName));
					} else {
						amount = numberToRemove - (numberToRemove%shop.itemBuyAmount(itemName));
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
			
			if(amount > freeSpots) amount = freeSpots;
			
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
					if(!PlayerData.payPlayer( playerName, shop.getShopOwner(), totalCost)) {
						player.sendMessage(ChatColor.AQUA + "Unexpected money problem: could not complete sale.");
						return false;
					}
				}
			}
			
			shop.removeStock(itemName, amount);
			if(isShopController(player, shop)) {
				player.sendMessage(ChatColor.AQUA + "You removed " + ChatColor.WHITE +  amount + " " 
						+ itemName + ChatColor.AQUA + " from the shop"); 
			} else {
				player.sendMessage(ChatColor.AQUA + "You purchased " + ChatColor.WHITE +  amount + " " 
						+ itemName + ChatColor.AQUA + " for " + ChatColor.WHITE + totalCost + " " + ShopData.currencyName); 
			}
			//add number of items to the buyer
			//Start by searching the inventory for any stacks that match the item we have
			for(int i: player.getInventory().all(item.getType()).keySet()) {
				if( amount == 0 ) continue;
				ItemStack thisStack = player.getInventory().getItem(i);
				if( thisStack.getType().equals(item.getType()) && thisStack.getDurability() == item.getDurability()) {
					if( thisStack.getAmount() < 64 ) {
						int remainder = 64 - thisStack.getAmount();
						if(remainder <= amount) {
							amount -= remainder;
							thisStack.setAmount(64); 
						} else {
							thisStack.setAmount(64 - remainder + amount);
							amount = 0;
						}
					} 
				}
				
			}
			
			while( amount > 0 ) {
				int nextEmpty = player.getInventory().firstEmpty();
				if( nextEmpty >= 0 && nextEmpty < player.getInventory().getSize()) {
					if( amount >= 64 ) {
						player.getInventory().setItem(nextEmpty, new ItemStack(item.getType(), 64));
						player.getInventory().getItem(nextEmpty).setDurability(item.getDurability());
						amount -= 64;
					} else {
						player.getInventory().setItem(nextEmpty, new ItemStack(item.getType(), amount));
						player.getInventory().getItem(nextEmpty).setDurability(item.getDurability());
						amount = 0;
					}
				} else {
					continue;
				}
			}
			

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
		 *  /shop set manager +managerName +managerName -managerName
		 *  /shop set owner ownerName
		 */
		
		Player player = (Player)sender;
		String playerName = player.getName();

		//get the shop the player is currently in
		if( PlayerData.playerShopsList(playerName).size() == 1 ) {
			String shopName = PlayerData.playerShopsList(playerName).get(0);
			Shop shop = ShopData.shops.get(shopName);
			
			if(!isShopController(player, shop))  {
				player.sendMessage(ChatColor.AQUA + "You must be the shop owner or a manager to set this.");
				player.sendMessage(ChatColor.AQUA + "The current shop owner is " + ChatColor.WHITE + shop.getShopOwner());
				return false;
			}
				
			if( args.length == 1) {
				//TODO print /shop set help message
				return true;
			}
			
			
			if( args[1].equalsIgnoreCase("buy")) {
				//  /shop set buy ItemName Price <bundle size>
				
				ItemStack item = null;
				String itemName = null;
				
				if(args.length == 4 || args.length == 5) {
					int price = 0;
					int bundle = 1;
					
					item = LocalShops.itemList.getItem(player, args[2]);
					if(item == null) {
						player.sendMessage(ChatColor.AQUA + "Could not complete command.");
						return false;
					} else {
						int itemData = 0;
						if( item.getData() != null) itemData = item.getDurability();
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
					
					item = LocalShops.itemList.getItem(player, args[2]);
					if(item == null) {
						player.sendMessage(ChatColor.AQUA + "Could not complete command.");
						return false;
					} else {
						int itemData = 0;
						if( item.getData() != null) itemData = item.getDurability();
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

				
			} else if ( args[1].equalsIgnoreCase("manager")) {
				String[] managers = shop.getShopManagers();
				if(!shop.getShopOwner().equalsIgnoreCase(player.getName())) {
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
						if(managers == null) {
							partial += part[1] + ",";
						} else {
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
						for(String name: managers) {
							if(name.equalsIgnoreCase(part[1])) continue;
							partial += name + ",";
						}
						managers = partial.split(",");
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
				if(!shop.getShopOwner().equalsIgnoreCase(player.getName())) {
					player.sendMessage(ChatColor.AQUA + "You must be the shop owner to set this.");
					player.sendMessage(ChatColor.AQUA + "The current shop owner is " + ChatColor.WHITE + shop.getShopOwner());
					return false;
				}
					shop.setShopOwner(args[2]);
				}
			} else {
				player.sendMessage(PlayerData.chatPrefix + ChatColor.AQUA + "The following set commands are available: ");
				player.sendMessage("   " + "/shop set buy [item name] [price] <bundle size>");
				player.sendMessage("   " + "/shop set sell [item name] [price] <bundle size>");
				player.sendMessage("   " + "/shop set manager +[playername] -[playername2]");
				player.sendMessage("   " + "/shop set owner [player name]");
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
			
			if(!isShopController(player, shop))  {
				player.sendMessage(ChatColor.AQUA + "You must be the shop owner or a manager to remove an item.");
				player.sendMessage(ChatColor.AQUA + "The current shop owner is " + ChatColor.WHITE + shop.getShopOwner());
				return false;
			}

			if( args.length != 2) {
				player.sendMessage(ChatColor.AQUA + "The command format is " + ChatColor.WHITE + "/shop remove [item name]");
				return false;
			}
			
			ItemStack item = LocalShops.itemList.getItem(player, args[1]);
			String itemName;
			
			if(item == null) {
				player.sendMessage(ChatColor.AQUA + "Could not complete command.");
				return false;
			} else {
				int itemData = 0;
				if( item.getData() != null) itemData = item.getDurability();
				itemName = LocalShops.itemList.getItemName(item.getTypeId(), itemData);
			}
			
			if(!shop.getItems().contains(itemName)) {
				player.sendMessage(ChatColor.AQUA + "The shop is not selling " + ChatColor.WHITE + itemName);
				return true;
			}
			
			int amount = shop.getItemStock(itemName);
			
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
			
			player.sendMessage(ChatColor.WHITE + itemName + ChatColor.AQUA + " removed from the shop. " );
			player.sendMessage("" + ChatColor.WHITE + amount + ChatColor.AQUA + " have been returned to your inventory"); 
			
			while(amount > freeSpots) {
				if((amount - freeSpots) >= 64 ) {
					item.setAmount(64);
					amount -= 64;
				} else {
					item.setAmount(amount - freeSpots);
					amount = freeSpots;
				}
				player.getWorld().dropItemNaturally(player.getLocation(), item);
				
			}
			shop.removeItem(itemName);
			
			player.sendMessage(ChatColor.WHITE + itemName + ChatColor.AQUA + " removed from the shop. " );
			player.sendMessage("" + ChatColor.WHITE + amount + ChatColor.AQUA + " have been returned to your inventory"); 
			
			//add number of items to the buyer
			//Start by searching the inventory for any stacks that match the item we have
			for(int i: player.getInventory().all(item.getType()).keySet()) {
				if( amount == 0 ) continue;
				ItemStack thisStack = player.getInventory().getItem(i);
				if( thisStack.getType().equals(item.getType()) && thisStack.getDurability() == item.getDurability()) {
					if( thisStack.getAmount() < 64 ) {
						int remainder = 64 - thisStack.getAmount();
						if(remainder <= amount) {
							amount -= remainder;
							thisStack.setAmount(64); 
						} else {
							thisStack.setAmount(64 - remainder + amount);
							amount = 0;
						}
					} 
				}
				
			}
			
			while( amount > 0 ) {
				int nextEmpty = player.getInventory().firstEmpty();
				if( nextEmpty >= 0 && nextEmpty < player.getInventory().getSize()) {
					if( amount >= 64 ) {
						player.getInventory().setItem(nextEmpty, new ItemStack(item.getType(), 64));
						player.getInventory().getItem(nextEmpty).setDurability(item.getDurability());
						amount -= 64;
					} else {
						player.getInventory().setItem(nextEmpty, new ItemStack(item.getType(), amount));
						player.getInventory().getItem(nextEmpty).setDurability(item.getDurability());
						amount = 0;
					}
				} else {
					continue;
				}
			}
			

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
		 *  /shop remove itemName
		 */
		
		Player player = (Player)sender;
		String playerName = player.getName();

		//get the shop the player is currently in
		if( PlayerData.playerShopsList(playerName).size() == 1 ) {
			String shopName = PlayerData.playerShopsList(playerName).get(0);
			Shop shop = ShopData.shops.get(shopName);
			
			if(!shop.getShopOwner().equalsIgnoreCase(player.getName())) {
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
}
