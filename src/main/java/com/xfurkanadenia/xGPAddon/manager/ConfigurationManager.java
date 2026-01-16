package com.xfurkanadenia.xGPAddon.manager;

import com.xfurkanadenia.xGPAddon.configuration.NConfiguration;
import com.xfurkanadenia.xGPAddon.configuration.impl.ClaimData;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationManager {

    private final List<NConfiguration> configurations = new ArrayList<>();

    private final ClaimData claimData;

    public ConfigurationManager() {
        configurations.add(claimData = new ClaimData());
    }

    public void loadConfigurations() {
        for (NConfiguration configuration : configurations) {
            configuration.loadConfiguration();
        }
    }

    public void saveConfigurations() {
        for (NConfiguration configuration : configurations) {
            configuration.saveConfiguration();
        }
    }

    public ClaimData getClaimData() {
        return claimData;
    }
}
