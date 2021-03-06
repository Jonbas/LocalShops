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
	public boolean sizeOkay;
	private long xyzA[] = null;
	private long xyzB[] = null;
	private String size = "";
	
	public PlayerData() {
		isSelecting = false;
		sizeOkay = false;
	}
	
	public long[] getPositionA() {
		return xyzA;
	}
	
	public long[] getPositionB() {
		return xyzB;
	}
	
	public void setPositionA(long[] xyz) {
		xyzA = xyz.clone();
		checkSize(xyzA, xyzB);
	}
	
	public void setPositionB(long[] xyz) {
		xyzB = xyz.clone();
		checkSize(xyzA, xyzB);
	}
	
	public String getSizeString() {
		return size;
	}
	
	private boolean checkSize(long[] xyzA, long[] xyzB) {
		if(xyzA != null && xyzB != null) {
			long width1 = Math.abs(xyzA[0] - xyzB[0]) + 1;
			long height = Math.abs(xyzA[1] - xyzB[1]) + 1;
			long width2 = Math.abs(xyzA[2] - xyzB[2]) + 1;
			
			size = "" + width1 + "x" + height + "x" + width2;
			
			if( width1 > ShopData.maxWidth || width2 > ShopData.maxWidth || height > ShopData.maxHeight ) {
				return false;
			} else {
				return true;
			}
		}
		return false;
		
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
				
		if( playerShopList.get(playerName).contains(shopName) ){
			if(	ShopData.shops.get(shopName).getWorldName().equalsIgnoreCase(playerWorld)) {
				return true;
			}
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
			account.setBalance(balance + (double)cost);
			ShopData.logPayment(playerName, "payment", cost, balance, balance + (double)cost);
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

			
			accountFrom.setBalance(balanceFrom - cost);
			ShopData.logPayment(playerFrom, "payment", cost, balanceFrom, balanceFrom + cost);
			accountTo.setBalance(balanceTo + cost);
			ShopData.logPayment(playerTo, "payment", cost, balanceTo, balanceTo + cost);
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
			double newBalance = balanceFrom - chargeAmount;
			if(balanceFrom >= chargeAmount) {
				account.setBalance(newBalance);
				ShopData.logPayment(shopOwner, "payment", chargeAmount, balanceFrom, newBalance);
				return true;
			} else {
				return false;
			}
			
		}
		return false;
	}

}
