package net.centerleft.localshops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Shop {
	
	private String worldName;
	private String shopName;
	private Location shopLocation;
	private String shopOwner;
	private String shopCreator;
	private String[] shopManagers;
	private boolean unlimitedMoney;
	private boolean unlimitedStock;
	
	private HashMap<String, Item> shopInventory;
	
	public Shop() {
		worldName = "";
		shopName = null;
		shopInventory = new HashMap<String, Item>();
		shopInventory.clear();
		long[] xyz = {0,0,0};
		shopLocation = new Location(xyz, xyz);
		shopOwner = "";
		shopCreator = "";
		shopManagers = null;
		unlimitedStock = false;
	}
	
	public void setWorldName( String name ) {
		worldName = name;
	}
	
	public String getWorldName() {
		return worldName;
	}
	
	public void setShopName( String name ) {
		shopName = name;
	}
	
	public String getShopName() {
		return shopName;
	}
	
	public void setShopOwner( String owner ) {
		shopOwner = owner;
	}

	private class Item {
		
		private String itemName;
		private int buyStackSize;
		private int buyStackPrice;
		private int sellStackSize;
		private int sellStackPrice;
		private int stock;
		public int maxStock;
		
		public Item() {
			itemName = null;
			buyStackSize = 1;
			buyStackPrice = 0;
			sellStackSize = 1;
			sellStackPrice = 0;
			stock = 0;
			maxStock = 0;
		}
		
		public Item(String name ) {
			this.itemName = name;

			buyStackSize = 1;
			buyStackPrice = 0;
			sellStackSize = 1;
			sellStackPrice = 0;
			stock = 0;
		}

		public String itemName () {
			return this.itemName;
		}

		public void setSell(int sellPrice, int sellSize) {
			sellStackPrice = sellPrice;
			sellStackSize = sellSize;
			
		}

		public void setBuy(int buyPrice, int buySize) {
			buyStackPrice = buyPrice;
			buyStackSize = buySize;			
		}
		
		public int getMaxStock() {
			return maxStock;
		}
	
	}
	
	private class Location {
		private long[] xyzA = {0,0,0};
		private long[] xyzB = {0,0,0};
		
		public Location(long[] xyzA, long[] xyzB) {
			this.xyzA = xyzA.clone();
			this.xyzB = xyzB.clone();
		}
		
		public Location() {
			
		}

		public long[] getLocation1() {
			return xyzA;
		}
		
		public long[] getLocation2() {
			return xyzB;
		}
		
		public boolean setLocation(long[] xyzA, long[] xyzB) {
			this.xyzA = xyzA.clone();
			this.xyzB = xyzB.clone();
			return true;
		}
		
	}

	public void setShopCreator(String name) {
		shopCreator = name;
	}

	public void setShopManagers(String[] names) {
		if(names != null) { 
			shopManagers = names.clone();
		} else {
			shopManagers = null;
		}
		
	}

	public void setLocation(long[] position1, long[] position2) {
		shopLocation.setLocation(position1, position2);
	}
	

	public void setUnlimitedStock(boolean b) {
		unlimitedStock = b;
	}
	
	public void setUnlimitedMoney(boolean b) {
		unlimitedMoney = b;
	}

	public void addItem(int itemNumber, int itemData, int buyPrice,
			int buyStackSize, int sellPrice, int sellStackSize, int stock, int maxStock) {
		//TODO add maxStock to item object
		String itemName = LocalShops.itemList.getItemName(itemNumber, itemData);
		Item thisItem = new Item( itemName );
		
		thisItem.setBuy( buyPrice, buyStackSize );
		thisItem.setSell( sellPrice, sellStackSize );
		
		thisItem.stock = stock;
		
		thisItem.maxStock = maxStock;
		
		if(shopInventory.containsKey(itemName)) {
			shopInventory.remove(itemName);
		}
		
		shopInventory.put(itemName, thisItem);
		
	}

	public String getShopOwner() {
		return this.shopOwner;
	}

	public String getShopCreator() {
		return this.shopCreator;
	}

	public String getShopPosition1String() {
		String returnString = "";
		for( long coord : shopLocation.getLocation1()) {
			returnString += coord + ",";
		}
		return returnString;
	}
	
	public String getShopPosition2String() {
		String returnString = "";
		for( long coord : shopLocation.getLocation2()) {
			returnString += coord + ",";
		}
		return returnString;
	}

	public String[] getShopManagers() {
		return shopManagers;
	}

	public String getValueofUnlimitedStock() {
		if(this.unlimitedStock) {
			return "true";
		} 
		return "false";
	}
	
	public String getValueofUnlimitedMoney() {
		if(this.unlimitedMoney) {
			return "true";
		} 
		return "false";
	}

	public ArrayList<String> getItems() {
		ArrayList<String> allItemNames = new ArrayList<String>();
		
		Iterator itr = shopInventory.entrySet().iterator();
		while(itr.hasNext()) {
			Map.Entry item = (Map.Entry)itr.next();
			String name = ((Item)item.getValue()).itemName();
			allItemNames.add(name);
		}
		
		Collections.sort(allItemNames);
		
		return allItemNames;
	}

	public int getItemBuyPrice(String itemName ) {
		return shopInventory.get(itemName).buyStackPrice;
		
	}

	public int itemBuyAmount(String itemName) {
		return shopInventory.get(itemName).buyStackSize;
	}

	public int getItemSellPrice(String itemName) {
		return shopInventory.get(itemName).sellStackPrice;
	}
	
	public int itemSellAmount(String itemName) {
		return shopInventory.get(itemName).sellStackSize;
	}

	public int getItemStock(String itemName) {
		return shopInventory.get(itemName).stock;
	}

	public boolean isUnlimitedStock() {
		return unlimitedStock;
	}
	
	public boolean isUnlimitedMoney() {
		return unlimitedMoney;
	}

	public boolean addStock(String itemName, int amount) {
		if(!shopInventory.containsKey(itemName)) return false;
		int oldStock = shopInventory.get(itemName).stock;
		shopInventory.get(itemName).stock = oldStock + amount;
		return true;
	}
	
	public boolean removeStock(String itemName, int amount) {
		if(!shopInventory.containsKey(itemName)) return false;
		int oldStock = shopInventory.get(itemName).stock;
		if( (oldStock - amount) >= 0) {
			shopInventory.get(itemName).stock = oldStock - amount;
		} else {
			shopInventory.get(itemName).stock = 0;
		}
		return true;
	}

	public void setItemBuyPrice(String itemName, int price) {
		int buySize = shopInventory.get(itemName).buyStackSize;
		shopInventory.get(itemName).setBuy(price, buySize);
		
	}

	public void setItemBuyAmount(String itemName, int buySize) {
		int price = shopInventory.get(itemName).buyStackPrice;
		shopInventory.get(itemName).setBuy(price, buySize);
	}

	public void setItemSellPrice(String itemName, int price) {
		int sellSize = shopInventory.get(itemName).sellStackSize;
		shopInventory.get(itemName).setSell(price, sellSize);
		
	}

	public void setItemSellAmount(String itemName, int sellSize) {
		int price = shopInventory.get(itemName).sellStackPrice;
		shopInventory.get(itemName).setSell(price, sellSize );
		
	}

	public void removeItem(String itemName) {
		shopInventory.remove(itemName);
		
	}

	public long[] getLocation1() {
		return shopLocation.getLocation1();
	}
	
	public long[] getLocation2() {
		return shopLocation.getLocation2();
	}
	
	public long[] getLocation() {
		long[] xyz = new long[3];
		long[] xyzA = shopLocation.getLocation1();
		long[] xyzB = shopLocation.getLocation2();
		xyz[0] = (Math.abs(xyzA[0]-xyzB[0]))/2 + xyzA[0];
		xyz[1] = (Math.abs(xyzA[1]-xyzB[1]))/2 + xyzA[1];
		xyz[2] = (Math.abs(xyzA[2]-xyzB[2]))/2 + xyzA[2];
		return xyz;
	}

	public int itemMaxStock(String itemName) {
		return shopInventory.get(itemName).maxStock;
	}

	public void setItemMaxStock(String itemName, int maxStock) {
		shopInventory.get(itemName).maxStock = maxStock;
	}

}