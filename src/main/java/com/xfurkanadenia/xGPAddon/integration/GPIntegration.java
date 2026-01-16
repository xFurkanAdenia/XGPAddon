package com.xfurkanadenia.xGPAddon.integration;

import com.xfurkanadenia.xGPAddon.XGPAddon;
import com.xfurkanadenia.xGPAddon.util.NLogger;

public class GPIntegration {

    private final XGPAddon main;

    public GPIntegration(XGPAddon main) {
        this.main = main;
        if (!setupGriefPrevention()) {
            NLogger.error("GriefPrevention not found! Disabling plugin...");
            main.getServer().getPluginManager().disablePlugin(main);
        }
    }

    private boolean setupGriefPrevention() {
        return main.getServer().getPluginManager().getPlugin("GriefPrevention") != null;
    }

}
