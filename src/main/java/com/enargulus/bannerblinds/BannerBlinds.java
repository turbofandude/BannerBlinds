package com.enargulus.bannerblinds;

import org.bukkit.plugin.java.JavaPlugin;
import com.enargulus.bannerblinds.managers.PluginManager;
import com.enargulus.bannerblinds.listeners.PlayerListener;

public class BannerBlinds extends JavaPlugin {
    
    @Override
    public void onEnable() {
        
        // Initialize managers
        PluginManager.getInstance().initialize();
        
        getConfig().addDefault("usePermission", false);
        getConfig().addDefault("maxWidth", 100);
        getConfig().addDefault("maxHeight", 100);
        getConfig().options().copyDefaults(true);
        saveConfig();

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        getLogger().info("BannerBlinds has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("BannerBlinds has been disabled!");
    }
    
}