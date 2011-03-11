Local Shops
======

A cuboid shop plugin for Bukkit
http://www.github.com/Bukkit

Implemented
-----------

* Shop Class
* Read shop data files and build hash table
* Add shops to cuboid tree
* check if user is in shop on move (fixed!)
* file output
* /shop create
* /shop list
* /shop list buy|sell
* /shop buy|sell
* /shop add
* /shop remove
* /shop set sell
* /shop set add
* /shop set manager
* /shop set owner
* /shop destroy
* created items.txt with unique names for each item and data combination
* iConomy 4.0+ integration
* GroupManager 0.99c integrated
* admin override to everything
* localshops.admin
* buy, sell, remove partial name matching is better (based on shop inventory not every available item)
* added command /shop add itemname
* added help for /shop set
* added /shop set max command
* added max stock level for items
* changed shop file to itemID,Data=buy,sell,stock format
* creates it's own items.txt file from inside the plugin
* CbutD integration added
* /shop reload gives response
* fixed bug with selling an item that doesn't exist
* fixed localshops.admin
* gave shop owner or managers to buy or sell if price is 0

**1.10**
* fixed adding and deleting shops conflicting worlds
* added check for shop entry on log in, reload, and create/destroy
* create shop overlap with other world problem
* fixed bug with items not in items.txt list
* added /shop set unlimited money|stock
* added /shop list info

Still TODO
-----------

* make sure every command has a response to the player
* Add shop move command
* minimum owner account balance for denying sale
* log transactions
* add messages when the amount of buy/sell changes because of money or space restrictions

* change command to /lshop
* change /shop set buy|sell

Eventual Goal:
-----------
* Display shop inventories in chests and purchase through inventory screen ++bump