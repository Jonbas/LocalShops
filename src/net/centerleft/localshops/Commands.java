package net.centerleft.localshops;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cuboidLocale.BookmarkedResult;
import cuboidLocale.PrimitiveCuboid;

public class Commands {
	static void createShop( CommandSender sender, String[] args ) {
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
				
				//write the file
				if( ShopData.saveShop(thisShop) ) { 
					player.sendMessage( PlayerData.chatPrefix + ChatColor.WHITE + shopName + ChatColor.AQUA + " was created succesfully.");
				} else {
					player.sendMessage( PlayerData.chatPrefix + ChatColor.AQUA + "There was an error, could not create shop.");
				}
			}
		} 
	}
	
	static boolean canUseCommand( CommandSender sender, String[] args ) {
		//TODO add control tests
		return true;
	}

	public static void printHelp(CommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		sender.sendMessage( PlayerData.chatPrefix + ChatColor.AQUA + "Looks like you need some help?");
		
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
		
		// TODO This needs a command for adding page #'s to the shop list
		// so that long pages will wrap to second page
		
		if(canUseCommand(sender, args) && (sender instanceof Player)) {
			Player player = (Player)sender;
			String playerName = player.getName();
			String inShopName;
		
			//get the shop the player is currently in
			if( PlayerData.playerShopList.get(playerName).size() == 1 ) {
				if( args.length > 1 ) {
					if( args[1].equalsIgnoreCase("buy")) {
						inShopName = PlayerData.playerShopList.get(playerName).get(0);
						
						Shop shop = ShopData.shops.get(inShopName);
						
						ArrayList<String> shopItems = shop.getItems();
						
						//TODO Finish this
						player.sendMessage(ChatColor.AQUA + "The shop " + ChatColor.WHITE 
								+ inShopName + ChatColor.AQUA + " is selling:");
						for(String item: shopItems ) {
							String message = "   " + item;
							//get buy price
							int price = shop.getItemBuyPrice(item);
							if(price == 0) continue;
							message += ChatColor.AQUA + " [" + ChatColor.WHITE + price + " " + ShopData.currencyName 
								+ ChatColor.AQUA + "]";
							//get stack size
							int stack = shop.itemBuyAmount(item);
							if( stack > 1 ) {
								message += ChatColor.AQUA + " [" + ChatColor.WHITE + "Bundle: " + stack + ChatColor.AQUA + "]";
							}
							//get stock
							int stock = shop.getItemStock(item);
							message += ChatColor.AQUA + " [" + ChatColor.WHITE + "Stock: " + stock + ChatColor.AQUA + "]";
							player.sendMessage(message);
						}
						player.sendMessage(ChatColor.AQUA + "To buy an item on the list type: " 
								+ ChatColor.WHITE + "/shop buy ItemName [ammount]");
					}
					if( args[1].equalsIgnoreCase("sell")) {
						inShopName = PlayerData.playerShopList.get(playerName).get(0);
						
						Shop shop = ShopData.shops.get(inShopName);
						
						ArrayList<String> shopItems = shop.getItems();
						
						//TODO Finish this
						player.sendMessage(ChatColor.AQUA + "The shop " + ChatColor.WHITE 
								+ inShopName + ChatColor.AQUA + " is buying:");
						for(String item: shopItems ) {
							String message = "   " + item;
							//get buy price
							int price = shop.getItemSellPrice(item);
							if(price == 0) continue;
							message += ChatColor.AQUA + " [" + ChatColor.WHITE + price + " " + ShopData.currencyName 
								+ ChatColor.AQUA + "]";
							//get stack size
							int stack = shop.itemSellAmount(item);
							if( stack > 1 ) {
								message += ChatColor.AQUA + " [" + ChatColor.WHITE + "Bundle: " + stack + ChatColor.AQUA + "]";
							}
							//get stock
							int stock = shop.getItemStock(item);
							message += ChatColor.AQUA + " [" + ChatColor.WHITE + "Stock: " + stock + ChatColor.AQUA + "]";
							player.sendMessage(message);
						}
						player.sendMessage(ChatColor.AQUA + "To sell an item on the list type: " 
								+ ChatColor.WHITE + "/shop sell ItemName [ammount]");
					}
					
				} else {
					inShopName = PlayerData.playerShopList.get(playerName).get(0);
					
					Shop shop = ShopData.shops.get(inShopName);
					
					ArrayList<String> shopItems = shop.getItems();

					player.sendMessage(ChatColor.AQUA + "The shop " + ChatColor.WHITE 
							+ inShopName + ChatColor.AQUA + " is buying and selling:");
					for(String item: shopItems ) {
						String message = "   " + item;
						//get stock
						int stock = shop.getItemStock(item);
						message += " " + ChatColor.AQUA + "[" + ChatColor.WHITE + "Stock: " + stock + ChatColor.AQUA + "]";
						player.sendMessage(message);
					}
					player.sendMessage(ChatColor.AQUA + "Type " + ChatColor.WHITE + "/shop list buy" 
							+ ChatColor.AQUA + " or " + ChatColor.WHITE + "/shop list sell");
					player.sendMessage(ChatColor.AQUA + "to see details about price and quantity.");
				}
				
			}
			
			
		}
	}
}
