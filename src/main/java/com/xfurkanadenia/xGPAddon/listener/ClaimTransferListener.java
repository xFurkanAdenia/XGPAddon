package com.xfurkanadenia.xGPAddon.listener;

import com.xfurkanadenia.xGPAddon.XGPAddon;
import com.xfurkanadenia.xGPAddon.model.NClaim;
import me.ryanhamshire.GriefPrevention.events.ClaimTransferEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ClaimTransferListener implements Listener {
    @EventHandler
    public void onClaimTransfer(final ClaimTransferEvent event) {
        NClaim claim = XGPAddon.getInstance().getDataManager().getClaim(event.getClaim());
        claim.setOwner(event.getNewOwner());

    }
}
