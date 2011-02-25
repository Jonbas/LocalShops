package net.centerleft.localshops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

import cuboidLocale.BookmarkedResult;
import cuboidLocale.PrimitiveCuboid;

/**
 * Handle events for all Player related events
 * 
 * @author Jonbas
 */
public class ShopsPlayerListener extends PlayerListener {
	private final LocalShops plugin;

	public ShopsPlayerListener(LocalShops instance) {
		plugin = instance;
	}

	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		String playerName = player.getName();
		
		if( !plugin.playerResult.containsKey(playerName)) {
			plugin.playerResult.put(playerName, new BookmarkedResult());
		}
		
		if( !PlayerData.playerShopList.containsKey(playerName)) {
			PlayerData.playerShopList.put(playerName, Collections.synchronizedList(new ArrayList<String>()));	
		}
		

		BookmarkedResult res = plugin.playerResult.get(playerName);
		long x, y, z;
		Location xyz = event.getTo();
		x = (long)xyz.getX();
		y = (long)xyz.getY();
		z = (long)xyz.getZ();
		
		synchronized(plugin.playerResult) {
			res = LocalShops.cuboidTree.relatedSearch(res.bookmark, x, y, z);
			
			synchronized(PlayerData.playerShopList.get(playerName)) {
			
				//check to see if we've entered any shops
				for( PrimitiveCuboid shop : res.results) {
					
					//for each shop that you find, check to see if we're already in it
					
					if( shop.name.equals(null)) continue;
					
							
					if(!PlayerData.playerIsInShop(player, shop.name)) {
						if(PlayerData.addPlayerToShop(player, shop.name) ) {
							notifyPlayerEnterShop(player, shop.name);
						}
					} 
				}
	
				synchronized(PlayerData.playerShopList) {
					//check to see if we've left any shops
					for( String checkShopName : PlayerData.playerShopList.get(playerName)) {

						//check the tree search results to see player is no longer in a shop.
						boolean removeShop = true;
						for( PrimitiveCuboid shop : res.results ) {
							if (shop.name.equalsIgnoreCase(checkShopName)) {
								removeShop = false;
								break;
							}
						}
						
						if(removeShop) {
							PlayerData.removePlayerFromShop(player, checkShopName);
							notifyPlayerLeftShop(player, checkShopName);
						}
						
					}
				}
			}
			
		}
		
	}

	private void notifyPlayerLeftShop(Player player, String shopName) {
		// TODO Auto-generated method stub
		player.sendMessage("You have left the shop " + shopName + "!");
	}

	private void notifyPlayerEnterShop(Player player, String shopName) {
		// TODO Auto-generated method stub
		player.sendMessage("You have entered the shop " + shopName + "!");
		
	}
}
