package com.xfurkanadenia.xGPAddon.listener;

import com.xfurkanadenia.xGPAddon.XGPAddon;
import com.xfurkanadenia.xGPAddon.events.ClaimTrustEvent;
import com.xfurkanadenia.xGPAddon.model.ClaimTrustedSettings;
import com.xfurkanadenia.xGPAddon.model.NClaim;
import com.xfurkanadenia.xGPAddon.util.ClaimSettingsUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Map;

public class ClaimTrustListener implements Listener {
    @EventHandler
    public void onClaimTrust(ClaimTrustEvent event) {
        NClaim claim = event.getClaim();
        claim.getClaimSettings().getTrustedSettings().put(event.getPlayer().getUniqueId().toString(), ClaimSettingsUtil.getDefaultClaimTrustedSettings());
    }
}
