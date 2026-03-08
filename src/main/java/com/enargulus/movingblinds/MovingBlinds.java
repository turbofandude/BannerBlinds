package com.enargulus.movingblinds;

import org.bukkit.plugin.java.JavaPlugin;
import com.enargulus.movingblinds.managers.PluginManager;
import com.enargulus.movingblinds.listeners.PlayerListener;

public class MovingBlinds extends JavaPlugin {
    
    @Override
    public void onEnable() {
        
        // Initialize managers
        PluginManager.getInstance().initialize();
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        getLogger().info("MovingBlinds has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("MovingBlinds has been disabled!");
    }
    
}