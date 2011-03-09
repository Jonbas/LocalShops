package net.centerleft.localshops;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
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
		for( int i = 0; i <= this.itemNumber.size(); i++ ){
			if( this.itemNumber.get(i) == itemNumber && this.itemData.get(i).dataValue == itemData ) {
				return this.itemName.get(i);
			}
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

}

