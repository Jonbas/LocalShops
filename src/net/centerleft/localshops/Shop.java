package net.centerleft.localshops;

import java.util.HashMap;

public class Shop {
	
	private String worldName;
	private String shopName;
	private Location shopLocation;
	private String shopOwner;
	private String shopCreator;
	private String[] shopManagers;
	private boolean unlimitedStock;
	
	private HashMap<String, Item> shopInventory;
	//TODO :: Need to find a naming convention so that items with different types are different

	public Shop() {
		worldName = "";
		shopName = null;
		shopInventory = new HashMap<String, Item>();
		shopInventory.clear();
		shopLocation = new Location(0,0,0);
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
		private String itemData;
		private int buyStackSize;
		private int buyStackPrice;
		private int sellStackSize;
		private int sellStackPrice;
		private int stock;
		
		public Item() {
			itemName = null;
			itemData = null;
			buyStackSize = 1;
			buyStackPrice = 0;
			sellStackSize = 1;
			sellStackPrice = 0;
			stock = 0;
		}
	
	}
	
	private class Location {
		private long lx, ly, lz;
		
		public Location(long x, long y, long z) {
			lx = x;
			ly = y;
			lz = z;
		}
		
		public Location(long[] xyz) {
			if(xyz.length == 3) {
				lx = xyz[0];
				ly = xyz[1];
				lz = xyz[2];
			} else {
				lx = 0;
				ly = 0;
				lz = 0;
			}
		}
		
		public long[] getLocation() {
			long[] location = {lx, ly, lz};
			return location;
		}
		
		public boolean setLocation(long x, long y, long z) {
			lx = x;
			ly = y;
			lz = z;
			return true;
		}
		
		public boolean setLocation(long[] xyz) {
			if(xyz.length == 3) {
				lx = xyz[0];
				ly = xyz[1];
				lz = xyz[2];
			} else {
				lx = 0;
				ly = 0;
				lz = 0;
				return false;
			}
			return true;
		}
		
	}

	public void setShopCreator(String name) {
		shopCreator = name;
	}

	public void setShopManagers(String[] names) {
		shopManagers = names.clone();
	}

	public void setLocation(long[] position) {
		shopLocation.setLocation(position);
	}
	
	public void setLocation( long x, long y, long z) {
		shopLocation.setLocation(x, y, z);
	}

	public void setUnlimited(boolean b) {
		unlimitedStock = b;
	}

	public void addItem(int itemType, int itemData, int buyPrice,
			int buyStackSize, int sellPrice, int sellStackSize, int stock) {
		// TODO Auto-generated method stub
		
	}

	public String getShopOwner() {
		return this.shopOwner;
	}

	public String getShopCreator() {
		return this.shopCreator;
	}

	public String getShopPositionString() {
		String returnString = "";
		for( long coord : shopLocation.getLocation()) {
			returnString += coord + ",";
		}
		return returnString;
	}

	public String[] getShopManagers() {
		return shopManagers;
	}

	public String getValueofUnlimited() {
		if(this.unlimitedStock) {
			return "true";
		} 
		return "false";
	}

}