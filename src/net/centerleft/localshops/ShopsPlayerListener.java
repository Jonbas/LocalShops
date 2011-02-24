package net.centerleft.localshops;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Handle events for all Player related events
 * 
 * @author Jonbas
 */
public class ShopsPlayerListener extends PlayerListener {
	private final LocalShops plugin;

	public ShopsPlayerListener(LocalShops instance) {
		plugin = instance;
	}

	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		World playerWorld = player.getWorld();
		String worldName = playerWorld.getName();
		
		
	}
}
