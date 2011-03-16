package net.centerleft.localshops;

public class PluginProperties {
	public static void loadProperties(PropertyHandler properties) {
		if(properties.keyExists("charge-for-shop")) {
			ShopData.chargeForShop = properties.getBoolean("charge-for-shop");
		} else {
			properties.setBoolean("charge-for-shop", ShopData.chargeForShop);
		}
		
		if(properties.keyExists("shop-cost")) {
			ShopData.shopCost = properties.getLong("shop-cost");
		} else {
			properties.setLong("shop-cost", ShopData.shopCost);
		}
		
		if(properties.keyExists("shop-width")) {
			ShopData.shopSize = properties.getLong("shop-width");
		} else {
			properties.setLong("shop-width", ShopData.shopSize);
		}
		
		if(properties.keyExists("shop-height")) {
			ShopData.shopHeight = properties.getLong("shop-height");
		} else {
			properties.setLong("shop-height", ShopData.shopHeight);
		}
		
		if(properties.keyExists("log-transactions")) {
			ShopData.logTransactions = properties.getBoolean("log-transactions");
		} else {
			properties.setBoolean("log-transactions", ShopData.logTransactions);
		}
		
		if(properties.keyExists("min-durability")) {
			ShopData.minDurability = properties.getInt("min-durability");
			if(ShopData.minDurability > 15) {
				ShopData.minDurability = 15;
				properties.setInt("min-durability", ShopData.minDurability);
			}
		} else {
			properties.setInt("min-durability", ShopData.minDurability);
		}
	}
	
}
