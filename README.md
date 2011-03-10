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
* created items.txt with unique names for each item and data combinaiton
* iConomy 4.0+ integration
* GroupManager 0.99c integrated

1.8
*admin overide to everything
*localshops.admin
*buy, sell, remove partial name matching is better (based on shop inventory not every available item)
*added command /shop add itemname
*added help for /shop set
*added /shop set max command
*added max stock level for items
*changed shop file to itemID,Data=buy,sell,stock format

Still TODO
-----------

* make sure every command has a response to the player
* Add shop move command
* be able to set unlimited stock
* add user tutorial
* minimum owner account balance for denying sale
* CbutD auto update integration?
* log transactions
* add/remove player to shop on create/destroy

Eventual Goal:
-----------
* Display shop inventories in chests and purchase through inventory screen ++bump