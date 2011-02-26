package net.centerleft.localshops;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PlayerData {
	  //define a synchronized map for keeping track of players
	static Map<String, List<String>> playerShopList = Collections.synchronizedMap(new HashMap<String, List<String>>());
	static String chatPrefix = ChatColor.AQUA + "[" + ChatColor.WHITE + "Shop" + ChatColor.AQUA + "] ";
	
	static boolean addPlayerToShop( Player player, String shopName ) {
		if( !playerIsInShop( player, shopName ) && 
				ShopData.shops.get(shopName).getWorldName().equalsIgnoreCase(player.getWorld().getName())){
			return playerShopList.get(player.getName()).add(shopName);
		}
		return false;
	}
	
	static boolean playerIsInShop( Player player, String shopName ) {
		String playerName = player.getName();
		String playerWorld = player.getWorld().getName();
				
		if( playerShopList.get(playerName).contains(shopName) && 
				ShopData.shops.get(shopName).getWorldName().equalsIgnoreCase(playerWorld)) {
			return true;
		}
		return false;
	}

	public static void removePlayerFromShop(Player player, String shopName) {
		playerShopList.get(player.getName()).remove(shopName);
	}

}
