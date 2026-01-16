package com.xfurkanadenia.xGPAddon.listener;

import com.xfurkanadenia.xGPAddon.XGPAddon;
import com.xfurkanadenia.xGPAddon.model.NClaim;
import com.xfurkanadenia.xGPAddon.util.NLogger;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.events.ClaimDeletedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ClaimDeleteListener implements Listener {

    @EventHandler
    public void onDelete(ClaimDeletedEvent e){
        Claim claim = e.getClaim();
        NClaim nclaim = XGPAddon.getInstance().getDataManager().getClaim(claim);
        //NGPAddon.getInstance().getConfigurationManager().getClaimData().getConfiguration().set("claims." + nclaim.getClaim().getID(), null);
        //NGPAddon.getInstance().getConfigurationManager().getClaimData().saveConfigurationSilent();
        if(XGPAddon.getInstance().getConfig().getBoolean("settings.debug")){
            NLogger.info("NClaim deleted while ClaimDeletedEvent");
            NLogger.info("NClaim: " + nclaim.getClaim().getID() + " | " + nclaim.getOwner().toString() + " | " + nclaim.getClaimName() + " | " + nclaim.getTime());
        }
        XGPAddon.getInstance().getDataManager().getClaims().remove(nclaim);

    }

}
