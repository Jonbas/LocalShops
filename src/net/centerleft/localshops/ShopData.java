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

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import cuboidLocale.BookmarkedResult;
import cuboidLocale.PrimitiveCuboid;
import cuboidLocale.QuadTree;

public class ShopData {
	static HashMap<String, Shop> shops;
	
	static long shopSize = 10;
	static long shopHeight = 3;
	static String currencyName = "Coin";
	
	static long shopCost = 4000;
	static boolean chargeForShop = false;
	public static boolean logTransactions = true;

	public static int maxDamage = 0;
	
	static void LoadShops( File shopsDir ) {
		  //initialize and setup the hash of shops
		shops = new HashMap<String, Shop>();
		shops.clear();
		
		LocalShops.cuboidTree = new QuadTree();

		
		String worldName = null;
		boolean defaultWorld = false;
		
		if(LocalShops.foundWorlds.size() == 1) {
			worldName = LocalShops.foundWorlds.get(0).getName().toString();
			defaultWorld = true;
		}
		
		if(ShopsPluginListener.useiConomy) {
			currencyName = ShopsPluginListener.iConomy.getBank().getCurrency();
		}
		
		
		File[] shopsList = shopsDir.listFiles();
		for( File shop : shopsList ) {
			
			if (!shop.isFile()) continue;
			if(!shop.getName().contains(".shop")) continue;
			// read the file and put the data away nicely
			Shop tempShop = new Shop();
			String text = null;
			String[] split = null;
		    Scanner scanner;
		    PrimitiveCuboid tempShopCuboid = null; 
		    
			String[] shopName = shop.getName().split("\\.");
			
			tempShop.setShopName(shopName[0]);
			
			System.out.println("LocalShops: Loading shop " + shopName[0]);
		    
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
				int stock, maxStock;
				long[] xyzA = new long[3];
				long[] xyzB = new long[3];
				
				while (scanner.hasNextLine()){
					text = scanner.nextLine();
					  //check if the next line is empty or a comment
					  //ignore comments
					if(!text.startsWith("#") && !text.isEmpty()){
						split = text.split("=");
						try {
							
							itemType = Integer.parseInt( split[0].split(":")[0].trim() );
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
								
								if(split[0].split(":").length == 1) {
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
									tempShop.addItem( itemType, itemData, buyPrice, buyStackSize, sellPrice, sellStackSize, stock, 0);
								} else {
									itemData = Integer.parseInt(split[0].split(":")[1]);
									
									String[] buy = args[1].split(":");
									buyPrice = Integer.parseInt(buy[0]);
									if(buy.length == 1) {
										buyStackSize = 1;
									} else {
										buyStackSize = Integer.parseInt(buy[1]);
									}
									
									String[] sell = args[2].split(":");
									sellPrice = Integer.parseInt(sell[0]);
									if(sell.length == 1) {
										sellStackSize = 1;
									} else {
										sellStackSize = Integer.parseInt(sell[1]);
									}
									
							
									String[] stockInfo = args[3].split(":");
									stock = Integer.parseInt(stockInfo[0]);
								
									if(stockInfo.length == 1) {
										maxStock = 0;
									} else {
										maxStock = Integer.parseInt(stockInfo[1]);
									}
									
									tempShop.addItem( itemType, itemData, buyPrice, buyStackSize, sellPrice, sellStackSize, stock, maxStock);
								}
								
									
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
								if(split.length > 1) {
									String[] args = split[1].split(",");
									tempShop.setShopManagers(args);
								}
							} else if(split[0].equalsIgnoreCase("world")) {
								tempShop.setWorldName(split[1]);
							} else if(split[0].equalsIgnoreCase("position")) {
								String[] args = split[1].split(",");
								
								xyzA = new long[3];
								xyzB = new long[3];
								long lx = 0;
								long ly = 0;
								long lz = 0;
								try {
	
									lx = Long.parseLong(args[0].trim());
									ly = Long.parseLong(args[1].trim());
									lz = Long.parseLong(args[2].trim());
									
									if( shopSize % 2 == 1) {
										xyzA[0] = lx - (shopSize / 2);
										xyzB[0] = lx + (shopSize / 2);
										xyzA[2] = lz - (shopSize / 2);
										xyzB[2] = lz + (shopSize / 2);
									} else {
										xyzA[0] = lx - (shopSize / 2) + 1;
										xyzB[0] = lx + (shopSize / 2);
										xyzA[2] = lz - (shopSize / 2) + 1;
										xyzB[2] = lz + (shopSize / 2);
									}
									
									xyzA[1] = ly - 1;
									xyzB[1] = ly + shopHeight - 1;
									
									tempShopCuboid = new PrimitiveCuboid( xyzA, xyzB );
								
									
								} catch (NumberFormatException ex2) {
							
									
									lx = 0;
									ly = 0;
									lz = 0;
									System.out.println( LocalShops.pluginName + ": Error - Problem with position data in " + shop.getName() );
								}
								tempShop.setLocation(xyzA, xyzB);
								
							} else if(split[0].equalsIgnoreCase("position1")) {
									String[] args = split[1].split(",");
									
									xyzA = new long[3];
									xyzB = new long[3];
									try {
		
										xyzA[0] = Long.parseLong(args[0].trim());
										xyzA[1] = Long.parseLong(args[1].trim());
										xyzA[2] = Long.parseLong(args[2].trim());
										
									} catch (NumberFormatException ex2) {
								
										
										xyzA[0] = 0;
										xyzA[1] = 0;
										xyzA[2] = 0;
										System.out.println( LocalShops.pluginName + ": Error - Problem with position1 data in " + shop.getName() );
									}
									xyzB = tempShop.getLocation2();
									tempShop.setLocation(xyzA, xyzB);
									
							} else if(split[0].equalsIgnoreCase("position2")) {
								String[] args = split[1].split(",");
								
								xyzA = new long[3];
								xyzB = new long[3];
								try {
	
									xyzB[0] = Long.parseLong(args[0].trim());
									xyzB[1] = Long.parseLong(args[1].trim());
									xyzB[2] = Long.parseLong(args[2].trim());
									
								} catch (NumberFormatException ex2) {
							
									
									xyzB[0] = 0;
									xyzB[1] = 0;
									xyzB[2] = 0;
									System.out.println( LocalShops.pluginName + ": Error - Problem with position2 data in " + shop.getName() );
								}
								xyzA = tempShop.getLocation1();
								tempShop.setLocation(xyzA, xyzB);
									
							} else if(split[0].equalsIgnoreCase("unlimited")) {
								if(split[1].equalsIgnoreCase("true")) {
									tempShop.setUnlimitedMoney(true);
								} else {
									tempShop.setUnlimitedMoney(false);
								}
								
								
							} else if(split[0].equalsIgnoreCase("unlimited-money")) {
								if(split[1].equalsIgnoreCase("true")) {
									tempShop.setUnlimitedMoney(true);
								} else {
									tempShop.setUnlimitedMoney(false);
								}
								
								
							} else if(split[0].equalsIgnoreCase("unlimited-stock")) {
								if(split[1].equalsIgnoreCase("true")) {
									tempShop.setUnlimitedStock(true);
								} else {
									tempShop.setUnlimitedStock(false);
								}
								
								
							}
						}
						
					}
				}
				
				tempShopCuboid = new PrimitiveCuboid( tempShop.getLocation1(), tempShop.getLocation2() );
				
				tempShopCuboid.name = tempShop.getShopName();
				tempShopCuboid.world = tempShop.getWorldName();
				
				if( shopPositionOk(tempShop, xyzA, xyzB )) {
				
					LocalShops.cuboidTree.insert(tempShopCuboid);
					shops.put(shopName[0], tempShop );
					
					//convert to new format
					saveShop(tempShop);
				}
				
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
			
			String outString = "";
			if( shop.getShopManagers() != null ) {
				for( String manager: shop.getShopManagers()) {
					outString = outString + manager + ",";
				} 
			} 
			if(outString.equalsIgnoreCase("null")) outString = "";
			
			fileOutput.add("managers=" + outString + "\n");
			fileOutput.add("creator=" + shop.getShopCreator() + "\n");
			fileOutput.add("position1=" + shop.getShopPosition1String() + "\n");
			fileOutput.add("position2=" + shop.getShopPosition2String() + "\n");
			fileOutput.add("unlimited-money=" + shop.getValueofUnlimitedMoney() + "\n");
			fileOutput.add("unlimited-stock=" + shop.getValueofUnlimitedStock() + "\n");
			
			for(String item: shop.getItems()) {
				int buyPrice = shop.getItemBuyPrice(item);
				int buySize = shop.itemBuyAmount(item);
				int sellPrice = shop.getItemSellPrice(item);
				int sellSize = shop.itemSellAmount(item);
				int stock = shop.getItemStock(item);
				int maxStock = shop.itemMaxStock(item);
				int[] itemInfo = LocalShops.itemList.getItemInfo(null, item);
				if(itemInfo == null) continue;
				//itemId=dataValue,buyPrice:buyStackSize,sellPrice:sellStackSize,stock
				fileOutput.add(itemInfo[0] + ":" + itemInfo[1] + "=" + buyPrice + ":" + buySize
						 + "," + sellPrice + ":" + sellSize + "," + stock + ":" + maxStock + "\n");
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

	public static boolean deleteShop(Shop shop) {
		long[] xyzA = shop.getLocation();
		BookmarkedResult res = new BookmarkedResult();
		
		res = LocalShops.cuboidTree.relatedSearch(res.bookmark, xyzA[0], xyzA[1], xyzA[2] );
		
		//get the shop's tree node and delete it
		for( PrimitiveCuboid shopLocation : res.results) {
			
			//for each shop that you find, check to see if we're already in it
			//this should only find one shop node
			if( shopLocation.name == null ) continue;
			if( !shopLocation.world.equalsIgnoreCase(shop.getWorldName())) continue;
			
			LocalShops.cuboidTree.delete(shopLocation);
		}
		
		//delete the file from the directory
		String filePath = LocalShops.folderPath + LocalShops.shopsPath + shop.getShopName() + ".shop";
		File shopFile = new File( filePath );
		shopFile.delete();

		//remove shop from data structure
		String name = shop.getShopName();
		shops.remove(name);
			
		return true;
	}
	
	private static boolean shopPositionOk( Shop shop, long[] xyzA, long[] xyzB ) {
		BookmarkedResult res = new BookmarkedResult();
		
		//make sure coords are in right order
		for( int i = 0; i < 3; i++) {
			if( xyzA[i] > xyzB[i]) {
				long temp = xyzA[i];
				xyzA[i] = xyzB[i];
				xyzB[i] = temp;
			}
		}
		
		//Need to test every position to account for variable shop sizes
		
		for( long x = xyzA[0]; x <= xyzB[0]; x++) {
			for( long z = xyzA[2]; z <= xyzB[2]; z++) {
				for( long y = xyzA[1]; y <= xyzB[1]; y++) {			
					res = LocalShops.cuboidTree.relatedSearch(res.bookmark, x, y, z );
					if( shopOverlaps(shop, res) ) return false;
				}
			}
		}
		return true;
	}
	
	private static boolean shopOverlaps( Shop shop, BookmarkedResult res ) {
		if( res.results.size() != 0 ) {
			for( PrimitiveCuboid foundShop : res.results) {
				if(foundShop.name != null) {
					if(foundShop.world.equalsIgnoreCase(shop.getWorldName())) {
					System.out.println("Could not create shop, it overlaps with " + foundShop.name );
					return true;
					}
				}
			}
		}
		return false;
	}
}
