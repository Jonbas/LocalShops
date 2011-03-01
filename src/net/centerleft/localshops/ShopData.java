package net.centerleft.localshops;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

import cuboidLocale.PrimitiveCuboid;

public class ShopData {
	static HashMap<String, Shop> shops;
	
	final static long shopSize = 10;
	final static long shopHeight = 3;
	static String currencyName = "Coin";
	
	static void LoadShops( File shopsDir ) {
		  //initialize and setup the hash of shops
		shops = new HashMap<String, Shop>();
		shops.clear();
		
		String worldName = null;
		boolean defaultWorld = false;
		
		if(LocalShops.foundWorlds.size() == 1) {
			worldName = LocalShops.foundWorlds.get(0).getName().toString();
			defaultWorld = true;
		}
		
		
		File[] shopsList = shopsDir.listFiles();
		for( File shop : shopsList ) {
			
			if (!shop.isFile()) continue;
			// read the file and put the data away nicely
			Shop tempShop = new Shop();
			String text = null;
			String[] split = null;
		    Scanner scanner;
		    PrimitiveCuboid tempShopCuboid = null; 
		    
		    //set default world just in case we're converting old files
		    //will be over-written in case the shop files are setup correctly
		    if(defaultWorld) {
		    	tempShop.setWorldName(worldName);
		    }
		    
		    
		    try {
				scanner = new Scanner(new FileInputStream(shop));	
				
				int itemType, itemData;
				int buyPrice, buyStackSize;
				int sellPrice, sellStackSize;
				int stock;
				
				while (scanner.hasNextLine()){
					text = scanner.nextLine();
					  //check if the next line is empty or a comment
					  //ignore comments
					if(!text.startsWith("#") && !text.isEmpty()){
						split = text.split("=");
						try {
							itemType = Integer.parseInt( split[0].trim() );
							String[] args = split[1].split(",");
							//args may be in one of two formats now:
							//buyPrice:buyStackSize sellPrice:sellStackSize stock
							//or
							//dataValue buyPrice:buyStackSize sellPrice:sellStackSize stock
							if(args.length == 3) {
								String[] temp = args.clone();
								args = new String[4];
								args[0] = "0";
								args[1] = temp[0];
								args[2] = temp[1];
								args[3] = temp[2];
							}
							
							try {
								itemData = Integer.parseInt(args[0]);
								
								String[] buy = args[1].split(";");
								buyPrice = Integer.parseInt(buy[0]);
								if(buy.length == 1) {
									buyStackSize = 1;
								} else {
									buyStackSize = Integer.parseInt(buy[1]);
								}
								
								String[] sell = args[2].split(";");
								sellPrice = Integer.parseInt(sell[0]);
								if(sell.length == 1) {
									sellStackSize = 1;
								} else {
									sellStackSize = Integer.parseInt(sell[1]);
								}
								
								stock = Integer.parseInt(args[3]);
								
								tempShop.addItem( itemType, itemData, buyPrice, buyStackSize, sellPrice, sellStackSize, stock);
								
							} catch (NumberFormatException ex3) {
								System.out.println( LocalShops.pluginName + ": Error - Problem with item data in " + shop.getName() );
							}
							
							
						} catch (NumberFormatException ex) {
							// this isn't an item number, so check what property it is
							if(split[0].equalsIgnoreCase("owner")) {
								tempShop.setShopOwner(split[1]);
							} else if(split[0].equalsIgnoreCase("creator")){
								tempShop.setShopCreator(split[1]);
								
							} else if(split[0].equalsIgnoreCase("managers")) {
								String[] args = split[1].split(",");
								tempShop.setShopManagers(args);
							} else if(split[0].equalsIgnoreCase("world")) {
								tempShop.setWorldName(split[1]);
							} else if(split[0].equalsIgnoreCase("position")) {
								String[] args = split[1].split(",");
								
								long[] xyzA = new long[3];
								long[] xyzB = new long[3];
								long lx = 0;
								long ly = 0;
								long lz = 0;
								try {
	
									lx = Long.parseLong(args[0].trim());
									ly = Long.parseLong(args[1].trim());
									lz = Long.parseLong(args[2].trim());
									
									xyzA[0] = lx - (shopSize / 2);
									xyzB[0] = lx + (shopSize / 2);
									xyzA[2] = lz - (shopSize / 2);
									xyzB[2] = lz + (shopSize / 2);
									
									xyzA[1] = ly - 1;
									xyzB[1] = ly + shopHeight - 1;

									tempShopCuboid = new PrimitiveCuboid( xyzA, xyzB );
								
									
								} catch (NumberFormatException ex2) {
							
									
									lx = 0;
									ly = 0;
									lz = 0;
									System.out.println( LocalShops.pluginName + ": Error - Problem with position data in " + shop.getName() );
								}
								tempShop.setLocation(lx, ly, lz);
									
							} else if(split[0].equalsIgnoreCase("unlimited")) {
								if(split[1].equalsIgnoreCase("true")) {
									tempShop.setUnlimited(true);
								} else {
									tempShop.setUnlimited(false);
								}
							}
						}
						
					}
				}
				
				String[] shopName = shop.getName().split("\\.");
				
				tempShop.setShopName(shopName[0]);
				tempShopCuboid.name = tempShop.getShopName();
				
				LocalShops.cuboidTree.insert(tempShopCuboid);

				shops.put(shopName[0], tempShop );
				
		    } catch (FileNotFoundException e) {
				System.out.println( LocalShops.pluginName + ": Error - Could not read file " + shop.getName() );
			}
		}

	}
	
	public static boolean saveShop( Shop shop ) {
		String filePath = LocalShops.folderPath + LocalShops.shopsPath + shop.getShopName() + ".shop";

		File shopFile = new File( filePath );
		try {

			shopFile.createNewFile();
		
			ArrayList<String> fileOutput = new ArrayList<String>();
			
			fileOutput.add("#" + shop.getShopName() + " shop file\n");
			
			DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
	        Date date = new Date();
			fileOutput.add("#" + dateFormat.format(date) + "\n");
			
			fileOutput.add("world=" + shop.getWorldName() + "\n");
			fileOutput.add("owner=" + shop.getShopOwner() + "\n");
			
			String outString = null;
			if( shop.getShopManagers() != null ) {
				for( String manager: shop.getShopManagers()) {
					outString = outString + manager + ",";
				} 
			} else {
				outString = "none";
			}
			fileOutput.add("managers=" + outString + "\n");
			fileOutput.add("creator=" + shop.getShopCreator() + "\n");
			fileOutput.add("position=" + shop.getShopPositionString() + "\n");
			fileOutput.add("unlimited=" + shop.getValueofUnlimited() + "\n");
			
			for(String item: shop.getItems()) {
				int buyPrice = shop.getItemBuyPrice(item);
				int buySize = shop.itemBuyAmount(item);
				int sellPrice = shop.getItemSellPrice(item);
				int sellSize = shop.itemSellAmount(item);
				int stock = shop.getItemStock(item);
				int[] itemInfo = LocalShops.itemList.getItemInfo(item);
				if(itemInfo == null) continue;
				//itemId=dataValue,buyPrice:buyStackSize,sellPrice:sellStackSize,stock
				fileOutput.add(itemInfo[0] + "=" + itemInfo[1] + "," + buyPrice + ";" + buySize
						 + "," + sellPrice + ";" + sellSize + "," + stock + "\n");
			}
			
			FileOutputStream shopFileOut = new FileOutputStream(filePath);
			
			for(String line: fileOutput) {
				shopFileOut.write(line.getBytes());
			}
	
			shopFileOut.close();
			
			
		} catch ( IOException e1 ) {
			System.out.println( LocalShops.pluginName + ": Error - Could not create file " + shopFile.getName());
			return false;
		}
		return true;
	}
}
