package net.centerleft.localshops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;

public class PlayerData {
	  //define a synchronized map for keeping track of players
	static Map<String, List<String>> playerShopList = Collections.synchronizedMap(new HashMap<String, List<String>>());
	static String chatPrefix = ChatColor.AQUA + "[" + ChatColor.WHITE + "Shop" + ChatColor.AQUA + "] ";
	
	public boolean isSelecting;
	private long xyzA[] = null;
	private long xyzB[] = null;
	
	public PlayerData() {
		isSelecting = false;
	}
	
	public long[] getPositionA() {
		return xyzA;
	}
	
	public long[] getPositionB() {
		return xyzB;
	}
	
	public void setPositionA(long[] xyz) {
		xyzA = xyz.clone();
	}
	
	public void setPositionB(long[] xyz) {
		xyzB = xyz.clone();
	}
	
	
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

	public static List<String> playerShopsList(String playerName) {
		if( !playerShopList.containsKey(playerName)) {
			playerShopList.put(playerName, Collections.synchronizedList(new ArrayList<String>()));	
		}
		return playerShopList.get(playerName);
	}

	public static boolean payPlayer(String playerName, int cost) {
		if( ShopsPluginListener.useiConomy ) {
			iConomy ic = ShopsPluginListener.iConomy;
			Account account = ic.getBank().getAccount(playerName);
			if(account == null) {
				ic.getBank().addAccount(playerName);
				account = ic.getBank().getAccount(playerName);
			}
			double balance = account.getBalance();
			balance += (double)cost;
			account.setBalance(balance);
			account.save();
			return true; 
		}
		return false;
	}

	public static boolean payPlayer(String playerFrom, String playerTo, int cost) {
		if( ShopsPluginListener.useiConomy ) {
			iConomy ic = ShopsPluginListener.iConomy;
			
			Account accountFrom = ic.getBank().getAccount(playerFrom);
			if(accountFrom == null) {
				ic.getBank().addAccount(playerFrom);
				accountFrom = ic.getBank().getAccount(playerFrom);
			}
			double balanceFrom = accountFrom.getBalance();
			
			Account accountTo = ic.getBank().getAccount(playerTo);
			if(accountTo == null) {
				ic.getBank().addAccount(playerTo);
				accountTo = ic.getBank().getAccount(playerTo);
			}
			double balanceTo = accountTo.getBalance();
			
			if( balanceFrom < cost ) return false;
			
			balanceFrom -= cost;
			balanceTo += cost;
			
			accountFrom.setBalance(balanceFrom);
			accountTo.setBalance(balanceTo);
			
			accountFrom.save();
			accountTo.save();
			return true; 
		}
		return false;
	}

	public static long getBalance(String shopOwner) {
		if( ShopsPluginListener.useiConomy ) {
			iConomy ic = ShopsPluginListener.iConomy;
			
			Account account = ic.getBank().getAccount(shopOwner);
			if(account == null) {
				ic.getBank().addAccount(shopOwner);
				account = ic.getBank().getAccount(shopOwner);
			}
			double balanceFrom = account.getBalance();
			
			return (long)Math.floor(balanceFrom);
		}
		return 0;
	}

	public static boolean chargePlayer(String shopOwner, long chargeAmount) {
		if( ShopsPluginListener.useiConomy ) {
			iConomy ic = ShopsPluginListener.iConomy;
			if(ic == null) return false;
			
			Account account = ic.getBank().getAccount(shopOwner);
			if(account == null) {
				ic.getBank().addAccount(shopOwner);
				account = ic.getBank().getAccount(shopOwner);
			}
			double balanceFrom = account.getBalance();
			long newBalance = (long)Math.floor(balanceFrom) - chargeAmount;
			if(balanceFrom >= chargeAmount) {
				account.setBalance(newBalance);
				account.save();
				return true;
			} else {
				return false;
			}
			
		}
		return false;
	}

}
