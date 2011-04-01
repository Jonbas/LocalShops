package net.centerleft.localshops;

import org.bukkit.Material;

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
		
		if(properties.keyExists("move-cost")) {
			ShopData.moveCost = properties.getLong("move-cost");
		} else {
			properties.setLong("move-cost", ShopData.moveCost);
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
		
		if(properties.keyExists("max-width")) {
			ShopData.maxWidth = properties.getLong("max-width");
		} else {
			properties.setLong("max-width", ShopData.maxWidth);
		}
		
		if(properties.keyExists("max-height")) {
			ShopData.maxHeight = properties.getLong("max-height");
		} else {
			properties.setLong("max-height", ShopData.maxHeight);
		}
		
		if(properties.keyExists("log-transactions")) {
			ShopData.logTransactions = properties.getBoolean("log-transactions");
		} else {
			properties.setBoolean("log-transactions", ShopData.logTransactions);
		}
		
		if(properties.keyExists("max-damage")) {
			ShopData.maxDamage = properties.getInt("max-damage");
			if(ShopData.maxDamage < 0) ShopData.maxDamage = 0;
		} else {
			properties.setInt("max-damage", ShopData.maxDamage);
		}
	}
	
}
