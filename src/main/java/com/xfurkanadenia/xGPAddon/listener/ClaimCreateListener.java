package com.xfurkanadenia.xGPAddon.listener;

import com.xfurkanadenia.xGPAddon.LanguageLoader;
import com.xfurkanadenia.xGPAddon.XGPAddon;
import com.xfurkanadenia.xGPAddon.manager.DataManager;
import com.xfurkanadenia.xGPAddon.model.NClaim;
import com.xfurkanadenia.xGPAddon.util.ClaimSettingsUtil;
import com.xfurkanadenia.xGPAddon.util.ClaimUtils;
import com.xfurkanadenia.xGPAddon.util.NLogger;
import com.xfurkanadenia.xGPAddon.util.Utils;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.events.ClaimCreatedEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class ClaimCreateListener implements Listener {

    @EventHandler
    public void onClaim(ClaimCreatedEvent e){
        XGPAddon main = XGPAddon.getInstance();
        LanguageLoader languageLoader = main.getLanguageLoader();
        DataManager dataManager = main.getDataManager();
        Claim claim = e.getClaim();
        int claimUnclaimableRadius = main.getConfig().getInt("settings.claimUnclaimableRadius");
        Player player = ((Player) e.getCreator());
        if(!dataManager.hasClaimLimit(player.getUniqueId()) && !player.isOp()) {
            player.sendMessage(languageLoader.getWithPlaceholders("no-claim-limit", Map.of("%player%", player.getName())));
            e.setCancelled(true);
            return;
        }
        Claim nearbyClaim = (ClaimUtils.getClaimNearby(claim.getLesserBoundaryCorner(), claim.getGreaterBoundaryCorner(), claimUnclaimableRadius));

        if(nearbyClaim != null && !nearbyClaim.getOwnerName().equals(player.getName())) {
            player.sendMessage(languageLoader.getWithPlaceholders("cant-claim-nearby", getPlaceholders(Map.of("%radius%", String.valueOf(claimUnclaimableRadius)), dataManager.getClaim(nearbyClaim))));
            e.setCancelled(true);
            return;
        }
        int defaultClaimTime = main.getConfig().getInt("settings.defaultClaimTime", 30) * 24 * 60 * 60;
        NClaim nclaim = new NClaim(claim, defaultClaimTime, "Claim-" + claim.getID(), claim.getID().toString(), claim.getOwnerID(), ClaimSettingsUtil.getDefaultClaimSettings());
        nclaim.setClaimName(Utils.getFormatted(main.getConfig().getString("settings.claim-default-name", "Claim-%claim_id%"), (Player) e.getCreator(), getPlaceholders(Map.of(), nclaim)));
        dataManager.getClaims().add(nclaim);

        if(!main.getTaskClaimTimer().getExpiringClaims().contains(nclaim)){
            main.getTaskClaimTimer().getExpiringClaims().add(nclaim);
        }

        if(main.getConfig().getBoolean("settings.debug")){
            NLogger.info("NClaim created while ClaimCreatedEvent");
            NLogger.info("NClaim: " + nclaim.getClaim().getID() + " | " + nclaim.getOwner().toString() + " | " + nclaim.getClaimName() + " | " + nclaim.getTime());
        }
    }

    private Map<String, String> getPlaceholders(Map<String, String> placeholders, NClaim nclaim) {
        Map<String, String> map = new HashMap<>(placeholders);
        map.putAll(Utils.getClaimVariables(nclaim));
        return map;
    }



}
