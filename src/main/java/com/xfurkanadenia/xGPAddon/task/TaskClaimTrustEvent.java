package com.xfurkanadenia.xGPAddon.task;

import com.xfurkanadenia.xGPAddon.XGPAddon;
import com.xfurkanadenia.xGPAddon.events.ClaimTrustEvent;
import com.xfurkanadenia.xGPAddon.model.ClaimTrustedSettings;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class TaskClaimTrustEvent extends BukkitRunnable {
    @Override
    public void run() {
        XGPAddon.getInstance().getDataManager().getClaims().forEach((claim) -> {
            Bukkit.getOnlinePlayers().forEach(p -> {
                Map<String, ClaimTrustedSettings> settings = claim.getClaimSettings().getTrustedSettings();
                ClaimTrustedSettings pSettings = settings.get(p.getUniqueId().toString());
                if(pSettings != null && !claim.getClaim().hasExplicitPermission(p, ClaimPermission.Access)) settings.remove(p.getUniqueId().toString());
                if(claim.getClaim().hasExplicitPermission(p, ClaimPermission.Build)) {
                    if(pSettings != null || claim.getOwner().equals(p.getUniqueId())) return;
                    XGPAddon.getInstance().getServer().getPluginManager().callEvent(new ClaimTrustEvent(claim, p));
                }
            });
        });
    }
}
