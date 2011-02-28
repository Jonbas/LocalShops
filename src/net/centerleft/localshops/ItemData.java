package net.centerleft.localshops;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

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
		
	public int[] getItemInfo(String name) {
		int index = itemName.indexOf(name);
		int[] data = { itemNumber.get(index), itemData.get(index).dataValue };
		return data;
	}
		
	public ArrayList<String> getItemName(int itemNumber) {
		ArrayList<String> foundNames = new ArrayList<String>();
		
		
		for( int i = 0; i < this.itemNumber.size(); i++ ){
			if( this.itemNumber.get(i) == itemNumber) {
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

}

