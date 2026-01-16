package com.xfurkanadenia.xGPAddon.manager;

import com.xfurkanadenia.xGPAddon.XGPAddon;
import com.xfurkanadenia.xGPAddon.model.NClaim;
import com.xfurkanadenia.xGPAddon.util.ClaimSettingsUtil;
import com.xfurkanadenia.xGPAddon.util.NLogger;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import java.util.UUID;

public class ClaimManager {

    public static void startAllClaims() {
        XGPAddon main = XGPAddon.getInstance();

        main.getDataManager().getCachedClaims().clear();
        main.getDataManager().getCachedClaims().addAll(GriefPrevention.instance.dataStore.getClaims());

        if(main.getConfig().getBoolean("settings.debug")){
            NLogger.info("All claims added to cache.");
            NLogger.info("Total claims: " + main.getDataManager().getCachedClaims().size());
        }
        for (Claim claim : GriefPrevention.instance.dataStore.getClaims()) {
            if (main.getDataManager().getClaim(claim) != null) {
                continue;
            }

            int defaultClaimTime = main.getConfig().getInt("settings.defaultClaimTime", 30) * 24 * 60 * 60;

            UUID ownerID = claim.getOwnerID();
            if (ownerID == null) {
                ownerID = UUID.fromString("00000000-0000-0000-0000-000000000000");
            }

            NClaim nclaim = new NClaim(claim, defaultClaimTime, "Claim-" + claim.getID(), claim.getID().toString(), ownerID, ClaimSettingsUtil.getDefaultClaimSettings());
            main.getDataManager().getClaims().add(nclaim);
        }

        main.getConfigurationManager().getClaimData().saveConfigurationSilent();
    }




}
