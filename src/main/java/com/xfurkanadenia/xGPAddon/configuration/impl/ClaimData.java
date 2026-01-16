package com.xfurkanadenia.xGPAddon.configuration.impl;

import com.xfurkanadenia.xGPAddon.XGPAddon;
import com.xfurkanadenia.xGPAddon.configuration.NConfiguration;

public class ClaimData extends NConfiguration {

    public ClaimData() {
        super(XGPAddon.getInstance(), "claimdata.yml");
    }
}
