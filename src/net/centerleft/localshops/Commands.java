package net.centerleft.localshops;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cuboidLocale.PrimitiveCuboid;

public class Commands {
	static void createShop( CommandSender sender, String[] args ) {
		if(canUseCommand(sender, args) && args.length == 2) {
			//command format /shop create ShopName
			Player player = (Player)sender;
			Location location = player.getLocation();
			
			long x = (long)location.getX();
			long y = (long)location.getY();
			long z = (long)location.getZ();
			
			String shopName = args[1];
			
			Shop thisShop = new Shop();
			
			thisShop.setLocation(x, y, z);
			thisShop.setShopCreator(player.getName());
			thisShop.setShopName(shopName);
			thisShop.setWorldName(player.getWorld().getName());
			
			//setup the cuboid for the tree
			long[] xyzA = new long[3];
			long[] xyzB = new long[3];
			
			xyzA[0] = x - (ShopData.shopSize / 2);
			xyzB[0] = x + (ShopData.shopSize / 2);
			xyzA[2] = z - (ShopData.shopSize / 2);
			xyzB[2] = z + (ShopData.shopSize / 2);
			
			xyzA[1] = y - 1;
			xyzB[1] = y + ShopData.shopHeight - 1;

			PrimitiveCuboid tempShopCuboid = new PrimitiveCuboid( xyzA, xyzB );
			
			//write the file
			ShopData.saveShop(thisShop);
			//insert the shop into the world
			LocalShops.cuboidTree.insert(tempShopCuboid);
			ShopData.shops.put(shopName, thisShop );
			
		} 
	}
	
	static boolean canUseCommand( CommandSender sender, String[] args ) {
		//TODO add control tests
		return true;
	}

	public static void printHelp(CommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		sender.sendMessage("Looks like you need some help?");
		
	}
}
