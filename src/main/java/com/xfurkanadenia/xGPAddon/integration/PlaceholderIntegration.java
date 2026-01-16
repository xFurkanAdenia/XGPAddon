package com.xfurkanadenia.xGPAddon.integration;

import com.xfurkanadenia.xGPAddon.LanguageLoader;
import com.xfurkanadenia.xGPAddon.XGPAddon;
import com.xfurkanadenia.xGPAddon.model.NClaim;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PlaceholderIntegration extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "xgpaddon";
    }

    @Override
    public @NotNull String getAuthor() {
        return "xFurkanAdenia";
    }

    @Override
    public @NotNull String getVersion() {
        return XGPAddon.getInstance().getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        params = params.toLowerCase();
        XGPAddon main = XGPAddon.getInstance();
        LanguageLoader languageLoader = main.getLanguageLoader();
        if(params.equalsIgnoreCase("claim_count")){
            return String.valueOf(XGPAddon.getInstance().getDataManager().getClaimCount(player.getUniqueId()));
        } else if(params.equalsIgnoreCase("claim_name")){
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), false, null);
            if(claim == null) return languageLoader.get("unowned-claim");
            NClaim nClaim = XGPAddon.getInstance().getDataManager().getClaim(claim);
            return nClaim.getClaimName();
        } else if (params.equalsIgnoreCase("claim_owner")){
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), false, null);
            if(claim == null) return "";
            NClaim nClaim = XGPAddon.getInstance().getDataManager().getClaim(claim);
            return Bukkit.getOfflinePlayer(nClaim.getOwner()).getName();
        } else if (params.equalsIgnoreCase("claim_time")){
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), false, null);
            if(claim == null) return "-1";
            NClaim nClaim = XGPAddon.getInstance().getDataManager().getClaim(claim);
            return String.valueOf(nClaim.getTime());
        } else if (params.equalsIgnoreCase("claim_time_formatted")){
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getLocation(), false, null);
            if(claim == null) return languageLoader.get("unowned-claim-time");
            NClaim nClaim = XGPAddon.getInstance().getDataManager().getClaim(claim);
            return LanguageLoader.formatTime(nClaim.getTime());
        }
        String[] args = params.split("_");
        // ngpaddon_remaining_<id>
        if(args.length == 2){
            if(args[0].equalsIgnoreCase("remaining")){
                if(args[1].equalsIgnoreCase("look")){
                    if(player.getTargetBlockExact(5) == null){
                        return "";
                    }
                    if(GriefPrevention.instance.dataStore.getClaimAt(player.getTargetBlockExact(5).getLocation(), true, null) == null){
                        return "";
                    }
                    Claim claim = GriefPrevention.instance.dataStore.getClaimAt(player.getTargetBlockExact(5).getLocation(), true, null);
                    NClaim nClaim = XGPAddon.getInstance().getDataManager().getClaim(claim);
                    return formatTime(nClaim.getTime());
                }
                Claim claim = GriefPrevention.instance.dataStore.getClaim(Long.parseLong(args[1]));
                NClaim nClaim = XGPAddon.getInstance().getDataManager().getClaim(claim);
                if(XGPAddon.getInstance().getDataManager().getClaims().contains(nClaim)){
                    return formatTime(XGPAddon.getInstance().getDataManager().getClaims().stream().filter(c -> Objects.equals(c.getClaim().getID(), claim.getID())).findFirst().get().getTime());
                }
            }
        }
        return "";
    }

    public static String formatTime(long seconds) {
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;

        StringBuilder formatted = new StringBuilder();

        if (days > 0) formatted.append(days).append(" " + XGPAddon.getInstance().getLanguageLoader().get("days") + " ");
        if (hours > 0) formatted.append(hours).append(" " + XGPAddon.getInstance().getLanguageLoader().get("hours") + " ");
        if (minutes > 0) formatted.append(minutes).append(" " + XGPAddon.getInstance().getLanguageLoader().get("minutes") + " ");

        return formatted.toString().trim();
    }


}
