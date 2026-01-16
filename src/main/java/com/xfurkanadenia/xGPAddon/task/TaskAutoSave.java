package com.xfurkanadenia.xGPAddon.task;

import com.xfurkanadenia.xGPAddon.XGPAddon;
import org.bukkit.scheduler.BukkitRunnable;

public class TaskAutoSave extends BukkitRunnable {

    @Override
    public void run() {
        XGPAddon.getInstance().getDataManager().saveClaims();
        XGPAddon.getInstance().getConfigurationManager().getClaimData().saveConfiguration();
    }


}
