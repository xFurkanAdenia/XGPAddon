package com.xfurkanadenia.xGPAddon.integration;

import com.xfurkanadenia.xGPAddon.XGPAddon;
import com.xfurkanadenia.xGPAddon.util.NLogger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultIntegration {

    private static Economy econ = null;
    private final XGPAddon main;

    public VaultIntegration(XGPAddon main){
        this.main = main;
        if(!setupEconomy()){
            NLogger.warn("Vault not found!");
            main.getServer().getPluginManager().disablePlugin(main);
        }
    }

    private boolean setupEconomy() {
        if (main.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = main.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEcon() {
        return econ;
    }
}
