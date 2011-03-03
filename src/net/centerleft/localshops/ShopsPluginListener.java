package net.centerleft.localshops;

import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.event.server.PluginEvent;
import org.bukkit.event.server.ServerListener;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class ShopsPluginListener extends ServerListener {
	private final LocalShops plugin;
	public static iConomy iConomy;
	
	public static Permissions permissions;
	public static PermissionHandler gmPermissionCheck; 
	public static boolean usePermissions = false;
	public static boolean useiConomy = false;
	
	public ShopsPluginListener(LocalShops instance) { 
		plugin = instance;
	}

    @Override
    public void onPluginEnabled(PluginEvent event) {
        if(event.getPlugin().getDescription().getName().equals("iConomy")) {
            iConomy = (iConomy)event.getPlugin();
            System.out.println("LocalShops: Attached to iConomy.");
            useiConomy = true;
        }
        
        if(event.getPlugin().getDescription().getName().equals("Permissions")) {
        	permissions = (Permissions)event.getPlugin();
        	System.out.print("LocalShops: Attached to Permissions");
        	usePermissions = true;
            
            
        } 
    }
    
    @Override
    public void onPluginDisabled(PluginEvent event) {
    	if(event.getPlugin().getDescription().getName().equals("iConomy")) {
            iConomy = null;
            System.out.println("LocalShops: Lost connection to iConomy.");
            useiConomy = false;
    	}
        if(event.getPlugin().getDescription().getName().equals("Permissions")) {
        	permissions = (Permissions)event.getPlugin();
        	System.out.print("LocalShops: Lost connection to Permissions");
        	usePermissions = false;
        }
    }
}
