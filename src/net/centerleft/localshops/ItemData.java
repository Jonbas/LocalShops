package net.centerleft.localshops;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class ItemData {
	private ArrayList<String> itemName;
	private ArrayList<Integer> itemNumber;
	private ArrayList<itemDataType> itemData;
	
	public ItemData() {
		itemName = new ArrayList<String>();
		itemNumber = new ArrayList<Integer>();
		itemData = new ArrayList<itemDataType>();
	}
	
	public void loadData( File dataFile ) {
		String line = null;
		try {
			try {
				if(dataFile.createNewFile()) writeItemsFile(dataFile);
			} catch (IOException e) {
				System.out.println("LocalShops: File IO error.  Could not create items.txt");
			}
			Scanner scanner = new Scanner(new FileInputStream(dataFile));
			
			while (scanner.hasNextLine()){
				try {
					line = scanner.next();
					String[] parts = line.split("=");
					if(parts.length == 2) {
						String[] idData = parts[1].split(",");
						if(idData.length == 1) {
							this.addItem( parts[0], Integer.parseInt(parts[1]));
							continue;
						}
						
						if(idData.length == 2) {
							this.addItem( parts[0], Integer.parseInt(idData[0]), Integer.parseInt(idData[1]));
							continue;
						}
						
						
					} 
				} catch (NoSuchElementException e3) {
					
				}
			}
			
			scanner.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void writeItemsFile(File dataFile) {

		InputStream is = getClass().getResourceAsStream("items.txt");
		Scanner fis = new Scanner(is);
		
		FileOutputStream os;
		try {
			os = new FileOutputStream(dataFile);
			try {
				while(fis.hasNext()) {
					os.write((fis.nextLine() + "\n").getBytes());
				}
				
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		fis.close();

	}
		

	public void addItem( String name, int blockNumber ) {
		if(!itemName.contains(name)) {
			itemName.add(name);
			itemNumber.add(blockNumber);
			itemDataType tmp = new itemDataType();
			itemData.add(tmp);
		}			
		
	}
	
	public void addItem( String name, int blockNumber, int dataValue ) {
		if(!itemName.contains(name)) {
			itemName.add(name);
			itemNumber.add(blockNumber);
			
			itemDataType tmp = new itemDataType( dataValue );
			itemData.add(tmp);
		}
	};
		
	/**
	 * Tries to match item name passed in string.  If sender is passed to function, will return message
	 * to sender if no matches are found or print list of matches if multiple are found.
	 * @param sender
	 * @param name
	 * @return Will return null if no matches are found.
	 */
	public int[] getItemInfo(CommandSender sender, String name) {
	
		int index = itemName.indexOf(name);
		if (index == -1) {
			Pattern myPattern = Pattern.compile(name, Pattern.CASE_INSENSITIVE);
			Matcher myMatcher = myPattern.matcher("tmp");
			
			ArrayList<String> foundMatches = new ArrayList<String>();
			foundMatches.clear();
			
			Iterator<String> itr = itemName.iterator();
			while(itr.hasNext()) {
				String thisItem = itr.next();
				myMatcher.reset(thisItem);
				if(myMatcher.find()) foundMatches.add(thisItem);
			}
			
			if (foundMatches.size() == 1) {
				index = itemName.indexOf(foundMatches.get(0));
			} else {
				if(sender != null) {
					if(foundMatches.size() > 1) {
						sender.sendMessage(name + ChatColor.AQUA + " matched multiple items:" );
						for(String foundName: foundMatches) {
							sender.sendMessage("  " + foundName );
						}
					} else {
						sender.sendMessage(name + ChatColor.AQUA + " did not match any items.");
					}
				}
				return null;
			}
		}
		int[] data = { itemNumber.get(index), itemData.get(index).dataValue };
		return data;
	}
	
	/**
	 * Returns list of all itemNames that match the itemId supplied.
	 * 
	 * @param sender
	 * @param itemNumber
	 * @return Will return list of all found matches.
	 */
	public ArrayList<String> getItemName( int itemId) {
		ArrayList<String> foundNames = new ArrayList<String>();
		
		
		for( int i = 0; i < this.itemNumber.size(); i++ ){
			if( itemNumber.get(i) == itemId) {
				foundNames.add(this.itemName.get(i));
			}
		}
		
		return foundNames;
		
	}
	
	public String getItemName(int itemNumber, int itemData) {
		
		//check if type and data match, if they do, return that one
		for( int i = 0; i < this.itemNumber.size(); i++ ){
			if( this.itemNumber.get(i) == itemNumber && this.itemData.get(i).dataValue == itemData ) {
				return this.itemName.get(i);
			}
		}
		
		//check if this is armor or an item
		ArrayList<String> itemList = getItemName(itemNumber);
		if( itemList.size() == 1 ) {
			return itemList.get(0);
		}
		
		return null;
	}

	private class itemDataType {
		public boolean hasData = false;
		public int dataValue = 0;
		
		public itemDataType( int dataValue ) {
			this.dataValue = dataValue;
			this.hasData = true;
		}
		
		public itemDataType() {
			this.dataValue = 0;
			this.hasData = false;
		}
	}

	public ItemStack getItem( CommandSender sender, String arg0) {
		
		int[] info = null;
		ItemStack item = null;
		
		try {
			ArrayList<String> list = getItemName(Integer.parseInt(arg0));
			if(list.size() == 1) {
				info = getItemInfo( sender, list.get(0));
			} else {
				if(sender != null) {
					if(list.size() > 1) {
						sender.sendMessage(arg0 + ChatColor.AQUA + " matched multiple items:" );
						for(String foundName: list) {
							sender.sendMessage("  " + foundName );
						}
					} else {
						sender.sendMessage(arg0 + ChatColor.AQUA + " did not match any items.");
					}
				}
			}
			

		} catch (NumberFormatException ex) {
			info = getItemInfo( sender, arg0);
		}
		
		if( info != null) {
			item = new ItemStack(info[0], 1);
			MaterialData data = new MaterialData(info[1]);
			item.setData(data);
//TODO this is a work around for bukkit glitch.  Check if this still works.
			item.setDurability((short)info[1]);
		
			return item;
		}
		
		return null;
	}

	public ItemStack getShopItem(CommandSender sender, Shop shop, String arg0) {

		int[] info = null;
		ItemStack item = null;
		
		try {
			ArrayList<String> list = getItemName(Integer.parseInt(arg0));
			if(list.size() == 1) {
				info = getItemInfo( sender, list.get(0));
			} else {
				if(sender != null) {
					if(list.size() > 1) {
						sender.sendMessage(arg0 + ChatColor.AQUA + " matched multiple items:" );
						for(String foundName: list) {
							sender.sendMessage("  " + foundName );
						}
					} else {
						sender.sendMessage(arg0 + ChatColor.AQUA + " did not match any items.");
					}
				}
			}
			

		} catch (NumberFormatException ex) {
			info = getShopItemInfo( sender, shop, arg0);
		}
		
		if( info != null) {
			item = new ItemStack(info[0], 1);
			MaterialData data = new MaterialData(info[1]);
			item.setData(data);
//TODO this is a work around for bukkit glitch.  Check if this still works.
			item.setDurability((short)info[1]);
		
			return item;
		}
		
		return null;
	}

	/**
	 * Tries to match item name passed in string based on the inventory of the shop.  
	 * If sender is passed to function, will return message to sender if no matches 
	 * are found or print list of matches if multiple are found.
	 * @param sender
	 * @param shop
	 * @param name
	 * @return Will return null if no matches are found.
	 */
	private int[] getShopItemInfo(CommandSender sender, Shop shop, String name) {
		int index = itemName.indexOf(name);
		if (index == -1) {
			Pattern myPattern = Pattern.compile(name, Pattern.CASE_INSENSITIVE);
			Matcher myMatcher = myPattern.matcher("tmp");
			
			ArrayList<String> foundMatches = new ArrayList<String>();
			foundMatches.clear();
			
			Iterator<String> itr = shop.getItems().iterator();
			while(itr.hasNext()) {
				String thisItem = itr.next();
				myMatcher.reset(thisItem);
				if(myMatcher.find()) foundMatches.add(thisItem);
			}
			
			if (foundMatches.size() == 1) {
				index = itemName.indexOf(foundMatches.get(0));
			} else {
				if(sender != null) {
					if(foundMatches.size() > 1) {
						sender.sendMessage(name + ChatColor.AQUA + " matched multiple items:" );
						for(String foundName: foundMatches) {
							sender.sendMessage("  " + foundName );
						}
					} else {
						sender.sendMessage(name + ChatColor.AQUA + " did not match any items.");
					}
				}
				return null;
			}
		}
		int[] data = { itemNumber.get(index), itemData.get(index).dataValue };
		return data;
	}
	
	public boolean isDurable( ItemStack item ) {
		Material itemType = item.getType();
		if( itemType == Material.CHAINMAIL_BOOTS ||
				itemType == Material.CHAINMAIL_CHESTPLATE ||
				itemType == Material.CHAINMAIL_HELMET ||
				itemType == Material.CHAINMAIL_LEGGINGS ||
				itemType == Material.WOOD_AXE ||
				itemType == Material.WOOD_HOE ||
				itemType == Material.WOOD_PICKAXE ||
				itemType == Material.WOOD_SPADE ||
				itemType == Material.WOOD_SWORD ||
				itemType == Material.STONE_AXE ||
				itemType == Material.STONE_HOE ||
				itemType == Material.STONE_PICKAXE ||
				itemType == Material.STONE_SPADE ||
				itemType == Material.STONE_SWORD ||
				itemType == Material.IRON_AXE ||
				itemType == Material.IRON_BOOTS ||
				itemType == Material.IRON_CHESTPLATE ||
				itemType == Material.IRON_HELMET ||
				itemType == Material.IRON_HOE ||
				itemType == Material.IRON_LEGGINGS ||
				itemType == Material.IRON_PICKAXE ||
				itemType == Material.IRON_SPADE ||
				itemType == Material.IRON_SWORD ||
				itemType == Material.GOLD_AXE ||
				itemType == Material.GOLD_BOOTS ||
				itemType == Material.GOLD_CHESTPLATE ||
				itemType == Material.GOLD_HELMET ||
				itemType == Material.GOLD_HOE ||
				itemType == Material.GOLD_LEGGINGS ||
				itemType == Material.GOLD_PICKAXE ||
				itemType == Material.GOLD_SPADE ||
				itemType == Material.GOLD_SWORD ||
				itemType == Material.DIAMOND_AXE ||
				itemType == Material.DIAMOND_BOOTS ||
				itemType == Material.DIAMOND_CHESTPLATE ||
				itemType == Material.DIAMOND_HELMET ||
				itemType == Material.DIAMOND_HOE ||
				itemType == Material.DIAMOND_LEGGINGS ||
				itemType == Material.DIAMOND_PICKAXE ||
				itemType == Material.DIAMOND_SPADE ||
				itemType == Material.DIAMOND_SWORD ) {
			return true;
		}
		return false;
	}
}

